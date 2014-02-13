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

package org.gwaspi.operations.genotypicassociationtest;

import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.reports.OutputTest;

public class AssociationTestOperationParams extends TrendTestOperationParams {

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final OPType type;

	public AssociationTestOperationParams(OperationKey hardyWeinbergExcludeOpKey, String name, OperationKey markerCensusOPKey, OPType type) {
		super(hardyWeinbergExcludeOpKey, name, markerCensusOPKey);

		this.type = type;
	}

	public AssociationTestOperationParams(OperationKey hardyWeinbergExcludeOpKey, OperationKey markerCensusOPKey, OPType type) {
		this(hardyWeinbergExcludeOpKey, null, markerCensusOPKey, type);
	}

	public OPType getType() {
		return type;
	}

	@Override
	protected String getNameDefault() {
//		return "Association test of type: " + type.name();
		return OutputTest.createTestName(getType()) + " Test";
	}
}
