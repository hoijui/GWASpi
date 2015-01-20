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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestOperationEntry;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SuperProgressSource;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write reports and generate plots for test data.
 */
public class OutputTest extends AbstractOutputOperation<TestOutputParams> {

	private static final Logger log = LoggerFactory.getLogger(OutputTest.class);

	private static final Color MANHATTAN_PLOT_CHART_BACKGROUD_COLOR
			= Color.getHSBColor(0.1f, 0.1f, 1.0f); // Hue, saturation, brightness

	private static final ProcessInfo testOutputProcessInfo = new DefaultProcessInfo("Write test output to files", ""); // TODO

	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Output Test data",
					"Output Test data", // TODO We need a more elaborate description of this operation!
					null,
					false,
					false);

	private final String testName;
	private final int qqPlotDof;
	private final String header;
	private ProgressHandler operationPH;
	private ProgressHandler creatingManhattanPlotPH;
	private ProgressHandler creatingQQPlotPH;
	private ProgressHandler writingAssociationReportPH;

	public OutputTest(TestOutputParams params) {
		super(params);

		switch (getParams().getTestType()) {
			case ALLELICTEST:
				this.qqPlotDof = 1;
				break;
			case GENOTYPICTEST:
				this.qqPlotDof = 2;
				break;
			case TRENDTEST:
				this.qqPlotDof = 1;
				break;
			default:
				throw new IllegalArgumentException("Not a supported test type: "
						+ getParams().getTestType().toString());
		}
		this.header = OutputQAMarkers.createReportHeaderLine(
				createColumnHeaders(getParams().getTestType()));
		this.testName = createTestName(getParams().getTestType());
	}

	public static String[] createColumnHeaders(final OPType associationTestType) {

		final List<String> columns = new LinkedList<String>();
		columns.add(Text.Reports.markerId);
		columns.add(Text.Reports.rsId);
		columns.add(Text.Reports.chr);
		columns.add(Text.Reports.pos);
		columns.add(Text.Reports.minAallele);
		columns.add(Text.Reports.majAallele);
		if (associationTestType == OPType.TRENDTEST) {
			columns.add(Text.Reports.trendTest);
		} else {
			columns.add(Text.Reports.chiSqr);
		}
		columns.add(Text.Reports.pVal);
		switch (associationTestType) {
			case TRENDTEST:
				break;
			case ALLELICTEST:
				columns.add(Text.Reports.oddsRatio);
				break;
			case GENOTYPICTEST:
				columns.add(Text.Reports.ORAAaa);
				columns.add(Text.Reports.ORAaaa);
				break;
			default:
				throw new IllegalArgumentException("Not a supported test type: "
						+ associationTestType.toString());
		}
		columns.add(Text.Reports.zoom);
		columns.add(Text.Reports.externalResource);

		return columns.toArray(new String[columns.size()]);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		// XXX For this class, we should use a different return type on this method (ialso for the othe Output* classes)
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return testOutputProcessInfo;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		if (operationPH == null) {
			final ProcessInfo creatingManhattanPlotPI = new DefaultProcessInfo(
					"creating & writing the Manhattan plot", null);
			creatingManhattanPlotPH
					= new IndeterminateProgressHandler(creatingManhattanPlotPI);

			final ProcessInfo creatingQQPlotPI = new DefaultProcessInfo(
					"creating & writing QQ plot", null);
			creatingQQPlotPH
					= new IndeterminateProgressHandler(creatingQQPlotPI);

			final ProcessInfo writingAssociationReportPI = new DefaultProcessInfo(
					"writing the association report", null);
			writingAssociationReportPH
					= new IndeterminateProgressHandler(writingAssociationReportPI);

			Map<ProgressSource, Double> subProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>();

			subProgressSourcesAndWeights.put(creatingManhattanPlotPH, 0.4); // TODO adjust these weights!
			subProgressSourcesAndWeights.put(creatingQQPlotPH, 0.3);
			subProgressSourcesAndWeights.put(writingAssociationReportPH, 0.3);

			operationPH = new SuperProgressSource(testOutputProcessInfo, subProgressSourcesAndWeights);
		}

		return operationPH;
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

	@Override
	public int processMatrix() throws IOException {

		operationPH.setNewStatus(ProcessStatus.INITIALIZING);
		OperationMetadata op = OperationsList.getOperationMetadata(getParams().getTestOperationKey());
		final StudyKey studyKey = getParams().getTestOperationKey().getParentMatrixKey().getStudyKey();

		creatingManhattanPlotPH.setNewStatus(ProcessStatus.INITIALIZING);
		Utils.createFolder(new File(Study.constructReportsPath(studyKey)));
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info("Start saving {} test", testName);
		operationPH.setNewStatus(ProcessStatus.RUNNING);
		creatingManhattanPlotPH.setNewStatus(ProcessStatus.RUNNING);
		writeManhattanPlotFromAssociationData(manhattanName, 4000, 500);
		creatingManhattanPlotPH.setNewStatus(ProcessStatus.FINALIZING);
		if (getParams().getTestType() != OPType.COMBI_ASSOC_TEST) {
			ReportsList.insertRPMetadata(new Report(
					testName + " Test Manhattan Plot",
					manhattanName + ".png",
					OPType.MANHATTANPLOT,
					getParams().getTestOperationKey(),
					testName + " Test Manhattan Plot",
					studyKey));
			log.info("Saved " + testName + " Test Manhattan Plot in reports folder");
		}
		creatingManhattanPlotPH.setNewStatus(ProcessStatus.COMPLEETED);

		creatingQQPlotPH.setNewStatus(ProcessStatus.INITIALIZING);
		String qqName = prefix + "qq";
		creatingQQPlotPH.setNewStatus(ProcessStatus.RUNNING);
		writeQQPlotFromAssociationData(qqName, 500, 500);
		creatingQQPlotPH.setNewStatus(ProcessStatus.FINALIZING);
		if (getParams().getTestType() != OPType.COMBI_ASSOC_TEST) {
			ReportsList.insertRPMetadata(new Report(
					testName + " Test QQ Plot",
					qqName + ".png",
					OPType.QQPLOT,
					getParams().getTestOperationKey(),
					testName + " Test QQ Plot",
					studyKey));
			log.info("Saved {} Test QQ Plot in reports folder", testName);
		}
		creatingQQPlotPH.setNewStatus(ProcessStatus.COMPLEETED);

		writingAssociationReportPH.setNewStatus(ProcessStatus.INITIALIZING);
		String assocName = prefix;
		writingAssociationReportPH.setNewStatus(ProcessStatus.RUNNING);
		createSortedAssociationReport(assocName);
		writingAssociationReportPH.setNewStatus(ProcessStatus.FINALIZING);
		ReportsList.insertRPMetadata(new Report(
				testName + " Tests Values",
				assocName + ".txt",
				getParams().getTestType(),
				getParams().getTestOperationKey(),
				testName + " Tests Values",
				studyKey));
		writingAssociationReportPH.setNewStatus(ProcessStatus.COMPLEETED);
		operationPH.setNewStatus(ProcessStatus.FINALIZING);

		Utils.sysoutCompleted(testName + " Test Reports & Charts");
		operationPH.setNewStatus(ProcessStatus.COMPLEETED);

		return Integer.MIN_VALUE;
	}

	private void writeManhattanPlotFromAssociationData(String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(getParams().getTestOperationKey());

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		chart.setBackgroundPaint(MANHATTAN_PLOT_CHART_BACKGROUD_COLOR);

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(getParams().getTestOperationKey());
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

		final StudyKey studyKey = getParams().getTestOperationKey().getParentMatrixKey().getStudyKey();
		String imagePath = Study.constructReportsPath(studyKey) + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(
					new File(imagePath),
					chart,
					picWidth,
					height);
		} catch (IOException ex) {
			throw new IOException("Problem occurred creating chart", ex);
		}
	}

	private void writeQQPlotFromAssociationData(String outName, int width, int height) throws IOException {

		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(getParams().getTestOperationKey(), qqPlotDof);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(getParams().getTestOperationKey());
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

	private static int[] createOrigIndexToQaMarkersIndexLookupTable(QAMarkersOperationDataSet qaMarkersOperationDataSet) throws IOException {

		final List<Integer> qaMarkersOrigIndices = qaMarkersOperationDataSet.getMarkersKeysSource().getIndices();
		final int[] origIndexToQaMarkersIndexLookupTable = new int[qaMarkersOperationDataSet.getOriginDataSetSource().getNumMarkers()];
		Arrays.fill(origIndexToQaMarkersIndexLookupTable, -1);
		int qaMarkersIndex = 0;
		for (Integer qaMarkersOrigIndex : qaMarkersOrigIndices) {
			origIndexToQaMarkersIndexLookupTable[qaMarkersOrigIndex] = qaMarkersIndex++;
		}

		return origIndexToQaMarkersIndexLookupTable;
	}

	private void createSortedAssociationReport(String reportName) throws IOException {

		OperationDataSet<? extends TrendTestOperationEntry> testOperationDataSet = OperationManager.generateOperationDataSet(getParams().getTestOperationKey());
		List<? extends TrendTestOperationEntry> testOperationEntries = new ArrayList<TrendTestOperationEntry>(testOperationDataSet.getEntries());
		Collections.sort(testOperationEntries, new TrendTestOperationEntry.PValueComparator());

		List<Integer> sortedOrigIndices = new ArrayList<Integer>(testOperationEntries.size());
		for (TrendTestOperationEntry trendTestOperationEntry : testOperationEntries) {
			sortedOrigIndices.add(trendTestOperationEntry.getIndex());
		}

		String sep = ExportConstants.SEPARATOR_REPORTS;
		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(getParams().getTestOperationKey());
		DataSetSource matrixDataSetSource = MatrixFactory.generateMatrixDataSetSource(getParams().getTestOperationKey().getParentMatrixKey());
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
		final QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(getParams().geQaMarkersOpKey());
		final int[] origIndexToQaMarkersIndexLookupTable = createOrigIndexToQaMarkersIndexLookupTable(qaMarkersOperationDataSet);
		final List<Byte> knownMinorAlleles = qaMarkersOperationDataSet.getKnownMinorAllele();
		final List<Byte> knownMajorAlleles = qaMarkersOperationDataSet.getKnownMajorAllele();
		final List<String> sortedMarkerAlleles = new ArrayList<String>(sortedOrigIndices.size());
		for (Integer sortedOrigIndex : sortedOrigIndices) {
			final int qaMarkersIndex = origIndexToQaMarkersIndexLookupTable[sortedOrigIndex];
			if (qaMarkersIndex < 0) {
				throw new IllegalArgumentException("The supplied QA Markers (" + getParams().geQaMarkersOpKey().toString() + ") operation does not contain all the required markers");
			}
			final char knownMinorAllele = (char) (byte) knownMinorAlleles.get(qaMarkersIndex);
			final char knownMajorAllele = (char) (byte) knownMajorAlleles.get(qaMarkersIndex);
			String concatenatedValue = knownMinorAllele + sep + knownMajorAllele;
			sortedMarkerAlleles.add(concatenatedValue);
		}
		ReportWriter.appendColumnToReport(reportPath, reportName, sortedMarkerAlleles, null, new Extractor.ToStringExtractor());

		// WRITE DATA TO REPORT
		ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_T));
		ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(TrendTestOperationEntry.TO_P));
		if (getParams().getTestType() != OPType.TRENDTEST) {
			ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(AllelicAssociationTestOperationEntry.TO_OR));
			if (getParams().getTestType() != OPType.ALLELICTEST) {
				ReportWriter.appendColumnToReport(reportPath, reportName, testOperationEntries, null, new Extractor.ToStringMetaExtractor(GenotypicAssociationTestOperationEntry.TO_OR2));
			}
		}
	}

	public static List<Object[]> parseAssociationTestReport(
			final File reportFile,
			final OPType associationTestType,
			final int numRowsToFetch,
			final boolean exactValues)
			throws IOException
	{
		final int numColumns = createColumnHeaders(associationTestType).length;
		return ReportWriter.parseReport(reportFile, new AssociationTestReportLineParser(exactValues, numColumns), numRowsToFetch);
	}

	private static class AssociationTestReportLineParser extends AbstractReportLineParser {

		private final int numColumns;
		private final boolean hasOr;
		private final boolean hasOr2;

		public AssociationTestReportLineParser(final boolean exactValues, final int numColumns, final boolean hasOr, final boolean hasOr2) {
			super(exactValues);

			this.numColumns = numColumns;
			this.hasOr = hasOr;
			this.hasOr2 = hasOr2;
		}

		public AssociationTestReportLineParser(final boolean exactValues, final int numColumns) {
			this(exactValues, numColumns, numColumns > 10, numColumns > 11);
		}

		@Override
		public Object[] extract(final String[] cVals) {

			final Object[] row = new Object[numColumns];

			final MarkerKey markerKey = new MarkerKey(cVals[0]);
			final String rsId = cVals[1];
			final ChromosomeKey chr = new ChromosomeKey(cVals[2]);
			final long position = Long.parseLong(cVals[3]);
			final String minAllele = cVals[4];
			final String majAllele = cVals[5];
			final Double chiSqr_f = maybeTryToRoundNicely(tryToParseDouble(cVals[6]));
			final Double pVal_f = maybeTryToRoundNicely(tryToParseDouble(cVals[7]));
			final Double or_f;
			if (hasOr) {
				or_f = maybeTryToRoundNicely(tryToParseDouble(cVals[8]));
			} else {
				or_f = null;
			}
			final Double or2_f;
			if (hasOr2) {
				or2_f = maybeTryToRoundNicely(tryToParseDouble(cVals[9]));
			} else {
				or2_f = null;
			}

			int col = 0;
			row[col++] = markerKey;
			row[col++] = rsId;
			row[col++] = chr;
			row[col++] = position;
			row[col++] = minAllele;
			row[col++] = majAllele;
			row[col++] = chiSqr_f;
			row[col++] = pVal_f;
			if (hasOr) {
				row[col++] = or_f;
			}
			if (hasOr2) {
				row[col++] = or2_f;
			}
			row[col++] = "";
			row[col++] = Text.Reports.queryDB;

			return row;
		}
	}
}
