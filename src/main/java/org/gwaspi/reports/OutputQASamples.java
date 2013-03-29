package org.gwaspi.reports;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OutputQASamples {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQASamples.class);

	private static String reportPath;
	private static String samplMissOutName;

	private OutputQASamples() {
	}

	public static boolean writeReportsForQASamplesData(int opId, boolean newReport) throws IOException {
		Operation op = OperationsList.getById(opId);

		org.gwaspi.global.Utils.createFolder(Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, ""), "STUDY_" + op.getStudyId());
		reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/" + "STUDY_" + op.getStudyId() + "/";

		String prefix = ReportsList.getReportNamePrefix(op);
		samplMissOutName = prefix + "samplmissing.txt";


		if (createSortedSampleMissingnessReport(opId, samplMissOutName, op.getStudyId())) {
			if (newReport) {
				ReportsList.insertRPMetadata(new Report(
						Integer.MIN_VALUE,
						"Sample Missingness Table",
						samplMissOutName,
						OPType.SAMPLE_QA,
						op.getParentMatrixId(),
						opId,
						"Sample Missingness Table",
						op.getStudyId()));

				org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
			}
		}

		samplMissOutName = prefix + "hetzyg-missing";
//		if(createSampleHetzygPlot(opId, samplMissOutName, 500, 500)){
//			if(newReport){
		ReportsList.insertRPMetadata(new Report(
				Integer.MIN_VALUE,
				"Sample Heterozygosity vs Missingness Plot",
				samplMissOutName,
				OPType.SAMPLE_HTZYPLOT,
				op.getParentMatrixId(),
				opId,
				"Sample Heterozygosity vs Missingness Plot",
				op.getStudyId()));

		org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
//            }
//        }


		return true;
	}

	public static boolean createSortedSampleMissingnessReport(int opId, String reportName, Integer poolId) throws IOException {
		boolean result;
		String sep = cExport.separator_REPORTS;

		try {
			Map<SampleKey, Object> unsortedSamplesMissingRatMap = GatherQASamplesData.loadSamplesQAMissingRatio(opId);
			Map<SampleKey, Object> sortedSamplesMissingRatMap = ReportsList.getSortedDescendingMarkerSetByDoubleValue(unsortedSamplesMissingRatMap);
			if (unsortedSamplesMissingRatMap != null) {
				unsortedSamplesMissingRatMap.clear();
			}

			Map<SampleKey, Object> samplesMissingRatMap = GatherQASamplesData.loadSamplesQAHetZygRatio(opId);

			//WRITE HEADER OF FILE
			FileWriter tempFW = new FileWriter(reportPath + samplMissOutName);
			BufferedWriter tempBW = new BufferedWriter(tempFW);

			String header = "FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tAge\tCategory\tDisease\tPopulation\tMissing Ratio\n";
			tempBW.append(header);


			//GET SAMPLE INFO FROM DB
			for (Map.Entry<SampleKey, Object> entry : sortedSamplesMissingRatMap.entrySet()) {
				SampleKey tempSampleKey = entry.getKey();
				SampleInfo sampleInfo = org.gwaspi.netCDF.exporter.Utils.getCurrentSampleFormattedInfo(tempSampleKey, poolId);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();
				String category = sampleInfo.getCategory();
				String desease = sampleInfo.getDisease();
				String population = sampleInfo.getPopulation();
				String age = String.valueOf((sampleInfo.getAge() == 0) ? -1 : sampleInfo.getAge());

				StringBuilder sb = new StringBuilder();
				sb.append(familyId);
				sb.append(sep);
				sb.append(tempSampleKey.getSampleId());
				sb.append(sep);
				sb.append(fatherId);
				sb.append(sep);
				sb.append(motherId);
				sb.append(sep);
				sb.append(sex);
				sb.append(sep);
				sb.append(affection);
				sb.append(sep);
				sb.append(age);
				sb.append(sep);
				sb.append(category);
				sb.append(sep);
				sb.append(desease);
				sb.append(sep);
				sb.append(population);
				sb.append(sep);
				sb.append(entry.getValue().toString());
				sb.append(sep);
				sb.append(samplesMissingRatMap.get(tempSampleKey).toString());
				sb.append("\n");
				tempBW.append(sb.toString());
			}

			tempBW.close();
			tempFW.close();

			result = true;
		} catch (IOException ex) {
			result = false;
			log.warn(null, ex);
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
