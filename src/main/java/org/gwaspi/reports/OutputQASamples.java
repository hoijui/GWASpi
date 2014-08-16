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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationEntry;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SuperProgressSource;

/**
 * Write reports for QA Samples data.
 */
public class OutputQASamples extends AbstractOutputOperation<QASamplesOutputParams> {

	private static final ProcessInfo qaSamplesOutputProcessInfo = new DefaultProcessInfo("Write QA Samples output to files", ""); // TODO

	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Output QA Samples data",
					"Output QA Samples data", // TODO We need a more elaborate description of this operation!
					null,
					false,
					false);

	private ProgressHandler operationPH;
	private ProgressHandler creatingMissingnessTablePH;
	private ProgressHandler creatingHetzyPlotPH;

	public OutputQASamples(QASamplesOutputParams params) {
		super(params);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		// XXX For this class, we should use a different return type on this method (ialso for the othe Output* classes)
		return OPERATION_TYPE_INFO;
	}

	@Override
	public int processMatrix() throws IOException {

		OperationMetadata op = OperationsList.getOperationMetadata(getParams().getSampleQAOpKey());

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(op.getStudyKey())));
		final String reportPath = Study.constructReportsPath(op.getStudyKey());
		final String prefix = ReportsList.getReportNamePrefix(op);

		creatingMissingnessTablePH.setNewStatus(ProcessStatus.INITIALIZING);
		final String sampleMissOutName = prefix + "samplmissing.txt";
		final File sampleMissOutFile = new File(reportPath, sampleMissOutName);
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.RUNNING);
		createSortedSampleMissingnessReport(getParams().getSampleQAOpKey(), sampleMissOutFile);
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.FINALIZING);
		if (getParams().isNewReport()) {
			ReportsList.insertRPMetadata(new Report(
					"Sample Missingness Table",
					sampleMissOutName,
					OPType.SAMPLE_QA,
					getParams().getSampleQAOpKey(),
					"Sample Missingness Table",
					op.getStudyKey()));
			org.gwaspi.global.Utils.sysoutCompleted("Sample Missingness QA Report");
		}
		creatingMissingnessTablePH.setNewStatus(ProcessStatus.COMPLEETED);

		creatingHetzyPlotPH.setNewStatus(ProcessStatus.INITIALIZING);
		final String hetzyMissOutName = prefix + "hetzyg-missing";
		creatingHetzyPlotPH.setNewStatus(ProcessStatus.RUNNING);
		// XXX Why is hetzy plot not created here?
//		if (createSampleHetzygPlot(opId, samplMissOutName, 500, 500)) {
//			if (params.isNewReport()) {
		creatingHetzyPlotPH.setNewStatus(ProcessStatus.FINALIZING);
		ReportsList.insertRPMetadata(new Report(
				"Sample Heterozygosity vs Missingness Plot",
				hetzyMissOutName,
				OPType.SAMPLE_HTZYPLOT,
				getParams().getSampleQAOpKey(),
				"Sample Heterozygosity vs Missingness Plot",
				op.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted("Sample Heterozygosity QA Report");
//			}
//		}
		creatingHetzyPlotPH.setNewStatus(ProcessStatus.COMPLEETED);

		return Integer.MIN_VALUE;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return qaSamplesOutputProcessInfo;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		if (operationPH == null) {
			final ProcessInfo creatingMissingnessTablePI = new DefaultProcessInfo(
					"writing the QA Samples missingness table to a text file", null);
			creatingMissingnessTablePH
					= new IndeterminateProgressHandler(creatingMissingnessTablePI);

			final ProcessInfo creatingHetzyPlotPI = new DefaultProcessInfo(
					"creating & writing the QA Samples Hetzy plot", null);
			creatingHetzyPlotPH
					= new IndeterminateProgressHandler(creatingHetzyPlotPI);

			Map<ProgressSource, Double> subProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>();

			subProgressSourcesAndWeights.put(creatingMissingnessTablePH, 0.2); // TODO adjust these weights!
			subProgressSourcesAndWeights.put(creatingHetzyPlotPH, 0.8);

			operationPH = new SuperProgressSource(qaSamplesOutputProcessInfo, subProgressSourcesAndWeights);
		}

		return operationPH;
	}

	private static class MissingRatioComparator implements Comparator<QASamplesOperationEntry> {

		@Override
		public int compare(QASamplesOperationEntry entry1, QASamplesOperationEntry entry2) {
			return Double.compare(entry1.getMissingRatio(), entry2.getMissingRatio());
		}
	}

	private static void createSortedSampleMissingnessReport(OperationKey samplesQAopKey, final File sampleMissOutFile) throws IOException {

		String sep = cExport.separator_REPORTS;

		QASamplesOperationDataSet qaSamplesOperationDataSet = (QASamplesOperationDataSet) OperationManager.generateOperationDataSet(samplesQAopKey);
		List<QASamplesOperationEntry> qaSamplesOperationEntries = (List<QASamplesOperationEntry>) qaSamplesOperationDataSet.getEntries(); // HACK This might not be a List!
		Collections.sort(qaSamplesOperationEntries, new MissingRatioComparator());

		// WRITE HEADER OF FILE
		FileWriter tempFW = new FileWriter(sampleMissOutFile);
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
