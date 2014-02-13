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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.combi.CombiTestMatrixOperation;
import org.gwaspi.operations.combi.CombiTestParams;
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
			MatrixKey rdMatrixKey,
			OperationKey samplesQAOpKey,
			OperationKey markersQAOpKey,
			double markerMissingRatio,
			boolean discardMismatches,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			String censusName)
			throws IOException
	{
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count by Affection");

		MatrixOperation operation = new OP_MarkerCensus(
				rdMatrixKey,
				censusName,
				samplesQAOpKey,
				sampleMissingRatio,
				sampleHetzygRatio,
				markersQAOpKey,
				discardMismatches,
				markerMissingRatio,
				null);

		int resultOpId = operation.processMatrix();

		OperationKey operationKey = new OperationKey(rdMatrixKey, resultOpId);

		return operationKey;
	}

	public static OperationKey censusCleanMatrixMarkersByPhenotypeFile(
			MatrixKey rdMatrixKey,
			OperationKey samplesQAOpKey,
			OperationKey markersQAOpKey,
			double markerMissingRatio,
			boolean discardMismatches,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			String censusName,
			File phenoFile)
			throws IOException
	{
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count using " + phenoFile.getName());

		MatrixOperation operation = new OP_MarkerCensus(
				rdMatrixKey,
				censusName,
				samplesQAOpKey,
				sampleMissingRatio,
				sampleHetzygRatio,
				markersQAOpKey,
				discardMismatches,
				markerMissingRatio,
				phenoFile);

		int resultOpId = operation.processMatrix();

		OperationKey operationKey = new OperationKey(rdMatrixKey, resultOpId);

		return operationKey;
	}

	public static OperationKey performHardyWeinberg(OperationKey censusOpKey, String hwName) throws IOException {

		org.gwaspi.global.Utils.sysoutStart("Hardy-Weinberg");

		MatrixOperation operation = new OP_HardyWeinberg(censusOpKey, hwName);

		int resultOpId = operation.processMatrix();

		OperationKey operationKey = new OperationKey(censusOpKey.getParentMatrixKey(), resultOpId);

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

		final MatrixOperation operation;
		if (testType == OPType.TRENDTEST) {
			operation = new OP_TrendTests(
					censusOpKey,
					hwOpKey,
					hwThreshold);
		} else {
			operation = new OP_AssociationTests(
					censusOpKey,
					hwOpKey,
					hwThreshold,
					testType);
		}

		int resultOpId = operation.processMatrix();

		OperationKey operationKey = new OperationKey(censusOpKey.getParentMatrixKey(), resultOpId);

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

	public static OperationKey performRawCombiTest(CombiTestParams params)
			throws IOException
	{
		org.gwaspi.global.Utils.sysoutStart(" Combi Association Test");

		MatrixOperation operation = new CombiTestMatrixOperation(params);

		final int resultOpId;
		if (operation.isValid()) {
			resultOpId = operation.processMatrix();
		} else {
			resultOpId = Integer.MIN_VALUE;
		}

		final OperationKey operationKey = new OperationKey(params.getParentKey().getOrigin(), resultOpId);

		return operationKey;
	}
	//</editor-fold>

	public static OperationKey performQASamplesOperationAndCreateReports(
			OP_QASamples operation)
			throws IOException
	{
		int samplesQAOpId = operation.processMatrix();

		OperationKey samplesQAOpKey = new OperationKey(operation.getParentMatrixKey(), samplesQAOpId);

		GWASpiExplorerNodes.insertOperationUnderMatrixNode(samplesQAOpKey);

		OutputQASamples.writeReportsForQASamplesData(samplesQAOpKey, true);
		GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpKey);

		return samplesQAOpKey;
	}

	public static OperationKey performQAMarkersOperationAndCreateReports(
			OP_QAMarkers operation)
			throws IOException
	{
		int markersQAOpId = operation.processMatrix();

		OperationKey markersQAOpKey = new OperationKey(operation.getParentMatrixKey(), markersQAOpId);

		GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);

		OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpKey);
		GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpKey);

		return markersQAOpKey;
	}

	//<editor-fold defaultstate="expanded" desc="OPERATIONS METADATA">
	public static List<OPType> checkForNecessaryOperations(final List<OPType> necessaryOPs, MatrixKey matrixKey) {

		List<OPType> missingOPs = new ArrayList<OPType>(necessaryOPs);

		try {
			List<OperationMetadata> chkOperations = OperationsList.getOperationsList(matrixKey);

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
			List<OperationMetadata> chkOperations = OperationsList.getOperationsList(matrixKey);

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
			List<OperationMetadata> chkOperations = OperationsList.getOperationsList(matrixKey);

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
			List<OperationMetadata> chkOperations = OperationsList.getOperations(operationKey);

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
