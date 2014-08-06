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

package org.gwaspi.operations.qamarkers;

import java.io.IOException;
import java.util.List;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.AbstractInMemoryOperationDataSet;
import org.gwaspi.operations.OperationTypeInfo;

public class InMemoryQAMarkersOperationDataSet extends AbstractInMemoryOperationDataSet<QAMarkersOperationEntry> implements QAMarkersOperationDataSet {

//	public InMemoryQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
//		super(true, origin, parent, operationKey);
//	}

	public InMemoryQAMarkersOperationDataSet(MatrixKey origin, DataSetKey parent) {
		super(origin, parent);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return QAMarkersOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public List<Boolean> getMismatchStates(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MISMATCH_STATE);
	}

	@Override
	public List<Double> getMissingRatio(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MISSING_RATIO);
	}

	@Override
	public List<Byte> getKnownMajorAllele(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MAJOR_ALLELE);
	}

	@Override
	public List<Double> getKnownMajorAlleleFrequencies(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MAJOR_ALLELE_FREQUENCY);
	}

	@Override
	public List<Byte> getKnownMinorAllele(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MINOR_ALLELE);
	}

	@Override
	public List<Double> getKnownMinorAlleleFrequencies(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MINOR_ALLELE_FREQUENCY);
	}

	@Override
	public List<Integer> getNumAAs(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_ALLELE_AA);
	}

	@Override
	public List<Integer> getNumAas(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_ALLELE_Aa);
	}

	@Override
	public List<Integer> getNumaas(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_ALLELE_aa);
	}

	@Override
	public List<Integer> getMissingCounts(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_MISSING_COUNT);
	}

	@Override
	public List<int[]> getAlleleCounts(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_ALLELE_COUNTS);
	}

	@Override
	public List<int[]> getGenotypeCounts(int from, int to) throws IOException {

		return AbstractInMemoryListSource.extractProperty(
				getEntries(from, to),
				QAMarkersOperationEntry.TO_GENOTYPE_COUNTS);
	}

	@Override
	public List<Byte> getKnownMajorAllele() throws IOException {
		return getKnownMajorAllele(-1, -1);
	}

	@Override
	public List<Double> getKnownMajorAlleleFrequencies() throws IOException {
		return getKnownMajorAlleleFrequencies(-1, -1);
	}

	@Override
	public List<Byte> getKnownMinorAllele() throws IOException {
		return getKnownMinorAllele(-1, -1);
	}

	@Override
	public List<Double> getKnownMinorAlleleFrequencies() throws IOException {
		return getKnownMinorAlleleFrequencies(-1, -1);
	}

	@Override
	public List<Boolean> getMismatchStates() throws IOException {
		return getMismatchStates(-1, -1);
	}

	@Override
	public List<Integer> getNumAAs() throws IOException {
		return getNumAAs(-1, -1);
	}

	@Override
	public List<Integer> getNumAas() throws IOException {
		return getNumAas(-1, -1);
	}

	@Override
	public List<Integer> getNumaas() throws IOException {
		return getNumaas(-1, -1);
	}

	@Override
	public List<Integer> getMissingCounts() throws IOException {
		return getMissingCounts(-1, -1);
	}

	@Override
	public List<Double> getMissingRatio() throws IOException {
		return getMissingRatio(-1, -1);
	}

	@Override
	public List<int[]> getAlleleCounts() throws IOException {
		return getAlleleCounts(-1, -1);
	}

	@Override
	public List<int[]> getGenotypeCounts() throws IOException {
		return getGenotypeCounts(-1, -1);
	}
}
