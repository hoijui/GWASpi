/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.operations.combi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.ReportService;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.trendtest.TrendTestOperationEntry;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.AbstractOutputOperation;
import org.gwaspi.reports.ReportWriterRowWise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class comment
 * - n / nSamples : #samples == #data-points in the SVM feature space == rows & columns in the SVM kernel matrix
 * - dSamples : #markers == #SNPs
 * - dEncoded : #markers * encodingFactor == #dimensions in the SVM  feature space
 */
public class CombiOutputOperation extends AbstractOutputOperation<CombiOutputOperationParams>
{
	private static final Logger LOG = LoggerFactory.getLogger(CombiOutputOperation.class);

	private static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			"Write COMBI output to files", ""); // TODO

	static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"COMBI reports",
					"COMBI reports", // TODO We need a more elaborate description of this operation!
					null,
					false,
					false);

	private ProgressHandler operationPH;
	private ProgressHandler prepareDataPH;
	private ProgressHandler findTowersPH;
	private ProgressHandler writeToFilesPH;

	public CombiOutputOperation(final CombiOutputOperationParams params) {
		super(params);
	}

	private OperationService getOperationService() {
		return OperationsList.getOperationService();
	}

	private ReportService getReportService() {
		return ReportsList.getReportService();
	}

	public static List<String> createColumnHeaders() {

		final List<String> columns = new ArrayList<String>(4);
		columns.add(Text.Reports.chr);
		columns.add(Text.Reports.rsId);
		columns.add(Text.Reports.pVal);
		columns.add(Text.Reports.svmWeight);

		return columns;
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		// XXX For this class, we should use a different return type on this method (also for the other Output* classes)
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {
		return getProgressHandler();
	}

	private ProgressHandler getProgressHandler() throws IOException {

		if (operationPH == null) {
			final ProcessInfo prepareDataPI = new DefaultProcessInfo(
					"preparing the data for towers extraction", null);
			prepareDataPH = new IndeterminateProgressHandler(prepareDataPI);

			final ProcessInfo findTowersPI = new DefaultProcessInfo(
					"finding towers in the COMBI-filtered P-Values", null);
			findTowersPH = new IndeterminateProgressHandler(findTowersPI);

			final ProcessInfo writeToFilesPI = new DefaultProcessInfo(
					"writing the significant & insignificant markers results", null);
			writeToFilesPH = new IndeterminateProgressHandler(writeToFilesPI);

			final Map<ProgressSource, Double> subProgressSourcesAndWeights
					= new LinkedHashMap<ProgressSource, Double>();

			subProgressSourcesAndWeights.put(prepareDataPH, 0.3); // TODO adjust these weights!
			subProgressSourcesAndWeights.put(findTowersPH, 0.6);
			subProgressSourcesAndWeights.put(writeToFilesPH, 0.1);

			operationPH = new SuperProgressSource(PROCESS_INFO, subProgressSourcesAndWeights);
		}

		return operationPH;
	}

	private static <I, O> List<O> extract(final List<? extends I> input, final Extractor<I, O> extractor) {

		final List<O> output = new ArrayList<O>(input.size());
		for (final I inputEntry : input) {
			output.add(extractor.extract(inputEntry));
		}

		return output;
	}

	private static Double invertPValue(final Double pValue) {

		return (1.0 - pValue);
	}

	private static void invertPValues(final List<Double> pValues) {

		for (int pvi = 0; pvi < pValues.size(); pvi++) {
			pValues.set(pvi, invertPValue(pValues.get(pvi)));
		}
	}

	private static <K> void invertPValues(final Map<K, Double> pValues) {

		for (final Map.Entry<K, Double> keyPValue : pValues.entrySet()) {
			keyPValue.setValue(invertPValue(keyPValue.getValue()));
		}
	}

	@Override
	public Object call() throws IOException {

		final ProgressHandler progressHandler = getProgressHandler();
		progressHandler.setNewStatus(ProcessStatus.INITIALIZING);
		final OperationKey parentOpKey = getParams().getParent().getOperationParent();
		final OperationMetadata parentOpMeta = getOperationService().getOperationMetadata(parentOpKey);
		final String testName = parentOpMeta.getName();
		final StudyKey studyKey = getParams().getParent().getOrigin().getStudyKey();

		final File reportsPath = new File(Study.constructReportsPath(studyKey));
		Utils.createFolder(reportsPath);
		final String prefix = getReportService().getReportNamePrefix(parentOpMeta);
		final String significantFileName = prefix + ".significant.txt";
		final String insignificantFileName = prefix + ".insignificant.txt";

		progressHandler.setNewStatus(ProcessStatus.RUNNING);
		prepareDataPH.setNewStatus(ProcessStatus.RUNNING);
		final OperationDataSet<? extends TrendTestOperationEntry> testOperationDataSet
				= OperationManager.generateOperationDataSet(parentOpKey);
		final OperationDataSet<CombiTestOperationEntry> combiOperationDataSet
				= OperationManager.generateOperationDataSet(getParams().getCombiOperationKey());
		final List<? extends TrendTestOperationEntry> testEntries
				= testOperationDataSet.getEntries();
		final Iterator<MarkerMetadata> markerInfos = testOperationDataSet.getMarkersMetadatasSource().iterator();
		final Map<ChromosomeKey, List<TrendTestOperationEntry>> chromToTestEntries
				= new LinkedHashMap<ChromosomeKey, List<TrendTestOperationEntry>>();
		final Map<ChromosomeKey, List<Integer>> chromToPositions
				= new LinkedHashMap<ChromosomeKey, List<Integer>>();
		for (final ChromosomeKey chromosome : testOperationDataSet.getChromosomesKeysSource()) {
			chromToTestEntries.put(chromosome, new ArrayList<TrendTestOperationEntry>());
			chromToPositions.put(chromosome, new ArrayList<Integer>());
		}
		for (final TrendTestOperationEntry testEntry : testEntries) {
			final MarkerMetadata markerMetadata = markerInfos.next();
			final ChromosomeKey chr = ChromosomeKey.valueOf(markerMetadata.getChr());
			chromToTestEntries.get(chr).add(testEntry);
			chromToPositions.get(chr).add(markerMetadata.getPos());
		}
		prepareDataPH.setNewStatus(ProcessStatus.COMPLEETED);

		findTowersPH.setNewStatus(ProcessStatus.RUNNING);
		final Map<ChromosomeKey, Map<Integer, Double>> chromToPeaks
				= new LinkedHashMap<ChromosomeKey, Map<Integer, Double>>();
		final List<Double> pValueThreasholds = getParams().getPValueThreasholds();
		int ci = 0;
		for (final ChromosomeKey chromosome : testOperationDataSet.getChromosomesKeysSource()) {
			final Double pValueThreashold;
			if (pValueThreasholds.size() > 1) {
				pValueThreashold = pValueThreasholds.get(ci);
			} else if (pValueThreasholds.isEmpty()) {
				pValueThreashold = CombiOutputOperationParams.getPValueThreasholdsDefault().get(0);
			} else {
				pValueThreashold = pValueThreasholds.get(0);
			}
//			final List<Integer> origIndices = extract(chromTestEntries.getValue(), OperationDataEntry.TO_INDEX);
			final List<Integer> origPositions = chromToPositions.get(chromosome);
			final List<Double> pValues = extract(chromToTestEntries.get(chromosome), TrendTestOperationEntry.TO_P);
			final Map<Integer, Double> indexedPeaksDescending;
			if (origPositions.size() < 3) {
				// add all
				indexedPeaksDescending = new LinkedHashMap<Integer, Double>();
				for (int pvi = 0; pvi < pValues.size(); pvi++) {
//					indexedPeaksDescending.put(origPositions.get(pvi), pValues.get(pvi));
					if (pValues.get(pvi) <= pValueThreashold) {
						indexedPeaksDescending.put(pvi, pValues.get(pvi));
					}
				}
			} else {
				// find peaks
				// invert P-Values, because peak-finder looks for max values, and we want min-peaks
				invertPValues(pValues);
				final Double invertedPValueThreashold = invertPValue(pValueThreashold);
				final FindPeaks peakFinder = new FindPeaks(
						origPositions,
						pValues,
						invertedPValueThreashold,
						getParams().getMinPeakDistance(),
						1,
						false);
				indexedPeaksDescending = peakFinder.findPeaks();
				invertPValues(indexedPeaksDescending);
			}
			chromToPeaks.put(chromosome, indexedPeaksDescending);
			ci++;
		}
		findTowersPH.setNewStatus(ProcessStatus.COMPLEETED);

		writeToFilesPH.setNewStatus(ProcessStatus.INITIALIZING);
		final List<String> towersFileColumnHeaders = new ArrayList<String>(4);
		towersFileColumnHeaders.add("Chromosome");
		towersFileColumnHeaders.add("RS-ID");
		towersFileColumnHeaders.add("P-Value");
		towersFileColumnHeaders.add("SVM-Weight");
		final List<Extractor<?, String>> significantFileColumnExtractors
				= new ArrayList<Extractor<?, String>>(4);
		significantFileColumnExtractors.add(new Extractor.ToStringExtractor<String>());
		significantFileColumnExtractors.add(new Extractor.ToStringExtractor<String>());
		significantFileColumnExtractors.add(new Extractor.ToStringExtractor<Double>());
		significantFileColumnExtractors.add(new Extractor.ToStringExtractor<Double>());
		final File significantFile = new File(reportsPath, significantFileName);
		final File insignificantFile = new File(reportsPath, insignificantFileName);
		final ReportWriterRowWise significantRW = new ReportWriterRowWise(significantFile, significantFileColumnExtractors);
		final ReportWriterRowWise insignificantRW = new ReportWriterRowWise(insignificantFile, significantFileColumnExtractors);
		writeToFilesPH.setNewStatus(ProcessStatus.RUNNING);
		significantRW.writeHeader(towersFileColumnHeaders);
		insignificantRW.writeHeader(towersFileColumnHeaders);
		final List<MarkerKey> testMarkerKeys = new ArrayList<MarkerKey>(testOperationDataSet.getMarkersKeysSource());
		final Map<MarkerKey, Double> markerKeyToSvmWeight = new HashMap<MarkerKey, Double>();
		for (final CombiTestOperationEntry combiEntry : combiOperationDataSet.getEntries()) {
			markerKeyToSvmWeight.put(combiEntry.getKey(), combiEntry.getWeight());
		}
		for (final Map.Entry<ChromosomeKey, Map<Integer, Double>> chromPeaks : chromToPeaks.entrySet()) {
			final ChromosomeKey chrom = chromPeaks.getKey();
			final String chromStr = chrom.getChromosome();
			final Iterator<Map.Entry<Integer, Double>> peaks = chromPeaks.getValue().entrySet().iterator();
			if (peaks.hasNext()) {
				final Map.Entry<Integer, Double> peakSignificant = peaks.next();
				final MarkerKey markerKeySignificant = testMarkerKeys.get(peakSignificant.getKey());
				significantRW.appendEntry(chromStr, markerKeySignificant.getMarkerId(), peakSignificant.getValue(), markerKeyToSvmWeight.get(markerKeySignificant));
				while (peaks.hasNext()) {
					final Map.Entry<Integer, Double> peakInsignificant = peaks.next();
					final MarkerKey markerKey = testMarkerKeys.get(peakInsignificant.getKey());
					insignificantRW.appendEntry(chromStr, markerKey.getMarkerId(), peakInsignificant.getValue(), markerKeyToSvmWeight.get(markerKey));
				}
			}
		}
		writeToFilesPH.setNewStatus(ProcessStatus.FINALIZING);
		significantRW.close();
		insignificantRW.close();
		writeToFilesPH.setNewStatus(ProcessStatus.COMPLEETED);
		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);

		return null;
	}
}
