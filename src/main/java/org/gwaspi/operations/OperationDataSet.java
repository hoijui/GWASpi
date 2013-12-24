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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;

public interface OperationDataSet<ET> {

	void setNumSamples(int numSamples) throws IOException;
	void setNumMarkers(int numMarkers) throws IOException;
	void setNumChromosomes(int numChromosomes) throws IOException;

	/**
	 * @param matrixIndexSampleKeys
	 *   the indices in the matrix, and the values (copied) of the sample keys
	 * NetCDF variable: Variables.VAR_OPSET
	 */
	void setSamples(Map<Integer, SampleKey> matrixIndexSampleKeys) throws IOException;

	/**
	 * @param matrixIndexMarkerKeys
	 *   the indices in the matrix, and the values (copied) of the marker keys
	 * NetCDF variable: Variables.VAR_IMPLICITSET
	 */
	void setMarkers(Map<Integer, MarkerKey> matrixIndexMarkerKeys) throws IOException;

	/**
	 * @param matrixIndexChromosomeKeys
	 *   the indices in the matrix, and the values (copied) of the chromosome keys
	 * NetCDF variable:
	 * - Variables.VAR_CHR_IN_MATRIX [ChromosomeKey]
	 * - Variables.VAR_CHR_INFO [ChromosomeKey]
	 */
	void setChromosomes(Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys/*, Collection<ChromosomeInfo> chromosomeInfos*/) throws IOException;

	void finnishWriting() throws IOException;

	int getNumSamples() throws IOException;
	int getNumMarkers() throws IOException;
	int getNumChromosomes() throws IOException;

	Map<Integer, SampleKey> getSamples() throws IOException;
	Map<Integer, MarkerKey> getMarkers() throws IOException;
	Map<Integer, ChromosomeKey> getChromosomes() throws IOException;

	/**
	 *
	 * @param from start index (inclusive), or -1 for the start
	 * @param to end index (exclusive), or -1 for the end
	 * @return
	 * @throws IOException
	 */
	Collection<ET> getEntries(int from, int to) throws IOException;

	Collection<ET> getEntries() throws IOException;
}
