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
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class OperationManager {

	private static final Logger log = LoggerFactory.getLogger(OperationManager.class);

	private OperationManager() {
	}

	//<editor-fold defaultstate="expanded" desc="MATRIX CENSUS">
	public static int censusCleanMatrixMarkers(
			int _rdMatrixId,
			int samplesQAOpId,
			int markersQAOpId,
			double markerMissingRatio,
			boolean discardMismatches,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			String censusName)
			throws IOException, InvalidRangeException
	{
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count by Affection");

		int resultOpId; // Integer.MIN_VALUE
		Operation sampleQAOP = OperationsList.getById(samplesQAOpId);
		Operation markerQAOP = OperationsList.getById(markersQAOpId);

		resultOpId = new OP_MarkerCensus(
				_rdMatrixId,
				censusName,
				sampleQAOP,
				sampleMissingRatio,
				sampleHetzygRatio,
				markerQAOP,
				discardMismatches,
				markerMissingRatio,
				null).processMatrix();

		return resultOpId;
	}

	public static int censusCleanMatrixMarkersByPhenotypeFile(
			int _rdMatrixId,
			int samplesQAOpId,
			int markersQAOpId,
			double markerMissingRatio,
			boolean discardMismatches,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			String censusName,
			File phenoFile)
			throws IOException, InvalidRangeException
	{
		org.gwaspi.global.Utils.sysoutStart("Genotypes Frequency Count using " + phenoFile.getName());

		int resultOpId; // Integer.MIN_VALUE
		Operation sampleQAOP = OperationsList.getById(samplesQAOpId);
		Operation markerQAOP = OperationsList.getById(markersQAOpId);

		resultOpId = new OP_MarkerCensus(
				_rdMatrixId,
				censusName,
				sampleQAOP,
				sampleMissingRatio,
				sampleHetzygRatio,
				markerQAOP,
				discardMismatches,
				markerMissingRatio,
				phenoFile).processMatrix();

		return resultOpId;
	}

	public static int performHardyWeinberg(int censusOpId, String hwName) throws IOException, InvalidRangeException {
		int resultOpId; // Integer.MIN_VALUE
		Operation censusOP = OperationsList.getById(censusOpId);

		org.gwaspi.global.Utils.sysoutStart("Hardy-Weinberg");

		resultOpId = new OP_HardyWeinberg(
				censusOP,
				hwName).processMatrix();

		org.gwaspi.reports.OutputHardyWeinberg.writeReportsForMarkersHWData(resultOpId);

		return resultOpId;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
	public static int performCleanAssociationTests(
			int rdMatrixId,
			int censusOpId,
			int hwOpId,
			double hwThreshold,
			boolean allelic)
			throws IOException, InvalidRangeException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart(" " + (allelic ? "Allelic" : "Genotypic") + " Association Test using QA and HW thresholds");

		Operation markerCensusOP = OperationsList.getById(censusOpId);
		Operation hwOP = OperationsList.getById(hwOpId);

		resultOpId = new OP_AssociationTests(
				rdMatrixId,
				markerCensusOP,
				hwOP,
				hwThreshold,
				allelic).processMatrix();

		return resultOpId;
	}

	public static int performCleanTrendTests(
			int _rdMatrixId,
			int censusOpId,
			int hwOpId,
			double hwThreshold)
			throws IOException, InvalidRangeException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart("Cochran-Armitage Trend Test using QA and HW thresholds");

		Operation markerCensusOP = OperationsList.getById(censusOpId);
		Operation hwOP = OperationsList.getById(hwOpId);

		resultOpId = new OP_TrendTests(
				_rdMatrixId,
				markerCensusOP,
				hwOP,
				hwThreshold).processMatrix();

		return resultOpId;
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="OPERATIONS METADATA">
	public static List<OPType> checkForNecessaryOperations(final List<OPType> necessaryOPs, int matrixId) {

		List<OPType> missingOPs = new ArrayList<OPType>(necessaryOPs);

		try {
			List<Operation> chkOperations = OperationsList.getOperationsList(matrixId);

			for (Operation operation : chkOperations) {
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

	public static List<OPType> checkForNecessaryOperations(List<OPType> necessaryOPs, int matrixId, int opId) {

		List<OPType> missingOPs = new ArrayList<OPType>(necessaryOPs);

		try {
			List<Operation> chkOperations = OperationsList.getOperationsList(matrixId);

			for (Operation operation : chkOperations) {
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

	public static List<OPType> checkForBlackListedOperations(List<OPType> blackListOPs, int matrixId) {

		List<OPType> nonoOPs = new ArrayList<OPType>();

		try {
			List<Operation> chkOperations = OperationsList.getOperationsList(matrixId);

			for (Operation operation : chkOperations) {
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
			List<Operation> chkOperations = OperationsList.getOperationsList(matrixId, opId);

			for (Operation operation : chkOperations) {
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
