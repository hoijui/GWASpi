package org.gwaspi.gui.utils;


import java.awt.Component;
import javax.swing.JTable;

/**
 *
 * @author u49878
 */
public class RowRendererAllelicAssocWithZoomQueryDB extends RowRendererDefault {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		setZoomAndQueryDbIcons(this, table, column, 9, 10);

		return this;
	}
}
