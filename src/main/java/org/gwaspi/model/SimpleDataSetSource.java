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

package org.gwaspi.model;


import java.io.IOException;

/**
 * TODO
 */
public class SimpleDataSetSource implements DataSetSource {

	private MatrixMetadata matrixMetadata;
	private MarkersGenotypesSource markersGenotypesSource;
	private MarkersMetadataSource markersMetadatasSource;
	private ChromosomesKeysSource chromosomesKeysSource;
	private ChromosomesInfosSource chromosomesInfosSource;
	private MarkersKeysSource markersKeysSource;
	private SamplesGenotypesSource samplesGenotypesSource;
	private SamplesInfosSource samplesInfosSource;
	private SamplesKeysSource samplesKeysSource;


	@Override
	public MatrixMetadata getMatrixMetadata() throws IOException {
		return matrixMetadata;
	}

	public void setMatrixMetadata(MatrixMetadata matrixMetadata) {
		this.matrixMetadata = matrixMetadata;
	}

	@Override
	public MarkersGenotypesSource getMarkersGenotypesSource() {
		return markersGenotypesSource;
	}

	public void setMarkersGenotypesSource(MarkersGenotypesSource markersGenotypesSource) {
		this.markersGenotypesSource = markersGenotypesSource;
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() {
		return markersMetadatasSource;
	}

	public void setMarkersMetadatasSource(MarkersMetadataSource markersMetadatasSource) {
		this.markersMetadatasSource = markersMetadatasSource;
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() {
		return chromosomesKeysSource;
	}

	public void setChromosomesKeysSource(ChromosomesKeysSource chromosomesKeysSource) {
		this.chromosomesKeysSource = chromosomesKeysSource;
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() {
		return chromosomesInfosSource;
	}

	public void setChromosomesInfosSource(ChromosomesInfosSource chromosomesInfosSource) {
		this.chromosomesInfosSource = chromosomesInfosSource;
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() {
		return markersKeysSource;
	}

	public void setMarkersKeysSource(MarkersKeysSource markersKeysSource) {
		this.markersKeysSource = markersKeysSource;
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() {
		return samplesGenotypesSource;
	}

	public void setSamplesGenotypesSource(SamplesGenotypesSource samplesGenotypesSource) {
		this.samplesGenotypesSource = samplesGenotypesSource;
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() {
		return samplesInfosSource;
	}

	public void setSamplesInfosSource(SamplesInfosSource samplesInfosSource) {
		this.samplesInfosSource = samplesInfosSource;
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() {
		return samplesKeysSource;
	}

	public void setSamplesKeysSource(SamplesKeysSource samplesKeysSource) {
		this.samplesKeysSource = samplesKeysSource;
	}
}
