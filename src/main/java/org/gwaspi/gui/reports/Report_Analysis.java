package org.gwaspi.gui.reports;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.MatrixAnalysePanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.IntegerInputVerifier;
import org.gwaspi.gui.utils.LinksExternalResouces;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.MarkerOperationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public abstract class Report_Analysis extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_Analysis.class);

	// Variables declaration - do not modify
	protected int studyId;
	protected int opId;
	protected String analysisFileName;
	protected String NRows;
	protected Map<String, Object> chrSetInfoMap = new LinkedHashMap<String, Object>();
	protected File reportFile;
	private JButton btn_Get;
	private JButton btn_Save;
	private JButton btn_Back;
	private JButton btn_Help;
	private JPanel pnl_Footer;
	private JLabel lbl_suffix1;
	private JPanel pnl_Summary;
	private JPanel pnl_SearchDB;
	protected JComboBox cmb_SearchDB;
	private JScrollPane scrl_ReportTable;
	protected final JTable tbl_ReportTable;
	protected final JTextField txt_NRows;
	private JTextField txt_PvalThreshold;
	// End of variables declaration

	protected Report_Analysis() {

		String reportName = GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);

		String reportPath = "";
		try {
			reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
		} catch (IOException ex) {
			log.error(null, ex);
		}
		reportFile = new File(reportPath + analysisFileName);

		pnl_Summary = new JPanel();
		txt_NRows = new JTextField();
		txt_NRows.setInputVerifier(new IntegerInputVerifier());
		txt_NRows.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_NRows.selectAll();
					}
				});
			}
		});
		lbl_suffix1 = new JLabel();
		txt_PvalThreshold = new JTextField();
		btn_Get = new JButton();

		pnl_SearchDB = new JPanel();
		pnl_SearchDB.setBorder(BorderFactory.createTitledBorder(Text.Reports.externalResourceDB));
		cmb_SearchDB = new JComboBox();
		cmb_SearchDB.setModel(new DefaultComboBoxModel(LinksExternalResouces.getLinkNames()));

		scrl_ReportTable = new JScrollPane();
		tbl_ReportTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tbl_ReportTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent me) {
				displayColumnCursor(me);
			}
		});

		// TO DISABLE COLUMN MOVING (DON'T WANT TO MOVE BEHIND COLUMN 9)
		tbl_ReportTable.getTableHeader().setReorderingAllowed(false);

		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Reports.report + ": " + reportName, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_Summary.setBorder(BorderFactory.createTitledBorder(Text.Reports.summary));

		try {
			Integer.parseInt(NRows);
			txt_NRows.setText(NRows);
		} catch (NumberFormatException ex) {
			log.warn(null, ex);
			txt_NRows.setText("100");
		}

		txt_NRows.setHorizontalAlignment(JTextField.TRAILING);
		txt_NRows.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				int key = e.getKeyChar();
				if (key == KeyEvent.VK_ENTER) {
					actionLoadReport();
				}
			}
		});
		lbl_suffix1.setText(Text.Reports.radio1Suffix_pVal);
		txt_PvalThreshold.setEnabled(false);

		btn_Get.setAction(new LoadReportAction());

		//<editor-fold defaultstate="expanded" desc="LAYOUT SUMMARY">
		GroupLayout pnl_SearchDBLayout = new GroupLayout(pnl_SearchDB);
		pnl_SearchDB.setLayout(pnl_SearchDBLayout);
		pnl_SearchDBLayout.setHorizontalGroup(
				pnl_SearchDBLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(cmb_SearchDB, 0, 357, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_SearchDBLayout.setVerticalGroup(
				pnl_SearchDBLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
				.addComponent(cmb_SearchDB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(14, Short.MAX_VALUE)));

		GroupLayout pnl_SummaryLayout = new GroupLayout(pnl_Summary);
		pnl_Summary.setLayout(pnl_SummaryLayout);
		pnl_SummaryLayout.setHorizontalGroup(
				pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(txt_NRows, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(lbl_suffix1)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_Get, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		pnl_SummaryLayout.setVerticalGroup(
				pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addGroup(pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(txt_NRows, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_suffix1)
				.addComponent(btn_Get))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

		tbl_ReportTable.setModel(new DefaultTableModel(
				new Object[][]{
					{null, null, null, "Go!"}
				},
				new String[]{"", "", "", ""}));

		scrl_ReportTable.setViewportView(tbl_ReportTable);

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		btn_Save.setAction(new SaveAsAction(studyId, analysisFileName, tbl_ReportTable, txt_NRows, 3));

		btn_Back.setAction(new BackAction(opId));

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.assocReport));

		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Help, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 321, Short.MAX_VALUE)
				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Back)
				.addComponent(btn_Help)));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Summary, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_SearchDB, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addComponent(scrl_ReportTable, GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				.addComponent(pnl_SearchDB, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Summary, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(20, 20, 20)
				.addComponent(scrl_ReportTable, GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
				.addContainerGap()));
		try {
			if (chrSetInfoMap == null) {
				initChrSetInfo();
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

	protected void initChrSetInfo() throws IOException {
		MarkerOperationSet opSet = new MarkerOperationSet(studyId, opId);
		chrSetInfoMap = opSet.getChrInfoSetMap(); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
	}

	protected abstract void actionLoadReport();

	private class LoadReportAction extends AbstractAction { // FIXME make static

		LoadReportAction() {

			putValue(NAME, Text.All.get);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			actionLoadReport();
		}
	}

	public static class SaveAsAction extends AbstractAction {

		private int studyId;
		private String reportFileName;
		private JTable reportTable;
		private JTextField nRows;
		private List<Integer> colIndToSave;

		public SaveAsAction(int studyId, String reportFileName, JTable reportTable, JTextField nRows, int trailingColsNotToSave) {

			this.studyId = studyId;
			this.reportFileName = reportFileName;
			this.reportTable = reportTable;
			this.nRows = nRows;
			// Don't want last trailingColsNotToSave columns
			this.colIndToSave = new ArrayList<Integer>(reportTable.getColumnCount() - trailingColsNotToSave);
			for (int ci = 0; ci < reportTable.getColumnCount() - trailingColsNotToSave; ci++) {
				colIndToSave.add(ci);
			}
			putValue(NAME, Text.All.save);
		}

		public SaveAsAction(int studyId, String reportFileName, JTable reportTable, JTextField nRows) {
			this(studyId, reportFileName, reportTable, nRows, 0);
		}

		private void actionSaveCompleteReportAs(int studyId, String chartPath) {
			try {
				String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
				File origFile = new File(reportPath + chartPath);
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
				if (origFile.exists()) {
					Utils.copyFile(origFile, newFile);
				}
			} catch (IOException ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (NullPointerException ex) {
				//Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (Exception ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			}
		}

		private void actionSaveReportViewAs(int studyId, String chartPath) {
			try {
				String newPath = Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + nRows.getText() + "rows_" + chartPath;
				File newFile = new File(newPath);
				FileWriter writer = new FileWriter(newFile);

				StringBuilder tableData = new StringBuilder();
				// HEADER
				for (int ri : colIndToSave) {
					tableData.append(reportTable.getColumnName(ri));
					tableData.append("\t");
				}
				// delete the last "\t"
				tableData.deleteCharAt(tableData.length() - 1);
				tableData.append("\n");
				writer.write(tableData.toString());

				// TABLE CONTENT
				for (int rowNb = 0; rowNb < reportTable.getModel().getRowCount(); rowNb++) {
					tableData = new StringBuilder();

					for (int colNb : colIndToSave) {
						String curVal = reportTable.getValueAt(rowNb, colNb).toString();

						if (curVal == null) {
							curVal = "";
						}

						tableData.append(curVal);
						tableData.append("\t");
					}
					// delete the last "\t"
					tableData.deleteCharAt(tableData.length() - 1);
					tableData.append("\n");
					writer.write(tableData.toString());
				}

				writer.flush();
				writer.close();
			} catch (NullPointerException ex) {
				//Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (IOException ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int decision = Dialogs.showOptionDialogue(Text.All.save, Text.Reports.selectSaveMode, Text.Reports.currentReportView, Text.Reports.completeReport, Text.All.cancel);

			switch (decision) {
				case JOptionPane.YES_OPTION:
					actionSaveReportViewAs(studyId, reportFileName);
					break;
				case JOptionPane.NO_OPTION:
					actionSaveCompleteReportAs(studyId, reportFileName);
					break;
				default: // JOptionPane.CANCEL_OPTION
					break;
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private int opId;

		BackAction(int opId) {

			this.opId = opId;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Operation op = OperationsList.getById(opId);
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(op.getParentMatrixId(), opId));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	/**
	 * Method to change cursor based on some arbitrary rule.
	 */
	protected void displayColumnCursor(MouseEvent me) {
		Point p = me.getPoint();
		int column = tbl_ReportTable.columnAtPoint(p);
		int row = tbl_ReportTable.rowAtPoint(p);
		String columnName = tbl_ReportTable.getColumnName(column);
		if (!getCursor().equals(CursorUtils.WAIT_CURSOR)) {
			if (columnName.equals(Text.Reports.zoom)) {
				setCursor(CursorUtils.HAND_CURSOR);
			} else if (columnName.equals(Text.Reports.externalResource)) {
				setCursor(CursorUtils.HAND_CURSOR);
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}
}
