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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
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
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public class OP_QASamples implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_QASamples.class);

	private final MatrixKey rdMatrixKey;

	public OP_QASamples(MatrixKey rdMatrixKey) {
		this.rdMatrixKey = rdMatrixKey;
	}

	@Override
	public int processMatrix() throws IOException {
		int resultOpId = Integer.MIN_VALUE;

		DataSetSource dataSetSource = new NetCDFDataSetSource(rdMatrixKey);

		Map<SampleKey, Integer> wrSampleSetMissingCountMap = new LinkedHashMap<SampleKey, Integer>();
		Map<SampleKey, Double> wrSampleSetMissingRatioMap = new LinkedHashMap<SampleKey, Double>();
		Map<SampleKey, Double> wrSampleSetHetzyRatioMap = new LinkedHashMap<SampleKey, Double>();

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixKey);

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

		// Write census, missing-ratio and mismatches to netCDF
		NetcdfFileWriteable wrNcFile = null;
		try {
			// CREATE netCDF-3 FILE

			OperationFactory wrOPHandler = new OperationFactory(
					rdMatrixMetadata.getStudyKey(),
					"Sample QA", //friendly name
					"Sample census on " + rdMatrixMetadata.getMatrixFriendlyName() + "\nSamples: " + wrSampleSetMissingCountMap.size(), //description
					wrSampleSetMissingCountMap.size(),
					rdMatrixMetadata.getMarkerSetSize(),
					0,
					OPType.SAMPLE_QA,
					rdMatrixKey, // Parent matrixId
					-1);                            // Parent operationId

			wrNcFile = wrOPHandler.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
			// SAMPLESET
			ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSet.getSampleKeys(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);

			int[] sampleOrig = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.VAR_OPSET, sampleOrig, samplesD2);
			log.info("Done writing SampleSet to matrix");

			// WRITE MARKERSET TO MATRIX
			ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkersKeysSource, cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, markersOrig, markersD2);
			log.info("Done writing MarkerSet to matrix");
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="CENSUS DATA WRITER">
			// MISSING RATIO
			NetCdfUtils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrSampleSetMissingRatioMap.values(), cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);

			// MISSING COUNT
			NetCdfUtils.saveIntMapD1ToWrMatrix(wrNcFile, wrSampleSetMissingCountMap.values(), cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);

			// HETEROZYGOSITY RATIO
			NetCdfUtils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrSampleSetHetzyRatioMap.values(), cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
			//</editor-fold>

			resultOpId = wrOPHandler.getResultOPId();
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		} finally {
//			if (null != rdNcFile) {
//				try {
//					rdNcFile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file " + rdNcFile, ex);
//				}
//			}
			if (null != wrNcFile) {
				try {
					wrNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file " + wrNcFile, ex);
				}
			}

			org.gwaspi.global.Utils.sysoutCompleted("Sample QA");
		}

		return resultOpId;
	}
}
