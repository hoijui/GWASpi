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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Allows access different GUI components that contain a value of some sort,
 * to be used through a single class.
 * This usage deals only with the value
 * (setting, getting, listening to changes).
 * @param <V> type of the values this container hosts
 */
public class ValueContainer<V> {

	private final JComponent contInner;
	private final List<ChangeListener> changeListeners;

	public ValueContainer(JSpinner container) {

		this.contInner = container;
		this.changeListeners = new ArrayList<ChangeListener>();

		container.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				fireValueChanged();
			}
		});
	}

	public ValueContainer(JTextField container) {

		this.contInner = container;
		this.changeListeners = new ArrayList<ChangeListener>();

		container.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						fireValueChanged();
					}
				});
	}

	public ValueContainer(JComboBox container) {

		this.contInner = container;
		this.changeListeners = new ArrayList<ChangeListener>();

		container.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				fireValueChanged();
			}
		});
	}

	public void addChangeListener(ChangeListener lst) {
		changeListeners.add(lst);
	}

	public void removeChangeListener(ChangeListener lst) {
		changeListeners.remove(lst);
	}

	private void fireValueChanged() {

		ChangeEvent evt = new ChangeEvent(ValueContainer.this);
		for (ChangeListener changeListener : changeListeners) {
			changeListener.stateChanged(evt);
		}
	}

	public void setValue(V value) {

		if (contInner instanceof JSpinner) {
			((JSpinner) contInner).setValue(value);
		} else if (contInner instanceof JTextField) {
			((JTextField) contInner).setText(value.toString());
		} else if (contInner instanceof JComboBox) {
			((JComboBox) contInner).setSelectedItem(value);
		} else {
			throw new IllegalStateException();
		}
	}

	public V getValue() {

		V value;

		if (contInner instanceof JSpinner) {
			value = (V) ((JSpinner) contInner).getValue();
		} else if (contInner instanceof JTextField) {
			value = (V) ((JTextField) contInner).getText();
		} else if (contInner instanceof JComboBox) {
			value = (V) ((JComboBox) contInner).getSelectedItem();
		} else {
			throw new IllegalStateException();
		}

		return value;
	}

	public JComponent getInnerContainer() {
		return contInner;
	}
}
