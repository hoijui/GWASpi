package org.gwaspi.gui.utils;


import java.awt.Component;
import javax.swing.JTable;

public class RowRendererTrendTestWithZoomQueryDB extends RowRendererDefault {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		setZoomAndQueryDbIcons(this, table, column, 8, 9);

		return this;
	}
}
