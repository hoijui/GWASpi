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

public interface QASamplesOperationDataSet extends OperationDataSet<QASamplesOperationEntry> {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getId()) marker keys
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT: (double) missing ratio for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT: (int) missing count for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT: (double) heterozygosity ratio for each sample

	void addEntry(QASamplesOperationEntry entry) throws IOException;

	Collection<Double> getMissingRatios(int from, int to) throws IOException;
	Collection<Integer> getMissingCounts(int from, int to) throws IOException;
	Collection<Double> getHetzyRatios(int from, int to) throws IOException;
}
