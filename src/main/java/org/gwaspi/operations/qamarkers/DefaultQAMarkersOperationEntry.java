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

import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.AbstractOperationDataEntry;

public class DefaultQAMarkersOperationEntry extends AbstractOperationDataEntry<MarkerKey> implements QAMarkersOperationEntry {

	private final double missingRatio;
	private final boolean mismatchState;
	private final byte majorAllele;
	private final double majorAlleleFrequency;
	private final byte minorAllele;
	private final double minorAlleleFrequency;
	private final int alleleAA;
	private final int alleleAa;
	private final int alleleaa;
	private final int missingCount;

	public DefaultQAMarkersOperationEntry(
			MarkerKey key,
			double missingRatio,
			boolean mismatchState,
			byte majorAllele,
			double majorAlleleFrequency,
			byte minorAllele,
			double minorAlleleFrequency,
			int alleleAA,
			int alleleAa,
			int alleleaa,
			int missingCount)
	{
		super(key);

		this.missingRatio = missingRatio;
		this.mismatchState = mismatchState;
		this.majorAllele = majorAllele;
		this.majorAlleleFrequency = majorAlleleFrequency;
		this.minorAllele = minorAllele;
		this.minorAlleleFrequency = minorAlleleFrequency;
		this.alleleAA = alleleAA;
		this.alleleAa = alleleAa;
		this.alleleaa = alleleaa;
		this.missingCount = missingCount;
	}

	@Override
	public double getMissingRatio() {
		return missingRatio;
	}

	@Override
	public boolean getMismatchState() {
		return mismatchState;
	}

	@Override
	public byte getMajorAllele() {
		return majorAllele;
	}

	@Override
	public double getMajorAlleleFrequency() {
		return majorAlleleFrequency;
	}

	@Override
	public byte getMinorAllele() {
		return minorAllele;
	}

	@Override
	public double getMinorAlleleFrequency() {
		return minorAlleleFrequency;
	}

	@Override
	public int[] getAllCensus() {
		return new int[] {alleleAA, alleleAa, alleleaa, missingCount};
	}

	@Override
	public int getAlleleAA() {
		return alleleAA;
	}

	@Override
	public int getAlleleAa() {
		return alleleAa;
	}

	@Override
	public int getAlleleaa() {
		return alleleaa;
	}

	@Override
	public int getMissingCount() {
		return missingCount;
	}
}
