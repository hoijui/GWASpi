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

import java.io.File;
import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;

public class MatrixDataExtractorNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final DataSetSource dataSetSource;
	private final String matrixDescription;
	private final String matrixFriendlyName;
	private final File markerCriteriaFile;
	private final File sampleCriteriaFile;
	private final SetMarkerPickCase markerPickCase;
	private final String markerPickerVar;
	private final SetSamplePickCase samplePickCase;
	private final String samplePickerVar;

	/**
	 * HACK This field should not exist.
	 */
	private MatrixDataExtractor matrixDataExtractor = null;

	public MatrixDataExtractorNetCDFDataSetDestination(
			DataSetSource dataSetSource,
			String matrixDescription,
			String matrixFriendlyName,
			File markerCriteriaFile,
			File sampleCriteriaFile,
			SetMarkerPickCase markerPickCase,
			String markerPickerVar,
			SetSamplePickCase samplePickCase,
			String samplePickerVar)
	{
		this.dataSetSource = dataSetSource;
		this.matrixDescription = matrixDescription;
		this.matrixFriendlyName = matrixFriendlyName;
		this.markerCriteriaFile = markerCriteriaFile;
		this.sampleCriteriaFile = sampleCriteriaFile;
		this.markerPickCase = markerPickCase;
		this.markerPickerVar = markerPickerVar;
		this.samplePickCase = samplePickCase;
		this.samplePickerVar = samplePickerVar;
	}

	@Override
	protected MatrixMetadata createMatrixMetadata() throws IOException {

//		wrSampleSetMap.size();
//		wrMarkerKeys.size()
//		rdChromosomeInfo.size();
		final int numMarkers = getDataSet().getMarkerMetadatas().size();
		final int numSamples = getDataSet().getSampleInfos().size();
		final int numChromosomes = getDataSet().getChromosomeInfos().size();

		MatrixMetadata sourceMatrixMetadata = dataSetSource.getMatrixMetadata();

		StringBuilder markerPickerCriteria = new StringBuilder();
		for (Object value : matrixDataExtractor.getFullMarkerCriteria()) {
			markerPickerCriteria.append(value.toString());
			markerPickerCriteria.append(",");
		}

		StringBuilder samplePickerCriteria = new StringBuilder();
		for (Object value : matrixDataExtractor.getFullSampleCriteria()) {
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
		description.append("\nThrough Matrix extraction from parent Matrix MX: ").append(sourceMatrixMetadata.getMatrixId()).append(" - ").append(sourceMatrixMetadata.getFriendlyName());

		description.append("\nMarker Filter Variable: ");
		String pickPrefix = "All Markers";
		if (markerPickCase.toString().contains("EXCLUDE")) {
			pickPrefix = "Exclude by ";
		} else if (markerPickCase.toString().contains("INCLUDE")) {
			pickPrefix = "Include by ";
		}
		description.append(pickPrefix).append(markerPickerVar.replaceAll("_", " ").toUpperCase());
		if (markerCriteriaFile.isFile()) {
			description.append("\nMarker Criteria File: ");
			description.append(markerCriteriaFile.getPath());
		} else if (!pickPrefix.equals("All Markers")) {
			description.append("\nMarker Criteria: ");
			description.append(markerPickerCriteria.deleteCharAt(markerPickerCriteria.length() - 1));
		}

		description.append("\nSample Filter Variable: ");
		pickPrefix = "All Samples";
		if (samplePickCase.toString().contains("EXCLUDE")) {
			pickPrefix = "Exclude by ";
		} else if (samplePickCase.toString().contains("INCLUDE")) {
			pickPrefix = "Include by ";
		}
		description.append(pickPrefix).append(samplePickerVar.replaceAll("_", " ").toUpperCase());
		if (sampleCriteriaFile.isFile()) {
			description.append("\nSample Criteria File: ");
			description.append(sampleCriteriaFile.getPath());
		} else if (!pickPrefix.equals("All Samples")) {
			description.append("\nSample Criteria: ");
			description.append(samplePickerCriteria.deleteCharAt(samplePickerCriteria.length() - 1));
		}

		if (!matrixDescription.isEmpty()) {
			description.append("\n\nDescription: ");
			description.append(matrixDescription);
			description.append("\n");
		}
//		description.append("\nGenotype encoding: ");
//		description.append(rdMatrixMetadata.getGenotypeEncoding());
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);

		return new MatrixMetadata(
				sourceMatrixMetadata.getStudyKey(),
				matrixFriendlyName,
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

	/**
	 * HACK This method should not exist.
	 */
	public void setMatrixDataExtractor(MatrixDataExtractor matrixDataExtractor) {
		this.matrixDataExtractor = matrixDataExtractor;
	}
}
