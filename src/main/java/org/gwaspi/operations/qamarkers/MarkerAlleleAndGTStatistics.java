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

package org.gwaspi.operations.qamarkers;

import org.gwaspi.constants.NetCDFConstants.Defaults.AlleleByte;

public class MarkerAlleleAndGTStatistics implements Cloneable {

//	private int numSamples; // can be calculated from the sum over alleleOrdinalCounts, and can be fetched through other means anyway
	private boolean mismatch;
	private byte majorAllele;
	private byte minorAllele;
	private double majorAlleleFreq;
//	private int majorAlleleOrdinal;
//	private int minorAlleleOrdinal;
	private int numAA;
	private int numAa;
	private int numaa;
	private int[] compactAlleleStatistics;
	private int[] compactGenotypeStatistics;

	public MarkerAlleleAndGTStatistics() {

		clear();
	}

	public byte getMajorAllele() {
		return majorAllele;
	}

	@Override
    public MarkerAlleleAndGTStatistics clone() {

		try {
			return (MarkerAlleleAndGTStatistics) super.clone();
		} catch (CloneNotSupportedException ex) {
			// This should never happen, as we do implement Cloneable.
			return null;
		}
	}

//	public byte getMajorAlleleNonZero() {
//		return (getMajorAllele() == AlleleByte._0_VALUE) ? getMinorAllele() : getMajorAllele();
//	}

	public void setMajorAllele(byte majorAllele) {
		this.majorAllele = majorAllele;
	}

	public byte getMinorAllele() {
		return minorAllele;
	}

//	public byte getMinorAlleleNonZero() {
//		return (getMinorAllele() == AlleleByte._0_VALUE) ? getMajorAllele() : getMinorAllele();
//	}

	public void setMinorAllele(byte minorAllele) {
		this.minorAllele = minorAllele;
	}

	public double getMajorAlleleFreq() {
		return majorAlleleFreq;
	}

	public void setMajorAlleleFreq(double majorAlleleFreq) {
		this.majorAlleleFreq = majorAlleleFreq;
	}

	/**
	 * @return true, if there are more then two kinds of non-zero alleles
	 *   present in the marker genotypes
	 */
	public boolean isMismatch() {
		return mismatch;
	}

	public void setMismatch() {

		mismatch = true;
		majorAllele = AlleleByte._0_VALUE;
		minorAllele = AlleleByte._0_VALUE;
		majorAlleleFreq = 0.0; // NOTE This could in theory produce problems, and maybe 1.0 would be a better value, or even 0.5
		numAA = 0;
		numAa = 0;
		numaa = 0;
	}

	/**
	 * @return number of observed GTs of type: AA, A0, 0A
	 */
	public int getNumAA() {
//
//		if (getMajorAllele() == AlleleByte._0_VALUE) {
//			return 0;
//		} else {
//			final int majorAlleleOrdinal = alleleValueToOrdinal[getMajorAllele()];
//			return Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][majorAlleleOrdinal])        // #AA
//					+ Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #A0
//					+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][majorAlleleOrdinal]); // #0A
//		}
		return numAA;
	}

	public void setNumAA(int numAA) {
		this.numAA = numAA;
	}

	public int getNumAa() {
//
//		if ((getMajorAllele() == AlleleByte._0_VALUE) || (getMinorAllele() == AlleleByte._0_VALUE)) {
//			return 0;
//		} else {
//			final int majorAlleleOrdinal = alleleValueToOrdinal[getMajorAllele()];
//			final int minorAlleleOrdinal = alleleValueToOrdinal[getMinorAllele()];
//			return Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][minorAlleleOrdinal])     // #Aa
//					+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][majorAlleleOrdinal]); // #aA
//		}
		return numAa;
	}

	public void setNumAa(int numAa) {
		this.numAa = numAa;
	}

	public int getNumaa() {
//
//		if (getMinorAllele() == AlleleByte._0_VALUE) {
//			return 0;
//		} else {
//			final int minorAlleleOrdinal = alleleValueToOrdinal[getMinorAllele()];
//			return Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][minorAlleleOrdinal])        // #aa
//					+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #a0
//					+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][minorAlleleOrdinal]); // #0a
//		}
		return numaa;
	}

	public void setNumaa(int numaa) {
		this.numaa = numaa;
	}

	public int[] getCompactAlleleStatistics() {
		return compactAlleleStatistics;
	}

	public void setCompactAlleleStatistics(int[] compactAlleleStatistics) {
		this.compactAlleleStatistics = compactAlleleStatistics;
	}

	public int[] getCompactGenotypeStatistics() {
		return compactGenotypeStatistics;
	}

	public void setCompactGenotypeStatistics(int[] compactGenotypeStatistics) {
		this.compactGenotypeStatistics = compactGenotypeStatistics;
	}

//	public final void calculateSummaryValues(RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics) {
//
//		numAA = 0;
//		numAa = 0;
//		numaa = 0;
//		if (getMajorAllele() != AlleleByte._0_VALUE) {
//			final int majorAlleleOrdinal = alleleValueToOrdinal[getMajorAllele()];
//			numAA
//					= Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][majorAlleleOrdinal])  // #AA
//					+ Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #A0
//					+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][majorAlleleOrdinal]); // #0A
//			if (orderedAlleles.getMinorAllele() != AlleleByte._0_VALUE) {
//				final int minorAlleleOrdinal = alleleValueToOrdinal[getMinorAllele()];
//				numAa
//						= Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][minorAlleleOrdinal])  // #Aa
//						+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][majorAlleleOrdinal]); // #aA
//				numaa
//						= Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][minorAlleleOrdinal])  // #aa
//						+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #a0
//						+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][minorAlleleOrdinal]); // #0a
//			}
//		}
//	}

	public final void clear() {

		majorAllele = -1;
		minorAllele = -1;
		majorAlleleFreq = -1.0;
		mismatch = false;
		numAA = -1;
		numAa = -1;
		numaa = -1;
		compactAlleleStatistics = null;
		compactGenotypeStatistics = null;
	}
}
