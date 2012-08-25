package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.util.Iterator;
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
public class OutputQAMarkers_opt {

	private OutputQAMarkers_opt() {
	}

	public static boolean writeReportsForQAMarkersData(int opId) throws IOException {
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String markMissOutName = prefix + "markmissing.txt";


		org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue("ReportsDir", ""), "STUDY_" + op.getStudyId());

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
			for (Iterator<String> it = sortedMarkerIdMissingRatLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				double missingValue = (Double) sortedMarkerIdMissingRatLHM.get(key);
				if (missingValue > 0) {
					storedMissingRatLHM.put(key, missingValue);
					sortingMarkerSetLHM.put(key, missingValue);
				}
			}
			if (sortedMarkerIdMissingRatLHM != null) {
				sortedMarkerIdMissingRatLHM.clear();
			}



			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);

			//WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
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
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = opMarkerSetLHM.get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele);
				}

				//MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele + sep + opMarkerSetLHM.get(key));
				}


			}
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
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
			for (Iterator<String> it = unsortedMarkerIdMismatchStateLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				int mismatchState = (Integer) unsortedMarkerIdMismatchStateLHM.get(key);
				if (mismatchState > 0) {
					sortingMarkerSetLHM.put(key, mismatchState);
				}
			}
			if (unsortedMarkerIdMismatchStateLHM != null) {
				unsortedMarkerIdMismatchStateLHM.clear();
			}

			//STORE MISMATCH STATE FOR LATER
			Map<String, Object> storedMismatchStateLHM = new LinkedHashMap<String, Object>();
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = sortingMarkerSetLHM.get(key);
				storedMismatchStateLHM.put(key, value);
			}


			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


			//WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, true);


			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
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
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = opMarkerSetLHM.get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele);
				}

				//MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele + sep + opMarkerSetLHM.get(key));
				}

			}
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
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
