package org.gwaspi.gui.utils;


import java.awt.Component;
import javax.swing.JTable;

public class RowRendererGenotypicAssocWithZoomQueryDB extends RowRendererDefault {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		setZoomAndQueryDbIcons(this, table, column, 10, 11);

		return this;
	}
}
