/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.datasource.filter;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndicesFilteredMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

	private final Map<K, V> wrapped;
	private final List<Integer> includeIndices;

	private class IndicesFilteredMapEntrySet extends AbstractSet<Map.Entry<K, V>> {

		private class IndicesFilteredMapEntrySetIterator implements Iterator<Map.Entry<K, V>> {

			private int currentWrappedIndex;
			private final Iterator<Integer> includeIndicesIt;
			private final Iterator<Map.Entry<K, V>> wrappedIterator;

			IndicesFilteredMapEntrySetIterator() {

				this.currentWrappedIndex = -1;
				this.includeIndicesIt = includeIndices.iterator();
				this.wrappedIterator = wrapped.entrySet().iterator();
			}

			@Override
			public boolean hasNext() {
				return includeIndicesIt.hasNext();
			}

			@Override
			public Entry<K, V> next() {

				Entry<K, V> nextWrapped = null;

				final int nextWrappedIndex = includeIndicesIt.next();
				while (currentWrappedIndex < nextWrappedIndex) {
					nextWrapped = wrappedIterator.next();
					currentWrappedIndex++;
				}

				return nextWrapped;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return new IndicesFilteredMapEntrySetIterator();
		}

		@Override
		public int size() {
			return includeIndices.size();
		}
	}

	public IndicesFilteredMap(final Map<K, V> wrapped, final List<Integer> includeIndices) {

		this.wrapped = wrapped;
		this.includeIndices = includeIndices;
	}

	public static <LK, LV> Map<LK, LV> getWrappedRange(final Map<LK, LV> wrapped, final List<Integer> includeIndices, int from, int to) {
		return new IndicesFilteredMap(wrapped, includeIndices.subList(from, to));
	}

	protected Map<K, V> getWrapped() {
		return wrapped;
	}

	protected List<Integer> getIncludeIndices() {
		return includeIndices;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new IndicesFilteredMapEntrySet();
	}
}
