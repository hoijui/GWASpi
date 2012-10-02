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
public class IntegerInputVerifier extends InputVerifier {

	private final Logger log = LoggerFactory.getLogger(IntegerInputVerifier.class);

	public IntegerInputVerifier() {
	}

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
