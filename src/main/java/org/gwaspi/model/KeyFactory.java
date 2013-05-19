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

/**
 * Allows to convert <code>Key</code>s of type <code>K</code>
 * from and to <code>String</code>s.
 */
public interface KeyFactory<K> {

	/**
	 * Allows to parse a key from a single string value.
	 * @param keyStr should be of the form returned by
	 *   {@link #encode(K)}
	 * @return the parsed key
	 */
	K decode(String keyStr);

	/**
	 * Allows to convert a key into a single string representation.
	 * @param key a valid key
	 * @return the string encoded key
	 */
	String encode(K key);
}
