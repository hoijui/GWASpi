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

import java.util.NoSuchElementException;

/**
 * Contains statistical data about a marker within a set of samples.
 */
public class Census {

	private static final class CensusExtractor4 extends AbstractObjectEnumeratedValueExtractor<Census> {

		@Override
		public int getNumberOfValues() {
			return 4;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getAA();
				case 1: return object.getAa();
				case 2: return object.getaa();
				case 3: return object.getMissingCount();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class CensusExtractor3 extends AbstractObjectEnumeratedValueExtractor<Census> {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getAA();
				case 1: return object.getAa();
				case 2: return object.getaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	public static final AbstractObjectEnumeratedValueExtractor<Census> EXTRACTOR_3 = new CensusExtractor3();
	public static final AbstractObjectEnumeratedValueExtractor<Census> EXTRACTOR_4 = new CensusExtractor4();

	private final int AA;
	private final int Aa;
	private final int aa;
	private final int missingCount;

	public Census(
			int AA,
			int Aa,
			int aa,
			int missingCount)
	{
		this.AA = AA;
		this.Aa = Aa;
		this.aa = aa;
		this.missingCount = missingCount;
	}

	/**
	 * @param values {AA, Aa, aa, missingCount} or {AA, Aa, aa}
	 */
	public Census(int[] values) {

		this.AA = values[0];
		this.Aa = values[1];
		this.aa = values[2];
		this.missingCount = (values.length > 3) ? values[3] : -1;
	}

	public Census() {
		this(0, 0, 0, 0);
	}

	public int getAA() {
		return AA;
	}

	public int getAa() {
		return Aa;
	}

	public int getaa() {
		return aa;
	}

	public int getMissingCount() {
		return missingCount;
	}
}
