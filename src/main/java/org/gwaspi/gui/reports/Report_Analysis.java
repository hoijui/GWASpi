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
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JFormattedTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.BackAction;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.IntegerInputVerifier;
import org.gwaspi.gui.utils.LinksExternalResouces;
import org.gwaspi.gui.utils.RowRendererAssociationTestWithZoomAndQueryDB;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.reports.ReportParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Report_Analysis extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_Analysis.class);

	public static final DecimalFormat FORMAT_SCIENTIFIC = new DecimalFormat("0.##E0#");
	public static final DecimalFormat FORMAT_ROUND = new DecimalFormat("0.#####");
	public static final DecimalFormat FORMAT_INTEGER = new DecimalFormat("#");

	/** @deprecated currently not added -> not visible */
	private JFormattedTextField txt_PvalThreshold;

	protected Report_Analysis(final OperationKey operationKey, final String reportFileName, final Integer nRows) {

		String reportName = GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);
		final String nRowsSuffix = Text.Reports.radio1Suffix_pVal;
		final String helpUrlSuffix = HelpURLs.QryURL.assocReport;
		final ReportParser reportParser
				= new OutputTest.AssociationTestReportParser(getAssociationTestType());

		final Map<ChromosomeKey, ChromosomeInfo> chrSetInfoMap
				= new LinkedHashMap<ChromosomeKey, ChromosomeInfo>();

		String reportPath = "";
		try {
			reportPath = Study.constructReportsPath(operationKey.getParentMatrixKey().getStudyKey());
		} catch (IOException ex) {
			log.error(null, ex);
		}
		final File reportFile = new File(reportPath + reportFileName);

		final JButton btn_Get;
		final JButton btn_Save;
		final JButton btn_Back;
		final JButton btn_Help;
		final JPanel pnl_Footer;
		final JPanel pnl_Summary;
		final JLabel lbl_suffix1;
		final JScrollPane scrl_ReportTable;
		final JTable tbl_ReportTable;
		final JFormattedTextField txt_NRows;

		pnl_Summary = new JPanel();
		txt_NRows = new JFormattedTextField();
		txt_NRows.setInputVerifier(new IntegerInputVerifier());
		final Integer actualNRows = (nRows == null) ? 100 : nRows;
		txt_NRows.setValue(actualNRows);
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
		txt_PvalThreshold = new JFormattedTextField();
		btn_Get = new JButton();

		final JPanel pnl_SearchDB = new JPanel();
		pnl_SearchDB.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(
				Text.Reports.externalResourceDB));
		final JComboBox cmb_SearchDB = new JComboBox();
		cmb_SearchDB.setModel(new DefaultComboBoxModel(LinksExternalResouces.getLinkNames()));

		scrl_ReportTable = new JScrollPane();
		tbl_ReportTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		// TO DISABLE COLUMN MOVING (DON'T WANT TO MOVE BEHIND COLUMN 9)
		tbl_ReportTable.getTableHeader().setReorderingAllowed(false);

		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(
				Text.Reports.report + ": " + reportName)); // NOI18N

		pnl_Summary.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Reports.summary));

		final Action loadReportAction = new LoadReportAction(
				reportFile, tbl_ReportTable, txt_NRows, reportParser);

		txt_NRows.setHorizontalAlignment(JFormattedTextField.TRAILING);
		lbl_suffix1.setText(nRowsSuffix);
		txt_PvalThreshold.setEnabled(false);

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

		scrl_ReportTable.setViewportView(tbl_ReportTable);

		//<editor-fold defaultstate="expanded" desc="FOOTER">
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
			if (chrSetInfoMap.isEmpty()) {
				chrSetInfoMap.putAll(loadChrSetInfo(operationKey));
			}
		} catch (IOException ex) {
			log.error(null, ex);
		}

		tbl_ReportTable.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null, null, "Go!"}
				},
				new String[] {"", "", "", ""}));

		txt_NRows.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				final int key = e.getKeyChar();
				if (key == KeyEvent.VK_ENTER) {
					loadReportAction.actionPerformed(null);
				}
			}
		});
		btn_Get.setAction(loadReportAction);
		btn_Save.setAction(new SaveAsAction(
				operationKey.getParentMatrixKey().getStudyKey(),
				reportFileName,
				tbl_ReportTable,
				txt_NRows,
				3));
		btn_Back.setAction(new BackAction(new DataSetKey(operationKey)));
		btn_Help.setAction(new BrowserHelpUrlAction(helpUrlSuffix));

		tbl_ReportTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent me) {
				displayColumnCursor(tbl_ReportTable, me);
			}
		});

		tbl_ReportTable.setDefaultRenderer(Object.class, createRowRenderer());
		tbl_ReportTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					int rowIndex = tbl_ReportTable.getSelectedRow();
					int colIndex = tbl_ReportTable.getSelectedColumn();
					if (chrSetInfoMap.isEmpty()) {
						chrSetInfoMap.putAll(loadChrSetInfo(operationKey));
					}

					if (colIndex == getZoomColumnIndex()) { // Zoom
						setCursor(CursorUtils.WAIT_CURSOR);
						long markerPhysPos = (Long) tbl_ReportTable.getValueAt(rowIndex, 3); // marker physical position in chromosome
						MarkerKey markerKey = (MarkerKey) tbl_ReportTable.getValueAt(rowIndex, 0);
						ChromosomeKey chr = (ChromosomeKey) tbl_ReportTable.getValueAt(rowIndex, 2);

						ChromosomeInfo chrInfo = chrSetInfoMap.get(chr);
						int nbMarkers = chrInfo.getMarkerCount();
						int startPhysPos = chrInfo.getFirstPos();
						int maxPhysPos = chrInfo.getPos();
						double avgMarkersPerPhysPos = (double) nbMarkers / (maxPhysPos - startPhysPos);
						int requestedWindowSize = Math.abs((int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPhysPos));

						GWASpiExplorerPanel.getSingleton().setPnl_Content(new ManhattanPlotZoom(
								operationKey,
								chr,
								markerKey,
								markerPhysPos,
								requestedWindowSize, // requested window size in phys positions
								((Number) txt_NRows.getValue()).intValue()));
						GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
					}
					if (colIndex == getExternalResourceColumnIndex()) { // Show selected resource database
						URLInDefaultBrowser.browseGenericURL(LinksExternalResouces.getResourceLink(
								cmb_SearchDB.getSelectedIndex(),
								(ChromosomeKey) tbl_ReportTable.getValueAt(rowIndex, 2), // chr
								tbl_ReportTable.getValueAt(rowIndex, 1).toString(), // rsId
								(Long) tbl_ReportTable.getValueAt(rowIndex, 3)) // pos
								);
					}
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		loadReportAction.actionPerformed(null);
	}

	protected abstract OPType getAssociationTestType();

	private String[] getColumns() {
		return OutputTest.createColumnHeaders(getAssociationTestType());
	}

	private int getZoomColumnIndex() {
		return getColumns().length - 2;
	}

	protected int getExternalResourceColumnIndex() {
		return getZoomColumnIndex() + 1;
	}

	private RowRendererDefault createRowRenderer() {

		return new RowRendererAssociationTestWithZoomAndQueryDB(
				getZoomColumnIndex(),
				getExternalResourceColumnIndex());
	}

	private Map<ChromosomeKey, ChromosomeInfo> loadChrSetInfo(final OperationKey operationKey) throws IOException {
		return OperationManager.extractChromosomeKeysAndInfos(operationKey);
	}

	private static class LoadReportAction extends AbstractAction {

		private final File reportFile;
		private final JTable reportTable;
		private final JFormattedTextField nRows;
		private final ReportParser reportParser;

		LoadReportAction(File reportFile, JTable reportTable, JFormattedTextField nRows, final ReportParser reportParser) {

			this.reportFile = reportFile;
			this.reportTable = reportTable;
			this.nRows = nRows;
			this.reportParser = reportParser;
			putValue(NAME, Text.All.get);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			if (reportFile.exists() && !reportFile.isDirectory()) {
				final int numRowsToFetch = Integer.parseInt(nRows.getText());

				final List<Object[]> tableRows;
				try {
					tableRows = reportParser.parseReport(reportFile, numRowsToFetch, false);
				} catch (final IOException ex) {
					log.error(null, ex);
					// TODO maybe inform the user through a dialog?
					return;
				}

				final Object[][] tableMatrix = tableRows.toArray(new Object[0][0]);

				TableModel model = new DefaultTableModel(tableMatrix, reportParser.getColumnHeaders());
				reportTable.setModel(model);

				TableRowSorter sorter = new TableRowSorter(model) {
					Comparator<Object> comparator = new Comparator<Object>() {
						@Override
						public int compare(Object o1, Object o2) {
							try {
								Double d1 = Double.parseDouble(o1.toString());
								Double d2 = Double.parseDouble(o2.toString());
								return d1.compareTo(d2);
							} catch (final NumberFormatException exDouble) {
								try {
									Integer i1 = Integer.parseInt(o1.toString());
									Integer i2 = Integer.parseInt(o2.toString());
									return i1.compareTo(i2);
								} catch (final NumberFormatException exInteger) {
									log.warn("To compare objects are neither both Double, nor both Integer: {} {}", o1, o2);
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
			}
		}
	}

	public static class SaveAsAction extends AbstractAction {

		private final StudyKey studyKey;
		private final String reportFileName;
		private final JTable reportTable;
		private final JFormattedTextField nRows;
		private final List<Integer> colIndToSave;

		public SaveAsAction(
				final StudyKey studyKey,
				final String reportFileName,
				final JTable reportTable,
				final JFormattedTextField nRows,
				final int trailingColsNotToSave)
		{
			this.studyKey = studyKey;
			this.reportFileName = reportFileName;
			this.reportTable = reportTable;
			this.nRows = nRows;
			// we do not want the last trailingColsNotToSave number of columns
			this.colIndToSave = new ArrayList<Integer>(reportTable.getColumnCount() - trailingColsNotToSave);
			for (int columnI = 0; columnI < reportTable.getColumnCount() - trailingColsNotToSave; columnI++) {
				colIndToSave.add(columnI);
			}
			putValue(NAME, Text.All.save);
		}

		public SaveAsAction(StudyKey studyKey, String reportFileName, JTable reportTable, JFormattedTextField nRows) {
			this(studyKey, reportFileName, reportTable, nRows, 0);
		}

		private void actionSaveCompleteReportAs(final Component dialogParent) {
			try {
				final String reportPath = Study.constructReportsPath(studyKey);
				final File origFile = new File(reportPath, reportFileName);
				final File newDir = Dialogs.selectDirectoryDialog(
						Config.PROPERTY_EXPORT_DIR,
						"Choose the new directory for " + reportFileName,
						dialogParent);
				if (newDir == null) {
					// the user has not choosen a directory to save to
					return;
				}
				final File newFile = new File(newDir, reportFileName);
				if (!origFile.exists()) {
					throw new FileNotFoundException("Could not read from original report file: "
							+ origFile.getAbsolutePath());
				}
				Utils.copyFile(origFile, newFile);
			} catch (final IOException ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (final NullPointerException ex) {
				//Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (final Exception ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			}
		}

		private void actionSaveReportViewAs(final Component dialogParent) {

			FileWriter writer = null;
			try {
				final String newFileName = nRows.getText() + "rows_" + reportFileName;
				final File newDir = Dialogs.selectDirectoryDialog(
						Config.PROPERTY_EXPORT_DIR,
						"Choose the new directory for " + newFileName,
						dialogParent);
				final File newFile = new File(newDir, newFileName);
				writer = new FileWriter(newFile);

				StringBuilder tableData = new StringBuilder();
				// HEADER
				for (final int columnI : colIndToSave) {
					tableData.append(reportTable.getColumnName(columnI));
					tableData.append("\t");
				}
				// delete the last "\t"
				tableData.deleteCharAt(tableData.length() - 1);
				tableData.append("\n");
				writer.write(tableData.toString());

				// TABLE CONTENT
				for (int rowI = 0; rowI < reportTable.getModel().getRowCount(); rowI++) {
					tableData = new StringBuilder();

					for (final int columnI : colIndToSave) {
						String curVal = (String) reportTable.getValueAt(rowI, columnI);

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
			} catch (final NullPointerException ex) {
				//Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} catch (final IOException ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (final IOException ex) {
						log.warn("Failed to close report-to-file writer", ex);
					}
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int decision = Dialogs.showOptionDialogue(Text.All.save, Text.Reports.selectSaveMode, Text.Reports.currentReportView, Text.Reports.completeReport, Text.All.cancel);

			switch (decision) {
				case JOptionPane.YES_OPTION:
					actionSaveReportViewAs(nRows);
					break;
				case JOptionPane.NO_OPTION:
					actionSaveCompleteReportAs(nRows);
					break;
				default: // JOptionPane.CANCEL_OPTION
					break;
			}
		}
	}

	/**
	 * Method to change cursor based on some arbitrary rule.
	 */
	private void displayColumnCursor(final JTable reportTable, final MouseEvent me) {

		final Point p = me.getPoint();
		final int column = reportTable.columnAtPoint(p);
		final String columnName = reportTable.getColumnName(column);
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
