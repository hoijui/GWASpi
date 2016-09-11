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

import java.util.Collections;
import java.util.List;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parameters for the {@link CombiTestMatrixOperation}.
 */
public class CombiOutputOperationParams extends AbstractOperationParams {

	private static final Logger LOG = LoggerFactory.getLogger(CombiOutputOperationParams.class);

	private final OperationKey combiOperationKey;
	/**
	 * The minimum number of markers that have to lie between to towers (peaks)
	 * to count them as separate/different towers.
	 */
	private Integer minPeakDistance;
	/**
	 * The maximum P-value values to consider for output.
	 * @see org.gwaspi.reports.TestOutputParams#getPValueThreasholds
	 */
	private final List<Double> pValueThreasholds;

	public CombiOutputOperationParams(
			final OperationKey trendTestOperationKey,
			final OperationKey combiOperationKey,
			final Integer minPeakDistance,
			final List<Double> pValueThreasholds,
			final String name)
	{
		super(OPType.COMBI_OUTPUT, new DataSetKey(trendTestOperationKey), name);

		this.combiOperationKey = (combiOperationKey == null)
				? getCombiOperationKeyDefault(trendTestOperationKey)
				: combiOperationKey;
		this.minPeakDistance = (minPeakDistance == null)
				? getMinPeakDistanceDefault()
				: minPeakDistance;
		this.pValueThreasholds = (pValueThreasholds == null)
				? getPValueThreasholdsDefault()
				: pValueThreasholds;
	}

	public CombiOutputOperationParams(final OperationKey trendTestOperationKey) {

		this(
				trendTestOperationKey,
				null,
				null,
				null,
				null
				);
	}

	/** @deprecated */
	public OperationKey getTrendTestOperationKey() {
		return getParent().getOperationParent();
	}

	public OperationKey getCombiOperationKey() {
		return combiOperationKey;
	}

	public static OperationKey getCombiOperationKeyDefault(final OperationKey trendTestOperationKey) {
//		return XXX;
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	public Integer getMinPeakDistance() {
		return minPeakDistance;
	}

	public static Integer getMinPeakDistanceDefault() {
		return 150; // TODO maybe adjust this?
	}

	/**
	 * The maximum P-value values to consider for output.
	 * @see org.gwaspi.reports.TestOutputParams#getPValueThreasholds
	 */
	public List<Double> getPValueThreasholds() {
		return pValueThreasholds;
	}

	public static List<Double> getPValueThreasholdsDefault() {
		return Collections.singletonList(1.0);
	}

	@Override
	protected String getNameDefault() {
		return "Combi-Test output for matrix " + getParent().getOrigin().toString(); // TODO use nicer matrix name!
	}
}
