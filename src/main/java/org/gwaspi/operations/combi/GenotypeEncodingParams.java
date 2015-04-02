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

package org.gwaspi.operations.combi;

public class GenotypeEncodingParams {

	/**
	 * The value for 'p', used when whitening the data (more precisely, when setting the mean to 1).
	 * The formula to be used should be:
	 * <math>(mean(abs(X) .^ p) * d) .^ (1 / p)</math>
	 * where 'd' is ??? (some weight?).
	 */
	private final double pStandardDeviation;

	public GenotypeEncodingParams(final Double pStandardDeviation) {

		this.pStandardDeviation = (pStandardDeviation == null)
				? getPStandardDeviationDefault()
				: pStandardDeviation;
	}

	public GenotypeEncodingParams() {
		this(null);
	}

	public double getPStandardDeviationDefault() {
		return 6.0;
	}

	public double getPStandardDeviation() {
		return pStandardDeviation;
	}
}
