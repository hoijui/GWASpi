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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
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
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperation;
import org.gwaspi.operations.markercensus.MarkerCensusOperation;
import org.gwaspi.operations.qamarkers.QAMarkersOperation;
import org.gwaspi.operations.qasamples.QASamplesOperation;
import org.gwaspi.operations.trendtest.TrendTestOperation;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperation;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperation;
import org.gwaspi.operations.combi.CombiTestOperation;
import org.gwaspi.operations.hardyweinberg.ByHardyWeinbergThresholdFilterOperation;
import org.gwaspi.operations.hardyweinberg.ByHardyWeinbergThresholdFilterOperationParams;
import org.gwaspi.operations.filter.ByValidAffectionFilterOperation;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestOperation;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationManager {

	private static final Logger log = LoggerFactory.getLogger(OperationManager.class);

	private static final Map<Class<? extends MatrixOperation>, OperationFactory> operationTypeToFactory
			= new HashMap<Class<? extends MatrixOperation>, OperationFactory>();
	private static final Map<OPType, Class<? extends MatrixOperation>> oldToNewType
			= new EnumMap<OPType, Class<? extends MatrixOperation>>(OPType.class);
	private static final Map<OperationTypeInfo, Class<? extends MatrixOperation>> operationTypeInfoToOperationType
			= new HashMap<OperationTypeInfo, Class<? extends MatrixOperation>>();

	static {
		QAMarkersOperation.register();
		QASamplesOperation.register();
		HardyWeinbergOperation.register();
		MarkerCensusOperation.register();
		TrendTestOperation.register();
		AllelicAssociationTestOperation.register();
		GenotypicAssociationTestOperation.register();
		ByHardyWeinbergThresholdFilterOperation.register();
		ByValidAffectionFilterOperation.register();
		ByCombiWeightsFilterOperation.register();
		CombiTestOperation.register();
	}

	private OperationManager() {
	}

	public static void registerOperationFactory(final OperationFactory operationFactory) {

		final Class<? extends MatrixOperation> type = operationFactory.getType();
		if (operationTypeToFactory.containsKey(type)) {
			throw new IllegalStateException("Operation factory type registered more then once: " + type.getCanonicalName());
		}
		operationTypeToFactory.put(type, operationFactory);
		final OPType oldType = operationFactory.getTypeInfo().getType();
		if (oldType != null) { // HACK we need to do this becasue matrix creating operations don't have an old type!
			oldToNewType.put(oldType, type);
		}
		operationTypeInfoToOperationType.put(operationFactory.getTypeInfo(), type);
	}

	private static OperationFactory getOperationFactory(final Class<? extends MatrixOperation> type) {

		final OperationFactory operationFactory
				= operationTypeToFactory.get(type);
		if (operationFactory == null) {
			throw new IllegalArgumentException("No factory available for operation type: "
					+ type.toString());
		}

		return operationFactory;
	}

	public static OperationTypeInfo getOperationTypeInfo(final Class<? extends MatrixOperation> type) {
		return getOperationFactory(type).getTypeInfo();
	}

	public static Map<String, Object> getDefaultFactoryProperties() {

		Map<String, Object> defaultFactoryProperties = new HashMap<String, Object>();

		final String storageType;
		if (Config.getSingleton().getBoolean(Config.PROPERTY_STORAGE_IN_MEMORY, false)) {
			storageType = AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_MEMORY;
		} else {
			storageType = AbstractDefaultTypesOperationFactory.PROPERTY_VALUE_TYPE_NETCDF;
		}
		defaultFactoryProperties.put(OperationFactory.PROPERTY_NAME_TYPE, storageType);

		return defaultFactoryProperties;
	}

	/**
	 * Creates a new OperationDataSet for the specified type.
	 * @param type
	 * @param origin
	 * @param parent
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static OperationDataSet generateOperationDataSet(final OPType type, MatrixKey origin, DataSetKey parent, OperationParams params) throws IOException {
		return generateOperationDataSet(oldToNewType.get(type), origin, parent, params);
	}
	public static OperationDataSet generateOperationDataSet(
			final Class<? extends MatrixOperation> type,
			MatrixKey origin,
			DataSetKey parent,
			OperationParams params)
			throws IOException
	{
		return generateOperationDataSet(type, null, origin, parent, getDefaultFactoryProperties(), params);
	}

	public static OperationDataSet generateOperationDataSet(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = OperationsList.getOperationMetadata(operationKey);
		OPType oldType = operationMetadata.getOperationType();
		final Class<? extends MatrixOperation> type = oldToNewType.get(oldType);

		return generateOperationDataSet(type, operationKey, operationKey.getParentMatrixKey(), operationMetadata.getParent(), getDefaultFactoryProperties(), null);
	}

	private static OperationDataSet generateOperationDataSet(
			final Class<? extends MatrixOperation> type,
			OperationKey operationKey,
			MatrixKey origin,
			DataSetKey parent,
			Map<String, Object> properties,
			OperationParams params)
			throws IOException
	{
		final OperationFactory operationFactory = getOperationFactory(type);
		if (operationFactory == null) {
			throw new IllegalArgumentException("No operation factory registered for this type: " + type);
		}

		if (operationKey == null) {
			OperationDataSet operationDataSet = operationFactory.generateWriteOperationDataSet(parent, properties);
			operationDataSet.setParams(params);
			return operationDataSet;
		} else {
			return operationFactory.generateReadOperationDataSet(operationKey, parent, properties);
		}
	}

	private static OperationMetadata generateOperationMetadata(final Class<? extends MatrixOperation> type, OperationDataSet dataSet, OperationParams params) throws IOException {
		return getOperationFactory(type).getOperationMetadataFactory().generateMetadata(dataSet, params);
	}

	public static OperationMetadata generateOperationMetadata(final OperationTypeInfo type, OperationDataSet dataSet, OperationParams params) throws IOException {
		return generateOperationMetadata(operationTypeInfoToOperationType.get(type), dataSet, params);
	}

	public static OperationKey censusCleanMatrixMarkers(
			final MarkerCensusOperation operation)
			throws IOException
	{
		final MarkerCensusOperationParams params = operation.getParams();
		final String byWhat = (params.getPhenotypeFile() == null) ? "Affection" : "Phenotype (file: " + params.getPhenotypeFile().getName() + ")";
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count (by " + byWhat + ")");

		final OperationKey operationKey = performOperationCreatingOperation(operation);

		org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");

		return operationKey;
	}

	public static OperationKey performCleanTests(
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			double hwThreshold,
			OPType testType,
			final SuperProgressSource superProgressSource)
			throws IOException
	{
		org.gwaspi.global.Utils.sysoutStart(OutputTest.createTestName(testType) + " Test using QA and HW thresholds");

		// TODO first check if such an exclude operation (with the same parameters) does already exist (would be a child of the hwOp), and if so, use it!
		// exclude by Hardy & Weinberg threshold
		final MatrixOperation excludeOperation = new ByHardyWeinbergThresholdFilterOperation(
				new ByHardyWeinbergThresholdFilterOperationParams(hwOpKey, null, hwOpKey, hwThreshold));
		superProgressSource.replaceSubProgressSource(ByHardyWeinbergThresholdFilterOperation.PLACEHOLDER_PS_HW_TF, excludeOperation.getProgressSource(), null);
		final OperationKey excludeOperationKey = performOperationCreatingOperation(excludeOperation);
		final DataSetKey excludeOperationDataSetKey = new DataSetKey(excludeOperationKey);

		// run the test
		final MatrixOperation operation;
		if (testType == OPType.TRENDTEST) {
			operation = new TrendTestOperation(new TrendTestOperationParams(excludeOperationDataSetKey, censusOpKey));
		} else {
			final AssociationTestOperationParams params
					= new AssociationTestOperationParams(testType, excludeOperationDataSetKey, censusOpKey);
			if (testType == OPType.ALLELICTEST) {
				operation = new AllelicAssociationTestOperation(params);
			} else {
				operation = new GenotypicAssociationTestOperation(params);
			}
		}
		superProgressSource.replaceSubProgressSource(AllelicAssociationTestOperation.PLACEHOLDER_PS_TEST, operation.getProgressSource(), null);

		final OperationKey operationKey = performOperationCreatingOperation(operation);

		return operationKey;
	}

	public static <R> R performOperation(final MatrixOperation<?, R> operation)
			throws IOException
	{
		final R result;
		if (operation.isValid()) {
			result = operation.call();
		} else {
			result = null;
			log.error(
					"Can not execute {} operation, because the given parameters are invalid: {}",
					operation.getClass().getSimpleName(), // HACK should be getType(), but only Operation-Operations have this method
					operation.getProblemDescription());
		}

		return result;
	}

	public static OperationKey performOperationCreatingOperation(final MatrixOperation<?, OperationKey> operation)
			throws IOException
	{
		final DataSetKey parent = operation.getParams().getParent();

		OperationKey resultOperationKey = performOperation(operation);
		if (resultOperationKey == null) {
			resultOperationKey = new OperationKey(parent.getOrigin(), Integer.MIN_VALUE);
		} else {
			final boolean opHasResultView
					= (OperationsList.getOperationMetadata(resultOperationKey) != null); // HACK maybe better add a getter to OperationType or so
			if (opHasResultView) {
				if (parent.isMatrix()) {
					GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultOperationKey);
				} else {
					GWASpiExplorerNodes.insertSubOperationUnderOperationNode(parent.getOperationParent(), resultOperationKey);
				}
			}
		}

		return resultOperationKey;
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
