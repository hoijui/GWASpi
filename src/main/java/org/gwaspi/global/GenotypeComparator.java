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
package org.gwaspi.global;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two genotypes, for example {'A', 'A'} with {'A', 'G'}
 */
public class GenotypeComparator implements Comparator<byte[]>, Serializable {

	@Override
	public int compare(byte[] gt1, byte[] gt2) {
		return 256 * (gt1[0] - gt2[0]) + (gt1[1] - gt2[1]);
	}
}
