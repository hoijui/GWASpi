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

package org.gwaspi.threadbox;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.netCDF.loader.NullDataSetDestination;
import org.gwaspi.netCDF.loader.SampleInfoExtractorDataSetDestination;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperation;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperation;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_GTFreq_HW extends CommonRunnable {

	private static final ProcessInfo mcAndHwProcessInfo
			= new DefaultProcessInfo("Marker Census followed by Hardy & Weinberg",
					"Marker Census followed by Hardy & Weinberg"); // TODO
	private static final ProcessInfo mcPreparationProcessInfo
			= new DefaultProcessInfo("prepare for Marker Census",
					"prepare for Marker Census"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_PREPARE_MARKER_CENSUS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_PREPARE_MARKER_CENSUS", null));
	private static final ProgressSource PLACEHOLDER_PS_MARKER_CENSUS = new NullProgressHandler(
			new SubProcessInfo(MarkerCensusOperation.PROCESS_INFO, "PLACEHOLDER_PS_MARKER_CENSUS", null));
	private static final ProgressSource PLACEHOLDER_PS_HARDY_WEINBERG = new NullProgressHandler(
			new SubProcessInfo(HardyWeinbergOperation.PROCESS_INFO, "PLACEHOLDER_PS_HARDY_WEINBERG", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_PREPARE_MARKER_CENSUS, 0.1);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MARKER_CENSUS, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_HARDY_WEINBERG, 0.7);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final GWASinOneGOParams gwasParams;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;
	private OperationKey markerCensusOperationKey;
	private OperationKey hardyWeinbergOperationKey;

	public Threaded_GTFreq_HW(GWASinOneGOParams gwasParams) {
		super(
				"Genotype Frequency count & Hardy-Weinberg test",
				"on " + gwasParams.getMarkerCensusOperationParams().getParent().toString());

		this.gwasParams = gwasParams;
		this.progressSource = new SuperProgressSource(mcAndHwProcessInfo, subProgressSourcesAndWeights);
		final DataSetKey parentKey = gwasParams.getMarkerCensusOperationParams().getParent();
		this.taskLockProperties = MultiOperations.createTaskLockProperties(parentKey);
		this.markerCensusOperationKey = null;
		this.hardyWeinbergOperationKey = null;
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected ProgressHandler getProgressHandler() {
		return progressSource;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return taskLockProperties;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GTFreq_HW.class);
	}

	private OperationKey checkPerformMarkerCensus(Logger log, GWASinOneGOParams gwasParams) throws IOException {

		OperationKey censusOpKey = null;

		final ProgressHandler prepareForMcPh = new IndeterminateProgressHandler(mcPreparationProcessInfo);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_PREPARE_MARKER_CENSUS, prepareForMcPh, null);
		prepareForMcPh.setNewStatus(ProcessStatus.INITIALIZING);
		final MarkerCensusOperationParams markerCensusOperationParams = gwasParams.getMarkerCensusOperationParams();

		final OperationKey sampleQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(markerCensusOperationParams.getParent(), OPType.SAMPLE_QA).get(0));
		final OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(markerCensusOperationParams.getParent(), OPType.MARKER_QA).get(0));

		markerCensusOperationParams.setSampleQAOpKey(sampleQAOpKey);
		markerCensusOperationParams.setMarkerQAOpKey(markersQAOpKey);

		// NOTE ABORTION_POINT We could be gracefully abort here

		//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
		// GENOTYPE FREQ. BY PHENOFILE OR DB AFFECTION
		prepareForMcPh.setNewStatus(ProcessStatus.RUNNING);
		final File phenotypeFile = markerCensusOperationParams.getPhenotypeFile();
		if (phenotypeFile != null) {
			// BY EXTERNAL PHENOTYPE FILE
			// use Sample Info file affection state
			Set<SampleInfo.Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath());

			if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
					&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
			{
				log.info("Updating Sample Info in DB");
				final SampleInfoExtractorDataSetDestination sampleInfoExtractor
						= new SampleInfoExtractorDataSetDestination(new NullDataSetDestination());
				SamplesParserManager.scanSampleInfo(
						markerCensusOperationParams.getParent().getOrigin().getStudyKey(),
						ImportFormat.GWASpi,
						phenotypeFile.getPath(),
						sampleInfoExtractor);
				Collection<SampleInfo> sampleInfos = sampleInfoExtractor.getSampleInfos().values();
				SampleInfoList.insertSampleInfos(sampleInfos);
			} else {
				log.warn(Text.Operation.warnAffectionMissing);
				return censusOpKey;
			}
		} else {
			// BY DB AFFECTION
			// use Sample Info from the DB to extract affection state
			Set<SampleInfo.Affection> affectionStates = SamplesParserManager.collectAffectionStates(markerCensusOperationParams.getParent());
			if (!(affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
					&& affectionStates.contains(SampleInfo.Affection.AFFECTED)))
			{
				log.warn(Text.Operation.warnAffectionMissing);
				return censusOpKey;
			}
		}
		prepareForMcPh.setNewStatus(ProcessStatus.COMPLEETED);

		final MarkerCensusOperation markerCensusOperation = new MarkerCensusOperation(markerCensusOperationParams);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MARKER_CENSUS, markerCensusOperation.getProgressSource(), null);
		censusOpKey = OperationManager.performOperationCreatingOperation(markerCensusOperation);

		return censusOpKey;
	}

