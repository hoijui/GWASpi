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

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableSortDemo2 extends JFrame {

	private JTable table = new JTable();
	private MyTableModel tableModel;
	private JLabel titleLabel = new JLabel("Click table header to sort the column.");

	public TableSortDemo2() {
		setSize(600, 300);

		tableModel = new MyTableModel();

		getContentPane().add(titleLabel, BorderLayout.NORTH);
		table.setModel(tableModel);

		JTableHeader header = table.getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.addMouseListener(tableModel.new ColumnListener(table));
		header.setReorderingAllowed(true);

		JScrollPane ps = new JScrollPane();
		ps.getViewport().add(table);
		getContentPane().add(ps, BorderLayout.CENTER);

		WindowListener wndCloser = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		addWindowListener(wndCloser);
		setVisible(true);
	}

	public static void main(String argv[]) {
		new TableSortDemo2();
	}
}

class MyTableModel extends AbstractTableModel {

	private int sortCol = 0;
	private boolean isSortAsc = true;
	private int m_result = 0;
	private int columnsCount = 1;
	private List<Integer> vector = new ArrayList<Integer>();

	MyTableModel() {
		vector.clear();
		vector.add(new Integer(24976600));
		vector.add(new Integer(24));
		vector.add(new Integer(2497));
		vector.add(new Integer(249766));
		vector.add(new Integer(2497660));
		vector.add(new Integer(6600));
		vector.add(new Integer(76600));
		vector.add(new Integer(976600));
		vector.add(new Integer(4976600));
	}

	public int getRowCount() {
		return vector == null ? 0 : vector.size();
	}

	public int getColumnCount() {
		return columnsCount;
	}

	@Override
	public String getColumnName(int column) {
		String str = "data";
		if (column == sortCol) {
			str += isSortAsc ? " >>" : " <<";
		}
		return str;
	}

	@Override
	public boolean isCellEditable(int nRow, int nCol) {
		return false;
	}

	public Object getValueAt(int nRow, int nCol) {
		if (nRow < 0 || nRow >= getRowCount()) {
			return "";
		}
		if (nCol > 1) {
			return "";
		}
		return vector.get(nRow);
	}

	public String getTitle() {
		return "data ";
	}

	class ColumnListener extends MouseAdapter {

		protected JTable table;

		ColumnListener(JTable t) {
			table = t;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0) {
				return;
			}
			if (sortCol == modelIndex) {
				isSortAsc = !isSortAsc;
			} else {
				sortCol = modelIndex;
			}

			for (int i = 0; i < columnsCount; i++) {
				TableColumn column = colModel.getColumn(i);
				column.setHeaderValue(getColumnName(column.getModelIndex()));
			}
			table.getTableHeader().repaint();

			Collections.sort(vector, new MyComparator(isSortAsc));
			table.tableChanged(new TableModelEvent(MyTableModel.this));
			table.repaint();
		}
	}
}

class MyComparator implements Comparator {

	private boolean isSortAsc;

	MyComparator(boolean sortAsc) {
		isSortAsc = sortAsc;
	}

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof Integer) || !(o2 instanceof Integer)) {
			return 0;
		}
		Integer s1 = (Integer) o1;
		Integer s2 = (Integer) o2;
		int result = s1.compareTo(s2);
		if (!isSortAsc) {
			result = -result;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MyComparator) {
			MyComparator compObj = (MyComparator) obj;
			return compObj.isSortAsc == isSortAsc;
		}
		return false;
	}
}