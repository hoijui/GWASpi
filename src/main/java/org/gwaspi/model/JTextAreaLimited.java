/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.model;

import javax.print.attribute.AttributeSet;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class JTextAreaLimited extends PlainDocument {
    private static int MAX_LENGTH = 50;
    private JTextArea tField;
    private int limit;
    public JTextAreaLimited(int limit) {
        super();
        this.limit = limit;
    }

    /**
     *
     * @param offset
     * @param str
     * @param attr
     * @throws BadLocationException
     */

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null)
          return;

        if ((getLength() + str.length()) <= limit) {
            super.insertString(offset, str,(javax.swing.text.AttributeSet) attr);
        }
    }


}
