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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.StudyKey;
import ucar.ma2.InvalidRangeException;

public interface GenotypesLoader /*extends Iterable<Map.Entry<MarkerKey, byte[]>>*/ {


	/**
	 * Process Genotypes
	 */
	void processData(GenotypesLoadDescription loadDescription, SamplesReceiver samplesReceiver) throws Exception;

	ImportFormat getFormat();

	/**
	 * @return if <code>null</code>, then we will use
	 *   <code>GenotypesLoadDescription#getStrand()</code> instead
	 */
	StrandType getMatrixStrand();

	boolean isHasDictionary();

	String getMarkersD2Variables();
}
