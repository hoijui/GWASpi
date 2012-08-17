package org.gwaspi.gui;

import org.gwaspi.global.Text;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
public class ProcessTab extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private static javax.swing.JPanel pnl_Logo;
	private static javax.swing.JLabel lbl_Logo;
	private javax.swing.JPanel pnl_Orverview;
	private javax.swing.JPanel pnl_ProcessLog;
	private static javax.swing.JScrollPane scrl_Overview;
	private javax.swing.JScrollPane scrl_ProcessLog;
	private static javax.swing.JTextArea txtA_ProcessLog;
	private javax.swing.JButton btn_Save;
	private static OutputStream sysOutPS = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			txtA_ProcessLog.append(String.valueOf((char) b));
			txtA_ProcessLog.setCaretPosition(txtA_ProcessLog.getDocument().getLength());
		}

		@Override
		public void write(byte[] b, int off, int len) {
			txtA_ProcessLog.append(new String(b, off, len));
			txtA_ProcessLog.setCaretPosition(txtA_ProcessLog.getDocument().getLength());
		}
	};
	// End of variables declaration

	public ProcessTab() throws IOException {
		pnl_Orverview = new javax.swing.JPanel();
		scrl_Overview = new javax.swing.JScrollPane();

		pnl_Logo = new javax.swing.JPanel();
		lbl_Logo = new javax.swing.JLabel();
		pnl_ProcessLog = new javax.swing.JPanel();
		scrl_ProcessLog = new javax.swing.JScrollPane();
		txtA_ProcessLog = new javax.swing.JTextArea();
		btn_Save = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Processes.processes, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_Logo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		pnl_Logo.setMaximumSize(new java.awt.Dimension(100, 100));
		pnl_Logo.setPreferredSize(new java.awt.Dimension(100, 100));


		//<editor-fold defaultstate="collapsed" desc="PROCESS OVERVIEW LAYOUT">
		javax.swing.GroupLayout pnl_LogoLayout = new javax.swing.GroupLayout(pnl_Logo);
		pnl_Logo.setLayout(pnl_LogoLayout);
		pnl_LogoLayout.setHorizontalGroup(
				pnl_LogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Logo, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE));
		pnl_LogoLayout.setVerticalGroup(
				pnl_LogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Logo, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE));


		javax.swing.GroupLayout pnl_OrverviewLayout = new javax.swing.GroupLayout(pnl_Orverview);
		pnl_Orverview.setLayout(pnl_OrverviewLayout);
		pnl_OrverviewLayout.setHorizontalGroup(
				pnl_OrverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_OrverviewLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_Overview, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
				.addGap(18, 18, 18)
				.addComponent(pnl_Logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_OrverviewLayout.setVerticalGroup(
				pnl_OrverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OrverviewLayout.createSequentialGroup()
				.addGroup(pnl_OrverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Overview, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(pnl_Logo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pnl_OrverviewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{pnl_Logo, scrl_Overview});
		//</editor-fold>


		pnl_ProcessLog.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Processes.processLog, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

		txtA_ProcessLog.setColumns(20);
		txtA_ProcessLog.setRows(5);
		txtA_ProcessLog.setEditable(false);
		scrl_ProcessLog.setViewportView(txtA_ProcessLog);
		btn_Save.setText(Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSaveAs();
			}
		});


		//<editor-fold defaultstate="collapsed" desc="PROCESS LOG LAYOUT">

		javax.swing.GroupLayout pnl_ProcessLogLayout = new javax.swing.GroupLayout(pnl_ProcessLog);
		pnl_ProcessLog.setLayout(pnl_ProcessLogLayout);
		pnl_ProcessLogLayout.setHorizontalGroup(
				pnl_ProcessLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_ProcessLogLayout.createSequentialGroup()
				.addGroup(pnl_ProcessLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addGroup(pnl_ProcessLogLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addComponent(scrl_ProcessLog, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE))
				.addContainerGap()));
		pnl_ProcessLogLayout.setVerticalGroup(
				pnl_ProcessLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ProcessLogLayout.createSequentialGroup()
				.addComponent(scrl_ProcessLog, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_Save)
				.addContainerGap()));

		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT">

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_ProcessLog, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Orverview, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Orverview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_ProcessLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));

		//</editor-fold>



		if (!org.gwaspi.gui.StartGWASpi.logOff) {
			PrintStream printStream = new PrintStream(System.out) {
				@Override
				public void println(String x) {
					txtA_ProcessLog.append(x + "\n");
				}
			};
			System.setOut(new PrintStream(sysOutPS, true));
		} else {
		}

	}

	public static void updateProcessOverview() {
		if (org.gwaspi.gui.StartGWASpi.guiMode) {
			final JTable tmpTable = new javax.swing.JTable() {
				@Override
				public boolean isCellEditable(int row, int col) {
					return false; //Renders column 0 uneditable.
				}
			};
			tmpTable.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent me) {
					//displayColumnCursor(me, tmpTable);
				}
			});
			tmpTable.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					int rowIndex = tmpTable.getSelectedRow();
					int colIndex = tmpTable.getSelectedColumn();
					if (colIndex == 7) {    //Abort
						if (rowIndex < org.gwaspi.threadbox.SwingWorkerItemList.getSwingWorkerItemsALsize()) {
							org.gwaspi.threadbox.SwingWorkerItemList.flagCurrentItemAborted(rowIndex);
						} else {
							org.gwaspi.threadbox.SwingDeleterItemList.abortSwingWorker(rowIndex - org.gwaspi.threadbox.SwingWorkerItemList.getSwingWorkerItemsALsize());
						}

					}
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseReleased(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}
			});
			tmpTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererProcessOverviewWithAbortIcon());
			tmpTable.setSelectionMode(0);

			tmpTable.setModel(new javax.swing.table.DefaultTableModel(
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

	protected static Object[][] buildProcessTableModel() {
		List<SwingWorkerItem> swingWorkerItemsAL = SwingWorkerItemList.getSwingWorkerItemsAL();
		List<SwingDeleterItem> swingDeleterItemsAL = SwingDeleterItemList.getSwingDeleterItemsAL();

		Object[][] spreadSheet = new Object[swingWorkerItemsAL.size() + swingDeleterItemsAL.size()][8];
		int count = 0;
		for (int i = count; i < swingWorkerItemsAL.size(); i++) {
			Integer[] studyIds = swingWorkerItemsAL.get(i).getParentStudyIds();
			StringBuilder sb = new StringBuilder(studyIds[0].toString());
			for (int j = 1; j < studyIds.length; j++) {
				sb.append(", ");
				sb.append(studyIds[j].toString());
			}

			spreadSheet[i][0] = i;
			spreadSheet[i][1] = sb != null ? sb.toString() : " - ";
			spreadSheet[i][2] = swingWorkerItemsAL.get(i).getSwingWorkerName() != null ? swingWorkerItemsAL.get(i).getSwingWorkerName() : " - ";
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

	public static void startBusyLogo() {
		URL logoPath = ProcessTab.class.getClass().getResource("/resources/logo_busy.gif");
		Icon logo = new ImageIcon(logoPath);

		lbl_Logo.setIcon(logo);
		lbl_Logo.setHorizontalAlignment(SwingConstants.CENTER);
	}

	public static void toggleBusyLogo() {
		List<SwingWorkerItem> swingWorkerItemsAL = SwingWorkerItemList.getSwingWorkerItemsAL();
		int count = 0;
		boolean idle = true;
		while (count < swingWorkerItemsAL.size()) {
			String queueState = swingWorkerItemsAL.get(count).getQueueState();
			if (!queueState.equals(org.gwaspi.threadbox.QueueStates.DONE)
					&& !queueState.equals(org.gwaspi.threadbox.QueueStates.ABORT)
					&& !queueState.equals(org.gwaspi.threadbox.QueueStates.ERROR)) {
				idle = false;
			} else {
				idle = true;
			}
			count++;
		}

		URL logoPath = ProcessTab.class.getClass().getResource("/resources/logo_busy.gif");
		if (idle) {
			logoPath = ProcessTab.class.getClass().getResource("/resources/logo_stopped.png");
		}
		Icon logo = new ImageIcon(logoPath);

		lbl_Logo.setIcon(logo);
		lbl_Logo.setHorizontalAlignment(SwingConstants.CENTER);
	}

	public static void showTab() {
		org.gwaspi.gui.StartGWASpi.allTabs.setSelectedIndex(org.gwaspi.gui.StartGWASpi.allTabs.getTabCount() - 1);
		startBusyLogo();
	}

	private void actionSaveAs() {
		try {
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/process.log");
			FileWriter writer = new FileWriter(newFile);
			writer.write(txtA_ProcessLog.getText());
			writer.flush();
			writer.close();

		} catch (IOException ex) {
			Logger.getLogger(ProcessTab.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			//Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Method to change cursor based on some arbitrary rule.
	 */
	protected void displayColumnCursor(MouseEvent me, JTable table) {
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
