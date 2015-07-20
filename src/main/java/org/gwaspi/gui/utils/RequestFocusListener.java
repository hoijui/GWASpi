/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.gui.utils;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Makes sure that a component gets focus when it first comes into view.
 */
public class RequestFocusListener implements HierarchyListener, AncestorListener {

	/**
	 * Makes sure that the supplied component gets focus when it first comes into view.
	 * @param component to receive the focus
	 */
	public static void applyOn(final JComponent component) {

		final RequestFocusListener requestFocusListener = new RequestFocusListener();
		component.addHierarchyListener(requestFocusListener);
		component.addAncestorListener(requestFocusListener);
	}

	@Override
	public void hierarchyChanged(final HierarchyEvent evt) {

		final Component component = evt.getComponent();
		if (component.isShowing() && (evt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
			final Window toplevel = SwingUtilities.getWindowAncestor(component);
			toplevel.addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowGainedFocus(final WindowEvent winEvt) {
					component.requestFocus();
//					component.requestFocusInWindow();
					toplevel.removeWindowFocusListener(this);
				}
			});
//			component.removeHierarchyListener(this);
		}
	}

	@Override
	public void ancestorAdded(final AncestorEvent evt) {

		final AncestorListener listener = this;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final JComponent component = (JComponent) evt.getComponent();
				component.requestFocusInWindow();
				component.removeAncestorListener(listener);
			}
		});
	}

	@Override
	public void ancestorMoved(final AncestorEvent evt) {
	}

	@Override
	public void ancestorRemoved(final AncestorEvent evt) {
	}
}
