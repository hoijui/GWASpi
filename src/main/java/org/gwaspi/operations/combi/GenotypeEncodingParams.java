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
	 * The value for 'p' used to calculate the standard deviation
	 * when whitening/scaling the feature matrix.
	 * More precisely, when setting the means of the feature matrix to 1.
	 * The formula to be used should be:
	 * <math>(mean(abs(X) .^ p) * d) .^ (1 / p)</math>
	 * where 'd' is ??? (some weight?).
	 * It is called 'feature_scaling_p_norm' in the Octave/Matlab scripts.
	 */
	private final double featureScalingP;
	/**
	 * The value for 'p' used to calculate the standard deviation
	 * used when decoding weights from feature- into marker-space.
	 * It is called 'svm_p' in the Octave/Matlab scripts.
	 */
	private final double weightsDecodingP;

	public GenotypeEncodingParams(final Double pStandardDeviation, final Double weightsDecodingP) {

		this.featureScalingP = (pStandardDeviation == null)
				? getFeatureScalingPDefault()
				: pStandardDeviation;
		this.weightsDecodingP = (weightsDecodingP == null)
				? getWeightsDecodingPDefault()
				: weightsDecodingP;
	}

	public GenotypeEncodingParams() {
		this(null, null);
	}

	public double getFeatureScalingPDefault() {
		return 6.0;
	}

	public double getFeatureScalingP() {
		return featureScalingP;
	}

	public double getWeightsDecodingPDefault() {
		return 2.0;
	}

	public double getWeightsDecodingP() {
		return weightsDecodingP;
	}
}
