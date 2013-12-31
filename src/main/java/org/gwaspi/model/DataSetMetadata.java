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

package org.gwaspi.model;

import java.util.Date;

public interface DataSetMetadata {

	boolean isOrigin();

	MatrixKey getOrigin();

	DataSetKey getParent();

	DataSetKey getDataSetKey();

	StudyKey getStudyKey();

	/**
	 * A human eye friendly name.
	 * @return a string matching with any characters
	 */
	String getFriendlyName();

	/**
	 * A simple, (generally) unique machine friendly name for this operation,
	 * to be used for storage file names, for example.
	 * @return a string matching "[0-9a-zA-Z_:.]+"
	 */
	String getSimpleName();

	String getDescription();

	int getNumMarkers();

	int getNumSamples();

	int getNumChromosomes();

	Date getCreationDate();
}
