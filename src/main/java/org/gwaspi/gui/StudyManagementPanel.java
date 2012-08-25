package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyManagementPanel extends javax.swing.JPanel {

	// Variables declaration
	private org.gwaspi.model.Study study;
	private Map<Integer, Object> treeChildrenLHM = new LinkedHashMap<Integer, Object>();
	private javax.swing.JButton btn_AddStudy;
	private javax.swing.JButton btn_DeleteStudy;
	private javax.swing.JLabel lbl_Desc;
	private javax.swing.JLabel lbl_NewStudyName;
	private javax.swing.JPanel pnl_StudiesTable;
	private javax.swing.JPanel pnl_StudyDesc;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JScrollPane scrl_Desc;
	private javax.swing.JScrollPane scrl_StudiesTable;
	private javax.swing.JTable tbl_StudiesTable;
	private javax.swing.JTextArea txtA_Desc;
	private javax.swing.JTextField txtF_NewStudyName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public StudyManagementPanel() throws IOException {

		DefaultMutableTreeNode studyManagementNode = (DefaultMutableTreeNode) org.gwaspi.gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent();
		treeChildrenLHM = org.gwaspi.gui.utils.NodeToPathCorrespondence.buildNodeToPathCorrespondence(studyManagementNode, false);

		pnl_StudyDesc = new javax.swing.JPanel();
		lbl_NewStudyName = new javax.swing.JLabel();
		txtF_NewStudyName = new javax.swing.JTextField();
		txtF_NewStudyName.setDocument(new org.gwaspi.model.JTextFieldLimited(64));
		lbl_Desc = new javax.swing.JLabel();
		scrl_Desc = new javax.swing.JScrollPane();
		txtA_Desc = new javax.swing.JTextArea();
		btn_DeleteStudy = new javax.swing.JButton();
		btn_AddStudy = new javax.swing.JButton();
		pnl_StudiesTable = new javax.swing.JPanel();
		pnl_Footer = new javax.swing.JPanel();
		btn_Help = new javax.swing.JButton();
		btn_Back = new javax.swing.JButton();
		scrl_StudiesTable = new javax.swing.JScrollPane();
		tbl_StudiesTable = new javax.swing.JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
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
		tbl_StudiesTable.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Study.studies, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_StudyDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Study.createNewStudy, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		lbl_NewStudyName.setText(Text.Study.studyName);
		txtF_NewStudyName.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(63));

		lbl_Desc.setText(Text.All.description);
		txtA_Desc.setColumns(20);
		txtA_Desc.setRows(5);
		txtA_Desc.setText(Text.All.optional);
		txtA_Desc.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_Desc.getText().equals(Text.All.optional)) {
							txtA_Desc.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_Desc.select(0, 0);
					}
				});
			}
		});

		scrl_Desc.setViewportView(txtA_Desc);
		btn_AddStudy.setText(Text.Study.addStudy);
		btn_AddStudy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					btn_AddStudyActionPerformed(evt);
				} catch (IOException ex) {
					Logger.getLogger(StudyManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		pnl_StudiesTable.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Study.availableStudies, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		tbl_StudiesTable.setModel(new javax.swing.table.DefaultTableModel(
				org.gwaspi.model.StudyList.getStudyTable(),
				new String[]{
					Text.Study.studyID, Text.Study.studyName, Text.All.description, Text.All.createDate
				}));
		scrl_StudiesTable.setViewportView(tbl_StudiesTable);
		btn_DeleteStudy.setText(Text.Study.deleteStudy);
		btn_DeleteStudy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteStudy(evt);
			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT STUDY TABLE">
		javax.swing.GroupLayout pnl_StudiesTableLayout = new javax.swing.GroupLayout(pnl_StudiesTable);
		pnl_StudiesTable.setLayout(pnl_StudiesTableLayout);
		pnl_StudiesTableLayout.setHorizontalGroup(
				pnl_StudiesTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudiesTableLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudiesTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_StudiesTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addComponent(btn_DeleteStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_StudiesTableLayout.setVerticalGroup(
				pnl_StudiesTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudiesTableLayout.createSequentialGroup()
				.addComponent(scrl_StudiesTable, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(btn_DeleteStudy)
				.addContainerGap()));
		//</editor-fold>


		// <editor-fold defaultstate="collapsed" desc="LAYOUT DESCRIPTION">
		javax.swing.GroupLayout pnl_StudyDescLayout = new javax.swing.GroupLayout(pnl_StudyDesc);
		pnl_StudyDesc.setLayout(pnl_StudyDescLayout);
		pnl_StudyDescLayout.setHorizontalGroup(
				pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_NewStudyName)
				.addComponent(lbl_Desc))
				.addGap(18, 18, 18)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Desc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
				.addComponent(txtF_NewStudyName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
				.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap(605, Short.MAX_VALUE)
				.addComponent(btn_AddStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(14, 14, 14)));
		pnl_StudyDescLayout.setVerticalGroup(
				pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewStudyName)
				.addComponent(txtF_NewStudyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Desc)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(scrl_Desc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_AddStudy)));
		// </editor-fold>

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

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnl_StudiesTable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_StudyDesc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_StudiesTable, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_StudyDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

	}

	private void btn_AddStudyActionPerformed(java.awt.event.ActionEvent evt) throws IOException {

		String study_name = txtF_NewStudyName.getText();
		if (!study_name.isEmpty()) {
			lbl_NewStudyName.setForeground(Color.black);
			String study_description = txtA_Desc.getText();
			if (txtA_Desc.getText().equals(Text.All.optional)) {
				study_description = "";
			}

			org.gwaspi.database.StudyGenerator.insertNewStudy(study_name, study_description);
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
			org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
			//model.GWASpiExplorer.insertLatestStudyNode();
		} else {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.Study.warnNoStudyName);
			lbl_NewStudyName.setForeground(Color.red);
		}
	}

	private void actionDeleteStudy(ActionEvent evt) {
		if (tbl_StudiesTable.getSelectedRow() != -1) {

			int[] selectedStudyRows = tbl_StudiesTable.getSelectedRows();
			int[] selectedStudyIds = new int[selectedStudyRows.length];
			for (int i = 0; i < selectedStudyRows.length; i++) {
				selectedStudyIds[i] = (Integer) tbl_StudiesTable.getModel().getValueAt(selectedStudyRows[i], 0);
			}

			int option = JOptionPane.showConfirmDialog(this, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
			if (option == JOptionPane.YES_OPTION) {
				int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
				for (int i = 0; i < selectedStudyIds.length; i++) {
					int studyId = selectedStudyIds[i];
					//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
					if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(studyId, null, null)) {
						if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

							boolean deleteReport = false;
							if (deleteReportOption == JOptionPane.YES_OPTION) {
								deleteReport = true;
							}
							MultiOperations.deleteStudy(studyId, deleteReport);

//							try {
//								org.gwaspi.database.StudyGenerator.deleteStudy(studyId, deleteReport);
//								try {
//									org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
//									org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
//									org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
//								} catch (IOException ex) {
//									Logger.getLogger(StudyManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
//								}
//
//							} catch (IOException ex) {
//								Logger.getLogger(StudyManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
//							}
						}
					} else {
						org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
					}

				}
				try {
					org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
				} catch (IOException ex) {
					Logger.getLogger(StudyManagementPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private void btn_BackActionPerformed(ActionEvent evt) {
		org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new IntroPanel();
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void btn_HelpActionPerformed(ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.createStudy);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
