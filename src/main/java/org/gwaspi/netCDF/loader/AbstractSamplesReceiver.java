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


import java.util.Collection;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;

/**
 * TODO
 */
public abstract class AbstractSamplesReceiver implements SamplesReceiver {

	private final DataSet dataSet;

	protected AbstractSamplesReceiver() {

		this.dataSet = new DataSet();
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	@Override
	public void init() throws Exception {
	}

	@Override
	public void startLoadingDummySampleInfos() throws Exception {
	}

	@Override
	public void finishedLoadingDummySampleInfos() throws Exception {
	}

	@Override
	public void startLoadingSampleInfos() throws Exception {
	}

	@Override
	public void addSampleInfo(SampleInfo sampleInfo) throws Exception {
		dataSet.getSampleInfos().add(sampleInfo);
	}

	@Override
	public void finishedLoadingSampleInfos() throws Exception {
	}

	@Override
	public void startLoadingMarkerMetadatas() throws Exception {
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws Exception {
		dataSet.getMarkerMetadatas().put(MarkerKey.valueOf(markerMetadata), markerMetadata);
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws Exception {
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws Exception {
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finishedLoadingAlleles() throws Exception {
	}

	@Override
	public void done() throws Exception {
	}
}
