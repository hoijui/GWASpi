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

package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public abstract class AbstractDataSetDestination implements DataSetDestination {

	private static final Logger log
			= LoggerFactory.getLogger(AbstractDataSetDestination.class);

	private final DataSet dataSet;
	private DataSetDestinationProgressHandler progressHandler;

	protected AbstractDataSetDestination() {

		this.dataSet = new DataSet();
		this.progressHandler = null;
	}

	/**
	 * @deprecated get rid of this and the whole dataSet, if possible, to reduce memory footprint
	 */
	protected DataSet getDataSet() {
		return dataSet;
	}

	public void setProgressHandler(DataSetDestinationProgressHandler progressHandler) {
		this.progressHandler = progressHandler;
	}

	@Override
	public void init() throws IOException {

		if (progressHandler == null) {
			// add basically a Null* (effectless) progress handler, so we do not have to check for == null in many places
			// NOTE this would be even better wiht a real Null* implementation, though it is a bit tricky to implement that
			progressHandler = new DataSetDestinationProgressHandler(new DefaultProcessInfo());
		}

		progressHandler.setNewStatus(ProcessStatus.INITIALIZING);
	}

	@Override
	public void startLoadingDummySampleInfos() throws IOException {
		progressHandler.setNewStatus(ProcessStatus.RUNNING);
		progressHandler.getSampleInfosProgressHandler().setNewStatus(ProcessStatus.RUNNING);
	}

	@Override
	public void finishedLoadingDummySampleInfos() throws IOException {
	}

	@Override
	public void startLoadingSampleInfos(boolean storeOnlyKeys) throws IOException {
		progressHandler.setNewStatus(ProcessStatus.RUNNING);
	}

	@Override
	public void addSampleKey(SampleKey sampleKey) throws IOException {

		SampleInfo sampleInfo = SampleInfoList.getSample(sampleKey);
		dataSet.getSampleInfos().add(sampleInfo);
	}

	@Override
	public void addSampleInfo(SampleInfo sampleInfo) throws IOException {

		Collection<SampleInfo> sampleInfos = dataSet.getSampleInfos();
		sampleInfos.add(sampleInfo);
		progressHandler.getSampleInfosProgressHandler().setProgress(sampleInfos.size() - 1);

		if ((sampleInfos.size() % 100) == 0) {
			logParsedSampleInfos();
		}
	}

	private void logParsedSampleInfos() {
		log.info("Parsed {} Samples for info...", dataSet.getSampleInfos().size());
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {

		progressHandler.getSampleInfosProgressHandler().setNewStatus(ProcessStatus.FINALIZING);
		SampleInfoList.insertSampleInfos(getDataSet().getSampleInfos());
		logParsedSampleInfos();
		progressHandler.getSampleInfosProgressHandler().setNewStatus(ProcessStatus.COMPLEETED);
	}

	@Override
	public void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException {

		progressHandler.setNewStatus(ProcessStatus.RUNNING);
		progressHandler.getMarkerInfosProgressHandler().setNewStatus(ProcessStatus.RUNNING);
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {

		dataSet.getMarkerMetadatas().put(MarkerKey.valueOf(markerMetadata), markerMetadata);
		progressHandler.getMarkerInfosProgressHandler().setProgress(dataSet.getMarkerMetadatas().size() - 1);
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		progressHandler.getMarkerInfosProgressHandler().setNewStatus(ProcessStatus.COMPLEETED);
	}

	public void extractChromosomeInfos() throws IOException {

		Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(dataSet.getMarkerMetadatas(), 2, 3);
		startLoadingChromosomeMetadatas();
		for (Map.Entry<ChromosomeKey, ChromosomeInfo> chromosomeInfoEntry : chromosomeInfo.entrySet()) {
			addChromosomeMetadata(chromosomeInfoEntry.getKey(), chromosomeInfoEntry.getValue());
		}
		finishedLoadingChromosomeMetadatas();
	}

	@Override
	public void startLoadingChromosomeMetadatas() throws IOException {
		progressHandler.getChromosomeInfosProgressHandler().setNewStatus(ProcessStatus.RUNNING);
	}

	@Override
	public void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException {

		dataSet.getChromosomeInfos().put(chromosomeKey, chromosomeInfo);
		progressHandler.getChromosomeInfosProgressHandler().setProgress(dataSet.getChromosomeInfos().size() - 1);
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
		progressHandler.getChromosomeInfosProgressHandler().setNewStatus(ProcessStatus.COMPLEETED);
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {

		progressHandler.getGenotypesProgressHandler().setNewStatus(ProcessStatus.INITIALIZING);
		progressHandler.getGenotypesProgressHandler().setStartState(0);
		if (perSample) {
			progressHandler.getGenotypesProgressHandler().setEndState(dataSet.getSampleInfos().size() - 1);
		} else {
			progressHandler.getGenotypesProgressHandler().setEndState(dataSet.getMarkerMetadatas().size() - 1);
		}
		progressHandler.getGenotypesProgressHandler().setNewStatus(ProcessStatus.RUNNING);
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, List<byte[]> sampleAlleles) throws IOException {
		progressHandler.getGenotypesProgressHandler().setProgress(sampleIndex);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, List<byte[]> markerAlleles) throws IOException {
		progressHandler.getGenotypesProgressHandler().setProgress(markerIndex);
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {

		progressHandler.getGenotypesProgressHandler().setNewStatus(ProcessStatus.COMPLEETED);
		progressHandler.setNewStatus(ProcessStatus.FINALIZING);
	}

	@Override
	public void done() throws IOException {
		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
