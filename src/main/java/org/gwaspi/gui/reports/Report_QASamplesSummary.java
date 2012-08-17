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
public class Report_QASamplesSummary extends javax.swing.JPanel {

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

	public Report_QASamplesSummary(final int _studyId, final String _qaFileName, int _opId) {

		opId = _opId;

		String reportPath = "";
		try {
			reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + _studyId + "/";
		} catch (IOException ex) {
			Logger.getLogger(Report_QASamplesSummary.class.getName()).log(Level.SEVERE, null, ex);
		}
		reportFile = new File(reportPath + _qaFileName);

		pnl_Summary = new javax.swing.JPanel();
		txt_NRows = new javax.swing.JTextField();
		txt_NRows.setInputVerifier(new org.gwaspi.gui.utils.IntegerInputVerifier());
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

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Reports.report + ": Sample Info & Missing Ratios", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_Summary.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.Reports.summary));

		txt_NRows.setText("100");
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
		lbl_suffix1.setText("Samples by most significant Missing Ratios");



		btn_Get.setText(Text.All.get);
		btn_Get.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
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
		scrl_ReportTable.setViewportView(tbl_ReportTable);


		//<editor-fold defaultstate="collapsed" desc="FOOTER">

		btn_Save.setText(org.gwaspi.global.Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int decision = org.gwaspi.gui.utils.Dialogs.showOptionDialogue(org.gwaspi.global.Text.All.save, org.gwaspi.global.Text.Reports.selectSaveMode, org.gwaspi.global.Text.Reports.currentReportView, org.gwaspi.global.Text.Reports.completeReport, org.gwaspi.global.Text.All.cancel);

				switch (decision) {
					case 0:
						actionSaveReportViewAs(_studyId, _qaFileName);
						break;
					case 1:
						actionSaveCompleteReportAs(_studyId, _qaFileName);
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
				String l;
				String header = inputBufferReader.readLine();
				int count = 0;
				while ((l = inputBufferReader.readLine()) != null && count < getRowsNb) {
					String[] cVals = l.split(org.gwaspi.constants.cImport.Separators.separators_SpaceTab_rgxp);
					Object[] row = new Object[cVals.length];

					String familyId = cVals[0];
					String sampleId = cVals[1];
					String fatherId = cVals[2];
					String motherId = cVals[3];
					String sex = cVals[4];
					String affection = cVals[5];
					String age = cVals[6];
					String category = cVals[7];
					String disease = cVals[8];
					String population = cVals[9];
					Double missRat = cVals[10] != null ? Double.parseDouble(cVals[10]) : Double.NaN;
					Double hetzyRat = Double.NaN;
					if (cVals.length > 11) {
						hetzyRat = cVals[11] != null ? Double.parseDouble(cVals[11]) : Double.NaN;
					}


					row[0] = familyId;
					row[1] = sampleId;
					row[2] = fatherId;
					row[3] = motherId;
					row[4] = sex;
					row[5] = affection;
					row[6] = age;
					row[7] = category;
					row[8] = disease;
					row[9] = population;
					row[10] = missRat;
					row[11] = hetzyRat;

					tableRowAL.add(row);
					count++;
				}

				Object[][] tableMatrix = new Object[tableRowAL.size()][12];
				for (int i = 0; i < tableRowAL.size(); i++) {
					tableMatrix[i] = (Object[]) tableRowAL.get(i);
				}

				String[] columns = new String[]{Text.Reports.familyId,
					Text.Reports.sampleId,
					Text.Reports.fatherId,
					Text.Reports.motherId,
					Text.Reports.sex,
					Text.Reports.affection,
					Text.Reports.age,
					Text.Reports.category,
					Text.Reports.disease,
					Text.Reports.population,
					Text.Reports.missRatio,
					Text.Reports.smplHetzyRat};


				TableModel model = new DefaultTableModel(tableMatrix, columns);
				tbl_ReportTable.setModel(model);

				//<editor-fold defaultstate="collapsed" desc="Linux Sorter">
//				if (!org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")) {
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
//                }
				//</editor-fold>

			}

		} catch (IOException ex) {
			Logger.getLogger(Report_QASamplesSummary.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			//Logger.getLogger(Report_QAMarkersSummary.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				inputFileReader.close();
			} catch (Exception ex) {
				//Logger.getLogger(Report_QASamplesSummary.class.getName()).log(Level.SEVERE, null, ex);
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
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.sampleQAreport);
		} catch (Exception ex) {
		}
	}
}
