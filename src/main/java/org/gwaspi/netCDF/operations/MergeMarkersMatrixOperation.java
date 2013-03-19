package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MergeMarkersMatrixOperation extends AbstractMergeMarkersMatrixOperation {

	public MergeMarkersMatrixOperation(
			int studyId,
			int rdMatrix1Id,
			int rdMatrix2Id,
			String wrMatrixFriendlyName,
			String wrMatrixDescription)
			throws IOException, InvalidRangeException
	{
		super(
				studyId,
				rdMatrix1Id,
				rdMatrix2Id,
				wrMatrixFriendlyName,
				wrMatrixDescription);
	}

	@Override
	public int processMatrix() throws IOException, InvalidRangeException {

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, Object> rdSampleSetMap1 = rdSampleSet1.getSampleIdSetMap();
		Map<SampleKey, Object> rdSampleSetMap2 = rdSampleSet2.getSampleIdSetMap();
		Map<SampleKey, Object> wrSampleSetMap = getSampleSetWithIndicesMap(rdSampleSetMap1, rdSampleSetMap2);
		Map<SampleKey, Object> theSamples = rdSampleSetMap1;

		final int numSamples = rdMatrix1Metadata.getSampleSetSize(); // Keep rdMatrix1Metadata from Matrix1. SampleSet is constant
		final String humanReadableMethodName = Text.Trafo.mergeMarkersOnly;
		final String methodDescription = Text.Trafo.mergeMethodMarkerJoin;

		return mergeMatrices(
				rdSampleSetMap1,
				rdSampleSetMap2,
				wrSampleSetMap,
				theSamples,
				numSamples,
				humanReadableMethodName,
				methodDescription);
	}

	@Override
	protected void writeGenotypes(
			NetcdfFileWriteable wrNcFile,
			Map<SampleKey, Object> wrSampleSetMap,
			Map<MarkerKey, Object> wrComboSortedMarkerSetMap,
			Map<SampleKey, Object> rdSampleSetMap1,
			Map<SampleKey, Object> rdSampleSetMap2)
			throws InvalidRangeException, IOException
	{
		// Get SampleId index from each Matrix
		// Iterate through wrSampleSetMap
		for (Object value : wrSampleSetMap.values()) {
			int[] sampleIndices = (int[]) value; // position[rdPos matrix 1, rdPos matrix 2]

			// Read from Matrix1
			rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			rdMarkerSet1.fillGTsForCurrentSampleIntoInitMap(sampleIndices[0]);

			// Read from Matrix2
			rdMarkerSet2.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			rdMarkerSet2.fillGTsForCurrentSampleIntoInitMap(sampleIndices[1]);

			// Fill wrSortedMingledMarkerMap with matrix 1+2 Genotypes
			for (Map.Entry<MarkerKey, Object> markerEntry : wrComboSortedMarkerSetMap.entrySet()) {
				MarkerKey markerKey = markerEntry.getKey();
				byte[] genotype = cNetCDF.Defaults.DEFAULT_GT;
				if (rdMarkerSet1.getMarkerIdSetMap().containsKey(markerKey)) {
					genotype = (byte[]) rdMarkerSet1.getMarkerIdSetMap().get(markerKey);
				}
				if (rdMarkerSet2.getMarkerIdSetMap().containsKey(markerKey)) {
					genotype = (byte[]) rdMarkerSet2.getMarkerIdSetMap().get(markerKey);
				}

				markerEntry.setValue(genotype);
			}

			// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrComboSortedMarkerSetMap, sampleIndices[0]);
		}
	}

	private static Map<SampleKey, Object> getSampleSetWithIndicesMap(Map<SampleKey, Object> sampleSetMap1, Map<SampleKey, Object> sampleSetMap2) {
		Map<SampleKey, Object> resultMap = new LinkedHashMap<SampleKey, Object>();

		int rdPos = 0;
		for (SampleKey key : sampleSetMap1.keySet()) {
			int[] position = new int[]{rdPos, 0}; // rdPos matrix 1
			resultMap.put(key, position);
			rdPos++;
		}

		rdPos = 0;
		for (SampleKey key : sampleSetMap2.keySet()) {
			// IF SAMPLE ALLREADY EXISTS IN MATRIX1 SUBSTITUTE VALUES WITH MATRIX2
			if (resultMap.containsKey(key)) {
				int[] position = (int[]) resultMap.get(key);
				position[1] = rdPos; // rdPos matrix 2
				resultMap.put(key, position);
			}

			rdPos++;
		}

		return resultMap;
	}
}
