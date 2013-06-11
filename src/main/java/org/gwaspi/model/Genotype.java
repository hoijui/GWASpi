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
 * Represents a single genotype (father & mother pair),
 * for example <code>{'A', 'G'}</code> or <code>{'A', 'A'}</code>.
 * For the encoding from <code>byte</code> to <code>char</code>,
 * see {@link org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes}.
 */
public class Genotype implements Serializable, Comparable<Genotype> {

	private final byte[] rawGt;

	public Genotype(byte[] rawGt) {
		this.rawGt = rawGt;
	}

	@Override
	public int compareTo(Genotype other) {
		return other.hashCode() - hashCode();
	}

	@Override
	public boolean equals(Object other) {

		if (other instanceof Genotype) {
			return (compareTo((Genotype) other) == 0);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (256 * rawGt[0]) + rawGt[1];
	}

	@Override
	public String toString() {
		return new String(rawGt);
	}
}
