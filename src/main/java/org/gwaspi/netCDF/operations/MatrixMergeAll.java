package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
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
public class MatrixMergeAll {

	private static int studyId = Integer.MIN_VALUE;
	private static int rdMatrix1Id = Integer.MIN_VALUE;
	private static int rdMatrix2Id = Integer.MIN_VALUE;
	private static int wrMatrixId = Integer.MIN_VALUE;
	private static NetcdfFile rdNcFile1 = null;
	private static NetcdfFile rdNcFile2 = null;
	private static String wrMatrixFriendlyName = "";
	private static String wrMatrixDescription = "";
	private static MatrixMetadata rdMatrix1Metadata = null;
	private static MatrixMetadata rdMatrix2Metadata = null;
	private static MatrixMetadata wrMatrixMetadata = null;
	private static MarkerSet_opt rdMarkerSet1 = null;
	private static MarkerSet_opt rdMarkerSet2 = null;
	private static MarkerSet_opt wrMarkerSet = null;
	private static SampleSet rdSampleSet1 = null;
	private static SampleSet rdSampleSet2 = null;
	private static SampleSet wrSampleSet = null;

	/**
	 * This constructor to join 2 Matrices.
	 * The SampleSet from the 1st Matrix will be used in the result Matrix.
	 * No new Samples from the 2nd Matrix will be added.
	 * Markers from the 2nd Matrix will be mingled as per their chromosome
	 * and position to the MarkersSet from the 1st Matrix.
	 * Duplicate Markers from the 2nd Matrix will overwrite Markers in the 1st Matrix
	 */
	public MatrixMergeAll(int _studyId,
			int _rdMatrix1Id,
			int _rdMatrix2Id,
			String _wrMatrixFriendlyName,
			String _wrMatrixDescription) throws IOException, InvalidRangeException {

		/////////// INIT EXTRACTOR OBJECTS //////////
		studyId = _studyId;

		rdMatrix1Id = _rdMatrix1Id;
		rdMatrix2Id = _rdMatrix2Id;

		rdMatrix1Metadata = new MatrixMetadata(rdMatrix1Id);
		rdMatrix2Metadata = new MatrixMetadata(rdMatrix2Id);

		rdMarkerSet1 = new MarkerSet_opt(studyId, rdMatrix1Id);
		rdMarkerSet2 = new MarkerSet_opt(studyId, rdMatrix2Id);

		rdSampleSet1 = new SampleSet(studyId, rdMatrix1Id);
		rdSampleSet2 = new SampleSet(studyId, rdMatrix2Id);

		wrMatrixFriendlyName = _wrMatrixFriendlyName;
		wrMatrixDescription = _wrMatrixDescription;

		rdNcFile1 = NetcdfFile.open(rdMatrix1Metadata.getPathToMatrix());
		rdNcFile2 = NetcdfFile.open(rdMatrix2Metadata.getPathToMatrix());

	}

	public static int mingleMarkersKeepSamplesConstant() throws InvalidRangeException {
		int resultMatrixId = Integer.MIN_VALUE;

		LinkedHashMap wrComboSortedMarkerSetLHM = mingleAndSortMarkerSet();

		//RETRIEVE CHROMOSOMES INFO
		LinkedHashMap chrSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrComboSortedMarkerSetLHM, 0, 1);

		//Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		LinkedHashMap rdSampleSetLHM1 = rdSampleSet1.getSampleIdSetLHM();
		LinkedHashMap rdSampleSetLHM2 = rdSampleSet2.getSampleIdSetLHM();
		LinkedHashMap wrComboSampleSetLHM = getComboSampleSetWithIndicesArray(rdSampleSetLHM1, rdSampleSetLHM2);


		//<editor-fold defaultstate="collapsed" desc="CREATE MATRIX">
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
			descSB.append("Markers: ").append(wrComboSortedMarkerSetLHM.size()).append(", Samples: ").append(wrComboSampleSetLHM.size());
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
			descSB.append(Text.Trafo.mergeAll);
			descSB.append(":\n");
			descSB.append(Text.Trafo.mergeMethodMergeAll);

