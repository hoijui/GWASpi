package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadGTFromPlinkFlatFiles implements GTFilesLoader {

	private String mapFilePath;
	private String sampleFilePath;
	private String pedFilePath;
	private List<String> sampleInfoLHM = new ArrayList<String>();
	private int studyId;
	private String format;
	private String strand;
	private String friendlyName;
	private String description;
	private String gtCode;
	private String matrixStrand;
	private int hasDictionary;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	//<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
	public LoadGTFromPlinkFlatFiles(String _mepFilePath,
			String _sampleFilePath,
			String _pedFilePath,
			int _studyId,
			String _strand,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoLHM)
	{
		mapFilePath = _mepFilePath;
		sampleFilePath = _sampleFilePath;
		pedFilePath = _pedFilePath;
		studyId = _studyId;
		format = cImport.ImportFormat.PLINK.toString();
		strand = _strand;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		matrixStrand = strand;
		hasDictionary = 0;
		description = _description;

		// GET SAMPLE LIST FROM INPUT FILE
		sampleInfoLHM.addAll(_sampleInfoLHM.keySet());
	}
	//</editor-fold>

	public int processData() throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();


		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderPlink markerSetLoader = new MetadataLoaderPlink(mapFilePath, pedFilePath, strand, studyId);
		Map<String, Object> markerSetLHM = markerSetLoader.getSortedMarkerSetWithMetaData(); //markerid, rsId, chr, pos

		System.out.println("Done initializing sorted MarkerSetLHM at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetLHM.size()).append(", Samples: ").append(sampleInfoLHM.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(format);
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(mapFilePath);
		descSB.append(" (MAP file)\n");
		descSB.append(pedFilePath);
		descSB.append(" (PED file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetLHM, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), //description
				gtCode,
				matrixStrand, // Affymetrix standard
				hasDictionary,
				sampleInfoLHM.size(),
				markerSetLHM.size(),
				chrSetLHM.size(),
				mapFilePath);

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeALToD2ArrayChar(sampleInfoLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

		int[] sampleOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		samplesD2 = null;
		System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE RSID & MARKERID METADATA FROM METADATALHM
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(markerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(markerSetLHM, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing MarkerId and RsId to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
		//Chromosome location for each marker
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(markerSetLHM, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(ncfile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(ncfile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(sortedMarkerSetLHM, 3, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(markerSetLHM, 3);
		int[] posOrig = new int[1];
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// WRITE GT STRAND FROM ANNOTATION FILE
		int[] gtOrig = new int[]{0, 0};
		String strandFlag;
		switch (cNetCDF.Defaults.StrandType.compareTo(strand)) {
			case PLUS:
				strandFlag = cImport.StrandFlags.strandPLS;
				break;
			case MINUS:
				strandFlag = cImport.StrandFlags.strandMIN;
				break;
			case FWD:
				strandFlag = cImport.StrandFlags.strandFWD;
				break;
			case REV:
				strandFlag = cImport.StrandFlags.strandREV;
				break;
			default:
				strandFlag = cImport.StrandFlags.strandUNK;
				break;
		}
		for (Map.Entry<String, Object> entry : markerSetLHM.entrySet()) {
			entry.setValue(strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueToD2ArrayChar(markerSetLHM, cNetCDF.Strides.STRIDE_STRAND);

		try {
			ncfile.write(cNetCDF.Variables.VAR_GT_STRAND, gtOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = null;
		System.out.println("Done writing strand info to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


		// </editor-fold>

		// <editor-fold defaultstate="collapsed" desc="MATRIX GENOTYPES LOAD ">
		System.out.println(Text.All.processing);
		Map<String, Object> mapMarkerSetLHM = markerSetLoader.parseOrigMapFile(mapFilePath);
		loadPedGenotypes(new File(pedFilePath),
				ncfile,
				markerSetLHM,
				mapMarkerSetLHM,
				sampleInfoLHM);

		System.out.println("Done writing genotypes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		// </editor-fold>

		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			//GUESS GENOTYPE ENCODING
			ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
			Index index = guessedGTCodeAC.getIndex();
			guessedGTCodeAC.setString(index.set(0, 0), guessedGTCode.toString().trim());
			int[] origin = new int[]{0, 0};
			ncfile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

			descSB.append("Genotype encoding: ");
			descSB.append(guessedGTCode);
			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			db.updateTable(cDBGWASpi.SCH_MATRICES,
					cDBMatrix.T_MATRICES,
					new String[]{cDBMatrix.f_DESCRIPTION},
					new Object[]{descSB.toString()},
					new String[]{cDBMatrix.f_ID},
					new Object[]{matrixFactory.getMatrixMetaData().getMatrixId()});

			//CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}

		AbstractLoadGTFromFiles.logAsWhole(startTime, studyId, mapFilePath, cImport.ImportFormat.PLINK.toString(), friendlyName, description);

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public void loadPedGenotypes(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> wrMarkerIdSetLHM,
			Map<String, Object> mapMarkerSetLHM,
			List<String> sampleInfoLHM) throws IOException, InvalidRangeException {

		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			//PURGE WRITE MARKER SET
			for (Iterator<String> it = wrMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
				String markerId = it.next();
				wrMarkerIdSetLHM.put(markerId, cNetCDF.Defaults.DEFAULT_GT);
			}

			StringTokenizer st = new StringTokenizer(l, cImport.Separators.separators_CommaSpaceTab_rgxp);

			//skip to genotype data
			String sampleId = "";
			int i = 0;
			while (i < Plink_Standard.ped_genotypes) {
				if (i == Plink_Standard.ped_sampleId) {
					sampleId = st.nextToken();
				} else {
					st.nextToken();
				}
				i++;
			}

			//read genotypes from this point on
			Iterator<String> it = mapMarkerSetLHM.keySet().iterator();
			while (st.hasMoreTokens()) {
				String markerId = it.next();
				byte[] alleles = new byte[]{(byte) (st.nextToken().charAt(0)),
					(byte) (st.nextToken().charAt(0))};
				wrMarkerIdSetLHM.put(markerId, alleles);
			}
			st = null;

			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(wrMarkerIdSetLHM);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(wrMarkerIdSetLHM);
			}

			/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
			int sampleIndex = sampleInfoLHM.indexOf(sampleId);
			if (sampleIndex != -1) {  //CHECK IF CURRENT SAMPLE IS KNOWN IN SAMPLEINFO FILE!!
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, wrMarkerIdSetLHM, sampleIndex);
			}
		}
	}
}
