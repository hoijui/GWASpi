package org.gwaspi.netCDF.operations;

import java.io.IOException;
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
public class MergeAllMatrixOperation extends AbstractMergeMarkersMatrixOperation {

	public MergeAllMatrixOperation(
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

	/**
	 * Mingles markers and keeps samples constant.
	 */
	@Override
	public int processMatrix() throws IOException, InvalidRangeException {

		// Get combo SampleSet with position[] (wrPos, rdMatrixNb, rdPos)
		Map<SampleKey, Object> rdSampleSetMap1 = rdSampleSet1.getSampleIdSetMap();
		Map<SampleKey, Object> rdSampleSetMap2 = rdSampleSet2.getSampleIdSetMap();
		Map<SampleKey, Object> wrSampleSetMap = getComboSampleSetWithIndicesArray(rdSampleSetMap1, rdSampleSetMap2);
		Map<SampleKey, Object> theSamples = wrSampleSetMap;

		final int numSamples = wrSampleSetMap.size(); // Comboed SampleSet
		final String humanReadableMethodName = Text.Trafo.mergeAll;
		final String methodDescription = Text.Trafo.mergeMethodMergeAll;

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
		int wrSampleIndex = 0;
		for (Map.Entry<SampleKey, Object> entry : wrSampleSetMap.entrySet()) {
			SampleKey sampleKey = entry.getKey();
			int[] rdSampleIndices = (int[]) entry.getValue(); // position[rdPos matrix 1, rdPos matrix 2]

			// Read from Matrix1
			rdMarkerSet1.fillWith(cNetCDF.Defaults.DEFAULT_GT);
			if (rdSampleSet1.getSampleIdSetMap().containsKey(sampleKey)) {
				rdMarkerSet1.fillGTsForCurrentSampleIntoInitMap(rdSampleIndices[0]);
			}

			// Read from Matrix2
			rdMarkerSet2.fillWith(cNetCDF.Defaults.DEFAULT_GT);

			if (rdSampleSet2.getSampleIdSetMap().containsKey(sampleKey)) {
				rdMarkerSet2.fillGTsForCurrentSampleIntoInitMap(rdSampleIndices[1]);
			}

			// Fill wrSortedMingledMarkerMap with matrix 1+2 Genotypes
			for (Map.Entry<MarkerKey, Object> markerEntry : wrComboSortedMarkerSetMap.entrySet()) {
				MarkerKey markerKey = markerEntry.getKey();
				byte[] genotype = cNetCDF.Defaults.DEFAULT_GT;
				if (rdSampleSetMap1.containsKey(sampleKey) && rdMarkerSet1.getMarkerIdSetMap().containsKey(markerKey)) {
					genotype = (byte[]) rdMarkerSet1.getMarkerIdSetMap().get(markerKey);
				}
				if (rdSampleSetMap2.containsKey(sampleKey) && rdMarkerSet2.getMarkerIdSetMap().containsKey(markerKey)) {
					genotype = (byte[]) rdMarkerSet2.getMarkerIdSetMap().get(markerKey);
				}

				markerEntry.setValue(genotype);
			}

			// Write wrMarkerIdSetMap to A3 ArrayChar and save to wrMatrix
			Utils.saveSingleSampleGTsToMatrix(wrNcFile, wrComboSortedMarkerSetMap, wrSampleIndex);
			wrSampleIndex++;
		}
	}
}
