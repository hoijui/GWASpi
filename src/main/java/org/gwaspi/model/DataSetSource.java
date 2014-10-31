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
 * TODO add class description
 */
public interface DataSetSource {

	/**
	 * Returns the original/root data-source to this one.
	 * @return the original/root data-source to this one (a matrix)
	 * @throws IOException
	 */
	DataSetSource getOriginDataSetSource() throws IOException;

	int getNumMarkers() throws IOException;

	MatrixMetadata getMatrixMetadata() throws IOException;

	MarkersGenotypesSource getMarkersGenotypesSource() throws IOException;

	MarkersMetadataSource getMarkersMetadatasSource() throws IOException;

	MarkersKeysSource getMarkersKeysSource() throws IOException;

	int getNumChromosomes() throws IOException;

	ChromosomesKeysSource getChromosomesKeysSource() throws IOException;

	ChromosomesInfosSource getChromosomesInfosSource() throws IOException;

	int getNumSamples() throws IOException;

	SamplesGenotypesSource getSamplesGenotypesSource() throws IOException;

	SamplesInfosSource getSamplesInfosSource() throws IOException;

	SamplesKeysSource getSamplesKeysSource() throws IOException;
}
