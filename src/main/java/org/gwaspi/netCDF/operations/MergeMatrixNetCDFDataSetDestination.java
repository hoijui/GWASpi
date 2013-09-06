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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class MergeMatrixNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final Logger log
			= LoggerFactory.getLogger(MergeMatrixNetCDFDataSetDestination.class);

	private final DataSetSource dataSetSource1;
	private final DataSetSource dataSetSource2;
	private final String matrixDescription;
	private final String matrixFriendlyName;
	private final String humanReadableMethodName;
	private final String methodDescription;

	public MergeMatrixNetCDFDataSetDestination(
			DataSetSource dataSetSource1,
			DataSetSource dataSetSource2,
			String matrixDescription,
			String matrixFriendlyName,
			String humanReadableMethodName,
			String methodDescription)
	{
		this.dataSetSource1 = dataSetSource1;
		this.dataSetSource2 = dataSetSource2;
		this.matrixDescription = matrixDescription;
		this.matrixFriendlyName = matrixFriendlyName;
		this.humanReadableMethodName = humanReadableMethodName;
		this.methodDescription = methodDescription;
//
//		if () {
//			this.humanReadableMethodName = Text.Trafo.mergeAll;
//			this.methodDescription = Text.Trafo.mergeMethodMergeAll;
//		} else if () {
//			this.humanReadableMethodName = Text.Trafo.mergeMarkersOnly;
//			this.methodDescription = Text.Trafo.mergeMethodMarkerJoin;
//		} else {
//			this.humanReadableMethodName = Text.Trafo.mergeSamplesOnly;
//			this.methodDescription = Text.Trafo.mergeMethodSampleJoin;
//		}
	}

	@Override
	protected MatrixFactory createMatrixFactory() throws IOException {

		final int numMarkers = getDataSet().getMarkerMetadatas().size();
		final int numSamples = getDataSet().getSampleInfos().size();
		final int numChromosomes = getDataSet().getChromosomeInfos().size();

		final MatrixMetadata rdMatrix1Metadata = dataSetSource1.getMatrixMetadata();
		final MatrixMetadata rdMatrix2Metadata = dataSetSource2.getMatrixMetadata();

		final boolean hasDictionary = (rdMatrix1Metadata.getHasDictionray() && rdMatrix2Metadata.getHasDictionray());
		final GenotypeEncoding gtEncoding = getGuessedGTCode();
		ImportFormat technology = ImportFormat.UNKNOWN;
		if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
			technology = rdMatrix1Metadata.getTechnology();
		}

		StringBuilder description = new StringBuilder();
		description.append(matrixDescription);
		description.append("\n\n");
		description.append(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);
		description.append("\n");
		description.append(Text.Trafo.mergedFrom);
		description.append("\nMX-");
		description.append(rdMatrix1Metadata.getMatrixId());
		description.append(" - ");
		description.append(rdMatrix1Metadata.getMatrixFriendlyName());
		description.append("\nMX-");
		description.append(rdMatrix2Metadata.getMatrixId());
		description.append(" - ");
		description.append(rdMatrix2Metadata.getMatrixFriendlyName());
		description.append("\n\n");
		description.append("Merge Method - ");
		description.append(humanReadableMethodName);
		description.append(":\n");
		description.append(methodDescription);
		description.append("\nGenotype encoding: ");
		description.append(gtEncoding.toString());

		try {
			return new MatrixFactory(
					technology, // technology
					matrixFriendlyName,
					description.toString(), // description
					gtEncoding, // GT encoding
					rdMatrix1Metadata.getStrand(),
					hasDictionary, // has dictionary?
					numSamples,
					numMarkers,
					numChromosomes,
					rdMatrix1Metadata.getKey(), // Parent matrix 1 key
					rdMatrix2Metadata.getKey()); // Parent matrix 2 key
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	protected String getStrandFlag() {
		return null;
	}

	@Override
	protected GenotypeEncoding getGuessedGTCode() {

		try {
			MatrixMetadata rdMatrix1Metadata = dataSetSource1.getMatrixMetadata();
			MatrixMetadata rdMatrix2Metadata = dataSetSource2.getMatrixMetadata();

			GenotypeEncoding gtEncoding = GenotypeEncoding.UNKNOWN;
			if (rdMatrix1Metadata.getGenotypeEncoding().equals(rdMatrix2Metadata.getGenotypeEncoding())) {
				gtEncoding = rdMatrix1Metadata.getGenotypeEncoding();
			}
			return gtEncoding;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
