package org.gwaspi.reports;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputQAMarkers {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQAMarkers.class);

	private OutputQAMarkers() {
	}

	public static boolean writeReportsForQAMarkersData(int opId) throws IOException {
		Operation op = OperationsList.getById(opId);

		String prefix = ReportsList.getReportNamePrefix(op);
		String markMissOutName = prefix + "markmissing.txt";


		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());

		if (createSortedMarkerMissingnessReport(opId, markMissOutName)) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Marker Missingness Table",
					markMissOutName,
					OPType.MARKER_QA.toString(),
					op.getParentMatrixId(),
					opId,
					"Marker Missingness Table",
					op.getStudyId()));

			org.gwaspi.global.Utils.sysoutCompleted("Marker Missingness QA Report");
		}


		String markMismatchOutName = prefix + "markmismatch.txt";
		if (createMarkerMismatchReport(opId, markMismatchOutName)) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Marker Mismatch State Table",
					markMismatchOutName,
					OPType.MARKER_QA.toString(),
					op.getParentMatrixId(),
					opId,
					"Marker Mismatch State Table",
					op.getStudyId()));

			org.gwaspi.global.Utils.sysoutCompleted("Marker Mismatch QA Report");
		}

		return true;
	}

	public static boolean createSortedMarkerMissingnessReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, Object> unsortedMarkerIdMissingRatMap = GatherQAMarkersData.loadMarkerQAMissingRatio(opId);
			Map<MarkerKey, Object> sortedMarkerIdMissingRatMap = ReportsList.getSortedDescendingMarkerSetByDoubleValue(unsortedMarkerIdMissingRatMap);
			if (unsortedMarkerIdMissingRatMap != null) {
				unsortedMarkerIdMissingRatMap.clear();
			}

			// PREPARE SORTING Map & STORE QA VALUES FOR LATER
			Map<MarkerKey, Object> sortingMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
			Map<MarkerKey, Object> storedMissingRatMap = new LinkedHashMap<MarkerKey, Object>();
			if (sortedMarkerIdMissingRatMap != null) {
				for (Map.Entry<MarkerKey, Object> entry : sortedMarkerIdMissingRatMap.entrySet()) {
					MarkerKey key = entry.getKey();
					double missingValue = (Double) entry.getValue();
					if (missingValue > 0) {
						storedMissingRatMap.put(key, missingValue);
						sortingMarkerSetMap.put(key, missingValue);
					}
				}

				sortedMarkerIdMissingRatMap.clear();
			}


			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetMap, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, false);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<Object[]> operationsAL = OperationsList.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<MarkerKey, Object> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MINOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = opMarkerSetMap.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				// MAJOR ALLELE
				rdOperationSet.fillMapWithDefaultValue(opMarkerSetMap, "");
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetMap.get(entry.getKey()));
				}
			}
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			if (rdInfoMarkerSet.getMarkerIdSetMap() != null) {
				rdInfoMarkerSet.getMarkerIdSetMap().clear();
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, false);

			// WRITE QA MISSINGNESS RATIO
			ReportWriter.appendColumnToReport(reportPath, reportName, storedMissingRatMap, false, false);
			if (storedMissingRatMap != null) {
				storedMissingRatMap.clear();
			}

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
		}

		return result;
	}

	public static boolean createMarkerMismatchReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, Object> unsortedMarkerIdMismatchStateMap = GatherQAMarkersData.loadMarkerQAMismatchState(opId);
			Map<MarkerKey, Object> sortingMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
			if (unsortedMarkerIdMismatchStateMap != null) {
				for (Map.Entry<MarkerKey, Object> entry : unsortedMarkerIdMismatchStateMap.entrySet()) {
					MarkerKey key = entry.getKey();
					int mismatchState = (Integer) entry.getValue();
					if (mismatchState > 0) {
						sortingMarkerSetMap.put(key, mismatchState);
					}
				}

				unsortedMarkerIdMismatchStateMap.clear();
			}

			// STORE MISMATCH STATE FOR LATER
			Map<MarkerKey, Object> storedMismatchStateMap = new LinkedHashMap<MarkerKey, Object>();
			storedMismatchStateMap.putAll(sortingMarkerSetMap);

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetMap, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, true);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<Object[]> operationsAL = OperationsList.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<MarkerKey, Object> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MINOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = opMarkerSetMap.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				// MAJOR ALLELE
				rdOperationSet.fillMapWithDefaultValue(opMarkerSetMap, "");
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetMap.get(entry.getKey()));
				}
			}
			for (Map.Entry<MarkerKey, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetMap, false, false);

			// WRITE QA MISMATCH STATE
			ReportWriter.appendColumnToReport(reportPath, reportName, storedMismatchStateMap, false, false);
			if (storedMismatchStateMap != null) {
				storedMismatchStateMap.clear();
			}

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
		}

		return result;
	}
}
