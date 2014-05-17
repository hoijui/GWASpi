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

package org.gwaspi.progress;

import java.util.Collections;
import java.util.List;

public final class NullProgressHandler<ST> implements ProgressHandler<ST> {

	private final ProcessInfo processInfo;
	private Integer numIntervals;

	public NullProgressHandler(ProcessInfo processInfo) {

		this.processInfo = processInfo;
		this.numIntervals = 1;
	}

	@Override
	public ProcessInfo getInfo() {
		return processInfo;
	}

	@Override
	public Integer getNumIntervals() {
		return numIntervals;
	}

	@Override
	public void setNumIntervals(Integer numIntervals) {
		this.numIntervals = numIntervals;
	}

	@Override
	public void addProgressListener(ProgressListener lst) {}

	@Override
	public void removeProgressListener(ProgressListener lst) {}

	@Override
	public List<ProgressListener> getProgressListeners() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public void setNewStatus(ProcessStatus newStatus) {}

	@Override
	public void setProgress(ST currentProgress) {}
}
