package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMergeSamples_opt {

	private int studyId = Integer.MIN_VALUE;
	private int rdMatrix1Id = Integer.MIN_VALUE;
	private int rdMatrix2Id = Integer.MIN_VALUE;
	private int wrMatrixId = Integer.MIN_VALUE;
	private String wrMatrixFriendlyName = "";
	private String wrMatrixDescription = "";
	private MatrixMetadata rdMatrix1Metadata = null;
	private MatrixMetadata rdMatrix2Metadata = null;
	private MatrixMetadata wrMatrixMetadata = null;
	private MarkerSet_opt rdwrMarkerSet1 = null;
	private MarkerSet_opt rdMarkerSet2 = null;
	private MarkerSet_opt wrMarkerSet = null;
	private SampleSet rdSampleSet1 = null;
	private SampleSet rdSampleSet2 = null;
	private SampleSet wrSampleSet = null;
	private static DbManager dBManager = null;

	/**
	 * This constructor to join 2 Matrices.
	 * The MarkerSet from the 1st Matrix will be used in the result Matrix.
	 * No new Markers from the 2nd Matrix will be added.
	 * Samples from the 2nd Matrix will be appended to the end of the SampleSet from the 1st Matrix.
	 * Duplicate Samples from the 2nd Matrix will overwrite Samples in the 1st Matrix
	 */
	public MatrixMergeSamples_opt(int _studyId,
			int _rdMatrix1Id,
			int _rdMatrix2Id,
			String _wrMatrixFriendlyName,
			String _wrMatrixDescription) throws IOException, InvalidRangeException {

		/////////// INIT EXTRACTOR OBJECTS //////////
		rdMatrix1Metadata = new MatrixMetadata(_rdMatrix1Id);
		rdMatrix2Metadata = new MatrixMetadata(_rdMatrix2Id);
		studyId = rdMatrix1Metadata.getStudyId();

		rdMatrix1Id = _rdMatrix1Id;
		rdMatrix2Id = _rdMatrix2Id;

		rdMatrix1Metadata = new MatrixMetadata(rdMatrix1Id);
		rdMatrix2Metadata = new MatrixMetadata(rdMatrix2Id);

		rdwrMarkerSet1 = new MarkerSet_opt(rdMatrix1Metadata.getStudyId(), rdMatrix1Id);
		rdMarkerSet2 = new MarkerSet_opt(rdMatrix2Metadata.getStudyId(), rdMatrix2Id);

		rdSampleSet1 = new SampleSet(rdMatrix1Metadata.getStudyId(), rdMatrix1Id);
		rdSampleSet2 = new SampleSet(rdMatrix2Metadata.getStudyId(), rdMatrix2Id);

		wrMatrixFriendlyName = _wrMatrixFriendlyName;
		wrMatrixDescription = _wrMatrixDescription;

	}

	public int appendSamplesKeepMarkersConstant() throws IOException, InvalidRangeException {
		int resultMatrixId = Integer.MIN_VALUE;

		NetcdfFile rdNcFile1 = NetcdfFile.open(rdMatrix1Metadata.getPathToMatrix());
		NetcdfFile rdNcFile2 = NetcdfFile.open(rdMatrix2Metadata.getPathToMatrix());


		//Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<String, Object> rdSampleSetLHM1 = rdSampleSet1.getSampleIdSetLHM();
		Map<String, Object> rdSampleSetLHM2 = rdSampleSet2.getSampleIdSetLHM();
		Map<String, Object> wrComboSampleSetLHM = getComboSampleSetwithPosArray(rdSampleSetLHM1, rdSampleSetLHM2);

		rdwrMarkerSet1.initFullMarkerIdSetLHM();
		rdMarkerSet2.initFullMarkerIdSetLHM();

		//RETRIEVE CHROMOSOMES INFO
//        rdwrMarkerSet1.fillMarkerSetLHMWithChrAndPos();
//        wrMarkerIdSetLHM = rdMarkerSet.replaceWithValuesFrom(wrMarkerIdSetLHM, rdMarkerSet.markerIdSetLHM);
//        rdChrInfoSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerIdSetLHM, 0, 1);

		Map<String, Object> rdChrInfoSetLHM = rdwrMarkerSet1.getChrInfoSetLHM();


		try {
			///////////// CREATE netCDF-3 FILE ////////////
			int hasDictionary = 0;
			if (rdMatrix1Metadata.getHasDictionray() == rdMatrix2Metadata.getHasDictionray()) {
				hasDictionary = rdMatrix1Metadata.getHasDictionray();
			}
			String gtEncoding = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString();
			if (rdMatrix1Metadata.getGenotypeEncoding().equals(rdMatrix2Metadata.getGenotypeEncoding())) {
				gtEncoding = rdMatrix1Metadata.getGenotypeEncoding();
			}
			String technology = cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString();
			if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
				technology = rdMatrix1Metadata.getTechnology();
			}

			StringBuilder descSB = new StringBuilder(Text.Matrix.descriptionHeader1);
			descSB.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			descSB.append("\n");
			descSB.append("Markers: ").append(rdSampleSetLHM1.size()).append(", Samples: ").append(wrComboSampleSetLHM.size());
			descSB.append("\n");
			descSB.append(Text.Trafo.mergedFrom);
			descSB.append("\nMX-");
			descSB.append(rdMatrix1Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix1Metadata.getMatrixFriendlyName());
			descSB.append("\nMX-");
			descSB.append(rdMatrix2Metadata.getMatrixId());
			descSB.append(" - ");
			descSB.append(rdMatrix2Metadata.getMatrixFriendlyName());
			descSB.append("\n\n");
			descSB.append("Merge Method - ");
			descSB.append(Text.Trafo.mergeSamplesOnly);
			descSB.append(":\n");
			descSB.append(Text.Trafo.mergeMethodSampleJoin);

			MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
					technology, //technology
					wrMatrixFriendlyName,
					wrMatrixDescription + "\n\n" + descSB.toString(), //description
					rdMatrix1Metadata.getStrand(),
					hasDictionary, //has dictionary?
					wrComboSampleSetLHM.size(), //Use comboed wrComboSampleSetLHM as SampleSet
					rdwrMarkerSet1.getMarkerSetSize(), //Keep rdwrMarkerIdSetLHM1 from Matrix1. MarkerSet is constant
					rdChrInfoSetLHM.size(),
					gtEncoding,
					rdMatrix1Id, //Parent matrixId 1
					rdMatrix2Id);         //Parent matrixId 2


			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException e) {
				System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			}
			//System.out.println("Done creating netCDF handle in MatrixSampleJoin_opt: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">

			//SAMPLESET
			ArrayChar.D2 samplesD2 = Utils.writeLHMKeysToD2ArrayChar(wrComboSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

			int[] sampleOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_SAMPLESET, sampleOrig, samplesD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}
			samplesD2 = null;
			System.out.println("Done writing SampleSet to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//Keep rdwrMarkerIdSetLHM1 from Matrix1 constant
			//MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}

			//MARKERSET RSID
			rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			//MARKERSET CHROMOSOME
			rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_CHR, cNetCDF.Strides.STRIDE_CHR);

			//Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			//Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[]{0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);


			//MARKERSET POSITION
			rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			//Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerIdSetLHM1, cNetCDF.Variables.VAR_MARKERS_POS, cNetCDF.Strides.STRIDE_POS);
			Utils.saveIntLHMD1ToWrMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_POS);


			//MARKERSET DICTIONARY ALLELES
			Attribute hasDictionary1 = rdNcFile1.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY);
			if ((Integer) hasDictionary1.getNumericValue() == 1) {
				rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
			}

			//GENOTYPE STRAND
			rdwrMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			Utils.saveCharLHMValueToWrMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), cNetCDF.Variables.VAR_GT_STRAND, 3);

			//</editor-fold>


			//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

			rdMarkerSet2.initFullMarkerIdSetLHM();

			//Iterate through wrSampleSetLHM, use item position to read correct sample GTs into rdMarkerIdSetLHM.
			for (Object value : wrComboSampleSetLHM.values()) {             //Next SampleId
				int[] sampleIndices = (int[]) value; //Next position[rdMatrixNb, rdPos, wrPos] to read/write

				//Iterate through wrMarkerIdSetLHM, get the correct GT from rdMarkerIdSetLHM
				if (sampleIndices[0] == 1) { //Read from Matrix1
					rdwrMarkerSet1.fillWith(org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT);
					rdwrMarkerSet1.fillGTsForCurrentSampleIntoInitLHM(sampleIndices[1]);
				}
				if (sampleIndices[0] == 2) { //Read from Matrix2
					rdwrMarkerSet1.fillWith(org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT);
					rdMarkerSet2.fillGTsForCurrentSampleIntoInitLHM(sampleIndices[1]);
					for (Map.Entry<String, Object> entry : rdwrMarkerSet1.getMarkerIdSetLHM().entrySet()) {
						String key = entry.getKey();
						if (rdMarkerSet2.getMarkerIdSetLHM().containsKey(key)) {
							Object markerValue = rdMarkerSet2.getMarkerIdSetLHM().get(key);
							entry.setValue(markerValue);
						}
					}
					//rdwrMarkerIdSetLHM1 = rdMarkerSet2.replaceWithValuesFrom(rdwrMarkerIdSetLHM1, rdMarkerIdSetLHM2);
				}

				//Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
				Utils.saveSingleSampleGTsToMatrix(wrNcFile, rdwrMarkerSet1.getMarkerIdSetLHM(), sampleIndices[2]);

			}

			//</editor-fold>


			// CLOSE THE FILE AND BY THIS, MAKE IT READ-ONLY
			try {
				//GENOTYPE ENCODING
				ArrayChar.D2 guessedGTCodeAC = new ArrayChar.D2(1, 8);
				Index index = guessedGTCodeAC.getIndex();
				guessedGTCodeAC.setString(index.set(0, 0), rdMatrix1Metadata.getGenotypeEncoding());
				int[] origin = new int[]{0, 0};
				wrNcFile.write(cNetCDF.Variables.GLOB_GTENCODING, origin, guessedGTCodeAC);

				descSB.append("\nGenotype encoding: ");
				descSB.append(rdMatrix1Metadata.getGenotypeEncoding());
				DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
				db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
						org.gwaspi.constants.cDBMatrix.T_MATRICES,
						new String[]{constants.cDBMatrix.f_DESCRIPTION},
						new Object[]{descSB.toString()},
						new String[]{constants.cDBMatrix.f_ID},
						new Object[]{resultMatrixId});

				resultMatrixId = wrMatrixHandler.getResultMatrixId();

				wrNcFile.close();
				rdNcFile1.close();
				rdNcFile2.close();

				//CHECK FOR MISMATCHES
				if (rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())
						|| rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O1234.toString())) {
					double[] mismatchState = checkForMismatches(wrMatrixHandler.getResultMatrixId()); //mismatchCount, mismatchRatio
					if (mismatchState[1] > 0.01) {
						System.out.println("Mismatch ratio is bigger that 1% (" + mismatchState[1] * 100 + " %)!\nThere might be an issue with strand positioning of your genotypes!");
						//resultMatrixId = new int[]{wrMatrixHandler.getResultMatrixId(),-4};  //The threshold of acceptable mismatching genotypes has been crossed
					}
				}

			} catch (IOException e) {
				System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			}

			org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");

		} catch (InvalidRangeException invalidRangeException) {
		} catch (IOException iOException) {
		}


		return resultMatrixId;
	}

	protected Map<String, Object> getComboSampleSetwithPosArray(Map<String, Object> sampleSetLHM1, Map<String, Object> sampleSetLHM2) {
		Map<String, Object> resultLHM = new LinkedHashMap<String, Object>();

		int wrPos = 0;
		int rdPos = 0;
		for (String key : sampleSetLHM1.keySet()) {
			int[] position = new int[]{1, rdPos, wrPos}; //rdMatrixNb, rdPos, wrPos
			resultLHM.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (String key : sampleSetLHM2.keySet()) {
			int[] position;
			//IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultLHM.containsKey(key)) {
				position = (int[]) resultLHM.get(key);
				position[0] = 2; //rdMatrixNb
				position[1] = rdPos; //rdPos
			} else {
				position = new int[]{2, rdPos, wrPos}; //rdMatrixNb, rdPos, wrPos
			}

			resultLHM.put(key, position);
			wrPos++;
			rdPos++;
		}

		return resultLHM;
	}

	protected double[] checkForMismatches(int wrMatrixId) throws IOException, InvalidRangeException {
		double[] result = new double[2];

		wrMatrixMetadata = new MatrixMetadata(wrMatrixId);
		wrSampleSet = new SampleSet(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet = new MarkerSet_opt(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet.initFullMarkerIdSetLHM();
		Map<String, Object> wrSampleSetLHM = wrSampleSet.getSampleIdSetLHM();

		NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

		// Iterate through markerset, take it marker by marker
		int markerNb = 0;
		double mismatchCount = 0;

		// Iterate through markerSet
		for (String markerId : wrMarkerSet.getMarkerIdSetLHM().keySet()) {
			Map<Character, Object> knownAlleles = new LinkedHashMap<Character, Object>();

			//Get a sampleset-full of GTs
			wrSampleSetLHM = wrSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, wrSampleSetLHM, markerNb);

			//Iterate through sampleSet
			for (Object value : wrSampleSetLHM.values()) {
				char[] tempGT = value.toString().toCharArray();

				//Gather alleles different from 0 into a list of known alleles and count the number of appearences
				if (tempGT[0] != '0') {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[0])) {
						tempCount = (Integer) knownAlleles.get(tempGT[0]);
					}
					knownAlleles.put(tempGT[0], tempCount + 1);
				}
				if (tempGT[1] != '0') {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[1])) {
						tempCount = (Integer) knownAlleles.get(tempGT[1]);
					}
					knownAlleles.put(tempGT[1], tempCount + 1);
				}
			}

			if (knownAlleles.size() > 2) {
				mismatchCount++;
			}

			markerNb++;
			if (markerNb % 100000 == 0) {
				System.out.println("Checking markers for mismatches: " + markerNb + " at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
			}
		}

		double mismatchRatio = mismatchCount / wrSampleSet.getSampleSetSize();
		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}
