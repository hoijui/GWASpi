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

import java.util.AbstractList;
import java.util.List;
import org.gwaspi.model.CompactGenotypesList.SelectiveIndicesGenotypesListFactory;
import org.gwaspi.model.GenotypesList;

public class InternalIndicesFilteredGenotypesListSource extends AbstractList<GenotypesList> {

	private final List<GenotypesList> wrapped;
	private final List<Integer> includeInternalIndices;
	private final SelectiveIndicesGenotypesListFactory genotypesListFactory;

	public InternalIndicesFilteredGenotypesListSource(final List<GenotypesList> wrapped, final List<Integer> includeInternalIndices) {

		this.wrapped = wrapped;
		this.includeInternalIndices = includeInternalIndices;
		this.genotypesListFactory = new SelectiveIndicesGenotypesListFactory(includeInternalIndices);
	}

	protected List<GenotypesList> getWrapped() {
		return wrapped;
	}

	protected List<Integer> getIncludeInternalIndices() {
		return includeInternalIndices;
	}

	@Override
	public GenotypesList get(int index) {
try {
		return genotypesListFactory.extract(wrapped.get(index));
} catch (Throwable t) {
	t.printStackTrace();
	throw new RuntimeException(t);
}
	}

	@Override
	public int size() {
		return includeInternalIndices.size();
	}
}
