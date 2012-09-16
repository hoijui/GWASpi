package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import ucar.ma2.ArrayByte;
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
public class LoadGTFromSequenomFiles implements GTFilesLoader {

	private String gtDirPath;
	private String sampleFilePath;
	private String annotationFilePath;
	private Map<String, Object> sampleInfoLHM;
	private Map<String, Object> markerSetLHM;
	private int studyId;
	private String format;
	private String friendlyName;
	private String description;
	private String gtCode;
	private cNetCDF.Defaults.GenotypeEncoding guessedGTCode = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN;

	// CONSTRUCTORS
	public LoadGTFromSequenomFiles(String _gtDirPath,
			String _sampleFilePath,
			String _annotationFilePath,
			int _studyId,
			String _format,
			String _friendlyName,
			String _gtCode,
			String _description,
			Map<String, Object> _sampleInfoLHM) {

		gtDirPath = _gtDirPath;
		sampleFilePath = _sampleFilePath;
		annotationFilePath = _annotationFilePath;
		studyId = _studyId;
		format = _format;
		friendlyName = _friendlyName;
		gtCode = _gtCode;
		description = _description;
		sampleInfoLHM = _sampleInfoLHM;

	}

	// METHODS
	public int processData() throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(gtDirPath, false);
//		File gtFileToImport = new File(gtDirPath);

		//<editor-fold defaultstate="collapsed" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderSequenom markerSetLoader = new MetadataLoaderSequenom(annotationFilePath, studyId);
		markerSetLHM = markerSetLoader.getSortedMarkerSetWithMetaData();

		System.out.println("Done initializing sorted MarkerSetLHM at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!description.isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(description);
			descSB.append("\n");
		}
//		descSB.append("\nGenotype encoding: ");
//		descSB.append(gtCode);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetLHM.size()).append(", Samples: ").append(sampleInfoLHM.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(format);
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(gtDirPath);
		descSB.append(" (Genotype files)\n");
		descSB.append("\n");
		descSB.append(annotationFilePath);
		descSB.append(" (Annotation file)\n");
		if (new File(sampleFilePath).exists()) {
			descSB.append(sampleFilePath);
			descSB.append(" (Sample Info file)\n");
		}

		// RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetLHM, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(studyId,
				format,
				friendlyName,
				descSB.toString(), // description
				gtCode,
				cNetCDF.Defaults.StrandType.PLSMIN.toString(),
				0,
				sampleInfoLHM.size(),
				markerSetLHM.size(),
				chrSetLHM.size(),
				gtDirPath);

		NetcdfFileWriteable ncfile = matrixFactory.getNetCDFHandler();

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
		//System.out.println("Done creating netCDF handle at "+global.Utils.getMediumDateTimeAsString());
		//</editor-fold>

		// <editor-fold defaultstate="collapsed" desc="WRITE MATRIX METADATA">
		//WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(sampleInfoLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(markerSetLHM, 1, cNetCDF.Strides.STRIDE_MARKER_NAME);

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
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(markerSetLHM, 5, cNetCDF.Strides.STRIDE_POS);
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
		for (Iterator<String> it = markerSetLHM.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			markerSetLHM.put(key, cNetCDF.Defaults.StrandType.FWD.toString());
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
		// INIT AND PURGE SORTEDMARKERSET LHM
		int sampleCounter = 0;
		for (Iterator<String> it = sampleInfoLHM.keySet().iterator(); it.hasNext();) {
			String sampleId = it.next();

			// PURGE MARKERSETLHM FOR CURRENT SAMPLE
			for (Iterator<String> it2 = markerSetLHM.keySet().iterator(); it2.hasNext();) {
				String key = it2.next().toString();
				markerSetLHM.put(key, cNetCDF.Defaults.DEFAULT_GT);
			}

			// PARSE ALL FILES FOR ANY DATA ON CURRENT SAMPLE
			for (int j = 0; j < gtFilesToImport.length; j++) {
				//System.out.println("Input file: "+i);
				loadIndividualFiles(gtFilesToImport[j],
						ncfile,
						markerSetLHM,
						sampleId.toString());
			}

			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(markerSetLHM);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(markerSetLHM);
			}

			/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
			ArrayByte.D3 genotypes = org.gwaspi.netCDF.operations.Utils.writeLHMToCurrentMarkerArrayByteD3(markerSetLHM, cNetCDF.Strides.STRIDE_GT);
			int[] origin = new int[]{sampleCounter, 0, 0}; //0,0,0 for 1st Sample ; 1,0,0 for 2nd Sample....
			try {
				ncfile.write(cNetCDF.Variables.VAR_GENOTYPES, origin, genotypes);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}

			if (sampleCounter == 0) {
				System.out.println(Text.All.processing);
			} else if (sampleCounter % 10 == 0) {
				System.out.println("Done processing sample Nº" + sampleCounter + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
			}
			sampleCounter++;
		}
		// </editor-fold>

		// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
		try {
			// GUESS GENOTYPE ENCODING
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

			// CLOSE FILE
			ncfile.close();
			result = matrixFactory.getMatrixMetaData().getMatrixId();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public void loadIndividualFiles(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> sortedMarkerSetLHM,
			String currentSampleId) throws IOException, InvalidRangeException {

		////////////// LOAD INPUT FILE ////////////////
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		// GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			if (!l.contains("SAMPLE_ID")) { // SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_Tab_rgxp);
				if (cVals[cImport.Genotypes.Sequenom.sampleId].equals(currentSampleId)) { //ONLY PROCESS CURRENT SAMPLEID DATA
					String tmpMarkerId = cVals[cImport.Genotypes.Sequenom.markerId].trim();
					try {
						Long.parseLong(tmpMarkerId);
						tmpMarkerId = "rs" + tmpMarkerId;
					} catch (Exception e) {
					}

					String sAlleles = cVals[cImport.Genotypes.Sequenom.alleles];
					if (sAlleles.length() == 0) {
						sAlleles = "00";
					} else if (sAlleles.length() == 1) {
						sAlleles = sAlleles + sAlleles;
					}
					byte[] tmpAlleles = new byte[]{(byte) sAlleles.charAt(0), (byte) sAlleles.charAt(1)};
					sortedMarkerSetLHM.put(tmpMarkerId, tmpAlleles);
				}
			}
		}
	}
}
