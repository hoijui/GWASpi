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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedLengthDocument extends PlainDocument {

	private final int limit;

	public LimitedLengthDocument(final int limit) {
		this.limit = limit;
	}

	LimitedLengthDocument(final int limit, final boolean upper) {
		this.limit = limit;
	}

	@Override
	public void insertString(final int offset, final String str, final AttributeSet attr)
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
	public void replace(final int offs, final int length, final String str, final AttributeSet attr)
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
