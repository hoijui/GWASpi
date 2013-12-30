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
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.combi.CombiTestMatrixOperation;
import org.gwaspi.operations.combi.CombiTestParams;
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

		int resultOpId; // Integer.MIN_VALUE
		OperationMetadata sampleQAOP = OperationsList.getOperation(samplesQAOpKey);
		OperationMetadata markerQAOP = OperationsList.getOperation(markersQAOpKey);

		resultOpId = new OP_MarkerCensus(
				rdMatrixKey,
				censusName,
				sampleQAOP,
				sampleMissingRatio,
				sampleHetzygRatio,
				markerQAOP,
				discardMismatches,
				markerMissingRatio,
				null).processMatrix();

		return new OperationKey(rdMatrixKey, resultOpId);
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

		int resultOpId; // Integer.MIN_VALUE
		OperationMetadata sampleQAOP = OperationsList.getOperation(samplesQAOpKey);
		OperationMetadata markerQAOP = OperationsList.getOperation(markersQAOpKey);

		resultOpId = new OP_MarkerCensus(
				rdMatrixKey,
				censusName,
				sampleQAOP,
				sampleMissingRatio,
				sampleHetzygRatio,
				markerQAOP,
				discardMismatches,
				markerMissingRatio,
				phenoFile).processMatrix();

		return new OperationKey(rdMatrixKey, resultOpId);
	}

	public static OperationKey performHardyWeinberg(OperationKey censusOpKey, String hwName) throws IOException {
		int resultOpId; // Integer.MIN_VALUE
		OperationMetadata censusOP = OperationsList.getOperation(censusOpKey);

		org.gwaspi.global.Utils.sysoutStart("Hardy-Weinberg");

		resultOpId = new OP_HardyWeinberg(censusOpKey, hwName).processMatrix();
		OperationKey operationKey = new OperationKey(censusOpKey.getParentMatrixKey(), resultOpId);

		org.gwaspi.reports.OutputHardyWeinberg.writeReportsForMarkersHWData(operationKey);

		return operationKey;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
	public static OperationKey performCleanAssociationTests(
			MatrixKey rdMatrixKey,
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			double hwThreshold,
			boolean allelic)
			throws IOException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart(" " + (allelic ? "Allelic" : "Genotypic") + " Association Test using QA and HW thresholds");

		AbstractTestMatrixOperation testOperation = new OP_AssociationTests(
				rdMatrixKey,
				censusOpKey,
				hwOpKey,
				hwThreshold,
				allelic);
		resultOpId = testOperation.processMatrix();

		return new OperationKey(censusOpKey.getParentMatrixKey(), resultOpId);
	}

	public static OperationKey performCleanCombiTest(CombiTestParams params)
			throws IOException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart(" Combi Association Test");

		CombiTestMatrixOperation testOperation
				= new CombiTestMatrixOperation(params);
		resultOpId = testOperation.processMatrix();

		return new OperationKey(params.getMatrixKey(), resultOpId);
	}

	public static OperationKey performCleanTrendTests(
			MatrixKey rdMatrixKey,
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			double hwThreshold)
			throws IOException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart("Cochran-Armitage Trend Test using QA and HW thresholds");

		resultOpId = new OP_TrendTests(
				rdMatrixKey,
				censusOpKey,
				hwOpKey,
				hwThreshold).processMatrix();

		return new OperationKey(censusOpKey.getParentMatrixKey(), resultOpId);
	}
	//</editor-fold>

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

	public static List<OPType> checkForBlackListedOperations(List<OPType> blackListOPs, int matrixId, int opId) {

		List<OPType> nonoOPs = new ArrayList<OPType>();

		try {
			List<OperationMetadata> chkOperations = OperationsList.getOperationsList(matrixId, opId);

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
