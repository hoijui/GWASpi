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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.ExportConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Text;
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

	public static final String[] COLUMNS = new String[] {
			Text.Reports.familyId,
			Text.Reports.sampleId,
			Text.Reports.fatherId,
			Text.Reports.motherId,
			Text.Reports.sex,
			Text.Reports.affection,
			Text.Reports.age,
			Text.Reports.category,
			Text.Reports.disease,
			Text.Reports.population,
			Text.Reports.missRatio,
			Text.Reports.smplHetzyRat};

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

		operationPH.setNewStatus(ProcessStatus.INITIALIZING);
		final OperationMetadata qaSamplesOperation = OperationsList.getOperationMetadata(getParams().getSampleQAOpKey());

		org.gwaspi.global.Utils.createFolder(new File(Study.constructReportsPath(qaSamplesOperation.getStudyKey())));
		final String reportPath = Study.constructReportsPath(qaSamplesOperation.getStudyKey());
		final String prefix = ReportsList.getReportNamePrefix(qaSamplesOperation);

		creatingMissingnessTablePH.setNewStatus(ProcessStatus.INITIALIZING);
		final String sampleMissOutName = prefix + "samplmissing.txt";
		final File sampleMissOutFile = new File(reportPath, sampleMissOutName);
		operationPH.setNewStatus(ProcessStatus.RUNNING);
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
					qaSamplesOperation.getStudyKey()));
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
				qaSamplesOperation.getStudyKey()));
		org.gwaspi.global.Utils.sysoutCompleted("Sample Heterozygosity QA Report");
//			}
//		}
		creatingHetzyPlotPH.setNewStatus(ProcessStatus.COMPLEETED);
		operationPH.setNewStatus(ProcessStatus.COMPLEETED);

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

		final String sep = ExportConstants.SEPARATOR_REPORTS;

		final QASamplesOperationDataSet qaSamplesOperationDataSet = (QASamplesOperationDataSet) OperationManager.generateOperationDataSet(samplesQAopKey);
		final List<QASamplesOperationEntry> qaSamplesOperationEntries = new ArrayList<QASamplesOperationEntry>(qaSamplesOperationDataSet.getEntries());
		Collections.sort(qaSamplesOperationEntries, new MissingRatioComparator());

		final Map<SampleKey, SampleInfo> sampleKeyToInfo
				= org.gwaspi.netCDF.exporter.Utils.createSampleKeyToInfoMap(qaSamplesOperationDataSet.getSamplesInfosSource());

		// WRITE HEADER OF FILE
		final FileWriter reportFW = new FileWriter(sampleMissOutFile);
		final BufferedWriter reportBW = new BufferedWriter(reportFW);

		final String header = OutputQAMarkers.createReportHeaderLine(COLUMNS);
		reportBW.append(header);

		// GET SAMPLE INFO FROM DB
		for (final QASamplesOperationEntry entry : qaSamplesOperationEntries) {
			final SampleKey tempSampleKey = entry.getKey();
//			final SampleInfo sampleInfo = org.gwaspi.netCDF.exporter.Utils.getCurrentSampleFormattedInfo(tempSampleKey); // read from DB
			final SampleInfo sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleKeyToInfo.get(tempSampleKey)); // read from backend storage

			final String familyId = sampleInfo.getFamilyId();
			final String fatherId = sampleInfo.getFatherId();
			final String motherId = sampleInfo.getMotherId();
			final String sex = sampleInfo.getSexStr();
			final String affection = sampleInfo.getAffectionStr();
			final String category = sampleInfo.getCategory();
			final String desease = sampleInfo.getDisease();
			final String population = sampleInfo.getPopulation();
//			final String age = String.valueOf((sampleInfo.getAge() == 0) ? -1 : sampleInfo.getAge());
			final String age = String.valueOf(sampleInfo.getAge());

			final StringBuilder sb = new StringBuilder();
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
			reportBW.append(sb.toString());
		}

		reportBW.close();
		reportFW.close();
	}

	public static List<Object[]> parseQASamplesReport(
			final File reportFile,
			final int numRowsToFetch,
			final boolean exactValues)
			throws IOException
	{
		return ReportWriter.parseReport(
				reportFile,
				new QASamplesReportLineParser(exactValues),
				numRowsToFetch);
	}

	private static class QASamplesReportLineParser extends AbstractReportLineParser {

		public QASamplesReportLineParser(final boolean exactValues) {
			super(exactValues);
		}

		@Override
		public Object[] extract(final String[] cVals) {

			final Object[] row = new Object[COLUMNS.length];

			final String familyId = cVals[0];
			final String sampleId = cVals[1];
			final String fatherId = cVals[2];
			final String motherId = cVals[3];
			final String sex = cVals[4];
			final String affection = cVals[5];
			final String age = cVals[6];
			final String category = cVals[7];
			final String disease = cVals[8];
			final String population = cVals[9];
			final Double missRat = tryToParseDouble(cVals[10]);
			final Double hetzyRat = (cVals.length > 11) ? tryToParseDouble(cVals[11]) : Double.NaN;

			row[0] = familyId;
			row[1] = sampleId;
			row[2] = fatherId;
			row[3] = motherId;
			row[4] = sex;
			row[5] = affection;
			row[6] = age;
			row[7] = category;
			row[8] = disease;
			row[9] = population;
			row[10] = missRat;
			row[11] = hetzyRat;

			return row;
		}
	}
}
