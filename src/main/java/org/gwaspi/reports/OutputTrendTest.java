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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class OutputTrendTest {

	private static final Logger log = LoggerFactory.getLogger(OutputTrendTest.class);

	private OutputTrendTest() {
	}

	public static void writeReportsForTrendTestData(OperationKey operationKey) throws IOException {

		OperationMetadata op = OperationsList.getOperation(operationKey);

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		//String manhattanName = "mnhtt_"+outName;
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info("Start saving trend test");
		writeManhattanPlotFromTrendTestData(operationKey, manhattanName, 4000, 500);
		ReportsList.insertRPMetadata(new Report(
				Integer.MIN_VALUE,
				"Trend Test Manhattan Plot",
				manhattanName + ".png",
				OPType.MANHATTANPLOT,
				operationKey,
				"Trend Test Manhattan Plot",
				op.getStudyKey()));
		log.info("Saved Manhattan Plot in reports folder");

		//String qqName = "qq_"+outName;
		String qqName = prefix + "qq";
		writeQQPlotFromTrendTestData(operationKey, qqName, 500, 500);
		ReportsList.insertRPMetadata(new Report(
				Integer.MIN_VALUE,
				"Trend Test QQ Plot",
				qqName + ".png",
				OPType.QQPLOT,
				operationKey,
				"Trend Test QQ Plot",
				op.getStudyKey()));
		log.info("Saved Trend Test QQ Plot in reports folder");

		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		createSortedTrendTestReport(operationKey, assocName);
		ReportsList.insertRPMetadata(new Report(
				Integer.MIN_VALUE,
				"Trend Tests Values",
				assocName + ".txt",
				OPType.TRENDTEST,
				operationKey,
				"Trend Tests Values",
				op.getStudyKey()));

		org.gwaspi.global.Utils.sysoutCompleted("Trend Test Reports & Charts");
	}

	public static void writeManhattanPlotFromTrendTestData(OperationKey operationKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(operationKey, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		//CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
		int pointNb = rdOPMetadata.getOpSetSize();
		int picWidth = 4000;
		if (pointNb < 1000) {
			picWidth = 600;
		} else if (pointNb < 1E4) {
			picWidth = 1000;
		} else if (pointNb < 1E5) {
			picWidth = 1500;
		} else if (pointNb < 5E5) {
			picWidth = 2000;
		}

		String imagePath = Study.constructReportsPath(rdOPMetadata.getStudyKey()) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					picWidth,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	public static void writeQQPlotFromTrendTestData(OperationKey operationKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(operationKey, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP, 1);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
		String imagePath = Study.constructReportsPath(rdOPMetadata.getStudyKey()) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					width,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	public static void createSortedTrendTestReport(OperationKey operationKey, String reportName) throws IOException {

		Map<MarkerKey, double[]> unsortedMarkerIdTrendTestValsMap = GenericReportGenerator.getAnalysisVarData(operationKey, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);
		Map<MarkerKey, Double> unsortedMarkerIdPvalMap = new LinkedHashMap<MarkerKey, Double>(unsortedMarkerIdTrendTestValsMap.size());
		for (Map.Entry<MarkerKey, double[]> entry : unsortedMarkerIdTrendTestValsMap.entrySet()) {
			double[] values = entry.getValue();
			unsortedMarkerIdPvalMap.put(entry.getKey(), values[1]);
		}
		Collection<MarkerKey> sortedMarkerKeys = org.gwaspi.global.Utils.createMapSortedByValue(unsortedMarkerIdPvalMap).keySet();
		unsortedMarkerIdTrendTestValsMap.clear(); // "garbage collection"
		unsortedMarkerIdPvalMap.clear(); // "garbage collection"

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
		MarkerSet rdInfoMarkerSet = new MarkerSet(operationKey.getParentMatrixKey());
		rdInfoMarkerSet.initFullMarkerIdSetMap();

		// WRITE HEADER OF FILE
		String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tTrend-Test\tPval\n";
		String reportNameExt = reportName + ".txt";
		String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		// WRITE MARKERSET RSID
		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
		Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
		ReportWriter.writeFirstColumnToReport(reportPath, reportNameExt, header, sortedMarkerRSIDs, true);

		// WRITE MARKERSET CHROMOSOME
		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
		Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
		ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerCHRs, false, false);

		// WRITE MARKERSET POS
		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
		Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, rdInfoMarkerSet.getMarkerIdSetMapInteger());
		ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerPos, false, false);

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
		Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortedMarkerKeys.size());
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
		sortedMarkerAlleles = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, sortedMarkerAlleles); // XXX probably not required?
		ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerAlleles, false, false);

		// WRITE TREND TEST VALUES
		Map<MarkerKey, double[]> sortedTrendTestVals = org.gwaspi.global.Utils.createOrderedMap(sortedMarkerKeys, unsortedMarkerIdTrendTestValsMap);
		ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedTrendTestVals, true, false);
	}
}
