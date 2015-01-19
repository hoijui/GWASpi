/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.global;

/**
 * Allows to accept or deny a value/object
 * @param <I> input/object/value type
 */
public interface Filter<I> {

	class BooleanFilter implements Filter<Boolean> {

		@Override
		public boolean accept(final Boolean object) {
			return object;
		}
	}

	class BooleanInvertedFilter implements Filter<Boolean> {

		@Override
		public boolean accept(final Boolean object) {
			return !object;
		}
	}

	class DoubleGreaterThenFilter implements Filter<Double> {

		private final double limit;

		public DoubleGreaterThenFilter(final double limit) {
			this.limit = limit;
		}

		@Override
		public boolean accept(final Double object) {
			return (object > limit);
		}
	}

	boolean accept(I object);
}
