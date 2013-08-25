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
 * Uniquely identifies a chromosome within a set of chromosomes.
 */
public final class ChromosomeKey implements Comparable<ChromosomeKey>, Serializable {

	private final String chromosome;

	public ChromosomeKey(String chromosome) {

		this.chromosome = chromosome;
	}

	@Override
	public int compareTo(ChromosomeKey other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		boolean equal = false;

		if (other instanceof ChromosomeKey) {
			ChromosomeKey otherMarkerKey = (ChromosomeKey) other;
			equal = getChromosome().equals(otherMarkerKey.getChromosome());
		}

		return equal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.chromosome != null ? this.chromosome.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return getChromosome();
	}

	/**
	 * Allows to parse a chromosome key from a single string value.
	 * @param keyStr should be of the form returned by
	 *   {@link #toString()}
	 * @return the parsed chromosome key
	 */
	public static ChromosomeKey valueOf(String keyStr) {
		return new ChromosomeKey(keyStr);
	}

	public String getChromosome() {
		return chromosome;
	}
}
