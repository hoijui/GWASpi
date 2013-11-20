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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import org.gwaspi.netCDF.operations.OperationFactory;

public class BaseNetCdfOperationDataSet<ET> extends AbstractNetCdfOperationDataSet<ET> {

	public BaseNetCdfOperationDataSet() {
		super();
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {
		throw new UnsupportedOperationException("This function should never be used!");
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<ET> writeBuffer) throws IOException {
		throw new UnsupportedOperationException("This function should never be used!");
	}

	@Override
	public Collection<ET> getEntries(int from, int to) throws IOException {
		throw new UnsupportedOperationException("This function should never be used!");
	}
}
