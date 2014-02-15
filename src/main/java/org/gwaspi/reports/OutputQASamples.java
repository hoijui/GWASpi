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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportKey;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputQASamples {

	private static final Logger log
			= LoggerFactory.getLogger(OutputQASamples.class);

	private static String reportPath;
	private static String sampleMissOutName;

	private OutputQASamples() {
	}

	public static boolean writeReportsForQASamplesData(OperationKey sampleQAOpKey, boolean newReport) throws IOException {
		OperationMetadata op = OperationsList.getOperationMetadata(sampleQAOpKey);

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		reportPath = Study.constructReportsPath(op.getStudyKey());

		String prefix = ReportsList.getReportNamePrefix(op);
		sampleMissOutName = prefix + "samplmissing.txt";

		createSortedSampleMissingnessReport(sampleQAOpKey, sampleMissOutName, op.getStudyKey());

		if (newReport) {
			ReportsList.insertRPMetadata(new Report(
					"Sample Missingness Table",
					sampleMissOutName,
					OPType.SAMPLE_QA,
					sampleQAOpKey,
					"Sample Missingness Table",
					op.getStudyKey()));

			org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
		}

		sampleMissOutName = prefix + "hetzyg-missing";
//		if (createSampleHetzygPlot(opId, samplMissOutName, 500, 500)) {
//			if (newReport) {
		ReportsList.insertRPMetadata(new Report(
				"Sample Heterozygosity vs Missingness Plot",
				sampleMissOutName,
				OPType.SAMPLE_HTZYPLOT,
				sampleQAOpKey,
				"Sample Heterozygosity vs Missingness Plot",
				op.getStudyKey()));

		org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
//			}
//		}


		return true;
	}

	private static class MissingRatioComparator implements Comparator<QASamplesOperationEntry> {

		@Override
		public int compare(QASamplesOperationEntry entry1, QASamplesOperationEntry entry2) {
			return (int) Math.signum(entry1.getMissingRatio() - entry2.getMissingRatio());
		}
	}

	public static void createSortedSampleMissingnessReport(OperationKey samplesQAopKey, String reportName, StudyKey studyKey) throws IOException {

		String sep = cExport.separator_REPORTS;

		QASamplesOperationDataSet qaSamplesOperationDataSet = (QASamplesOperationDataSet) OperationFactory.generateOperationDataSet(samplesQAopKey);
		List<QASamplesOperationEntry> qaSamplesOperationEntries = (List<QASamplesOperationEntry>) qaSamplesOperationDataSet.getEntries(); // HACK This might not be a List!
		Collections.sort(qaSamplesOperationEntries, new MissingRatioComparator());

		// WRITE HEADER OF FILE
		FileWriter tempFW = new FileWriter(reportPath + sampleMissOutName);
		BufferedWriter tempBW = new BufferedWriter(tempFW);

		String header = "FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tAge\tCategory\tDisease\tPopulation\tMissing Ratio\n";
		tempBW.append(header);

		// GET SAMPLE INFO FROM DB
		for (QASamplesOperationEntry entry : qaSamplesOperationEntries) {
			SampleKey tempSampleKey = entry.getKey();
			SampleInfo sampleInfo = org.gwaspi.netCDF.exporter.Utils.getCurrentSampleFormattedInfo(tempSampleKey);

			String familyId = sampleInfo.getFamilyId();
			String fatherId = sampleInfo.getFatherId();
			String motherId = sampleInfo.getMotherId();
			String sex = sampleInfo.getSexStr();
			String affection = sampleInfo.getAffectionStr();
			String category = sampleInfo.getCategory();
			String desease = sampleInfo.getDisease();
			String population = sampleInfo.getPopulation();
//			String age = String.valueOf((sampleInfo.getAge() == 0) ? -1 : sampleInfo.getAge());
			String age = String.valueOf(sampleInfo.getAge());

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
			sb.append(entry.getMissingRatio());
			sb.append(sep);
			sb.append(entry.getHetzyRatio());
			sb.append("\n");
			tempBW.append(sb.toString());
		}

		tempBW.close();
		tempFW.close();
	}
//	public static boolean createSampleHetzygPlot(int opId, String outName, int width, int height) throws IOException {
//		boolean result = false;
//		//Generating XY scatter plot with loaded data
//		XYDataset hetZygDataset = GenericReportGenerator_opt.getSampleHetzygDataset(opId);
//
//		JFreeChart chart = new JFreeChart("Sample Heterozygosity vs. Missingness", JFreeChart.DEFAULT_TITLE_FONT, qqPlot, true);
//
//		OperationMetadata rdOPMetadata = new OperationMetadata(opId);
//		String imagePath = Study.constructReportsPath(rdOPMetadata.getStudyKey()) + outName + ".png";
//		try {
//			ChartUtilities.saveChartAsPNG(new File(imagePath),
//										   chart,
//										   width,
//										   height);
//			result = true;
//		} catch (IOException ex) {
//			log.error("Problem occurred creating chart", ex);
//		}
//
//		return result;
//	}
}
