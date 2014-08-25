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
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixMetadataFactory;

public class MergeMatrixMetadataFactory implements MatrixMetadataFactory<DataSet, MergeMatrixOperationParams> {

	public static final MergeMatrixMetadataFactory SINGLETON = new MergeMatrixMetadataFactory();

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MergeMatrixOperationParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata rdMatrix1Metadata = MatricesList.getMatrixMetadataById(
				params.getParent().getMatrixParent());
		final MatrixMetadata rdMatrix2Metadata = MatricesList.getMatrixMetadataById(
				params.getSource2().getMatrixParent());

		final boolean hasDictionary = (rdMatrix1Metadata.getHasDictionary() && rdMatrix2Metadata.getHasDictionary());
		final GenotypeEncoding gtEncoding = getGuessedGTCode(params);
		ImportFormat technology = ImportFormat.UNKNOWN;
		if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
			technology = rdMatrix1Metadata.getTechnology();
		}

		StringBuilder description = new StringBuilder();
		description.append(params.getMatrixDescription());
		description.append("\n\n");
		description.append(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);
		description.append("\n");
		description.append(Text.Trafo.mergedFrom);
		description.append("\nMX-");
		description.append(params.getParent().getMatrixParent().getMatrixId());
		description.append(" - ");
		description.append(rdMatrix1Metadata.getFriendlyName());
		description.append("\nMX-");
		description.append(params.getSource2().getMatrixParent().getMatrixId());
		description.append(" - ");
		description.append(rdMatrix2Metadata.getFriendlyName());
		description.append("\n\n");
		description.append("Merge Method - ");
		description.append(params.getHumanReadableMethodName());
		description.append(":\n");
		description.append(params.getMethodDescription());
		description.append("\nGenotype encoding: ");
		description.append(gtEncoding.toString());

		return new MatrixMetadata(
				params.getParent().getMatrixParent().getStudyKey(),
				params.getMatrixFriendlyName(),
				technology,
				description.toString(),
				gtEncoding,
				rdMatrix1Metadata.getStrand(),
				hasDictionary,
				numMarkers,
				numSamples,
				numChromosomes,
				params.getParent().getMatrixParent().getMatrixId(), // Parent matrix 1 key
				params.getSource2().getMatrixParent().getMatrixId()); // Parent matrix 2 key
	}

	@Override
	public String getStrandFlag() {
		return null;
	}

	@Override
	public GenotypeEncoding getGuessedGTCode(MergeMatrixOperationParams params) {

		try {
			final MatrixMetadata rdMatrix1Metadata = MatricesList.getMatrixMetadataById(
					params.getParent().getMatrixParent());
			final MatrixMetadata rdMatrix2Metadata = MatricesList.getMatrixMetadataById(
					params.getSource2().getMatrixParent());

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
