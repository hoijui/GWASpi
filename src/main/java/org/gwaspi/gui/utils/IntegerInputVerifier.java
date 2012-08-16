/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui.utils;

import java.awt.Toolkit;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class IntegerInputVerifier extends InputVerifier {

	private int lengthBeforeDot = 1;
	private int lengthAfterDot = 10;

	public IntegerInputVerifier() {
	}

	public IntegerInputVerifier(int lengthBeforeDot, int lengthAfterDot) {
		this.lengthAfterDot = lengthAfterDot;
		this.lengthBeforeDot = lengthBeforeDot;
	}

	public boolean verify(JComponent input) {
		boolean correct = true;
		try {
			JTextField tField = (JTextField) input;
			String text = tField.getText();
			if (text.length() == 0) {
				return true;
			}

			if ((correct = isInteger(text))) {
				//correct = isFormatCorrect(text);
			}

		} finally {
			if (!correct) {
				Toolkit.getDefaultToolkit().beep();
				System.out.println("Must be integer!");
			}
		}
		return correct;
	}

	private boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
