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

import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;

public class MarkerGTStatistics {

	private static final int NUM_GLOBALLY_POSSIBLE_ALLELE = AlleleByte.values().length;

//	private int numSamples; // can be calculated from the sum over alleleOrdinalCounts, and can be fetched through other means anyway
	private byte majorAllele;
	private byte minorAllele;
//	private int majorAlleleOrdinal;
//	private int minorAlleleOrdinal;
	private float[] alleleOrdinalCounts;
	private float[][] gtOrdinalCounts;
	private int missingCount;

	public MarkerGTStatistics() {

		this.majorAllele = -1;
		this.minorAllele = -1;
		this.alleleOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE];
		this.gtOrdinalCounts = new float[NUM_GLOBALLY_POSSIBLE_ALLELE][NUM_GLOBALLY_POSSIBLE_ALLELE];
		this.missingCount = -1;
	}

	public byte getMajorAllele() {
		return majorAllele;
	}

	public void setMajorAllele(byte majorAllele) {
		this.majorAllele = majorAllele;
	}

	public byte getMinorAllele() {
		return minorAllele;
	}

	public void setMinorAllele(byte minorAllele) {
		this.minorAllele = minorAllele;
	}

	public float[] getAlleleOrdinalCounts() {
		return alleleOrdinalCounts;
	}

	public float[][] getGtOrdinalCounts() {
		return gtOrdinalCounts;
	}

	public int getMissingCount() {
		return missingCount;
	}

	public void setMissingCount(int missingCount) {
		this.missingCount = missingCount;
	}
}
