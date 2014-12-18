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
import java.util.List;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;

public interface OperationDataSet<E extends OperationDataEntry> extends DataSetSource {

	void addOperationKeyListener(OperationKeyListener lst);
	void removeOperationKeyListener(OperationKeyListener lst);
	OperationKey getOperationKey();

	OperationTypeInfo getTypeInfo();

	DataSetKey getParent();

	void setParams(OperationParams params);

	void setNumSamples(int numSamples) throws IOException;
	void setNumMarkers(int numMarkers) throws IOException;
	void setNumChromosomes(int numChromosomes) throws IOException;

	/**
	 * Set the samples used by this operation.
	 * This can only be a sub-set of the samples used by the parent operation,
	 * and is only called if it is not the full set used by the parent.
	 * NetCDF variable: Variables.VAR_OPSET
	 * @param originalIndices the indices in the origin matrix
	 * @param keys the values related to the <code>originalIndices</code>
	 * @throws IOException
	 */
	void setSamples(List<Integer> originalIndices, List<SampleKey> keys) throws IOException;

	/**
	 * Set the markers used by this operation.
	 * This can only be a sub-set of the markers used by the parent operation,
	 * and is only called if it is not the full set used by the parent.
	 * NetCDF variable: Variables.VAR_IMPLICITSET
	 * @param originalIndices the indices in the origin matrix
	 * @param keys the values related to the <code>originalIndices</code>
	 * @throws IOException
	 */
	void setMarkers(List<Integer> originalIndices, List<MarkerKey> keys) throws IOException;

	/**
	 * Set the chromosomes used by this operation.
	 * This can only be a sub-set of the chromosomes used by the parent operation,
	 * and is only called if it is not the full set used by the parent.
	 * NetCDF variable:
	 * - Variables.VAR_CHR_IN_MATRIX [ChromosomeKey]
	 * - Variables.VAR_CHR_INFO [ChromosomeKey]
	 * @param originalIndices the indices in the origin matrix
	 * @param keys the values related to the <code>originalIndices</code>
	 * @throws IOException
	 */
	void setChromosomes(List<Integer> originalIndices, List<ChromosomeKey> keys) throws IOException;

	void finnishWriting() throws IOException;

	/**
	 * Returns the immediate parent data-source to this one.
	 * @return the parent data-source to this one,
	 *   or <code>null</code>, if it is the root source (a matrix)
	 * @throws IOException
	 */
	DataSetSource getParentDataSetSource() throws IOException;

//	int getNumSamples() throws IOException;
//	int getNumMarkers() throws IOException;
//	int getNumChromosomes() throws IOException;
//
//	Map<Integer, SampleKey> getSamples() throws IOException;
//	Map<Integer, MarkerKey> getMarkers() throws IOException;
//	Map<Integer, ChromosomeKey> getChromosomes() throws IOException;

	/**
	 *
	 * @param from start index (inclusive), or -1 for the start
	 * @param to end index (exclusive), or -1 for the end
	 * @return
	 * @throws IOException
	 */
	List<E> getEntries(int from, int to) throws IOException;

	List<E> getEntries() throws IOException;
}
