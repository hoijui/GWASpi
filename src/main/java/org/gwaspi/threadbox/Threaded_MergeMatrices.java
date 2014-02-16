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

package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.operations.MatrixMergeSamples;
import org.gwaspi.netCDF.operations.MatrixOperation;
import org.gwaspi.netCDF.operations.MergeAllMatrixOperation;
import org.gwaspi.netCDF.operations.MergeMarkersMatrixOperation;
import org.gwaspi.netCDF.operations.MergeMatrixNetCDFDataSetDestination;

public class Threaded_MergeMatrices extends AbstractThreaded_MergeMatrices {

	/**
	 * Whether to merge all, or only the marked samples
	 * TODO the previous sentence needs revising
	 */
	private final boolean samples;
	private final boolean markers;

	public Threaded_MergeMatrices(
			MatrixKey parentMatrixKey1,
			MatrixKey parentMatrixKey2,
			String newMatrixName,
			String description,
			boolean samples,
			boolean markers)
	{
		super(
				parentMatrixKey1,
				parentMatrixKey2,
				newMatrixName,
				description);

		this.samples = samples;
		this.markers = markers;
	}

	@Override
	protected AbstractNetCDFDataSetDestination createMatrixDataSetDestination(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2)
			throws IOException
	{
		final String humanReadableMethodName;
		final String methodDescription;
		if (samples) {
			humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
			methodDescription = Text.Trafo.mergeMethodSampleJoin;
		} else if (markers) {
			humanReadableMethodName = Text.Trafo.mergeMarkersOnly;
			methodDescription = Text.Trafo.mergeMethodMarkerJoin;
		} else {
			humanReadableMethodName = Text.Trafo.mergeAll;
			methodDescription = Text.Trafo.mergeMethodMergeAll;
		}
		AbstractNetCDFDataSetDestination dataSetDestination
				= new MergeMatrixNetCDFDataSetDestination(
				dataSetSource1,
				dataSetSource2,
				newMatrixName,
				description,
				humanReadableMethodName,
				methodDescription);

		return dataSetDestination;
	}

	@Override
	protected MatrixOperation createMatrixOperation(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			AbstractNetCDFDataSetDestination dataSetDestination)
			throws IOException
	{
		final MatrixOperation joinMatrices;
		if (samples) {
			joinMatrices = new MatrixMergeSamples(
					dataSetSource1,
					dataSetSource2,
					dataSetDestination);
		} else if (markers) {
			joinMatrices = new MergeMarkersMatrixOperation(
					dataSetSource1,
					dataSetSource2,
					dataSetDestination);
		} else {
			joinMatrices = new MergeAllMatrixOperation(
					dataSetSource1,
					dataSetSource2,
					dataSetDestination);
		}

		return joinMatrices;
	}
}
