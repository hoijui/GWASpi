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

package org.gwaspi.operations.genotypestranslator;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixMetadataFactory;

public class MatrixTranslatorMetadataFactory
		implements MatrixMetadataFactory<DataSet, MatrixGenotypesTranslatorParams> {

	public static final MatrixTranslatorMetadataFactory SINGLETON
			= new MatrixTranslatorMetadataFactory();

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MatrixGenotypesTranslatorParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata sourceMatrixMetadata
				= MatricesList.getMatrixMetadataById(params.getParent().getMatrixParent());

		GenotypeEncoding gtEncoding = sourceMatrixMetadata.getGenotypeEncoding();
		String translationMethodDesc;
		if (gtEncoding.equals(GenotypeEncoding.AB0)
				|| gtEncoding.equals(GenotypeEncoding.O12))
		{
			translationMethodDesc = "AB0 or 012 to ACGT0 using the parent's dictionnary";
		} else if (gtEncoding.equals(GenotypeEncoding.O1234)) {
			translationMethodDesc = "O1234 to ACGT0 using 0=0, 1=A, 2=C, 3=G, 4=T";
		} else {
			throw new IllegalStateException(
					"Can not convert genotype-encoding: "
					+ gtEncoding.toString() + " to "
					+ GenotypeEncoding.ACGT0.toString());
		}

		StringBuilder description = new StringBuilder(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\nThrough Matrix translation from parent Matrix MX: ").append(sourceMatrixMetadata.getMatrixId());
		description.append(" - ").append(sourceMatrixMetadata.getFriendlyName());
		description.append("\nTranslation method: ").append(translationMethodDesc);
		if (!params.getMatrixDescription().isEmpty()) {
			description.append("\n\nDescription: ");
			description.append(params.getMatrixDescription());
			description.append("\n");
		}
		description.append("\nGenotype encoding: ");
		description.append(GenotypeEncoding.ACGT0.toString());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);

		return new MatrixMetadata(
				sourceMatrixMetadata.getStudyKey(),
				params.getMatrixFriendlyName(),
				sourceMatrixMetadata.getTechnology(),
				description.toString(),
				GenotypeEncoding.ACGT0, // New matrix genotype encoding
				sourceMatrixMetadata.getStrand(),
				sourceMatrixMetadata.getHasDictionary(),
				numMarkers,
				numSamples,
				numChromosomes,
				sourceMatrixMetadata.getKey().getMatrixId(), // orig/parent matrix 1 key
				MatrixKey.NULL_ID); // Orig matrixId 2
	}

	@Override
	public String getStrandFlag() {
		return null;
	}

	@Override
	public GenotypeEncoding getGuessedGTCode(MatrixGenotypesTranslatorParams params) {
		return GenotypeEncoding.ACGT0;
	}
}