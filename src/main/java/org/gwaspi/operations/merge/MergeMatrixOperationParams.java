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

package org.gwaspi.operations.merge;

import java.io.IOException;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.operations.AbstractMatrixCreatingOperationParams;

public class MergeMatrixOperationParams extends AbstractMatrixCreatingOperationParams {

	private final DataSetKey source2;
	private final String humanReadableMethodName;
	private final String methodDescription;
	private final boolean mergeSamples;
	private final boolean mergeMarkers;

	public MergeMatrixOperationParams(
			DataSetKey source1,
			DataSetKey source2,
			String matrixDescription,
			String matrixFriendlyName,
			String humanReadableMethodName,
			String methodDescription,
			boolean mergeSamples,
			boolean mergeMarkers)
	{
		super(source1, matrixDescription, matrixFriendlyName);

		this.source2 = source2;
		this.humanReadableMethodName = humanReadableMethodName;
		this.methodDescription = methodDescription;
		this.mergeSamples = mergeSamples;
		this.mergeMarkers = mergeMarkers;
	}

	public static MergeMatrixOperationParams create(
			final DataSetKey source1,
			final DataSetKey source2,
			final String newMatrixName,
			final String description,
			final boolean mergeSamples,
			final boolean mergeMarkers)
			throws IOException
	{
		final String humanReadableMethodName;
		final String methodDescription;
		if (mergeSamples && !mergeMarkers) {
			humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
			methodDescription = Text.Trafo.mergeMethodSampleJoin;
		} else if (mergeMarkers && !mergeSamples) {
			humanReadableMethodName = Text.Trafo.mergeMarkersOnly;
			methodDescription = Text.Trafo.mergeMethodMarkerJoin;
		} else {
			humanReadableMethodName = Text.Trafo.mergeAll;
			methodDescription = Text.Trafo.mergeMethodMergeAll;
		}

		return new MergeMatrixOperationParams(
				source1,
				source2,
				newMatrixName,
				description,
				humanReadableMethodName,
				methodDescription,
				mergeSamples,
				mergeMarkers);
	}

	public DataSetKey getSource2() {
		return source2;
	}

	public String getHumanReadableMethodName() {
		return humanReadableMethodName;
	}

	public String getMethodDescription() {
		return methodDescription;
	}

	public boolean isMergeSamples() {
		return (mergeSamples && !mergeMarkers);
	}

	public boolean isMergeMarkers() {
		return (mergeMarkers && !mergeSamples);
	}

	public boolean isMergeAll() {
		return (mergeSamples == mergeMarkers);
	}
}
