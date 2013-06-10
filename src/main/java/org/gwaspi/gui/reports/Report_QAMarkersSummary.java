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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.constants.cImport;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.MatrixMarkerQAPanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.IntegerInputVerifier;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Report_QAMarkersSummary extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_QAMarkersSummary.class);

	// Variables declaration - do not modify
	private final File reportFile;
	private final int opId;
	private final String qaValue;
	private final JButton btn_Get;
	private final JButton btn_Save;
	private final JButton btn_Back;
	private final JButton btn_Help;
	private final JPanel pnl_Footer;
	private final JPanel pnl_Summary;
	private final JLabel lbl_suffix1;
	private final JScrollPane scrl_ReportTable;
	private final JTable tbl_ReportTable;
	private final JFormattedTextField txt_NRows;
	// End of variables declaration

	public Report_QAMarkersSummary(final StudyKey studyKey, final String _qaFileName, int _opId) throws IOException {

		opId = _opId;
		String reportName = GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);

		String tmpQaValue = "Mismatching";
		String nRowsSuffix = "Markers";
		if (reportName.contains("Missingness")) {
			tmpQaValue = "Missing Ratio";
			nRowsSuffix = "Markers by most significant Missing Ratios";
		}
		this.qaValue = tmpQaValue;

		String reportPath = "";
		try {
			reportPath = Study.constructReportsPath(studyKey);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		reportFile = new File(reportPath + _qaFileName);

		pnl_Summary = new JPanel();
		txt_NRows = new JFormattedTextField();
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

		setBorder(BorderFactory.createTitledBorder(null, Text.Reports.report + ": " + reportName, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_Summary.setBorder(BorderFactory.createTitledBorder(Text.Reports.summary));

		final Action loadReportAction = new LoadReportAction(reportFile, tbl_ReportTable, txt_NRows, qaValue);

		txt_NRows.setValue(Integer.valueOf(100));
		txt_NRows.setHorizontalAlignment(JFormattedTextField.TRAILING);
		txt_NRows.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyChar();
				if (key == KeyEvent.VK_ENTER) {
					loadReportAction.actionPerformed(null);
				}
			}
		});
		lbl_suffix1.setText(nRowsSuffix);

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
		btn_Save.setAction(new Report_Analysis.SaveAsAction(studyKey, _qaFileName, tbl_ReportTable, txt_NRows));

		btn_Back.setAction(new BackAction(opId));

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.markerQAreport));

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

		private final File reportFile;
		private final JTable reportTable;
		private final JFormattedTextField nRows;
		private final String[] columns;

		LoadReportAction(File reportFile, JTable reportTable, JFormattedTextField nRows, String qaValue) {

			this.reportFile = reportFile;
			this.reportTable = reportTable;
			this.nRows = nRows;
			this.columns = new String[] {
					Text.Reports.markerId,
					Text.Reports.rsId,
					Text.Reports.chr,
					Text.Reports.pos,
					Text.Reports.minAallele,
					Text.Reports.majAallele,
					qaValue};
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
						Object[] row = new Object[columns.length];

						String markerId = cVals[0];
						String rsId = cVals[1];
						String chr = cVals[2];
						int position = Integer.parseInt(cVals[3]);
						String minAllele = cVals[4];
						String majAllele = cVals[5];
						Double missRat = cVals[6] != null ? Double.parseDouble(cVals[6]) : Double.NaN;

						row[0] = markerId;
						row[1] = rsId;
						row[2] = chr;
						row[3] = position;
						row[4] = minAllele;
						row[5] = majAllele;

//						if (!cGlobal.OSNAME.contains("Windows")) {
						Double missRat_f;
						try {
							missRat_f = Double.parseDouble(Report_Analysis.FORMAT_ROUND.format(missRat));
						} catch (NumberFormatException ex) {
							missRat_f = missRat;
							log.warn(null, ex);
						}
						row[6] = missRat_f;
//						} else {
//							row[6] = dfRound.format(missRat);
//						}

						tableRows.add(row);
						count++;
					}

					Object[][] tableMatrix = new Object[tableRows.size()][columns.length];
					for (int i = 0; i < tableRows.size(); i++) {
						tableMatrix[i] = tableRows.get(i);
					}

					TableModel model = new DefaultTableModel(tableMatrix, columns);
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
									} catch (NumberFormatException ex1) {
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
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private final int opId;
		private final OperationMetadata op;

		BackAction(int opId) throws IOException {

			this.opId = opId;
			this.op = OperationsList.getById(opId);
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixMarkerQAPanel(new MatrixKey(op.getStudyKey(), op.getParentMatrixId()), opId));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
