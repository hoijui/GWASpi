package org.gwaspi.gui.utils;

import java.awt.Toolkit;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class DoubleInputVerifier extends InputVerifier {

	private final Logger log = LoggerFactory.getLogger(DoubleInputVerifier.class);

	public DoubleInputVerifier() {
	}

	public boolean verify(JComponent input) {
		boolean correct = true;
		try {
			JTextField tField = (JTextField) input;
			String text = tField.getText();
			if (text.length() == 0) {
				return true;
			}

			correct = isDoubleOrFloat(text);
//			if (correct) {
//				correct = isFormatCorrect(text);
//			}
		} finally {
			if (!correct) {
				Toolkit.getDefaultToolkit().beep();
				log.warn("Must be numeric!");
			}
		}
		return correct;
	}

	private boolean isDoubleOrFloat(String text) {
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
}
