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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.BackAction;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Report_SampleInfoPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(Report_SampleInfoPanel.class);

	private static final String[] COLUMNS = new String[] {
			"#",
			Text.Reports.familyId,
			Text.Reports.sampleId,
			Text.Reports.fatherId,
			Text.Reports.motherId,
			Text.Reports.sex,
			Text.Reports.affection,
			Text.Reports.age,
			Text.Reports.category,
			Text.Reports.disease,
			Text.Reports.population};

	// Variables declaration - do not modify
	private File missingFile;
	private final StudyKey studyKey;
	private final JButton btn_Save;
	private final JButton btn_Back;
	private final JButton btn_Help;
	private final JPanel pnl_Footer;
	private final JScrollPane scrl_ReportTable;
	private final JTable tbl_ReportTable;
	// End of variables declaration

	public Report_SampleInfoPanel(final StudyKey studyKey) throws IOException {

		this.studyKey = studyKey;

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

		setBorder(GWASpiExplorerPanel.createMainTitledBorder("Study Samples Info")); // NOI18N

		tbl_ReportTable.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null, null, "Go!"}
				},
				new String[] {"", "", "", "", ""}));
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
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrl_ReportTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(scrl_ReportTable)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>

		btn_Save.setAction(new SaveReportViewAsAction(tbl_ReportTable, this));
		btn_Back.setAction(new BackAction(studyKey));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.sampleInfoReport));

		actionLoadReport();
	}

	private void actionLoadReport() throws IOException {

		List<SampleInfo> allSamplesFromPool = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyKey);

		// Getting data from file and subdividing to series all points by chromosome
		Object[][] tableMatrix = new Object[allSamplesFromPool.size()][COLUMNS.length];
		int id = 0;
		for (SampleInfo sampleInfo : allSamplesFromPool) {
			String familyId = sampleInfo.getFamilyId();
			String sampleId = sampleInfo.getSampleId();
			String fatherId = sampleInfo.getFatherId();
			String motherId = sampleInfo.getMotherId();
			String sex = sampleInfo.getSexStr();
			String affection = sampleInfo.getAffectionStr();
			String age = String.valueOf(sampleInfo.getAge());
			String category = sampleInfo.getCategory();
			String disease = sampleInfo.getDisease();
			String population = sampleInfo.getPopulation();

			tableMatrix[id][0] = id + 1;
			tableMatrix[id][1] = familyId;
			tableMatrix[id][2] = sampleId;
			tableMatrix[id][3] = fatherId;
			tableMatrix[id][4] = motherId;
			tableMatrix[id][5] = sex;
			tableMatrix[id][6] = affection;
			tableMatrix[id][7] = age;
			tableMatrix[id][8] = category;
			tableMatrix[id][9] = disease;
			tableMatrix[id][10] = population;

			id++;
		}

		TableModel model = new DefaultTableModel(tableMatrix, COLUMNS);
		tbl_ReportTable.setModel(model);

		TableRowSorter sorter = new TableRowSorter(model) {
			Comparator<Object> comparator = new Comparator<Object>() {
				@Override
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

		tbl_ReportTable.setRowSorter(sorter);
	}

	private void actionSaveCompleteReportAs(StudyKey studyKey, String chartPath, final Component dialogParent) {

		try {
			final String reportPath = Study.constructReportsPath(studyKey);
			final File origFile = new File(reportPath + chartPath);
			final File newDir = Dialogs.selectDirectoryDialog(Config.PROPERTY_EXPORT_DIR, "Choose the new directory for " + chartPath, dialogParent);
			final File newFile = new File(newDir, chartPath);
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

	private static class SaveReportViewAsAction extends AbstractAction {

		private final JTable reportTable;
		private final Component dialogParent;

		SaveReportViewAsAction(JTable reportTable, final Component dialogParent) {

			this.reportTable = reportTable;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			FileWriter writer = null;
			try {
				final String newFileName = "sampleInfo.txt";
				final File newDir = Dialogs.selectDirectoryDialog(Config.PROPERTY_EXPORT_DIR, "Choose the new directory for " + newFileName, dialogParent);
				final File newFile = new File(newDir, newFileName);
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
						Object curVal = reportTable.getValueAt(rowNb, colNb);
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
				writer.close();
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
						log.warn("Failed to close report-to-file writer", ex);
					}
				}
			}
		}
	}
}
