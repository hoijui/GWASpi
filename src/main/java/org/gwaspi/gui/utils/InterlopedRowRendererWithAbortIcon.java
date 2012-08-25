package org.gwaspi.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import org.gwaspi.threadbox.SwingDeleterItem;
import org.gwaspi.threadbox.SwingWorkerItem;

/**
 *
 * @author u49878
 */
public class InterlopedRowRendererWithAbortIcon extends InterlopedRowRenderer {

	private URL abortIconPath = getClass().getResource("/img/icon/abort_16x16.png");
	private URL noabortIconPath = getClass().getResource("/img/icon/abort-grey_16x16.png");
	private List<SwingWorkerItem> swAL = org.gwaspi.threadbox.SwingWorkerItemList.getSwingWorkerItemsAL();
	private List<SwingDeleterItem> sdAL = org.gwaspi.threadbox.SwingDeleterItemList.getSwingDeleterItemsAL();

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

		//System.out.println("Row:"+row+" - Column count:"+table.getColumnModel().getColumnCount());
		if (table.getColumnModel().getColumnCount() == 8) {
			ImageIcon ico;
			String queueState;
			if (swAL.size() > row) {
				queueState = swAL.get(row).getQueueState();
			} else {
				queueState = sdAL.get(row - swAL.size()).getQueueState();
			}

			if (column == 0 || column == 1) {
				ico = null;
				setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else if (column == 6) {
				ico = null;
				setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else if (column == 7) {
				if (queueState.equals(org.gwaspi.threadbox.QueueStates.PROCESSING) || queueState.equals(org.gwaspi.threadbox.QueueStates.QUEUED)) {
					ico = new ImageIcon(abortIconPath);
				} else {
					ico = new ImageIcon(noabortIconPath);
				}
				setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn col = table.getColumnModel().getColumn(column);
				col.setPreferredWidth(25);
			} else {
				ico = null;
				setHorizontalAlignment(SwingConstants.LEFT);
			}
			setIcon(ico);
		}

		return this;
	}
}
