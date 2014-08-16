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
import java.util.AbstractList;
import java.util.List;

/**
 * Represents a view of a list of just one property of each item
 * in the original list.
 * Differences to a list where we actually store the extracted properties:
 * - costs extraction process each time the list is accessed,
 *   instead of only once
 * + uses no additional memory (or just a very small, constant amount)
 * @param <IT> input/original type
 * @param <OT> output/result type
 */
public class ExtractorList<IT, OT> extends AbstractList<OT> implements Serializable {

	private final List<? extends IT> originalItems;
	private final Extractor<IT, OT> extractor;

	public ExtractorList(List<? extends IT> originalItems, Extractor<IT, OT> extractor) {

		this.originalItems = originalItems;
		this.extractor = extractor;
	}

	@Override
	public OT get(int index) {
		return extractor.extract(originalItems.get(index));
	}

	@Override
	public int size() {
		return originalItems.size();
	}
}
