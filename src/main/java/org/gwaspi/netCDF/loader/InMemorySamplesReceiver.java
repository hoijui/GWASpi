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

/**
 * TODO
 */
public class InMemorySamplesReceiver extends AbstractDataSetDestination {

	public InMemorySamplesReceiver() {
	}

	@Override
	public void finishedLoadingSampleInfos() throws IOException {
		getDataSet().getSampleInfos()
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
		super.startLoadingAlleles(perSample);

		getDataSet().initAlleleStorage();
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {
		getDataSet().setSampleAlleles(sampleIndex, sampleAlleles);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
		getDataSet().setMarkerAlleles(markerIndex, markerAlleles);
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {

	}

	@Override
	public void done() throws IOException {
//		new MatrixInMemoryDataSetSource(
//				null,
//				getDataSet().getMatrixMetadata(),
//				getDataSet().get,
//				getDataSet().equals(this),
//				getDataSet()., null, null, null, null, null, null)
	}
}
