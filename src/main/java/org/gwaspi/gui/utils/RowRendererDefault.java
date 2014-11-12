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

package org.gwaspi.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.gwaspi.constants.cGlobal;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.threadbox.SwingDeleterItem;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItem;
import org.gwaspi.threadbox.SwingWorkerItemList;

public class RowRendererDefault extends DefaultTableCellRenderer {

	private static final URL ICON_PATH_ZOOM     = RowRendererDefault.class.getResource("/img/icon/zoom2_20x20.png");
	private static final URL ICON_PATH_QUERY_DB = RowRendererDefault.class.getResource("/img/icon/arrow_20x20.png");
	private static final URL ICON_PATH_ABORT    = RowRendererDefault.class.getResource("/img/icon/abort_16x16.png");
	private static final URL ICON_PATH_NO_ABORT = RowRendererDefault.class.getResource("/img/icon/abort-grey_16x16.png");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focused, row, column);

		setColors(this, selected, row);

		return this;
	}

	protected static void setZoomAndQueryDbIcons(DefaultTableCellRenderer tableCellRenderer, JTable table, int column, int zoomColumn, int queryDbColumn) {

		ImageIcon ico;
		if (column == zoomColumn) {
			ico = new ImageIcon(ICON_PATH_ZOOM);
			tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			TableColumn col = table.getColumnModel().getColumn(column);
			col.setPreferredWidth(45);
		} else if (column == queryDbColumn) {
			ico = new ImageIcon(ICON_PATH_QUERY_DB);
			tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			TableColumn col = table.getColumnModel().getColumn(column);
			col.setPreferredWidth(80);
		} else {
			ico = null;
			tableCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		}
		tableCellRenderer.setIcon(ico);
	}

	protected static void setAbortIcon(DefaultTableCellRenderer tableCellRenderer, JTable table, int row, int column) {

		if (table.getColumnModel().getColumnCount() == 8) {
			ProcessStatus status;
			List<SwingWorkerItem> swingWorkers  = SwingWorkerItemList.getItems();
			if (swingWorkers.size() > row) {
				status = swingWorkers.get(row).getStatus();
			} else {
				List<SwingDeleterItem> swingDeleters = SwingDeleterItemList.getItems();
				status = swingDeleters.get(row - swingWorkers.size()).getStatus();
			}

			if (column == 0 || column == 1 || column == 6 || column == 7) {
				tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else {
				tableCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
			}
			final ImageIcon ico;
			if (column == 7) {
				final URL imageIconUrl;
				if (status.isEnd()) {
					imageIconUrl = ICON_PATH_NO_ABORT;
				} else {
					imageIconUrl = ICON_PATH_ABORT;
				}
				ico = new ImageIcon(imageIconUrl);
			} else {
				ico = null;
			}
			tableCellRenderer.setIcon(ico);
		}
	}

	private static void setColors(DefaultTableCellRenderer tableCellRenderer, boolean selected, int row) {

		Color bg;
		if (!selected) {
			bg = (row % 2 == 0 ? cGlobal.alternateRowColor : cGlobal.background);
		} else {
			bg = cGlobal.selectionBackground;
		}
		tableCellRenderer.setBackground(bg);

		Color fg;
		if (selected) {
			fg = cGlobal.selectionForeground;
		} else {
			fg = cGlobal.foreground;
		}
		tableCellRenderer.setForeground(fg);
	}
}
