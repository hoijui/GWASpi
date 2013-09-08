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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 * Used when loading a data-set from an external source
 * directly into the NetCDF storage format.
 * For example, from a set of PLink files.
 */
public class LoadingNetCDFDataSetDestination extends AbstractNetCDFDataSetDestination {

	private final Logger log
			= LoggerFactory.getLogger(LoadingNetCDFDataSetDestination.class);

	private String startTime;
	private final GenotypesLoadDescription loadDescription;
	private AbstractLoadGTFromFiles gtLoader; // HACK

	public LoadingNetCDFDataSetDestination(
			GenotypesLoadDescription loadDescription)
	{
		this.loadDescription = loadDescription;
		this.gtLoader = null;
	}

	public void setGTLoader(AbstractLoadGTFromFiles gtLoader) {
		this.gtLoader = gtLoader;
	}

	@Override
	public void init() throws IOException {
		super.init();

		startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	}

	@Override
	protected MatrixFactory createMatrixFactory() throws IOException {

		final int numMarkers = getDataSet().getMarkerMetadatas().size();
		final int numSamples = getDataSet().getSampleInfos().size();
		final int numChromosomes = getDataSet().getChromosomeInfos().size();

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
		if (new File(loadDescription.getSampleFilePath()).exists()) {
			description.append(loadDescription.getSampleFilePath()); // the FAM file, in case of PLink Binary
			description.append(" (Sample Info file)\n");
		}

		try {
			return new MatrixFactory(
					loadDescription.getStudyKey(),
					loadDescription.getFormat(),
					loadDescription.getFriendlyName(),
					description.toString(), // description
					loadDescription.getGtCode(),
					(gtLoader.getMatrixStrand() != null) // NOTE getMatrixStrand() is only used here!
							? gtLoader.getMatrixStrand()
							: loadDescription.getStrand(),
					gtLoader.isHasDictionary(),
					numSamples,
					numMarkers,
					numChromosomes,
					loadDescription.getGtDirPath());
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	protected String getStrandFlag() {

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
	public void finishedLoadingMarkerMetadatas() throws IOException {
		super.finishedLoadingMarkerMetadatas();
		
		extractChromosomeInfos();
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
		super.finishedLoadingAlleles();

		logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());
	}

	@Override
	protected GenotypeEncoding getGuessedGTCode() {
		return gtLoader.getGuessedGTCode();
	}

	private static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		StringBuilder operation = new StringBuilder("\nLoaded raw " + format + " genotype data in path " + dirPath + ".\n");
		operation.append("Start Time: ").append(startTime).append("\n");
		operation.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append(".\n");
		operation.append("Data stored in matrix ").append(matrixName).append(".\n");
		operation.append("Description: ").append(description).append(".\n");
		org.gwaspi.global.Utils.logOperationInStudyDesc(operation.toString(), studyId);
	}

	static List<SampleKey> extractKeys(Collection<SampleInfo> sampleInfos) {

		List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());

		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(sampleInfo.getKey());
		}

		return sampleKeys;
	}
}