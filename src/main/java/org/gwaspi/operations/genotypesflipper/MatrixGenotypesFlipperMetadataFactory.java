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

package org.gwaspi.operations.genotypesflipper;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixMetadataFactory;

public class MatrixGenotypesFlipperMetadataFactory
		implements MatrixMetadataFactory<DataSet, MatrixGenotypesFlipperParams> {

	public static final MatrixGenotypesFlipperMetadataFactory SINGLETON
			= new MatrixGenotypesFlipperMetadataFactory();

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MatrixGenotypesFlipperParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata sourceMatrixMetadata
				= MatricesList.getMatrixMetadataById(params.getParent().getMatrixParent());

		StringBuilder description = new StringBuilder();
		description.append(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(sourceMatrixMetadata.getMatrixId());
		description.append(" - ").append(sourceMatrixMetadata.getFriendlyName());
		description.append("\nUsed list of markers to be flipped: ").append(params.getFlipperFile().getPath());
		if (!params.getMatrixDescription().isEmpty()) {
			description.append("\n\nDescription: ");
			description.append(params.getMatrixDescription());
			description.append("\n");
		}
		description.append("\nGenotype encoding: ");
		description.append(sourceMatrixMetadata.getGenotypeEncoding());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);

		return new MatrixMetadata(
				sourceMatrixMetadata.getStudyKey(),
				params.getMatrixFriendlyName(),
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
	public String getStrandFlag() {
		return null;
	}

	@Override
	public GenotypeEncoding getGuessedGTCode(MatrixGenotypesFlipperParams params) {
		try {
			final MatrixMetadata sourceMatrixMetadata
					= MatricesList.getMatrixMetadataById(params.getParent().getMatrixParent());
			return sourceMatrixMetadata.getGenotypeEncoding();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
