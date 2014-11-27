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

package org.gwaspi.netCDF.exporter;

import java.io.IOException;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;

interface Formatter {

	static final ProgressSource PLACEHOLDER_PS_EXPORT = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_EXPORT", null));

	/**
	 * Exports a given data-set (sample-infos, marker-infos and genotypes) into a certain format.
	 * @param exportPath
	 * @param rdDataSetMetadata
	 * @param dataSetSource
	 * @param phenotype
	 * @throws IOException
	 */
	void export(
			final String exportPath,
			final DataSetMetadata rdDataSetMetadata,
			final DataSetSource dataSetSource,
			final SuperProgressSource superProgressSource,
			final String phenotype)
			throws IOException;
}
