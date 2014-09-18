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
 * Signals that a sub-process was replaced by an other one.
 * @see SuperProgressListener
 */
public class SubProcessReplacedEvent extends EventObject {

	private final int index;
	private final ProgressSource replacedSubProcess;
	private final ProgressSource replacingSubProcess;
	private final Double replacedWeight;
	private final Double replacingWeight;

	public SubProcessReplacedEvent(
			ProgressSource source,
			final int index,
			ProgressSource replacedSubProcess,
			ProgressSource replacingSubProcess,
			final Double replacedWeight,
			final Double replacingWeight)
	{
		super(source);

		this.index = index;
		this.replacedSubProcess = replacedSubProcess;
		this.replacingSubProcess = replacingSubProcess;
		this.replacedWeight = replacedWeight;
		this.replacingWeight = replacingWeight;
	}

	/**
	 * Getter for the index of the sub process(eS) in the SuperProgressSource.
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public ProgressSource getSource() {
		return (ProgressSource) super.getSource();
	}

	public ProgressSource getReplacedSubProcess() {
		return replacedSubProcess;
	}

	public ProgressSource getReplacingSubProcess() {
		return replacingSubProcess;
	}

	public Double getReplacedWeight() {
		return replacedWeight;
	}

	public Double getReplacingWeight() {
		return replacingWeight;
	}
}
