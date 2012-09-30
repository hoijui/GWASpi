package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBOperations;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	private final static Logger log = LoggerFactory.getLogger(OperationManager.class);

	private OperationManager() {
	}

	//<editor-fold defaultstate="collapsed" desc="MATRIX CENSUS">
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
		Operation sampleQAOP = new Operation(samplesQAOpId);
		Operation markerQAOP = new Operation(markersQAOpId);

		resultOpId = new OP_MarkerCensus_opt(
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
		Operation sampleQAOP = new Operation(samplesQAOpId);
		Operation markerQAOP = new Operation(markersQAOpId);

		resultOpId = new OP_MarkerCensus_opt(
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
		Operation censusOP = new Operation(censusOpId);

		org.gwaspi.global.Utils.sysoutStart("Hardy-Weinberg");

		resultOpId = new OP_HardyWeinberg(
				censusOP,
				hwName).processMatrix();

		org.gwaspi.reports.OutputHardyWeinberg.writeReportsForMarkersHWData(resultOpId);

		return resultOpId;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ANALYSIS">
	public static int performCleanAllelicTests(
			int _rdMatrixId,
			int censusOpId,
			int hwOpId,
			double hwThreshold)
			throws IOException, InvalidRangeException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart(" Allelic Association Test using QA and HW thresholds");

		Operation markerCensusOP = new Operation(censusOpId);
		Operation hwOP = new Operation(hwOpId);

		resultOpId = new OP_AllelicAssociationTests_opt(
				_rdMatrixId,
				markerCensusOP,
				hwOP,
				hwThreshold).processMatrix();

		return resultOpId;
	}

	public static int performCleanGenotypicTests(
			int _rdMatrixId,
			int censusOpId,
			int hwOpId,
			double hwThreshold)
			throws IOException, InvalidRangeException
	{
		int resultOpId; // Integer.MIN_VALUE

		org.gwaspi.global.Utils.sysoutStart(" Genotypic Association Test using QA and HW thresholds");

		Operation markerCensusOP = new Operation(censusOpId);
		Operation hwOP = new Operation(hwOpId);

		resultOpId = new OP_GenotypicAssociationTests_opt(
				_rdMatrixId,
				markerCensusOP,
				hwOP,
				hwThreshold).processMatrix();

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

		Operation markerCensusOP = new Operation(censusOpId);
		Operation hwOP = new Operation(hwOpId);

		resultOpId = new OP_TrendTests_opt(
				_rdMatrixId,
				markerCensusOP,
				hwOP,
				hwThreshold).processMatrix();

		return resultOpId;
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="OPERATIONS METADATA">
	public static String createOperationsMetadataTable(DbManager db) {
		boolean result = false;
		try {
			// CREATE SAMPLESET_METADATA table in given SCHEMA
			db.createTable(cDBGWASpi.SCH_MATRICES,
					cDBOperations.T_OPERATIONS,
					cDBOperations.T_CREATE_OPERATIONS);

		} catch (Exception ex) {
			log.error("Failed creating management database", ex);
		}

		return (result) ? "1" : "0";
	}

	static void insertOPMetadata(
			DbManager dBManager,
			int parentMatrixId,
			int parentOperationId,
			String friendlyName,
			String resultOPName,
			String OPType,
			String command,
			String description,
			Integer studyId)
	{
		Object[] opMetaData = new Object[]{parentMatrixId,
			parentOperationId,
			friendlyName,
			resultOPName,
			OPType,
			command,
			description,
			studyId};

		dBManager.insertValuesInTable(cDBGWASpi.SCH_MATRICES,
				cDBOperations.T_OPERATIONS,
				cDBOperations.F_INSERT_OPERATION,
				opMetaData);
	}

	public static List<Object[]> getMatrixOperations(int matrixId) throws IOException {
		List<Object[]> result = new ArrayList<Object[]>();

		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		List<Map<String, Object>> rs = dBManager.executeSelectStatement("SELECT * FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_PARENT_MATRIXID + "=" + matrixId + "  WITH RR");

		for (int rowcount = 0; rowcount < rs.size(); rowcount++) {
			// PREVENT PHANTOM-DB READS EXCEPTIONS
			if (!rs.isEmpty() && rs.get(rowcount).size() == cDBOperations.T_CREATE_OPERATIONS.length) {
				Object[] element = new Object[2];
				element[0] = (Integer) rs.get(rowcount).get(cDBOperations.f_ID);
				element[1] = rs.get(rowcount).get(cDBOperations.f_OP_TYPE).toString();
				result.add(element);
			}
		}

		return result;
	}

	public static void deleteOperationBranch(int studyId, int opId, boolean deleteReports) throws IOException {

		try {
			Operation op = new Operation(opId);
			String genotypesFolder = Config.getConfigValue(Config.PROPERTY_GENOTYPES_DIR, "");

			List<Operation> operations = OperationsList.getOperationsList(op.getParentMatrixId(), opId);
			if (!operations.isEmpty()) {
				operations.add(op);
				for (int i = 0; i < operations.size(); i++) {
					File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + operations.get(i).getOperationNetCDFName() + ".nc");
					if (matrixOPFile.exists()) {
						if (!matrixOPFile.canWrite()) {
							throw new IllegalArgumentException("Delete: write protected: " + matrixOPFile.getPath());
						}
						boolean success = matrixOPFile.delete();
					}
					if (deleteReports) {
						org.gwaspi.reports.ReportManager.deleteReportByOperationId(operations.get(i).getOperationId());
					}

					DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
					String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + operations.get(i).getOperationId();
					dBManager.executeStatement(statement);
				}
			} else {
				File matrixOPFile = new File(genotypesFolder + "/STUDY_" + studyId + "/" + op.getOperationNetCDFName() + ".nc");
				if (matrixOPFile.exists()) {
					if (!matrixOPFile.canWrite()) {
						throw new IllegalArgumentException("Delete: write protected: " + matrixOPFile.getPath());
					}
					boolean success = matrixOPFile.delete();
				}
				if (deleteReports) {
					org.gwaspi.reports.ReportManager.deleteReportByOperationId(opId);
				}

				DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
				String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
				dBManager.executeStatement(statement);
			}
		} catch (IOException ex) {
			// PURGE INEXISTING OPERATIONS FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
			dBManager.executeStatement(statement);
		} catch (IllegalArgumentException ex) {
			// PURGE INEXISTING OPERATIONS FROM DB
			DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			String statement = "DELETE FROM " + cDBGWASpi.SCH_MATRICES + "." + cDBOperations.T_OPERATIONS + " WHERE " + cDBOperations.f_ID + "=" + opId;
			dBManager.executeStatement(statement);
		}
	}

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
				if (chkMatrixAL.get(i).getParentOperationId() == -1 || chkMatrixAL.get(i).getParentOperationId() == opId) {
					if (necessaryOPs.contains(chkMatrixAL.get(i).getOperationType())) {
						necessaryOPs.remove(chkMatrixAL.get(i).getOperationType());
					}
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
