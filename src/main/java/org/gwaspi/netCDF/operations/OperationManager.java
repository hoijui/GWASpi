package org.gwaspi.netCDF.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
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
	public static List<String> checkForNecessaryOperations(List<String> necessaryOPs, int matrixId) {
		try {
			List<Operation> chkOpAL = OperationsList.getOperationsList(matrixId);

			for (int i = 0; i < chkOpAL.size(); i++) {
				if (necessaryOPs.contains(chkOpAL.get(i).getOperationType())) {
					necessaryOPs.remove(chkOpAL.get(i).getOperationType());
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return necessaryOPs;
	}

	public static List<String> checkForNecessaryOperations(List<String> necessaryOPs, int matrixId, int opId) {
		try {
			List<Operation> chkMatrixAL = OperationsList.getOperationsList(matrixId);

			for (int i = 0; i < chkMatrixAL.size(); i++) {
				// Check if current operation is from parent matrix or parent operation
				int parentOperationId = chkMatrixAL.get(i).getParentOperationId();
				if ((parentOperationId == -1) || (parentOperationId == opId)) {
					necessaryOPs.remove(chkMatrixAL.get(i).getOperationType());
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return necessaryOPs;
	}

	public static List<String> checkForBlackListedOperations(List<String> blackListOPs, int matrixId) {
		List<String> nonoOPs = new ArrayList<String>();
		try {
			List<Operation> chkOpAL = OperationsList.getOperationsList(matrixId);

			for (int i = 0; i < chkOpAL.size(); i++) {
				if (blackListOPs.contains(chkOpAL.get(i).getOperationType())) {
					nonoOPs.add(chkOpAL.get(i).getOperationType());
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return nonoOPs;
	}

	public static List<String> checkForBlackListedOperations(List<String> blackListOPs, int matrixId, int opId) {
		List<String> nonoOPs = new ArrayList<String>();
		try {
			List<Operation> chkOpAL = OperationsList.getOperationsList(matrixId, opId);

			for (int i = 0; i < chkOpAL.size(); i++) {
				if (blackListOPs.contains(chkOpAL.get(i).getOperationType())) {
					nonoOPs.add(chkOpAL.get(i).getOperationType());
				}
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return nonoOPs;
	}
	//</editor-fold>
}
