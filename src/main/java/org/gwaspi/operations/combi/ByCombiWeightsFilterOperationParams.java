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

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

public class ByCombiWeightsFilterOperationParams extends AbstractOperationParams {

	/**
	 * The number of total markers in the matrix we operate on, unfiltered.
	 */
	private Integer totalMarkers;
	/**
	 * Whether to run the filtering once per chromosome, or rather over the whole genome at once.
	 */
	private final Boolean perChromosome;
	/**
	 * The filter-width of the moving average filter (p-norm-filter)
	 * applied to the weights after SVM training.
	 */
	private final int weightsFilterWidth;
	/**
	 * How many markers to be left with,
	 * after the filtering with the COMBI method.
	 * It is set to -1 if {@link #markersToKeepFraction} should be used instead.
	 * NOTE This value is used as per sub-data-set,
	 *   whether it is the whole genome or just the data of a single chromosome!
	 */
	private final int markersToKeep;
	/**
	 * How many markers to be left with, as a fraction of the (sub-)data-set,
	 * after the filtering with the COMBI method.
	 * @see #markersToKeep
	 */
	private final double markersToKeepFraction;

	ByCombiWeightsFilterOperationParams(
			Integer totalMarkers,
			OperationKey combiParentOpKey,
			final Boolean perChromosome,
			Integer weightsFilterWidth,
			Integer markersToKeep,
			final Double markersToKeepFraction,
			String name)
	{
		super(OPType.FILTER_BY_WEIGHTS, new DataSetKey(combiParentOpKey), name);

		this.totalMarkers = totalMarkers;

		if ((markersToKeep != null) && (markersToKeepFraction != null)) {
			throw new IllegalArgumentException(
					"You may specify at most one of \"markersToKeep\" "
							+ "and \"markersToKeepFraction\".");
		} else if (markersToKeepFraction != null) {
			if ((markersToKeepFraction <= 0.0) || (markersToKeepFraction > 1.0)) {
				throw new IllegalArgumentException(
						"The valid range for \"markersToKeepFraction\" is (0.0, 1.0].");
			}
			markersToKeep = (int) Math.ceil(totalMarkers * markersToKeepFraction);
		}

		this.perChromosome = (perChromosome == null)
				? isPerChromosomeDefault()
				: perChromosome;
		this.weightsFilterWidth = ((weightsFilterWidth == null)
				|| (weightsFilterWidth <= 0) || (weightsFilterWidth >= totalMarkers))
				? getWeightsFilterWidthDefault()
				: weightsFilterWidth;
		this.markersToKeep =
				((markersToKeep == null) || (markersToKeep <= 0) || (markersToKeep >= totalMarkers))
				? -1
				: markersToKeep;
		this.markersToKeepFraction = ((this.markersToKeep == -1) && (markersToKeepFraction == null))
				? getMarkersToKeepFractionDefault()
				: (markersToKeepFraction == null) ? -1 : markersToKeepFraction;
	}

	public ByCombiWeightsFilterOperationParams(
			OperationKey combiParentOpKey,
			final Boolean perChromosome,
			Integer weightsFilterWidth,
			Integer markersToKeep,
			final Double markersToKeepFraction,
			String name)
	{
		this(null, combiParentOpKey, perChromosome, weightsFilterWidth, markersToKeep, markersToKeepFraction, name);
	}

	public ByCombiWeightsFilterOperationParams(
			int totalMarkers,
			final Boolean perChromosome,
			Integer weightsFilterWidth,
			Integer markersToKeep,
			final Double markersToKeepFraction,
			String name)
	{
		this(totalMarkers, null, perChromosome, weightsFilterWidth, markersToKeep, markersToKeepFraction, name);
	}

	public ByCombiWeightsFilterOperationParams(OperationKey combiParentOpKey) {
		this(
				combiParentOpKey,
				null,
				null,
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
		return "Exclude by low COMBI weight";
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
				(int) Math.ceil(getTotalMarkers() * getMarkersToKeepFractionDefault()))));
	}

	public double getMarkersToKeepFraction() {
		return markersToKeepFraction;
	}

	public double getMarkersToKeepFractionDefault() { // XXX review this with marius
		return 0.02;
	}

	public int getMarkersToKeep(final int totalMarkers) {

		final int relativeMarkersToKeep;
		if (getMarkersToKeep() == -1) {
			relativeMarkersToKeep = (int) Math.ceil(getMarkersToKeepFraction() * totalMarkers);
		} else {
			relativeMarkersToKeep = getMarkersToKeep();
		}

		return relativeMarkersToKeep;
	}

	public boolean isPerChromosome() {
		return perChromosome;
	}

	public static boolean isPerChromosomeDefault() {
		return true;
	}
}
