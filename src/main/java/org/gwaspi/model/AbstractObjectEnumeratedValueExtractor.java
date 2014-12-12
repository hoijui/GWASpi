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
import org.gwaspi.global.EnumeratedValueExtractor;

/**
 * TODO add class description
 */
public abstract class AbstractObjectEnumeratedValueExtractor<V> implements EnumeratedValueExtractor<V, Iterator<Integer>> {

	abstract Integer extractIndex(V object, int extractIndex);

	private final class CensusExtractorIterator implements Iterator<Integer> {

		private final V object;
		private int nextIndex;

		CensusExtractorIterator(V object) {
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
	public Iterator<Integer> extract(V object) {
		return new CensusExtractorIterator(object);
	}
}
