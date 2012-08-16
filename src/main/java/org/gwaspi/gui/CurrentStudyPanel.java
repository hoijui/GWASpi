package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CurrentStudyPanel extends javax.swing.JPanel {

	// Variables declaration
	private org.gwaspi.model.Study study;
	private LinkedHashMap treeChildrenLHM = new LinkedHashMap();
	private javax.swing.JButton btn_DeleteMatrix;
	private javax.swing.JButton btn_LoadGenotypes;
	private javax.swing.JButton btn_UpdateSampleInfo;
	private javax.swing.JButton btn_SaveDesc;
	private javax.swing.JButton btn_DeleteStudy;
	private javax.swing.JPanel pnl_MatrixTable;
	private javax.swing.JPanel pnl_StudyDesc;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JScrollPane scrl_Desc;
	private javax.swing.JScrollPane scrl_MatrixTable;
	private javax.swing.JTable tbl_MatrixTable;
	private javax.swing.JTextArea txtA_StudyDesc;
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

		pnl_StudyDesc = new javax.swing.JPanel();
		scrl_Desc = new javax.swing.JScrollPane();
		txtA_StudyDesc = new javax.swing.JTextArea();
		btn_LoadGenotypes = new javax.swing.JButton();
		btn_UpdateSampleInfo = new javax.swing.JButton();
		btn_SaveDesc = new javax.swing.JButton();
		btn_DeleteStudy = new javax.swing.JButton();
		pnl_MatrixTable = new javax.swing.JPanel();
		pnl_Footer = new javax.swing.JPanel();
		btn_Help = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();
		scrl_MatrixTable = new javax.swing.JScrollPane();
		tbl_MatrixTable = new javax.swing.JTable() {
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

		btn_DeleteMatrix = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Study.study, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_StudyDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Study.currentStudy + " " + study.getStudyName() + ", StudyID: STUDY_" + study.getStudyId(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.All.description, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_StudyDesc.setColumns(20);
		txtA_StudyDesc.setRows(5);
		txtA_StudyDesc.setText(study.getStudyDescription().toString());
		scrl_Desc.setViewportView(txtA_StudyDesc);

		btn_SaveDesc.setText(Text.All.saveDescription);
		btn_SaveDesc.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveDescActionPerformed();
			}
		});

		btn_DeleteStudy.setText(Text.Study.deleteStudy);
		btn_DeleteStudy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteStudy(evt);
			}
		});

		btn_UpdateSampleInfo.setText(Text.Study.updateSampleInfo);
		btn_UpdateSampleInfo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionLoadSampleInfo();
				} catch (Exception ex) {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.All.warnLoadError + "\n" + org.gwaspi.global.Text.All.warnWrongFormat);
					System.out.println(org.gwaspi.global.Text.All.warnLoadError);
					System.out.println(org.gwaspi.global.Text.All.warnWrongFormat);
					//Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		btn_LoadGenotypes.setText(Text.Matrix.loadGenotypes);
		btn_LoadGenotypes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_LoadGenotypesActionPerformed(evt);
			}
		});

		pnl_MatrixTable.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.matrices, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		tbl_MatrixTable.setModel(new javax.swing.table.DefaultTableModel(
				org.gwaspi.model.MatricesList.getMatricesTable(study.getStudyId()),
				new String[]{
					Text.Matrix.matrixID, Text.Matrix.matrix, Text.All.description, Text.All.createDate
				}));
		scrl_MatrixTable.setViewportView(tbl_MatrixTable);

		btn_DeleteMatrix.setText("Delete Matrix");
		btn_DeleteMatrix.setBackground(new java.awt.Color(242, 138, 121));
		btn_DeleteMatrix.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteMatrix(evt);
			}
		});




		//<editor-fold defaultstate="collapsed" desc="LAYOUT STUDY">
		javax.swing.GroupLayout pnl_StudyDescLayout = new javax.swing.GroupLayout(pnl_StudyDesc);
		pnl_StudyDesc.setLayout(pnl_StudyDescLayout);
		pnl_StudyDescLayout.setHorizontalGroup(
				pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(scrl_Desc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_StudyDescLayout.createSequentialGroup()
				.addComponent(btn_DeleteStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
				.addComponent(btn_LoadGenotypes, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_UpdateSampleInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_SaveDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));

		pnl_StudyDescLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});

		pnl_StudyDescLayout.setVerticalGroup(
				pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addComponent(scrl_Desc, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_DeleteStudy)
				.addComponent(btn_SaveDesc)
				.addComponent(btn_UpdateSampleInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_LoadGenotypes, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pnl_StudyDescLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT MATRICES">
		javax.swing.GroupLayout pnl_MatrixTableLayout = new javax.swing.GroupLayout(pnl_MatrixTable);
		pnl_MatrixTable.setLayout(pnl_MatrixTableLayout);
		pnl_MatrixTableLayout.setHorizontalGroup(
				pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixTableLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixTable, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
				.addComponent(btn_DeleteMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_MatrixTableLayout.setVerticalGroup(
				pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixTableLayout.createSequentialGroup()
				.addComponent(scrl_MatrixTable, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteMatrix)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		btn_Back.setText("Back");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_BackActionPerformed(evt);
			}
		});
		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_HelpActionPerformed(evt);
			}
		});
		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 437, Short.MAX_VALUE)
				.addComponent(btn_Help)));

		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Help});
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(0, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>


		// <editor-fold defaultstate="collapsed/expanded" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_StudyDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_StudyDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_MatrixTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		// </editor-fold>
	}

	private void saveDescActionPerformed() {
		try {
			org.gwaspi.global.Utils.logBlockInStudyDesc(txtA_StudyDesc.getText(), study.getStudyId());

			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STUDIES,
					new String[]{constants.cDBGWASpi.f_STUDY_DESCRIPTION},
					new Object[]{txtA_StudyDesc.getText()},
					new String[]{constants.cDBGWASpi.f_ID},
					new Object[]{study.getStudyId()});

		} catch (IOException ex) {
			Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void btn_LoadGenotypesActionPerformed(ActionEvent evt) {
		//gui.GWASpiExplorerPanel.pnl_Content = new LoadDataPanel(study.getStudyId());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new LoadDataPanel(study.getStudyId());
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void actionLoadSampleInfo() throws IOException {
		org.gwaspi.gui.utils.Dialogs.showInfoDialogue(Text.Study.infoSampleInfo);
		File sampleInfoFile = org.gwaspi.gui.utils.Dialogs.selectFilesAndDirertoriesDialogue(JOptionPane.OK_OPTION);
		if (sampleInfoFile != null && sampleInfoFile.exists()) {
			org.gwaspi.gui.ProcessTab.showTab();

			MultiOperations.updateSampleInfo(study.getStudyId(),
					sampleInfoFile);

		}
	}

	private void actionDeleteMatrix(ActionEvent evt) {
		if (tbl_MatrixTable.getSelectedRow() != -1) {
			int[] selectedMatrices = tbl_MatrixTable.getSelectedRows();
			int option = JOptionPane.showConfirmDialog(this, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
			if (option == JOptionPane.YES_OPTION) {
				int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
				if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {
					setCursor(org.gwaspi.gui.utils.CursorUtils.waitCursor);
					for (int i = 0; i < selectedMatrices.length; i++) {
						int tmpMatrixRow = selectedMatrices[i];
						int matrixId = (Integer) tbl_MatrixTable.getModel().getValueAt(tmpMatrixRow, 0);
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
		setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);
	}

	private void actionDeleteStudy(ActionEvent evt) {
		//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
		if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(study.getStudyId(), null, null)) {
			int option = JOptionPane.showConfirmDialog(this, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
			if (option == JOptionPane.YES_OPTION) {
				int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
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

	private void btn_BackActionPerformed(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void btn_HelpActionPerformed(ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.currentStudy);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
