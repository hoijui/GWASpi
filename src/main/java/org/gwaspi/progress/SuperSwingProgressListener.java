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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Tries to show pretty much all information that is available about a given process
 * and its sub-processes, if there are any.
 * @param <ST> the status type
 */
public class SuperSwingProgressListener<ST> extends AbstractProgressListener<ST> implements SwingProgressListener<ST> {

	private final SuperProgressSource progressSource;
	private final JPanel main;
	private final JPanel subProgressDisplay;
	private final Map<ProgressSource, JProgressBar> subProgressSourcesAndProgressBarParts;
	private final Map<ProgressSource, SwingProgressListener> subProgressSourcesAndDisplay;
	private SwingProgressListener subProgressSourcesDisplay;

	protected SuperSwingProgressListener(SuperProgressSource progressSource) {

		final Map<ProgressSource, Double> subProgressSourcesAndWeights
				= progressSource.getSubProgressSourcesAndWeights();
		final int numSubs = subProgressSourcesAndWeights.size();

		this.progressSource = progressSource;
		this.main = new JPanel();
		this.subProgressDisplay = new JPanel();
		this.subProgressSourcesAndProgressBarParts = new HashMap<ProgressSource, JProgressBar>(numSubs);
		this.subProgressSourcesAndDisplay = new HashMap<ProgressSource, SwingProgressListener>(numSubs);
		this.subProgressSourcesDisplay = null;

		this.main.setLayout(new BorderLayout());

		final JPanel superDisplay = new JPanel();
		superDisplay.setLayout(new BorderLayout());

		final JPanel superInfo = new JPanel();
		final JLabel superInfoName = new JLabel();
		// XXX There has to be more info here, and it may has to be dynamically updated
		superInfoName.setText(progressSource.getInfo().getShortName());
		superInfoName.setToolTipText(progressSource.getInfo().getDescription());
		superDisplay.add(superInfo, BorderLayout.SOUTH);

		final JPanel superBar = new JPanel();
		final GridBagLayout mainLayout = new GridBagLayout();
		superBar.setLayout(mainLayout);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		for (Map.Entry<ProgressSource, Double> subProgressSourceAndWeight : subProgressSourcesAndWeights.entrySet()) {
			final ProgressSource subProgressSource = subProgressSourceAndWeight.getKey();

			final JProgressBar subProgressBar = new JProgressBar();
			subProgressSourcesAndProgressBarParts.put(subProgressSource, subProgressBar);
			gbc.weightx = subProgressSourceAndWeight.getValue();
			superBar.add(subProgressBar, gbc);

			visualizeStatus(subProgressBar, subProgressSource, subProgressSource.getStatus());

			subProgressSource.addProgressListener(this);
		}
		superDisplay.add(superBar, BorderLayout.SOUTH);

		this.main.add(superDisplay, BorderLayout.NORTH);

		this.main.add(subProgressDisplay, BorderLayout.CENTER);

//		JLabel lbl20 = new JLabel("20%");
//		lbl20.setOpaque(true);
//		lbl20.setBackground(Color.RED);
//		gbc.weightx = 0.2;
//		this.main.add(lbl20, gbc);
//
//		JLabel lbl10 = new JLabel("10%");
//		lbl10.setOpaque(true);
//		lbl10.setBackground(Color.GREEN);
//		gbc.weightx = 0.1;
//		this.main.add(lbl10, gbc);
//
//		JLabel lbl70 = new JLabel("70%");
//		lbl70.setOpaque(true);
//		lbl70.setBackground(Color.BLUE);
//		gbc.weightx = 0.7;
//		this.main.add(lbl70, gbc);
	}

	@Override
	public void statusChanged(ProcessStatusChangeEvent evt) {
		visualizeStatus(subProgressSourcesAndProgressBarParts.get(evt.getProgressSource()), evt.getProgressSource(), evt.getNewStatus());
	}

	public static SwingProgressListener newDisplay(final ProgressSource progressSource) {

		if (progressSource instanceof SuperProgressSource) {
			return new SuperSwingProgressListener((SuperProgressSource) progressSource);
		} else {
			return new SimpleSwingProgressListener(progressSource);
		}
	}

	private void visualizeStatus(final JProgressBar subProgressBar, final ProgressSource progressSource, final ProcessStatus status) {

			subProgressBar.setToolTipText(String.valueOf(status));

			if (status.isEnd()) {
				subProgressBar.setValue(100);
			}
			if (status.isActive()) {
				subProgressBar.setIndeterminate(status.isActive());
				SwingProgressListener subDisplay = subProgressSourcesAndDisplay.get(progressSource);
				if (subDisplay == null) {
					subDisplay = newDisplay(progressSource);
					subProgressSourcesAndDisplay.put(progressSource, subDisplay);
				}
				if (subProgressSourcesDisplay != subDisplay) { // is not yet in details display
					if (subProgressSourcesDisplay != null) {
						// remove the previous display
						main.remove(subProgressSourcesDisplay.getMainComponent());
					}
					main.add(subDisplay.getMainComponent(), BorderLayout.CENTER);
					subProgressSourcesDisplay = subDisplay;
				}
			}

			final Color newColor;
			switch (status) {
				case ABORTED:
					newColor = Color.ORANGE;
					break;
				case FAILED:
					newColor = Color.RED;
					break;
				case COMPLEETED:
					newColor = Color.BLUE;
					break;
				case PAUSED:
					newColor = Color.YELLOW;
					break;
				case INITIALIZING:
					newColor = Color.WHITE;
					break;
				case RUNNING:
					newColor = Color.GREEN;
					break;
				case FINALIZING:
					newColor = Color.CYAN;
					break;
				default:
					newColor = Color.BLACK;
			}
			subProgressBar.setForeground(newColor);
	}

	@Override
	public JComponent getMainComponent() {
		return main;
	}

	public static void main(String[] args) {

		SuperSwingProgressListener superSwingProgressListener
				= new SuperSwingProgressListener(null);

		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(superSwingProgressListener.getMainComponent(), BorderLayout.CENTER);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
