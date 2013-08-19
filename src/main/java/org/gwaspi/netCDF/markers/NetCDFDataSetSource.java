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
package org.gwaspi.netCDF.markers;

import java.util.ArrayList;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkersChromosomeInfosSource;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.samples.SampleSet;

/**
 * TODO
 */
public class NetCDFDataSetSource implements DataSetSource {

	private final MarkerSet markerSet;
	private final SampleSet sampleSet;

	public NetCDFDataSetSource(MarkerSet markerSet, SampleSet sampleSet) {

		this.markerSet = markerSet;
		this.sampleSet = sampleSet;
	}

	@Override
	public MarkersGenotypesSource getMarkersGenotypesSource() {
		return sampleSet;
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private static final class NetCdfMarkersChromosomeInfosSource extends ArrayList<ChromosomeInfo> implements MarkersChromosomeInfosSource {
	}

	@Override
	public MarkersChromosomeInfosSource getMarkersChromosomeInfosSource() {
		NetCdfMarkersChromosomeInfosSource chrInfSrc = new NetCdfMarkersChromosomeInfosSource();
		markerSet.initFullMarkerIdSetMap(); // XXX may not always be required
		chrInfSrc.addAll(markerSet.getChrInfoSetMap().values());

		return chrInfSrc;
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() {
		return markerSet;
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
