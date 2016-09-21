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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Allows to generate reproducible values by index.
 * @param <O> output/generated value type
 */
public interface Generator<O> {
//
//	class RandomRangeGenerator implements Generator<Integer> {
//
//		@Override
//		public String extract(I object) {
//			return object.toString();
//		}
//	}

	public static class ListRandomGenerator<O> implements Generator<O> {

		private final long baseRandomSeed;
		private final Random random;
		private final List<O> values;
		private final List<Double> valuesProbLimits;

		public ListRandomGenerator(
				final long randomSeed,
				final List<O> values,
				final List<Double> valuesProbabilities)
		{
			this.baseRandomSeed = randomSeed;
			// java.util.Random gives nearly same values as the first value
			// for consecutive seeds, which is not ok for us.
			this.random = new SecureRandom(); // seed will be overridden later
			if (values.size() < 2) {
				throw new IllegalArgumentException(
						"We need at least two distinct values");
			}
			if (values.size() != valuesProbabilities.size()) {
				throw new IllegalArgumentException(
						"The list of values and their probabilities have to be of same size");
			}
			this.values = values;
			this.valuesProbLimits = new ArrayList<Double>(valuesProbabilities);
			// convert probabilities to probability limits
			for (int i = 1; i < valuesProbabilities.size(); i++) {
				this.valuesProbLimits.set(i, this.valuesProbLimits.get(i - 1)
								+ this.valuesProbLimits.get(i));
			}
		}

		@Override
		public O generate(final int index) {

			random.setSeed(baseRandomSeed + index);
			final double randomVar = random.nextDouble();
			O value = values.get(values.size() - 1);
			for (int i = 0; i < valuesProbLimits.size(); i++) {
				if (randomVar < valuesProbLimits.get(i)) {
					value = values.get(i);
					break;
				}
			}
			return value;
		}
	}

	public static class EnumRandomGenerator<O extends Enum> extends ListRandomGenerator<O> {

		public EnumRandomGenerator(
				final long randomSeed,
				final List<Double> elementProbabilities,
				final Class<O> enumClass)
		{
			super(
					randomSeed,
					Arrays.asList(enumClass.getEnumConstants()),
					elementProbabilities);
		}
	}

	O generate(int index);
}
