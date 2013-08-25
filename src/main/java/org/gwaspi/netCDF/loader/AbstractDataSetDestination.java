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
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;

/**
 * TODO
 */
public abstract class AbstractDataSetDestination implements DataSetDestination {

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
	public void startLoadingSampleInfos() throws IOException {
	}

	@Override
	public void addSampleInfo(SampleInfo sampleInfo) throws IOException {
		dataSet.getSampleInfos().add(sampleInfo);
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
	}

	@Override
	public void startLoadingMarkerMetadatas() throws IOException {
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {
		dataSet.getMarkerMetadatas().put(MarkerKey.valueOf(markerMetadata), markerMetadata);
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		dataSet.extractChromosomeInfos();
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
	}

	@Override
	public void done() throws IOException {
	}
}
