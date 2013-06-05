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

package org.gwaspi.reports;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.operations.AbstractOperationSet;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class OutputQAMarkers {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQAMarkers.class);

	private OutputQAMarkers() {
	}

	public static boolean writeReportsForQAMarkersData(OperationKey operationKey) throws IOException {
		OperationMetadata op = OperationsList.getOperation(operationKey);

		String prefix = ReportsList.getReportNamePrefix(op);
		String markMissOutName = prefix + "markmissing.txt";


		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));

		if (createSortedMarkerMissingnessReport(operationKey, markMissOutName)) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Marker Missingness Table",
					markMissOutName,
					OPType.MARKER_QA,
					operationKey,
					"Marker Missingness Table",
					op.getStudyKey()));

			org.gwaspi.global.Utils.sysoutCompleted("Marker Missingness QA Report");
		}


		String markMismatchOutName = prefix + "markmismatch.txt";
		if (createMarkerMismatchReport(operationKey, markMismatchOutName)) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Marker Mismatch State Table",
					markMismatchOutName,
					OPType.MARKER_QA,
					operationKey,
					"Marker Mismatch State Table",
					op.getStudyKey()));

			org.gwaspi.global.Utils.sysoutCompleted("Marker Mismatch QA Report");
		}

		return true;
	}

	private static boolean createSortedMarkerMissingnessReport(OperationKey operationKey, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, Double> unsortedMarkerIdMissingRatMap = GatherQAMarkersData.loadMarkerQAMissingRatio(operationKey);
			Map<MarkerKey, Double> sortedMarkerIdMissingRatMap = org.gwaspi.global.Utils.createMapSortedByValueDescending(unsortedMarkerIdMissingRatMap);
			if (unsortedMarkerIdMissingRatMap != null) {
				unsortedMarkerIdMissingRatMap.clear();
			}

			// PREPARE SORTING Map & STORE QA VALUES FOR LATER
			Map<MarkerKey, Double> sortingMarkerSetMap = new LinkedHashMap<MarkerKey, Double>();
			Map<MarkerKey, Double> storedMissingRatMap = new LinkedHashMap<MarkerKey, Double>();
			if (sortedMarkerIdMissingRatMap != null) {
				for (Map.Entry<MarkerKey, Double> entry : sortedMarkerIdMissingRatMap.entrySet()) {
					MarkerKey key = entry.getKey();
					double missingValue = entry.getValue();
					if (missingValue > 0) {
						storedMissingRatMap.put(key, missingValue);
						sortingMarkerSetMap.put(key, missingValue);
					}
				}

				sortedMarkerIdMissingRatMap.clear();
			}

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
			MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyKey(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMissing Ratio\n";
			String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortedMarkerRSIDs, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerCHRs, false, false);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapInteger());
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerPos, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<OperationMetadata> operations = OperationsList.getOperationsList(rdOPMetadata.getParentMatrixKey());
			OperationKey markersQAopKey = null;
			for (int i = 0; i < operations.size(); i++) {
				OperationMetadata op = operations.get(i);
				if (op.getType().equals(OPType.MARKER_QA)) {
					markersQAopKey = OperationKey.valueOf(op);
				}
			}
			Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortingMarkerSetMap.size());
			if (markersQAopKey != null) {
				OperationMetadata qaMetadata = OperationsList.getOperation(markersQAopKey);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(markersQAopKey);
				Map<MarkerKey, char[]> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MINOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (MarkerKey key : rdInfoMarkerSet.getMarkerKeys()) {
					char[] minorAllele = opMarkerSetMap.get(key);
					sortedMarkerAlleles.put(key, new String(minorAllele));
				}

				// MAJOR ALLELE
				AbstractOperationSet.fillMapWithDefaultValue(opMarkerSetMap, new char[0]);
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<MarkerKey, String> entry : sortedMarkerAlleles.entrySet()) {
					String minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + new String(opMarkerSetMap.get(entry.getKey())));
				}
			}
			sortedMarkerAlleles = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, sortedMarkerAlleles); // XXX probably not required?
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, false, false);

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

	private static boolean createMarkerMismatchReport(OperationKey operationKey, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, Integer> unsortedMarkerIdMismatchStateMap = GatherQAMarkersData.loadMarkerQAMismatchState(operationKey);
			Map<MarkerKey, Integer> sortingMarkerSetMap = new LinkedHashMap<MarkerKey, Integer>();
			if (unsortedMarkerIdMismatchStateMap != null) {
				for (Map.Entry<MarkerKey, Integer> entry : unsortedMarkerIdMismatchStateMap.entrySet()) {
					MarkerKey key = entry.getKey();
					int mismatchState = entry.getValue();
					if (mismatchState > 0) {
						sortingMarkerSetMap.put(key, mismatchState);
					}
				}

				unsortedMarkerIdMismatchStateMap.clear();
			}

			// STORE MISMATCH STATE FOR LATER
			Map<MarkerKey, Integer> storedMismatchStateMap = new LinkedHashMap<MarkerKey, Integer>();
			storedMismatchStateMap.putAll(sortingMarkerSetMap);

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
			MarkerSet rdInfoMarkerSet = new MarkerSet(operationKey.getParentMatrixKey());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tMismatching\n";
			String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortedMarkerRSIDs, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerCHRs, false, true);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapInteger());
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerPos, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<OperationMetadata> operations = OperationsList.getOperationsList(rdOPMetadata.getParentMatrixKey());
			OperationKey markersQAopKey = null;
			for (int i = 0; i < operations.size(); i++) {
				OperationMetadata op = operations.get(i);
				if (op.getType().equals(OPType.MARKER_QA)) {
					markersQAopKey = OperationKey.valueOf(op);
				}
			}
			Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortingMarkerSetMap.size());
			if (markersQAopKey != null) {
				OperationMetadata qaMetadata = OperationsList.getOperation(markersQAopKey);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(markersQAopKey);
				Map<MarkerKey, char[]> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MINOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (MarkerKey key : rdInfoMarkerSet.getMarkerKeys()) {
					char[] minorAllele = opMarkerSetMap.get(key);
					sortedMarkerAlleles.put(key, new String(minorAllele));
				}

				// MAJOR ALLELE
				AbstractOperationSet.fillMapWithDefaultValue(opMarkerSetMap, new char[0]);
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<MarkerKey, String> entry : sortedMarkerAlleles.entrySet()) {
					String minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + new String(opMarkerSetMap.get(entry.getKey())));
				}
			}
			sortedMarkerAlleles = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, sortedMarkerAlleles); // XXX probably not required?
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, false, false);

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
