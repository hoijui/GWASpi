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

package org.gwaspi.gui.reports;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.constants.cImport;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.IntegerInputVerifier;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Report_QASamplesSummary extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_QASamplesSummary.class);

	private static final String[] COLUMNS = new String[] {
			Text.Reports.familyId,
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

	// Variables declaration - do not modify
	private File reportFile;
	private int opId;
	private JButton btn_Get;
	private JButton btn_Save;
	private JButton btn_Back;
	private JButton btn_Help;
	private JPanel pnl_Footer;
	private JLabel lbl_suffix1;
	private JPanel pnl_Summary;
	private JScrollPane scrl_ReportTable;
	private JTable tbl_ReportTable;
	private JTextField txt_NRows;
	// End of variables declaration

	public Report_QASamplesSummary(final int studyId, final String qaFileName, final int opId) {

		this.opId = opId;

		String reportPath = "";
		try {
			reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
		} catch (IOException ex) {
			log.error(null, ex);
		}
		reportFile = new File(reportPath + qaFileName);

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
		btn_Get = new JButton();
		scrl_ReportTable = new JScrollPane();
		tbl_ReportTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tbl_ReportTable.setDefaultRenderer(Object.class, new RowRendererDefault());

		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Reports.report + ": Sample Info & Missing Ratios", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_Summary.setBorder(BorderFactory.createTitledBorder(Text.Reports.summary));

		final Action loadReportAction = new LoadReportAction(reportFile, tbl_ReportTable, txt_NRows);

		txt_NRows.setText("100");
		txt_NRows.setHorizontalAlignment(JTextField.TRAILING);
		txt_NRows.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyChar();
				if (key == KeyEvent.VK_ENTER) {
					loadReportAction.actionPerformed(null);
				}
			}
		});
		lbl_suffix1.setText("Samples by most significant Missing Ratios");

		btn_Get.setAction(loadReportAction);

		//<editor-fold defaultstate="expanded" desc="LAYOUT1">
		GroupLayout pnl_SummaryLayout = new GroupLayout(pnl_Summary);
		pnl_Summary.setLayout(pnl_SummaryLayout);
		pnl_SummaryLayout.setHorizontalGroup(
				pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(txt_NRows, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(lbl_suffix1)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
				.addComponent(btn_Get, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_SummaryLayout.setVerticalGroup(
				pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SummaryLayout.createSequentialGroup()
				.addGroup(pnl_SummaryLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(txt_NRows, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_suffix1)
				.addComponent(btn_Get))
				.addContainerGap()));
		//</editor-fold>

		tbl_ReportTable.setModel(new DefaultTableModel(
				new Object[][]{
					{null, null, null, "Go!"}
				},
				new String[]{"", "", "", ""}));
		scrl_ReportTable.setViewportView(tbl_ReportTable);

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		btn_Save.setAction(new SaveAsAction(studyId, qaFileName, tbl_ReportTable, txt_NRows));

		btn_Back.setAction(new Report_Analysis.BackAction(this.opId));

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.sampleQAreport));

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
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrl_ReportTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
				.addComponent(pnl_Summary, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Summary, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_ReportTable)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>

		loadReportAction.actionPerformed(null);
	}

	private static class LoadReportAction extends AbstractAction {

		private File reportFile;
		private JTable reportTable;
		private JTextField nRows;

		LoadReportAction(File reportFile, JTable reportTable, JTextField nRows) {

			this.reportFile = reportFile;
			this.reportTable = reportTable;
			this.nRows = nRows;
			putValue(NAME, Text.All.get);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			FileReader inputFileReader = null;
			BufferedReader inputBufferReader = null;
			try {
				if (reportFile.exists() && !reportFile.isDirectory()) {
					int getRowsNb = Integer.parseInt(nRows.getText());

					inputFileReader = new FileReader(reportFile);
					inputBufferReader = new BufferedReader(inputFileReader);

					// Getting data from file and subdividing to series all points by chromosome
					List<Object[]> tableRows = new ArrayList<Object[]>();
					// read but ignore the header
					/*String header = */inputBufferReader.readLine();
					int count = 0;
					while (count < getRowsNb) {
						String l = inputBufferReader.readLine();
						if (l == null) {
							break;
						}
						String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);
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

						tableRows.add(row);
						count++;
					}

					Object[][] tableMatrix = new Object[tableRows.size()][COLUMNS.length];
					for (int i = 0; i < tableRows.size(); i++) {
						tableMatrix[i] = tableRows.get(i);
					}

					TableModel model = new DefaultTableModel(tableMatrix, COLUMNS);
					reportTable.setModel(model);

					//<editor-fold defaultstate="expanded" desc="Linux Sorter">
//					if (!cGlobal.OSNAME.contains("Windows")) {
//						RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					TableRowSorter sorter = new TableRowSorter(model) {
						Comparator<Object> comparator = new Comparator<Object>() {
							public int compare(Object o1, Object o2) {
								try {
									Double d1 = Double.parseDouble(o1.toString());
									Double d2 = Double.parseDouble(o2.toString());
									return d1.compareTo(d2);
								} catch (NumberFormatException ex) {
									log.warn(null, ex);
									try {
										Integer i1 = Integer.parseInt(o1.toString());
										Integer i2 = Integer.parseInt(o2.toString());
										return i1.compareTo(i2);
									} catch (Exception ex1) {
										log.warn(null, ex1);
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

					reportTable.setRowSorter(sorter);
//					}
					//</editor-fold>
				}
			} catch (IOException ex) {
				log.error(null, ex);
			} catch (Exception ex) {
				log.error(null, ex);
			} finally {
				try {
					if (inputBufferReader != null) {
						inputBufferReader.close();
					} else if (inputFileReader != null) {
						inputFileReader.close();
					}
				} catch (Exception ex) {
					log.warn(null, ex);
				}
			}
		}
	}

	private static class SaveAsAction extends AbstractAction {

		private int studyId;
		private String chartPath;
		private JTable reportTable;
		private JTextField nRows;

		SaveAsAction(int studyId, String chartPath, JTable reportTable, JTextField nRows) {

			this.studyId = studyId;
			this.chartPath = chartPath;
			this.reportTable = reportTable;
			this.nRows = nRows;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int decision = Dialogs.showOptionDialogue(Text.All.save, Text.Reports.selectSaveMode, Text.Reports.currentReportView, Text.Reports.completeReport, Text.All.cancel);

			switch (decision) {
				case JOptionPane.YES_OPTION:
					actionSaveReportViewAs();
					break;
				case JOptionPane.NO_OPTION:
					actionSaveCompleteReportAs();
					break;
				default: // JOptionPane.CANCEL_OPTION
					break;
			}
		}

		private void actionSaveReportViewAs() {
			FileWriter writer = null;
			try {
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + nRows.getText() + "rows_" + chartPath);
				writer = new FileWriter(newFile);

				StringBuilder tableData = new StringBuilder();
				// HEADER
				for (int k = 0; k < reportTable.getColumnCount(); k++) {
					tableData.append(reportTable.getColumnName(k));
					if (k != reportTable.getColumnCount() - 1) {
						tableData.append("\t");
					}
				}
				tableData.append("\n");
				writer.write(tableData.toString());

				// TABLE CONTENT
				for (int rowNb = 0; rowNb < reportTable.getModel().getRowCount(); rowNb++) {
					tableData = new StringBuilder();

					for (int colNb = 0; colNb < reportTable.getModel().getColumnCount(); colNb++) {
						String curVal = (String) reportTable.getValueAt(rowNb, colNb);

						if (curVal == null) {
							curVal = "";
						}

						tableData.append(curVal);
						if (colNb != reportTable.getModel().getColumnCount() - 1) {
							tableData.append("\t");
						}
					}
					tableData.append("\n");
					writer.write(tableData.toString());
				}

				writer.flush();
			} catch (NullPointerException ex) {
				//Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (IOException ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
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

		private void actionSaveCompleteReportAs() {
			try {
				String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
				File origFile = new File(reportPath + chartPath);
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
				if (origFile.exists()) {
					org.gwaspi.global.Utils.copyFile(origFile, newFile);
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
	}
}
