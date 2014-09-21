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
	/**
	 * Whether we can show a meaningful/useful completion ratio.
	 * We can do that, if the number of intervals is known, and more then 1.
	 * If it is only 1, it makes more sense to signal "active"
	 * instead of showing the actual progress, which could be only 0% or 100%.
	 */
	private boolean indeterminateProgress;

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
		indeterminateProgress = ((numIntervals == null) || (numIntervals == 1));
		if (indeterminateProgress) {
			bar.setMaximum(100);
		} else {
			bar.setMaximum(numIntervals);
		}
		resetIndeterminateBarState(progressSource);
	}

	private void resetIndeterminateBarState(ProgressSource progressSource) {
		bar.setIndeterminate(progressSource.getStatus().isActive()
				&& (indeterminateProgress || (progressSource.getStatus() != ProcessStatus.RUNNING)));
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

		final ProgressSource progressSource = evt.getSource();

		numIntervalsChanged(progressSource);
		updateToolTipText(progressSource);
	}

	@Override
	public void statusChanged(ProcessStatusChangeEvent evt) {
		super.statusChanged(evt);

		updateToolTipText(evt.getProgressSource());
		bar.setForeground(statusToColor(evt.getNewStatus())); // the color of the bar before the current value
//		bar.setBackground(statusToColor(evt.getNewStatus())); // the color of the bar after the current value
		bar.setIndeterminate(evt.getNewStatus().isActive());
		if (indeterminateProgress) {
			final int progressValue;
			switch (evt.getNewStatus()) {
				case NONE: progressValue = 0; break;
				case INITIALIZING: progressValue = 10; break;
				case FAILED: progressValue = 20; break;
				case ABORTED: progressValue = 30; break;
				case RUNNING: progressValue = 50; break;
				case PAUSED: progressValue = 50; break;
				case FINALIZING: progressValue = 90; break;
				case COMPLEETED: progressValue = 100; break;
				default: throw new UnsupportedOperationException("Unsupported status: " + evt.getNewStatus());
			}
			bar.setValue(progressValue);
		}
		resetIndeterminateBarState(evt.getProgressSource());
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {
		super.progressHappened(evt);

		if (!indeterminateProgress) {
			bar.setValue(evt.getIntervalIndex() + 1);
		}
	}
}
