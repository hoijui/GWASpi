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

import org.gwaspi.constants.NetCDFConstants.Defaults.AlleleByte;
import org.gwaspi.global.Extractor;

public class OrderedAlleles {

	public static final Extractor<OrderedAlleles, Byte> TO_MAJOR_ALLELE
			= new Extractor<OrderedAlleles, Byte>()
	{
		@Override
		public Byte extract(OrderedAlleles from) {
			return from.getMajorAllele();
		}
	};

	public static final Extractor<OrderedAlleles, Double> TO_MAJOR_ALLELE_FREQ
			= new Extractor<OrderedAlleles, Double>()
	{
		@Override
		public Double extract(OrderedAlleles from) {
			return from.getMajorAlleleFreq();
		}
	};

	public static final Extractor<OrderedAlleles, Byte> TO_MINOR_ALLELE
			= new Extractor<OrderedAlleles, Byte>()
	{
		@Override
		public Byte extract(OrderedAlleles from) {
			return from.getMinorAllele();
		}
	};

	public static final Extractor<OrderedAlleles, Double> TO_MINOR_ALLELE_FREQ
			= new Extractor<OrderedAlleles, Double>()
	{
		@Override
		public Double extract(OrderedAlleles from) {
			return from.setMinorAlleleFreq();
		}
	};

	private byte majorAllele;
	private final double majorAlleleFreq;
	private byte minorAllele;

	public OrderedAlleles(
			byte majorAllele,
			double majorAlleleFreq,
			byte minorAllele
			)
	{
		this.majorAllele = majorAllele;
		this.majorAlleleFreq = majorAlleleFreq;
		this.minorAllele = minorAllele;
	}

	public OrderedAlleles() {
		this(AlleleByte._0_VALUE, 0.0, AlleleByte._0_VALUE); // XXX maybe make this value 1.0? should not matter too much though
	}

	/**
	 * @return the major allele (A, the one that appears more often)
	 */
	public byte getMajorAllele() {
		return majorAllele;
	}

	public void setMajorAllele(byte majorAllele) {
		this.majorAllele = majorAllele;
	}

	/**
	 * How frequently the major allele appears.
	 * @return a value in [0.5, 1.0]
	 */
	public double getMajorAlleleFreq() {
		return majorAlleleFreq;
	}

	/**
	 * @return the minor allele (a, the one that appears less often)
	 */
	public byte getMinorAllele() {
		return minorAllele;
	}

	public void setMinorAllele(byte minorAllele) {
		this.minorAllele = minorAllele;
	}

	/**
	 * How frequently the minor allele appears.
	 * @return a value in [0.0, 0.5)
	 */
	public double setMinorAlleleFreq() {
		return 1.0 - majorAlleleFreq;
	}
}
