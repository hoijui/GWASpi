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

package org.gwaspi.operations.hardyweinberg;

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;

public class HardyWeinbergOperationParams extends AbstractOperationParams {

	private final OperationKey markersQAOpKey;

	public HardyWeinbergOperationParams(OperationKey markerCensusOPKey, String name, final OperationKey markersQAOpKey) {
		super(OPType.HARDY_WEINBERG, new DataSetKey(markerCensusOPKey), name);

		this.markersQAOpKey = markersQAOpKey;
	}

	public HardyWeinbergOperationParams(OperationKey markerCensusOPKey, final OperationKey markersQAOpKey) {
		this(markerCensusOPKey, null, markersQAOpKey);
	}

	@Override
	protected String getNameDefault() {
		return "Hardy & Weinberg test operation (calculates P and Heterozygosity values)";
	}

	public OperationKey getMarkersQAOpKey() {
		return markersQAOpKey;
	}
}
