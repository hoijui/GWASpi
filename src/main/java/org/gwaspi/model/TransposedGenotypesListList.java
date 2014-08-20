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

package org.gwaspi.model;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.gwaspi.operations.NetCdfUtils;

/**
 * A <code>List<GenotypesList></code> representing a transposed view
 * of an other <code>List<GenotypesList></code>.
 * This is very similar to the matrix transposition in math (<math>M^\top</math>).
 */
public class TransposedGenotypesListList extends ArrayList<GenotypesList> {

	private final List<GenotypesList> originalView;

	private class InnerTransposedGenotypesList extends AbstractList<byte[]> implements GenotypesList {

		private final int index;
		private Set<byte[]> possibleGenotypes;

		InnerTransposedGenotypesList(final int index) {

			this.index = index;
			this.possibleGenotypes = null;
		}

		@Override
		public byte[] get(int innerIndex) {
			return originalView.get(innerIndex).get(index);
		}

		@Override
		public int size() {
			return originalView.size();
		}

		@Override
		public Set<byte[]> getPossibleGenotypes() {

			if (possibleGenotypes == null) {
				final Set<byte[]> tmpPossibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(this);
				possibleGenotypes = Collections.unmodifiableSet(new LinkedHashSet<byte[]>(tmpPossibleGenotypes));
			}

			return possibleGenotypes;
		}
	}

	public TransposedGenotypesListList(final List<GenotypesList> originalView) {
		super(originalView.get(0).size());

		this.originalView = originalView;

		final int size = originalView.get(0).size();
		for (int i = 0; i < size; i++) {
			add(new InnerTransposedGenotypesList(i));
		}
	}
}
