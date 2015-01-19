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

package org.gwaspi.global;

/**
 * Allows to extract one or multiple values out of an object
 * @param <I> input/container value type
 * @param <O> output/extracted value type
 */
public interface Extractor<I, O> {

	class ToStringExtractor<I> implements Extractor<I, String> {

		@Override
		public String extract(I object) {
			return object.toString();
		}
	}

	class ToStringMetaExtractor<I, M> implements Extractor<I, String> {

		private final Extractor<I, M> preExtractor;

		public ToStringMetaExtractor(Extractor<I, M> preExtractor) {
			this.preExtractor = preExtractor;
		}

		@Override
		public String extract(I object) {
			return preExtractor.extract(object).toString();
		}
	}

	class EnumToIntExtractor<I extends Enum> implements Extractor<I, Integer> {

		@Override
		public Integer extract(I object) {
			return object.ordinal();
		}
	}

	class EnumToIntMetaExtractor<I, M extends Enum> implements Extractor<I, Integer> {

		private final Extractor<I, M> preExtractor;

		public EnumToIntMetaExtractor(Extractor<I, M> preExtractor) {
			this.preExtractor = preExtractor;
		}

		@Override
		public Integer extract(I object) {
			return preExtractor.extract(object).ordinal();
		}
	}

	class IntToEnumExtractor<O extends Enum> implements Extractor<Integer, O> {

		private final O[] enumValues;

		public IntToEnumExtractor(O[] enumValues) {

			this.enumValues = enumValues;
		}

		@Override
		public O extract(Integer object) {
			return enumValues[object];
		}
	}

	class ByteToStringExtractor implements Extractor<Byte, String> {

		@Override
		public String extract(final Byte object) {
			return String.valueOf((char) (byte) object);
		}
	}

	O extract(I object);
}
