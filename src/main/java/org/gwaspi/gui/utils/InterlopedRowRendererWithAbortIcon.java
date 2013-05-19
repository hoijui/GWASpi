package org.gwaspi.gui.utils;

import java.awt.Component;
import javax.swing.JTable;

public class InterlopedRowRendererWithAbortIcon extends InterlopedRowRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		setAbortIcon(this, table, row, column);

		return this;
	}
}
