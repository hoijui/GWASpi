/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.operations.combi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.math.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.GenotypeComparator;
import org.gwaspi.global.Text;
import org.gwaspi.model.Genotype;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.netCDF.operations.Utils;
import org.gwaspi.samples.SampleSet;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public class CombiTestMatrixOperation implements MatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(CombiTestMatrixOperation.class);

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final CombiTestParams params;

	public CombiTestMatrixOperation(CombiTestParams params) {

		this.params = params;
	}

//	/**
//	 * Performs the Allelic or Genotypic Association Tests.
//	 * @param wrNcFile
//	 * @param wrCaseMarkerIdSetMap
//	 * @param wrCtrlMarkerSet
//	 */
//	protected void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, int[]> wrCaseMarkerIdSetMap, Map<MarkerKey, int[]> wrCtrlMarkerSet) {
//
////		svm_problem prob = new svm_problem();
////		prob.l = wrCaseMarkerIdSetMap.size();
////		prob.x = new svm_node[prob.l][];
////		for (int i = 0; i < prob.l; i++) {
////			prob.x[i] = vx.get(i);
////		}
////		prob.y = new double[prob.l];
////		for (int i = 0; i < prob.l; i++) {
////			prob.y[i] = vy.get(i);
////		}
//
//
//
//		// Iterate through markerset
//		int markerNb = 0;
//		Map<MarkerKey, Double[]> result = new LinkedHashMap<MarkerKey, Double[]>(wrCaseMarkerIdSetMap.size());
//		for (Map.Entry<MarkerKey, int[]> entry : wrCaseMarkerIdSetMap.entrySet()) {
//			MarkerKey markerKey = entry.getKey();
//
//			int[] caseCntgTable = entry.getValue();
//			int[] ctrlCntgTable = wrCtrlMarkerSet.get(markerKey);
//
//			// INIT VALUES
//			int caseAA = caseCntgTable[0];
//			int caseAa = caseCntgTable[1];
//			int caseaa = caseCntgTable[2];
//			int caseTot = caseAA + caseaa + caseAa;
//
//			int ctrlAA = ctrlCntgTable[0];
//			int ctrlAa = ctrlCntgTable[1];
//			int ctrlaa = ctrlCntgTable[2];
//			int ctrlTot = ctrlAA + ctrlaa + ctrlAa;
//
//			Double[] store;
//			if (allelic) {
//				// allelic test
//				int sampleNb = caseTot + ctrlTot;
//
//				double allelicT = Associations.calculateAllelicAssociationChiSquare(
//						sampleNb,
//						caseAA,
//						caseAa,
//						caseaa,
//						caseTot,
//						ctrlAA,
//						ctrlAa,
//						ctrlaa,
//						ctrlTot);
//				double allelicPval = Pvalue.calculatePvalueFromChiSqr(allelicT, 1);
//
//				double allelicOR = Associations.calculateAllelicAssociationOR(
//						caseAA,
//						caseAa,
//						caseaa,
//						ctrlAA,
//						ctrlAa,
//						ctrlaa);
//
//				store = new Double[3];
//				store[0] = allelicT;
//				store[1] = allelicPval;
//				store[2] = allelicOR;
//			} else {
//				// genotypic test
//				double gntypT = Associations.calculateGenotypicAssociationChiSquare(
//						caseAA,
//						caseAa,
//						caseaa,
//						caseTot,
//						ctrlAA,
//						ctrlAa,
//						ctrlaa,
//						ctrlTot);
//				double gntypPval = Pvalue.calculatePvalueFromChiSqr(gntypT, 2);
//				double[] gntypOR = Associations.calculateGenotypicAssociationOR(
//						caseAA,
//						caseAa,
//						caseaa,
//						ctrlAA,
//						ctrlAa,
//						ctrlaa);
//
//				store = new Double[4];
//				store[0] = gntypT;
//				store[1] = gntypPval;
//				store[2] = gntypOR[0];
//				store[3] = gntypOR[1];
//			}
//			result.put(markerKey, store); // store P-value and stuff
//
//			markerNb++;
//			if (markerNb % 100000 == 0) {
//				log.info("Processed {} markers", markerNb);
//			}
//		}
//
//		//<editor-fold defaultstate="expanded" desc="ALLELICTEST DATA WRITER">
//		int[] boxes;
//		String variableName;
//		if (allelic) {
//			boxes = new int[] {0, 1, 2};
//			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR;
//		} else {
//			boxes = new int[] {0, 1, 2, 3};
//			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
//		}
//		Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, result, boxes, variableName);
//		//</editor-fold>
//	}

