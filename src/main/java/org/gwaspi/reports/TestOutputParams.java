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

package org.gwaspi.reports;

import java.io.IOException;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.AbstractOperationParams;

/**
 * Parameters for the {@link OutputTest}.
 */
public class TestOutputParams extends AbstractOperationParams {

	private final OPType testType;
	private final OperationKey qaMarkersOpKey;

	public TestOutputParams(OperationKey testOpKey, OPType testType, OperationKey qaMarkersOpKey) {
		super(null /* NOTE it is built on testType, but not that! */, new DataSetKey(testOpKey), null);

		this.testType = testType;
		this.qaMarkersOpKey = qaMarkersOpKey;
	}

	public TestOutputParams(OperationKey testOpKey, OperationKey qaMarkersOpKey) throws IOException {
		this(testOpKey, OperationsList.getOperationService().getOperationMetadata(testOpKey).getOperationType(), qaMarkersOpKey);
	}

	/** @deprecated */
	public OperationKey getTestOperationKey() {
		return getParent().getOperationParent();
	}

	public OPType getTestType() {
		return testType;
	}

	public OperationKey geQaMarkersOpKey() {
		return qaMarkersOpKey;
	}

	@Override
	protected String getNameDefault() {
		return "Output to file for test: " + getParent().toString();
	}
}
