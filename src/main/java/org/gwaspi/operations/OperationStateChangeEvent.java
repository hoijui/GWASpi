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

package org.gwaspi.operations;

import java.util.EventObject;
import org.gwaspi.progress.ProcessStatus;

public class OperationStateChangeEvent extends EventObject {

	private final ProcessStatus oldState;
	private final ProcessStatus newState;
	private final String description;

	public OperationStateChangeEvent(
			MatrixOperation source,
			ProcessStatus oldState,
			ProcessStatus newState,
			String description)
	{
		super(source);

		this.oldState = oldState;
		this.newState = newState;
		this.description = description;
	}

	public MatrixOperation getOperation() {
		return (MatrixOperation) getSource();
	}

	public ProcessStatus getOldState() {
		return oldState;
	}

	public ProcessStatus getNewState() {
		return newState;
	}

	public String getDescription() {
		return description;
	}
}
