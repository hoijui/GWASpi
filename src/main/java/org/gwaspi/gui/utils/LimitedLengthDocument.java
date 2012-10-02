package org.gwaspi.gui.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LimitedLengthDocument extends PlainDocument {

	private int limit;

	public LimitedLengthDocument(int limit) {
		this.limit = limit;
	}

	LimitedLengthDocument(int limit, boolean upper) {
		this.limit = limit;
	}

	@Override
	public void insertString(int offset, String str, AttributeSet attr)
			throws BadLocationException
	{
		if (str == null) {
			return;
		}

		// This rejects the entire insertion if it would make
		// the contents too long. Another option would be
		// to truncate the inserted string so the contents
		// would be exactly maxCharacters in length.
		int newLength = getLength() + str.length();
		if (newLength <= limit) {
			super.insertString(offset, str, attr);
		}
	}

	@Override
	public void replace(int offs, int length, String str, AttributeSet attr)
			throws BadLocationException
	{
		// This rejects the entire replacement if it would make
		// the contents too long. Another option would be
		// to truncate the replacement string so the contents
		// would be exactly maxCharacters in length.
		int newLength = getLength() + str.length() - length;
		if (newLength <= limit) {
			super.replace(offs, length, str, attr);
		} else {
			int tooMuch = Math.max(0, newLength - limit);
			super.replace(offs, length, str.substring(0, length - tooMuch), attr);
		}
	}
}
