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


import java.util.Iterator;
import java.util.NoSuchElementException;
import org.gwaspi.global.EnumeratedValueExtractor;

/**
 * Contains statistical data about a marker within a set of samples.
 */
public class Census {
	private static abstract class CensusExtractor implements EnumeratedValueExtractor<Census, Iterator<Integer>> {

		abstract Integer extractIndex(Census object, int extractIndex);

		private final class CensusExtractorIterator implements Iterator<Integer> {

			private final Census object;
			private int nextIndex;

			CensusExtractorIterator(Census object) {
				this.object = object;
				this.nextIndex = 0;
			}

			@Override
			public boolean hasNext() {
				return nextIndex < getNumberOfValues();
			}

			@Override
			public Integer next() {
				return extractIndex(object, nextIndex++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("You may not remove through this Iterator.");
			}
		}

		@Override
		public Iterator<Integer> extract(Census object) {
			return new CensusExtractorIterator(object);
		}
	}

	private static final class AllCensusExtractor extends CensusExtractor {

		@Override
		public int getNumberOfValues() {
			return 4;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getAllAA();
				case 1: return object.getAllAa();
				case 2: return object.getAllaa();
				case 3: return object.getMissingCount();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class CaseCensusExtractor extends CensusExtractor {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getCaseAA();
				case 1: return object.getCaseAa();
				case 2: return object.getCaseaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class ControlCensusExtractor extends CensusExtractor {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getControlAA();
				case 1: return object.getControlAa();
				case 2: return object.getControlaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	private static final class AlternateHWCensusExtractor extends CensusExtractor {

		@Override
		public int getNumberOfValues() {
			return 3;
		}

		@Override
		public Integer extractIndex(Census object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getHwAA();
				case 1: return object.getHwAa();
				case 2: return object.getHwaa();
				default: throw new NoSuchElementException();
			}
		}
	}

	public static final CensusExtractor EXTRACTOR_ALL = new AllCensusExtractor();
	public static final CensusExtractor EXTRACTOR_CASE = new CaseCensusExtractor();
	public static final CensusExtractor EXTRACTOR_CONTROL = new ControlCensusExtractor();
	public static final CensusExtractor EXTRACTOR_ALTERNATE_HW = new AlternateHWCensusExtractor();

	private final int allAA; // all
	private final int allAa; // all
	private final int allaa; // all
	private final int missingCount; // all
	private final int caseAA; // case
	private final int caseAa; // case
	private final int caseaa; // case
	private final int controlAA; // control
	private final int controlAa; // control
	private final int controlaa; // control
	private final int hwAA; // HW samples
	private final int hwAa; // HW samples
	private final int hwaa; // HW samples

	public Census(
			int allAA,
			int allAa,
			int allaa,
			int missingCount,
			int caseAA,
			int caseAa,
			int caseaa,
			int controlAA,
			int controlAa,
			int controlaa,
			int hwAA,
			int hwAa,
			int hwaa)
	{
		this.allAA = allAA;
		this.allAa = allAa;
		this.allaa = allaa;
		this.missingCount = missingCount;
		this.caseAA = caseAA;
		this.caseAa = caseAa;
		this.caseaa = caseaa;
		this.controlAA = controlAA;
		this.controlAa = controlAa;
		this.controlaa = controlaa;
		this.hwAA = hwAA;
		this.hwAa = hwAa;
		this.hwaa = hwaa;
	}

	public Census(
			int allAA,
			int allAa,
			int allaa,
			int missingCount)
	{
		this(allAA, allAa, allaa, missingCount, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public Census() {
		this(0, 0, 0, 0);
	}

	public int getAllAA() {
		return allAA;
	}

	public int getAllAa() {
		return allAa;
	}

	public int getAllaa() {
		return allaa;
	}

	public int getMissingCount() {
		return missingCount;
	}

	public int getCaseAA() {
		return caseAA;
	}

	public int getCaseAa() {
		return caseAa;
	}

	public int getCaseaa() {
		return caseaa;
	}

	public int getControlAA() {
		return controlAA;
	}

	public int getControlAa() {
		return controlAa;
	}

	public int getControlaa() {
		return controlaa;
	}

	public int getHwAA() {
		return hwAA;
	}

	public int getHwAa() {
		return hwAa;
	}

	public int getHwaa() {
		return hwaa;
	}

}
