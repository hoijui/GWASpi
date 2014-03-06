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

package org.gwaspi.operations.combi;

import java.util.List;
import libsvm.svm_model;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleKey;

public interface CombiTestOperationSpy {

	void initializing(
			List<MarkerKey> markerKeys,
			final List<Byte> majorAlleles,
			final List<Byte> minorAlleles,
			final List<int[]> markerGenotypesCounts,
			List<SampleKey> sampleKeys,
			List<Affection> sampleAffections,
			List<GenotypesList> markerGTs,
			GenotypeEncoder genotypeEncoder,
			MarkerGenotypesEncoder markerGenotypesEncoder);

	void kernelCalculated(float[][] K);

	void svmModelTrained(svm_model svmModel);

	void originalSpaceWeightsCalculated(List<Double> weightsEncoded);

	void decodedWeightsCalculated(List<Double> weights);

	void smoothedWeightsCalculated(List<Double> weightsFiltered);
}
