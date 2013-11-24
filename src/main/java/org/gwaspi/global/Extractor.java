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
 */
public interface Extractor<IV, OV> {

	public static class ToStringExtractor<IV> implements Extractor<IV, String> {

		public ToStringExtractor() {
		}

		@Override
		public String extract(IV object) {
			return object.toString();
		}
	}

	public static class ToStringMetaExtractor<IV, M> implements Extractor<IV, String> {

		private final Extractor<IV, M> preExtractor;

		public ToStringMetaExtractor(Extractor<IV, M> preExtractor) {
			this.preExtractor = preExtractor;
		}

		@Override
		public String extract(IV object) {
			return preExtractor.extract(object).toString();
		}
	}

	OV extract(IV object);
}
