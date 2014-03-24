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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.gwaspi.operations.NetCdfUtils;

/**
 * A simple, ArrayList based implementation of GenotypesList.
 */
public class ArrayGenotypesList extends ArrayList<byte[]> implements GenotypesList {

	public static final GenotypesListFactory FACTORY = new GenotypesListFactory() {
		@Override
		public GenotypesList extract(List<byte[]> rawGenotypes) {

			// HACK We should/could probably get this list from the QA report, instead of generating it here
			Set<byte[]> possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(rawGenotypes);
			return new ArrayGenotypesList(rawGenotypes, possibleGenotypes);
		}
	};
	private final Set<byte[]> possibleGenotypes;

	public ArrayGenotypesList(Collection<byte[]> originalGenotypes,
			Set<byte[]> possibleGenotypes)
	{
		super(originalGenotypes);

		this.possibleGenotypes = Collections.unmodifiableSet(new LinkedHashSet<byte[]>(possibleGenotypes));
	}

	@Override
	public Set<byte[]> getPossibleGenotypes() {
		return possibleGenotypes;
	}
}
