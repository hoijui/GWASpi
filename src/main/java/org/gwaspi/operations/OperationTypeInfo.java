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

package org.gwaspi.operations;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;

/**
 * Contains info about one type of operation.
 * This information is static for the operation type,
 * meaning that all operations instances of this type share this info,
 * even if they were invoked with different parameters.
 */
public interface OperationTypeInfo {

	/**
	 * Returns whether this creates a matrix or an operation as result.
	 * @return <code>true</code> if this creates a matrix,
	 *   <code>true</code> if this creates an operation
	 */
	boolean isCreatingMatrix();

	/**
	 * Returns whether this operation stores its data by using the markers
	 * as the main index.
	 * Both this and {@link #isSamplesOriented()} may return <code>false</code>.
	 * NOTE This method only makes sense for operation creating operations,
	 *   not for matrix creating ones.
	 * @return true, if the output of this operation "appends" data to markers.
	 */
	boolean isMarkersOriented();

	/**
	 * Returns whether this operation stores its data by using the samples
	 * as the main index.
	 * Both this and {@link #isMarkersOriented()} may return <code>false</code>.
	 * NOTE This method only makes sense for operation creating operations,
	 *   not for matrix creating ones.
	 * @return true, if the output of this operation "appends" data to samples.
	 */
	boolean isSamplesOriented();

	/**
	 * Returns a human readable, short explanation of what this operation does.
	 * @return a human readable, short explanation of what this operation does
	 * @see #getDescription()
	 */
	String getName();

	/**
	 * Returns a human readable, long explanation of what this operation does.
	 * @return a human readable, long explanation of what this operation does
	 * @see #getName()
	 */
	String getDescription();

	/**
	 * @deprecated
	 */
	OPType getType();
}
