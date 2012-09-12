package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
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
public class OutputHardyWeinberg {

	private OutputHardyWeinberg() {
	}

	public static boolean writeReportsForMarkersHWData(int opId) throws IOException {
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		//String hwOutName = "hw_"+op.getOperationId()+"_"+op.getOperationFriendlyName()+".hw";
		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String hwOutName = prefix + "hardy-weinberg.txt";

		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());

		if (processSortedHardyWeinbergReport(opId, hwOutName)) {
			ReportManager.insertRPMetadata(dBManager,
					"Hardy Weinberg Table",
					hwOutName,
					cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString(),
					op.getParentMatrixId(),
					opId,
					"Hardy Weinberg Table",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Report");
		}

		return true;
	}

	protected static boolean processSortedHardyWeinbergReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdHWPval_ALTLHM = GatherHardyWeinbergData.loadHWPval_ALT(opId);
			Map<String, Object> sortingMarkerSetLHM = ReportManager.getSortedMarkerSetByDoubleValue(unsortedMarkerIdHWPval_ALTLHM);
			if (unsortedMarkerIdHWPval_ALTLHM != null) {
				unsortedMarkerIdHWPval_ALTLHM.clear();
			}

			//STORE HW VALUES FOR LATER
			Map<String, Object> storeHWPval_ALTLHM = new LinkedHashMap<String, Object>();
			storeHWPval_ALTLHM.putAll(sortingMarkerSetLHM);

			//GET MARKER INFO
			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin_Allele\tMaj_Allele\t" + Text.Reports.hwPval + Text.Reports.CTRL + "\t" + Text.Reports.hwObsHetzy + Text.Reports.CTRL + "\t" + Text.Reports.hwExpHetzy + Text.Reports.CTRL + "\n";
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


			//WRITE HW PVAL
			ReportWriter.appendColumnToReport(reportPath, reportName, storeHWPval_ALTLHM, false, false);
			if (storeHWPval_ALTLHM != null) {
				storeHWPval_ALTLHM.clear();
			}

			//WRITE HW HETZY ARRAY
			Map<String, Object> markerIdHWHETZY_CTRLLHM = GatherHardyWeinbergData.loadHWHETZY_ALT(opId);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = markerIdHWHETZY_CTRLLHM.get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, true, false);

			result = true;
		} catch (IOException iOException) {
			result = false;
		}

		return result;
	}
}
