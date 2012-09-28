package org.gwaspi.netCDF.loader;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBMatrix;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.Plink_Standard;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
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
public class LoadGTFromPlinkFlatFiles implements GenotypesLoader {

	public LoadGTFromPlinkFlatFiles() {
	}

	@Override
	public ImportFormat getFormat() {
		return ImportFormat.PLINK;
	}

	@Override
	public StrandType getMatrixStrand() {
		return null;
	}

	@Override
	public boolean isHasDictionary() {
		return false;
	}

	@Override
	public int getMarkersD2ItemNb() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public String getMarkersD2Variables() {
		throw new UnsupportedOperationException("Not supported yet."); // FIXME
	}

	@Override
	public int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException {
		int result = Integer.MIN_VALUE;

		String startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();

		List<String> sampleInfoList = new ArrayList<String>();
		sampleInfoList.addAll(sampleInfo.keySet());

		//<editor-fold defaultstate="collapsed/expanded" desc="CREATE MARKERSET & NETCDF">
		MetadataLoaderPlink markerSetLoader = new MetadataLoaderPlink(
				loadDescription.getGtDirPath(),
				loadDescription.getAnnotationFilePath(),
				loadDescription.getStrand(),
				loadDescription.getStudyId());
		Map<String, Object> markerSetMap = markerSetLoader.getSortedMarkerSetWithMetaData(); //markerid, rsId, chr, pos

		System.out.println("Done initializing sorted MarkerSetMap at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		///////////// CREATE netCDF-3 FILE ////////////
		StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
		descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!loadDescription.getDescription().isEmpty()) {
			descSB.append("\nDescription: ");
			descSB.append(loadDescription.getDescription());
			descSB.append("\n");
		}
//		descSB.append("\nStrand: ");
//		descSB.append(strand);
		descSB.append("\n");
		descSB.append("Markers: ").append(markerSetMap.size()).append(", Samples: ").append(sampleInfo.size());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader2);
		descSB.append(loadDescription.getFormat());
		descSB.append("\n");
		descSB.append(Text.Matrix.descriptionHeader3);
		descSB.append("\n");
		descSB.append(loadDescription.getGtDirPath());
		descSB.append(" (MAP file)\n");
		descSB.append(loadDescription.getAnnotationFilePath());
		descSB.append(" (PED file)\n");
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			descSB.append(loadDescription.getSampleFilePath());
			descSB.append(" (Sample Info file)\n");
		}

		//RETRIEVE CHROMOSOMES INFO
		Map<String, Object> chrSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(markerSetMap, 2, 3);

		MatrixFactory matrixFactory = new MatrixFactory(
				loadDescription.getStudyId(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				descSB.toString(), // description
				loadDescription.getGtCode(),
				(getMatrixStrand() != null) ? getMatrixStrand() : loadDescription.getStrand(),
				isHasDictionary(),
				sampleInfo.size(),
				markerSetMap.size(),
				chrSetMap.size(),
				loadDescription.getGtDirPath());

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
		// WRITE SAMPLESET TO MATRIX FROM SAMPLES LIST
		ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeCollectionToD2ArrayChar(sampleInfoList, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

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

		// WRITE RSID & MARKERID METADATA FROM METADATAMap
		ArrayChar.D2 markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);

		int[] markersOrig = new int[]{0, 0};
		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_RSID, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 0, cNetCDF.Strides.STRIDE_MARKER_NAME);
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
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(markerSetMap, 2, cNetCDF.Strides.STRIDE_CHR);

		try {
			ncfile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

		// Set of chromosomes found in matrix along with number of markersinfo
		org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(ncfile, chrSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);

		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[]{0, 1, 2, 3};
		org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(ncfile, chrSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);


		// WRITE POSITION METADATA FROM ANNOTATION FILE
		//markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD2ArrayChar(sortedMarkerSetMap, 3, cNetCDF.Strides.STRIDE_POS);
		ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeMapValueItemToD1ArrayInt(markerSetMap, 3);
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
		switch (loadDescription.getStrand()) {
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
		for (Map.Entry<String, Object> entry : markerSetMap.entrySet()) {
			entry.setValue(strandFlag);
		}
		markersD2 = org.gwaspi.netCDF.operations.Utils.writeMapValueToD2ArrayChar(markerSetMap, cNetCDF.Strides.STRIDE_STRAND);

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
		GenotypeEncoding guessedGTCode = GenotypeEncoding.UNKNOWN;
		System.out.println(Text.All.processing);
		Map<String, Object> mapMarkerSetMap = markerSetLoader.parseOrigMapFile(loadDescription.getGtDirPath());
		loadPedGenotypes(
				new File(loadDescription.getAnnotationFilePath()),
				ncfile,
				markerSetMap,
				mapMarkerSetMap,
				sampleInfoList,
				guessedGTCode);

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

		AbstractLoadGTFromFiles.logAsWhole(
				startTime,
				loadDescription.getStudyId(),
				loadDescription.getGtDirPath(),
				ImportFormat.PLINK,
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());

		org.gwaspi.global.Utils.sysoutCompleted("writing Genotypes to Matrix");
		return result;
	}

	public void loadPedGenotypes(File file,
			NetcdfFileWriteable ncfile,
			Map<String, Object> wrMarkerIdSetMap,
			Map<String, Object> mapMarkerSetMap,
			List<String> sampleInfoMap,
			GenotypeEncoding guessedGTCode)
			throws IOException, InvalidRangeException
	{
		FileReader inputFileReader = new FileReader(file);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		//GET ALLELES
		String l;
		while ((l = inputBufferReader.readLine()) != null) {
			//PURGE WRITE MARKER SET
			for (Iterator<String> it = wrMarkerIdSetMap.keySet().iterator(); it.hasNext();) {
				String markerId = it.next();
				wrMarkerIdSetMap.put(markerId, cNetCDF.Defaults.DEFAULT_GT);
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
			Iterator<String> it = mapMarkerSetMap.keySet().iterator();
			while (st.hasMoreTokens()) {
				String markerId = it.next();
				byte[] alleles = new byte[]{(byte) (st.nextToken().charAt(0)),
					(byte) (st.nextToken().charAt(0))};
				wrMarkerIdSetMap.put(markerId, alleles);
			}
			st = null;

			if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN)) {
				guessedGTCode = Utils.detectGTEncoding(wrMarkerIdSetMap);
			} else if (guessedGTCode.equals(cNetCDF.Defaults.GenotypeEncoding.O12)) {
				guessedGTCode = Utils.detectGTEncoding(wrMarkerIdSetMap);
			}

			/////////// WRITING GENOTYPE DATA INTO netCDF FILE ////////////
			int sampleIndex = sampleInfoMap.indexOf(sampleId);
			if (sampleIndex != -1) {  //CHECK IF CURRENT SAMPLE IS KNOWN IN SAMPLEINFO FILE!!
				org.gwaspi.netCDF.operations.Utils.saveSingleSampleGTsToMatrix(ncfile, wrMarkerIdSetMap, sampleIndex);
			}
		}
	}
}
