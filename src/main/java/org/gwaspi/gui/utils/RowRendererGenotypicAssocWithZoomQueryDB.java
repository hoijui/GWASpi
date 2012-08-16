/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author u49878
 */
public class RowRendererGenotypicAssocWithZoomQueryDB extends DefaultTableCellRenderer {

	private URL ZoomIconPath = getClass().getResource("/resources/zoom2_20x20.png");
	private URL queryDBIconPath = getClass().getResource("/resources/arrow_20x20.png");

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

		ImageIcon ico;
		if (column == 10) {
			ico = new ImageIcon(ZoomIconPath);
			setHorizontalAlignment(SwingConstants.CENTER);
			TableColumn col = table.getColumnModel().getColumn(column);
			col.setPreferredWidth(45);
		} else if (column == 11) {
			ico = new ImageIcon(queryDBIconPath);
			setHorizontalAlignment(SwingConstants.CENTER);
			TableColumn col = table.getColumnModel().getColumn(column);
			col.setPreferredWidth(80);
		} else {
			ico = null;
			setHorizontalAlignment(SwingConstants.LEFT);
		}
		setIcon(ico);

		return this;
	}
}