//	private static OperationKey checkPerformHW(SwingWorkerItem thisSwi, OperationKey censusOpKey, final OperationKey markersQAOpKey) throws Exception {
//
//		// HW ON GENOTYPE FREQ.
//		OperationKey hwOpKey = null;
//		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
//				&& (censusOpKey != null))
//		{
//			final HardyWeinbergOperationParams params = new HardyWeinbergOperationParams(censusOpKey, cNetCDF.Defaults.DEFAULT_AFFECTION, markersQAOpKey);
//			final Threaded_HardyWeinberg threaded_HardyWeinberg = new Threaded_HardyWeinberg(params);
//			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_HARDY_WEINBERG, threaded_HardyWeinberg.getProgressSource(), null);
//			threaded_HardyWeinberg.runInternal(thisSwi);
//			hwOpKey = threaded_HardyWeinberg.getHardyWeinbergOperationKey();
//		}
//
//		return hwOpKey;
//	}

	public OperationKey getMarkerCensusOperationKey() {
		return markerCensusOperationKey;
	}

	public OperationKey getHardyWeinbergOperationKey() {
		return hardyWeinbergOperationKey;
	}

	@Override
	protected void runInternal() throws IOException {

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		markerCensusOperationKey = checkPerformMarkerCensus(getLog(), gwasParams);

		// NOTE ABORTION_POINT We could be gracefully abort here

		// HW ON GENOTYPE FREQ.
		if (markerCensusOperationKey == null) {
			getLog().warn("Hardy&Weinberg operation canceled because Marker-Census is not available");
		} else {
			final OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(gwasParams.getMarkerCensusOperationParams().getParent(), OPType.MARKER_QA).get(0));
			HardyWeinbergOperationParams params = new HardyWeinbergOperationParams(markerCensusOperationKey, NetCDFConstants.Defaults.DEFAULT_AFFECTION, markersQAOpKey);
			final Threaded_HardyWeinberg threaded_HardyWeinberg = new Threaded_HardyWeinberg(params);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_HARDY_WEINBERG, threaded_HardyWeinberg.getProgressSource(), null);
			CommonRunnable.doRunNowInThread(threaded_HardyWeinberg);
			progressSource.setNewStatus(ProcessStatus.FINALIZING);
			hardyWeinbergOperationKey = threaded_HardyWeinberg.getHardyWeinbergOperationKey();
			progressSource.setNewStatus(ProcessStatus.COMPLEETED);
		}
	}
}
