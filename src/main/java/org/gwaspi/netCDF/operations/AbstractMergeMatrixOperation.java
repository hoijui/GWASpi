package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.ComparatorChrAutPosMarkerIdAsc;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

public abstract class AbstractMergeMatrixOperation implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(AbstractMergeMatrixOperation.class);

	protected int studyId;
	protected int rdMatrix1Id;
	protected int rdMatrix2Id;
	protected final String wrMatrixFriendlyName;
	protected final String wrMatrixDescription;
	protected MatrixMetadata rdMatrix1Metadata;
	protected MatrixMetadata rdMatrix2Metadata;
	protected MatrixMetadata wrMatrixMetadata;
	protected MarkerSet rdMarkerSet1;
	protected MarkerSet rdMarkerSet2;
	protected MarkerSet wrMarkerSet;
	protected SampleSet rdSampleSet1;
	protected SampleSet rdSampleSet2;
	protected SampleSet wrSampleSet;

	private AbstractMergeMatrixOperation(
			Integer rdMatrix1Id,
			Integer rdMatrix2Id,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		this.wrMatrixMetadata = null;
		this.wrMarkerSet = null;
		this.wrSampleSet = null;

		this.rdMatrix1Id = rdMatrix1Id;
		this.rdMatrix2Id = rdMatrix2Id;

		this.rdMatrix1Metadata = MatricesList.getMatrixMetadataById(this.rdMatrix1Id);
		this.rdMatrix2Metadata = MatricesList.getMatrixMetadataById(this.rdMatrix2Id);

		this.wrMatrixFriendlyName = wrMatrixFriendlyName;
		this.wrMatrixDescription = wrMatrixDescription;
	}

	public AbstractMergeMatrixOperation(
			int studyId,
			int rdMatrix1Id,
			int rdMatrix2Id,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		this(
				rdMatrix1Id,
				rdMatrix2Id,
				wrMatrixFriendlyName,
				wrMatrixDescription);

		this.studyId = studyId;

		this.rdMarkerSet1 = new MarkerSet(this.studyId, this.rdMatrix1Id);
		this.rdMarkerSet2 = new MarkerSet(this.studyId, this.rdMatrix2Id);

		this.rdSampleSet1 = new SampleSet(this.studyId, this.rdMatrix1Id);
		this.rdSampleSet2 = new SampleSet(this.studyId, this.rdMatrix2Id);
	}

	public AbstractMergeMatrixOperation(
			int rdMatrix1Id,
			int rdMatrix2Id,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		this(
				(Integer) rdMatrix1Id,
				(Integer) rdMatrix2Id,
				wrMatrixFriendlyName,
				wrMatrixDescription);

		this.studyId = this.rdMatrix1Metadata.getStudyId();

		this.rdMarkerSet1 = new MarkerSet(this.rdMatrix1Metadata.getStudyId(), this.rdMatrix1Id);
		this.rdMarkerSet2 = new MarkerSet(this.rdMatrix2Metadata.getStudyId(), this.rdMatrix2Id);

		this.rdSampleSet1 = new SampleSet(this.rdMatrix1Metadata.getStudyId(), this.rdMatrix1Id);
		this.rdSampleSet2 = new SampleSet(this.rdMatrix2Metadata.getStudyId(), this.rdMatrix2Id);
	}

	private static Map<MarkerKey, char[]> getMatrixMapWithChrAndPos(MarkerSet rdMarkerSet) {

		rdMarkerSet.initFullMarkerIdSetMap();

		rdMarkerSet.fillWith(new char[0]);
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		Map<MarkerKey, char[]> chrMap = new LinkedHashMap<MarkerKey, char[]>(rdMarkerSet.getMarkerIdSetMapCharArray());
		Map<MarkerKey, char[]> workMap = new LinkedHashMap<MarkerKey, char[]>(chrMap.size());
		rdMarkerSet.fillWith(new char[0]);
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Map.Entry<MarkerKey, char[]> entry : chrMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			char[] chr = entry.getValue();
			Integer pos = rdMarkerSet.getMarkerIdSetMapInteger().get(markerKey);
			StringBuilder sbKey = new StringBuilder(new String(chr));
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos.toString());
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerKey.getMarkerId());
			workMap.put(markerKey, sbKey.toString().toCharArray());
		}
		if (rdMarkerSet.getMarkerIdSetMapInteger() != null) {
			rdMarkerSet.getMarkerIdSetMapInteger().clear();
		}

		return workMap;
	}

	protected Map<MarkerKey, MarkerMetadata> mingleAndSortMarkerSet() {

		Map<MarkerKey, char[]> workMap = getMatrixMapWithChrAndPos(rdMarkerSet1);
		workMap.putAll(getMatrixMapWithChrAndPos(rdMarkerSet2));

		// SORT MERGED Map
		SortedMap<String, MarkerKey> sortedMetadataTM = new TreeMap<String, MarkerKey>(new ComparatorChrAutPosMarkerIdAsc());
		for (Map.Entry<MarkerKey, char[]> entry : workMap.entrySet()) {
			MarkerKey key = entry.getKey();
			String value = new String(entry.getValue());
			sortedMetadataTM.put(value, key);
		}

		// PACKAGE IN A Map
		Map<MarkerKey, MarkerMetadata> result = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		for (Map.Entry<String, MarkerKey> entry : sortedMetadataTM.entrySet()) {
			String key = entry.getKey();
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			MarkerMetadata markerInfo = new MarkerMetadata(
					keyValues[0], // chr
					Integer.parseInt(keyValues[1])); // pos

			MarkerKey markerKey = entry.getValue();
			result.put(markerKey, markerInfo);
		}

		return result;
	}

	protected static Map<SampleKey, int[]> getComboSampleSetWithIndicesArray(Map<SampleKey, ?> sampleSetMap1, Map<SampleKey, ?> sampleSetMap2) {
		Map<SampleKey, int[]> resultMap = new LinkedHashMap<SampleKey, int[]>();

		int wrPos = 0;
		int rdPos = 0;
		for (SampleKey key : sampleSetMap1.keySet()) {
			int[] position = new int[] {1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleSetMap2.keySet()) {
			int[] position;
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				position = resultMap.get(key);
				position[0] = 2; // rdMatrixNb
				position[1] = rdPos; // rdPos
			} else {
				position = new int[]{2, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			}

			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		return resultMap;
	}

	protected double[] checkForMismatches(int wrMatrixId) throws IOException, InvalidRangeException {
		double[] result = new double[2];

		wrMatrixMetadata = MatricesList.getMatrixMetadataById(wrMatrixId);
		wrSampleSet = new SampleSet(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet = new MarkerSet(wrMatrixMetadata.getStudyId(), wrMatrixId);
		wrMarkerSet.initFullMarkerIdSetMap();
		Map<SampleKey, char[]> wrSampleSetMap = wrSampleSet.getSampleIdSetMapCharArray();

		NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

		// Iterate through markerset, take it marker by marker
		int markerNb = 0;
		double mismatchCount = 0;

		// Iterate through markerSet
		for (MarkerKey markerKey : wrMarkerSet.getMarkerKeys()) {
			Map<Character, Integer> knownAlleles = new LinkedHashMap<Character, Integer>();

			// Get a sampleset-full of GTs
			wrSampleSet.readAllSamplesGTsFromCurrentMarkerToStringMap(rdNcFile, wrSampleSetMap, markerNb);

			// Iterate through sampleSet
			for (char[] tempGT : wrSampleSetMap.values()) {
				// Gather alleles different from 0 into a list of known alleles and count the number of appearences
				if (tempGT[0] != '0') {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[0])) {
						tempCount = knownAlleles.get(tempGT[0]);
					}
					knownAlleles.put(tempGT[0], tempCount + 1);
				}
				if (tempGT[1] != '0') {
					int tempCount = 0;
					if (knownAlleles.containsKey(tempGT[1])) {
						tempCount = knownAlleles.get(tempGT[1]);
					}
					knownAlleles.put(tempGT[1], tempCount + 1);
				}
			}

			if (knownAlleles.size() > 2) {
				mismatchCount++;
			}

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Checking markers for mismatches: {}", markerNb);
			}
		}

		double mismatchRatio = mismatchCount / wrSampleSet.getSampleSetSize();
		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}
