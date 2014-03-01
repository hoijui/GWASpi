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
 * Meta info about a chromosome.
 */
public class ChromosomeInfo {

	private static final class AllChromosomeInfoExtractor extends AbstractObjectEnumeratedValueExtractor<ChromosomeInfo> {

		@Override
		public int getNumberOfValues() {
			return 4;
		}

		@Override
		public Integer extractIndex(ChromosomeInfo object, int extractIndex) {
			switch (extractIndex) {
				case 0: return object.getMarkerCount();
				case 1: return object.getFirstPos();
				case 2: return object.getPos();
				case 3: return object.getIndex();
				default: throw new NoSuchElementException();
			}
		}
	}

	public static final AbstractObjectEnumeratedValueExtractor<ChromosomeInfo>
			EXTRACTOR = new AllChromosomeInfoExtractor();

	/** How many markers in current chromosome. */
	private final int markerCount;
	/** First physical position in chromosome. */
	private final int firstPos;
	/** Last physical position in current chromosome. */
	private final int pos;
	/** Last set index for current chromosome. */
	private final int index;

	public ChromosomeInfo(int markerCount, int firstPos, int pos, int index) {

		this.markerCount = markerCount;
		this.firstPos = firstPos;
		this.pos = pos;
		this.index = index;
	}

	public ChromosomeInfo() {
		this(0, 0, 0, 0);
	}

	/**
	 * How many markers in current chromosome.
	 * @return the markerCount
	 */
	public int getMarkerCount() {
		return markerCount;
	}

	/**
	 * First physical position in chromosome.
	 * @return the firstPos
	 */
	public int getFirstPos() {
		return firstPos;
	}

	/**
	 * Last physical position in the current chromosome.
	 * @return the position
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * Last set index for current chromosome.
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
}
