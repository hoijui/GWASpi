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
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
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

public class OutputHardyWeinberg {

	private static final Logger log
			= LoggerFactory.getLogger(OutputHardyWeinberg.class);

	private OutputHardyWeinberg() {
	}

	public static boolean writeReportsForMarkersHWData(int opId) throws IOException {
		OperationMetadata op = OperationsList.getById(opId);

		//String hwOutName = "hw_"+op.getId()+"_"+op.getFriendlyName()+".hw";
		String prefix = ReportsList.getReportNamePrefix(op);
		String hwOutName = prefix + "hardy-weinberg.txt";

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));

		if (processSortedHardyWeinbergReport(opId, hwOutName)) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Hardy Weinberg Table",
					hwOutName,
					OPType.HARDY_WEINBERG,
					op.getParentMatrixId(),
					opId,
					"Hardy Weinberg Table",
					op.getStudyKey()));

			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Report");
		}

		return true;
	}

	protected static boolean processSortedHardyWeinbergReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, Double> unsortedMarkerIdHWPval_ALTMap = GatherHardyWeinbergData.loadHWPval_ALT(opId);
			Map<MarkerKey, Double> sortingMarkerSetMap = org.gwaspi.global.Utils.createMapSortedByValue(unsortedMarkerIdHWPval_ALTMap);
			if (unsortedMarkerIdHWPval_ALTMap != null) {
				unsortedMarkerIdHWPval_ALTMap.clear();
			}

			// STORE HW VALUES FOR LATER
			Map<MarkerKey, Double> storeHWPval_ALTMap = new LinkedHashMap<MarkerKey, Double>();
			storeHWPval_ALTMap.putAll(sortingMarkerSetMap);

			// GET MARKER INFO
			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
			MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyKey(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin_Allele\tMaj_Allele\t" + Text.Reports.hwPval + Text.Reports.CTRL + "\t" + Text.Reports.hwObsHetzy + Text.Reports.CTRL + "\t" + Text.Reports.hwExpHetzy + Text.Reports.CTRL + "\n";
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
			List<OperationMetadata> operations = OperationsList.getOperationsList(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operations.size(); i++) {
				OperationMetadata op = operations.get(i);
				if (op.getType().equals(OPType.MARKER_QA)) {
					markersQAopId = op.getId();
				}
			}
			Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortingMarkerSetMap.size());
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(rdOPMetadata.getStudyKey(), markersQAopId);
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

			// WRITE HW PVAL
			ReportWriter.appendColumnToReport(reportPath, reportName, storeHWPval_ALTMap, false, false);
			if (storeHWPval_ALTMap != null) {
				storeHWPval_ALTMap.clear();
			}

			// WRITE HW HETZY ARRAY
			Map<MarkerKey, Double> markerIdHWHETZY_CTRLMap = GatherHardyWeinbergData.loadHWHETZY_ALT(opId);
			Map<MarkerKey, Double> sortedHWHETZYCTRLs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, markerIdHWHETZY_CTRLMap);
			ReportWriter.appendColumnToReport(reportPath, reportName, sortedHWHETZYCTRLs, true, false);

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
		}

		return result;
	}
}