//	private static class SampleInfosFetcher {
//
//		private final SampleSet sampleSet;
//		private final NetcdfFile netCdfFile;
//		private final List<MarkerKey> markerKeys;
//		private int nextMarker;
//		private Map<SampleKey, byte[]> samples;
//
//		SampleInfosFetcher(MatrixKey matrixKey) throws IOException, InvalidRangeException {
//
//			int readStudyId = matrixKey.getStudyKey().getId();
//			int readMatrixId = matrixKey.getId();
//			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(readMatrixId);
////			netCdfFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
////
////			MarkerSet rdMarkerSet = new MarkerSet(readStudyId, readMatrixId);
////			rdMarkerSet.initFullMarkerIdSetMap();
////	//		rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(readStudyId);
////	//		rdMarkerSet.fillWith(cNetCDF.Defaults.DEFAULT_GT);
////	//
////	//		Map<MarkerKey, byte[]> wrMarkerSetMap = new LinkedHashMap<MarkerKey, byte[]>();
////	//		wrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMapByteArray());
////
//			sampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), readMatrixId);
//			samples = sampleSet.getSampleIdSetMapByteArray();
////			// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
//			Set<SampleKey> sampleKeys = sampleSet.getSampleIdSetMapByteArray().keySet();
////			// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
////			markerKeys = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerIdSetMapInteger().keySet());
//
//			List<SampleInfo> allSampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(readStudyId);
//			Map<SampleKey, SampleInfo> sampleInfos
//					= new LinkedHashMap<SampleKey, SampleInfo>(allSampleInfos.size());
//			for (SampleInfo sampleInfo : allSampleInfos) {
//				sampleInfos.put(sampleInfo.getKey(), sampleInfo);
//			}
//			Map<SampleKey, Double> affectionStates
//					= new LinkedHashMap<SampleKey, Double>(allSampleInfos.size());
//			// we iterate over sampleKeys now, to get the correct order
//			for (SampleKey sampleKey : sampleKeys) {
//				Affection affection = sampleInfos.get(sampleKey).getAffection();
//				if (affection == Affection.UNKNOWN) {
//					throw new RuntimeException("Should we filter this out beforehand?");
//				}
//	//			System.err.println("\tlabel: " + affection);
//				Double encodedDisease = affection.equals(Affection.AFFECTED) ? 1.0 : 0.0; // XXX or should it be -1.0 instead of 0.0?
//				affectionStates.put(sampleKey, encodedDisease);
//			}
//
//			nextMarker = 0;
//		}
//	}

	/** TODO move into MarkerSet ? */
	private static class MatrixSamples implements
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>>, // HACK
			Iterator<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>>
	{
		private final MatrixKey matrixKey;
		private final SampleSet sampleSet;
		private final NetcdfFile netCdfFile;
		private final List<MarkerKey> markerKeys;
		private Set<SampleKey> sampleKeys;
		private int nextMarker;
		private Map<SampleKey, SampleInfo> sampleInfos;

		MatrixSamples(MatrixKey matrixKey) throws IOException, InvalidRangeException {

			this.matrixKey = matrixKey;
			int studyId = matrixKey.getStudyId();
			int matrixId = matrixKey.getMatrixId();
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);
			netCdfFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

			MarkerSet rdMarkerSet = new MarkerSet(matrixKey);
			rdMarkerSet.initFullMarkerIdSetMap();
	//		rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(readStudyId);
	//		rdMarkerSet.fillWith(cNetCDF.Defaults.DEFAULT_GT);
	//
	//		Map<MarkerKey, byte[]> wrMarkerSetMap = new LinkedHashMap<MarkerKey, byte[]>();
	//		wrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMapByteArray());

			sampleSet = new SampleSet(matrixKey);
//			samples = sampleSet.getSampleIdSetMapByteArray();
			sampleKeys = sampleSet.getSampleKeys();
			// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
			markerKeys = new ArrayList<MarkerKey>(rdMarkerSet.getMarkerIdSetMapInteger().keySet());

			nextMarker = 0;
			sampleInfos = retrieveSampleInfos();
		}

		private Map<SampleKey, SampleInfo> retrieveSampleInfos() throws IOException, InvalidRangeException {

			// This one has to be ordered! (and it is, due to the map being a LinkedHashMap)
			Set<SampleKey> sampleKeysOrdered = sampleSet.getSampleIdSetMapByteArray().keySet();

			List<SampleInfo> allSampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(matrixKey.getStudyKey());
			Map<SampleKey, SampleInfo> sampleInfosUnordered = new LinkedHashMap<SampleKey, SampleInfo>(allSampleInfos.size());
			for (SampleInfo sampleInfo : allSampleInfos) {
				sampleInfosUnordered.put(sampleInfo.getKey(), sampleInfo);
			}

			// we use LinkedHashMap for retainig the order of input
			Map<SampleKey, SampleInfo> localSampleInfos = new LinkedHashMap<SampleKey, SampleInfo>(sampleInfosUnordered.size());
			for (SampleKey sampleKey : sampleKeysOrdered) {
				localSampleInfos.put(sampleKey, sampleInfosUnordered.get(sampleKey));
			}

			return localSampleInfos;
		}

		public Map<SampleKey, SampleInfo> getSampleInfos() {
			return sampleInfos;
		}

		public List<MarkerKey> getMarkerKeys() {
			return markerKeys;
		}

		@Override
		public boolean hasNext() {
			return nextMarker < markerKeys.size();
		}

		@Override
		public Map.Entry<MarkerKey, Map<SampleKey, byte[]>> next() {

			Map<SampleKey, byte[]> samples
					= new LinkedHashMap<SampleKey, byte[]>(sampleKeys.size());
			for (SampleKey sampleKey : sampleKeys) {
				samples.put(sampleKey, null);
			}
			try {
				sampleSet.readAllSamplesGTsFromCurrentMarkerToMap(netCdfFile, samples, nextMarker);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			Map.Entry<MarkerKey, Map<SampleKey, byte[]>> next
					= Collections.singletonMap(markerKeys.get(nextMarker), samples).entrySet().iterator().next();
			nextMarker++;

			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"We do not support removing elements (from the persistent storage) through this iterator.");
		}

		public Iterator<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> iterator() {
			return this; // FIXME HACK Will fail if iterated a second time!
		}
	}

	public void TESTING() throws IOException, InvalidRangeException {

//		svm_problem prob = new svm_problem();
//		prob.l = wrCaseMarkerIdSetMap.size();
//		prob.x = new svm_node[prob.l][];
//		for (int i = 0; i < prob.l; i++) {
//			prob.x[i] = vx.get(i);
//		}
//		prob.y = new double[prob.l];
//		for (int i = 0; i < prob.l; i++) {
//			prob.y[i] = vy.get(i);
//		}


//		int resultOpId = Integer.MIN_VALUE;
//
//		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markerCensusOP.getId());
//		NetcdfFile rdNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
//
//		MarkerOperationSet rdOperationSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), markerCensusOP.getId());
//		Map<MarkerKey, char[]> rdMarkerSetMap = rdOperationSet.getOpSetMap();
//		Map<SampleKey, ?> rdSampleSetMap = rdOperationSet.getImplicitSetMap();
//
//		NetcdfFileWriteable wrNcFile = null;
//		try {
//			// CREATE netCDF-3 FILE
//
//			OperationFactory wrOPHandler = new OperationFactory(
//					rdOPMetadata.getStudyId(),
//					"Combi_" + censusName, // friendly name
//					"Combi test on Samples marked as controls (only females for the X chromosome)\nMarkers: " + rdMarkerSetMap.size() + "\nSamples: " + rdSampleSetMap.size(), //description
//					rdMarkerSetMap.size(),
//					rdSampleSetMap.size(),
//					0,
//					OPType.COMBI_ASSOC_TEST,
//					rdOPMetadata.getParentMatrixId(), // Parent matrixId
//					markerCensusOP.getId()); // Parent operationId
//			wrNcFile = wrOPHandler.getNetCDFHandler();
//
//			try {
//				wrNcFile.create();
//			} catch (IOException ex) {
//				log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
//			}
//
//			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
//			// MARKERSET MARKERID
//			ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(rdMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
//			int[] markersOrig = new int[] {0, 0};
//			try {
//				wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
//			} catch (IOException ex) {
//				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
//			} catch (InvalidRangeException ex) {
//				log.error(null, ex);
//			}
//
//			// MARKERSET RSID
//			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
//			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//
//			// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
//			ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//
//			int[] sampleOrig = new int[]{0, 0};
//			try {
//				wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
//			} catch (IOException ex) {
//				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
//			} catch (InvalidRangeException ex) {
//				log.error(null, ex);
//			}
//			log.info("Done writing SampleSet to matrix");
//			//</editor-fold>
//
//			//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM HW">
//			Map<MarkerKey, int[]> markersCensus;
////			// PROCESS ALL SAMPLES
////			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
////			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
////			performHardyWeinberg(wrNcFile, markersCensus, "ALL");
////
////			// PROCESS CASE SAMPLES
////			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
////			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
////			performHardyWeinberg(wrNcFile, markersCensus, "CASE");
//
//			// PROCESS CONTROL SAMPLES
//			log.info(Text.All.processing);
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
//			performHardyWeinberg(wrNcFile, markersCensus, "CTRL");
//
//			// PROCESS ALTERNATE HW SAMPLES
//			log.info(Text.All.processing);
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
//			performHardyWeinberg(wrNcFile, markersCensus, "HW-ALT");
//			//</editor-fold>
//
//			resultOpId = wrOPHandler.getResultOPId();
//			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
//		} catch (InvalidRangeException ex) {
//			log.error(null, ex);
//		} catch (IOException ex) {
//			log.error(null, ex);
//		} finally {
//			if (null != rdNcFile) {
//				try {
//					rdNcFile.close();
//					wrNcFile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file", ex);
//				}
//			}
//		}
//
//		return resultOpId;
	}

	@Override
	public int processMatrix() throws IOException, InvalidRangeException {
		System.out.println("XXX Combi-test Start");

		MatrixSamples matrixSamples = new MatrixSamples(params.getMatrixKey());
		Map<SampleKey, SampleInfo> sampleInfos = matrixSamples.getSampleInfos();

		// dimensions of the samples(-space) == #markers (== #SNPs)
		int dSamples = matrixSamples.getMarkerKeys().size();
		// dimensions of the encoded samples(-space) == #markers * encoding-factor
		int dEncoded = dSamples * params.getEncoder().getEncodingFactor();
		int n = sampleInfos.size();

		storeForEncoding(matrixSamples, sampleInfos, dSamples, dEncoded, n);

		return Integer.MIN_VALUE;
	}

	private static Map<SampleKey, Double> encodeAffectionStates(final Map<SampleKey, SampleInfo> sampleInfos, int n) {

		// we use LinkedHashMap to preserve the inut order
		Map<SampleKey, Double> affectionStates
				= new LinkedHashMap<SampleKey, Double>(n);
		// we iterate over sampleKeys now, to get the correct order
		for (SampleInfo sampleInfo : sampleInfos.values()) {
			Affection affection = sampleInfo.getAffection();
			if (affection == Affection.UNKNOWN) {
				throw new RuntimeException("Should we filter this out beforehand?");
			}
			Double encodedDisease = affection.equals(Affection.AFFECTED) ? 1.0 : -1.0; // XXX or should it be 0.0 instead of -1.0?
			affectionStates.put(sampleInfo.getKey(), encodedDisease);
		}

		return affectionStates;
	}

	private static Map<SampleKey, List<Double>> encodeSamples(
			Iterable<Map.Entry<MarkerKey, Map<SampleKey, byte[]>>> markerSamplesIterable,
			Set<SampleKey> sampleKeys, // NOTE needs to be well ordered!
			GenotypeEncoder encoder,
			int dSamples,
			int dEncoded,
			int n)
			throws IOException, InvalidRangeException
	{
		System.err.println("XXX samples:");

		// we use LinkedHashMap to preserve the inut order
		Map<SampleKey, List<Double>> encodedSamples
				= new LinkedHashMap<SampleKey, List<Double>>(n);
		for (SampleKey sampleKey : sampleKeys) {
			encodedSamples.put(sampleKey, new ArrayList<Double>(dEncoded));
		}

//		Map<MarkerKey, Set<Genotype>> uniqueGts
//				= new LinkedHashMap<MarkerKey, Set<Genotype>>(markerKeys.size());
		// collect unique GTs per marker
		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : markerSamplesIterable) {
			Map<SampleKey, byte[]> samples = markerSamples.getValue();
			System.err.print("\t");
			for (byte[] gt : samples.values()) {
				System.err.print(" " + new String(gt));
			}
			System.err.println();
//			System.err.println("XXX Combi-test");

			// convert & collect unique GTs (unique per marker)
			List<Genotype> all = new ArrayList<Genotype>(n);
			Set<Genotype> unique = new LinkedHashSet<Genotype>(4);
			for (Map.Entry<SampleKey, byte[]> sample : samples.entrySet()) {
				Genotype genotype = new Genotype(sample.getValue());
				all.add(genotype);
				unique.add(genotype);
//				System.err.println("\t" + sample.getKey() + ": " + new String(sample.getValue()));
			}
			List<Genotype> uniqueList = new ArrayList<Genotype>(unique);

			// test output
//			uniqueGts.put(markerKey, curUniqueGts);
//			System.err.println("\t" + markerKey + ": " + curUniqueGts.size());
//			for (Genotype genotype : curUniqueGts) {
//				System.err.println("\t\t" + genotype);
//			}

			// encode all samples for this marker
			encoder.encodeGenotypes(uniqueList, all, encodedSamples);
		}

		return encodedSamples;
	}

	private static final File TMP_RAW_DATA_FILE = new File(System.getProperty("user.home") + "/Projects/GWASpi/repos/GWASpi/rawDataTmp.ser");

	private static void storeForEncoding(MatrixSamples matrixSamples, Map<SampleKey, SampleInfo> sampleInfos, int dSamples, int dEncoded, int n) {

		// we use LinkedHashMap to preserve the inut order
		Map<MarkerKey, Map<SampleKey, byte[]>> loadedMatrixSamples
				= new LinkedHashMap<MarkerKey, Map<SampleKey, byte[]>>(dSamples);
		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : matrixSamples) {
			loadedMatrixSamples.put(markerSamples.getKey(), markerSamples.getValue());
			for (byte[] gt : markerSamples.getValue().values()) {
			}
		}

		try {
			FileOutputStream fout = new FileOutputStream(TMP_RAW_DATA_FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(loadedMatrixSamples);
			oos.writeObject(sampleInfos);
			oos.writeObject((Integer) dSamples);
			oos.writeObject((Integer) dEncoded);
			oos.writeObject((Integer) n);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runEncodingAndSVM(GenotypeEncoder genotypeEncoder) {

		Map<MarkerKey, Map<SampleKey, byte[]>> matrixSamples;
		Map<SampleKey, SampleInfo> sampleInfos;
		int dSamples;
		int dEncoded;
		int n;
		try {
			FileInputStream fin = new FileInputStream(TMP_RAW_DATA_FILE);
			ObjectInputStream ois = new ObjectInputStream(fin);
			matrixSamples = (Map<MarkerKey, Map<SampleKey, byte[]>>) ois.readObject();
			sampleInfos = (Map<SampleKey, SampleInfo>) ois.readObject();
			dSamples = (Integer) ois.readObject();
			dEncoded = (Integer) ois.readObject();
			n = (Integer) ois.readObject();
			ois.close();

			Map<SampleKey, List<Double>> encodedSamples = encodeSamples(
					matrixSamples.entrySet(),
					sampleInfos.keySet(),
					genotypeEncoder,
					dSamples,
					dEncoded,
					n);
			Map<SampleKey, Double> encodedAffectionStates = encodeAffectionStates(
					sampleInfos,
					n);

			// do the SVM magic!
			Map<SampleKey, List<Double>> X = encodedSamples;
			Map<SampleKey, Double> Y = encodedAffectionStates;

			storeForSVM(X, Y);

			runSVM(X, Y, genotypeEncoder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final File TMP_SVM_DATA_FILE = new File(System.getProperty("user.home") + "/Projects/GWASpi/repos/GWASpi/svmDataTmp.ser");

	private static void storeForSVM(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y) {

		try {
			FileOutputStream fout = new FileOutputStream(TMP_SVM_DATA_FILE);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(X);
			oos.writeObject(Y);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runSVM(GenotypeEncoder genotypeEncoder) {

		Map<SampleKey, List<Double>> X;
		Map<SampleKey, Double> Y;
		try {
			FileInputStream fin = new FileInputStream(TMP_SVM_DATA_FILE);
			ObjectInputStream ois = new ObjectInputStream(fin);
			X = (Map<SampleKey, List<Double>>) ois.readObject();
			Y = (Map<SampleKey, Double>) ois.readObject();
			ois.close();

			runSVM(X, Y, genotypeEncoder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void whiten(Map<SampleKey, List<Double>> X) {

		int dEncoded = X.values().iterator().next().size();
		int n = X.size();

		System.err.println("XXX X raw: " + X.size() + " * " + X.values().iterator().next().size());
		for (List<Double> x : X.values()) {
			System.err.println("\tx: " + x);
		}

		// center the data
		// ... using Double to calculate the mean, to prevent nummerical inaccuracies
		List<Double> sums = new ArrayList<Double>(dEncoded);
		List<Double> varianceSums = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
			sums.add(0.0);
			varianceSums.add(0.0);
		}
		for (List<Double> x : X.values()) {
			for (int di = 0; di < dEncoded; di++) {
				sums.set(di, sums.get(di) + x.get(di));
			}
		}
		System.err.println("XXX sums: " + sums);
		List<Double> mean = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
//			Double divide = sums.get(di).setScale(4).divide(new Double(nSamples), Double.ROUND_HALF_UP);
//			System.err.println("XXX mean part: " + sums.get(di) + " / " + nSamples + " = " + divide);
//			mean.add(sums.get(di).divide(new Double(nSamples), Double.ROUND_HALF_UP).doubleValue());
			final double curSum = sums.get(di);
			final double curMean = (curSum == 0.0) ? 0.0 : (curSum / n);
			mean.add(curMean);
		}
		System.err.println("XXX mean: " + mean);
		// alternatively, using a moving average as described in the second formula here:
		// https://en.wikipedia.org/wiki/Moving_average#Cumulative_moving_average
		// this might be faster, might not.
		// TODO

		// subtract the mean & calculate the variance sums
		for (List<Double> x : X.values()) {
			for (int di = 0; di < dEncoded; di++) {
				final double newValue = x.get(di) - mean.get(di);
				x.set(di, newValue);
				//varianceSums.set(di, varianceSums.get(di).add(new Double(newValue * newValue)))); // faster
//				varianceSums.set(di, varianceSums.get(di).add(new Double(newValue.pow(2))); // XXX more precise
				varianceSums.set(di, varianceSums.get(di) + (newValue * newValue));
			}
		}

//		// calculate the variance sums separately?
//		for (List<Double> x : X.values()) {
//			for (int di = 0; di < dEncoded; di++) {
//				x.set(di, x.get(di) - mean.get(di));
//			}
//		}

		// calculate the variance
		List<Double> variance = new ArrayList<Double>(dEncoded);
		List<Double> stdDev = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
			double curVariance = varianceSums.get(di) / n * dEncoded;
			variance.add(curVariance);
			stdDev.add(Math.sqrt(curVariance));
		}

		// set the variance to 1
		for (List<Double> x : X.values()) {
			for (int di = 0; di < dEncoded; di++) {
				final double curStdDev = stdDev.get(di);
				final double oldValue = x.get(di);
				final double newValue = (curStdDev == 0.0) ? oldValue : (oldValue / curStdDev);
				x.set(di, newValue);
			}
		}
	}

	private static svm_problem createLibSvmProblem(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y) {

		int dEncoded = X.values().iterator().next().size();
		int n = X.size();

		svm_problem prob = new svm_problem();

		// prepare the features
		prob.x = new svm_node[n][dEncoded];
		Iterator<List<Double>> itX = X.values().iterator();
		for (int si = 0; si < n; si++) {
			List<Double> sampleGTs = itX.next();
			for (int mi = 0; mi < dEncoded; mi++) {
				svm_node curNode = new svm_node();
//				curNode.index = mi;
				curNode.index = si; /// XXX correct?
				curNode.value = sampleGTs.get(mi);
				prob.x[si][mi] = curNode;
			}
		}
		System.err.println("XXX X: " + prob.x.length + " * " + prob.x[0].length);
		for (int i = 0; i < prob.x.length; i++) {
			System.err.print("\tx:");
			for (int j = 0; j < prob.x[i].length; j++) {
				System.err.print(" " + prob.x[i][j].value);
			}
			System.err.println();
		}

		// prepare the labels
		prob.l = n;
		prob.y = new double[prob.l];
		Iterator<Double> itY = Y.values().iterator();
		for (int si = 0; si < n; si++) {
			prob.y[si] = itY.next();
		}

		return prob;
	}

	private static svm_parameter createLibSvmParameters() {

		svm_parameter svmParams = new svm_parameter();

		/** possible values: C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR */
		svmParams.svm_type = svm_parameter.C_SVC;
		/** possible values: LINEAR, POLY, RBF, SIGMOID, PRECOMPUTED */
		svmParams.kernel_type = svm_parameter.LINEAR;
		/** for poly */
		svmParams.degree = 3;
		/** for poly/RBF/sigmoid */
		svmParams.gamma = 0.0;
		/** for poly/sigmoid */
		svmParams.coef0 = 0;

		// these are for training only
		/** The cache size in MB */
		svmParams.cache_size = 40;
		/** stopping criteria */
		svmParams.eps = 1E-5;
		/** for C_SVC, EPSILON_SVR and NU_SVR */
		svmParams.C = 1.0;
		/** for C_SVC */
		svmParams.nr_weight = 0;
		/** for C_SVC */
		svmParams.weight_label = new int[svmParams.nr_weight];
		/** for C_SVC */
		svmParams.weight = new double[svmParams.nr_weight];
		/** for NU_SVC, ONE_CLASS, and NU_SVR */
		svmParams.nu = 0.5;
		/** for EPSILON_SVR */
		svmParams.p = 0.5;
		/** use the shrinking heuristics */
		svmParams.shrinking = 1;
		/** do probability estimates */
		svmParams.probability = 0;

		return svmParams;
	}

	/**
	 * Calculate the weights 'w' in the original space,
	 * using the weights 'alpha' from the kernel-space.
	 * <math>\mathbf{w} = \sum_i \alpha_i y_i \mathbf{x}_i.</math>
	 * @param alphas the SVM problem weights in kernel-space
	 * @param xs the support-vector coordinates in the original space
	 * @param ys the labels of the data (-1, or 1)
	 * @return the SVM problem weights 'w' in the original space
	 */
	private static List<Double> calculateOriginalSpaceWeights(
			final double[][] alphas,
			final svm_node[][] xs,
			final double[] ys)
	{
		final int d = xs[0].length;

		List<Double> weights
				= new ArrayList<Double>(Collections.nCopies(d , 0.0));
		for (int svi = 0; svi < xs.length; svi++) {
			final svm_node[] x = xs[svi];
			final double alpha = alphas[0][svi];
			final double y = ys[x[0].index];
			for (int di = 0; di < d; di++) {
				final double alphaYXi = alpha * y * x[di].value;
//				System.err.print(" " + svwp);
				weights.set(di, weights.get(di) + alphaYXi);
			}
//			System.err.println();
		}

		return weights;
	}


	private static int runSVM(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y, GenotypeEncoder genotypeEncoder) {

		int dEncoded = X.values().iterator().next().size();
		int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		int n = X.size();

		whiten(X);

		svm_problem libSvmProblem = createLibSvmProblem(X, Y);

		svm_parameter libSvmParameters = createLibSvmParameters();

		svm_model svmModel = svm.svm_train(libSvmProblem, libSvmParameters);

//		double[][] alphas = svmModel.sv_coef;
//		svm_node[][] SVs = svmModel.SV;
//
//		System.err.println("XXX alphas: " + alphas.length + " * " + alphas[0].length);
//		for (int i = 0; i < alphas.length; i++) {
//			System.err.print("\talpha:");
//			for (int j = 0; j < alphas[i].length; j++) {
//				System.err.print(" " + alphas[i][j]);
//			}
//			System.err.println();
//		}
//
//		System.err.println("XXX SVs: " + SVs.length + " * " + SVs[0].length);
//		for (int i = 0; i < SVs.length; i++) {
//			System.err.print("\tsv:");
//			for (int j = 0; j < SVs[i].length; j++) {
//				System.err.print(" " + SVs[i][j].value + "(" + SVs[i][j].index + ")");
//			}
//			System.err.println();
//		}

		List<Double> weightsEncoded = calculateOriginalSpaceWeights(
				svmModel.sv_coef, svmModel.SV, libSvmProblem.y);

		System.err.println("XXX weights(encoded): " + weightsEncoded.size() + "" + weightsEncoded);

		System.err.println("XXX dSamples: " + dSamples);
		System.err.println("XXX dEncoded: " + dEncoded);
		System.err.println("XXX n: " + n);

		List<Double> weights = new ArrayList<Double>(dSamples);
		genotypeEncoder.decodeWeights(weightsEncoded, weights);
		System.err.println("XXX weights: (" + weights.size() + ") " + weights);

		return Integer.MIN_VALUE;
	}

	public static void main(String[] args) {

//		GenotypeEncoder encoder = new AllelicGenotypeEncoder(); // TODO
//		GenotypeEncoder encoder = new GenotypicGenotypeEncoder(); // TODO
		GenotypeEncoder encoder = new NominalGenotypeEncoder(); // TODO

		runEncodingAndSVM(encoder);
//		runSVM(encoder);
	}
}
