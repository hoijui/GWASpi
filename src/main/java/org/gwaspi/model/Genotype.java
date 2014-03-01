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

	public static final Genotype INVALID = new Genotype(new byte[] {'0', '0'});

	private final byte[] rawGt;

	public Genotype(byte[] rawGt) {
		this.rawGt = rawGt;
	}

	@Override
	public int compareTo(Genotype other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		if (other instanceof Genotype) {
			return (compareTo((Genotype) other) == 0);
		}

		return false;
	}

	public static int hashCode(byte[] rawGt) {
		return hashCode(rawGt[0], rawGt[1]);
	}

	public static int hashCode(final byte fatherAllele, final byte motherAllele) {
		return (256 * fatherAllele) + motherAllele;
	}

	public static byte[] unhash(int gtHash) {

		byte[] rawGt = new byte[2];

		rawGt[0] = (byte) (gtHash / 256);
		rawGt[1] = (byte) (gtHash % 256);

		return rawGt;
	}

	@Override
	public int hashCode() {
		return hashCode(rawGt);
	}

	@Override
	public String toString() {
		return new String(rawGt);
	}

	public byte getFather() {
		return rawGt[0];
	}

	public byte getMother() {
		return rawGt[1];
	}

	public static Byte getFather(byte[] rawGt) {
		return rawGt[0];
	}

	public static Byte getMother(byte[] rawGt) {
		return rawGt[1];
	}
}
