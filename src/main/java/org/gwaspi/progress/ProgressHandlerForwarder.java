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

package org.gwaspi.progress;

public class ProgressHandlerForwarder<ST> extends ProgressForwarder<ST>
		implements ProgressHandler<ST>
{
	private ProgressHandler<ST> innerProgressHandler;

	public ProgressHandlerForwarder(ProcessInfo processInfo) {
		super(processInfo);

		this.innerProgressHandler = null;
	}

	public void setInnerProgressHandler(ProgressHandler<ST> innerProgressHandler) {

		this.innerProgressHandler = innerProgressHandler;
		setInnerProgressSource(innerProgressHandler);
	}

	@Override
	public void setNewStatus(ProcessStatus newStatus) {
		innerProgressHandler.setNewStatus(newStatus);
	}

	@Override
	public void setProgress(ST currentProgress) {
		innerProgressHandler.setProgress(currentProgress);
	}
}
