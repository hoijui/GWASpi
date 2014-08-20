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
import org.gwaspi.netCDF.loader.DataSetDestinationProgressHandler;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.LoadingNetCDFDataSetDestination;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.loader.SampleInfoExtractorDataSetDestination;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Loader_GWASifOK extends CommonRunnable {

	private static final ProcessInfo loadAndFullGwasProcessInfo
			= new DefaultProcessInfo("Load GTs & GWAS",
					"Load Genotypes and conduct a full GWAS on them"); // TODO
	private static final ProcessInfo loadGTsProcessInfo
			= new DefaultProcessInfo("Load GTs",
					"Load Genotypes"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_LOAD_GTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_LOAD_GTS", null));
	public static final ProgressSource PLACEHOLDER_PS_GWAS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_GWAS", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(3);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_LOAD_GTS, 0.2);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_GWAS, 0.6);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final boolean dummySamples;
	private final boolean performGwas;
	private final GenotypesLoadDescription loadDescription;
	private final GWASinOneGOParams gwasParams;
	private final SuperProgressSource progressSource;

	public Threaded_Loader_GWASifOK(
			GenotypesLoadDescription loadDescription,
			boolean dummySamples,
			boolean performGwas,
			GWASinOneGOParams gwasParams)
	{
		super(
				"Genotypes Loader & GWAS if OK",
				"Loading Genotypes & Performing GWAS",
				"Genotypes Loader & GWAS if OK: " + loadDescription.getFriendlyName(),
				"Loading Genotypes & Performing GWAS");

		this.loadDescription = loadDescription;
		this.dummySamples = dummySamples;
		this.performGwas = performGwas;
		this.gwasParams = gwasParams;
		this.progressSource = new SuperProgressSource(loadAndFullGwasProcessInfo, subProgressSourcesAndWeights);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_GWASifOK.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		final LoadingNetCDFDataSetDestination dataReceiver = new LoadingNetCDFDataSetDestination(loadDescription); // HACK FIXME
//		ZipTwoWaySaverSamplesReceiver samplesReceiver = new ZipTwoWaySaverSamplesReceiver(loadDescription); // HACK FIXME
//		InMemorySamplesReceiver samplesReceiver = new InMemorySamplesReceiver(); // HACK FIXME
		final DataSetDestinationProgressHandler dataSetDestinationProgressHandler = new DataSetDestinationProgressHandler(loadGTsProcessInfo);
		dataReceiver.setProgressHandler(dataSetDestinationProgressHandler);
		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_LOAD_GTS, dataSetDestinationProgressHandler, null);
		final SampleInfoExtractorDataSetDestination sampleInfoExtractor
				= new SampleInfoExtractorDataSetDestination(dataReceiver);
		SampleInfoCollectorSwitch.collectSampleInfo(
				loadDescription.getStudyKey(),
				loadDescription.getFormat(),
				dummySamples,
				loadDescription.getSampleFilePath(),
				loadDescription.getGtDirPath(),
				loadDescription.getAnnotationFilePath(),
				sampleInfoExtractor);
		Set<SampleInfo.Affection> affectionStates = SampleInfoCollectorSwitch.collectAffectionStates(sampleInfoExtractor.getSampleInfos().values());

		final String markerCensusName = cNetCDF.Defaults.DEFAULT_AFFECTION;

		final DataSetKey parent;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			LoadManager.dispatchLoadByFormat(
					loadDescription,
					dataReceiver);
			MatrixKey matrixKey = dataReceiver.getResultMatrixKey();
			dataReceiver.done();
			MultiOperations.printCompleted("Loading Genotypes");
			parent = new DataSetKey(matrixKey);
		} else {
			return;
		}

		final OperationKey[] qaOpKeys = Threaded_MatrixQA.matrixCompleeted(thisSwi, parent.getMatrixParent(), progressSource);

		final OperationKey samplesQAOpKey = qaOpKeys[0];
		final OperationKey markersQAOpKey = qaOpKeys[1];
		final MarkerCensusOperationParams markerCensusOperationParams
				= new MarkerCensusOperationParams(parent, samplesQAOpKey, markersQAOpKey);
		markerCensusOperationParams.setName(markerCensusName);
		gwasParams.setMarkerCensusOperationParams(markerCensusOperationParams);

		if (performGwas
				&& affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
				&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
		{
			final Threaded_GWAS threaded_GWAS = new Threaded_GWAS(gwasParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_GWAS, threaded_GWAS.getProgressSource(), null);
			CommonRunnable.doRunNowInThread(threaded_GWAS, thisSwi);
		}
	}
}
