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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMergeMarkersMatrixOperation extends AbstractMergeMatrixOperation {

	private static final Logger log = LoggerFactory.getLogger(AbstractMergeMarkersMatrixOperation.class);

	public AbstractMergeMarkersMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		super(
				dataSetSource1,
				dataSetSource2,
				dataSetDestination);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	/**
	 * Mingles markers and keeps samples constant.
	 */
	protected MatrixKey mergeMatrices(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<SampleKey> sampleKeys,
			final int numSamples,
			final String humanReadableMethodName,
			final String methodDescription)
			throws IOException
	{
		MatrixKey resultMatrixKey = null;

		MatrixMetadata rdMatrix1Metadata = dataSetSource1.getMatrixMetadata();
		MatrixMetadata rdMatrix2Metadata = dataSetSource2.getMatrixMetadata();

		Map<MarkerKey, MarkerMetadata> wrCombinedSortedMarkersMetadata = mingleAndSortMarkerSet();

		// RETRIEVE CHROMOSOMES INFO
		Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(wrCombinedSortedMarkersMetadata, 0, 1);

		GenotypeEncoding genotypeEncoding1 = rdMatrix1Metadata.getGenotypeEncoding();

		final boolean hasCombinedDictionary = (rdMatrix1Metadata.getHasDictionray() && rdMatrix2Metadata.getHasDictionray());

//			rdMarkerSet1.initFullMarkerIdSetMap();
//			rdMarkerSet2.initFullMarkerIdSetMap();

		Map<MarkerKey, String> combinedMarkerRSIDs = new LinkedHashMap<MarkerKey, String>(wrCombinedSortedMarkersMetadata.size());
		Map<MarkerKey, String> combinedMarkerBasesDicts = null;
		if (hasCombinedDictionary) {
			combinedMarkerBasesDicts = new LinkedHashMap<MarkerKey, String>();
		}
		Map<MarkerKey, String> combinedMarkerGTStrands = new LinkedHashMap<MarkerKey, String>();

//			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//			combinedMarkerRSIDs.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//			rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//			combinedMarkerRSIDs.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

		Iterator<MarkerKey> markersKeysSource1It = dataSetSource1.getMarkersKeysSource().iterator();
		for (MarkerMetadata markerMetadata : dataSetSource1.getMarkersMetadatasSource()) {
			MarkerKey key = markersKeysSource1It.next();
			combinedMarkerRSIDs.put(key, markerMetadata.getRsId());
			if (hasCombinedDictionary) {
				combinedMarkerBasesDicts.put(key, markerMetadata.getAlleles());
			}
			combinedMarkerGTStrands.put(key, markerMetadata.getStrand());
		}
		Iterator<MarkerKey> markersKeysSource2It = dataSetSource2.getMarkersKeysSource().iterator();
		for (MarkerMetadata markerMetadata : dataSetSource2.getMarkersMetadatasSource()) {
			MarkerKey key = markersKeysSource2It.next();
			combinedMarkerRSIDs.put(key, markerMetadata.getRsId());
			if (hasCombinedDictionary) {
				combinedMarkerBasesDicts.put(key, markerMetadata.getAlleles());
			}
			combinedMarkerGTStrands.put(key, markerMetadata.getStrand());
		}

//			Map<MarkerKey, byte[]> combinedMarkerBasesDicts = null;
//			if (hasCombinedDictionary) {
//				combinedMarkerBasesDicts = new LinkedHashMap<MarkerKey, byte[]>();
//				rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//				combinedMarkerBasesDicts.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//				rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//				combinedMarkerBasesDicts.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());
//			}

//			Map<MarkerKey, String> combinedMarkerGTStrands = new LinkedHashMap<MarkerKey, String>();
//			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//			combinedMarkerGTStrands.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//			rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//			combinedMarkerGTStrands.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

		for (Map.Entry<MarkerKey, MarkerMetadata> markerEntry : wrCombinedSortedMarkersMetadata.entrySet()) {
			MarkerKey markerKey = markerEntry.getKey();
			MarkerMetadata origMarkerMetadata = markerEntry.getValue();
			MarkerMetadata newMarkerMetadata = new MarkerMetadata(
					origMarkerMetadata.getMarkerId(),
					combinedMarkerRSIDs.get(markerKey),
					origMarkerMetadata.getChr(),
					origMarkerMetadata.getPos(),
					hasCombinedDictionary ? combinedMarkerBasesDicts.get(markerKey) : origMarkerMetadata.getAlleles(),
					combinedMarkerGTStrands.get(markerKey));
			markerEntry.setValue(newMarkerMetadata);
		}

		AbstractNetCDFDataSetDestination.saveSamplesMatadata(sampleKeys, wrNcFile);
		AbstractNetCDFDataSetDestination.saveMarkersMatadata(wrCombinedSortedMarkersMetadata.values(), chromosomeInfo, hasCombinedDictionary, null, wrNcFile);

		writeGenotypesMeta(wrSampleSetMap, wrCombinedSortedMarkersMetadata, rdSampleSetMap1, rdSampleSetMap2);

		resultMatrixKey = matrixFactory.getResultMatrixKey();

		// CHECK FOR MISMATCHES
		if (genotypeEncoding1.equals(GenotypeEncoding.ACGT0)
				|| genotypeEncoding1.equals(GenotypeEncoding.O1234))
		{
			double[] mismatchState = checkForMismatches(resultMatrixKey); // mismatchCount, mismatchRatio
			if (mismatchState[1] > 0.01) {
				log.warn("");
				log.warn("Mismatch ratio is bigger than 1% ({}%)!", (mismatchState[1] * 100));
				log.warn("There might be an issue with strand positioning of your genotypes!");
				log.warn("");
				//resultMatrixId = new int[] {wrMatrixHandler.getResultMatrixId(),-4};  // The threshold of acceptable mismatching genotypes has been crossed
			}
		}

		org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");

		return resultMatrixKey;
	}

	protected void writeGenotypesMeta(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<MarkerKey> wrComboSortedMarkers,
			Map<SampleKey, byte[]> rdSampleSetMap1,
			Map<SampleKey, byte[]> rdSampleSetMap2)
			throws IOException
	{
		initiateGenotypesMismatchChecking(wrComboSortedMarkers.size());
		writeGenotypesMeta(wrSampleSetMap, wrComboSortedMarkers, rdSampleSetMap1, rdSampleSetMap2);
	}

	protected abstract void writeGenotypes(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<MarkerKey> wrComboSortedMarkers,
			Map<SampleKey, byte[]> rdSampleSetMap1,
			Map<SampleKey, byte[]> rdSampleSetMap2)
			throws IOException;
}
