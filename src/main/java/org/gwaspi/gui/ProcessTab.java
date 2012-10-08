package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.LogDocument;
import org.gwaspi.gui.utils.RowRendererProcessOverviewWithAbortIcon;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.threadbox.QueueState;
import org.gwaspi.threadbox.SwingDeleterItem;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItem;
import org.gwaspi.threadbox.SwingWorkerItemList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ProcessTab extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(MatrixAnalysePanel.class);

	// Variables declaration - do not modify
	private JPanel pnl_Logo;
	private JLabel lbl_Logo;
	private JPanel pnl_Orverview;
	private JPanel pnl_ProcessLog;
	private JScrollPane scrl_Overview;
	private JScrollPane scrl_ProcessLog;
	private JTextArea txtA_ProcessLog;
	private JButton btn_Save;
	private static ProcessTab singleton = null;
	// End of variables declaration

	private ProcessTab() {
		pnl_Orverview = new JPanel();
		scrl_Overview = new JScrollPane();

		pnl_Logo = new JPanel();
		lbl_Logo = new JLabel();
		pnl_ProcessLog = new JPanel();
		scrl_ProcessLog = new JScrollPane();
		txtA_ProcessLog = new JTextArea();
		btn_Save = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Processes.processes, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_Logo.setBorder(BorderFactory.createEtchedBorder());
		pnl_Logo.setMaximumSize(new Dimension(100, 100));
		pnl_Logo.setPreferredSize(new Dimension(100, 100));

		//<editor-fold defaultstate="collapsed" desc="PROCESS OVERVIEW LAYOUT">
		GroupLayout pnl_LogoLayout = new GroupLayout(pnl_Logo);
		pnl_Logo.setLayout(pnl_LogoLayout);
		pnl_LogoLayout.setHorizontalGroup(
				pnl_LogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Logo, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE));
		pnl_LogoLayout.setVerticalGroup(
				pnl_LogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Logo, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE));


		GroupLayout pnl_OrverviewLayout = new GroupLayout(pnl_Orverview);
		pnl_Orverview.setLayout(pnl_OrverviewLayout);
		pnl_OrverviewLayout.setHorizontalGroup(
				pnl_OrverviewLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_OrverviewLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Overview, GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
				.addGap(18, 18, 18)
				.addComponent(pnl_Logo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_OrverviewLayout.setVerticalGroup(
				pnl_OrverviewLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OrverviewLayout.createSequentialGroup()
				.addGroup(pnl_OrverviewLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Overview, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
				.addComponent(pnl_Logo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pnl_OrverviewLayout.linkSize(SwingConstants.VERTICAL, new Component[]{pnl_Logo, scrl_Overview});
		//</editor-fold>

		pnl_ProcessLog.setBorder(BorderFactory.createTitledBorder(null, Text.Processes.processLog, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N

		txtA_ProcessLog.setColumns(20);
		txtA_ProcessLog.setRows(5);
		txtA_ProcessLog.setEditable(false);
		scrl_ProcessLog.setViewportView(txtA_ProcessLog);
		btn_Save.setAction(new SaveAsAction(txtA_ProcessLog));

		//<editor-fold defaultstate="collapsed" desc="PROCESS LOG LAYOUT">
		GroupLayout pnl_ProcessLogLayout = new GroupLayout(pnl_ProcessLog);
		pnl_ProcessLog.setLayout(pnl_ProcessLogLayout);
		pnl_ProcessLogLayout.setHorizontalGroup(
				pnl_ProcessLogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_ProcessLogLayout.createSequentialGroup()
				.addGroup(pnl_ProcessLogLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(pnl_ProcessLogLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrl_ProcessLog, GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE))
				.addContainerGap()));
		pnl_ProcessLogLayout.setVerticalGroup(
				pnl_ProcessLogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ProcessLogLayout.createSequentialGroup()
				.addComponent(scrl_ProcessLog, GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_Save)
				.addContainerGap()));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_ProcessLog, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Orverview, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Orverview, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_ProcessLog, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>

		if (!StartGWASpi.logOff) {
			txtA_ProcessLog.setDocument(new LogDocument());
		}
	}

	public static ProcessTab getSingleton() {

		if (singleton == null) {
			singleton = new ProcessTab();
		}

		return singleton;
	}

	public void updateProcessOverview() {
		if (StartGWASpi.guiMode) {
			final JTable tmpTable = new JTable() {
				@Override
				public boolean isCellEditable(int row, int col) {
					return false; // Renders column 0 uneditable.
				}
			};
			tmpTable.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent me) {
					//displayColumnCursor(me, tmpTable);
				}
			});
			tmpTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int rowIndex = tmpTable.getSelectedRow();
					int colIndex = tmpTable.getSelectedColumn();
					if (colIndex == 7) {    //Abort
						if (rowIndex < SwingWorkerItemList.size()) {
							SwingWorkerItemList.flagCurrentItemAborted(rowIndex);
						} else {
							SwingDeleterItemList.abortSwingWorker(rowIndex - SwingWorkerItemList.size());
						}
					}
				}
			});
			tmpTable.setDefaultRenderer(Object.class, new RowRendererProcessOverviewWithAbortIcon());
			tmpTable.setSelectionMode(0);

			tmpTable.setModel(new DefaultTableModel(
					buildProcessTableModel(),
					new String[]{
						Text.Processes.id,
						Text.Study.studyID,
						Text.Processes.processeName,
						Text.Processes.launchTime,
						Text.Processes.startTime,
						Text.Processes.endTime,
						Text.Processes.queueState,
						Text.All.abort
					}));
			tmpTable.scrollRectToVisible(tmpTable.getBounds());
			int X = scrl_Overview.getHorizontalScrollBar().getValue();
			int Y = scrl_Overview.getVerticalScrollBar().getValue();
			scrl_Overview.setViewportView(tmpTable);
			scrl_Overview.getHorizontalScrollBar().setValue(X);
			scrl_Overview.getVerticalScrollBar().setValue(Y);
		}
	}

	protected Object[][] buildProcessTableModel() {
		List<SwingWorkerItem> swingWorkerItemsAL = SwingWorkerItemList.getSwingWorkerItems();
		List<SwingDeleterItem> swingDeleterItemsAL = SwingDeleterItemList.getSwingDeleterItems();

		Object[][] spreadSheet = new Object[swingWorkerItemsAL.size() + swingDeleterItemsAL.size()][8];
		int count = 0;
		for (int i = count; i < swingWorkerItemsAL.size(); i++) {
			StringBuilder studyIdsStr = new StringBuilder();
			for (Integer studyId : swingWorkerItemsAL.get(i).getParentStudyIds()) {
				studyIdsStr.append(", ");
				studyIdsStr.append(studyId.toString());
			}
			studyIdsStr.delete(0, 2); // delete the first ", "

			spreadSheet[i][0] = i;
			spreadSheet[i][1] = studyIdsStr != null ? studyIdsStr.toString() : " - ";
			spreadSheet[i][2] = swingWorkerItemsAL.get(i).getTask().getTaskName() != null ? swingWorkerItemsAL.get(i).getTask().getTaskName() : " - ";
			spreadSheet[i][3] = swingWorkerItemsAL.get(i).getLaunchTime() != null ? swingWorkerItemsAL.get(i).getLaunchTime() : " - ";
			spreadSheet[i][4] = swingWorkerItemsAL.get(i).getStartTime() != null ? swingWorkerItemsAL.get(i).getStartTime() : " - ";
			spreadSheet[i][5] = swingWorkerItemsAL.get(i).getEndTime() != null ? swingWorkerItemsAL.get(i).getEndTime() : " - ";
			spreadSheet[i][6] = swingWorkerItemsAL.get(i).getQueueState() != null ? swingWorkerItemsAL.get(i).getQueueState() : " - ";
			spreadSheet[i][7] = " ";

			count++;
		}

		for (int i = 0; i < swingDeleterItemsAL.size(); i++) {

			spreadSheet[count][0] = "Del_" + i;
			spreadSheet[count][1] = swingDeleterItemsAL.get(i).getStudyId();
			spreadSheet[count][2] = swingDeleterItemsAL.get(i).getDescription() != null ? swingDeleterItemsAL.get(i).getDescription() : " - ";
			spreadSheet[count][3] = swingDeleterItemsAL.get(i).getLaunchTime() != null ? swingDeleterItemsAL.get(i).getLaunchTime() : " - ";
			spreadSheet[count][4] = swingDeleterItemsAL.get(i).getStartTime() != null ? swingDeleterItemsAL.get(i).getStartTime() : " - ";
			spreadSheet[count][5] = swingDeleterItemsAL.get(i).getEndTime() != null ? swingDeleterItemsAL.get(i).getEndTime() : " - ";
			spreadSheet[count][6] = swingDeleterItemsAL.get(i).getQueueState() != null ? swingDeleterItemsAL.get(i).getQueueState() : " - ";
			spreadSheet[count][7] = " ";

			count++;
		}

		return spreadSheet;
	}

	public void startBusyLogo() {
		URL logoPath = ProcessTab.class.getClass().getResource("/img/logo/logo_busy.gif");
		Icon logo = new ImageIcon(logoPath);

		lbl_Logo.setIcon(logo);
		lbl_Logo.setHorizontalAlignment(SwingConstants.CENTER);
	}

	public void toggleBusyLogo() {
		List<SwingWorkerItem> swingWorkerItemsAL = SwingWorkerItemList.getSwingWorkerItems();
		int count = 0;
		boolean idle = true;
		while (count < swingWorkerItemsAL.size()) {
			QueueState queueState = swingWorkerItemsAL.get(count).getQueueState();
			if (!queueState.equals(QueueState.DONE)
					&& !queueState.equals(QueueState.ABORT)
					&& !queueState.equals(QueueState.ERROR)) {
				idle = false;
			} else {
				idle = true;
			}
			count++;
		}

		URL logoPath = ProcessTab.class.getClass().getResource("/img/logo/logo_busy.gif");
		if (idle) {
			logoPath = ProcessTab.class.getClass().getResource("/img/logo/logo_stopped.png");
		}
		Icon logo = new ImageIcon(logoPath);

		lbl_Logo.setIcon(logo);
		lbl_Logo.setHorizontalAlignment(SwingConstants.CENTER);
	}

	public void showTab() {
		StartGWASpi.allTabs.setSelectedIndex(StartGWASpi.allTabs.getTabCount() - 1);
		startBusyLogo();
	}

	private static class SaveAsAction extends AbstractAction {

		private JTextArea processLog;

		SaveAsAction(JTextArea processLog) {

			this.processLog = processLog;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			FileWriter writer = null;
			try {
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/process.log");
				writer = new FileWriter(newFile);
				writer.write(processLog.getText());
				writer.flush();
			} catch (IOException ex) {
				log.error(null, ex);
			} catch (Exception ex) {
				log.error(null, ex);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ex) {
						log.warn(null, ex);
					}
				}
			}
		}
	}

	/**
	 * Method to change cursor based on some arbitrary rule.
	 */
	private void displayColumnCursor(MouseEvent me, JTable table) {
		Point p = me.getPoint();
		int column = table.columnAtPoint(p);
		int row = table.rowAtPoint(p);
		String columnName = table.getColumnName(column);
		if (columnName.equals(Text.Reports.zoom)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (columnName.equals(Text.Reports.ensemblLink)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (columnName.equals(Text.Reports.NCBILink)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}
}
