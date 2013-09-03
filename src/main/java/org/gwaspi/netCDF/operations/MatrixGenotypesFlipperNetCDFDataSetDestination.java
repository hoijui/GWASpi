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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 * Used when loading a data-set from an external source
 * directly into the NetCDF storage format.
 * For example, from a set of PLink files.
 */
public class MatrixGenotypesFlipperNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final Logger log
			= LoggerFactory.getLogger(MatrixGenotypesFlipperNetCDFDataSetDestination.class);

	private final DataSetSource dataSetSource;
	private final MatrixKey matrixKey;
	private final String matrixDescription;
	private final String matrixFriendlyName;
	private final File flipperFile;

	public MatrixGenotypesFlipperNetCDFDataSetDestination(
			DataSetSource dataSetSource,
			MatrixKey matrixKey,
			String matrixDescription,
			String matrixFriendlyName,
			File flipperFile)
	{
		this.dataSetSource = dataSetSource;
		this.matrixKey = matrixKey;
		this.matrixDescription = matrixDescription;
		this.matrixFriendlyName = matrixFriendlyName;
		this.flipperFile = flipperFile;
	}

	@Override
	protected MatrixFactory createMatrixFactory() throws IOException {

			StringBuilder description = new StringBuilder();
			description.append(Text.Matrix.descriptionHeader1);
			description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			description.append("\nThrough Matrix genotype flipping from parent Matrix MX: ").append(dataSetSource.getMatrixMetadata().getMatrixId());
			description.append(" - ").append(dataSetSource.getMatrixMetadata().getMatrixFriendlyName());
			description.append("\nUsed list of markers to be flipped: ").append(flipperFile.getPath());
			if (!matrixDescription.isEmpty()) {
				description.append("\n\nDescription: ");
				description.append(matrixDescription);
				description.append("\n");
			}
			description.append("\nGenotype encoding: ");
			description.append(dataSetSource.getMatrixMetadata().getGenotypeEncoding());
			description.append("\n");
			description.append("Markers: ").append(dataSetSource.getMarkersKeysSource().size());
			description.append(", Samples: ").append(dataSetSource.getSamplesKeysSource().size());

		try {
			return new MatrixFactory(
					dataSetSource.getMatrixMetadata().getTechnology(), // technology
					matrixFriendlyName,
					description.toString(), // description
					dataSetSource.getMatrixMetadata().getGenotypeEncoding(), // matrix genotype encoding from the original matrix
					StrandType.valueOf("FLP"), // FIXME this will fail at runtime
					dataSetSource.getMatrixMetadata().getHasDictionray(), // has dictionary?
					dataSetSource.getSamplesKeysSource().size(),
					dataSetSource.getMarkersKeysSource().size(),
					dataSetSource.getMarkersChromosomeInfosSource().size(),
					matrixKey, // orig/parent matrix 1 key
					null); // orig/parent matrix 2 key
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	protected String getStrandFlag() {
		return null;
	}

	@Override
	protected String getGuessedGTCode() {
		return dataSetSource.getMatrixMetadata().getGenotypeEncoding().toString();
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}
