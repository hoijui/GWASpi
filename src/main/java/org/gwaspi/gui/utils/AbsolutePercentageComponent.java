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
 * TODO document this class!
 */
public class AbsolutePercentageComponent extends JPanel {

	private final JSpinner value;
	private final JSpinner percentage;
	private final JLabel percentageLabel;
	private final JCheckBox defaultIndicator;
	private AbsolutePercentageComponentRelation componentRelation;

	public AbsolutePercentageComponent(
			final int initialValue,
			final int defaultValue,
			final int totalValue, // 100%
			final int minimumValue,
			final int step,
			final int maximumValue)
	{
		this.value = new JSpinner();
		this.percentage = new JSpinner();
		this.percentageLabel = new JLabel();
		this.defaultIndicator = new JCheckBox();
		this.componentRelation = null;

		add(this.value);
		add(this.percentage);
		add(this.percentageLabel);
		add(this.defaultIndicator);

		FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);
		setLayout(contentPanelLayout);

		this.percentageLabel.setText("%");

		final SpinnerModel valueModel = new SpinnerNumberModel(
				initialValue, // initial value
				minimumValue, // min
				maximumValue, // max
				step); // step
		this.value.setModel(valueModel);

		final SpinnerModel percentageModel = new SpinnerNumberModel(
				(double) initialValue / totalValue * 100.0, // initial value
				minimumValue / totalValue * 100.0, // min
				maximumValue / totalValue * 100.0, // max
				step / totalValue * 100.0); // step
		this.percentage.setModel(percentageModel);

		this.defaultIndicator.setAction(new SpinnerDefaultAction(this.value, defaultValue));

		this.componentRelation
				= new AbsolutePercentageComponentRelation(
				new ValueContainer<Number>(value),
				new ValueContainer<Number>(percentage),
				totalValue);

		this.validate();
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
