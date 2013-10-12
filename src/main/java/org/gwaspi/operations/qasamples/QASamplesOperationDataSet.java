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

package org.gwaspi.operations.qasamples;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.operations.OperationDataSet;

public interface QASamplesOperationDataSet extends OperationDataSet {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getId()) marker keys
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT: (double) missing ratio for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT: (int) missing count for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT: (double) heterozygosity ratio for each sample

	/**
	 * @param sampleMissingRatios
	 *   the sample missing ratio values, one per sample in this operation
	 * NetCDF variable: Census.VAR_OP_SAMPLES_MISSINGRAT
	 */
	void setSampleMissingRatios(Collection<Double> sampleMissingRatios) throws IOException;

	/**
	 * @param sampleMissingCount
	 *   the sample missing count values, one per sample in this operation
	 * NetCDF variable: Census.VAR_OP_SAMPLES_MISSINGCOUNT
	 */
	void setSampleMissingCount(Collection<Integer> sampleMissingCount) throws IOException;

	/**
	 * @param sampleHetzyRatios
	 *   the sample hetzy ratio values, one per sample in this operation
	 * NetCDF variable: Census.VAR_OP_SAMPLES_HETZYRAT
	 */
	void setSampleHetzyRatios(Collection<Double> sampleHetzyRatios) throws IOException;

	Collection<QASamplesOperationEntry> getEntries() throws IOException;
}
