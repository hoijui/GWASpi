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

package org.gwaspi.datasource.filter;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;

/**
 * TODO add class description
 */
public class SampleIndicesFilterDataSetSource implements DataSetSource {

	private final DataSetSource parent;
	private final List<Integer> toKeepSampleOrigIndices;

	public SampleIndicesFilterDataSetSource(DataSetSource parent, List<Integer> toKeepSampleOrigIndices) {

		this.parent = parent;
		this.toKeepSampleOrigIndices = toKeepSampleOrigIndices;
	}

	@Override
	public DataSetSource getOriginDataSetSource() throws IOException {
		return parent.getOriginDataSetSource();
	}

	@Override
	public int getNumMarkers() throws IOException {
		return parent.getNumMarkers();
	}

	@Override
	public MatrixMetadata getMatrixMetadata() throws IOException {
		return parent.getMatrixMetadata();
	}

	@Override
	public MarkersGenotypesSource getMarkersGenotypesSource() throws IOException {
		return new InternalIndicesFilteredMarkersGenotypesSource(parent.getMarkersGenotypesSource(), toKeepSampleOrigIndices);
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {
		return parent.getMarkersMetadatasSource();
	}

	@Override
	public int getNumChromosomes() throws IOException {
		return parent.getNumChromosomes();
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() throws IOException {
		return parent.getChromosomesKeysSource();
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {
		return parent.getChromosomesInfosSource();
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() throws IOException {
		return parent.getMarkersKeysSource();
	}

	@Override
	public int getNumSamples() {
		return toKeepSampleOrigIndices.size();
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {
		return new IndicesFilteredSamplesGenotypesSource(parent.getSamplesGenotypesSource(), toKeepSampleOrigIndices);
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {
		return new IndicesFilteredSamplesInfosSource(this, parent.getSamplesInfosSource(), toKeepSampleOrigIndices);
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() throws IOException {
		return new IndicesFilteredSamplesKeysSource(parent.getSamplesKeysSource(), toKeepSampleOrigIndices);
	}
}
