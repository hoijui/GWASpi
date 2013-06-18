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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static final File BASE_DIR = new File(System.getProperty("user.home"), "/Projects/GWASpi/var/data/marius/example/extra");

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
		int mi = 0;
		for (Map.Entry<MarkerKey, Map<SampleKey, byte[]>> markerSamples : markerSamplesIterable) {
			Map<SampleKey, byte[]> samples = markerSamples.getValue();
			System.err.print("\nmarker " + mi + "\n");
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
			Collections.sort(uniqueList);
//			System.err.println("\tunique GT list:");
//			for (Genotype genotype : uniqueList) {
//				System.err.println("\t\t\t\t" + genotype);
//			}

			// test output
//			uniqueGts.put(markerKey, curUniqueGts);
//			System.err.println("\t" + markerKey + ": " + curUniqueGts.size());
//			for (Genotype genotype : curUniqueGts) {
//				System.err.println("\t\t" + genotype);
//			}

			// encode all samples for this marker
			encoder.encodeGenotypes(uniqueList, all, encodedSamples);
			mi++;
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

			String encoderString;
			if (genotypeEncoder instanceof AllelicGenotypeEncoder) {
				encoderString = "allelic";
			} else if (genotypeEncoder instanceof GenotypicGenotypeEncoder) {
				encoderString = "genotypic";
			} else if (genotypeEncoder instanceof NominalGenotypeEncoder) {
				encoderString = "nominal";
			} else {
				throw new RuntimeException();
			}

			runSVM(new ArrayList<List<Double>>(X.values()), new ArrayList<Double>(Y.values()), genotypeEncoder, encoderString);
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

			String encoderString;
			if (genotypeEncoder instanceof AllelicGenotypeEncoder) {
				encoderString = "allelic";
			} else if (genotypeEncoder instanceof GenotypicGenotypeEncoder) {
				encoderString = "genotypic";
			} else if (genotypeEncoder instanceof NominalGenotypeEncoder) {
				encoderString = "nominal";
			} else {
				throw new RuntimeException();
			}

			runSVM(new ArrayList<List<Double>>(X.values()), new ArrayList<Double>(Y.values()), genotypeEncoder, encoderString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void whiten(List<List<Double>> X) {

		int dEncoded = X.iterator().next().size();
		int n = X.size();

//		System.err.println("XXX X raw: " + X.size() + " * " + X.values().iterator().next().size());
//		for (List<Double> x : X.values()) {
//			System.err.println("\tx: " + x);
//		}

		// center the data
		// ... using Double to calculate the mean, to prevent nummerical inaccuracies
		List<Double> sums = new ArrayList<Double>(dEncoded);
		List<Double> varianceSums = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
			sums.add(0.0);
			varianceSums.add(0.0);
		}
		for (List<Double> x : X) {
			for (int di = 0; di < dEncoded; di++) {
				sums.set(di, sums.get(di) + x.get(di));
			}
		}
//		System.err.println("XXX sums: " + sums);
		List<Double> mean = new ArrayList<Double>(dEncoded);
		for (int di = 0; di < dEncoded; di++) {
//			Double divide = sums.get(di).setScale(4).divide(new Double(nSamples), Double.ROUND_HALF_UP);
//			System.err.println("XXX mean part: " + sums.get(di) + " / " + nSamples + " = " + divide);
//			mean.add(sums.get(di).divide(new Double(nSamples), Double.ROUND_HALF_UP).doubleValue());
			final double curSum = sums.get(di);
			final double curMean = (curSum == 0.0) ? 0.0 : (curSum / n);
			mean.add(curMean);
		}
//		System.err.println("XXX mean: " + mean);
		// alternatively, using a moving average as described in the second formula here:
		// https://en.wikipedia.org/wiki/Moving_average#Cumulative_moving_average
		// this might be faster, might not.
		// TODO

		// subtract the mean & calculate the variance sums
		for (List<Double> x : X) {
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
		for (List<Double> x : X) {
			for (int di = 0; di < dEncoded; di++) {
				final double curStdDev = stdDev.get(di);
				final double oldValue = x.get(di);
				final double newValue = (curStdDev == 0.0) ? oldValue : (oldValue / curStdDev);
				x.set(di, newValue);
			}
		}
	}

	private static List<List<Double>> matrixMult(
			List<List<Double>> matrixA,
			List<List<Double>> matrixB)
	{
		final int nA = matrixA.size();
		final int mA = matrixA.get(0).size();
		final int nB = matrixB.size();
		final int mB = matrixB.get(0).size();

		if (mA != nB) {
			throw new RuntimeException(String.format(
					"can not multiply matrizes with sizes (%d x %d) and (%d x %d)",
					nA, mA, nB, mB));
		}

		// init the resulting matrix wiht zeros
		List<List<Double>> res = new ArrayList<List<Double>>(matrixA.size());
		for (int ri = 0; ri < nA; ri++) {
			List<Double> row = new ArrayList<Double>(mB);
			for (int ci = 0; ci < mB; ci++) {
				row.add(0.0);
			}
			res.add(row);
		}

		for (int ri = 0; ri < nA; ri++) {
			List<Double> rowA = matrixA.get(ri);
			List<Double> rowRes = res.get(ri);
			for (int ci = 0; ci < mB; ci++) {
				for (int ii = 0; ii < mA; ii++) {
					rowRes.set(ci, rowRes.get(ci) + (rowA.get(ii) * matrixB.get(ii).get(ci)));
				}
			}
		}

		return res;
	}

	private static svm_problem createLibSvmProblem(
			List<List<Double>> X,
			List<Double> Y,
			svm_parameter libSvmParameters,
			String encoderString)
	{
		svm_problem prob = new svm_problem();

		int dEncoded = X.iterator().next().size();
		int n = X.size();

		// prepare the features
		List<List<Double>> problemInput;
		if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
			// precomute the kernel: K = X' * X
//			prob.x = new svm_node[n][n];
			List<List<Double>> XT = transpose(X);
			problemInput = matrixMult(X, XT);

			if (encoderString != null) {
				File correctKernelFile = new File(BASE_DIR, "K_" + encoderString);
				List<List<Double>> correctKernel = parsePlainTextMatrix(correctKernelFile, false);

				System.err.println("\ncompare kernel matrices ...");
				compareMatrices(correctKernel, problemInput);
				System.err.println("done. they are equal! good!\n");
			}

			// This is required by the libSVM standard for a PRECOMPUTED kernel
			int sampleIndex = 1;
			for (List<Double> problemInputRow : problemInput) {
				// XXX NOTE This is bad, because it will double the underlaying arrays size!
				problemInputRow.add(0, (double) sampleIndex++);
			}
			// TESTING output to libSVM input format for a precomputed kernel, to test it externally
			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + ".txt");
//			System.err.println("\nX: " + X);
//			System.err.println("\nXT: " + XT);
//			System.err.println("\nX * XT: " + problemInput);
			System.err.println("\nwriting generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
			try {
				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
				Iterator<Double> Yit = Y.iterator();
				for (List<Double> problemInputRow : problemInput) {
					final double y = Yit.next();
					kernOut.write(String.valueOf(y));
					int ci = 0;
					for (Double value : problemInputRow) {
						kernOut.write(' ');
						kernOut.write(String.valueOf(ci));
						kernOut.write(':');
						kernOut.write(String.valueOf(value));
						ci++;
					}
					kernOut.write('\n');
				}
				kernOut.close();
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.err.println("done writing kernel file.");

			// XXX NOTE Do not delete this code! as it will be bael to save us memory!
////			// TODO
////			throw new RuntimeException();
//			Iterator<List<Double>> itX = X.values().iterator();
//			for (int si1 = 0; si1 < n; si1++) {
//				List<Double> sampleGTs1 = itX.next();
//				for (int si2 = 0; si2 <= si1; si2++) { // calculate only half of the matrix, caus it is symmetric
//					List<Double> sampleGTs2 = matX.get(si2);
//					double res = 0.0;
//					for (int mi = 0; mi < dEncoded; mi++) {
//						res += sampleGTs1.get(mi) * sampleGTs2.get(mi);
//					}
//
//					// save two times, cause the matrix is symmetric
//					svm_node curNodeL = new svm_node();
//					curNodeL.index = si1;
//					curNodeL.value = res;
//					prob.x[si1][si2] = curNodeL;
//
//					svm_node curNodeU = new svm_node();
//					curNodeU.index = si2;
//					curNodeU.value = res;
//					prob.x[si2][si1] = curNodeU;
//				}
//			}
		} else {
			problemInput = X;

//			prob.x = new svm_node[n][dEncoded];
//			Iterator<List<Double>> itX = X.values().iterator();
//			for (int si = 0; si < n; si++) {
//				List<Double> sampleGTs = itX.next();
//				for (int mi = 0; mi < dEncoded; mi++) {
//					svm_node curNode = new svm_node();
//	//				curNode.index = mi;
//					curNode.index = si; /// XXX correct?
//					curNode.value = sampleGTs.get(mi);
//					prob.x[si][mi] = curNode;
//				}
//			}
		}
		prob.x = new svm_node[problemInput.size()][problemInput.get(0).size()];
		System.err.println("\nproblemInput: " + problemInput.size() + " * " + problemInput.get(0).size());
		Iterator<List<Double>> itX = problemInput.iterator();
		for (int si = 0; si < problemInput.size(); si++) {
			List<Double> sampleGTs = itX.next();
			for (int mi = 0; mi < problemInput.get(0).size(); mi++) {
				svm_node curNode = new svm_node();
				curNode.index = mi; // XXX correct?
//				curNode.index = si; // XXX correct? pretty sure that yes
				curNode.value = sampleGTs.get(mi);
				prob.x[si][mi] = curNode;
			}
		}


//		System.err.println("XXX X: " + prob.x.length + " * " + prob.x[0].length);
//		for (int i = 0; i < prob.x.length; i++) {
//			System.err.print("\tx:");
//			for (int j = 0; j < prob.x[i].length; j++) {
//				System.err.print(" " + prob.x[i][j].value);
//			}
//			System.err.println();
//		}

		// prepare the labels
		prob.l = n;
		prob.y = new double[prob.l];
		System.err.print("\ty:");
		Iterator<Double> itY = Y.iterator();
		for (int si = 0; si < n; si++) {
			double y = itY.next();
//			y = (y + 1.0) / 2.0;
			prob.y[si] = y;
			System.err.print(" " + y);
		}
		System.err.println();

		{
			File generatedLibSvmKernelFile = new File(BASE_DIR, "generatedLibSvmKernel_" + encoderString + "_after.txt");
//			System.err.println("\nX: " + X);
//			System.err.println("\nXT: " + XT);
//			System.err.println("\nX * XT: " + problemInput);
			System.err.println("\nAGAIN writing generated libSVM PRECOMPUTED kernel file to " + generatedLibSvmKernelFile + " ...");
			try {
				OutputStreamWriter kernOut = new FileWriter(generatedLibSvmKernelFile);
				for (int si = 0; si < prob.x.length; si++) {
					kernOut.write(String.valueOf(prob.y[si]));
					for (int mi = 0; mi < prob.x[si].length; mi++) {
						kernOut.write(' ');
						kernOut.write(String.valueOf(prob.x[si][mi].index));
						kernOut.write(':');
						kernOut.write(String.valueOf(prob.x[si][mi].value));
					}
					kernOut.write('\n');
				}
				kernOut.close();
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.err.println("done writing kernel file.");
		}

		return prob;
	}

	private static svm_parameter createLibSvmParameters() {

		svm_parameter svmParams = new svm_parameter();

		/** possible values: C_SVC, NU_SVC, ONE_CLASS, EPSILON_SVR, NU_SVR */
		svmParams.svm_type = svm_parameter.C_SVC;
		/** possible values: LINEAR, POLY, RBF, SIGMOID, PRECOMPUTED */
//		svmParams.kernel_type = svm_parameter.LINEAR;
		svmParams.kernel_type = svm_parameter.PRECOMPUTED;
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
		svmParams.eps = 1E-7;
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
	 * Calculate the weights 'w' in the original space (as in, the orignal space as known to the SVM, not the genotype space),
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
			final List<List<Double>> X,
			final double[] ys)
	{
//		final int d = xs[0].length;
		final int d = X.get(0).size();

		List<Double> weights
				= new ArrayList<Double>(Collections.nCopies(d , 0.0));
System.err.println("calculateOriginalSpaceWeights: " + xs.length);
		for (int svi = 0; svi < xs.length; svi++) {
			final svm_node[] xsi = xs[svi];
//			final int svIndex = xsi[0].index; // FIXME this is wrong! it is the other index (marker-id, not sample-id!
			final int svIndex = (int) xsi[0].value - 1; // FIXME this only works wiht PRECOMPUTED!
			System.err.println("svIndex: " + svIndex);
			final List<Double> Xsi = X.get(svIndex);
			final double alpha = alphas[0][svi];
			final double y = ys[svIndex];
			for (int di = 0; di < d; di++) {
//				final double x = xsi[di].value;
//				final double x = Xsi.get(di);
				final double x = Math.abs(Xsi.get(di));
//				final double alphaYXi = alpha * y * x;
				// NOTE We dismiss the y, which would be part of normal SVM,
				// because we want the absolute sum (i forgot again why so :/ )
				final double alphaYXi = alpha * x;
//				System.err.print(" " + svwp);
				weights.set(di, weights.get(di) + alphaYXi);
			}
//			System.err.println();
		}

		return weights;
	}


//	private static int runSVM(Map<SampleKey, List<Double>> X, Map<SampleKey, Double> Y, GenotypeEncoder genotypeEncoder) {
	private static int runSVM(List<List<Double>> X, List<Double> Y, GenotypeEncoder genotypeEncoder, String encoderString) {

		int dEncoded = X.iterator().next().size();
		int dSamples = dEncoded / genotypeEncoder.getEncodingFactor();
		int n = X.size();

		whiten(X);

		// check if feature matrix is equivalent to the one calculated with matlab
		if (encoderString != null) {
			File correctFeaturesFile = new File(BASE_DIR, "featmat_" + encoderString + "_extra");
			List<List<Double>> correctFeatures = parsePlainTextMatrix(correctFeaturesFile, false);
			List<List<Double>> xValuesTrans = transpose(X);
//			System.err.println("\nXXX correctFeatures[2]: " + correctFeatures.get(2));
//			System.err.println("\nXXX xValues[2]: " + xValuesTrans.get(2));
			System.err.println("\ncompare feature matrices ...");
			compareMatrices(correctFeatures, xValuesTrans);
			System.err.println("done. they are equal! good!\n");
		}

		svm_parameter libSvmParameters = createLibSvmParameters();

		svm_problem libSvmProblem = createLibSvmProblem(X, Y, libSvmParameters, encoderString);

		svm_model svmModel = svm.svm_train(libSvmProblem, libSvmParameters);


		List<Double> myAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
		int curSVIndex = 0;
		for (int i = 0; i < svmModel.sv_coef[0].length; i++) {
			final double value = svmModel.sv_coef[0][i] * -1.0; // HACK FIXME no idea why we get inverted signs, but it should not matter much for our purpose
			int index;
			if (libSvmParameters.kernel_type == svm_parameter.PRECOMPUTED) {
				index = (int) svmModel.SV[i][0].value - 1; // XXX NOTE only works with PRECOMPUTED!
			} else { // LINEAR
//				while (libSvmProblem.x[curSVIndex][0] != svmModel.SV[i][0]) {
//					curSVIndex++;
//				}
//				index = curSVIndex;
				index = (int) svmModel.sv_indices[0][i]; // XXX testing
			}
//			final int index = svmModel.SV[i][0].index;
//			final int index = (int) svmModel.SV[i][0].index; // XXX NOTE does NOT work with PRECOMPUTED!
//			final int index = (int) svmModel.SV[i][0].value - 1; // XXX NOTE only works with PRECOMPUTED!
//			final int index = (int) svmModel.sv_indices[i][0]; // XXX testing
//			final int index = (int) svmModel.sv_indices[0][i]; // XXX testing
			myAlphas.set(index, value);
		}

		// check if the alphas are equivalent to the ones calculated with matlab
		if (encoderString != null) {
			double[][] alphas = svmModel.sv_coef;
			svm_node[][] SVs = svmModel.SV;
			System.err.println("\n alphas: " + alphas.length + " * " + alphas[0].length + ": " + Arrays.asList(alphas[0]));
			System.err.println("\n SVs: " + SVs.length + " * " + SVs[0].length);

			List<List<Double>> alphasLM = new ArrayList<List<Double>>(alphas.length);
			for (int i = 0; i < alphas.length; i++) {
				List<Double> curRow = new ArrayList<Double>(alphas[i].length);
				for (int j = 0; j < alphas[i].length; j++) {
System.err.println("\talpha: " + i + ", " + j + ": " + alphas[i][j]);
					curRow.add(alphas[i][j]);
				}
				alphasLM.add(curRow);
			}

//			System.err.println("\nXXX alphas: " + alphas.length + " * " + alphas[0].length);
//			for (int i = 0; i < alphas.length; i++) {
//				System.err.print("\talpha:");
//				for (int j = 0; j < alphas[i].length; j++) {
//					System.err.print(" " + alphas[i][j]);
//				}
//				System.err.println();
//			}

			File correctAlphasFile = new File(BASE_DIR, "alpha_" + encoderString);
			List<List<Double>> correctAlphasSparse = parsePlainTextMatrix(correctAlphasFile, false);
			List<Double> correctAlphas = new ArrayList<Double>(Collections.nCopies(n, 0.0));
			for (int i = 0; i < correctAlphasSparse.size(); i++) {
				final double value = correctAlphasSparse.get(i).get(0);
				final int index = correctAlphasSparse.get(i).get(1).intValue();
				correctAlphas.set(index, value);
			}

//			{
//				List<List<Double>> matrixA = new ArrayList<List<Double>>(3);
//				matrixA.add(Arrays.asList(new Double[] {1.0, 2.0}));
//				matrixA.add(Arrays.asList(new Double[] {3.0, 4.0}));
//				matrixA.add(Arrays.asList(new Double[] {5.0, 6.0}));
//
//				List<List<Double>> matrixB = new ArrayList<List<Double>>(2);
//				matrixB.add(Arrays.asList(new Double[] {1.0, -1.0, 1.0}));
//				matrixB.add(Arrays.asList(new Double[] {2.0, 1.0, 0.0}));
//
//				List<List<Double>> res = matrixMult(matrixA, matrixB);
//				System.err.println("\nmatrixA: " + matrixA);
//				System.err.println("\nmatrixB: " + matrixB);
//				System.err.println("\nres: " + res);
//			}

//			Collections.sort(correctAlphas);
//			Collections.sort(myAlphas);
			System.err.println("\nmatlab alphas: ("+correctAlphas.size()+")\n" + correctAlphas);
			System.err.println("\njava alphas: ("+myAlphas.size()+")\n" + myAlphas);
			System.err.println("\ncompare alpha vectors ...");
			compareVectors(correctAlphas, myAlphas);
			System.err.println("done. they are equal! good!\n");
		} else {
			System.err.println("\njava alphas: ("+myAlphas.size()+")\n" + myAlphas);
		}

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
				svmModel.sv_coef, svmModel.SV, X, libSvmProblem.y);

		// check if the weights are equivalent to the ones calculated with matlab
		{
			File mlWeightsRawFile = new File(BASE_DIR, "w_" + encoderString + "_raw");
			List<Double> mlWeightsRaw = parsePlainTextMatrix(mlWeightsRawFile, true).get(0);

			System.err.println("\ncompare raw, encoded weights vectors ...");
			compareVectors(mlWeightsRaw, weightsEncoded);
			System.err.println("done. they are equal! good!\n");
		}

		System.err.println("XXX weights(encoded): " + weightsEncoded.size());
		System.err.println("\t" + weightsEncoded);

		System.err.println("XXX dSamples: " + dSamples);
		System.err.println("XXX dEncoded: " + dEncoded);
		System.err.println("XXX n: " + n);
		System.err.println("XXX genotypeEncoder: " + genotypeEncoder.getClass().getSimpleName());
		System.err.println("XXX encodingFactor: " + genotypeEncoder.getEncodingFactor());

		if (encoderString != null) {
			List<Double> weights = new ArrayList<Double>(dSamples);
			genotypeEncoder.decodeWeights(weightsEncoded, weights);
			System.err.println("XXX weights: (" + weights.size() + ") " + weights);
		}

		return Integer.MIN_VALUE;
	}

	public static void compareMatrices(
			List<List<Double>> matrixA,
			List<List<Double>> matrixB)
	{
		final int rowsA = matrixA.size();
		final int rowsB = matrixB.size();
		final int colsA = matrixA.get(0).size();
		final int colsB = matrixB.get(0).size();

		if ((rowsA != rowsB) || (colsA != colsB)) {
			throw new RuntimeException(String.format(
					"matrix A dimension (%d, %d) differ from dimensions of matrix B (%d, %d)",
					rowsA, colsA, rowsB, colsB));
		}
		for (int y = 0; y < matrixA.size(); y++) {
			List<Double> rowA = matrixA.get(y);
			List<Double> rowB = matrixB.get(y);
			for (int x = 0; x < rowA.size(); x++) {
				double valA = rowA.get(x);
				double valB = rowB.get(x);
//				valA = Math.abs(valA); // FIXME do not use!
//				valB = Math.abs(valB); // FIXME do not use!
				if (!compareValues(valA, valB)) {
					throw new RuntimeException(String.format(
						"matrix A differs from matrix B at (%d, %d): %f, %f",
						y, x, valA, valB));
				}
			}

		}
	}

	public static void compareVectors(
			List<Double> vectorA,
			List<Double> vectorB)
	{
		final int rowsA = vectorA.size();
		final int rowsB = vectorB.size();

		if (rowsA != rowsB) {
			throw new RuntimeException(String.format(
					"vector A dimension (%d) differs from dimension of vector B (%d)",
					rowsA, rowsB));
		}
		for (int y = 0; y < vectorA.size(); y++) {
			double valA = vectorA.get(y);
			double valB = vectorB.get(y);
//			valA = Math.abs(valA); // FIXME do not use!
//			valB = Math.abs(valB); // FIXME do not use!
			if (!compareValues(valA, valB)) {
				throw new RuntimeException(String.format(
					"vector A differs from vector B at (%d): %f, %f",
					y, valA, valB));
			}

		}
	}

	/**
	 * Returns true if the supplied values are (quite) equal.
	 */
	public static boolean compareValues(double valA, double valB) {

		final double relativeDiff = Math.abs((valA - valB) / (valA + valB));
		return (valA == valA) || Double.isNaN(relativeDiff) || !(relativeDiff > 0.01);
	}

	public static List<List<Double>> transpose(List<List<Double>> matrix) {

		List<List<Double>> transposed = new ArrayList<List<Double>>(matrix.get(0).size());

		for (int c = 0; c < matrix.get(0).size(); c++) {
			transposed.add(new ArrayList<Double>(matrix.size()));
		}

		for (int r = 0; r < matrix.size(); r++) {
			List<Double> row = matrix.get(r);
			for (int c = 0; c < row.size(); c++) {
				transposed.get(c).add(row.get(c));
			}
		}

		return transposed;
	}

	public static List<List<Double>> parsePlainTextMatrix(File sourceFile) {
		return parsePlainTextMatrix(sourceFile, false);
	}
	public static List<List<Double>> parsePlainTextMatrix(File sourceFile, boolean transposed) {

		List<List<Double>> matrix = new ArrayList<List<Double>>();

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(sourceFile);
			bufferedReader = new BufferedReader(fileReader);
			String line = bufferedReader.readLine();
			while (line != null) {
				String[] strValues = line.split("[ ]+");
				if (transposed) {
					if (matrix.isEmpty()) {
						// initialize the rows
						for (int svi = 0; svi < strValues.length; svi++) {
							matrix.add(new ArrayList<Double>());
						}
					}
					for (int svi = 0; svi < strValues.length; svi++) {
						matrix.get(svi).add(Double.parseDouble(strValues[svi]));
					}
				} else {
					List<Double> row = new ArrayList<Double>(strValues.length);
					for (int svi = 0; svi < strValues.length; svi++) {
						row.add(Double.parseDouble(strValues[svi]));
					}
					matrix.add(row);
				}
				line = bufferedReader.readLine();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return matrix;
	}

	public static void main(String[] args) {

//		GenotypeEncoder genotypeEncoder = new AllelicGenotypeEncoder(); // TODO
		GenotypeEncoder genotypeEncoder = new GenotypicGenotypeEncoder(); // TODO
//		GenotypeEncoder genotypeEncoder = new NominalGenotypeEncoder(); // TODO

//		runSVM(genotypeEncoder);

		runEncodingAndSVM(genotypeEncoder);

//		List<List<Double>> X = new ArrayList<List<Double>>(2);
//		X.add(Arrays.asList(new Double[] {1.0, 0.0}));
//		X.add(Arrays.asList(new Double[] {0.0, 1.0}));
//		List<Double> Y = new ArrayList<Double>(2);
//		Y.add(1.0);
//		Y.add(-1.0);
//		runSVM(X, Y, genotypeEncoder, null);
	}
}
