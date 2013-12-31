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
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestOperationEntry;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputTest {

	private static final Logger log = LoggerFactory.getLogger(OutputTest.class);

	private final OperationKey testOpKey;
	private final OPType testType;
	private final String testName;
	private final int qqPlotDof;
	private final String header;

	public OutputTest(OperationKey testOpKey, OPType testType) {

		this.testOpKey = testOpKey;
		this.testType = testType;

		final String headerBase = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele";
		switch (testType) {
			case ALLELICTEST:
				this.qqPlotDof = 1;
				this.header = headerBase + "\tX²\tPval\tOR\n";
				break;
			case GENOTYPICTEST:
				this.qqPlotDof = 2;
				this.header = headerBase + "\tX²\tPval\t?OR-AA/aa?\t?OR-Aa/aa?\n";
				break;
			case COMBI_ASSOC_TEST:
				this.qqPlotDof = 2; // FIXME for COMBI test
				this.header = headerBase + "\tX²\tPval\tOR-AA/aa\tOR-Aa/aa\n"; // FIXME for COMBI test
				break;
			case TRENDTEST:
				this.qqPlotDof = 1;
				this.header = headerBase + "\tTrend-Test\tPval\n";
				break;
			default:
				throw new IllegalArgumentException("Not a supported test type: " + testType.toString());
		}
		this.testName = createTestName(testType);
	}

	public static String createTestName(OPType testType) {

		final String testName;
		switch (testType) {
			case ALLELICTEST:
				testName = "Allelic Association";
				break;
			case GENOTYPICTEST:
				testName = "Genotypic Association";
				break;
			case COMBI_ASSOC_TEST:
				testName = "COMBI Association";
				break;
			case TRENDTEST:
				testName = "Cochran-Armitage Trend";
				break;
			default:
				throw new IllegalArgumentException("Not a supported test type: " + testType.toString());
		}

		return testName;
	}

	public OutputTest(OperationKey testOpKey) throws IOException {
		this(testOpKey, OperationsList.getOperation(testOpKey).getOperationType());
	}

	public void writeReportsForTestData() throws IOException {

		OperationMetadata op = OperationsList.getOperation(testOpKey);
		final StudyKey studyKey = testOpKey.getParentMatrixKey().getStudyKey();

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(studyKey)));
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info("Start saving {} test", testName);
		writeManhattanPlotFromAssociationData(manhattanName, 4000, 500);
		if (testType != OPType.COMBI_ASSOC_TEST) {
			ReportsList.insertRPMetadata(new Report(
					testName + " Test Manhattan Plot",
					manhattanName + ".png",
					OPType.MANHATTANPLOT,
					testOpKey,
					testName + " Test Manhattan Plot",
					studyKey));
			log.info("Saved " + testName + " Test Manhattan Plot in reports folder");
		}

		String qqName = prefix + "qq";
		writeQQPlotFromAssociationData(qqName, 500, 500);
		if (testType != OPType.COMBI_ASSOC_TEST) {
			ReportsList.insertRPMetadata(new Report(
					testName + " Test QQ Plot",
					qqName + ".png",
					OPType.QQPLOT,
					testOpKey,
					testName + " Test QQ Plot",
					studyKey));
			log.info("Saved {} Test QQ Plot in reports folder", testName);
		}

		String assocName = prefix;
		createSortedAssociationReport(assocName);
		ReportsList.insertRPMetadata(new Report(
				testName + " Tests Values",
				assocName + ".txt",
				testType,
				testOpKey,
				testName + " Tests Values",
				studyKey));

		org.gwaspi.global.Utils.sysoutCompleted(testName + " Test Reports & Charts");
	}

	private void writeManhattanPlotFromAssociationData(String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(testOpKey);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		// CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); // Hue, saturation, brightness

		OperationMetadata rdOPMetadata = OperationsList.getOperation(testOpKey);
		int pointNb = rdOPMetadata.getNumMarkers();
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

		String imagePath = Study.constructReportsPath(testOpKey.getParentMatrixKey().getStudyKey()) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					picWidth,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	private void writeQQPlotFromAssociationData(String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(testOpKey, qqPlotDof);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperation(testOpKey);
		String imagePath = Study.constructReportsPath(rdOPMetadata.getStudyKey()) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(
					new File(imagePath),
					chart,
					width,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	private void createSortedAssociationReport(String reportName) throws IOException {

		OperationDataSet<? extends TrendTestOperationEntry> testOperationDataSet = OperationFactory.generateOperationDataSet(testOpKey);
		List<? extends TrendTestOperationEntry> testOperationEntries = (List) testOperationDataSet.getEntries(); // HACK This might not be a List!
		Collections.sort(testOperationEntries, new TrendTestOperationEntry.PValueComparator());

		List<Integer> sortedOrigIndices = new ArrayList<Integer>(testOperationEntries.size());
		for (TrendTestOperationEntry trendTestOperationEntry : testOperationEntries) {
			sortedOrigIndices.add(trendTestOperationEntry.getIndex());
		}

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperation(testOpKey);
		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(testOpKey.getParentMatrixKey());
		MarkersMetadataSource markersMetadatas = matrixDataSetSource.getMarkersMetadatasSource();
		List<MarkerMetadata> orderedMarkersMetadatas = Utils.createIndicesOrderedList(sortedOrigIndices, markersMetadatas);

		// WRITE HEADER OF FILE
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

		// WRITE DATA TO REPORT
		ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_T));
		ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_P));
		if (testType != OPType.TRENDTEST) { // FIXME for COMBI test
			ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(AllelicAssociationTestOperationEntry.TO_OR));
			if (testType != OPType.ALLELICTEST) { // FIXME for COMBI test
				ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(GenotypicAssociationTestOperationEntry.TO_OR2));
			}
		}
	}
}
