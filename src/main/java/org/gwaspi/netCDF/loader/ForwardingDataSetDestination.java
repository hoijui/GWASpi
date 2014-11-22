/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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
import java.util.List;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

/**
 * Forwards all method calls to the internally stored DataSetDestination.
 */
public class ForwardingDataSetDestination implements DataSetDestination {

	private final DataSetDestination internalDataSetDestination;

	protected ForwardingDataSetDestination(final DataSetDestination internalDataSetDestination) {

		this.internalDataSetDestination = internalDataSetDestination;
	}

	protected DataSetDestination getInternalDataSetDestination() {
		return internalDataSetDestination;
	}

	@Override
	public void init() throws IOException {
		internalDataSetDestination.init();
	}

	@Override
	public void startLoadingDummySampleInfos() throws IOException {
		internalDataSetDestination.startLoadingDummySampleInfos();
	}

	@Override
	public void finishedLoadingDummySampleInfos() throws IOException {
		internalDataSetDestination.finishedLoadingDummySampleInfos();
	}

	@Override
	public void startLoadingSampleInfos(boolean storeOnlyKeys) throws IOException {
		internalDataSetDestination.startLoadingSampleInfos(storeOnlyKeys);
	}

	@Override
	public void addSampleInfo(SampleInfo sampleInfo) throws IOException {
		internalDataSetDestination.addSampleInfo(sampleInfo);
	}

	@Override
	public void addSampleKey(SampleKey sampleKey) throws IOException {
		internalDataSetDestination.addSampleKey(sampleKey);
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		internalDataSetDestination.finishedLoadingSampleInfos();
	}

	@Override
	public void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException {
		internalDataSetDestination.startLoadingMarkerMetadatas(storeOnlyKeys);
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {
		internalDataSetDestination.addMarkerMetadata(markerMetadata);
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		internalDataSetDestination.finishedLoadingMarkerMetadatas();
	}

	@Override
	public void startLoadingChromosomeMetadatas() throws IOException {
		internalDataSetDestination.startLoadingChromosomeMetadatas();
	}

	@Override
	public void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException {
		internalDataSetDestination.addChromosomeMetadata(chromosomeKey, chromosomeInfo);
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
		internalDataSetDestination.finishedLoadingChromosomeMetadatas();
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
		internalDataSetDestination.startLoadingAlleles(perSample);
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, List<byte[]> sampleAlleles) throws IOException {
		internalDataSetDestination.addSampleGTAlleles(sampleIndex, sampleAlleles);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, List<byte[]> markerAlleles) throws IOException {
		internalDataSetDestination.addMarkerGTAlleles(markerIndex, markerAlleles);
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
		internalDataSetDestination.finishedLoadingAlleles();
	}

	@Override
	public void done() throws IOException {
		internalDataSetDestination.done();
	}

	@Override
	public MatrixKey getResultMatrixKey() {
		return internalDataSetDestination.getResultMatrixKey();
	}
}
