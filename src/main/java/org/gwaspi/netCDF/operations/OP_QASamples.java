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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.netCDF.markers.NetCDFDataSetSource;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.qasamples.NetCdfQASamplesOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_QASamples implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_QASamples.class);

	private final MatrixKey rdMatrixKey;

	public OP_QASamples(MatrixKey rdMatrixKey) {
		this.rdMatrixKey = rdMatrixKey;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {
		int resultOpId = Integer.MIN_VALUE;

		DataSetSource dataSetSource = new NetCDFDataSetSource(rdMatrixKey);

		Map<SampleKey, Integer> wrSampleSetMissingCountMap = new LinkedHashMap<SampleKey, Integer>();
		Map<SampleKey, Double> wrSampleSetMissingRatioMap = new LinkedHashMap<SampleKey, Double>();
		Map<SampleKey, Double> wrSampleSetHetzyRatioMap = new LinkedHashMap<SampleKey, Double>();

//		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		SamplesGenotypesSource rdMarkerSet = dataSetSource.getSamplesGenotypesSource();
		MarkersKeysSource rdMarkersKeysSource = dataSetSource.getMarkersKeysSource();
//		rdMarkerSet.initFullMarkerIdSetMap();

//		MarkersChromosomeInfosSource markersChrInfSrc = rdMarkerSet.getChrInfoSetMap();
		MarkersMetadataSource markersInfSrc = dataSetSource.getMarkersMetadatasSource();

		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; // This to test heap usage of copying locally the Map from markerset

		SampleSet rdSampleSet = new SampleSet(rdMatrixKey);

		// Iterate through samples
		int sampleNb = 0;
		Iterator<GenotypesList> samplesGenotypesIt = rdMarkerSet.iterator();
		for (SampleKey sampleKey : rdSampleSet.getSampleKeys()) {
			Integer missingCount = 0;
			Integer heterozygCount = 0;

			// Iterate through markerset
			GenotypesList sampleGenotypes = samplesGenotypesIt.next();
			int markerIndex = 0;
			Iterator<MarkerMetadata> markersInfSrcIt = markersInfSrc.iterator();
			for (byte[] tempGT : sampleGenotypes) {
				if (tempGT[0] == AlleleBytes._0 && tempGT[1] == AlleleBytes._0) {
					missingCount++;
				}

				// WE DON'T WANT NON AUTOSOMAL CHR FOR HETZY
				String currentChr = markersInfSrcIt.next().getChr();
				if (!currentChr.equals("X")
						&& !currentChr.equals("Y")
						&& !currentChr.equals("XY")
						&& !currentChr.equals("MT")
						&& (tempGT[0] != tempGT[1]))
				{
					heterozygCount++;
				}
				markerIndex++;
			}

			wrSampleSetMissingCountMap.put(sampleKey, missingCount);

			double missingRatio = (double) missingCount / markersInfSrc.size();
			wrSampleSetMissingRatioMap.put(sampleKey, missingRatio);
			double heterozygRatio = (double) heterozygCount / (markersInfSrc.size() - missingCount);
			wrSampleSetHetzyRatioMap.put(sampleKey, heterozygRatio);

			sampleNb++;

			if (sampleNb % 100 == 0) {
				log.info("Processed samples: {}", sampleNb);
			}
		}

		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixKey);

			QASamplesOperationDataSet dataSet = new NetCdfQASamplesOperationDataSet(); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setReadMatrixKey(rdMatrixKey); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(rdMatrixMetadata.getMarkerSetSize()); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumSamples(wrSampleSetMissingCountMap.size()); // HACK

//			dataSet.setSamples(rdSampleSet.getSampleKeys());
//			dataSet.setMarkers(rdMarkersKeysSource);
//			Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = rdMarkerSet.getChrInfoSetMap();
//			dataSet.setChromosomes(chromosomeInfo.keySet(), chromosomeInfo.values());
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllSamples(rdMatrixKey);
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllMarkers(rdMatrixKey);
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllChromosomes(rdMatrixKey);

			dataSet.setSampleMissingRatios(wrSampleSetMissingRatioMap.values());
			dataSet.setSampleMissingCount(wrSampleSetMissingCountMap.values());
			dataSet.setSampleHetzyRatios(wrSampleSetHetzyRatioMap.values());

			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK
		} finally {
//			if (null != rdNcFile) {
//				try {
//					rdNcFile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file " + rdNcFile, ex);
//				}
//			}

			org.gwaspi.global.Utils.sysoutCompleted("Sample QA");
		}

		return resultOpId;
	}
}
