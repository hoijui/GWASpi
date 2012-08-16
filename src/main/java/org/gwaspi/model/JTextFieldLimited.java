package org.gwaspi.model;

import javax.swing.text.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class JTextFieldLimited extends PlainDocument {

	private int limit;

	public JTextFieldLimited(int limit) {
		super();
		this.limit = limit;
	}

	/**
	 *
	 * @param offs
	 * @param str
	 * @param a
	 * @throws BadLocationException
	 */
	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {


		//This rejects the entire insertion if it would make
		//the contents too long. Another option would be
		//to truncate the inserted string so the contents
		//would be exactly maxCharacters in length.
		if (str.length() <= limit) {
			super.insertString(offs, str, a);
		} else {
			super.insertString(offs, str.substring(0, limit - 1), a);
		}
	}

	@Override
	public void replace(int offs,
			int length,
			String str,
			AttributeSet a)
			throws BadLocationException {
		//This rejects the entire replacement if it would make
		//the contents too long. Another option would be
		//to truncate the replacement string so the contents
		//would be exactly maxCharacters in length.
		if ((str.length() - length) <= limit) {
			super.replace(offs, length, str, a);
		} else {
			super.replace(offs, length, str.substring(0, limit - 1), a);
		}
	}
}