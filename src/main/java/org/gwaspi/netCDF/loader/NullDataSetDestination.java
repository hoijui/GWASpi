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
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

/**
 * Similar to <code>/dev/null</code> on the Linux file system,
 * this class just throws away all data it receives.
 */
public class NullDataSetDestination implements DataSetDestination {

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
	public void addSampleInfo(SampleInfo sampleInfo) throws IOException {
	}

	@Override
	public void addSampleKey(SampleKey sampleKey) throws IOException {
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
	}

	@Override
	public void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException {
	}

	@Override
	public void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException {
	}

	@Override
	public void addMarkerKey(MarkerKey markerKey) throws IOException {
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
	}

	@Override
	public void startLoadingChromosomeMetadatas() throws IOException {
	}

	@Override
	public void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException {
	}

	@Override
	public void finishedLoadingChromosomeMetadatas() throws IOException {
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, List<byte[]> sampleAlleles) throws IOException {
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, List<byte[]> markerAlleles) throws IOException {
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
	}

	@Override
	public void done() throws IOException {
	}
}
