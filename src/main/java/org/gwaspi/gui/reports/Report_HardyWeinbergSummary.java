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
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.IntegerInputVerifier;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Report_HardyWeinbergSummary extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_HardyWeinbergSummary.class);

	private static final String[] COLUMNS = new String[] {
			Text.Reports.markerId,
			Text.Reports.rsId,
			Text.Reports.chr,
			Text.Reports.pos,
			Text.Reports.minAallele,
			Text.Reports.majAallele,
			Text.Reports.hwPval + Text.Reports.CTRL,
			Text.Reports.hwObsHetzy + Text.Reports.CTRL,
			Text.Reports.hwExpHetzy + Text.Reports.CTRL};

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
	private JFormattedTextField txt_NRows;
	// End of variables declaration

	public Report_HardyWeinbergSummary(final int _studyId, final String _hwFileName, int _opId) {

		opId = _opId;
		String reportName = GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);

		String reportPath = "";
		try {
			reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + _studyId + "/";
		} catch (IOException ex) {
			log.error(null, ex);
		}
		reportFile = new File(reportPath + _hwFileName);

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

		final Action loadReportAction = new LoadReportAction(reportFile, tbl_ReportTable, txt_NRows);

		txt_NRows.setValue(Integer.valueOf(100));
		txt_NRows.setInputVerifier(new IntegerInputVerifier());
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
		lbl_suffix1.setText(Text.Reports.radio1Suffix_pVal);

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
//		tbl_ReportTable.addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent e) {
//				try {
//					int rowIndex = tbl_ReportTable.getSelectedRow();
//					int colIndex = tbl_ReportTable.getSelectedColumn();
//					URLInDefaultBrowser.browseGenericURL(EnsemblUrl.getHomoSapiensLink(tbl_ReportTable.getModel().getValueAt(rowIndex, 0).toString(), (Integer) tbl_ReportTable.getModel().getValueAt(rowIndex, 8)));
//				} catch (IOException ex) {
//					log.error(null, ex);
//				}
//			}
//		});
		scrl_ReportTable.setViewportView(tbl_ReportTable);

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		btn_Save.setAction(new Report_Analysis.SaveAsAction(_studyId, _hwFileName, tbl_ReportTable, txt_NRows));

		btn_Back.setAction(new Report_Analysis.BackAction(opId));

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.hwReport));

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
		private JFormattedTextField nRows;

		LoadReportAction(File reportFile, JTable reportTable, JFormattedTextField nRows) {

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
						Object[] row = new Object[COLUMNS.length];

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

//						if (!cGlobal.OSNAME.contains("Windows")) {
						Double hwPvalCtrl_f;
						Double obsHetzyCtrl_f;
						Double expHetzyCtrl_f;
						try {
							hwPvalCtrl_f = Double.parseDouble(Report_Analysis.FORMAT_SCIENTIFIC.format(hwPvalCtrl));
						} catch (NumberFormatException ex) {
							hwPvalCtrl_f = hwPvalCtrl;
							log.warn(null, ex);
						}
						try {
							obsHetzyCtrl_f = Double.parseDouble(Report_Analysis.FORMAT_ROUND.format(obsHetzyCtrl));
						} catch (NumberFormatException ex) {
							obsHetzyCtrl_f = obsHetzyCtrl;
							log.warn(null, ex);
						}
						try {
							expHetzyCtrl_f = Double.parseDouble(Report_Analysis.FORMAT_ROUND.format(expHetzyCtrl));
						} catch (NumberFormatException ex) {
							expHetzyCtrl_f = expHetzyCtrl;
							log.warn(null, ex);
						}
						row[6] = hwPvalCtrl_f;
						row[7] = obsHetzyCtrl_f;
						row[8] = expHetzyCtrl_f;
//						} else {
//							row[6] = dfRound.format(hwPvalCtrl);
//							row[7] = dfSci.format(obsHetzyCtrl);
//							row[8] = dfRound.format(expHetzyCtrl);
//						}

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
	//				}
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
}
