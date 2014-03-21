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

package org.gwaspi.netCDF.operations;

public class DefaultOperationTypeInfo implements OperationTypeInfo {

	private final boolean creatingMatrix;
	private final String name;
	private final String description;

	public DefaultOperationTypeInfo(
			boolean creatingMatrix,
			String name,
			String description)
	{
		this.creatingMatrix = creatingMatrix;
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean isCreatingMatrix() {
		return creatingMatrix;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
