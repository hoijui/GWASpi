package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
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

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
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

	protected Map<MarkerKey, Object> mingleAndSortMarkerSet() {

		// GET 1st MATRIX Map WITH CHR AND POS
		Map<MarkerKey, Object> workMap = new LinkedHashMap<MarkerKey, Object>();
		rdMarkerSet1.initFullMarkerIdSetMap();
		rdMarkerSet2.initFullMarkerIdSetMap();
		rdMarkerSet1.fillWith("");
		rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		workMap.putAll(rdMarkerSet1.getMarkerIdSetMap());
		rdMarkerSet1.fillWith("");
		rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Map.Entry<MarkerKey, Object> entry : workMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			String chr = entry.getValue().toString();
			String pos = rdMarkerSet1.getMarkerIdSetMap().get(markerKey).toString();
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerKey.getMarkerId());
			entry.setValue(sbKey.toString());
		}
		if (rdMarkerSet1.getMarkerIdSetMap() != null) {
			rdMarkerSet1.getMarkerIdSetMap().clear();
		}

		// GET 2nd MATRIX Map WITH CHR AND POS
		Map<MarkerKey, Object> workMap2 = new LinkedHashMap<MarkerKey, Object>();
		rdMarkerSet2.fillWith("");
		rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		workMap2.putAll(rdMarkerSet2.getMarkerIdSetMap());
		rdMarkerSet2.fillWith("");
		rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		for (Map.Entry<MarkerKey, Object> entry : workMap2.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			String chr = entry.getValue().toString();
			String pos = rdMarkerSet2.getMarkerIdSetMap().get(markerKey).toString();
			StringBuilder sbKey = new StringBuilder(chr);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(pos);
			sbKey.append(cNetCDF.Defaults.TMP_SEPARATOR);
			sbKey.append(markerKey.getMarkerId());
			entry.setValue(sbKey.toString());
		}
		if (rdMarkerSet2.getMarkerIdSetMap() != null) {
			rdMarkerSet2.getMarkerIdSetMap().clear();
		}

		workMap.putAll(workMap2);

		// SORT MERGED Map
		SortedMap<String, MarkerKey> sortedMetadataTM = new TreeMap<String, MarkerKey>(new ComparatorChrAutPosMarkerIdAsc());
		for (Map.Entry<MarkerKey, Object> entry : workMap.entrySet()) {
			MarkerKey key = entry.getKey();
			String value = entry.getValue().toString();
			sortedMetadataTM.put(value, key);
		}
		if (workMap != null) {
			workMap.clear();
		}

		// PACKAGE IN AN Map
		for (Map.Entry<String, MarkerKey> entry : sortedMetadataTM.entrySet()) {
			String key = entry.getKey();
			String[] keyValues = key.split(cNetCDF.Defaults.TMP_SEPARATOR);
			Object[] markerInfo = new Object[2];
			markerInfo[0] = keyValues[0]; // => chr
			markerInfo[1] = Integer.parseInt(keyValues[1]); // => pos

			MarkerKey markerKey = entry.getValue();
			workMap.put(markerKey, markerInfo);
		}

		return workMap;
	}

	protected static Map<SampleKey, Object> getComboSampleSetWithIndicesArray(Map<SampleKey, Object> sampleSetMap1, Map<SampleKey, Object> sampleSetMap2) {
		Map<SampleKey, Object> resultMap = new LinkedHashMap<SampleKey, Object>();

		int wrPos = 0;
		int rdPos = 0;
		for (SampleKey key : sampleSetMap1.keySet()) {
			int[] position = new int[]{1, rdPos, wrPos}; // rdMatrixNb, rdPos, wrPos
			resultMap.put(key, position);
			wrPos++;
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleSetMap2.keySet()) {
			int[] position;
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				position = (int[]) resultMap.get(key);
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
		Map<SampleKey, Object> wrSampleSetMap = wrSampleSet.getSampleIdSetMap();

		NetcdfFile rdNcFile = NetcdfFile.open(wrMatrixMetadata.getPathToMatrix());

		// Iterate through markerset, take it marker by marker
		int markerNb = 0;
		double mismatchCount = 0;

		// Iterate through markerSet
		for (MarkerKey markerKey : wrMarkerSet.getMarkerIdSetMap().keySet()) {
			Map<Character, Object> knownAlleles = new LinkedHashMap<Character, Object>();

			// Get a sampleset-full of GTs
			wrSampleSetMap = wrSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, wrSampleSetMap, markerNb);

			// Iterate through sampleSet
			for (Object value : wrSampleSetMap.values()) {
				char[] tempGT = value.toString().toCharArray();

				// Gather alleles different from 0 into a list of known alleles and count the number of appearences
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
				log.info("Checking markers for mismatches: {}", markerNb);
			}
		}

		double mismatchRatio = mismatchCount / wrSampleSet.getSampleSetSize();
		result[0] = mismatchCount;
		result[1] = mismatchRatio;

		return result;
	}
}
