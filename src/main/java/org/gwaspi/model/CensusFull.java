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
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;

/**
 * Contains statistical data about a marker within a set of samples.
 */
public class CensusFull implements Serializable {

	private static final class AllCensusExtractor extends AbstractObjectEnumeratedValueExtractor<CensusFull> {

		@Override
		public int getNumberOfValues() {
			return 4;
		}

		@Override
		public Integer extractIndex(CensusFull object, int extractIndex) {

			Census census = object.getCategoryCensus().get(Category.ALL);
			switch (extractIndex) {
				case 0: return census.getAA();
				case 1: return census.getAa();
				case 2: return census.getaa();
				case 3: return census.getMissingCount();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class CaseCensusExtractor extends AbstractObjectEnumeratedValueExtractor<CensusFull> {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(CensusFull object, int extractIndex) {

			Census census = object.getCategoryCensus().get(Category.CASE);
			switch (extractIndex) {
				case 0: return census.getAA();
				case 1: return census.getAa();
				case 2: return census.getaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class ControlCensusExtractor extends AbstractObjectEnumeratedValueExtractor<CensusFull> {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(CensusFull object, int extractIndex) {

			Census census = object.getCategoryCensus().get(Category.CONTROL);
			switch (extractIndex) {
				case 0: return census.getAA();
				case 1: return census.getAa();
				case 2: return census.getaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class AlternateHWCensusExtractor extends AbstractObjectEnumeratedValueExtractor<CensusFull> {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(CensusFull object, int extractIndex) {

			Census census = object.getCategoryCensus().get(Category.ALTERNATE);
			switch (extractIndex) {
				case 0: return census.getAA();
				case 1: return census.getAa();
				case 2: return census.getaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	public static final AbstractObjectEnumeratedValueExtractor<CensusFull> EXTRACTOR_ALL = new AllCensusExtractor();
	public static final AbstractObjectEnumeratedValueExtractor<CensusFull> EXTRACTOR_CASE = new CaseCensusExtractor();
	public static final AbstractObjectEnumeratedValueExtractor<CensusFull> EXTRACTOR_CONTROL = new ControlCensusExtractor();
	public static final AbstractObjectEnumeratedValueExtractor<CensusFull> EXTRACTOR_ALTERNATE_HW = new AlternateHWCensusExtractor();

	private final Map<Category, Census> categoryCensus;

	public CensusFull(
			Census censusAll,
			Census censusCase,
			Census censusControl,
			Census censusHardyWeinberg)
	{
		this.categoryCensus = new EnumMap<Category, Census>(Category.class);
		this.categoryCensus.put(Category.ALL, censusAll);
		this.categoryCensus.put(Category.CASE, censusCase);
		this.categoryCensus.put(Category.CONTROL, censusControl);
		this.categoryCensus.put(Category.ALTERNATE, censusHardyWeinberg);
	}

	public CensusFull() {
		this(new Census(), new Census(), new Census(), new Census());
	}

	public Map<Category, Census> getCategoryCensus() {
		return categoryCensus;
	}
}
