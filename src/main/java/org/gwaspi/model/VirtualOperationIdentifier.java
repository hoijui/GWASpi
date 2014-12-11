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

/**
 * Uniquely identifies an operation that may not exist.
 */
public class VirtualOperationIdentifier extends AbstractVirtualIdentifier<OperationKey> {

	public VirtualOperationIdentifier(final OperationKey key, final String name) {
		super(key, name);
	}

	public VirtualOperationIdentifier(final OperationKey key) {
		super(key);
	}

	protected VirtualOperationIdentifier() {
		this(null);
	}

	public static VirtualOperationIdentifier valueOf(final OperationMetadata operation) {
		return new VirtualOperationIdentifier(OperationKey.valueOf(operation));
	}
}
