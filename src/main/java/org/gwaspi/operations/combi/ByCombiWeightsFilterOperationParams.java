/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

public class ByCombiWeightsFilterOperationParams extends AbstractOperationParams {

	/**
	 * The number of total markers in the matrix we operate on, unfiltered.
	 */
	private Integer totalMarkers;
	/**
	 * The filter-width of the moving average filter (p-norm-filter)
	 * applied to the weights after SVM training.
	 */
	private Integer weightsFilterWidth;
	/**
	 * How many markers to be left with,
	 * after the filtering with the COMBI method.
	 */
	private final int markersToKeep;

	ByCombiWeightsFilterOperationParams(
			Integer totalMarkers,
			OperationKey combiParentOpKey,
			Integer weightsFilterWidth,
			Integer markersToKeep,
			String name)
	{
		super(OPType.FILTER_BY_WEIGHTS, new DataSetKey(combiParentOpKey), name);

		this.totalMarkers = totalMarkers;

		this.weightsFilterWidth = ((weightsFilterWidth == null)
				|| (weightsFilterWidth <= 0) || (weightsFilterWidth >= getTotalMarkers()))
				? getWeightsFilterWidthDefault()
				: weightsFilterWidth;
		this.markersToKeep = ((markersToKeep == null)
				|| (markersToKeep <= 0) || (markersToKeep >= getTotalMarkers()))
				? getMarkersToKeepDefault()
				: markersToKeep;
	}

	public ByCombiWeightsFilterOperationParams(
			OperationKey combiParentOpKey,
			Integer weightsFilterWidth,
			Integer markersToKeep,
			String name)
	{
		this(null, combiParentOpKey, weightsFilterWidth, markersToKeep, name);
	}

	public ByCombiWeightsFilterOperationParams(OperationKey combiParentOpKey) {
		this(
				combiParentOpKey,
				null,
				null,
				null
		);
	}

	public ByCombiWeightsFilterOperationParams(int totalMarkers) {
		this(
				totalMarkers,
				null,
				null,
				null,
				null
		);
	}

	public int getTotalMarkers() {

		if (totalMarkers == null) {
			totalMarkers = CombiTestOperationParams.fetchTotalMarkers(getParent());
		}

		return totalMarkers;
	}

	@Override
	protected String getNameDefault() {
		return "Exclude by COMBI weight";
	}

	public int getWeightsFilterWidth() {
		return weightsFilterWidth;
	}

	public int getWeightsFilterWidthDefault() { // XXX review this mechanism with marius

		return Math.min(getTotalMarkers(), Math.min(35, Math.max(3,
				(int) Math.ceil(getTotalMarkers() * 0.05))));
	}

	public int getMarkersToKeep() {
		return markersToKeep;
	}

	public int getMarkersToKeepDefault() { // XXX review this mechanism with marius

		return Math.min(getTotalMarkers(), Math.min(20, Math.max(3,
				(int) Math.ceil(getTotalMarkers() * 0.02))));
	}
}
