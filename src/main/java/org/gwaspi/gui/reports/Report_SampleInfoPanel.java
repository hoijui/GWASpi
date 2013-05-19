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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.CurrentStudyPanel;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
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
	private int studyId;
	private JButton btn_Save;
	private JButton btn_Back;
	private JButton btn_Help;
	private JPanel pnl_Footer;
	private JScrollPane scrl_ReportTable;
	private JTable tbl_ReportTable;
	// End of variables declaration

	public Report_SampleInfoPanel(final int _studyId) throws IOException {

		studyId = _studyId;

		scrl_ReportTable = new JScrollPane();
		tbl_ReportTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		pnl_Footer = new JPanel();
		btn_Save = new JButton();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, "Study Samples Info", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		tbl_ReportTable.setModel(new DefaultTableModel(
				new Object[][]{
					{null, null, null, "Go!"}
				},
				new String[]{"", "", "", "", ""}));
		tbl_ReportTable.setDefaultRenderer(Object.class, new RowRendererDefault());
		scrl_ReportTable.setViewportView(tbl_ReportTable);

		//<editor-fold defaultstate="expanded" desc="FOOTER">

		btn_Save.setAction(new SaveReportViewAsAction(tbl_ReportTable));

		btn_Back.setAction(new BackAction(studyId));

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.sampleInforeport));

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

		actionLoadReport();
	}

	//private void actionLoadReport(ActionEvent evt) {
	private void actionLoadReport() throws IOException {

		List<SampleInfo> allSamplesFromPool = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyId);

		// Getting data from file and subdividing to series all points by chromosome
		List<Object[]> tableRows = new ArrayList<Object[]>();
		int id = 1;
		for (SampleInfo sampleInfo : allSamplesFromPool) {
			Object[] row = new Object[COLUMNS.length];

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

			row[0] = id;
			row[1] = familyId;
			row[2] = sampleId;
			row[3] = fatherId;
			row[4] = motherId;
			row[5] = sex;
			row[6] = affection;
			row[7] = age;
			row[8] = category;
			row[9] = disease;
			row[10] = population;

			tableRows.add(row);

			id++;
		}

		Object[][] tableMatrix = new Object[tableRows.size()][COLUMNS.length];
		for (int i = 0; i < tableRows.size(); i++) {
			tableMatrix[i] = tableRows.get(i);
		}

		TableModel model = new DefaultTableModel(tableMatrix, COLUMNS);
		tbl_ReportTable.setModel(model);

		//<editor-fold defaultstate="expanded" desc="Linux Sorter">
//		if (!cGlobal.OSNAME.contains("Windows")) {
//			RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
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

		tbl_ReportTable.setRowSorter(sorter);
//		}
		//</editor-fold>
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

	private static class SaveReportViewAsAction extends AbstractAction {

		private JTable reportTable;

		SaveReportViewAsAction(JTable reportTable) {

			this.reportTable = reportTable;
			putValue(NAME, Text.All.save);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/sampleInfo.txt");
				FileWriter writer = new FileWriter(newFile);

				StringBuilder tableData = new StringBuilder();
				//HEADER
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
						String curVal = reportTable.getValueAt(rowNb, colNb).toString();

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
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private int studyId;

		BackAction(int studyId) {

			this.studyId = studyId;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Operation op = OperationsList.getById(studyId);
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentStudyPanel(studyId));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
