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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Tries to show pretty much all information that is available about a given process
 * (excluding info about possible children).
 * @param <ST> the status type
 */
public class SimpleSwingProgressListener<ST> extends AbstractProgressListener<ST> implements SwingProgressListener<ST> {

	private final JPanel main;
	private final JProgressBar bar;

	public SimpleSwingProgressListener(ProgressSource progressSource) {

		this.main = new JPanel();
		this.main.setLayout(new BorderLayout());

		final JPanel superDisplay = new JPanel();
		superDisplay.setLayout(new BorderLayout());

		final JPanel superInfo = new JPanel();
		final JLabel superInfoName = new JLabel();
		// XXX There has to be more info here, and it may has to be dynamically updated
		superInfoName.setText(progressSource.getInfo().getShortName());
		superInfoName.setToolTipText(progressSource.getInfo().getDescription());
		superDisplay.add(superInfo, BorderLayout.SOUTH);

		final JPanel barContainer = new JPanel();
		this.bar = new JProgressBar();
		barContainer.setLayout(new BorderLayout());
		barContainer.add(bar, BorderLayout.CENTER);
		superDisplay.add(barContainer, BorderLayout.SOUTH);

		this.main.add(superDisplay, BorderLayout.NORTH);
	}

	@Override
	public JComponent getMainComponent() {
		return main;
	}

	@Override
	public void processDetailsChanged(ProcessDetailsChangeEvent evt) {

		bar.setMaximum(evt.getProgressSource().getNumIntervals());
	}

	@Override
	public void progressHappened(ProgressEvent<ST> evt) {

		bar.setValue(evt.getIntervalIndex());
	}
}
