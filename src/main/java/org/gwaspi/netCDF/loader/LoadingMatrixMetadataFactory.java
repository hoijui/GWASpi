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

package org.gwaspi.netCDF.loader;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.MatrixCreatingOperationParams;
import org.gwaspi.operations.MatrixMetadataFactory;

public class LoadingMatrixMetadataFactory
		implements MatrixMetadataFactory<DataSet, MatrixCreatingOperationParams>
{
	private final GenotypesLoadDescription loadDescription;
	private AbstractLoadGTFromFiles gtLoader; // HACK

	public LoadingMatrixMetadataFactory(GenotypesLoadDescription loadDescription) {

		this.loadDescription = loadDescription;
		this.gtLoader = null;
	}

	public void setGTLoader(AbstractLoadGTFromFiles gtLoader) {
		this.gtLoader = gtLoader;
	}

	@Override
	public MatrixMetadata generateMetadata(DataSet dataSet, MatrixCreatingOperationParams params) throws IOException {

		final int numMarkers = dataSet.getMarkerMetadatas().size();
		final int numSamples = dataSet.getSampleInfos().size();
		final int numChromosomes = dataSet.getChromosomeInfos().size();

		StringBuilder description = new StringBuilder(Text.Matrix.descriptionHeader1);
		description.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		if (!loadDescription.getDescription().isEmpty()) {
			description.append("\nDescription: ");
			description.append(loadDescription.getDescription());
			description.append("\n");
		}
//		description.append("\nStrand: ");
//		description.append(strand);
//		description.append("\nGenotype encoding: ");
//		description.append(gtCode);
		description.append("\n");
		description.append("Markers: ").append(numMarkers);
		description.append(", Samples: ").append(numSamples);
		description.append("\n");
		description.append(Text.Matrix.descriptionHeader2);
		description.append(loadDescription.getFormat().toString());
		description.append("\n");
		description.append(Text.Matrix.descriptionHeader3);
		description.append("\n");
		gtLoader.addAdditionalBigDescriptionProperties(description, loadDescription);
//		if (new File(loadDescription.getSampleFilePath()).exists()) {
//			description.append(loadDescription.getSampleFilePath()); // the FAM file, in case of PLink Binary
//			description.append(" (Sample Info file)\n");
//		}

		return new MatrixMetadata(
				loadDescription.getStudyKey(),
				loadDescription.getFriendlyName(),
				loadDescription.getFormat(),
				description.toString(), // description
				loadDescription.getGtCode(),
				(gtLoader.getMatrixStrand() != null) // NOTE getMatrixStrand() is only used here!
						? gtLoader.getMatrixStrand()
						: loadDescription.getStrand(),
				gtLoader.isHasDictionary(),
				numMarkers,
				numSamples,
				numChromosomes,
				loadDescription.getGtDirPath());
	}

	@Override
	public String getStrandFlag() {

		String strandFlag;
		if (gtLoader.getMetadataLoader().isHasStrandInfo()) { // NOTE ... and here!
			strandFlag = null;
		} else if (gtLoader.getMetadataLoader().getFixedStrandFlag() != null) {
			strandFlag = gtLoader.getMetadataLoader().getFixedStrandFlag().toString();
		} else {
			strandFlag = loadDescription.getStrandFlag(); // NOTE getStrandFlag(...) is only used here!
		}

		return strandFlag;
	}

	@Override
	public GenotypeEncoding getGuessedGTCode(MatrixCreatingOperationParams params) {
		return gtLoader.getGuessedGTCode();
	}
}
