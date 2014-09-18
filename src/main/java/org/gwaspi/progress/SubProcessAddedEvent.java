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

import java.util.EventObject;

/**
 * Signals that a sub-process was added.
 * @see SuperProgressListener
 */
public class SubProcessAddedEvent extends EventObject {

	private final int index;
	private final ProgressSource addedSubProcess;
	private final Double weight;

	public SubProcessAddedEvent(
			ProgressSource source,
			final int index,
			ProgressSource addedSubProcess,
			Double weight)
	{
		super(source);

		this.index = index;
		this.addedSubProcess = addedSubProcess;
		this.weight = weight;
	}

	/**
	 * Getter for the index of the sub process in the SuperProgressSource.
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public ProgressSource getSource() {
		return (ProgressSource) super.getSource();
	}

	public ProgressSource getAddedSubProcess() {
		return addedSubProcess;
	}

	public Double getWeight() {
		return weight;
	}
}
