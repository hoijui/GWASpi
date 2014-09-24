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

import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

public interface GenotypesLoader {

	/**
	 * Process Genotypes
	 * @param loadDescription
	 * @param sampleInfos
	 * @param gtsReceiver
	 * @throws java.lang.Exception
	 */
	void processData(
			GenotypesLoadDescription loadDescription,
			Map<SampleKey, SampleInfo> sampleInfos,
			DataSetDestination gtsReceiver)
			throws Exception;

	ImportFormat getFormat();

	/**
	 * @return if <code>null</code>, then we will use
	 *   <code>GenotypesLoadDescription#getStrand()</code> instead
	 */
	StrandType getMatrixStrand();

	boolean isHasDictionary();
}
