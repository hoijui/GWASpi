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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Tries to show pretty much all information that is available
 * about a given process and its sub-processes, if there are any.
 * @param <ST> the status type
 */
public class SuperSwingProgressListener<ST>
		extends SimpleSwingProgressListener<ST>
		implements SuperProgressListener<ST>
{
	private final Map<ProgressSource, SubTaskProgressListener> subProgressSourcesToContainer;
//	private SwingProgressListener subProgressSourcesDisplay;
	private final JPanel activeSubDisplays;
	private final JPanel subBars;
	private final GridBagConstraints subBarsGBC;

	protected SuperSwingProgressListener(SuperProgressSource progressSource) {
		super(progressSource);

		final Map<ProgressSource, Double> subProgressSourcesAndWeights
				= progressSource.getSubProgressSourcesAndWeights();
		final int numSubs = subProgressSourcesAndWeights.size();

		this.subProgressSourcesToContainer
				= new HashMap<ProgressSource, SubTaskProgressListener>(numSubs);
//		this.subProgressSourcesDisplay = null;

		final JPanel subContainer = new JPanel();
		subContainer.setLayout(new BorderLayout());
		getContentContainer().add(subContainer, BorderLayout.SOUTH);

		this.subBars = new JPanel();
		final GridBagLayout mainLayout = new GridBagLayout();
		this.subBars.setLayout(mainLayout);
		this.subBarsGBC = new GridBagConstraints();
		this.subBarsGBC.fill = GridBagConstraints.HORIZONTAL;
		subContainer.add(this.subBars, BorderLayout.NORTH);

		this.activeSubDisplays = new JPanel();
		this.activeSubDisplays.setLayout(new FlowLayout());
		subContainer.add(this.activeSubDisplays, BorderLayout.SOUTH);
	}

	@Override
	public void subProcessAdded(final SubProcessAddedEvent evt) {

		final ProgressSource subProgressSource = evt.getAddedSubProcess();

		final SwingProgressListener gui = newDisplay(subProgressSource);
		subProgressSource.addProgressListener(gui);

		subBarsGBC.weightx = evt.getWeight();
		subBars.add(gui.getProgressBar(), subBarsGBC, evt.getIndex());

		final SubTaskProgressListener subTaskProgressListener
				= new SubTaskProgressListener(subProgressSource, gui);
		subProgressSourcesToContainer.put(subProgressSource, subTaskProgressListener);

		visualizeSubTaskStatus(subTaskProgressListener);
		subProgressSource.addProgressListener(subTaskProgressListener);
	}

	@Override
	public void subProcessReplaced(final SubProcessReplacedEvent evt) {

		subProcessRemoved(new SubProcessRemovedEvent(evt.getSource(), evt.getIndex(), evt.getReplacedSubProcess()));
		subProcessAdded(new SubProcessAddedEvent(evt.getSource(), evt.getIndex(), evt.getReplacingSubProcess(), evt.getReplacingWeight()));
	}

	@Override
	public void subProcessRemoved(final SubProcessRemovedEvent evt) {

		final ProgressSource subProgressSource = evt.getRemovedSubProcess();

		final SubTaskProgressListener subTaskProgressListener
				= subProgressSourcesToContainer.get(subProgressSource);

		subBars.remove(subTaskProgressListener.getGui().getProgressBar());
		if (subTaskProgressListener.isWasActive()) {
			activeSubDisplays.remove(subTaskProgressListener.getGui().getMainComponent());
		}
		subTaskProgressListener.getProgressSource().removeProgressListener(subTaskProgressListener);
		subProgressSourcesToContainer.remove(subProgressSource);
	}

	private class SubTaskProgressListener extends AbstractProgressListener<ST> {

		private final ProgressSource progressSource;
//		private final JProgressBar progressBar;
		private final SwingProgressListener gui;
		private boolean wasActive;

		SubTaskProgressListener(final ProgressSource progressSource, /*final JProgressBar progressBar, */final SwingProgressListener gui) {

			this.progressSource = progressSource;
//			this.progressBar = progressBar;
			this.gui = gui;
			this.wasActive = progressSource.getStatus().isActive();
		}

		public ProgressSource getProgressSource() {
			return progressSource;
		}

//		public JProgressBar getProgressBar() {
//			return progressBar;
//		}

//		public void setGui(SwingProgressListener gui) {
//			this.gui = gui;
//		}

		public SwingProgressListener getGui() {
			return gui;
		}

		public void setWasActive(boolean wasActive) {
			this.wasActive = wasActive;
		}

		public boolean isWasActive() {
			return wasActive;
		}

		@Override
		public void statusChanged(ProcessStatusChangeEvent evt) {
			visualizeSubTaskStatus(this);
		}
	}

	public static SwingProgressListener newDisplay(final ProgressSource progressSource) {

		SwingProgressListener swingProgressListener;

		if (progressSource instanceof SuperProgressSource) {
			final SuperProgressSource superProgressSource = (SuperProgressSource) progressSource;
			swingProgressListener = new SuperSwingProgressListener(superProgressSource);
			superProgressSource.addSuperProgressListener((SuperProgressListener) swingProgressListener, true);
		} else {
			swingProgressListener = new SimpleSwingProgressListener(progressSource);
		}
//		progressSource.addProgressListener(swingProgressListener);

		return swingProgressListener;
	}

	private void visualizeSubTaskStatus(final SubTaskProgressListener subTaskProgressListener) {

//		final JProgressBar subProgressBar = subTaskProgressListener.getProgressBar();
		final boolean wasActive = subTaskProgressListener.isWasActive();
		final boolean isActive = subTaskProgressListener.getProgressSource().getStatus().isActive();

//		final SwingProgressListener subDisplay = subTaskProgressListener.getGui();
//		subProgressBar.setToolTipText(String.valueOf(status));
//
//		if (status.isEnd()) {
//			subProgressBar.setValue(100);
//		}
		if (!wasActive && isActive) {
			activeSubDisplays.add(subTaskProgressListener.getGui().getMainComponent());
//			subProgressBar.setIndeterminate(status.isActive());
//			SwingProgressListener subDisplay = subTaskProgressListener.getGui();
//			if (subDisplay == null) {
//				subDisplay = newDisplay(subTaskProgressListener.getProgressSource());
//				subTaskProgressListener.setGui(subDisplay);
//			}
//			if (subProgressSourcesDisplay != subDisplay) { // is not yet in details display
//				if (subProgressSourcesDisplay != null) {
//					// remove the previous display
//					getContentContainer().remove(subProgressSourcesDisplay.getMainComponent());
//				}
//				getContentContainer().add(subDisplay.getMainComponent(), BorderLayout.CENTER);
//				getContentContainer().getTopLevelAncestor().validate();
//				subProgressSourcesDisplay = subDisplay;
//			}
		} else if (wasActive && !isActive) {
			activeSubDisplays.remove(subTaskProgressListener.getGui().getMainComponent());
		}

		if (wasActive != isActive) {
			// actualize GUI
			final Container topLevelAncestor = getContentContainer().getTopLevelAncestor();
			// only validate if we are actually in some container,
			// as otherwise we are not visible anyway
			if (topLevelAncestor != null) {
				topLevelAncestor.validate();
			}
			// actualize state for next time
			subTaskProgressListener.setWasActive(isActive);
		}

//		subProgressBar.setForeground(statusToColor(status));
	}
}
