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

package org.gwaspi.operations.markercensus;

import java.io.IOException;
import java.util.List;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.Census;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;

public class InMemoryMarkerCensusOperationDataSet
		extends AbstractInMemoryOperationDataSet<MarkerCensusOperationEntry>
		implements MarkerCensusOperationDataSet
{

	public InMemoryMarkerCensusOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(origin, parent, operationKey);
	}

	public InMemoryMarkerCensusOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return MarkerCensusOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public List<byte[]> getKnownAlleles() throws IOException {
		return getKnownAlleles(-1, -1);
	}

	@Override
	public List<byte[]> getKnownAlleles(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				MarkerCensusOperationEntry.TO_KNOWN_ALLELES);
	}

	private List<Census> getCensusMarkerData(Category category, int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				new MarkerCensusOperationEntry.CensusExtractor(category));
	}

	private List<Census> getCensusMarkerData(Category category) throws IOException {
		return getCensusMarkerData(category, -1, -1);
	}

	@Override
	public List<Census> getCensus(Category category) throws IOException {
		return getCensus(category, -1, -1);
	}

	@Override
	public List<Census> getCensus(Category category, int from, int to) throws IOException {

		List<Census> censusesData = getCensusMarkerData(category, from, to);
		return censusesData;
	}
}
