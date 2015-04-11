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
import javax.persistence.Transient;

/**
 * Used for extended comparison of marker keys, for example,
 * across multiple data-sets.
 */
public class ExtendedMarkerKey implements Comparable<ExtendedMarkerKey>, Serializable {

	private static final long serialVersionUID = 1L;

	private MarkerKey markerKey;
	private String chr;
	private int pos;

	public ExtendedMarkerKey(MarkerKey markerKey, String chr, int pos) {

		this.markerKey = markerKey;
		this.chr = chr;
		this.pos = pos;
	}

	protected ExtendedMarkerKey() {
		this(null, null, Integer.MIN_VALUE);
	}

	public static ExtendedMarkerKey valueOf(MarkerMetadata marker) {
		return new ExtendedMarkerKey(
				MarkerKey.valueOf(marker),
				marker.getChr(),
				marker.getPos());
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ExtendedMarkerKey other = (ExtendedMarkerKey) obj;
		if (!this.getMarkerKey().equals(other.getMarkerKey())) {
			return false;
		}
		if (this.getPos() != other.getPos()) {
			return false;
		}
		if (!this.getChr().equals(other.getChr())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 61 * hash + (this.markerKey != null ? this.markerKey.hashCode() : 0);
		hash = 61 * hash + (this.chr != null ? this.chr.hashCode() : 0);
		hash = 61 * hash + this.pos;
		return hash;
	}

	@Override
	public int compareTo(ExtendedMarkerKey other) {

		int diff = this.getMarkerKey().compareTo(other.getMarkerKey());
		if (diff == 0) {
			diff = this.getChr().compareTo(other.getChr());
			if (diff == 0) {
				diff = this.getPos() - other.getPos();
			}
		}

		return diff;
	}

	@Override
	public String toString() {

		StringBuilder strRep = new StringBuilder();

		strRep.append(markerKey.toString());
		strRep.append(" [");
		strRep.append("chr: ").append(getChr());
		strRep.append(", pos: ").append(getPos());
		strRep.append("]");

		return strRep.toString();
	}

	public String getMarkerId() {
		return markerKey.getMarkerId();
	}

	protected void setMarkerId(String markerId) {
		this.markerKey = new MarkerKey(markerId);
	}

	@Transient
	public MarkerKey getMarkerKey() {
		return markerKey;
	}

	public String getChr() {
		return chr;
	}

	protected void setChr(String chr) {
		this.chr = chr;
	}

	public int getPos() {
		return pos;
	}

	protected void setPos(int pos) {
		this.pos = pos;
	}
}
