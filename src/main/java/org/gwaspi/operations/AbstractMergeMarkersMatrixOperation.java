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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;

public abstract class AbstractMergeMarkersMatrixOperation extends AbstractMergeMatrixOperation {

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
	protected void mergeMatrices(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<SampleKey> sampleKeys,
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

//		GenotypeEncoding genotypeEncoding1 = rdMatrix1Metadata.getGenotypeEncoding();

		final boolean hasCombinedDictionary = (rdMatrix1Metadata.getHasDictionary() && rdMatrix2Metadata.getHasDictionary());

//		rdMarkerSet1.initFullMarkerIdSetMap();
//		rdMarkerSet2.initFullMarkerIdSetMap();

		Map<MarkerKey, String> combinedMarkerRSIDs = new LinkedHashMap<MarkerKey, String>(wrCombinedSortedMarkersMetadata.size());
		Map<MarkerKey, String> combinedMarkerBasesDicts = null;
		if (hasCombinedDictionary) {
			combinedMarkerBasesDicts = new LinkedHashMap<MarkerKey, String>();
		}
		Map<MarkerKey, String> combinedMarkerGTStrands = new LinkedHashMap<MarkerKey, String>();

//		rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		combinedMarkerRSIDs.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//		rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
//		combinedMarkerRSIDs.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

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

//		Map<MarkerKey, byte[]> combinedMarkerBasesDicts = null;
//		if (hasCombinedDictionary) {
//			combinedMarkerBasesDicts = new LinkedHashMap<MarkerKey, byte[]>();
//			rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//			combinedMarkerBasesDicts.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//			rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_BASES_DICT);
//			combinedMarkerBasesDicts.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());
//		}
//
//		Map<MarkerKey, String> combinedMarkerGTStrands = new LinkedHashMap<MarkerKey, String>();
//		rdMarkerSet1.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//		combinedMarkerGTStrands.putAll(rdMarkerSet1.getMarkerIdSetMapCharArray());
//		rdMarkerSet2.fillInitMapWithVariable(cNetCDF.Variables.VAR_GT_STRAND);
//		combinedMarkerGTStrands.putAll(rdMarkerSet2.getMarkerIdSetMapCharArray());

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

		dataSetDestination.init();

		// NOTE We do not need to safe the sample-info again,
		//   cause it is already stored in the study
		//   from the two matrices we are merging
		// FIXME the above only applies to NetCDF!
		dataSetDestination.startLoadingSampleInfos(true);
		for (SampleKey sampleKey : sampleKeys) {
			dataSetDestination.addSampleKey(sampleKey);
		}
		dataSetDestination.finishedLoadingSampleInfos();

		// copy & paste the marker-metadata from matrix 1
		dataSetDestination.startLoadingMarkerMetadatas(false);
		for (MarkerMetadata markerMetadata : wrCombinedSortedMarkersMetadata.values()) {
			dataSetDestination.addMarkerMetadata(markerMetadata);
		}
		dataSetDestination.finishedLoadingMarkerMetadatas();

		// RETRIEVE CHROMOSOMES INFO
		dataSetDestination.startLoadingChromosomeMetadatas();
		for (Map.Entry<ChromosomeKey, ChromosomeInfo> chromosomeEntry : chromosomeInfo.entrySet()) {
			dataSetDestination.addChromosomeMetadata(chromosomeEntry.getKey(), chromosomeEntry.getValue());
		}
		dataSetDestination.finishedLoadingChromosomeMetadatas();

		writeGenotypesMeta(wrSampleSetMap, wrCombinedSortedMarkersMetadata.keySet());

		dataSetDestination.done();

		org.gwaspi.global.Utils.sysoutCompleted("extraction to new Matrix");
	}

	protected void writeGenotypesMeta(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<MarkerKey> wrComboSortedMarkers)
			throws IOException
	{
		initiateGenotypesMismatchChecking(wrComboSortedMarkers.size());
		writeGenotypes(wrSampleSetMap, wrComboSortedMarkers);
		finalizeGenotypesMismatchChecking();
		validateMissingRatio();
	}

	protected abstract void writeGenotypes(
			Map<SampleKey, int[]> wrSampleSetMap,
			Collection<MarkerKey> wrComboSortedMarkers)
			throws IOException;
}
