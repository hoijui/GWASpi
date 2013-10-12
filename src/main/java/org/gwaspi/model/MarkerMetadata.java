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


import java.io.Serializable;
import org.gwaspi.global.Extractor;

public class MarkerMetadata implements Serializable, Comparable<MarkerMetadata> {

	public static final Extractor<MarkerMetadata, String> TO_MARKER_ID
			= new Extractor<MarkerMetadata, String>()
	{
		@Override
		public String extract(MarkerMetadata from) {
			return from.getMarkerId();
		}
	};

	public static final Extractor<MarkerMetadata, String> TO_RS_ID
			= new Extractor<MarkerMetadata, String>()
	{
		@Override
		public String extract(MarkerMetadata from) {
			return from.getRsId();
		}
	};

	public static final Extractor<MarkerMetadata, String> TO_CHR
			= new Extractor<MarkerMetadata, String>()
	{
		@Override
		public String extract(MarkerMetadata from) {
			return from.getChr();
		}
	};

	public static final Extractor<MarkerMetadata, Integer> TO_POS
			= new Extractor<MarkerMetadata, Integer>()
	{
		@Override
		public Integer extract(MarkerMetadata from) {
			return from.getPos();
		}
	};

	public static final Extractor<MarkerMetadata, String> TO_ALLELES
			= new Extractor<MarkerMetadata, String>()
	{
		@Override
		public String extract(MarkerMetadata from) {
			return from.getAlleles();
		}
	};

	public static final Extractor<MarkerMetadata, String> TO_STRAND
			= new Extractor<MarkerMetadata, String>()
	{
		@Override
		public String extract(MarkerMetadata from) {
			return from.getStrand();
		}
	};

	private final String markerId;
	private final String rsId;
	private final String chr;
	private final int pos;
	private final String alleles;
	private final String strand;

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos,
			String alleles,
			String strand)
	{
		this.markerId = markerId;
		this.rsId = rsId;
		this.chr = chr;
		this.pos = pos;
		this.alleles = alleles;
		this.strand = strand;
	}

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos,
			String alleles)
	{
		this(markerId, rsId, chr, pos, alleles, null);
	}

	public MarkerMetadata(
			String markerId,
			String rsId,
			String chr,
			int pos)
	{
		this(markerId, rsId, chr, pos, null);
	}

	public MarkerMetadata(
			String chr,
			int pos)
	{
		this(null, null, chr, pos, null);
	}

	@Override
	public boolean equals(Object other) {

		boolean equals = false;

		if (other instanceof MarkerMetadata) {
			MarkerMetadata otherMM = (MarkerMetadata) other;
			equals = getMarkerId().equals(otherMM.getMarkerId())
					&& getChr().equals(otherMM.getChr())
					&& (getPos() == otherMM.getPos());
		}

		return equals;
	}

	@Override
	public int hashCode() {

		int hash = 7;
		hash = 19 * hash + (this.markerId != null ? this.markerId.hashCode() : 0);
		hash = 19 * hash + (this.chr != null ? this.chr.hashCode() : 0);
		hash = 19 * hash + this.pos;
		return hash;
	}

	@Override
	public int compareTo(MarkerMetadata other) {
		return hashCode() - other.hashCode();
	}

	public String getMarkerId() {
		return markerId;
	}

	public String getRsId() {
		return rsId;
	}

	public String getChr() {
		return chr;
	}

	public int getPos() {
		return pos;
	}

	public String getAlleles() {
		return alleles;
	}

	public String getStrand() {
		return strand;
	}
}
