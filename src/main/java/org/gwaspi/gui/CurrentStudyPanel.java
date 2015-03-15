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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Deleter;
import org.gwaspi.threadbox.Threaded_UpdateSampleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentStudyPanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(CurrentStudyPanel.class);
	static final Color DANGER_RED = new Color(242, 138, 121);
	static final int GAP = 18;
	static final int GAP_SMALL = 5;

	// Variables declaration // TODO remove all of these comments
	// End of variables declaration

	public CurrentStudyPanel(StudyKey studyKey) throws IOException {

		Study study = StudyList.getStudy(studyKey);

		JPanel pnl_desc = new JPanel();
		JScrollPane scrl_desc = new JScrollPane();
		JTextArea txtA_desc = new JTextArea();
		pnl_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(createTitle(study)));
		txtA_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.All.description)); // NOI18N
		txtA_desc.setColumns(20);
		txtA_desc.setRows(5);
		txtA_desc.setText(study.getDescription());
		scrl_desc.setViewportView(txtA_desc);
		pnl_desc.setLayout(new BorderLayout(GAP, GAP_SMALL));
		JButton btn_DeleteStudy = new JButton();
		JButton btn_LoadGenotypes = new JButton();
		JButton btn_UpdateSampleInfo = new JButton();
		JButton btn_SaveDesc = new JButton();
		JPanel pnl_StudyDescButtons = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_DeleteStudy},
				new JComponent[] {btn_LoadGenotypes, btn_UpdateSampleInfo, btn_SaveDesc});
		pnl_desc.add(scrl_desc, BorderLayout.CENTER);
		pnl_desc.add(pnl_StudyDescButtons, BorderLayout.SOUTH);

		JPanel pnl_MatrixTable = new JPanel();
		JScrollPane scrl_MatrixTable = new JScrollPane();
		JTable tbl_MatrixTable = new MatrixTable();
		JButton btn_DeleteMatrix = new JButton();
		pnl_MatrixTable.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Matrix.matrices)); // NOI18N
		tbl_MatrixTable.setModel(new MatricesTableModel(MatricesList.getMatricesTable(studyKey)));
		tbl_MatrixTable.setDefaultRenderer(Object.class, new RowRendererDefault());
		scrl_MatrixTable.setViewportView(tbl_MatrixTable);
		btn_DeleteMatrix.setBackground(DANGER_RED);
		pnl_MatrixTable.setLayout(new BorderLayout(GAP, GAP_SMALL));
		pnl_MatrixTable.add(scrl_MatrixTable, BorderLayout.CENTER);
		JPanel pnl_MatrixTableButtons = GWASpiExplorerPanel.createButtonsPanel(btn_DeleteMatrix);
		pnl_MatrixTable.add(pnl_MatrixTableButtons, BorderLayout.SOUTH);

		JButton btn_Help = new JButton();
		JButton btn_Back = new JButton();
		JPanel pnl_Footer = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_Back},
				new JComponent[] {btn_Help});

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(Text.Study.study)); // NOI18N
		this.setLayout(new BorderLayout(GAP, GAP));
		this.add(pnl_desc, BorderLayout.NORTH);
		this.add(pnl_MatrixTable, BorderLayout.CENTER);
		this.add(pnl_Footer, BorderLayout.SOUTH);

		btn_SaveDesc.setAction(new SaveDescriptionAction(study, txtA_desc));
		btn_DeleteStudy.setAction(new DeleteStudyAction(studyKey, this));
		btn_UpdateSampleInfo.setAction(new LoadSampleInfoAction(studyKey, this));
		btn_LoadGenotypes.setAction(new LoadGenotypesAction(study));
		btn_DeleteMatrix.setAction(new DeleteMatrixAction(studyKey, this, tbl_MatrixTable));
		btn_Back.setAction(new BackAction());
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.currentStudy));
	}

	private static String createTitle(Study study) {
		return Text.Study.currentStudy + " " + study.getName() + ", StudyID: STUDY_" + study.getId(); // NOI18N
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
			GWASpiExplorerPanel.getSingleton().setPnlContent(new LoadDataPanel(StudyKey.valueOf(study)));
			GWASpiExplorerPanel.getSingleton().getScrlContent().setViewportView(GWASpiExplorerPanel.getSingleton().getPnlContent());
		}
	}

	private static class LoadSampleInfoAction extends AbstractAction {

		private final StudyKey studyKey;
		private final Component dialogParent;

		LoadSampleInfoAction(StudyKey studyKey, final Component dialogParent) {

			this.studyKey = studyKey;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.Study.updateSampleInfo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				File sampleInfoFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION, Text.Study.infoSampleInfo, dialogParent);
				if (sampleInfoFile == null) {
					// the user chose "Cancel"
					return;
				}
				if (!sampleInfoFile.exists()) {
					throw new RuntimeException("Chosen sample-info file does not exist: "
							+ sampleInfoFile.getAbsolutePath());
				}

				ProcessTab.getSingleton().showTab();
				final CommonRunnable updateSampleInfoTask = new Threaded_UpdateSampleInfo(studyKey, sampleInfoFile);
				MultiOperations.queueTask(updateSampleInfoTask);
			} catch (Exception ex) {
				Dialogs.showWarningDialogue(Text.All.warnLoadError + "\n" + Text.All.warnWrongFormat);
				log.error(Text.All.warnLoadError, ex);
				log.error(Text.All.warnWrongFormat);
			}
		}
	}

	private static class DeleteMatrixAction extends AbstractAction implements ListSelectionListener {

		private final StudyKey studyKey;
		private final Component dialogParent;
		private final JTable table;

		DeleteMatrixAction(StudyKey studyKey, Component dialogParent, JTable table) {

			this.studyKey = studyKey;
			this.dialogParent = dialogParent;
			this.table = table;
			putValue(NAME, "Delete Matrix");

			this.table.getSelectionModel().addListSelectionListener(this);
			refreshEnabledness();
		}

		private void refreshEnabledness() {

			final boolean selectedMatrices = (table.getSelectedRowCount() > 0);
			setEnabled(selectedMatrices);
		}

		@Override
		public void valueChanged(ListSelectionEvent evt) {

			if (evt.getValueIsAdjusting()) {
				return;
			}

			refreshEnabledness();
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			if (table.getSelectedRow() != -1) {
				int[] selectedMatrices = table.getSelectedRows();
				final int option = JOptionPane.showConfirmDialog(dialogParent, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					final int deleteReportsOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (deleteReportsOption != JOptionPane.CANCEL_OPTION) {
						dialogParent.setCursor(CursorUtils.WAIT_CURSOR);
						final boolean deleteReports = (deleteReportsOption == JOptionPane.YES_OPTION);
						for (int i = 0; i < selectedMatrices.length; i++) {
							final int tmpMatrixRow = selectedMatrices[i];
							final int matrixid = (Integer) table.getModel().getValueAt(tmpMatrixRow, 0);
							final MatrixKey matrixKey = new MatrixKey(studyKey, matrixid);
							// test if the deleted item is required for a queued worker
							final Deleter matrixDeleter = new Deleter(matrixKey, deleteReports);
							if (MultiOperations.canBeDoneNow(matrixDeleter)) {
								MultiOperations.queueTask(matrixDeleter);
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

			final int option = JOptionPane.showConfirmDialog(dialogParent, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
			if (option == JOptionPane.YES_OPTION) {
				final int deleteReportsOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
				if (deleteReportsOption != JOptionPane.CANCEL_OPTION) {
					final boolean deleteReports = (deleteReportsOption == JOptionPane.YES_OPTION);
					final Deleter studyDeleter = new Deleter(studyKey, deleteReports);
					// test if the deleted item is required for a queued worker
					if (MultiOperations.canBeDoneNow(studyDeleter)) {
						MultiOperations.queueTask(studyDeleter);
					} else {
						Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
					}
				}
			}
		}
	}

	private static class BackAction extends AbstractAction {

		BackAction() {

			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().selectNodeStudyManagement();
		}
	}
}
