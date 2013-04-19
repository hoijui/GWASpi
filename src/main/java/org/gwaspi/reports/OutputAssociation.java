package org.gwaspi.reports;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Association;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.dao.OperationService.MatrixOperationSpec;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
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

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputAssociation {

	private static final Logger log = LoggerFactory.getLogger(OutputAssociation.class);

	private final OPType testType;
	private final String testName;
	private final String variableName;
	private final int qqPlotDof;
	private final String header;

	public OutputAssociation(boolean allelic) {

		this.testType = allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST;
		this.testName = allelic ? "Allelic" : "Genotypic";
		this.variableName = allelic
				? Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR
				: Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
		this.qqPlotDof = allelic ? 1 : 2;
		this.header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tX²\tPval\t" + (allelic ? "OR" : "OR-AA/aa\tOR-Aa/aa") + "\n";
	}

	public boolean writeReportsForAssociationData(int opId) throws IOException {
		boolean result = false;
		Operation op = OperationsList.getById(opId);

		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());
		//String manhattanName = "mnhtt_" + outName;
		String prefix = ReportsList.getReportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info(Text.All.processing);
		if (writeManhattanPlotFromAssociationData(opId, manhattanName, 4000, 500)) {
			result = true;
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					testName + " assoc. Manhattan Plot",
					manhattanName + ".png",
					OPType.MANHATTANPLOT,
					op.getParentMatrixId(),
					opId,
					testName + " Association Manhattan Plot",
					op.getStudyId()));
			log.info("Saved " + testName + " Association Manhattan Plot in reports folder");
		}
		//String qqName = "qq_" + outName;
		String qqName = prefix + "qq";
		if (result && writeQQPlotFromAssociationData(opId, qqName, 500, 500)) {
			result = true;
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					testName + " assoc. QQ Plot",
					qqName + ".png",
					OPType.QQPLOT,
					op.getParentMatrixId(),
					opId,
					testName + " Association QQ Plot",
					op.getStudyId()));

			log.info("Saved " + testName + " Association QQ Plot in reports folder");
		}
		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		if (result && createSortedAssociationReport(opId, assocName)) {
			result = true;
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					testName + " Association Tests Values",
					assocName + ".txt",
					testType,
					op.getParentMatrixId(),
					opId,
					testName + " Association Tests Values",
					op.getStudyId()));

			org.gwaspi.global.Utils.sysoutCompleted(testName + " Association Reports & Charts");
		}

		return result;
	}

	private boolean writeManhattanPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		// Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(opId, variableName);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

		// CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); // Hue, saturation, brightness

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
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

		String imagePath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					picWidth,
					height);
			result = true;
		} catch (IOException ex) {
			log.error("Problem occurred creating chart", ex);
		}

		return result;
	}

	private boolean writeQQPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		// Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(opId, variableName, qqPlotDof);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
		String imagePath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					width,
					height);
			result = true;
		} catch (IOException ex) {
			log.error("Problem occurred creating chart", ex);
		}

		return result;
	}

	public boolean createSortedAssociationReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<MarkerKey, double[]> unsortedMarkerIdAssocValsMap = GenericReportGenerator.getAnalysisVarData(opId, variableName);
			Map<MarkerKey, Double> unsortedMarkerIdPvalMap = new LinkedHashMap<MarkerKey, Double>();
			for (Map.Entry<MarkerKey, double[]> entry : unsortedMarkerIdAssocValsMap.entrySet()) {
				double[] values = entry.getValue();
				unsortedMarkerIdPvalMap.put(entry.getKey(), values[1]);
			}

			Map<MarkerKey, ?> sortingMarkerSetMap = org.gwaspi.global.Utils.createMapSortedByValue(unsortedMarkerIdPvalMap);
			if (unsortedMarkerIdPvalMap != null) {
				unsortedMarkerIdPvalMap.clear();
			}

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
			MarkerSet rdInfoMarkerSet = new MarkerSet(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String reportNameExt = reportName + ".txt";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Map<MarkerKey, char[]> sortedMarkerRSIDs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.writeFirstColumnToReport(reportPath, reportNameExt, header, sortedMarkerRSIDs, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Map<MarkerKey, char[]> sortedMarkerCHRs = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapCharArray());
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerCHRs, false, false);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			Map<MarkerKey, Integer> sortedMarkerPos = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, rdInfoMarkerSet.getMarkerIdSetMapInteger());
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerPos, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<MatrixOperationSpec> operations = OperationsList.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operations.size(); i++) {
				MatrixOperationSpec op = operations.get(i);
				if (op.geType().equals(OPType.MARKER_QA)) {
					markersQAopId = op.getId();
				}
			}
			Map<MarkerKey, String> sortedMarkerAlleles = new LinkedHashMap<MarkerKey, String>(sortingMarkerSetMap.size());
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = OperationsList.getOperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				MarkerOperationSet rdOperationSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), markersQAopId);
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
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerAlleles, false, false);

			// WRITE DATA TO REPORT
			Map<MarkerKey, double[]> sortedMarkerAssocVals = org.gwaspi.global.Utils.createOrderedMap(sortingMarkerSetMap, unsortedMarkerIdAssocValsMap);
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortedMarkerAssocVals, true, false);

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
		}

		return result;
	}
}
