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

public abstract class AbstractOperationDataEntry<OK> implements OperationDataEntry<OK> {

	private final OK key;
	private final int index;

	public AbstractOperationDataEntry(OK key, int index) {

		this.key = key;
		this.index = index;
	}

	@Override
	public OK getKey() {
		return key;
	}

	@Override
	public int getIndex() {
		return index;
	}
}
