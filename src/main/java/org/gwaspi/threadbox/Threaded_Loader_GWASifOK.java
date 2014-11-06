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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.AbstractDataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.loader.DataSetDestinationProgressHandler;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.LoadingMatrixMetadataFactory;
import org.gwaspi.netCDF.loader.LoadingDataSetDestination;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.loader.SampleInfoExtractorDataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
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

public class Threaded_Loader_GWASifOK extends CommonRunnable {

	private static final ProcessInfo loadAndFullGwasProcessInfo
			= new DefaultProcessInfo("Load GTs & QA & GWAS",
					"Load Genotypes and conduct a full GWAS on them"); // TODO
	private static final ProcessInfo loadOnlyProcessInfo // this includes loading data (info & GTs) followed by QA
			= new DefaultProcessInfo("Load GTs & QA",
					"Load Genotypes and run QA on them"); // TODO
	private static final ProcessInfo pureLoadProcessInfo // this is only loading data (info & GTs)
			= new DefaultProcessInfo("Load GTs",
					"Load Genotypes"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_LOAD_GTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_LOAD_GTS", null));
	public static final ProgressSource PLACEHOLDER_PS_GWAS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_GWAS", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeightsLoadOnly;
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeightsFull;
	static {
		LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_LOAD_GTS, 0.5);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.5);
		subProgressSourcesAndWeightsLoadOnly = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);

		tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(3);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_LOAD_GTS, 0.2);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_GWAS, 0.6);
		subProgressSourcesAndWeightsFull = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final boolean dummySamples;
	private final boolean performGwas;
	private final GenotypesLoadDescription loadDescription;
	private final GWASinOneGOParams gwasParams;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;
	private MatrixKey resultMatrixKey;

	public Threaded_Loader_GWASifOK(
			GenotypesLoadDescription loadDescription,
			boolean dummySamples,
			boolean performGwas,
			GWASinOneGOParams gwasParams)
	{
		super(
				"Genotypes Loader & GWAS if OK",
				"on " + loadDescription.getFriendlyName());

		this.loadDescription = loadDescription;
		this.dummySamples = dummySamples;
		this.performGwas = performGwas;
		this.gwasParams = gwasParams;
		if (performGwas) {
			this.progressSource = new SuperProgressSource(loadAndFullGwasProcessInfo, subProgressSourcesAndWeightsFull);
		} else {
			this.progressSource = new SuperProgressSource(loadOnlyProcessInfo, subProgressSourcesAndWeightsLoadOnly);
		}
		this.taskLockProperties = new TaskLockProperties();
		this.taskLockProperties.getStudyIds().add(loadDescription.getStudyKey().getId());
		this.resultMatrixKey = null;
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
		return LoggerFactory.getLogger(Threaded_Loader_GWASifOK.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);
		final LoadingMatrixMetadataFactory loadingMatrixMetadataFactory
				= new LoadingMatrixMetadataFactory(loadDescription);
		final AbstractDataSetDestination innerDataReceiver
				= (AbstractDataSetDestination) MatrixFactory.generateMatrixDataSetDestination(
						null, loadingMatrixMetadataFactory); // HACK FIXME
		final DataSetDestination dataReceiver = new LoadingDataSetDestination(innerDataReceiver, loadDescription); // HACK FIXME
		final DataSetDestinationProgressHandler dataSetDestinationProgressHandler = new DataSetDestinationProgressHandler(pureLoadProcessInfo);
		innerDataReceiver.setProgressHandler(dataSetDestinationProgressHandler);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_LOAD_GTS, dataSetDestinationProgressHandler, null);
		final SampleInfoExtractorDataSetDestination sampleInfoExtractor
				= new SampleInfoExtractorDataSetDestination(dataReceiver);

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		SampleInfoCollectorSwitch.collectSampleInfo(
				loadDescription.getStudyKey(),
				loadDescription.getFormat(),
				dummySamples,
				loadDescription.getSampleFilePath(),
				loadDescription.getGtDirPath(),
				loadDescription.getAnnotationFilePath(),
				sampleInfoExtractor);

		// NOTE ABORTION_POINT We could be gracefully abort here

		LoadManager.dispatchLoadByFormat(
				loadDescription,
				sampleInfoExtractor.getSampleInfos(),
				dataReceiver,
				loadingMatrixMetadataFactory);
		dataReceiver.done();
		resultMatrixKey = dataReceiver.getResultMatrixKey();
		MultiOperations.printCompleted("Loading Genotypes");
		final DataSetKey parent = new DataSetKey(resultMatrixKey);

		final OperationKey[] qaOpKeys = Threaded_MatrixQA.matrixCompleeted(thisSwi, parent.getMatrixParent(), progressSource);

		if (performGwas) {
			final OperationKey samplesQAOpKey = qaOpKeys[0];
			final OperationKey markersQAOpKey = qaOpKeys[1];
			final MarkerCensusOperationParams markerCensusOperationParams
					= new MarkerCensusOperationParams(parent, samplesQAOpKey, markersQAOpKey);
			final String markerCensusName = cNetCDF.Defaults.DEFAULT_AFFECTION;
			markerCensusOperationParams.setName(markerCensusName);
			gwasParams.setMarkerCensusOperationParams(markerCensusOperationParams);

			final Set<SampleInfo.Affection> affectionStates
					= SamplesParserManager.collectAffectionStates(
							sampleInfoExtractor.getSampleInfos().values());
			if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
					&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
			{
				final Threaded_GWAS threaded_GWAS = new Threaded_GWAS(gwasParams);
				progressSource.replaceSubProgressSource(PLACEHOLDER_PS_GWAS, threaded_GWAS.getProgressSource(), null);
				CommonRunnable.doRunNowInThread(threaded_GWAS, thisSwi);
			} else {
				getLog().warn("GWAS is not performed, because the data set did not contain both affected and unaffected samples");
			}
		}
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}

	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}
}
