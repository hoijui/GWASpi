package org.gwaspi.gui.utils;

import org.gwaspi.constants.cGlobal;
import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.gwaspi.threadbox.QueueState;
import org.gwaspi.threadbox.SwingDeleterItem;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItem;
import org.gwaspi.threadbox.SwingWorkerItemList;

/**
 *
 * @author u49878
 */
public class RowRendererDefault extends DefaultTableCellRenderer {

	private static final URL ICON_PATH_ZOOM     = RowRendererDefault.class.getResource("/img/icon/zoom2_20x20.png");
	private static final URL ICON_PATH_QUERY_DB = RowRendererDefault.class.getResource("/img/icon/arrow_20x20.png");
	private static final URL ICON_PATH_ABORT    = RowRendererDefault.class.getResource("/img/icon/abort_16x16.png");
	private static final URL ICON_PATH_NO_ABORT = RowRendererDefault.class.getResource("/img/icon/abort-grey_16x16.png");
	private static List<SwingWorkerItem> swAL  = SwingWorkerItemList.getSwingWorkerItems();
	private static List<SwingDeleterItem> sdAL = SwingDeleterItemList.getSwingDeleterItems();

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
			ImageIcon ico;
			QueueState queueState;
			if (swAL.size() > row) {
				queueState = swAL.get(row).getQueueState();
			} else {
				queueState = sdAL.get(row - swAL.size()).getQueueState();
			}

			if (column == 0 || column == 1) {
				ico = null;
				tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else if (column == 6) {
				ico = null;
				tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else if (column == 7) {
				if (queueState.equals(QueueState.PROCESSING) || queueState.equals(QueueState.QUEUED)) {
					ico = new ImageIcon(ICON_PATH_ABORT);
				} else {
					ico = new ImageIcon(ICON_PATH_NO_ABORT);
				}
				tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else {
				ico = null;
				tableCellRenderer.setHorizontalAlignment(SwingConstants.LEFT);
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
