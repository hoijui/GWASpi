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
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gwaspi.global.Extractor;

/**
 * Allows to connect two GUI components, one holding the absolute value,
 * the other holding the same value in percentages,
 * to update one components value, if it changed in the other.
 */
public class AbsolutePercentageComponentRelation<C extends JComponent, V>
		implements PropertyChangeListener, ChangeListener
{
	private final ValueContainer absoluteComponent;
	private final ValueContainer percentageComponent;
	private final Number totalValue;
	private final Extractor<Number, Number> absoluteConstrainer;
	private final Extractor<Number, Number> percentageConstrainer;

	public static class RoundingConstrainer
			implements Extractor<Number, Number>
	{
		private final double roundingMultiplier;

		public RoundingConstrainer(double roundingMultiplier) {
			this.roundingMultiplier = roundingMultiplier;
		}

		@Override
		public Number extract(Number object) {
			return Math.round(object.doubleValue() * roundingMultiplier) / roundingMultiplier;
		}
	}

	public static class NullConstrainer implements Extractor<Number, Number> {

		public NullConstrainer() {}

		@Override
		public Number extract(Number object) {
			return object;
		}
	}

	public AbsolutePercentageComponentRelation(
			ValueContainer absoluteComponent,
			ValueContainer percentageComponent,
			Number totalValue,
			Extractor<Number, Number> absoluteConstrainer,
			Extractor<Number, Number> percentageConstrainer)
	{
		this.absoluteComponent = absoluteComponent;
		this.percentageComponent = percentageComponent;
		this.totalValue = totalValue;
		this.absoluteConstrainer = absoluteConstrainer;
		this.percentageConstrainer = percentageConstrainer;

//		this.absoluteComponent.addPropertyChangeListener("value", this);
//		this.percentageComponent.addPropertyChangeListener("value", this);
		this.absoluteComponent.addChangeListener(this);
		this.percentageComponent.addChangeListener(this);
	}

	public AbsolutePercentageComponentRelation(
			ValueContainer absoluteComponent,
			ValueContainer percentageComponent,
			Number totalValue)
	{
		this(
			absoluteComponent,
			percentageComponent,
			totalValue,
			new RoundingConstrainer(1.0),
			new NullConstrainer());
	}

	private Number absoluteToPercentage(Number absoluteValue) {

		double rawPercentageValue = absoluteValue.doubleValue() / totalValue.doubleValue() * 100.;
		return percentageConstrainer.extract(rawPercentageValue);
	}

	private Number percentageToAbsolute(Number percentageValue) {

		double rawAbsoluteValue = totalValue.doubleValue() * percentageValue.doubleValue() / 100.0;
		return absoluteConstrainer.extract(rawAbsoluteValue);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		valueChanged(evt.getSource());
	}

	@Override
	public void stateChanged(ChangeEvent evt) {

		valueChanged(evt.getSource());
	}

	private void valueChanged(Object changedComponent) {

		if (changedComponent.equals(absoluteComponent)) {
			Number newPercentage = absoluteToPercentage((Number) absoluteComponent.getValue());
			if (!newPercentage.equals(percentageComponent.getValue())) {
				percentageComponent.setValue(newPercentage);
			}
		} else if (changedComponent.equals(percentageComponent)) {
			Number newAbsolute = percentageToAbsolute((Number) percentageComponent.getValue());
			if (!newAbsolute.equals(absoluteComponent.getValue())) {
				absoluteComponent.setValue(newAbsolute);
			}
		} else {
			throw new RuntimeException();
		}
	}
}
