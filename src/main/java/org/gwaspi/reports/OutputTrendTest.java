package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
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
public class OutputTrendTest {

	private final static Logger log = LoggerFactory.getLogger(OutputTrendTest.class);

	private OutputTrendTest() {
	}

	public static boolean writeReportsForTrendTestData(int opId) throws IOException {
		boolean result = false;
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());
		//String manhattanName = "mnhtt_"+outName;
		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info(Text.All.processing);
		if (writeManhattanPlotFromTrendTestData(opId, manhattanName, 4000, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Trend Test Manhattan Plot",
					manhattanName + ".png",
					OPType.MANHATTANPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Trend Test Manhattan Plot",
					op.getStudyId());
			log.info("Saved Manhattan Plot in reports folder at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
		}
		//String qqName = "qq_"+outName;
		String qqName = prefix + "qq";
		if (result && writeQQPlotFromTrendTestData(opId, qqName, 500, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Trend Test QQ Plot",
					qqName + ".png",
					OPType.QQPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Trend Test QQ Plot",
					op.getStudyId());

			log.info("Saved Trend Test QQ Plot in reports folder at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
		}
		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		if (result && createSortedTrendTestReport(opId, assocName)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Trend Tests Values",
					assocName + ".txt",
					OPType.TRENDTEST.toString(),
					op.getParentMatrixId(),
					opId,
					"Trend Tests Values",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Trend Test Reports & Charts");
		}

		return result;
	}

	public static boolean writeManhattanPlotFromTrendTestData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(opId, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);

		JFreeChart chart = new JFreeChart("P value", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);


		//CHART BACKGROUD COLOR
		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);
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

	public static boolean writeQQPlotFromTrendTestData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(opId, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP, 1);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);
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

	public static boolean createSortedTrendTestReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdTrendTestValsMap = GenericReportGenerator.getAnalysisVarData(opId, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);
			Map<String, Object> unsortedMarkerIdPvalMap = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, Object> entry : unsortedMarkerIdTrendTestValsMap.entrySet()) {
				double[] values = (double[]) entry.getValue();
				unsortedMarkerIdPvalMap.put(entry.getKey(), values[1]);
			}

			Map<String, Object> sortingMarkerSetMap = ReportManager.getSortedMarkerSetByDoubleValue(unsortedMarkerIdPvalMap);
			if (unsortedMarkerIdPvalMap != null) {
				unsortedMarkerIdPvalMap.clear();
			}

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetMap();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tTrend-Test\tPval\n";
			String reportNameExt = reportName + ".txt";
			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";

			// WRITE MARKERSET RSID
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportNameExt, header, sortingMarkerSetMap, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);

			// WRITE MARKERSET POS
			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);

			// WRITE KNOWN ALLELES FROM QA
			// get MARKER_QA Operation
			List<Object[]> operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetMap = rdOperationSet.getOpSetMap();

				// MINOR ALLELE
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = opMarkerSetMap.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				// MAJOR ALLELE
				rdOperationSet.fillMapWithDefaultValue(opMarkerSetMap, "");
				opMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetMap.get(entry.getKey()));
				}
			}
			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetMap().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, false, false);

			// WRITE TREND TEST VALUES
			for (Map.Entry<String, Object> entry : sortingMarkerSetMap.entrySet()) {
				Object value = unsortedMarkerIdTrendTestValsMap.get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetMap, true, false);

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
		}

		return result;
	}
}
