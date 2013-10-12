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

import org.gwaspi.operations.OperationDataEntry;
import org.gwaspi.model.SampleKey;

public interface QASamplesOperationEntry extends OperationDataEntry<SampleKey> {

	/**
	 * @return the missing ratio of this sample
	 * NetCDF variable: cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT
	 */
	double getMissingRatio();

	/**
	 * @return the missing count of this sample
	 * NetCDF variable: cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT
	 */
	int getMissingCount();

	/**
	 * @return hetzy ratio of this sample
	 * NetCDF variable: cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT
	 */
	double getHetzyRatio();
}
