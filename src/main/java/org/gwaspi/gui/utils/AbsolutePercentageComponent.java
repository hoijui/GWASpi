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

import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * TODO add class description
 */
public class AbsolutePercentageComponent extends JPanel {

	private final JSpinner value;
	private final JSpinner percentage;
	private final JLabel percentageLabel;
	private JCheckBox defaultIndicator;
	private AbsolutePercentageComponentRelation componentRelation;
	private AbsolutePercentageModel model;

	public AbsolutePercentageComponent() {

		this.value = new JSpinner();
		this.percentage = new JSpinner();
		this.percentageLabel = new JLabel();
		this.defaultIndicator = null;
		this.componentRelation = null;

		add(this.value);
		add(this.percentage);
		add(this.percentageLabel);

		final FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);
		setLayout(contentPanelLayout);

		this.percentageLabel.setText("%");

		this.validate();
	}

	public void setAbsoluteEnabled(boolean absoluteEnabled) {

		value.setEnabled(absoluteEnabled);
	}

	public void setPercentageEnabled(boolean absoluteEnabled) {

		percentage.setEnabled(absoluteEnabled);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		value.setEnabled(enabled);
		percentage.setEnabled(enabled);
		defaultIndicator.setEnabled(enabled);
	}

	private void clear() {

		value.setModel(new SpinnerNumberModel());
		percentage.setModel(new SpinnerNumberModel());

		if (defaultIndicator != null) {
			remove(defaultIndicator);
			defaultIndicator = null;
		}

		validate();

		setEnabled(false);
	}

	public void setModel(AbsolutePercentageModel model) {

		if (model == null) {
			clear();
		} else {
			final SpinnerModel valueModel = new SpinnerNumberModel(
					model.getInitialValue().doubleValue(), // initial value
					model.getMinimumValue().doubleValue(), // min
					model.getMaximumValue().doubleValue(), // max
					model.getStepSize().doubleValue()); // step
			value.setModel(valueModel);

			final SpinnerModel percentageModel = new SpinnerNumberModel(
					model.getInitialValue().doubleValue() / model.getTotalValue().doubleValue() * 100.0, // initial value
					model.getMinimumValue().doubleValue() / model.getTotalValue().doubleValue() * 100.0, // min
					model.getMaximumValue().doubleValue() / model.getTotalValue().doubleValue() * 100.0, // max
					model.getStepSize().doubleValue() / model.getTotalValue().doubleValue() * 100.0); // step
			percentage.setModel(percentageModel);

			if ((model.getDefaultValue() == null) && (defaultIndicator != null)) {
				remove(defaultIndicator);
				defaultIndicator = null;
			} else if ((model.getDefaultValue() != null) && (defaultIndicator == null)) {
				defaultIndicator = new JCheckBox();
				add(defaultIndicator);
			}
			if (model.getDefaultValue() != null) {
				defaultIndicator.setAction(new SpinnerDefaultAction(value, model.getDefaultValue().doubleValue()));
			}

			componentRelation
					= new AbsolutePercentageComponentRelation(
					new ValueContainer<Number>(value),
					new ValueContainer<Number>(percentage),
					model.getTotalValue());

			validate();
		}

		this.model = model;
	}

	public int getValue() {
		return ((Number) value.getValue()).intValue();
	}

	public double getFractionValue() {
		return getPercentageValue() / 100.0;
	}

	public double getPercentageValue() {
		return ((Number) percentage.getValue()).doubleValue();
	}
}
