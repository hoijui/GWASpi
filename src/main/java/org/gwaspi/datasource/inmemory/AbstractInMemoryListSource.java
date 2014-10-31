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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.List;
import org.gwaspi.datasource.AbstractListSource;
import org.gwaspi.global.Extractor;
import org.gwaspi.global.ExtractorList;
import org.gwaspi.model.MatrixKey;

/**
 * TODO add class description
 * @param <VT> list value type
 */
public abstract class AbstractInMemoryListSource<VT> extends AbstractListSource<VT> {

	private final List<VT> items;

	protected AbstractInMemoryListSource(MatrixKey origin, List<VT> items, List<Integer> originalIndices) {
		super(origin, Integer.MAX_VALUE, originalIndices);

		this.items = items;
	}

	protected AbstractInMemoryListSource(MatrixKey origin, final List<VT> items) {
		this(origin, items, null);
	}

	protected List<VT> getItems() {
		return items;
	}

	public static <OT, IT> List<OT> extractProperty(List<? extends IT> items, Extractor<IT, OT> propertyExtractor) {

//		final List<OT> extractedProperties = new ArrayList<OT>(items.size());
//		for (IT item : items) {
//			extractedProperties.add(propertyExtractor.extract(item));
//		}
//
//		return extractedProperties;
		return new ExtractorList<IT, OT>(items, propertyExtractor);
	}

	@Override
	public List<VT> getRange(int from, int to) throws IOException {
		return getItems().subList(from, to + 1);
	}

	@Override
	public int sizeInternal() {

		if (getOriginalIndicesRaw() == null) {
			return items.size();
		} else {
			try {
				return getOrigSource().size();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
