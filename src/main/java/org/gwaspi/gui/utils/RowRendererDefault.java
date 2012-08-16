/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author u49878
 */
public class RowRendererDefault extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		Color bg;
		if (!selected) {
			bg = (row % 2 == 0 ? org.gwaspi.constants.cGlobal.alternateRowColor : org.gwaspi.constants.cGlobal.background);
		} else {
			bg = org.gwaspi.constants.cGlobal.selectionBackground;
		}
		setBackground(bg);

		Color fg;
		if (selected) {
			fg = org.gwaspi.constants.cGlobal.selectionForeground;
		} else {
			fg = org.gwaspi.constants.cGlobal.foreground;
		}
		setForeground(fg);

		return this;
	}
}
