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

/**
 * Lets through only one progress happened event per X milliseconds; forwards all other events.
 * @param <ST> the status type
 */
public class PerTimeIntervalFilteredProgressListener<ST> extends ForwardingProgressListener<ST> {

	private final long reportIntervalMillis;
	private long lastReportedProgressMillis;

	public PerTimeIntervalFilteredProgressListener(final ProgressListener<ST> innerListener, final long reportIntervalMillis) {
		super(innerListener);

		this.reportIntervalMillis = reportIntervalMillis;
		this.lastReportedProgressMillis = 0;
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {

		final long currentMillis = System.currentTimeMillis();
		if ((currentMillis - lastReportedProgressMillis) >= reportIntervalMillis) {
			getInnerListener().progressHappened(evt);
			lastReportedProgressMillis = currentMillis;
		}
	}
}
