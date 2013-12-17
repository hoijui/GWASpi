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

import java.io.File;
import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;

public class MatrixGenotypesFlipperNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final DataSetSource dataSetSource;
	private final String matrixDescription;
	private final String matrixFriendlyName;
	private final File flipperFile;

	public MatrixGenotypesFlipperNetCDFDataSetDestination(
			DataSetSource dataSetSource,
			String matrixDescription,
			String matrixFriendlyName,
			File flipperFile)
	{
		this.dataSetSource = dataSetSource;
		this.matrixDescription = matrixDescription;
		this.matrixFriendlyName = matrixFriendlyName;
		this.flipperFile = flipperFile;
	}

	@Override
	protected MatrixMetadata createMatrixMetadata() throws IOException {

		final int numMarkers = getDataSet().getMarkerMetadatas().size();
		final int numSamples = getDataSet().getSampleInfos().size();
		final int numChromosomes = getDataSet().getChromosomeInfos().size();

		MatrixMetadata sourceMatrixMetadata = dataSetSource.getMatrixMetadata();

		StringBuilder description = new StringBuilder();
		description.append(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(sourceMatrixMetadata.getMatrixId());
		description.append(" - ").append(sourceMatrixMetadata.getFriendlyName());
		description.append("\nUsed list of markers to be flipped: ").append(flipperFile.getPath());
		if (!matrixDescription.isEmpty()) {
			description.append("\n\nDescription: ");
			description.append(matrixDescription);
			description.append("\n");
		}
		description.append("\nGenotype encoding: ");
		description.append(sourceMatrixMetadata.getGenotypeEncoding());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);

		return new MatrixMetadata(
				sourceMatrixMetadata.getStudyKey(),
				matrixFriendlyName,
				sourceMatrixMetadata.getTechnology(),
				description.toString(),
				sourceMatrixMetadata.getGenotypeEncoding(), // matrix genotype encoding from the original matrix
				StrandType.fromString("FLP"), // FIXME this will fail at runtime
				sourceMatrixMetadata.getHasDictionary(), // has dictionary?
				numMarkers,
				numSamples,
				numChromosomes,
				sourceMatrixMetadata.getKey().getMatrixId(), // orig/parent matrix 1 key
				MatrixKey.NULL_ID); // orig/parent matrix 2 key
	}

	@Override
	protected String getStrandFlag() {
		return null;
	}

	@Override
	protected GenotypeEncoding getGuessedGTCode() {
		try {
			return dataSetSource.getMatrixMetadata().getGenotypeEncoding();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
