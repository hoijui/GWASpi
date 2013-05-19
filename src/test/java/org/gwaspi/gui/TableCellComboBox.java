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

package org.gwaspi.gui;

import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableCellComboBox extends JPanel {

	public TableCellComboBox() {
		super(new GridLayout(1, 0));

		JTable table = new JTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		// Add some columns
		model.addColumn("A", new Object[]{"item1"});
		model.addColumn("B", new Object[]{"item2"});

		// These are the combobox values
		String[] values = new String[]{"item1", "item2", "item3"};

		// Set the combobox editor on the 1st visible column
		int vColIndex = 0;
		TableColumn col = table.getColumnModel().getColumn(vColIndex);
		col.setCellEditor(new MyComboBoxEditor(values));

		// If the cell should appear like a combobox in its
		// non-editing state, also set the combobox renderer

		col.setCellRenderer(new MyComboBoxRenderer(values));

		// Add the scroll pane to this panel.
		add(table);
	}

	public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

		public MyComboBoxRenderer(String[] items) {
			super(items);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}

			// Select the current value
			setSelectedItem(value);
			return this;
		}
	}

	public static class MyComboBoxEditor extends DefaultCellEditor {

		public MyComboBoxEditor(String[] items) {
			super(new JComboBox(items));
		}
	}

	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TableRenderDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		TableCellComboBox newContentPane = new TableCellComboBox();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
