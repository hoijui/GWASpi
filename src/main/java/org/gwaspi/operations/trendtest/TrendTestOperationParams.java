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

package org.gwaspi.operations.trendtest;

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

public class TrendTestOperationParams extends AbstractOperationParams {

	private final OperationKey markerCensusOpKey;

	public TrendTestOperationParams(final OPType type, OperationKey hardyWeinbergExcludeOpKey, String name, OperationKey markerCensusOpKey) {
		super(type, new DataSetKey(hardyWeinbergExcludeOpKey), name);

		this.markerCensusOpKey = markerCensusOpKey;
	}

	public TrendTestOperationParams(final OPType type, OperationKey hardyWeinbergExcludeOpKey, OperationKey markerCensusOpKey) {
		this(type, hardyWeinbergExcludeOpKey, null, markerCensusOpKey);
	}

	public TrendTestOperationParams(OperationKey hardyWeinbergExcludeOpKey, String name, OperationKey markerCensusOpKey) {
		this(OPType.TRENDTEST, hardyWeinbergExcludeOpKey, name, markerCensusOpKey);
	}

	public TrendTestOperationParams(OperationKey hardyWeinbergExcludeOpKey, OperationKey markerCensusOpKey) {
		this(OPType.TRENDTEST, hardyWeinbergExcludeOpKey, null, markerCensusOpKey);
	}

	public OperationKey getMarkerCensus() {
		return markerCensusOpKey;
	}

	@Override
	protected String getNameDefault() {
		return "Cochran-Armitage Trend test operation";
	}
}
