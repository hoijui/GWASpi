/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.CurrentStudyPanel;
import org.gwaspi.gui.utils.HelpURLs;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.model.Operation;
import org.gwaspi.samples.SampleManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Report_SampleInfoPanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private File missingFile;
	private int studyId;
	private javax.swing.JButton btn_Save;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JScrollPane scrl_ReportTable;
	private javax.swing.JTable tbl_ReportTable;
	// End of variables declaration

	public Report_SampleInfoPanel(final int _studyId) throws IOException {

		studyId = _studyId;

		scrl_ReportTable = new javax.swing.JScrollPane();
		tbl_ReportTable = new javax.swing.JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		pnl_Footer = new javax.swing.JPanel();
		btn_Save = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Study Samples Info", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		tbl_ReportTable.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][]{
					{null, null, null, "Go!"}
				},
				new String[]{"", "", "", "", ""}));
		tbl_ReportTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());
		scrl_ReportTable.setViewportView(tbl_ReportTable);


		//<editor-fold defaultstate="collapsed" desc="FOOTER">

		btn_Save.setText(org.gwaspi.global.Text.All.save);
		btn_Save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionSaveReportViewAs(_studyId);
			}
		});

		btn_Back.setText(Text.All.Back);
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionBack();
				} catch (IOException ex) {
					Logger.getLogger(Report_SampleInfoPanel.class.getName()).log(Level.SEVERE, null, ex);
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
				.addComponent(scrl_ReportTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(scrl_ReportTable)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>

		actionLoadReport();
	}

	//private void actionLoadReport(ActionEvent evt) {
	private void actionLoadReport() throws IOException {

		List<Map<String, Object>> rsAllSamplesFromPool = SampleManager.getAllSampleInfoFromDBByPoolID(studyId);

		DecimalFormat dfSci = new DecimalFormat("0.##E0#");
		DecimalFormat dfRound = new DecimalFormat("0.#####");

		//Getting data from file and subdividing to series all points by chromosome
		int count = 0;
		ArrayList tableRowAL = new ArrayList();
		while (count < rsAllSamplesFromPool.size()) {
			//PREVENT PHANTOM-DB READS EXCEPTIONS
			if (!rsAllSamplesFromPool.isEmpty() && rsAllSamplesFromPool.get(count).size() == org.gwaspi.constants.cDBSamples.T_CREATE_SAMPLES_INFO.length) {
				Object[] row = new Object[11];

				String familyId = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
				String sampleId = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_SAMPLE_ID).toString();
				String fatherId = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
				String motherId = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
				String sex = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
				String affection = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();
				String age = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_AGE).toString();
				String category = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_CATEGORY).toString();
				String disease = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_DISEASE).toString();
				String population = rsAllSamplesFromPool.get(count).get(org.gwaspi.constants.cDBSamples.f_POPULATION).toString();

				row[0] = count + 1;
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

				tableRowAL.add(row);
			}

			count++;
		}

		Object[][] tableMatrix = new Object[tableRowAL.size()][10];
		for (int i = 0; i < tableRowAL.size(); i++) {
			tableMatrix[i] = (Object[]) tableRowAL.get(i);
		}

		String[] columns = new String[]{"#",
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


		TableModel model = new DefaultTableModel(tableMatrix, columns);
		tbl_ReportTable.setModel(model);

		//<editor-fold defaultstate="collapsed" desc="Linux Sorter">
//        if (!org.gwaspi.constants.cGlobal.OSNAME.contains("Windows")){
		//RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
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

			public Comparator getComparator(int column) {
				return comparator;
			}

			public boolean useToString(int column) {
				return false;
			}
		};

		tbl_ReportTable.setRowSorter(sorter);
//        }
		//</editor-fold>




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

	private void actionSaveReportViewAs(int studyId) {
		try {
			File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath() + "/sampleInfo.txt");
			FileWriter writer = new FileWriter(newFile);

			StringBuilder tableData = new StringBuilder();
			//HEADER
			for (int k = 0; k < tbl_ReportTable.getColumnCount(); k++) {
				tableData.append(tbl_ReportTable.getColumnName(k));
				if (k != tbl_ReportTable.getColumnCount() - 1) {
					tableData.append("\t");
				}
			}
			tableData.append("\n");
			writer.write(tableData.toString());

			//TABLE CONTENT
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
		Operation op = new Operation(studyId);
		org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentStudyPanel(studyId);
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void actionHelp() {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.sampleInforeport);
		} catch (Exception ex) {
		}
	}
}