			MatrixFactory wrMatrixHandler = new MatrixFactory(studyId,
					technology, //technology
					wrMatrixFriendlyName,
					wrMatrixDescription + "\n\n" + descSB.toString(), //description
					rdMatrix1Metadata.getStrand(),
					hasDictionary, //has dictionary?
					wrComboSampleSetLHM.size(), //Comboed SampleSet
					wrComboSortedMarkerSetLHM.size(), //Use comboed wrSortedMingledMarkerLHM as MarkerSet
					chrSetLHM.size(),
					gtEncoding, //GT encoding
					rdMatrix1Id, //Parent matrixId 1
					rdMatrix2Id);                     //Parent matrixId 2

			resultMatrixId = wrMatrixHandler.getResultMatrixId();

			NetcdfFileWriteable wrNcFile = wrMatrixHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException e) {
				System.err.println("ERROR creating file " + wrNcFile.getLocation() + "\n" + e);
			}
			//System.out.println("Done creating netCDF handle in MatrixSampleJoin: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


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


			//MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrComboSortedMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERSET, markersOrig, markersD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}

			//WRITE CHROMOSOME METADATA FROM ANNOTATION FILE
			markersD2 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD2ArrayChar(wrComboSortedMarkerSetLHM, 0, cNetCDF.Strides.STRIDE_CHR);

			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_CHR, markersOrig, markersD2);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}
			System.out.println("Done writing chromosomes to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

			//Set of chromosomes found in matrix along with number of markersinfo
			org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrNcFile, chrSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
			//Number of marker per chromosome & max pos for each chromosome
			int[] columns = new int[]{0, 1, 2, 3};
			org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrNcFile, chrSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);

			//WRITE POSITION METADATA FROM ANNOTATION FILE
			ArrayInt.D1 markersPosD1 = org.gwaspi.netCDF.operations.Utils.writeLHMValueItemToD1ArrayInt(wrComboSortedMarkerSetLHM, 1);
			int[] posOrig = new int[1];
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_MARKERS_POS, posOrig, markersPosD1);
			} catch (IOException e) {
				System.err.println("ERROR writing file");
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}
			System.out.println("Done writing positions to matrix at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//<editor-fold defaultstate="collapsed" desc="GATHER METADATA FROM BOTH MATRICES">
			rdMarkerSet1.initFullMarkerIdSetLHM();
			rdMarkerSet2.initFullMarkerIdSetLHM();

			//<editor-fold defaultstate="collapsed" desc="MARKERSET RSID">

			rdMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator it = rdMarkerSet1.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = rdMarkerSet1.getMarkerIdSetLHM().get(key);
				wrComboSortedMarkerSetLHM.put(key, value);
			}
			rdMarkerSet2.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator it = rdMarkerSet2.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = rdMarkerSet2.getMarkerIdSetLHM().get(key);
				wrComboSortedMarkerSetLHM.put(key, value);
			}

			Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrComboSortedMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="MARKERSET DICTIONARY ALLELES">

			Attribute hasDictionary1 = rdNcFile1.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY);
			Attribute hasDictionary2 = rdNcFile2.findGlobalAttribute(cNetCDF.Attributes.GLOB_HAS_DICTIONARY);
			if ((Integer) hasDictionary1.getNumericValue() == 1
					&& (Integer) hasDictionary2.getNumericValue() == 1) {
				rdMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				for (Iterator it = rdMarkerSet1.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object value = rdMarkerSet1.getMarkerIdSetLHM().get(key);
					wrComboSortedMarkerSetLHM.put(key, value);
				}
				rdMarkerSet2.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
				for (Iterator it = rdMarkerSet2.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object value = rdMarkerSet2.getMarkerIdSetLHM().get(key);
					wrComboSortedMarkerSetLHM.put(key, value);
				}

				Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrComboSortedMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_BASES_DICT, cNetCDF.Strides.STRIDE_GT);
			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed/expanded" desc="GENOTYPE STRAND">

			rdMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			for (Iterator it = rdMarkerSet1.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = rdMarkerSet1.getMarkerIdSetLHM().get(key);
				wrComboSortedMarkerSetLHM.put(key, value);
			}
			rdMarkerSet2.fillInitLHMWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
			for (Iterator it = rdMarkerSet2.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = rdMarkerSet2.getMarkerIdSetLHM().get(key);
				wrComboSortedMarkerSetLHM.put(key, value);
			}

			Utils.saveCharLHMValueToWrMatrix(wrNcFile, wrComboSortedMarkerSetLHM, cNetCDF.Variables.VAR_GT_STRAND, 3);

			//</editor-fold>

			//</editor-fold>


			//</editor-fold>


			//<editor-fold defaultstate="collapsed" desc="GENOTYPES WRITER">

			//Get SampleId index from each Matrix
			//Iterate through wrSampleSetLHM
			int wrSampleIndex = 0;
			for (Iterator it = wrComboSampleSetLHM.keySet().iterator(); it.hasNext();) {
				Object sampleId = it.next();                 //Next SampleId
				int[] rdSampleIndices = (int[]) wrComboSampleSetLHM.get(sampleId); //Next position[rdPos matrix 1, rdPos matrix 2]

				//Read from Matrix1
				rdMarkerSet1.fillInitLHMWithMyValue(org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT);
				if (rdSampleSet1.getSampleIdSetLHM().containsKey(sampleId)) {
					rdMarkerSet1.fillGTsForCurrentSampleIntoInitLHM(rdSampleIndices[0]);
				}


				//Read from Matrix2
				rdMarkerSet2.fillInitLHMWithMyValue(org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT);

				if (rdSampleSet2.getSampleIdSetLHM().containsKey(sampleId)) {
					rdMarkerSet2.fillGTsForCurrentSampleIntoInitLHM(rdSampleIndices[1]);
				}


				//Fill wrSortedMingledMarkerLHM with matrix 1+2 Genotypes
				for (Iterator it3 = wrComboSortedMarkerSetLHM.keySet().iterator(); it3.hasNext();) {
					Object markerId = it3.next();
					byte[] genotype = org.gwaspi.constants.cNetCDF.Defaults.DEFAULT_GT;
					if (rdSampleSetLHM1.containsKey(sampleId) && rdMarkerSet1.getMarkerIdSetLHM().containsKey(markerId)) {
						genotype = (byte[]) rdMarkerSet1.getMarkerIdSetLHM().get(markerId);
					}
					if (rdSampleSetLHM2.containsKey(sampleId) && rdMarkerSet2.getMarkerIdSetLHM().containsKey(markerId)) {
						genotype = (byte[]) rdMarkerSet2.getMarkerIdSetLHM().get(markerId);
					}

					wrComboSortedMarkerSetLHM.put(markerId, genotype);
				}

				//Write wrMarkerIdSetLHM to A3 ArrayChar and save to wrMatrix
				Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrComboSortedMarkerSetLHM, wrSampleIndex);
				wrSampleIndex++;
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

				wrNcFile.close();
				rdNcFile1.close();
				rdNcFile2.close();


				// CHECK FOR MISMATCHES
				if (rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())
						|| rdMatrix1Metadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O1234.toString())) {
					double[] mismatchState = checkForMismatches(wrMatrixHandler.getResultMatrixId()); //mismatchCount, mismatchRatio
					if (mismatchState[1] > 0.01) {
						System.out.println("\n\nWARNING! Mismatch ratio is bigger that 1% (" + mismatchState[1] * 100 + " %)!\nThere might be an issue with strand positioning of your genotypes!\n\n");
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
		//</editor-fold>

		return resultMatrixId;
	}

	protected static LinkedHashMap mingleAndSortMarkerSet() {

		//GET 1st MATRIX LHM WITH CHR AND POS
		LinkedHashMap workLHM = new LinkedHashMap();
		rdMarkerSet1.initFullMarkerIdSetLHM();
		rdMarkerSet2.initFullMarkerIdSetLHM();
		rdMarkerSet1.fillInitLHMWithMyValue("");
		rdMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		workLHM.putAll(rdMarkerSet1.getMarkerIdSetLHM());
		rdMarkerSet1.fillInitLHMWithMyValue("");
		rdMarkerSet1.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Iterator it = workLHM.keySet().iterator(); it.hasNext();) {
			Object markerId = it.next();
			String chr = workLHM.get(markerId).toString();
			String pos = rdMarkerSet1.getMarkerIdSetLHM().get(markerId).toString();
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);
			workLHM.put(markerId, sbKey.toString());
		}
		if (rdMarkerSet1.getMarkerIdSetLHM() != null) {
			rdMarkerSet1.getMarkerIdSetLHM().clear();
		}

		//GET 2nd MATRIX LHM WITH CHR AND POS
		LinkedHashMap workLHM2 = new LinkedHashMap();
		rdMarkerSet2.fillInitLHMWithMyValue("");
		rdMarkerSet2.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		workLHM2.putAll(rdMarkerSet2.getMarkerIdSetLHM());
		rdMarkerSet2.fillInitLHMWithMyValue("");
		rdMarkerSet2.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Iterator it = workLHM2.keySet().iterator(); it.hasNext();) {
			Object markerId = it.next();
			String chr = workLHM2.get(markerId).toString();
			String pos = rdMarkerSet2.getMarkerIdSetLHM().get(markerId).toString();
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerId);
			workLHM2.put(markerId, sbKey.toString());
		}
		if (rdMarkerSet2.getMarkerIdSetLHM() != null) {
			rdMarkerSet2.getMarkerIdSetLHM().clear();
		}

		workLHM.putAll(workLHM2);


		//SORT MERGED LHM
		TreeMap sortedMetadataTM = new TreeMap(new org.gwaspi.netCDF.loader.ComparatorChrAutPosMarkerIdAsc());
		for (Iterator it = workLHM.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object value = workLHM.get(key);
			sortedMetadataTM.put(value, key);
		}
		if (workLHM != null) {
			workLHM.clear();
		}

		//PACKAGE IN AN LHM

		for (Iterator it = sortedMetadataTM.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			Object[] markerInfo = new Object[2];
			markerInfo[0] = keyValues[0]; // => chr
			markerInfo[1] = Integer.parseInt(keyValues[1]); // => pos

			Object markerId = sortedMetadataTM.get(key);
			workLHM.put(markerId, markerInfo);
		}

		return workLHM;
	}

	protected static LinkedHashMap getSampleSetWithIndicesLHM(LinkedHashMap sampleSetLHM1, LinkedHashMap sampleSetLHM2) {
		LinkedHashMap resultLHM = new LinkedHashMap();

		int rdPos = 0;
		for (Iterator it = sampleSetLHM1.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			int[] position = new int[]{rdPos, 0}; // rdPos matrix 1
			resultLHM.put(key, position);
			rdPos++;
		}

		rdPos = 0;
		for (Iterator it = sampleSetLHM2.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultLHM.containsKey(key)) {
				int[] position = (int[]) resultLHM.get(key);
				position[1] = rdPos; // rdPos matrix 2
				resultLHM.put(key, position);
			}

			rdPos++;
		}

		return resultLHM;
	}

	protected static LinkedHashMap getComboSampleSetWithIndicesArray(LinkedHashMap sampleSetLHM1, LinkedHashMap sampleSetLHM2) {
		LinkedHashMap resultLHM = new LinkedHashMap();

		int wrPos = 0;
		int rdPos = 0;
		for (Iterator it = sampleSetLHM1.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			int[] position = new int[]{1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultLHM.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (Iterator it = sampleSetLHM2.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			int[] position = new int[3];
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultLHM.containsKey(key)) {
				position = (int[]) resultLHM.get(key);
				position[0] = 2; //rdMatrixNb
				position[1] = rdPos; //rdPos
			} else {
				position = new int[]{2, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			}

			resultLHM.put(key, position);
			wrPos++;
			rdPos++;
		}

		return resultLHM;
	}

	protected static double[] checkForMismatches(int wrMatrixId) throws IOException, InvalidRangeException {
		double[] result = new double[2];

		wrMatrixMetadata = new MatrixMetadata(wrMatrixId);
		wrSampleSet = new SampleSet(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet = new MarkerSet_opt(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet.initFullMarkerIdSetLHM();
		LinkedHashMap wrSampleSetLHM = wrSampleSet.getSampleIdSetLHM();

		NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

		// Iterate through markerset, take it marker by marker
		int markerNb = 0;
		double mismatchCount = 0;

		// Iterate through markerSet
		for (Iterator it = wrMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
			Object markerId = it.next();
			LinkedHashMap knownAlleles = new LinkedHashMap();

			// Get a sampleset-full of GTs
			wrSampleSetLHM = wrSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, wrSampleSetLHM, markerNb);

			// Iterate through sampleSet
			for (Iterator it2 = wrSampleSetLHM.keySet().iterator(); it2.hasNext();) {

				Object sampleId = it2.next();

				char[] tempGT = wrSampleSetLHM.get(sampleId).toString().toCharArray();

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
