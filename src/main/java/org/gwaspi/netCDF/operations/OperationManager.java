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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperation;
import org.gwaspi.operations.allelicassociationtest.NetCdfAllelicAssociationTestsOperationDataSet;
import org.gwaspi.operations.combi.CombiTestMatrixOperation;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.combi.NetCdfCombiTestOperationDataSet;
import org.gwaspi.operations.filter.ByHardyWeinbergThresholdFilterOperation;
import org.gwaspi.operations.filter.ByHardyWeinbergThresholdFilterOperationParams;
import org.gwaspi.operations.filter.NetCdfSimpleOperationDataSet;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestOperation;
import org.gwaspi.operations.genotypicassociationtest.NetCdfGenotypicAssociationTestsOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.operations.hardyweinberg.NetCdfHardyWeinbergOperationDataSet;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.markercensus.NetCdfMarkerCensusOperationDataSet;
import org.gwaspi.operations.qamarkers.NetCdfQAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.NetCdfQASamplesOperationDataSet;
import org.gwaspi.operations.trendtest.NetCdfTrendTestOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.reports.OutputHardyWeinberg;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationManager {

	private static final Logger log = LoggerFactory.getLogger(OperationManager.class);

	private static final Map<Class<? extends MatrixOperation>, OperationTypeInfo> operationTypeInfos
			= new HashMap<Class<? extends MatrixOperation>, OperationTypeInfo>();

	private OperationManager() {
	}

	public static void registerOperationTypeInfo(final Class<? extends MatrixOperation> type, final OperationTypeInfo info) {
		operationTypeInfos.put(type, info);
	}

	public static OperationTypeInfo getOperationTypeInfo(final Class<? extends MatrixOperation> type) {
		return operationTypeInfos.get(type);
	}

	/**
	 * Creates a new OperationDataSet for the specified type.
	 * @param operationType
	 * @param origin
	 * @param parent
	 * @return
	 * @throws IOException
	 */
	public static OperationDataSet generateOperationDataSet(OPType operationType, MatrixKey origin, DataSetKey parent) throws IOException {
		return generateOperationDataSet(operationType, null, origin, parent);
	}

	public static OperationDataSet generateOperationDataSet(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = OperationsList.getOperationMetadata(operationKey);
		OPType operationType = operationMetadata.getOperationType();

		return generateOperationDataSet(operationType, operationKey, operationKey.getParentMatrixKey(), operationMetadata.getParent());
	}

	private static OperationDataSet generateOperationDataSet(OPType operationType, OperationKey operationKey, MatrixKey origin, DataSetKey parent) throws IOException {

		AbstractOperationDataSet operationDataSet;

		boolean useNetCdf = true;
		if (useNetCdf) {
			switch (operationType) {
				case SAMPLE_QA:
					operationDataSet = new NetCdfQASamplesOperationDataSet(origin, parent, operationKey);
					break;
				case MARKER_QA:
					operationDataSet = new NetCdfQAMarkersOperationDataSet(origin, parent, operationKey);
					break;
				case MARKER_CENSUS_BY_AFFECTION:
				case MARKER_CENSUS_BY_PHENOTYPE:
					operationDataSet = new NetCdfMarkerCensusOperationDataSet(origin, parent, operationKey);
					break;
				case HARDY_WEINBERG:
					operationDataSet = new NetCdfHardyWeinbergOperationDataSet(origin, parent, operationKey);
					break;
				case ALLELICTEST:
					operationDataSet = new NetCdfAllelicAssociationTestsOperationDataSet(origin, parent, operationKey);
					break;
				case GENOTYPICTEST:
					operationDataSet = new NetCdfGenotypicAssociationTestsOperationDataSet(origin, parent, operationKey);
					break;
				case COMBI_ASSOC_TEST:
					operationDataSet = new NetCdfCombiTestOperationDataSet(origin, parent, operationKey);
					break;
				case TRENDTEST:
					operationDataSet = new NetCdfTrendTestOperationDataSet(origin, parent, operationKey);
					break;
				case FILTER_BY_HW_THREASHOLD:
				case FILTER_BY_VALID_AFFECTION:
				case FILTER_BY_WEIGHTS:
					operationDataSet = new NetCdfSimpleOperationDataSet(origin, parent, operationKey);
					break;
				case SAMPLE_HTZYPLOT:
				case MANHATTANPLOT:
				case QQPLOT:
				default:
					throw new IllegalArgumentException("This operation type is invalid, or has no data-attached");
			}
		} else {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		return operationDataSet;
	}

	public static OperationKey censusCleanMatrixMarkers(
			final MarkerCensusOperationParams params)
			throws IOException
	{
		final String byWhat = (params.getPhenotypeFile() == null) ? "Affection" : "Phenotype (file: " + params.getPhenotypeFile().getName() + ")";
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count (by " + byWhat + ")");

		final MatrixOperation operation = new OP_MarkerCensus(params);

		final OperationKey operationKey = performOperation(operation);

		org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");

		return operationKey;
	}

	public static OperationKey performHardyWeinberg(HardyWeinbergOperationParams params) throws IOException {

		org.gwaspi.global.Utils.sysoutStart("Hardy-Weinberg");

		final MatrixOperation operation = new OP_HardyWeinberg(params);

		final OperationKey operationKey = performOperation(operation);

		OutputHardyWeinberg.writeReportsForMarkersHWData(operationKey, params.getMarkersQAOpKey());

		return operationKey;
	}

	public static OperationKey performCleanTests(
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			double hwThreshold,
			OPType testType)
			throws IOException
	{
		org.gwaspi.global.Utils.sysoutStart(OutputTest.createTestName(testType) + " Test using QA and HW thresholds");

		// TODO first check if such an exclude operation (with the same parameters) does already exist (would be a child of the hwOp), and if so, use it!
		// exclude by Hardy & Weinberg threshold
		final MatrixOperation excludeOperation = new ByHardyWeinbergThresholdFilterOperation(
				new ByHardyWeinbergThresholdFilterOperationParams(hwOpKey, null, hwOpKey, hwThreshold));
		final OperationKey excludeOperationKey = performOperation(excludeOperation);

		// run the test
		final MatrixOperation operation;
		if (testType == OPType.TRENDTEST) {
			operation = new OP_TrendTests(new TrendTestOperationParams(excludeOperationKey, censusOpKey));
		} else {
			final AssociationTestOperationParams params
					= new AssociationTestOperationParams(testType, excludeOperationKey, censusOpKey);
			if (testType == OPType.ALLELICTEST) {
				operation = new AllelicAssociationTestOperation(params);
			} else {
				operation = new GenotypicAssociationTestOperation(params);
			}
		}

		final OperationKey operationKey = performOperation(operation);

		return operationKey;
	}

	public static OperationKey performRawCombiTest(CombiTestOperationParams params)
			throws IOException
	{
		MatrixOperation operation = new CombiTestMatrixOperation(params);

		return performOperation(operation);
	}

	public static OperationKey performOperation(MatrixOperation operation)
			throws IOException
	{
		final DataSetKey parent = operation.getParams().getParent();

		final OperationKey resultOperationKey;
		if (operation.isValid()) {
			final int resultOperationId = operation.processMatrix();

			resultOperationKey = new OperationKey(parent.getOrigin(), resultOperationId);

			if (parent.isMatrix()) {
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultOperationKey);
			} else {
				GWASpiExplorerNodes.insertSubOperationUnderOperationNode(parent.getOperationParent(), resultOperationKey);
			}
		} else {
			resultOperationKey = new OperationKey(parent.getOrigin(), Integer.MIN_VALUE);
			log.error(
					"Can not execute {} operation, because the given parameters are invalid: {}",
					operation.getClass().getSimpleName(), // HACK should be getType(), but only Operation-Operations have this method
					operation.getProblemDescription());
		}

		return resultOperationKey;
	}

	public static OperationKey performQASamplesOperationAndCreateReports(
			OP_QASamples operation)
			throws IOException
	{
		OperationKey samplesQAOpKey = performOperation(operation);

		OutputQASamples.writeReportsForQASamplesData(samplesQAOpKey, true);

		return samplesQAOpKey;
	}

	public static OperationKey performQAMarkersOperationAndCreateReports(
			OP_QAMarkers operation)
			throws IOException
	{
		OperationKey markersQAOpKey = performOperation(operation);

		OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpKey);

		return markersQAOpKey;
	}

	public static List<OPType> checkForNecessaryOperations(List<OPType> necessaryOpTypes, DataSetKey rootKey, boolean childrenOnly) {

		try {
			final List<OperationMetadata> presentOperations;
			if (childrenOnly) {
				presentOperations = OperationsList.getChildrenOperationsMetadata(rootKey);
			} else {
				presentOperations = OperationsList.getOffspringOperationsMetadata(rootKey);
			}

			return checkForNecessaryOperations(necessaryOpTypes, presentOperations);
		} catch (IOException ex) {
			log.error(null, ex);
			return necessaryOpTypes;
		}
	}

	public static List<OPType> checkForNecessaryOperations(List<OPType> necessaryOpTypes, List<OperationMetadata> operations) {

		List<OPType> missingOpTypes = new ArrayList<OPType>(necessaryOpTypes);

		for (OperationMetadata operation : operations) {
			// Remove this operations type as a necessity, if it is one
			missingOpTypes.remove(operation.getType());
		}

		return missingOpTypes;
	}

	public static Map<ChromosomeKey, ChromosomeInfo> extractChromosomeKeysAndInfos(OperationKey operationKey) throws IOException {

		Map<ChromosomeKey, ChromosomeInfo> chromosomes;

		OperationDataSet opDS = OperationManager.generateOperationDataSet(operationKey);
		Map<Integer, ChromosomeKey> chromosomeKeys = opDS.getChromosomesKeysSource().getIndicesMap();

		DataSetSource matrixDS = MatrixFactory.generateMatrixDataSetSource(operationKey.getParentMatrixKey());
		ChromosomesInfosSource matrixChromosomesInfos = matrixDS.getChromosomesInfosSource();

		chromosomes = new LinkedHashMap<ChromosomeKey, ChromosomeInfo>(chromosomeKeys.size());
		for (Map.Entry<Integer, ChromosomeKey> chromosomeKey : chromosomeKeys.entrySet()) {
			chromosomes.put(chromosomeKey.getValue(), matrixChromosomesInfos.get(chromosomeKey.getKey()));
		}

		return chromosomes;
	}
}
