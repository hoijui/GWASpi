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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;

public class InMemoryHardyWeinbergOperationDataSet
		extends AbstractInMemoryOperationDataSet<HardyWeinbergOperationEntry>
		implements HardyWeinbergOperationDataSet
{
	private List<HardyWeinbergOperationEntry> entriesControl;
	private List<HardyWeinbergOperationEntry> entriesAlternate;

	public InMemoryHardyWeinbergOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(origin, parent, operationKey);

		this.entriesControl = null;
		this.entriesAlternate = null;
	}

	public InMemoryHardyWeinbergOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return HardyWeinbergOperationFactory.OPERATION_TYPE_INFO;
	}

	private void filterEntries() throws IOException {

		final List<HardyWeinbergOperationEntry> entries = getEntries();

		final ArrayList<HardyWeinbergOperationEntry> tmpEntriesControl
				= new ArrayList<HardyWeinbergOperationEntry>(entries.size());
		final ArrayList<HardyWeinbergOperationEntry> tmpEntriesAlternate
				= new ArrayList<HardyWeinbergOperationEntry>(entries.size());
		for (HardyWeinbergOperationEntry entry : entries) {
			if (entry.getCategory() == Category.CONTROL) {
				tmpEntriesControl.add(entry);
			} else if (entry.getCategory() == Category.ALTERNATE) {
				tmpEntriesAlternate.add(entry);
			}
		}
		tmpEntriesControl.trimToSize();
		tmpEntriesAlternate.trimToSize();

		entriesControl = Collections.unmodifiableList(tmpEntriesControl);
		entriesAlternate = Collections.unmodifiableList(tmpEntriesAlternate);
	}

	@Override
	public List<HardyWeinbergOperationEntry> getEntriesControl() throws IOException {

		if (entriesControl == null) {
			filterEntries();
		}

		return entriesControl;
	}

	@Override
	public List<HardyWeinbergOperationEntry> getEntriesAlternate() throws IOException {

		if (entriesAlternate == null) {
			filterEntries();
		}

		return entriesAlternate;
	}

	@Override
	public List<Double> getPs(Category category, int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				HardyWeinbergOperationEntry.P_VALUE_EXTRACTOR);
	}

	@Override
	public List<Double> getHwHetzyObses(Category category, int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				HardyWeinbergOperationEntry.HETZY_OBSERVED_EXTRACTOR);
	}

	@Override
	public List<Double> getHwHetzyExps(Category category, int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				HardyWeinbergOperationEntry.HETZY_EXPECTED_EXTRACTOR);
	}
}
