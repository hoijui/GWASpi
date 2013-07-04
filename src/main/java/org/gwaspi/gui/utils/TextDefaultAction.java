/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Allows to reset the value of a text component to its default.
 * In case of a toggle-button as actuator, it also buffers the custom value,
 * and later goes back to that value.
 */
public class TextDefaultAction extends AbstractDefaultAction<JTextComponent, String> implements DocumentListener {

	public TextDefaultAction(JTextComponent valueComponent, String defaultValue, boolean makeUneditable) {
		super(valueComponent, defaultValue, makeUneditable);

		valueComponent.getDocument().addDocumentListener(this);
	}

	public TextDefaultAction(JTextComponent valueComponent, String defaultValue) {
		this(valueComponent, defaultValue, false);
	}

	@Override
	protected void setValue(String value) {
		getValueComponent().setText(value);
	}

	@Override
	protected String getValue() {
		return getValueComponent().getText();
	}

	@Override
	protected void setValueComponentEditable(boolean editable) {
		getValueComponent().setEditable(editable);
	}

	@Override
	public void insertUpdate(DocumentEvent evt) {
		valueChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent evt) {
		valueChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent evt) {
		valueChanged();
	}
}