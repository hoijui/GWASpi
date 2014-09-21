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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Tries to show pretty much all information that is available
 * about a given process (excluding info about possible children).
 * @param <ST> the status type
 */
public abstract class AbstractSwingProgressListener<ST>
		extends AbstractProgressListener<ST>
		implements SwingProgressListener<ST>
{
	private final JPanel main;
	private final JPanel infoContainer;
	private final JPanel contentContainer;

	public AbstractSwingProgressListener(ProgressSource progressSource) {

		this.main = new JPanel();
		this.main.setLayout(new BorderLayout());

		final JPanel display = new JPanel();
		display.setLayout(new BorderLayout());

		this.infoContainer = new JPanel();
		this.infoContainer.setLayout(new BorderLayout());
		display.add(this.infoContainer, BorderLayout.SOUTH);
		updateInfos(progressSource);

		this.contentContainer = new JPanel();
		this.contentContainer.setLayout(new BorderLayout());
		display.add(this.contentContainer, BorderLayout.CENTER);
		this.main.add(display, BorderLayout.NORTH);
	}

	public static Color statusToColor(final ProcessStatus status) {

		final Color statusColor;

		switch (status) {
			case ABORTED:
				statusColor = Color.ORANGE;
				break;
			case FAILED:
				statusColor = Color.RED;
				break;
			case COMPLEETED:
				statusColor = Color.BLUE;
				break;
			case PAUSED:
				statusColor = Color.YELLOW;
				break;
			case INITIALIZING:
				statusColor = Color.WHITE;
				break;
			case RUNNING:
				statusColor = Color.GREEN;
				break;
			case FINALIZING:
				statusColor = Color.CYAN;
				break;
			default:
				statusColor = Color.BLACK;
		}

		return statusColor;
	}

	protected JPanel getContentContainer() {
		return contentContainer;
	}

	private void updateInfos(final ProgressSource progressSource) {

		infoContainer.removeAll();

		// XXX There has to be more info here, and it may has to be dynamically updated in the processDetailsChanged method
		final JLabel infoName = new JLabel();
		infoName.setText(progressSource.getInfo().getShortName());
		infoName.setToolTipText(progressSource.getInfo().getDescription());
		infoContainer.add(infoName, BorderLayout.CENTER);
	}

	@Override
	public JComponent getMainComponent() {
		return main;
	}

	@Override
	public void processDetailsChanged(ProcessDetailsChangeEvent evt) {
		updateInfos(evt.getSource());
	}
}
