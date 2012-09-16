package org.gwaspi.gui;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.model.Study;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CurrentStudyPanel extends JPanel {

	// Variables declaration
	private Study study;
	private Map<Integer, Object> treeChildrenLHM = new LinkedHashMap<Integer, Object>();
	private JButton btn_DeleteMatrix;
	private JButton btn_LoadGenotypes;
	private JButton btn_UpdateSampleInfo;
	private JButton btn_SaveDesc;
	private JButton btn_DeleteStudy;
	private JPanel pnl_MatrixTable;
	private JPanel pnl_StudyDesc;
	private JPanel pnl_Footer;
	private JButton btn_Back;
	private JButton btn_Help;
	private JScrollPane scrl_Desc;
	private JScrollPane scrl_MatrixTable;
	private JTable tbl_MatrixTable;
	private JTextArea txtA_StudyDesc;
	// End of variables declaration

	/**
	 *
	 * @param _studyId
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public CurrentStudyPanel(int _studyId) throws IOException {

		study = new org.gwaspi.model.Study(_studyId);
		DefaultMutableTreeNode matrixNode = (DefaultMutableTreeNode) org.gwaspi.gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent();
		treeChildrenLHM = org.gwaspi.gui.utils.NodeToPathCorrespondence.buildNodeToPathCorrespondence(matrixNode, false);

		pnl_StudyDesc = new JPanel();
		scrl_Desc = new JScrollPane();
		txtA_StudyDesc = new JTextArea();
		btn_LoadGenotypes = new JButton();
		btn_UpdateSampleInfo = new JButton();
		btn_SaveDesc = new JButton();
		btn_DeleteStudy = new JButton();
		pnl_MatrixTable = new JPanel();
		pnl_Footer = new JPanel();
		btn_Help = new JButton();
		btn_Back = new JButton();
		scrl_MatrixTable = new JScrollPane();
		tbl_MatrixTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false; //Renders column 0 uneditable.
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
		};
		tbl_MatrixTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());

		btn_DeleteMatrix = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Study.study, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_StudyDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Study.currentStudy + " " + study.getStudyName() + ", StudyID: STUDY_" + study.getStudyId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setColumns(20);
		txtA_StudyDesc.setRows(5);
		txtA_StudyDesc.setText(study.getStudyDescription().toString());
		scrl_Desc.setViewportView(txtA_StudyDesc);

		btn_SaveDesc.setAction(new SaveDescriptionAction(study, txtA_StudyDesc));

		btn_DeleteStudy.setAction(new DeleteStudyAction(study, this));

		btn_UpdateSampleInfo.setAction(new LoadSampleInfoAction(study));

		btn_LoadGenotypes.setAction(new LoadGenotypesAction(study));

		pnl_MatrixTable.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrices, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		tbl_MatrixTable.setModel(new DefaultTableModel(
				org.gwaspi.model.MatricesList.getMatricesTable(study.getStudyId()),
				new String[]{
					Text.Matrix.matrixID, Text.Matrix.matrix, Text.All.description, Text.All.createDate
				}));
		scrl_MatrixTable.setViewportView(tbl_MatrixTable);

		btn_DeleteMatrix.setAction(new DeleteMatrixAction(study, this, tbl_MatrixTable));
		btn_DeleteMatrix.setBackground(new Color(242, 138, 121));




		//<editor-fold defaultstate="collapsed" desc="LAYOUT STUDY">
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


		//<editor-fold defaultstate="collapsed" desc="LAYOUT MATRICES">
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


		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		btn_Back.setAction(new BackAction());
		btn_Help.setAction(new HelpAction());
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 437, Short.MAX_VALUE)
				.addComponent(btn_Help)));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(0, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>


		// <editor-fold defaultstate="collapsed/expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_StudyDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_StudyDesc, GroupLayout.PREFERRED_SIZE, 243, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_MatrixTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		// </editor-fold>
	}

	private static class SaveDescriptionAction extends AbstractAction {

		private Study study;
		private JTextArea descriptionSource;

		SaveDescriptionAction(Study study, JTextArea descriptionSource) {

			this.study = study;
			this.descriptionSource = descriptionSource;
			putValue(NAME, Text.All.saveDescription);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				org.gwaspi.global.Utils.logBlockInStudyDesc(descriptionSource.getText(), study.getStudyId());

				DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
				db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
						org.gwaspi.constants.cDBGWASpi.T_STUDIES,
						new String[]{constants.cDBGWASpi.f_STUDY_DESCRIPTION},
						new Object[]{descriptionSource.getText()},
						new String[]{constants.cDBGWASpi.f_ID},
						new Object[]{study.getStudyId()});
			} catch (IOException ex) {
				Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static class LoadGenotypesAction extends AbstractAction {

		private Study study;

		LoadGenotypesAction(Study study) {

			this.study = study;
			putValue(NAME, Text.Matrix.loadGenotypes);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			//gui.GWASpiExplorerPanel.pnl_Content = new LoadDataPanel(study.getStudyId());
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new LoadDataPanel(study.getStudyId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		}
	}

	private static class LoadSampleInfoAction extends AbstractAction {

		private Study study;

		LoadSampleInfoAction(Study study) {

			this.study = study;
			putValue(NAME, Text.Study.updateSampleInfo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				org.gwaspi.gui.utils.Dialogs.showInfoDialogue(Text.Study.infoSampleInfo);
				File sampleInfoFile = org.gwaspi.gui.utils.Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
				if (sampleInfoFile != null && sampleInfoFile.exists()) {
					org.gwaspi.gui.ProcessTab.showTab();

					MultiOperations.updateSampleInfo(study.getStudyId(),
							sampleInfoFile);

				}
			} catch (Exception ex) {
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.All.warnLoadError + "\n" + org.gwaspi.global.Text.All.warnWrongFormat);
				System.out.println(org.gwaspi.global.Text.All.warnLoadError);
				System.out.println(org.gwaspi.global.Text.All.warnWrongFormat);
				//Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static class DeleteMatrixAction extends AbstractAction {

		private Study study;
		private Component dialogParent;
		private JTable table;

		DeleteMatrixAction(Study study, Component dialogParent, JTable table) {

			this.study = study;
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
						dialogParent.setCursor(org.gwaspi.gui.utils.CursorUtils.waitCursor);
						for (int i = 0; i < selectedMatrices.length; i++) {
							int tmpMatrixRow = selectedMatrices[i];
							int matrixId = (Integer) table.getModel().getValueAt(tmpMatrixRow, 0);
							//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
							if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(null, matrixId, null)) {
								boolean deleteReport = false;
								if (deleteReportOption == JOptionPane.YES_OPTION) {
									deleteReport = true;
								}
								MultiOperations.deleteMatrix(study.getStudyId(), matrixId, deleteReport);
								//netCDF.matrices.MatrixManager.deleteMatrix(matrixId, deleteReport);
							} else {
								org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
							}
						}
					}
				}
			}
			dialogParent.setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);
		}
	}

	private static class DeleteStudyAction extends AbstractAction {

		private Study study;
		private Component dialogParent;

		DeleteStudyAction(Study study, Component dialogParent) {

			this.study = study;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.Study.deleteStudy);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// TODO TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
			if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(study.getStudyId(), null, null)) {
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

						boolean deleteReport = false;
						if (deleteReportOption == JOptionPane.YES_OPTION) {
							deleteReport = true;
						}
						MultiOperations.deleteStudy(study.getStudyId(), deleteReport);
					}
				}
			} else {
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
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
				org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
				org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
				org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
			} catch (IOException ex) {
				Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}


	private static class HelpAction extends AbstractAction {

		HelpAction() {

			putValue(NAME, Text.Help.help);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.currentStudy);
			} catch (IOException ex) {
				Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
