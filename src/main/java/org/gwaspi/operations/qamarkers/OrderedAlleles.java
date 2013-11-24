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

import org.gwaspi.global.Extractor;

public class OrderedAlleles {

	public static final Extractor<OrderedAlleles, Byte> TO_ALLELE_1
			= new Extractor<OrderedAlleles, Byte>()
	{
		@Override
		public Byte extract(OrderedAlleles from) {
			return from.getAllele1();
		}
	};

	public static final Extractor<OrderedAlleles, Double> TO_ALLELE_1_FREQ
			= new Extractor<OrderedAlleles, Double>()
	{
		@Override
		public Double extract(OrderedAlleles from) {
			return from.getAllele1Freq();
		}
	};

	public static final Extractor<OrderedAlleles, Byte> TO_ALLELE_2
			= new Extractor<OrderedAlleles, Byte>()
	{
		@Override
		public Byte extract(OrderedAlleles from) {
			return from.getAllele2();
		}
	};

	public static final Extractor<OrderedAlleles, Double> TO_ALLELE_2_FREQ
			= new Extractor<OrderedAlleles, Double>()
	{
		@Override
		public Double extract(OrderedAlleles from) {
			return from.getAllele2Freq();
		}
	};

	private byte allele1;
	private final double allele1Freq;
	private byte allele2;

	public OrderedAlleles(
			byte allele1,
			double allele1Freq,
			byte allele2
			)
	{
		this.allele1 = allele1;
		this.allele1Freq = allele1Freq;
		this.allele2 = allele2;
	}

	public OrderedAlleles() {
		this((byte) '0', 0.0, (byte) '0');
	}

	/**
	 * @return the mayor allele
	 */
	public byte getAllele1() {
		return allele1;
	}

	public void setAllele1(byte allele1) {
		this.allele1 = allele1;
	}

	public double getAllele1Freq() {
		return allele1Freq;
	}

	/**
	 * @return the minor allele
	 */
	public byte getAllele2() {
		return allele2;
	}

	public void setAllele2(byte allele2) {
		this.allele2 = allele2;
	}

	public double getAllele2Freq() {
		return 1.0 - allele1Freq;
	}
}
