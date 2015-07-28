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
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.dao.MatrixService;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixMetadataFactory;

public class MergeMatrixMetadataFactory implements MatrixMetadataFactory<DataSet, MergeMatrixOperationParams> {

	public static final MergeMatrixMetadataFactory SINGLETON = new MergeMatrixMetadataFactory();

	private MatrixService getMatrixService() {
		return MatricesList.getMatrixService();
	}

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MergeMatrixOperationParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata rdMatrix1Metadata = getMatrixService().getMatrix(
				params.getParent().getMatrixParent());
		final MatrixMetadata rdMatrix2Metadata = getMatrixService().getMatrix(
				params.getSource2().getMatrixParent());

		final boolean hasDictionary = (rdMatrix1Metadata.getHasDictionary() && rdMatrix2Metadata.getHasDictionary());
		final GenotypeEncoding gtEncoding = getGuessedGTCode(params);
		ImportFormat technology = ImportFormat.UNKNOWN;
		if (rdMatrix1Metadata.getTechnology().equals(rdMatrix2Metadata.getTechnology())) {
			technology = rdMatrix1Metadata.getTechnology();
		}

		final StringBuilder description = new StringBuilder(1024);
		description
				.append(params.getMatrixDescription())
				.append("\n\n")
				.append(Text.Matrix.descriptionHeader1)
				.append(org.gwaspi.global.Utils.getShortDateTimeAsString())
				.append('\n')
				.append("Markers: ").append(numMarkers)
				.append(", Samples: ").append(numSamples)
				.append('\n')
				.append(Text.Trafo.mergedFrom)
				.append("\nMX-")
				.append(params.getParent().getMatrixParent().getMatrixId())
				.append(" - ")
				.append(rdMatrix1Metadata.getFriendlyName())
				.append("\nMX-")
				.append(params.getSource2().getMatrixParent().getMatrixId())
				.append(" - ")
				.append(rdMatrix2Metadata.getFriendlyName())
				.append("\n\n")
				.append("Merge Method - ")
				.append(params.getHumanReadableMethodName())
				.append(":\n")
				.append(params.getMethodDescription())
				.append("\nGenotype encoding: ")
				.append(gtEncoding.toString());

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
			final MatrixMetadata rdMatrix1Metadata = getMatrixService().getMatrix(
					params.getParent().getMatrixParent());
			final MatrixMetadata rdMatrix2Metadata = getMatrixService().getMatrix(
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
