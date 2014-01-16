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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Utils;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputTrendTest {

	private static final Logger log = LoggerFactory.getLogger(OutputTrendTest.class);

	private OutputTrendTest() {
	}

	public static void writeReportsForTrendTestData(OperationKey trendTestOpKey) throws IOException {

		OperationMetadata op = OperationsList.getOperation(trendTestOpKey);

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		//String manhattanName = "mnhtt_"+outName;
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info("Start saving trend test");
		writeManhattanPlotFromTrendTestData(trendTestOpKey, manhattanName, 4000, 500);
		ReportsList.insertRPMetadata(new Report(
				"Trend Test Manhattan Plot",
				manhattanName + ".png",
				OPType.MANHATTANPLOT,
				trendTestOpKey,
				"Trend Test Manhattan Plot",
				op.getStudyKey()));
		log.info("Saved Manhattan Plot in reports folder");

		//String qqName = "qq_"+outName;
		String qqName = prefix + "qq";
		writeQQPlotFromTrendTestData(trendTestOpKey, qqName, 500, 500);
		ReportsList.insertRPMetadata(new Report(
				"Trend Test QQ Plot",
				qqName + ".png",
				OPType.QQPLOT,
				trendTestOpKey,
				"Trend Test QQ Plot",
				op.getStudyKey()));
		log.info("Saved Trend Test QQ Plot in reports folder");

		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		createSortedTrendTestReport(trendTestOpKey, assocName);
		ReportsList.insertRPMetadata(new Report(
				"Trend Tests Values",
				assocName + ".txt",
				OPType.TRENDTEST,
				trendTestOpKey,
				"Trend Tests Values",
				op.getStudyKey()));

		org.gwaspi.global.Utils.sysoutCompleted("Trend Test Reports & Charts");
	}

	public static void writeManhattanPlotFromTrendTestData(OperationKey trendTestOpKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(trendTestOpKey);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		//CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness

		OperationMetadata rdOPMetadata = OperationsList.getOperation(trendTestOpKey);
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

	public static void writeQQPlotFromTrendTestData(OperationKey testOpKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(testOpKey, 1);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperation(testOpKey);
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

	public static void createSortedTrendTestReport(OperationKey trendTestOpKey, String reportName) throws IOException {

		TrendTestOperationDataSet trendTestOperationDataSet = (TrendTestOperationDataSet) OperationFactory.generateOperationDataSet(trendTestOpKey);
		List<TrendTestOperationEntry> trendTestOperationEntries = (List) trendTestOperationDataSet.getEntries(); // HACK This might not be a List!
		Collections.sort(trendTestOperationEntries, new TrendTestOperationEntry.PValueComparator());

		List<Integer> sortedOrigIndices = new ArrayList<Integer>(trendTestOperationEntries.size());
		for (TrendTestOperationEntry trendTestOperationEntry : trendTestOperationEntries) {
			sortedOrigIndices.add(trendTestOperationEntry.getIndex());
		}

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperation(trendTestOpKey);
		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(trendTestOpKey.getParentMatrixKey());
		MarkersMetadataSource markersMetadatas = matrixDataSetSource.getMarkersMetadatasSource();
		List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(sortedOrigIndices, markersMetadatas);

		// WRITE HEADER OF FILE
		String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tTrend-Test\tPval\n";
		reportName = reportName + ".txt";
		String reportPath = Study.constructReportsPath(rdOPMetadata.getStudyKey());

		// WRITE MARKERS ID & RSID
		ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, orderedMarkersMetadatas, null, MarkerMetadata.TO_MARKER_ID);
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_RS_ID);

		// WRITE MARKERSET CHROMOSOME
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, MarkerMetadata.TO_CHR);

		// WRITE MARKERSET POS
		ReportWriter.appendColumnToReport(reportPath, reportName, orderedMarkersMetadatas, null, new Extractor.ToStringMetaExtractor(MarkerMetadata.TO_POS));

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
		QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationFactory.generateOperationDataSet(markersQAopKey);
		List<Byte> knownMinorAlleles = (List) qaMarkersOperationDataSet.getKnownMinorAllele(-1, -1); // HACK might not be a List!
		List<Byte> knownMajorAlleles = (List) qaMarkersOperationDataSet.getKnownMajorAllele(-1, -1); // HACK might not be a List!
		List<String> sortedMarkerAlleles = new ArrayList<String>(sortedOrigIndices.size());
		for (Integer origIndices : sortedOrigIndices) {
			final char knownMinorAllele = (char) (byte) knownMinorAlleles.get(origIndices);
			final char knownMajorAllele = (char) (byte) knownMajorAlleles.get(origIndices);
			String concatenatedValue = knownMinorAllele + sep + knownMajorAllele;
			sortedMarkerAlleles.add(concatenatedValue);
		}
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, null, new Extractor.ToStringExtractor());

		// WRITE TREND TEST VALUES
		ReportWriter.appendColumnToReport(reportPath, reportName, trendTestOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_T));
		ReportWriter.appendColumnToReport(reportPath, reportName, trendTestOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_P));
	}
}
