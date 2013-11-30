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

public class OutputAssociation {

	private static final Logger log = LoggerFactory.getLogger(OutputAssociation.class);

	private final OPType testType;
	private final String testName;
	private final int qqPlotDof;
	private final String header;
	private final boolean allelic;
	private final boolean combi = false;

	public OutputAssociation(boolean allelic) {

		this.testType = (combi ? OPType.COMBI_ASSOC_TEST : (allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST));
		this.testName = (allelic ? "Allelic" : "Genotypic") + (combi ? " Combi" : "");
		this.qqPlotDof = allelic ? 1 : 2; // FIXME
		this.allelic = allelic;
		this.header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tX²\tPval\t" + (allelic ? "OR" : "OR-AA/aa\tOR-Aa/aa") + "\n"; // FIXME
	}

	public void writeReportsForAssociationData(OperationKey assocTestOpKey) throws IOException {

		OperationMetadata op = OperationsList.getOperation(assocTestOpKey);

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		//String manhattanName = "mnhtt_" + outName;
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info("Start saving {} test", testName);
		writeManhattanPlotFromAssociationData(assocTestOpKey, manhattanName, 4000, 500);
		if (!combi) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					testName + " assoc. Manhattan Plot",
					manhattanName + ".png",
					OPType.MANHATTANPLOT,
					assocTestOpKey,
					testName + " Association Manhattan Plot",
					op.getStudyKey()));
			log.info("Saved " + testName + " Association Manhattan Plot in reports folder");
		}

		//String qqName = "qq_" + outName;
		String qqName = prefix + "qq";
		writeQQPlotFromAssociationData(assocTestOpKey, qqName, 500, 500);
		if (!combi) {
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					testName + " assoc. QQ Plot",
					qqName + ".png",
					OPType.QQPLOT,
					assocTestOpKey,
					testName + " Association QQ Plot",
					op.getStudyKey()));
			log.info("Saved {} Association QQ Plot in reports folder", testName);
		}

		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		createSortedAssociationReport(assocTestOpKey, assocName);
		ReportsList.insertRPMetadata(new Report(
				Integer.MIN_VALUE,
				testName + " Association Tests Values",
				assocName + ".txt",
				testType,
				assocTestOpKey,
				testName + " Association Tests Values",
				op.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted(testName + " Association Reports & Charts");
	}

	private void writeManhattanPlotFromAssociationData(OperationKey assocTestOpKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(assocTestOpKey);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		// CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); // Hue, saturation, brightness

		OperationMetadata rdOPMetadata = OperationsList.getOperation(assocTestOpKey);
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

		String imagePath = Study.constructReportsPath(assocTestOpKey.getParentMatrixKey().getStudyKey()) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					picWidth,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	private void writeQQPlotFromAssociationData(OperationKey operationKey, String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(operationKey, qqPlotDof);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperation(operationKey);
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

	private void createSortedAssociationReport(OperationKey assocTestOpKey, String reportName) throws IOException {

		OperationDataSet<? extends TrendTestOperationEntry> testOperationDataSet = OperationFactory.generateOperationDataSet(assocTestOpKey);
		List<? extends TrendTestOperationEntry> testOperationEntries = (List) testOperationDataSet.getEntries(); // HACK This might not be a List!
		Collections.sort(testOperationEntries, new TrendTestOperationEntry.PValueComparator());

		List<Integer> sortedOrigIndices = new ArrayList<Integer>(testOperationEntries.size());
		for (TrendTestOperationEntry trendTestOperationEntry : testOperationEntries) {
			sortedOrigIndices.add(trendTestOperationEntry.getIndex());
		}

		String sep = cExport.separator_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperation(assocTestOpKey);
		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(assocTestOpKey.getParentMatrixKey());
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
		ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(AllelicAssociationTestOperationEntry.TO_OR));
		if (!allelic) {
			ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(GenotypicAssociationTestOperationEntry.TO_OR2));
		}
	}
}
