package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
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
public class OutputGenotypicAssociation {

	private final static Logger log = LoggerFactory.getLogger(OutputGenotypicAssociation.class);

	private OutputGenotypicAssociation() {
	}

	public static boolean writeReportsForAssociationData(int opId) throws IOException {
		boolean result = false;
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue("ReportsDir", ""), "STUDY_" + op.getStudyId());
		//String manhattanName = "mnhtt_"+outName;
		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		log.info(org.gwaspi.global.Text.All.processing);
		if (writeManhattanPlotFromAssociationData(opId, manhattanName, 4000, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Genotypic assoc. Manhattan Plot",
					manhattanName + ".png",
					cNetCDF.Defaults.OPType.MANHATTANPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Genotypic Association Manhattan Plot",
					op.getStudyId());
			log.info("Saved Genotypic Association Manhattan Plot in reports folder at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
		}
		//String qqName = "qq_"+outName;
		String qqName = prefix + "qq";
		if (result && writeQQPlotFromAssociationData(opId, qqName, 500, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Genotypic assoc. QQ Plot",
					qqName + ".png",
					cNetCDF.Defaults.OPType.QQPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Genotypic Association QQ Plot",
					op.getStudyId());

			log.info("Saved Genotypic Association QQ Plot in reports folder at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
		}
		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		if (result && createSortedAssociationReport(opId, assocName)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Genotypic Association Tests Values",
					assocName + ".txt",
					cNetCDF.Defaults.OPType.GENOTYPICTEST.toString(),
					op.getParentMatrixId(),
					opId,
					"Genotypic Association Tests Values",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Genotypic Association Reports & Charts");
		}

		return result;
	}

	public static boolean writeManhattanPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator.buildManhattanPlot(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR);

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

		String imagePath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
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

	public static boolean writeQQPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator.buildQQPlot(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR, 2);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);
		String imagePath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
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

	public static boolean createSortedAssociationReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdAssocValsLHM = GenericReportGenerator.getAnalysisVarData(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR);
			Map<String, Object> unsortedMarkerIdPvalLHM = new LinkedHashMap<String, Object>();
			for (Map.Entry<String, Object> entry : unsortedMarkerIdAssocValsLHM.entrySet()) {
				double[] values = (double[]) entry.getValue();
				unsortedMarkerIdPvalLHM.put(entry.getKey(), values[1]);
			}

			Map<String, Object> sortingMarkerSetLHM = ReportManager.getSortedMarkerSetByDoubleValue(unsortedMarkerIdPvalLHM);
			if (unsortedMarkerIdPvalLHM != null) {
				unsortedMarkerIdPvalLHM.clear();
			}

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			// WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tX²\tPval\tOR-AA/aa\tOR-Aa/aa\n";
			String reportNameExt = reportName + ".txt";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";

			// WRITE MARKERSET RSID
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportNameExt, header, sortingMarkerSetLHM, true);

			// WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetLHM, false, false);

			// WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetLHM, false, false);

			// WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			List<Object[]> operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = operationsAL.get(i);
				if (element[1].toString().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				// MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = opMarkerSetLHM.get(entry.getKey());
					entry.setValue(minorAllele);
				}

				// MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Map.Entry<String, Object> entry : rdInfoMarkerSet.getMarkerIdSetLHM().entrySet()) {
					Object minorAllele = entry.getValue();
					entry.setValue(minorAllele + sep + opMarkerSetLHM.get(entry.getKey()));
				}

			}
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetLHM, false, false);

			// WRITE DATA TO REPORT
			for (Map.Entry<String, Object> entry : sortingMarkerSetLHM.entrySet()) {
				Object value = unsortedMarkerIdAssocValsLHM.get(entry.getKey());
				entry.setValue(value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportNameExt, sortingMarkerSetLHM, true, false);

			result = true;
		} catch (IOException ex) {
			result = false;
		}

		return result;
	}
}
