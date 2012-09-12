package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputQAMarkers {

	private OutputQAMarkers() {
	}

	public static boolean writeReportsForQAMarkersData(int opId) throws IOException {
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String markMissOutName = prefix + "markmissing.txt";


		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());

		if (createSortedMarkerMissingnessReport(opId, markMissOutName)) {
			ReportManager.insertRPMetadata(dBManager,
					"Marker Missingness Table",
					markMissOutName,
					cNetCDF.Defaults.OPType.MARKER_QA.toString(),
					op.getParentMatrixId(),
					opId,
					"Marker Missingness Table",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Marker Missingness QA Report");
		}


		String markMismatchOutName = prefix + "markmismatch.txt";
		if (createMarkerMismatchReport(opId, markMismatchOutName)) {
			ReportManager.insertRPMetadata(dBManager,
					"Marker Mismatch State Table",
					markMismatchOutName,
					cNetCDF.Defaults.OPType.MARKER_QA.toString(),
					op.getParentMatrixId(),
					opId,
					"Marker Mismatch State Table",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Marker Mismatch QA Report");
		}

		return true;
	}

	public static boolean createSortedMarkerMissingnessReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdMissingRatLHM = GatherQAMarkersData.loadMarkerQAMissingRatio(opId);
			Map<String, Object> sortedMarkerIdMissingRatLHM = ReportManager.getSortedDescendingMarkerSetByDoubleValue(unsortedMarkerIdMissingRatLHM);
			if (unsortedMarkerIdMissingRatLHM != null) {
				unsortedMarkerIdMissingRatLHM.clear();
			}

			//PREPARE SORTING LHM & STORE QA VALUES FOR LATER
			Map<String, Object> sortingMarkerSetLHM = new LinkedHashMap<String, Object>();
			Map<String, Object> storedMissingRatLHM = new LinkedHashMap<String, Object>();
			if (sortedMarkerIdMissingRatLHM != null) {
				for (Map.Entry<String, Object> entry : sortedMarkerIdMissingRatLHM.entrySet()) {
					String key = entry.getKey();
					double missingValue = (Double) entry.getValue();
					if (missingValue > 0) {
						storedMissingRatLHM.put(key, missingValue);
						sortingMarkerSetLHM.put(key, missingValue);
					}
				}

				sortedMarkerIdMissingRatLHM.clear();
			}


			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);

			//WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			List<Object[]> operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = (Object[]) operationsAL.get(i);
				if (element[1].toString().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				//MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = opMarkerSetLHM.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				//MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetLHM.get(entry.getKey()));
				}
			}
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			if (rdInfoMarkerSet.getMarkerIdSetLHM() != null) {
				rdInfoMarkerSet.getMarkerIdSetLHM().clear();
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);


			//WRITE QA MISSINGNESS RATIO
			ReportWriter.appendColumnToReport(reportPath, reportName, storedMissingRatLHM, false, false);
			if (storedMissingRatLHM != null) {
				storedMissingRatLHM.clear();
			}

			result = true;
		} catch (IOException iOException) {
			result = false;
		}

		return result;
	}

	public static boolean createMarkerMismatchReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdMismatchStateLHM = GatherQAMarkersData.loadMarkerQAMismatchState(opId);
			Map<String, Object> sortingMarkerSetLHM = new LinkedHashMap<String, Object>();
			if (unsortedMarkerIdMismatchStateLHM != null) {
				for (Map.Entry<String, Object> entry : unsortedMarkerIdMismatchStateLHM.entrySet()) {
					String key = entry.getKey();
					int mismatchState = (Integer) entry.getValue();
					if (mismatchState > 0) {
						sortingMarkerSetLHM.put(key, mismatchState);
					}
				}

				unsortedMarkerIdMismatchStateLHM.clear();
			}

			//STORE MISMATCH STATE FOR LATER
			Map<String, Object> storedMismatchStateLHM = new LinkedHashMap<String, Object>();
			storedMismatchStateLHM.putAll(sortingMarkerSetLHM);


			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


			//WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, true);


			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			List<Object[]> operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				//MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = opMarkerSetLHM.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				//MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetLHM.get(entry.getKey()));
				}
			}
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE QA MISMATCH STATE
			ReportWriter.appendColumnToReport(reportPath, reportName, storedMismatchStateLHM, false, false);
			if (storedMismatchStateLHM != null) {
				storedMismatchStateLHM.clear();
			}

			result = true;
		} catch (IOException iOException) {
			result = false;
		}

		return result;
	}
}
