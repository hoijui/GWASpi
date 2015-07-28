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
import org.gwaspi.dao.MatrixService;
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

	private MatrixService getMatrixService() {
		return MatricesList.getMatrixService();
	}

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MatrixDataExtractorParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		final MatrixMetadata sourceMatrixMetadata
				= getMatrixService().getMatrix(params.getParent().getMatrixParent());

		final StringBuilder markerPickerCriteria = new StringBuilder();
		for (Object value : params.getFullMarkerCriteria()) {
			markerPickerCriteria
					.append(value.toString())
					.append(',');
		}

		final StringBuilder samplePickerCriteria = new StringBuilder();
		for (Object value : params.getFullSampleCriteria()) {
			samplePickerCriteria
					.append(value.toString())
					.append(',');
		}

		if (numSamples == 0) {
			throw new IllegalStateException("No samples selected for extraction");
		}
		if (numMarkers == 0) {
			throw new IllegalStateException("No markers selected for extraction");
		}

		final StringBuilder description = new StringBuilder(1024);
		description
				.append(Text.Matrix.descriptionHeader1)
				.append(org.gwaspi.global.Utils.getShortDateTimeAsString())
				.append("\nThrough Matrix extraction from parent Matrix MX: ")
				.append(sourceMatrixMetadata.getMatrixId()).append(" - ")
				.append(sourceMatrixMetadata.getFriendlyName())
				.append("\nMarker Filter Variable: ");
		final String markerPickPrefix;
		if (params.getMarkerPickCase().toString().contains("EXCLUDE")) {
			markerPickPrefix = "Exclude by ";
		} else if (params.getMarkerPickCase().toString().contains("INCLUDE")) {
			markerPickPrefix = "Include by ";
		} else {
			markerPickPrefix = "All Markers";
		}
		description.append(markerPickPrefix).append(params.getMarkerPickerVar().replaceAll("_", " ").toUpperCase());
		if (params.getMarkerCriteriaFile().isFile()) {
			description
					.append("\nMarker Criteria File: ")
					.append(params.getMarkerCriteriaFile().getPath());
		} else if (!markerPickPrefix.equals("All Markers")) {
			description
					.append("\nMarker Criteria: ")
					.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length() - 1));
		}

		description.append("\nSample Filter Variable: ");
		final String samplePickPrefix;
		if (params.getSamplePickCase().toString().contains("EXCLUDE")) {
			samplePickPrefix = "Exclude by ";
		} else if (params.getSamplePickCase().toString().contains("INCLUDE")) {
			samplePickPrefix = "Include by ";
		} else {
			samplePickPrefix = "All Samples";
		}
		description.append(samplePickPrefix).append(params.getSamplePickerVar().replaceAll("_", " ").toUpperCase());
		if (params.getSampleCriteriaFile().isFile()) {
			description
					.append("\nSample Criteria File: ")
					.append(params.getSampleCriteriaFile().getPath());
		} else if (!samplePickPrefix.equals("All Samples")) {
			description
					.append("\nSample Criteria: ")
					.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length() - 1));
		}

		if (!params.getMatrixDescription().isEmpty()) {
			description
					.append("\n\nDescription: ")
					.append(params.getMatrixDescription())
					.append('\n');
		}
//		description.append("\nGenotype encoding: ");
//		description.append(rdMatrixMetadata.getGenotypeEncoding());
		description
				.append('\n')
				.append("Markers: ").append(numMarkers)
				.append(", Samples: ").append(numSamples);

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
					= getMatrixService().getMatrix(params.getParent().getMatrixParent());
			return sourceMatrixMetadata.getGenotypeEncoding();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
