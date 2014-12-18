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

package org.gwaspi.operations;

import java.io.Serializable;
import org.gwaspi.global.Extractor;

/**
 * TODO
 *
 * @param <K> operation key type, this is the main/primary key type
 */
public interface OperationDataEntry<K> extends Serializable {

	class KeyExtractor<K>
			implements Extractor<OperationDataEntry<K>, K>
	{
		@Override
		public K extract(OperationDataEntry<K> from) {
			return from.getKey();
		}
	};

	class IndexExtractor
			implements Extractor<OperationDataEntry, Integer>
	{
		@Override
		public Integer extract(OperationDataEntry from) {
			return from.getIndex();
		}
	};
	Extractor<OperationDataEntry, Integer> TO_INDEX = new IndexExtractor();

	/**
	 * @return the (operation/main-)key of this data entry
	 */
	K getKey();

	/**
	 * @return the index of this entry in the original data-set
	 */
	int getIndex();
}
