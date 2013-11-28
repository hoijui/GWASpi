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

package org.gwaspi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import org.gwaspi.global.Text;
import org.gwaspi.global.Utils;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.MatricesTableModel;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentStudyPanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(CurrentStudyPanel.class);
	private static final Color DANGER_RED = new Color(242, 138, 121);

	// Variables declaration // TODO remove all of these comments
	// End of variables declaration

	private static class JMatrixTable extends JTable {

		@Override
		public boolean isCellEditable(int row, int col) {
			return false; // Renders column 0 uneditable.
		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
			if (c instanceof JComponent && getValueAt(rowIndex, vColIndex) != null) {
				JComponent jc = (JComponent) c;
				jc.setToolTipText("<html>" + getValueAt(rowIndex, vColIndex).toString().replaceAll("\n", "<br>") + "</html>");
			}
			return c;
		}
	}

	public CurrentStudyPanel(StudyKey studyKey) throws IOException {

		Study study = StudyList.getStudy(studyKey);

		JPanel pnl_StudyDesc = new JPanel();
		JScrollPane scrl_Desc = new JScrollPane();
		JTextArea txtA_StudyDesc = new JTextArea();
		JPanel pnl_StudyDescButtons = new JPanel();
		JButton btn_DeleteStudy = new JButton();
		JButton btn_LoadGenotypes = new JButton();
		JButton btn_UpdateSampleInfo = new JButton();
		JButton btn_SaveDesc = new JButton();
		JPanel pnl_MatrixTable = new JPanel();
		JPanel pnl_MatrixTableButtons = new JPanel();
		JScrollPane scrl_MatrixTable = new JScrollPane();
		JTable tbl_MatrixTable = new JMatrixTable();
		JButton btn_DeleteMatrix = new JButton();
		JPanel pnl_Footer = new JPanel();
		JButton btn_Help = new JButton();
		JButton btn_Back = new JButton();

		tbl_MatrixTable.setDefaultRenderer(Object.class, new RowRendererDefault());

		btn_SaveDesc.setAction(new SaveDescriptionAction(study, txtA_StudyDesc));
		btn_DeleteStudy.setAction(new DeleteStudyAction(studyKey, this));
		btn_UpdateSampleInfo.setAction(new LoadSampleInfoAction(study));
		btn_LoadGenotypes.setAction(new LoadGenotypesAction(study));
		btn_DeleteMatrix.setAction(new DeleteMatrixAction(studyKey, this, tbl_MatrixTable));
		btn_Back.setAction(new BackAction());
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.currentStudy));

		setBorder(BorderFactory.createTitledBorder(null, Text.Study.study, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_StudyDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Study.currentStudy + " " + study.getName() + ", StudyID: STUDY_" + study.getId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setColumns(20);
		txtA_StudyDesc.setRows(5);
		txtA_StudyDesc.setText(study.getDescription().toString());
		scrl_Desc.setViewportView(txtA_StudyDesc);

		pnl_MatrixTable.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrices, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		tbl_MatrixTable.setModel(new MatricesTableModel(MatricesList.getMatricesTable(StudyKey.valueOf(study))));
		scrl_MatrixTable.setViewportView(tbl_MatrixTable);

		btn_DeleteMatrix.setBackground(DANGER_RED);

		//<editor-fold defaultstate="expanded" desc="LAYOUT STUDY">
		GroupLayout pnl_StudyDescLayout = new GroupLayout(pnl_StudyDesc);
		pnl_StudyDesc.setLayout(pnl_StudyDescLayout);
		pnl_StudyDescLayout.setHorizontalGroup(
				pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(scrl_Desc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.LEADING, pnl_StudyDescLayout.createSequentialGroup()
				.addComponent(btn_DeleteStudy, GroupLayout.PREFERRED_SIZE, 159, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
				.addComponent(btn_LoadGenotypes, GroupLayout.PREFERRED_SIZE, 175, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_UpdateSampleInfo, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_SaveDesc, GroupLayout.PREFERRED_SIZE, 159, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));

		pnl_StudyDescLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});

		pnl_StudyDescLayout.setVerticalGroup(
				pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addComponent(scrl_Desc, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_DeleteStudy)
				.addComponent(btn_SaveDesc)
				.addComponent(btn_UpdateSampleInfo, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_LoadGenotypes, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pnl_StudyDescLayout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT MATRICES">
		GroupLayout pnl_MatrixTableLayout = new GroupLayout(pnl_MatrixTable);
		pnl_MatrixTable.setLayout(pnl_MatrixTableLayout);
		pnl_MatrixTableLayout.setHorizontalGroup(
				pnl_MatrixTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixTableLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixTable, GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
				.addComponent(btn_DeleteMatrix, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_MatrixTableLayout.setVerticalGroup(
				pnl_MatrixTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixTableLayout.createSequentialGroup()
				.addComponent(scrl_MatrixTable, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteMatrix)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		FlowLayout lyt_Footer = new FlowLayout();
		pnl_Footer.setLayout(lyt_Footer);
		pnl_Footer.add(btn_Back);
		pnl_Footer.add(btn_Help);
//		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
//		pnl_Footer.setLayout(pnl_FooterLayout);
//		pnl_FooterLayout.setHorizontalGroup(
//				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//				.addGroup(pnl_FooterLayout.createSequentialGroup()
//				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
//				.addGap(18, 437, Short.MAX_VALUE)
//				.addComponent(btn_Help)));
//
//		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});
//		pnl_FooterLayout.setVerticalGroup(
//				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//				.addGroup(pnl_FooterLayout.createSequentialGroup()
//				.addContainerGap(0, Short.MAX_VALUE)
//				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//				.addComponent(btn_Back)
//				.addComponent(btn_Help))));
		//</editor-fold>

		// <editor-fold defaultstate="expanded" desc="LAYOUT">
		this.setLayout(new BorderLayout());
		this.add(pnl_StudyDesc, BorderLayout.NORTH);
		this.add(pnl_MatrixTable, BorderLayout.CENTER);
		this.add(pnl_Footer, BorderLayout.SOUTH);
//		GroupLayout layout = new GroupLayout(this);
//		this.setLayout(layout);
//		layout.setHorizontalGroup(
//				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
//				.addContainerGap()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//				.addComponent(pnl_MatrixTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//				.addComponent(pnl_StudyDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//				.addContainerGap()));
//		layout.setVerticalGroup(
//				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//				.addGroup(layout.createSequentialGroup()
//				.addContainerGap()
//				.addComponent(pnl_StudyDesc, GroupLayout.PREFERRED_SIZE, 243, GroupLayout.PREFERRED_SIZE)
//				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//				.addComponent(pnl_MatrixTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		// </editor-fold>
	}

	private static class SaveDescriptionAction extends AbstractAction {

		private final Study study;
		private final JTextArea descriptionSource;

		SaveDescriptionAction(Study study, JTextArea descriptionSource) {

			this.study = study;
			this.descriptionSource = descriptionSource;
			putValue(NAME, Text.All.saveDescription);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Utils.logBlockInStudyDesc(descriptionSource.getText(), study.getId());

				study.setDescription(descriptionSource.getText());
				StudyList.updateStudy(study);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class LoadGenotypesAction extends AbstractAction {

		private final Study study;

		LoadGenotypesAction(Study study) {

			this.study = study;
			putValue(NAME, Text.Matrix.loadGenotypes);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			//GWASpiExplorerPanel.getSingleton().pnl_Content = new LoadDataPanel(study.getId());
			GWASpiExplorerPanel.getSingleton().setPnl_Content(new LoadDataPanel(StudyKey.valueOf(study)));
			GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
		}
	}

	private static class LoadSampleInfoAction extends AbstractAction {

		private final Study study;

		LoadSampleInfoAction(Study study) {

			this.study = study;
			putValue(NAME, Text.Study.updateSampleInfo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Dialogs.showInfoDialogue(Text.Study.infoSampleInfo);
				File sampleInfoFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
				if (sampleInfoFile != null && sampleInfoFile.exists()) {
					ProcessTab.getSingleton().showTab();

					MultiOperations.updateSampleInfo(study.getId(),
							sampleInfoFile);
				}
			} catch (Exception ex) {
				Dialogs.showWarningDialogue(Text.All.warnLoadError + "\n" + Text.All.warnWrongFormat);
				log.error(Text.All.warnLoadError, ex);
				log.error(Text.All.warnWrongFormat);
				//Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static class DeleteMatrixAction extends AbstractAction {

		private final StudyKey studyKey;
		private final Component dialogParent;
		private final JTable table;

		DeleteMatrixAction(StudyKey studyKey, Component dialogParent, JTable table) {

			this.studyKey = studyKey;
			this.dialogParent = dialogParent;
			this.table = table;
			putValue(NAME, "Delete Matrix");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (table.getSelectedRow() != -1) {
				int[] selectedMatrices = table.getSelectedRows();
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {
						dialogParent.setCursor(CursorUtils.WAIT_CURSOR);
						for (int i = 0; i < selectedMatrices.length; i++) {
							int tmpMatrixRow = selectedMatrices[i];
							int matrixid = (Integer) table.getModel().getValueAt(tmpMatrixRow, 0);
							MatrixKey matrixKey = new MatrixKey(studyKey, matrixid);
							//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
							if (SwingWorkerItemList.permitsDeletionOf(matrixKey)) {
								boolean deleteReport = false;
								if (deleteReportOption == JOptionPane.YES_OPTION) {
									deleteReport = true;
								}
								MultiOperations.deleteMatrix(matrixKey, deleteReport);
								//netCDF.matrices.MatrixManager.deleteMatrix(matrixId, deleteReport);
							} else {
								Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
							}
						}
					}
				}
			}
			dialogParent.setCursor(CursorUtils.DEFAULT_CURSOR);
		}
	}

	private static class DeleteStudyAction extends AbstractAction {

		private final StudyKey studyKey;
		private final Component dialogParent;

		DeleteStudyAction(StudyKey studyKey, Component dialogParent) {

			this.studyKey = studyKey;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.Study.deleteStudy);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// TODO TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
			if (SwingWorkerItemList.permitsDeletionOf(studyKey)) {
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

						boolean deleteReport = false;
						if (deleteReportOption == JOptionPane.YES_OPTION) {
							deleteReport = true;
						}
						MultiOperations.deleteStudy(studyKey, deleteReport);
					}
				}
			} else {
				Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
			}
		}
	}


	private static class BackAction extends AbstractAction {

		BackAction() {

			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new StudyManagementPanel());
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
