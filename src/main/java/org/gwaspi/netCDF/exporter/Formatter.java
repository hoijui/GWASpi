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

interface Formatter {

	/**
	 *
	 * @param exportPath
	 * @param rdDataSetMetadata
	 * @param dataSetSource
	 * @param phenotype
	 * @return true if formatting and exporting went through successfully, false otherwise
	 * @throws IOException
	 */
	boolean export(
			String exportPath,
			DataSetMetadata rdDataSetMetadata,
			DataSetSource dataSetSource,
			String phenotype)
			throws IOException;
}
