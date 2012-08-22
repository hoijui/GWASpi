package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.MatrixAnalysePanel;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.model.Operation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Report_HardyWeinbergSummary extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private File reportFile;
	private int opId;
	private javax.swing.JButton btn_Get;
	private javax.swing.JButton btn_Save;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JLabel lbl_suffix1;
	private javax.swing.JPanel pnl_Summary;
	private javax.swing.JScrollPane scrl_ReportTable;
	private javax.swing.JTable tbl_ReportTable;
	private javax.swing.JTextField txt_NRows;
	// End of variables declaration

	public Report_HardyWeinbergSummary(final int _studyId, final String _hwFileName, int _opId) {

		opId = _opId;
		String reportName = org.gwaspi.gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);

		String reportPath = "";
		try {
			reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + _studyId + "/";
		} catch (IOException ex) {
			Logger.getLogger(Report_HardyWeinbergSummary.class.getName()).log(Level.SEVERE, null, ex);
		}
		reportFile = new File(reportPath + _hwFileName);

		pnl_Summary = new javax.swing.JPanel();
		txt_NRows = new javax.swing.JTextField();
		txt_NRows.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_NRows.selectAll();
					}
				});
			}
		});
		lbl_suffix1 = new javax.swing.JLabel();
		btn_Get = new javax.swing.JButton();
		scrl_ReportTable = new javax.swing.JScrollPane();
		tbl_ReportTable = new javax.swing.JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tbl_ReportTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());

		pnl_Footer = new javax.swing.JPanel();
		btn_Save = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Reports.report + ": " + reportName, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_Summary.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.Reports.summary));

		txt_NRows.setText("100");
		txt_NRows.setInputVerifier(new org.gwaspi.gui.utils.IntegerInputVerifier());
		txt_NRows.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
		txt_NRows.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				int key = e.getKeyChar();
				if (key == 10) {
					actionLoadReport();
				}
			}
		});
		lbl_suffix1.setText(Text.Reports.radio1Suffix_pVal);

		btn_Get.setText(Text.All.get);
		btn_Get.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				//actionLoadReport(evt);
				actionLoadReport();
			}
		});

		//<editor-fold defaultstate="collapsed" desc="LAYOUT1">
		javax.swing.GroupLayout pnl_SummaryLayout = new javax.swing.GroupLayout(pnl_Summary);
		pnl_Summary.setLayout(pnl_SummaryLayout);
		pnl_SummaryLayout.setHorizontalGroup(
				pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(lbl_suffix1)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
				.addComponent(btn_Get, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_SummaryLayout.setVerticalGroup(
				pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addGroup(pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_suffix1)
				.addComponent(btn_Get))
				.addContainerGap()));
		//</editor-fold>


		tbl_ReportTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][]{
					{null, null, null, "Go!"}
				},
				new String[]{"", "", "", ""}));
