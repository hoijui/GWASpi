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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public abstract class AbstractDataSetDestination implements DataSetDestination {

	private static final Logger log
			= LoggerFactory.getLogger(AbstractDataSetDestination.class);

	private final DataSet dataSet;

	protected AbstractDataSetDestination() {

		this.dataSet = new DataSet();
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	@Override
	public void init() throws IOException {
	}

	@Override
	public void startLoadingDummySampleInfos() throws IOException {
	}

	@Override
	public void finishedLoadingDummySampleInfos() throws IOException {
	}

	@Override
	public void startLoadingSampleInfos(boolean storeOnlyKeys) throws IOException {
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

		if ((sampleInfos.size() % 100) == 0) {
			logParsedSampleInfos();
		}
	}

	private void logParsedSampleInfos() {
		log.info("Parsed {} Samples for info...", dataSet.getSampleInfos().size());
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		logParsedSampleInfos();
	}

	@Override
	public void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException {
	}

	@Override
	public void addMarkerKey(MarkerKey markerKey) throws IOException {

		// we simply ignore this, and rely on #addMarkerMetadata() receiving
		// the correct order aswell, and we just extract the keys there
//		XXX;
//		throw new UnsupportedOperationException("Not yet implemented (was not in use when first introduced)");
//		MarkerMetadata markerMetadata = Matrix.getSample(markerKey);
//		dataSet.getMarkerMetadatas().put(markerKey, markerMetadata);
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {
		dataSet.getMarkerMetadatas().put(MarkerKey.valueOf(markerMetadata), markerMetadata);
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
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
	}

	@Override
	public void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException {
		dataSet.getChromosomeInfos().put(chromosomeKey, chromosomeInfo);
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
	}

//	@Override
//	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {
//		throw new UnsupportedOperationException(); XXX;
//	}
//
//	@Override
//	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
//		throw new UnsupportedOperationException(); XXX;
//	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
	}

	@Override
	public void done() throws IOException {
	}
}
