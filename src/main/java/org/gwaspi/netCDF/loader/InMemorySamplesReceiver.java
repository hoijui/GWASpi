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

/**
 * TODO
 */
public class InMemorySamplesReceiver extends AbstractSamplesReceiver implements SamplesReceiver {

	public InMemorySamplesReceiver() {
	}

	@Override
	public void startLoadingAlleles(boolean perSample) throws Exception {
		super.startLoadingAlleles(perSample);
		
		getDataSet().initAlleleStorage();
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws Exception {
		getDataSet().setSampleAlleles(sampleIndex, sampleAlleles);
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws Exception {
		getDataSet().setMarkerAlleles(markerIndex, markerAlleles);
	}
}
