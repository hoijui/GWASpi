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
package org.gwaspi.operations.combi;

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * TODO
 */
public class GenotypeEncoderChooserGUI /*extends JOptionPane*/ {

	private static final String TITLE = "Choose the GenotypeEncoder to use";
	private static final Object[] ALL_ENCODERS = new Object[] {
		new AllelicGenotypeEncoder(),
		new GenotypicGenotypeEncoder(),
		new NominalGenotypeEncoder()
	};

	public GenotypeEncoderChooserGUI() {

//		setMessageType(JOptionPane.QUESTION_MESSAGE);
//		setOptionType(JOptionPane.OK_CANCEL_OPTION);
//
//		Object[] genotypeEncoders = new Object[] {
//			new AllelicGenotypeEncoder(),
//			new GenotypicGenotypeEncoder(),
//			new NominalGenotypeEncoder()
//		};
//		setOptions(genotypeEncoders);
//		setInitialValue(genotypeEncoders[0]);
	}

	public static GenotypeEncoder chooseGenotypeEncoder(Component parentComponent) {

//		GenotypeEncoderChooserGUI optionPane = new GenotypeEncoderChooserGUI();
//		JDialog dialog = optionPane.createDialog(parentComponent, TITLE);
//		dialog.setVisible(true);
//		dialog.dispose();
//		GenotypeEncoder selectedValue = (GenotypeEncoder) optionPane.getValue();

		GenotypeEncoder selectedValue = (GenotypeEncoder) JOptionPane.showInputDialog(
				parentComponent,
				TITLE, // no message, title is enough
				TITLE,
				JOptionPane.QUESTION_MESSAGE,
				null, // use the default icon
				ALL_ENCODERS,
				ALL_ENCODERS[0]);

		return selectedValue;
	}

	public static GenotypeEncoder chooseGenotypeEncoder() {
		return chooseGenotypeEncoder(null);
	}
}
