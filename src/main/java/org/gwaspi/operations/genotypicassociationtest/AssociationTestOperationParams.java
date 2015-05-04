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

import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.trendtest.TrendTestOperationParams;
import org.gwaspi.reports.OutputTest;

/**
 * Whether we are to perform allelic or genotypic association tests
 * is indicated by the 'type' member.
 */
public class AssociationTestOperationParams extends TrendTestOperationParams {

	public AssociationTestOperationParams(
			final OPType type,
			final DataSetKey parent,
			final String name,
			final OperationKey markerCensusOPKey)
	{
		super(type, parent, name, markerCensusOPKey);
	}

	public AssociationTestOperationParams(
			final OPType type,
			final DataSetKey parent,
			final OperationKey markerCensusOPKey)
	{
		this(type, parent, null, markerCensusOPKey);
	}

	@Override
	protected String getNameDefault() {
//		return "Association test of type: " + type.name();
		return OutputTest.createTestName(getType()) + " Test";
	}
}
