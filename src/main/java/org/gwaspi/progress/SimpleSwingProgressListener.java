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

import java.awt.BorderLayout;
import javax.swing.JProgressBar;

/**
 * Tries to show pretty much all information that is available
 * about a given process (excluding info about possible children).
 * @param <ST> the status type
 */
public class SimpleSwingProgressListener<ST>
		extends AbstractSwingProgressListener<ST>
{
	private final JProgressBar bar;
	private boolean numIntervallsKnown;

	public SimpleSwingProgressListener(ProgressSource progressSource) {
		super(progressSource);

		this.bar = new JProgressBar();
		numIntervalsChanged(progressSource);
		updateToolTipText(progressSource);

		getContentContainer().add(bar, BorderLayout.CENTER);
	}

	@Override
	public JProgressBar getProgressBar() {
		return bar;
	}

	private void numIntervalsChanged(ProgressSource progressSource) {

		final Integer numIntervals = progressSource.getNumIntervals();
		numIntervallsKnown = (numIntervals != null);
		if (numIntervallsKnown) {
			bar.setMaximum(numIntervals);
		}
	}

	protected void setToolTipText(String toolTipText) {

		bar.setToolTipText(toolTipText);
		getMainComponent().setToolTipText(toolTipText);
	}

	protected void updateToolTipText(final ProgressSource progressSource) {

		setToolTipText(progressSource.getInfo().getShortName()
				+ " - " + progressSource.getStatus().toString());
	}

	@Override
	public void processDetailsChanged(ProcessDetailsChangeEvent evt) {
		super.processDetailsChanged(evt);

		final ProgressSource progressSource = evt.getProgressSource();

		numIntervalsChanged(progressSource);
		updateToolTipText(progressSource);
	}

	@Override
	public void statusChanged(ProcessStatusChangeEvent evt) {
		super.statusChanged(evt);

		updateToolTipText(evt.getProgressSource());
		bar.setIndeterminate(!numIntervallsKnown && evt.getNewStatus().isActive());
		bar.setForeground(statusToColor(evt.getNewStatus())); // the color of the bar before the current value
//		bar.setBackground(statusToColor(evt.getNewStatus())); // the color of the bar after the current value
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {
		super.progressHappened(evt);

		if (numIntervallsKnown) {
			bar.setValue(evt.getIntervalIndex() + 1);
		}
	}
}
