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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Selects all of the source text component, whenever it gains focus.
 * This may only be used with {@link JTextComponent} and its subclasses.
 */
public class SelectAllTextFocusListener implements FocusListener {

	private final String defaultText;

	/**
	 * Only select text on focus gained if it equals defaultText.
	 */
	public SelectAllTextFocusListener(final String defaultText) {

		this.defaultText = defaultText;
	}

	/**
	 * Always select text on focus gained.
	 */
	public SelectAllTextFocusListener() {
		this(null);
	}

	private JTextComponent extractSource(final FocusEvent evt) {

		if (!(evt.getSource() instanceof JTextComponent)) {
			throw new IllegalStateException(
					getClass().getSimpleName()
					+ " can only be used with sources of type "
					+ JTextComponent.class.getSimpleName()
					+ ", but it is here used with "
					+ evt.getSource().getClass().getSimpleName());
		}
		return ((JTextComponent) evt.getSource());
	}

	@Override
	public void focusGained(final FocusEvent evt) {

		final JTextComponent sourceTxtComp = extractSource(evt);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if ((defaultText == null) || sourceTxtComp.getText().equals(defaultText)) {
					sourceTxtComp.selectAll();
				}
			}
		});
	}

	@Override
	public void focusLost(final FocusEvent evt) {

		final JTextComponent sourceTxtComp = extractSource(evt);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sourceTxtComp.select(0, 0);
			}
		});
	}
}
