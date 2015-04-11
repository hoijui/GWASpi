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
import org.gwaspi.constants.ImportConstants.ImportFormat;

/**
 * Used when loading a data-set from an external source
 * directly into the NetCDF storage format.
 * For example, from a set of PLink files.
 */
public class LoadingDataSetDestination extends ForwardingDataSetDestination {

	private String startTime;
	private final GenotypesLoadDescription loadDescription;

	public LoadingDataSetDestination(
			final AbstractDataSetDestination internalDataSetDestination,
			GenotypesLoadDescription loadDescription)
	{
		super(internalDataSetDestination);

		this.loadDescription = loadDescription;
	}

	@Override
	public void init() throws IOException {
		getInternalDataSetDestination().init();

		startTime = org.gwaspi.global.Utils.getMediumDateTimeAsString();
	}

	@Override
	public void finishedLoadingMarkerMetadatas() throws IOException {
		getInternalDataSetDestination().finishedLoadingMarkerMetadatas();

		((AbstractDataSetDestination) getInternalDataSetDestination()).extractChromosomeInfos();
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {
		getInternalDataSetDestination().finishedLoadingAlleles();

		logAsWhole(
				startTime,
				loadDescription.getStudyKey().getId(),
				loadDescription.getGtDirPath(),
				loadDescription.getFormat(),
				loadDescription.getFriendlyName(),
				loadDescription.getDescription());
	}

	private static void logAsWhole(String startTime, int studyId, String dirPath, ImportFormat format, String matrixName, String description) throws IOException {
		// LOG OPERATION IN STUDY HISTORY
		final StringBuilder operationDesc = new StringBuilder(1024);
		operationDesc
				.append("\nLoaded raw ").append(format).append(" genotype data in path ").append(dirPath).append(".\n")
				.append("Start Time: ").append(startTime).append('\n')
				.append("End Time: ").append(org.gwaspi.global.Utils.getMediumDateTimeAsString()).append('\n')
				.append("Data stored in matrix ").append(matrixName).append('\n')
				.append("Description: ").append(description).append('\n');
		org.gwaspi.global.Utils.logOperationInStudyDesc(operationDesc.toString(), studyId);
	}
}
