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
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.progress.ProcessInfo;

public abstract class AbstractOperation<P extends OperationParams, R>
		implements MatrixOperation<P, R>
{
	private final List<OperationListener> operationListeners;
	private final DataSetDestination dataSetDestination;

	public AbstractOperation(DataSetDestination dataSetDestination) {

		this.operationListeners = new ArrayList<OperationListener>();
		this.dataSetDestination = dataSetDestination;
	}

	public AbstractOperation() {
		this(null);
	}

	public abstract ProcessInfo getProcessInfo();

	@Override
	public void addOperationListener(OperationListener listener) {
		operationListeners.add(listener);
	}

	@Override
	public void removeOperationListener(OperationListener listener) {
		operationListeners.remove(listener);
	}

	protected void fireOperationStatusChanged(OperationStateChangeEvent evt) {

		for (OperationListener operationListener : operationListeners) {
			operationListener.operationStatusChanged(evt);
		}
	}

	@Override
	public boolean isValid() throws IOException {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public P getParams() {
		throw new UnsupportedOperationException("Not supported yet for all operations (only for those creating operations as a result).");
	}

	public DataSetDestination getDataSetDestination() {
		return dataSetDestination;
	}
}