//		tbl_ReportTable.addMouseListener(new MouseListener() {
//			public void mouseClicked(MouseEvent e) {
//				try {
//					int rowIndex = tbl_ReportTable.getSelectedRow();
//					int colIndex = tbl_ReportTable.getSelectedColumn();
//					org.gwaspi.gui.utils.URLInDefaultBrowser.browseGenericURL(org.gwaspi.gui.utils.EnsemblUrl.getHomoSapiensLink(tbl_ReportTable.getModel().getValueAt(rowIndex, 0).toString(), (Integer) tbl_ReportTable.getModel().getValueAt(rowIndex, 8)));
//				} catch (IOException ex) {
//					Logger.getLogger(Report_HardyWeinbergSummary.class.getName()).log(Level.SEVERE, null, ex);
//				}
//			}
//			public void mousePressed(MouseEvent e) {}
//			public void mouseReleased(MouseEvent e) {}
//			public void mouseExited(MouseEvent e) {}
//			public void mouseEntered(MouseEvent e) {}
//		});
		scrl_ReportTable.setViewportView(tbl_ReportTable);


		//<editor-fold defaultstate="collapsed" desc="FOOTER">

		btn_Save.setText(org.gwaspi.global.Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int decision = org.gwaspi.gui.utils.Dialogs.showOptionDialogue(org.gwaspi.global.Text.All.save, org.gwaspi.global.Text.Reports.selectSaveMode, org.gwaspi.global.Text.Reports.currentReportView, org.gwaspi.global.Text.Reports.completeReport, org.gwaspi.global.Text.All.cancel);

				switch (decision) {
					case 0:
						actionSaveReportViewAs(_studyId, _hwFileName);
						break;
					case 1:
						actionSaveCompleteReportAs(_studyId, _hwFileName);
						break;
					default:
						break;
				}
			}
		});

		btn_Back.setText(Text.All.Back);
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionBack();
				} catch (IOException ex) {
					Logger.getLogger(Report_QASamplesSummary.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		btn_Help.setText(Text.Help.help);
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp();
			}
		});

		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Help, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 321, Short.MAX_VALUE)
				.addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Save)
				.addComponent(btn_Back)
				.addComponent(btn_Help)));

		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrl_ReportTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
				.addComponent(pnl_Summary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Summary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_ReportTable)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>

		actionLoadReport();
	}

	//private void actionLoadReport(ActionEvent evt) {
	private void actionLoadReport() {
		FileReader inputFileReader = null;
		try {
			if (reportFile.exists() && !reportFile.isDirectory()) {
				int getRowsNb = Integer.parseInt(txt_NRows.getText());


				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
				DecimalFormat dfRound = new DecimalFormat("0.#####");
				inputFileReader = new FileReader(reportFile);
				BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

				// Getting data from file and subdividing to series all points by chromosome
				ArrayList tableRowAL = new ArrayList();
				String header = inputBufferReader.readLine();
				int count = 0;
				while (count < getRowsNb) {
					String l = inputBufferReader.readLine();
					if (l == null) {
						break;
					}
					Object[] row = new Object[9];
					String[] cVals = l.split(org.gwaspi.constants.cImport.Separators.separators_SpaceTab_rgxp);

					String markerId = cVals[0];
					String rsId = cVals[1];
					String chr = cVals[2];
					int position = Integer.parseInt(cVals[3]);
					String minAllele = cVals[4];
					String majAllele = cVals[5];
					Double hwPvalCtrl = cVals[6] != null ? Double.parseDouble(cVals[6]) : Double.NaN;
					Double obsHetzyCtrl = cVals[7] != null ? Double.parseDouble(cVals[7]) : Double.NaN;
					Double expHetzyCtrl = cVals[8] != null ? Double.parseDouble(cVals[8]) : Double.NaN;

					row[0] = markerId;
					row[1] = rsId;
					row[2] = chr;
					row[3] = position;
					row[4] = minAllele;
					row[5] = majAllele;

//                    if (!org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")){
					Double hwPvalCtrl_f;
					Double obsHetzyCtrl_f;
					Double expHetzyCtrl_f;
					try {
						hwPvalCtrl_f = Double.parseDouble(dfSci.format(hwPvalCtrl));
					} catch (NumberFormatException numberFormatException) {
						hwPvalCtrl_f = hwPvalCtrl;
					}
					try {
						obsHetzyCtrl_f = Double.parseDouble(dfRound.format(obsHetzyCtrl));
					} catch (NumberFormatException numberFormatException) {
						obsHetzyCtrl_f = obsHetzyCtrl;
					}
					try {
						expHetzyCtrl_f = Double.parseDouble(dfRound.format(expHetzyCtrl));
					} catch (NumberFormatException numberFormatException) {
						expHetzyCtrl_f = expHetzyCtrl;
					}
					row[6] = hwPvalCtrl_f;
					row[7] = obsHetzyCtrl_f;
					row[8] = expHetzyCtrl_f;
//                    } else {
//                        row[6] = dfRound.format(hwPvalCtrl);
//                        row[7] = dfSci.format(obsHetzyCtrl);
//                        row[8] = dfRound.format(expHetzyCtrl);
//                    }

					tableRowAL.add(row);
					count++;
				}

				Object[][] tableMatrix = new Object[tableRowAL.size()][9];
				for (int i = 0; i < tableRowAL.size(); i++) {
					tableMatrix[i] = (Object[]) tableRowAL.get(i);
				}

				String[] columns = new String[]{Text.Reports.markerId,
					Text.Reports.rsId,
					Text.Reports.chr,
					Text.Reports.pos,
					Text.Reports.minAallele,
					Text.Reports.majAallele,
					Text.Reports.hwPval + Text.Reports.CTRL,
					Text.Reports.hwObsHetzy + Text.Reports.CTRL,
					Text.Reports.hwExpHetzy + Text.Reports.CTRL
				};


				TableModel model = new DefaultTableModel(tableMatrix, columns);
				tbl_ReportTable.setModel(model);

				//<editor-fold defaultstate="collapsed" desc="Linux Sorter">
//				if (!org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")){
//					RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				TableRowSorter sorter = new TableRowSorter(model) {
					Comparator<Object> comparator = new Comparator<Object>() {
						public int compare(Object o1, Object o2) {
							try {
								Double d1 = Double.parseDouble(o1.toString());
								Double d2 = Double.parseDouble(o2.toString());
								return d1.compareTo(d2);
							} catch (NumberFormatException numberFormatException) {
								try {
									Integer i1 = Integer.parseInt(o1.toString());
									Integer i2 = Integer.parseInt(o2.toString());
									return i1.compareTo(i2);
								} catch (Exception e) {
									return o1.toString().compareTo(o2.toString());
								}
							}
						}
					};

					@Override
					public Comparator getComparator(int column) {
						return comparator;
					}

					@Override
					public boolean useToString(int column) {
						return false;
					}
				};

				tbl_ReportTable.setRowSorter(sorter);
//				}
				//</editor-fold>

			}

		} catch (IOException ex) {
			Logger.getLogger(Report_HardyWeinbergSummary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			//Logger.getLogger(Report_QAMarkersSummary.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				inputFileReader.close();
			} catch (Exception ex) {
				//Logger.getLogger(Report_HardyWeinbergSummary.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void actionSaveCompleteReportAs(int studyId, String chartPath) {
		try {
			String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
			File origFile = new File(reportPath + chartPath);
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}
		} catch (IOException ex) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NullPointerException ex) {
			//gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			//Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionSaveReportViewAs(int studyId, String chartPath) {
		try {
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/" + txt_NRows.getText() + "rows_" + chartPath);
			FileWriter writer = new FileWriter(newFile);

			StringBuilder tableData = new StringBuilder();
			// HEADER
			for (int k = 0; k < tbl_ReportTable.getColumnCount(); k++) {
				tableData.append(tbl_ReportTable.getColumnName(k));
				if (k != tbl_ReportTable.getColumnCount() - 1) {
					tableData.append("\t");
				}
			}
			tableData.append("\n");
			writer.write(tableData.toString());

			// TABLE CONTENT
			for (int rowNb = 0; rowNb < tbl_ReportTable.getModel().getRowCount(); rowNb++) {
				tableData = new StringBuilder();

				for (int colNb = 0; colNb < tbl_ReportTable.getModel().getColumnCount(); colNb++) {
					String curVal = tbl_ReportTable.getValueAt(rowNb, colNb).toString();

					if (curVal == null) {
						curVal = "";
					}

					tableData.append(curVal);
					if (colNb != tbl_ReportTable.getModel().getColumnCount() - 1) {
						tableData.append("\t");
					}
				}
				tableData.append("\n");
				writer.write(tableData.toString());
			}

			writer.flush();
			writer.close();

		} catch (NullPointerException ex) {
			//gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			//Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException e) {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
			Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	private void actionBack() throws IOException {
		Operation op = new Operation(opId);
		org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(op.getParentMatrixId(), opId);
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void actionHelp() {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.hwReport);
		} catch (Exception ex) {
		}
	}
}
