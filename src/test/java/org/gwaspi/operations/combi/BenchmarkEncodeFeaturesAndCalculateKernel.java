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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.operations.NetCdfUtils;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.progress.NullProgressHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkEncodeFeaturesAndCalculateKernel {

	private static final Logger log = LoggerFactory.getLogger(BenchmarkEncodeFeaturesAndCalculateKernel.class);

	public static void main(String[] args) throws Exception {

		boolean isInitiated = Config.initPreferences(true, null, null);

		final List<StudyKey> studies = StudyList.getStudies();
		MatrixKey matrix = null;
		for (final StudyKey study : studies) {
			final List<MatrixKey> studyMatrices = MatricesList.getMatrixList(study);
			if (!studyMatrices.isEmpty()) {
				matrix = studyMatrices.get(0);
				break;
			}
		}
		OperationMetadata qaMarkersOperation = OperationsList.getOffspringOperationsMetadata(matrix, OPType.MARKER_QA).get(0);
		OperationKey qaMarkersOperationKey = OperationKey.valueOf(qaMarkersOperation);
		OperationManager.generateOperationDataSet(qaMarkersOperationKey);
		QAMarkersOperationDataSet parentQAMarkersOperationDataSet
				= (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(qaMarkersOperationKey);

		log.debug("#samples: " + parentQAMarkersOperationDataSet.getNumSamples());
		log.debug("#markers: " + parentQAMarkersOperationDataSet.getNumMarkers());

		final Map<String, GenotypeEncoder> genotypeEncoders = CombiTestScriptCommand.GENOTYPE_ENCODERS;
		final int[] kernelCalculationAlgorithms = new int[] {2, 3, 4, 5};
		final int[] chunkSizes = new int[] {1, 10, 100, 1000};
		final boolean[] arrayCopyStates = new boolean[] {/*true, */false};

		final StringBuilder benchmarkSummary = new StringBuilder("\n");
		String header = ("WCT(s)\tGT-enc\tkern-a\tchunk-s\tarray-c");
		log.info(header);
		benchmarkSummary.append(header).append("\n");
		for (Map.Entry<String, GenotypeEncoder> genotypeEncoder : genotypeEncoders.entrySet()) {
			for (int kernelCalculationAlgorithm : kernelCalculationAlgorithms) {
				CombiTestMatrixOperation.KERNEL_CALCULATION_ALGORTIHM = kernelCalculationAlgorithm;
				for (int chunkSize : chunkSizes) {
					for (boolean arrayCopy : arrayCopyStates) {
						NetCdfUtils.ARRAY_COPY = arrayCopy;
						final long wallClockTime = runCombi(parentQAMarkersOperationDataSet, genotypeEncoder.getValue(), chunkSize);
						String benchmarkEntry = String.format(
								"%d\t%s\t%d\t%d\t%s",
								wallClockTime / 1000,
								genotypeEncoder.getKey(),
								kernelCalculationAlgorithm,
								chunkSize,
								arrayCopy ? "yes" : "no"
						);
						log.info(benchmarkEntry);
						benchmarkSummary.append(benchmarkEntry).append("\n");
					}
				}
			}
		}
		log.info(benchmarkSummary.toString());
	}

	private static long runCombi(
			QAMarkersOperationDataSet parentQAMarkersOperationDataSet,
			GenotypeEncoder genotypeEncoder,
			int chunkSize)
			throws IOException
	{
//		DataSetSource parentDataSetSource = getParentDataSetSource();
////		MarkerCensusOperationDataSet parentMarkerCensusOperationDataSet
//////				= (MarkerCensusOperationDataSet) OperationManager.generateOperationDataSet(params.getCensusOperationKey());
////				= (MarkerCensusOperationDataSet) parentDataSetSource;
//		QAMarkersOperationDataSet parentQAMarkersOperationDataSet
////				= (MarkerCensusOperationDataSet) OperationManager.generateOperationDataSet(params.getCensusOperationKey());
//				= (QAMarkersOperationDataSet) parentDataSetSource;

//		CombiTestOperationDataSet dataSet = generateFreshOperationDataSet();

//		dataSet.setNumMarkers(parentDataSetSource.getNumMarkers());
//		dataSet.setNumChromosomes(parentDataSetSource.getNumChromosomes());
//		dataSet.setNumSamples(parentDataSetSource.getNumSamples());

//		final int dSamples = parentDataSetSource.getNumMarkers();
//		final int dEncoded = dSamples * getParams().getEncoder().getEncodingFactor();
//		final int n = parentDataSetSource.getNumSamples();

		final List<MarkerKey> markerKeys = parentQAMarkersOperationDataSet.getMarkersKeysSource();
//		final List<SampleKey> validSamplesKeys = parentQAMarkersOperationDataSet.getSamplesKeysSource();
//		final List<SampleInfo.Affection> validSampleAffections = parentQAMarkersOperationDataSet.getSamplesInfosSource().getAffections();
		final int numSamples = parentQAMarkersOperationDataSet.getNumSamples();

//		final List<Census> allMarkersCensus = parentMarkerCensusOperationDataSet.getCensus(HardyWeinbergOperationEntry.Category.ALL);
		final List<Byte> majorAlleles = parentQAMarkersOperationDataSet.getKnownMajorAllele();
		final List<Byte> minorAlleles = parentQAMarkersOperationDataSet.getKnownMinorAllele();
		final List<int[]> markerGenotypesCounts = parentQAMarkersOperationDataSet.getGenotypeCounts();
		final MarkersGenotypesSource markersGenotypesSource = parentQAMarkersOperationDataSet.getMarkersGenotypesSource();

		MarkerGenotypesEncoder markerGenotypesEncoder = CombiTestMatrixOperation.createMarkerGenotypesEncoder(
				markersGenotypesSource,
				majorAlleles,
				minorAlleles,
				markerGenotypesCounts,
				genotypeEncoder,
				markerKeys.size(),
				numSamples,
				chunkSize);

		final long startTime = System.currentTimeMillis();
//		List<Double> weights = CombiTestMatrixOperation.runEncodingAndSVM(
//				markerKeys,
//				majorAlleles,
//				minorAlleles,
//				markerGenotypesCounts,
//				validSamplesKeys,
//				validSampleAffections,
//				markersGenotypesSource,
//				genotypeEncoder);
		float[][] kernelMatrix = CombiTestMatrixOperation.encodeFeaturesAndCalculateKernel(
				numSamples,
				markerGenotypesEncoder,
				new NullProgressHandler<Integer>(null));
		final long endTime = System.currentTimeMillis();

		return endTime - startTime;
	}
}
