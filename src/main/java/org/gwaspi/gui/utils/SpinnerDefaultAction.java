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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JSpinner;

/**
 * Allows to reset the value of a spinner component to its default.
 * In case of a toggle-button as actuator, it also buffers the custom value,
 * and later goes back to that value.
 */
public class SpinnerDefaultAction extends AbstractDefaultAction<JSpinner, Object> implements PropertyChangeListener {

	public SpinnerDefaultAction(JSpinner valueComponent, Object defaultValue, boolean makeUneditable) {
		super(valueComponent, defaultValue, makeUneditable);

		valueComponent.addPropertyChangeListener(this);
	}

	public SpinnerDefaultAction(JSpinner valueComponent, Object defaultValue) {
		this(valueComponent, defaultValue, false);
	}

	@Override
	protected void setValue(Object value) {
		getValueComponent().setValue(value);
	}

	@Override
	protected Object getValue() {
		return getValueComponent().getValue();
	}

	@Override
	protected void setValueComponentEditable(boolean editable) {
		((JSpinner.DefaultEditor) getValueComponent().getEditor()).getTextField().setEditable(editable);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		valueChanged();
	}
}