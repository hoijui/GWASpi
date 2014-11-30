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

package org.gwaspi.operations.dataextractor;

import java.io.IOException;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixMetadataFactory;

public class MatrixDataExtractorMetadataFactory
		implements MatrixMetadataFactory<DataSet, MatrixDataExtractorParams>
{
	public static final MatrixDataExtractorMetadataFactory SINGLETON
			= new MatrixDataExtractorMetadataFactory();

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MatrixDataExtractorParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata sourceMatrixMetadata
				= MatricesList.getMatrixMetadataById(params.getParent().getMatrixParent());

		StringBuilder markerPickerCriteria = new StringBuilder();
		for (Object value : params.getFullMarkerCriteria()) {
			markerPickerCriteria.append(value.toString());
			markerPickerCriteria.append(",");
		}

		StringBuilder samplePickerCriteria = new StringBuilder();
		for (Object value : params.getFullSampleCriteria()) {
			samplePickerCriteria.append(value.toString());
			samplePickerCriteria.append(",");
		}

		if (numSamples == 0) {
			throw new IllegalStateException("No samples selected for extraction");
		}
		if (numMarkers == 0) {
			throw new IllegalStateException("No markers selected for extraction");
		}

		StringBuilder description = new StringBuilder(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		description.append("\nThrough Matrix extraction from parent Matrix MX: ")
				.append(sourceMatrixMetadata.getMatrixId()).append(" - ")
				.append(sourceMatrixMetadata.getFriendlyName());

		description.append("\nMarker Filter Variable: ");
		String pickPrefix = "All Markers";
		if (params.getMarkerPickCase().toString().contains("EXCLUDE")) {
			pickPrefix = "Exclude by ";
		} else if (params.getMarkerPickCase().toString().contains("INCLUDE")) {
			pickPrefix = "Include by ";
		}
		description.append(pickPrefix).append(params.getMarkerPickerVar().replaceAll("_", " ").toUpperCase());
		if (params.getMarkerCriteriaFile().isFile()) {
			description.append("\nMarker Criteria File: ");
			description.append(params.getMarkerCriteriaFile().getPath());
		} else if (!pickPrefix.equals("All Markers")) {
			description.append("\nMarker Criteria: ");
			description.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length() - 1));
		}

		description.append("\nSample Filter Variable: ");
		pickPrefix = "All Samples";
		if (params.getSamplePickCase().toString().contains("EXCLUDE")) {
			pickPrefix = "Exclude by ";
		} else if (params.getSamplePickCase().toString().contains("INCLUDE")) {
			pickPrefix = "Include by ";
		}
		description.append(pickPrefix).append(params.getSamplePickerVar().replaceAll("_", " ").toUpperCase());
		if (params.getSampleCriteriaFile().isFile()) {
			description.append("\nSample Criteria File: ");
			description.append(params.getSampleCriteriaFile().getPath());
		} else if (!pickPrefix.equals("All Samples")) {
			description.append("\nSample Criteria: ");
			description.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length() - 1));
		}

		if (!params.getMatrixDescription().isEmpty()) {
			description.append("\n\nDescription: ");
			description.append(params.getMatrixDescription());
			description.append("\n");
		}
//		description.append("\nGenotype encoding: ");
//		description.append(rdMatrixMetadata.getGenotypeEncoding());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);

		return new MatrixMetadata(
				sourceMatrixMetadata.getStudyKey(),
				params.getMatrixFriendlyName(),
				sourceMatrixMetadata.getTechnology(),
				description.toString(),
				sourceMatrixMetadata.getGenotypeEncoding(), // matrix genotype encoding from the original matrix
				sourceMatrixMetadata.getStrand(),
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
	public GenotypeEncoding getGuessedGTCode(MatrixDataExtractorParams params) {
		try {
			final MatrixMetadata sourceMatrixMetadata
					= MatricesList.getMatrixMetadataById(params.getParent().getMatrixParent());
			return sourceMatrixMetadata.getGenotypeEncoding();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
