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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

/**
 * Allows to reset the value of a check-box component to its default.
 * In case of a toggle-button as actuator, it also buffers the custom value,
 * and later goes back to that value.
 */
public class CheckBoxDefaultAction extends AbstractDefaultAction<JCheckBox, Boolean> implements ItemListener {

	public CheckBoxDefaultAction(final JCheckBox valueComponent, final Boolean defaultValue, boolean makeUneditable) {
		super(valueComponent, defaultValue, makeUneditable);

		valueComponent.addItemListener(this);

		putValue(NAME, "default");
	}

	public CheckBoxDefaultAction(final JCheckBox valueComponent, final Boolean defaultValue) {
		this(valueComponent, defaultValue, false);
	}

	@Override
	protected void setValue(final Boolean value) {
		getValueComponent().setSelected(value);
	}

	@Override
	protected Boolean getValue() {
		return getValueComponent().isSelected();
	}

	@Override
	protected void setValueComponentEditable(boolean editable) {
		getValueComponent().setEnabled(editable);
	}

	@Override
	public void itemStateChanged(ItemEvent evt) {
		valueChanged();
	}
}
