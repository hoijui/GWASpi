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

import org.gwaspi.constants.cNetCDF.Defaults.StrandType;

public interface MetadataLoader {

	/**
	 * Indicates whether each marker may have a separate strand info,
	 * or all of them share the same.
	 * If false, then a global one has to be provided.
	 * If true, then the MarkerMetadata has to have strand info set;
	 * the ctor to set it has to be used, or a setter, if one exists.
	 * @return true if each marker may have a separate strand info
	 */
	boolean isHasStrandInfo();

	/**
	 * @return the strand that is always used for markers in this format,
	 *   or <code>null</code>, if the format is used with different
	 *   strand flags.
	 */
	StrandType getFixedStrandFlag();

	void loadMarkers(DataSetDestination samplesReceiver, GenotypesLoadDescription loadDescription) throws Exception;
}
