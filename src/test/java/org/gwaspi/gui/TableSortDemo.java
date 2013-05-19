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

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class TableSortDemo {

	public static void main(String[] args) {
		Object[][] data = {{"A", 5}, {"B", 2}, {"C", 4}, {"D", 8}};
		String columnNames[] = {"Item", "Value"};
		TableModel model = new DefaultTableModel(data, columnNames) {
			@Override
			public Class<?> getColumnClass(int column) {
				return getValueAt(0, column).getClass();
			}
		};
		JTable table = new JTable(model);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		JFrame frame = new JFrame("Sorting Table");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(scrollPane);
		frame.setSize(300, 200);
		frame.setVisible(true);
	}
}
