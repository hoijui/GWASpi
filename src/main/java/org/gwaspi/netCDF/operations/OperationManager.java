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
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.combi.CombiTestMatrixOperation;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.filter.ByHardyWeinbergThresholdFilterOperation;
import org.gwaspi.operations.filter.ByHardyWeinbergThresholdFilterOperationParams;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.reports.OutputHardyWeinberg;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.gwaspi.reports.OutputTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationManager {

	private static final Logger log = LoggerFactory.getLogger(OperationManager.class);

	private OperationManager() {
	}

	//<editor-fold defaultstate="expanded" desc="MATRIX CENSUS">
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

		OutputHardyWeinberg.writeReportsForMarkersHWData(operationKey);

		return operationKey;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
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
			operation = new OP_AssociationTests(new AssociationTestOperationParams(testType, excludeOperationKey, censusOpKey));
		}

		final OperationKey operationKey = performOperation(operation);

		return operationKey;
	}

//	public static OperationKey performCombiTestPackage(CombiTestParams params)
//			throws IOException
//	{
//		OperationKey parent = params.getParentKey().getOperationParent();
//
//		List<OPType> ancestorOperationTypes = OperationsList.getAncestorOperationTypes(parent);
//		OPType parentType = ancestorOperationTypes.get(ancestorOperationTypes.size() - 1);
//
//		// TODO
//		// - run "hardy weinberg" & "exclude by hardy weinberg", if none is in the ancestry
//		// - run "filter by valid" affection, if none is in the ancestry
//		// - run QA markers operation, and use it as the direct parent,
//		//   if the given parent is not already a QA markers operation
//		if (!ancestorOperationTypes.contains(OPType.HARDY_WEINBERG)) {
//			if (parentType != OPType.MARKER_CENSUS_BY_AFFECTION
//					&& parentType != OPType.MARKER_CENSUS_BY_PHENOTYPE)
//			{
//				censusCleanMatrixMarkers(null, parent, parent, markerMissingRatio, true, sampleMissingRatio, sampleHetzygRatio, null)
//			}
//			perfo
//			params.getParentKey().
//		}
//
//		MatrixOperation operation = new CombiTestMatrixOperation(params);
//
//		final int resultOpId;
//		if (operation.isValid()) {
//			resultOpId = operation.processMatrix();
//		} else {
//			resultOpId = Integer.MIN_VALUE;
//		}
//
//		final OperationKey operationKey = new OperationKey(params.getMatrixKey(), resultOpId);
//
//		return operationKey;
//	}

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
	//</editor-fold>

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

	//<editor-fold defaultstate="expanded" desc="OPERATIONS METADATA">
	public static List<OPType> checkForNecessaryOperations(final List<OPType> necessaryOPs, MatrixKey matrixKey) {

		List<OPType> missingOPs = new ArrayList<OPType>(necessaryOPs);

		try {
			List<OperationMetadata> chkOperations = OperationsList.getOffspringOperationsMetadata(matrixKey);

			for (OperationMetadata operation : chkOperations) {
				OPType type = operation.getOperationType();
				if (necessaryOPs.contains(type)) {
					missingOPs.remove(type);
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return missingOPs;
	}

	public static List<OPType> checkForNecessaryOperations(List<OPType> necessaryOPs, MatrixKey matrixKey, int opId) {

		List<OPType> missingOPs = new ArrayList<OPType>(necessaryOPs);

		try {
			List<OperationMetadata> chkOperations = OperationsList.getOffspringOperationsMetadata(matrixKey);

			for (OperationMetadata operation : chkOperations) {
				// Check if current operation is from parent matrix or parent operation
				int parentOperationId = operation.getParentOperationId();
				if ((parentOperationId == -1) || (parentOperationId == opId)) {
					missingOPs.remove(operation.getOperationType());
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return missingOPs;
	}

	public static List<OPType> checkForBlackListedOperations(List<OPType> blackListOPs, MatrixKey matrixKey) {

		List<OPType> nonoOPs = new ArrayList<OPType>();

		try {
			List<OperationMetadata> chkOperations = OperationsList.getOffspringOperationsMetadata(matrixKey);

			for (OperationMetadata operation : chkOperations) {
				OPType type = operation.getOperationType();
				if (blackListOPs.contains(type)) {
					nonoOPs.add(type);
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return nonoOPs;
	}

	/**
	 * @deprected unused
	 */
	public static List<OPType> checkForBlackListedOperations(List<OPType> blackListOPs, OperationKey operationKey) {

		List<OPType> nonoOPs = new ArrayList<OPType>();

		try {
			List<OperationMetadata> chkOperations = OperationsList.getChildrenOperationsMetadata(operationKey);

			for (OperationMetadata operation : chkOperations) {
				OPType type = operation.getOperationType();
				if (blackListOPs.contains(type)) {
					nonoOPs.add(type);
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return nonoOPs;
	}
	//</editor-fold>
}
