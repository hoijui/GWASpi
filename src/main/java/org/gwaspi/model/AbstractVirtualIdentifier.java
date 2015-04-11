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

import java.util.UUID;

/**
 * Uniquely identifies an object that may not exist.
 */
public class AbstractVirtualIdentifier<K extends Identifier<K>> implements VirtualIdentifier<K> {

	private static final String NAME_DEFAULT = "pure-virtual";

	private final UUID virtualId;
	private final String name;
	private K key;

	public AbstractVirtualIdentifier(final K key, final String name) {

		this.key = key;
		this.virtualId = UUID.randomUUID();
		this.name = name;
	}

	public AbstractVirtualIdentifier(final K key) {
		this(key, NAME_DEFAULT);
	}

	public AbstractVirtualIdentifier(final String name) {
		this(null, name);
	}

	@Override
	public boolean equals(final Object obj) {

		if (obj == null) {
			return false;
		}
		if (!Identifier.class.isInstance(obj)) {
			return false;
		}
		if (!VirtualIdentifier.class.isInstance(obj)) {
			// we assume it is of type KT
			if (key != null) {
				return key.equals(obj);
			} else {
				return false;
			}
		}
		if (getClass() != obj.getClass()) {
			// NOTE this would be a problem if we have multiple virtual identifiers implemented per one KT
			return false;
		}
		final AbstractVirtualIdentifier other = (AbstractVirtualIdentifier) obj;
		if (!this.isVirtual() && !other.isVirtual()) {
			return this.getKey().equals(other.getKey());
		}
		return (this.getVirtualId() == other.getVirtualId());
	}

	@Override
	public int hashCode() {

		if (!isVirtual()) {
			return getKey().hashCode();
		}

		return getVirtualId().hashCode();
	}

	@Override
	public int compareTo(final Identifier<K> other) {

		if (!(other instanceof VirtualIdentifier)) {
			return - other.compareTo(this);
		}
		final AbstractVirtualIdentifier<K> otherCasted = (AbstractVirtualIdentifier<K>) other;
		if (!this.isVirtual() && !otherCasted.isVirtual()) {
			return this.getKey().compareTo(otherCasted.getKey());
		}
		return this.getVirtualId().compareTo(otherCasted.getVirtualId());
	}

	@Override
	public String toRawIdString() {

		if (!isVirtual()) {
			return getKey().toRawIdString();
		}

		final StringBuilder strRep = new StringBuilder();

		strRep.append("virtual-id: ").append(getVirtualId().toString());

		return strRep.toString();
	}

	@Override
	public String toIdString() {

		final StringBuilder strRep = new StringBuilder();

		strRep
				.append(getClass().getSimpleName())
				.append('[')
				.append(toRawIdString())
				.append(']');

		return strRep.toString();
	}

	@Override
	public String fetchName() {

		if (!isVirtual()) {
			return getKey().fetchName();
		}
		return name;
	}

	@Override
	public String toString() {

		final StringBuilder strRep = new StringBuilder();

		strRep
				.append(fetchName())
				.append(" [")
				.append(toRawIdString())
				.append(']');

		return strRep.toString();
	}

	public static <KT extends Identifier<KT>> AbstractVirtualIdentifier<KT> valueOf(final KT key) {
		return new AbstractVirtualIdentifier<KT>(key);
	}

	@Override
	public boolean isVirtual() {
		return (key == null);
	}

	@Override
	public void setKey(final K key) {
		this.key = key;
	}

	public K getKey() {
		return key;
	}

	public UUID getVirtualId() {
		return virtualId;
	}
}
