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

import java.io.Serializable;

/**
 * Identifies an object, either by in (number) or name (string).
 */
public class IdNameKey implements Comparable<IdNameKey>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int NULL_ID = -1; // alternatively: Integer.MIN_VALUE

	private final int id;
	private final String name;

	public IdNameKey(int id) {
		this.id = id;
		this.name = null;
	}

	public IdNameKey(String name) {
		this.id = NULL_ID;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isSpecifiedById() {
		return (id != NULL_ID);
	}

	public boolean isSpecifiedByName() {
		return name != null;
	}

	@Override
	public String toString() {

		final StringBuilder strRep = new StringBuilder(getClass().getSimpleName());
		strRep.append('[');
		if (isSpecifiedById()) {
			strRep.append("id: ").append(getId());
		} else {
			strRep.append("name: ").append(getName());
		}
		strRep.append(']');

		return strRep.toString();
	}

	@Override
	public boolean equals(Object other) {

		if (other == null) {
			return false;
		} else if (!(other instanceof IdNameKey)) {
			return false;
		} else {
			final IdNameKey otherKey = (IdNameKey) other;
			if (isSpecifiedById() != otherKey.isSpecifiedById()) {
				return false;
			} else if (isSpecifiedById()) {
				return getId() == otherKey.getId();
			} else {
				return getName().equals(otherKey.getName());
			}
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + this.id;
		hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(IdNameKey other) {

		if (other == null) {
			return -1;
		} else if (isSpecifiedById() != other.isSpecifiedById()) {
			return -1;
		} else if (isSpecifiedById()) {
			return getId() - other.getId();
		} else {
			return getName().compareTo(other.getName());
		}
	}
}
