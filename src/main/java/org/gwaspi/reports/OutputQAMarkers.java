package org.gwaspi.reports;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
import ucar.nc2.NetcdfFile;

public class OutputQAMarkers {

	public static boolean writeReportsForQAMarkersData(int opId) throws FileNotFoundException, IOException {
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

	public static boolean createSortedMarkerMissingnessReport(int opId, String reportName) throws FileNotFoundException, IOException {
		boolean result = false;

		try {
			LinkedHashMap unsortedMarkerIdMissingRatLHM = GatherQAMarkersData.loadMarkerQAMissingRatio(opId);
			LinkedHashMap sortedMarkerIdMissingRatLHM = ReportManager.getSortedDescendingMarkerSetByDoubleValue(unsortedMarkerIdMissingRatLHM);
			if (unsortedMarkerIdMissingRatLHM != null) {
				unsortedMarkerIdMissingRatLHM.clear();
			}

			//PREPARE SORTING LHM & STORE QA VALUES FOR LATER
			LinkedHashMap sortingMarkerSetLHM = new LinkedHashMap();
			LinkedHashMap storedMissingRatLHM = new LinkedHashMap();
			for (Iterator it = sortedMarkerIdMissingRatLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
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
			MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());
			MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			NetcdfFile matrixNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			LinkedHashMap infoMatrixMarkerSetLHM = rdInfoMarkerSet.getMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID, sep);
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);

			//WRITE MARKERSET CHROMOSOME
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			ArrayList operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
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
				LinkedHashMap opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				//MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object minorAllele = opMarkerSetLHM.get(key);
					infoMatrixMarkerSetLHM.put(key, minorAllele);
				}

				//MAJOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object minorAllele = infoMatrixMarkerSetLHM.get(key);
					infoMatrixMarkerSetLHM.put(key, minorAllele + sep + opMarkerSetLHM.get(key));
				}


			}
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
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

	public static boolean createMarkerMismatchReport(int opId, String reportName) throws FileNotFoundException, IOException {
		boolean result = false;

		try {
			LinkedHashMap unsortedMarkerIdMismatchStateLHM = GatherQAMarkersData.loadMarkerQAMismatchState(opId);
			LinkedHashMap sortingMarkerSetLHM = new LinkedHashMap();
			for (Iterator it = unsortedMarkerIdMismatchStateLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				int mismatchState = (Integer) unsortedMarkerIdMismatchStateLHM.get(key);
				if (mismatchState > 0) {
					sortingMarkerSetLHM.put(key, mismatchState);
				}
			}
			if (unsortedMarkerIdMismatchStateLHM != null) {
				unsortedMarkerIdMismatchStateLHM.clear();
			}

			//STORE MISMATCH STATE FOR LATER
			LinkedHashMap storedMismatchStateLHM = new LinkedHashMap();
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = sortingMarkerSetLHM.get(key);
				storedMismatchStateLHM.put(key, value);
			}


			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MatrixMetadata rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());
			MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			NetcdfFile matrixNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
			LinkedHashMap infoMatrixMarkerSetLHM = rdInfoMarkerSet.getMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


			//WRITE MARKERSET CHROMOSOME
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, true);


			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			infoMatrixMarkerSetLHM = rdInfoMarkerSet.fillMarkerSetLHMWithVariable(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS);
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			ArrayList operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
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
				LinkedHashMap opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				//MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object minorAllele = opMarkerSetLHM.get(key);
					infoMatrixMarkerSetLHM.put(key, minorAllele);
				}

				//MAJOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Iterator it = infoMatrixMarkerSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					Object minorAllele = infoMatrixMarkerSetLHM.get(key);
					infoMatrixMarkerSetLHM.put(key, minorAllele + sep + opMarkerSetLHM.get(key));
				}

			}
			for (Iterator it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Object value = infoMatrixMarkerSetLHM.get(key);
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
