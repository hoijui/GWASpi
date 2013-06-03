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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputQASamples {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQASamples.class);

	private static String reportPath;
	private static String samplMissOutName;

	private OutputQASamples() {
	}

	public static boolean writeReportsForQASamplesData(int opId, boolean newReport) throws IOException {
		OperationMetadata op = OperationsList.getById(opId);

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		reportPath = Study.constructReportsPath(op.getStudyKey());

		String prefix = ReportsList.getReportNamePrefix(op);
		samplMissOutName = prefix + "samplmissing.txt";


		if (createSortedSampleMissingnessReport(opId, samplMissOutName, op.getStudyKey())
				&& newReport)
		{
			ReportsList.insertRPMetadata(new Report(
					Integer.MIN_VALUE,
					"Sample Missingness Table",
					samplMissOutName,
					OPType.SAMPLE_QA,
					op.getParentMatrixId(),
					opId,
					"Sample Missingness Table",
					op.getStudyKey()));

			org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
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
				op.getStudyKey()));

		org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
//            }
//        }


		return true;
	}

	public static boolean createSortedSampleMissingnessReport(int opId, String reportName, StudyKey studyKey) throws IOException {
		boolean result;
		String sep = cExport.separator_REPORTS;

		try {
			Map<SampleKey, Double> unsortedSamplesMissingRatMap = GatherQASamplesData.loadSamplesQAMissingRatio(opId);
			Map<SampleKey, Double> sortedSamplesMissingRatMap = org.gwaspi.global.Utils.createMapSortedByValueDescending(unsortedSamplesMissingRatMap);
			if (unsortedSamplesMissingRatMap != null) {
				unsortedSamplesMissingRatMap.clear();
			}

			Map<SampleKey, Double> samplesMissingRatMap = GatherQASamplesData.loadSamplesQAHetZygRatio(opId);

			//WRITE HEADER OF FILE
			FileWriter tempFW = new FileWriter(reportPath + samplMissOutName);
			BufferedWriter tempBW = new BufferedWriter(tempFW);

			String header = "FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tAge\tCategory\tDisease\tPopulation\tMissing Ratio\n";
			tempBW.append(header);

			// GET SAMPLE INFO FROM DB
			for (Map.Entry<SampleKey, Double> entry : sortedSamplesMissingRatMap.entrySet()) {
				SampleKey tempSampleKey = entry.getKey();
				SampleInfo sampleInfo = org.gwaspi.netCDF.exporter.Utils.getCurrentSampleFormattedInfo(tempSampleKey, studyKey);

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
//        String imagePath = Study.constructReportsPath(rdOPMetadata.getStudyKey()) + outName + ".png";
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
