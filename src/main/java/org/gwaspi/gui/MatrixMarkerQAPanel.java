package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMarkerQAPanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private org.gwaspi.model.Matrix parentMatrix;
	private org.gwaspi.model.Operation currentOP;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_DeleteOperation;
	private javax.swing.JButton btn_Help;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JPanel pnl_MatrixDesc;
	private javax.swing.JScrollPane scrl_MatrixDesc;
	private javax.swing.JTextArea txtA_Description;
	public GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixMarkerQAPanel(int _matrixId, int _opId) throws IOException {

		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);
		if (_opId != Integer.MIN_VALUE) {
			currentOP = new org.gwaspi.model.Operation(_opId);
		}


		pnl_MatrixDesc = new javax.swing.JPanel();
		scrl_MatrixDesc = new javax.swing.JScrollPane();
		txtA_Description = new javax.swing.JTextArea();

		btn_DeleteOperation = new javax.swing.JButton();
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.operation + ": " + currentOP.getOperationFriendlyName(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N


		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.All.description, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		if (_opId != Integer.MIN_VALUE) {
			pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.operationId + ": " + currentOP.getOperationId(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(currentOP.getDescription().toString());
		} else {
			pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.matrix + ": " + parentMatrix.matrixMetadata.getMatrixFriendlyName(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(parentMatrix.matrixMetadata.getDescription().toString());
		}
		scrl_MatrixDesc.setViewportView(txtA_Description);

		btn_DeleteOperation.setText(Text.Operation.deleteOperation);
		btn_DeleteOperation.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteOperation(evt);
			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESC">
		javax.swing.GroupLayout pnl_MatrixDescLayout = new javax.swing.GroupLayout(pnl_MatrixDesc);
		pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
		pnl_MatrixDescLayout.setHorizontalGroup(
				pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 810, Short.MAX_VALUE)
				.addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_MatrixDescLayout.setVerticalGroup(
				pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		//</editor-fold>


		btn_Back.setText("Back");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionGoBack(evt);
				} catch (IOException ex) {
					Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp(evt);
			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 664, Short.MAX_VALUE)
				.addComponent(btn_Help)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>
	}

	private void actionDeleteOperation(java.awt.event.ActionEvent evt) {
		try {
			int opId = currentOP.getOperationId();
			//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
			if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(null, null, opId)) {
				int option = JOptionPane.showConfirmDialog(this, Text.Operation.confirmDelete1);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
					if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
						if (option == JOptionPane.YES_OPTION) {
							boolean deleteReport = false;
							if (deleteReportOption == JOptionPane.YES_OPTION) {
								deleteReport = true;
							}
							MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), opId, deleteReport);

							//netCDF.operations.OperationManager.deleteOperationAndChildren(parentMatrix.getStudyId(), opId, deleteReport);
						}
						if (currentOP.getOperationId() == opId) {
							org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
						}
						org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
					}
				}
			} else {
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
			}
		} catch (IOException ex) {
			Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private void actionGoBack(java.awt.event.ActionEvent evt) throws IOException {
		if (currentOP != null) {
			org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		}
	}

	private void actionHelp(java.awt.event.ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.markerQAreport);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
