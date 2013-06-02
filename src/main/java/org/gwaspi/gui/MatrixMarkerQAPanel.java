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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixMarkerQAPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixMarkerQAPanel.class);

	// Variables declaration - do not modify
	private final MatrixKey parentMatrix;
	private final OperationMetadata currentOP;
	private final JButton btn_Back;
	private final JButton btn_DeleteOperation;
	private final JButton btn_Help;
	private final JPanel pnl_Footer;
	private final JPanel pnl_MatrixDesc;
	private final JScrollPane scrl_MatrixDesc;
	private final JTextArea txtA_Description;
	private final GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	// End of variables declaration

	public MatrixMarkerQAPanel(MatrixKey parentMatrixKey, int _opId) throws IOException {

		parentMatrix = parentMatrixKey;
		MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrix.getMatrixId());

		if (_opId != Integer.MIN_VALUE) {
			currentOP = OperationsList.getById(_opId);
		} else {
			currentOP = null;
		}

		pnl_MatrixDesc = new JPanel();
		scrl_MatrixDesc = new JScrollPane();
		txtA_Description = new JTextArea();

		btn_DeleteOperation = new JButton();
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null,
				Text.Operation.operation + ": "
				+ ((currentOP == null) ? "<NONE>" : currentOP.getFriendlyName()),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("FreeSans", 1, 18))); // NOI18N

		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(BorderFactory.createTitledBorder(null,
				Text.All.description,
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("DejaVu Sans", 1, 13))); // NOI18N
		if (_opId != Integer.MIN_VALUE) {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null,
					Text.Operation.operationId + ": "
					+ ((currentOP == null) ? "<NONE>" : currentOP.getId()),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION,
					new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText((currentOP == null) ? "<NONE>"
					: currentOP.getDescription());
		} else {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null,
					Text.Matrix.matrix + ": " + parentMatrixMetadata.getMatrixFriendlyName(),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION,
					new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(parentMatrixMetadata.getDescription().toString());
		}
		scrl_MatrixDesc.setViewportView(txtA_Description);

		btn_DeleteOperation.setAction(new DeleteOperationAction(currentOP, this, parentMatrix));

		//<editor-fold defaultstate="expanded" desc="LAYOUT MATRIX DESC">
		GroupLayout pnl_MatrixDescLayout = new GroupLayout(pnl_MatrixDesc);
		pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
		pnl_MatrixDescLayout.setHorizontalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixDesc, GroupLayout.DEFAULT_SIZE, 810, Short.MAX_VALUE)
				.addComponent(btn_DeleteOperation, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_MatrixDescLayout.setVerticalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_MatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		//</editor-fold>

		Action backAction = new BackAction(parentMatrix);
		backAction.setEnabled(currentOP != null);
		btn_Back.setAction(backAction);
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.markerQAreport));

		//<editor-fold defaultstate="expanded" desc="LAYOUT FOOTER">
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 664, Short.MAX_VALUE)
				.addComponent(btn_Help)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_MatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>
	}

	private static class DeleteOperationAction extends AbstractAction {

		private final OperationMetadata currentOP;
		private final Component dialogParent;
		private final MatrixKey parentMatrix;

		DeleteOperationAction(OperationMetadata currentOP, Component dialogParent, MatrixKey parentMatrix) {

			this.currentOP = currentOP;
			this.dialogParent = dialogParent;
			this.parentMatrix = parentMatrix;
			setEnabled(currentOP != null);
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int opId = currentOP.getId();
				// TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
				if (SwingWorkerItemList.permitsDeletionOfOperationId(opId)) {
					int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
					if (option == JOptionPane.YES_OPTION) {
						int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
						if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
							if (option == JOptionPane.YES_OPTION) {
								boolean deleteReport = false;
								if (deleteReportOption == JOptionPane.YES_OPTION) {
									deleteReport = true;
								}
								MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), opId, deleteReport);

								//OperationManager.deleteOperationAndChildren(parentMatrix.getStudyId(), opId, deleteReport);
							}
							if (currentOP.getId() == opId) {
								GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
							}
							GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
						}
					}
				} else {
					Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private final MatrixKey parentMatrix;

		BackAction(MatrixKey parentMatrix) {

			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
