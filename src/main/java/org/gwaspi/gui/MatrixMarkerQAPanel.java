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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Deleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixMarkerQAPanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(MatrixMarkerQAPanel.class);

	private final OperationKey currentOPKey;

	public MatrixMarkerQAPanel(final MatrixKey parentMatrixKey, final int opId) throws IOException {

		MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);

		final OperationMetadata currentOP;
		if (opId != OperationKey.NULL_ID) {
			this.currentOPKey = new OperationKey(parentMatrixKey, opId);
			currentOP = OperationsList.getOperationMetadata(currentOPKey);
		} else {
			this.currentOPKey = null;
			currentOP = null;
		}

		final JPanel pnl_MatrixDesc = new JPanel();
		final JScrollPane scrl_MatrixDesc = new JScrollPane();
		final JTextArea txtA_Description = new JTextArea();

		final JButton btn_DeleteOperation = new JButton();
		final JPanel pnl_Footer = new JPanel();
		final JButton btn_Back = new JButton();
		final JButton btn_Help = new JButton();

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(
				Text.Operation.operation + ": "
				+ ((currentOP == null) ? "<NONE>" : currentOP.getFriendlyName()))); // NOI18N

		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(
				Text.All.description)); // NOI18N
		if (opId != OperationKey.NULL_ID) {
			pnl_MatrixDesc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(
					Text.Operation.operationId + ": "
					+ ((currentOPKey == null) ? "<NONE>" : currentOPKey.getId()))); // NOI18N
			txtA_Description.setText((currentOP == null) ? "<NONE>"
					: currentOP.getDescription());
		} else {
			pnl_MatrixDesc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(
					Text.Matrix.matrix + ": " + parentMatrixMetadata.getFriendlyName())); // NOI18N
			txtA_Description.setText(parentMatrixMetadata.getDescription());
		}
		scrl_MatrixDesc.setViewportView(txtA_Description);

		btn_DeleteOperation.setAction(new DeleteOperationAction(currentOPKey, this));

		//<editor-fold defaultstate="expanded" desc="LAYOUT MATRIX DESC">
		final int gapSpace = 5;
		pnl_MatrixDesc.setLayout(new BorderLayout(gapSpace, gapSpace));
		pnl_MatrixDesc.add(scrl_MatrixDesc, BorderLayout.CENTER);
		final JPanel pnl_MatrixDescFooter = new JPanel();
		pnl_MatrixDescFooter.setLayout(new BorderLayout(gapSpace, gapSpace));
		pnl_MatrixDescFooter.add(btn_DeleteOperation, BorderLayout.WEST);
		pnl_MatrixDesc.add(pnl_MatrixDescFooter, BorderLayout.SOUTH);
		//</editor-fold>

		Action backAction = new BackAction(new DataSetKey(parentMatrixKey));
		btn_Back.setAction(backAction);
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.markerQAreport));

		//<editor-fold defaultstate="expanded" desc="LAYOUT FOOTER">
		pnl_Footer.setLayout(new BorderLayout(gapSpace, gapSpace));
		pnl_Footer.add(btn_Back, BorderLayout.WEST);
		pnl_Footer.add(btn_Help, BorderLayout.EAST);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		setLayout(new BorderLayout(gapSpace, gapSpace));
		add(pnl_MatrixDesc, BorderLayout.NORTH);
		add(pnl_Footer, BorderLayout.SOUTH);
		//</editor-fold>
	}

	private static class DeleteOperationAction extends AbstractAction {

		private final OperationKey currentOPKey;
		private final Component dialogParent;

		DeleteOperationAction(OperationKey currentOPKey, Component dialogParent) {

			this.currentOPKey = currentOPKey;
			this.dialogParent = dialogParent;
			setEnabled(currentOPKey != null);
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			final int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
			if (option == JOptionPane.YES_OPTION) {
				final int deleteReportsOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
				if (deleteReportsOption != JOptionPane.CANCEL_OPTION) {
					final boolean deleteReports = (deleteReportsOption == JOptionPane.YES_OPTION);
					final Deleter operationDeleter = new Deleter(currentOPKey, deleteReports);
					// test if the deleted item is required for a queued worker
					if (MultiOperations.canBeDoneNow(operationDeleter)) {
						MultiOperations.queueTask(operationDeleter);
						// XXX OperationManager.deleteOperationAndChildren(parentMatrix.getStudyKey(), opId, deleteReport);
					} else {
						Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
					}
					try {
						GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
						GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
			}
		}
	}
}
