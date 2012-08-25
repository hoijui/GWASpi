package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.netCDF.operations.OperationMetadata;
import org.gwaspi.netCDF.operations.OperationSet;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputAllelicAssociation_opt {

	private OutputAllelicAssociation_opt() {
	}

	public static boolean writeReportsForAssociationData(int opId) throws IOException {
		boolean result = false;
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		org.gwaspi.global.Utils.createFolder(org.gwaspi.global.Config.getConfigValue("ReportsDir", ""), "STUDY_" + op.getStudyId());
		//String manhattanName = "mnhtt_"+outName;
		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		String manhattanName = prefix + "manhtt";

		System.out.println(org.gwaspi.global.Text.All.processing);
		if (writeManhattanPlotFromAssociationData(opId, manhattanName, 4000, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Allelic assoc. Manhattan Plot",
					manhattanName + ".png",
					cNetCDF.Defaults.OPType.MANHATTANPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Allelic Association Manhattan Plot",
					op.getStudyId());
			System.out.println("Saved Allelic Association Manhattan Plot in reports folder at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		}
		//String qqName = "qq_"+outName;
		String qqName = prefix + "qq";
		if (result && writeQQPlotFromAssociationData(opId, qqName, 500, 500)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Allelic assoc. QQ Plot",
					qqName + ".png",
					cNetCDF.Defaults.OPType.QQPLOT.toString(),
					op.getParentMatrixId(),
					opId,
					"Allelic Association QQ Plot",
					op.getStudyId());

			System.out.println("Saved Allelic Association QQ Plot in reports folder at " + org.gwaspi.global.Utils.getMediumDateTimeAsString());
		}
		//String assocName = "assoc_"+outName;
		String assocName = prefix;
		if (result && createSortedAssociationReport(opId, assocName)) {
			result = true;
			ReportManager.insertRPMetadata(dBManager,
					"Allelic Association Tests Values",
					assocName + ".txt",
					cNetCDF.Defaults.OPType.ALLELICTEST.toString(),
					op.getParentMatrixId(),
					opId,
					"Allelic Association Tests Values",
					op.getStudyId());

			org.gwaspi.global.Utils.sysoutCompleted("Allelic Association Reports & Charts");
		}

		return result;
	}

	public static boolean writeManhattanPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		CombinedRangeXYPlot combinedPlot = GenericReportGenerator_opt.buildManhattanPlot(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);

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
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
			e.printStackTrace();
		}

		return result;
	}

	public static boolean writeQQPlotFromAssociationData(int opId, String outName, int width, int height) throws IOException {
		boolean result = false;
		//Generating XY scatter plot with loaded data
		XYPlot qqPlot = GenericReportGenerator_opt.buildQQPlot(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR, 1);

		JFreeChart chart = new JFreeChart("X² QQ", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);

		OperationMetadata rdOPMetadata = new OperationMetadata(opId);
		String imagePath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/" + outName + ".png";
		try {
			ChartUtilities.saveChartAsPNG(new File(imagePath),
					chart,
					width,
					height);
			result = true;
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.");
			e.printStackTrace();
		}

		return result;
	}

	public static boolean createSortedAssociationReport(int opId, String reportName) throws IOException {
		boolean result;

		try {
			Map<String, Object> unsortedMarkerIdAssocValsLHM = GenericReportGenerator_opt.getAnalysisVarData(opId, org.gwaspi.constants.cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);
			Map<String, Object> unsortedMarkerIdPvalLHM = new LinkedHashMap<String, Object>();
			for (Iterator<String> it = unsortedMarkerIdAssocValsLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				double[] values = (double[]) unsortedMarkerIdAssocValsLHM.get(key);
				unsortedMarkerIdPvalLHM.put(key, values[1]);
			}

			Map<String, Object> sortingMarkerSetLHM = ReportManager.getSortedMarkerSetByDoubleValue(unsortedMarkerIdPvalLHM);
			if (unsortedMarkerIdPvalLHM != null) {
				unsortedMarkerIdPvalLHM.clear();
			}

			String sep = cExport.separator_REPORTS;
			OperationMetadata rdOPMetadata = new OperationMetadata(opId);
			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
			rdInfoMarkerSet.initFullMarkerIdSetLHM();

			//WRITE HEADER OF FILE
			String header = "MarkerID\trsID\tChr\tPosition\tMin. Allele\tMaj. Allele\tX²\tPval\tOR\n";
			reportName += ".txt";
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + rdOPMetadata.getStudyId() + "/";


			//WRITE MARKERSET RSID
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_RSID, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.writeFirstColumnToReport(reportPath, reportName, header, sortingMarkerSetLHM, true);


			//WRITE MARKERSET CHROMOSOME
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE MARKERSET POS
			//infoMatrixMarkerSetLHM = rdInfoMarkerSet.appendVariableToMarkerSetLHMValue(matrixNcFile, cNetCDF.Variables.VAR_MARKERS_POS, sep);
			rdInfoMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);


			//WRITE KNOWN ALLELES FROM QA
			//get MARKER_QA Operation
			ArrayList operationsAL = OperationManager.getMatrixOperations(rdOPMetadata.getParentMatrixId());
			int markersQAopId = Integer.MIN_VALUE;
			for (int i = 0; i < operationsAL.size(); i++) {
				Object[] element = (Object[]) operationsAL.get(i);
				if (element[1].toString().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
					markersQAopId = (Integer) element[0];
				}
			}
			if (markersQAopId != Integer.MIN_VALUE) {
				OperationMetadata qaMetadata = new OperationMetadata(markersQAopId);
				NetcdfFile qaNcFile = NetcdfFile.open(qaMetadata.getPathToMatrix());

				OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markersQAopId);
				Map<String, Object> opMarkerSetLHM = rdOperationSet.getOpSetLHM();

				//MINOR ALLELE
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES);
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = opMarkerSetLHM.get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele);
				}

				//MAJOR ALLELE
				rdOperationSet.fillLHMWithDefaultValue(opMarkerSetLHM, "");
				opMarkerSetLHM = rdOperationSet.fillOpSetLHMWithVariable(qaNcFile, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES);
				for (Iterator<String> it = rdInfoMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
					String key = it.next();
					Object minorAllele = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
					rdInfoMarkerSet.getMarkerIdSetLHM().put(key, minorAllele + sep + opMarkerSetLHM.get(key));
				}

			}
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = rdInfoMarkerSet.getMarkerIdSetLHM().get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, false, false);

			//WRITE HW HETZY ARRAY
			for (Iterator<String> it = sortingMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				Object value = unsortedMarkerIdAssocValsLHM.get(key);
				sortingMarkerSetLHM.put(key, value);
			}
			ReportWriter.appendColumnToReport(reportPath, reportName, sortingMarkerSetLHM, true, false);

			result = true;
		} catch (IOException iOException) {
			result = false;
		}

		return result;
	}
}
