package org.gwaspi.reports;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.Config;
import org.gwaspi.global.ServiceLocator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.Operation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputQASamples {

	protected static String reportPath;
	protected static String samplMissOutName;

	private OutputQASamples() {
	}

	public static boolean writeReportsForQASamplesData(int opId, boolean newReport) throws IOException {
		Operation op = new Operation(opId);
		DbManager dBManager = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());
		reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/" + "STUDY_" + op.getStudyId() + "/";

		String prefix = org.gwaspi.reports.ReportManager.getreportNamePrefix(op);
		samplMissOutName = prefix + "samplmissing.txt";


		if (createSortedSampleMissingnessReport(opId, samplMissOutName, op.getStudyId())) {
			if (newReport) {
				ReportManager.insertRPMetadata(dBManager,
						"Sample Missingness Table",
						samplMissOutName,
						cNetCDF.Defaults.OPType.SAMPLE_QA.toString(),
						op.getParentMatrixId(),
						opId,
						"Sample Missingness Table",
						op.getStudyId());

				org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
			}
		}

		samplMissOutName = prefix + "hetzyg-missing";
//        if(createSampleHetzygPlot(opId, samplMissOutName, 500, 500)){
//            if(newReport){
		ReportManager.insertRPMetadata(dBManager,
				"Sample Heterozygosity vs Missingness Plot",
				samplMissOutName,
				cNetCDF.Defaults.OPType.SAMPLE_HTZYPLOT.toString(),
				op.getParentMatrixId(),
				opId,
				"Sample Heterozygosity vs Missingness Plot",
				op.getStudyId());

		org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
//            }
//        }


		return true;
	}

	public static boolean createSortedSampleMissingnessReport(int opId, String reportName, Object poolId) throws IOException {
		boolean result;
		String sep = cExport.separator_REPORTS;

		try {
			Map<String, Object> unsortedSamplesMissingRatLHM = GatherQASamplesData.loadSamplesQAMissingRatio(opId);
			Map<String, Object> sortedSamplesMissingRatLHM = ReportManager.getSortedDescendingMarkerSetByDoubleValue(unsortedSamplesMissingRatLHM);
			if (unsortedSamplesMissingRatLHM != null) {
				unsortedSamplesMissingRatLHM.clear();
			}

			Map<String, Object> samplesMissingRatLHM = GatherQASamplesData.loadSamplesQAHetZygRatio(opId);

			//WRITE HEADER OF FILE
			FileWriter tempFW = new FileWriter(reportPath + samplMissOutName);
			BufferedWriter tempBW = new BufferedWriter(tempFW);

			String header = "FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tAge\tCategory\tDisease\tPopulation\tMissing Ratio\n";
			tempBW.append(header);


			//GET SAMPLE INFO FROM DB
			for (Map.Entry<String, Object> entry : sortedSamplesMissingRatLHM.entrySet()) {
				String tempSampleId = entry.getKey();
				Map<String, Object> sampleInfo = org.gwaspi.netCDF.exporter.Utils.getCurrentSampleFormattedInfo(tempSampleId, poolId);

				String tmpFamId = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString() : "0";
				String tmpSex = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString() : "0";
				String tmpAffection = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString() : "0";
				String tmpAge = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AGE) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AGE).toString() : "-1";
				String tmpCategory = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_CATEGORY) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_CATEGORY).toString() : "0";
				String tmpDisease = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_DISEASE) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_DISEASE).toString() : "0";
				String tmpPopulation = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_POPULATION) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_POPULATION).toString() : "0";
				String tmpFatherId = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString() : "0";
				String tmpMotherId = (sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID) != null) ? sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString() : "0";

				StringBuilder sb = new StringBuilder();
				sb.append(tmpFamId);
				sb.append(sep);
				sb.append(tempSampleId);
				sb.append(sep);
				sb.append(tmpFatherId);
				sb.append(sep);
				sb.append(tmpMotherId);
				sb.append(sep);
				sb.append(tmpSex);
				sb.append(sep);
				sb.append(tmpAffection);
				sb.append(sep);
				sb.append(tmpAge);
				sb.append(sep);
				sb.append(tmpCategory);
				sb.append(sep);
				sb.append(tmpDisease);
				sb.append(sep);
				sb.append(tmpPopulation);
				sb.append(sep);
				sb.append(entry.getValue().toString());
				sb.append(sep);
				sb.append(samplesMissingRatLHM.get(tempSampleId).toString());
				sb.append("\n");
				tempBW.append(sb.toString());

			}

			tempBW.close();
			tempFW.close();

			result = true;
		} catch (IOException ex) {
			result = false;
		}

		return result;
	}
//    public static boolean createSampleHetzygPlot(int opId, String outName, int width, int height) throws IOException {
//        boolean result = false;
//        //Generating XY scatter plot with loaded data
//        XYDataset hetZygDataset = GenericReportGenerator_opt.getSampleHetzygDataset(opId);
//
//        JFreeChart chart = new JFreeChart("Sample Heterozygosity vs. Missingness", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);
//
//        OperationMetadata rdOPMetadata = new OperationMetadata(opId);
//        String imagePath=Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_"+rdOPMetadata.getStudyId()+"/"+outName+".png";
//        try {
//            ChartUtilities.saveChartAsPNG(new File(imagePath),
//                                           chart,
//                                           width,
//                                           height);
//            result = true;
//        } catch (IOException ex) {
//            log.error("Problem occurred creating chart", ex);
//        }
//
//        return result;
//    }
}
