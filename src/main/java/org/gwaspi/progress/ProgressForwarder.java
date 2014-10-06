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

public class ProgressForwarder<ST> extends AbstractProgressSource<ST> implements ProgressListener<ST> {

	private ProgressSource<ST> progressSource;

	public ProgressForwarder(ProcessInfo processInfo) {
		super(processInfo, -1);

		this.progressSource = new NullProgressHandler<ST>(processInfo);
	}

	public void setInnerProgressSource(ProgressSource<ST> progressSource) {

		if (this.progressSource != null) {
			this.progressSource.removeProgressListener(this);
		}
		this.progressSource = progressSource;
		progressSource.addProgressListener(this);
		setInfo(progressSource.getInfo());
//		fireProcessDetailsChanged(); // done by the call above already!
	}

	@Override
	public void processDetailsChanged(ProcessDetailsChangeEvent evt) {
		fireProcessDetailsChanged(evt);
	}

	@Override
	public void statusChanged(ProcessStatusChangeEvent evt) {
		fireStatusChanged(evt);
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {
		fireProgressHappened(evt);
	}
}
