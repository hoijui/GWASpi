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

import java.awt.Toolkit;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerInputVerifier extends InputVerifier {

	private final Logger log = LoggerFactory.getLogger(IntegerInputVerifier.class);

	public boolean verify(JComponent input) {
		boolean correct = true;
		try {
			JTextField tField = (JTextField) input;
			String text = tField.getText();
			if (text.length() == 0) {
				return true;
			}

			correct = isInteger(text);
//			if (correct) {
//				correct = isFormatCorrect(text);
//			}
		} finally {
			if (!correct) {
				Toolkit.getDefaultToolkit().beep();
				log.warn("Must be integer!");
			}
		}
		return correct;
	}

	private boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
}
