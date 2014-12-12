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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * TODO add class description
 */
public class MinMaxDoubleVerifier extends InputVerifier {

	private final double min;
	private final double max;

	public MinMaxDoubleVerifier(double min, double max) {

		this.min = min;
		this.max = max;
	}

	@Override
	public boolean verify(JComponent input) {

		boolean valid;

	   JTextField textField = (JTextField) input;
	   String valueStr = textField.getText();
	   try {
			double valueDouble = Double.parseDouble(valueStr);
			valid = ((valueDouble >= min) && (valueDouble <= max));
	   } catch (NumberFormatException ex) {
		   valid = false;
	   }

	   return valid;
	}
}
