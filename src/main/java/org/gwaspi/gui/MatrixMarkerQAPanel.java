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
		if (opId != OperationKey.NULL_ID) {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null,
					Text.Operation.operationId + ": "
					+ ((currentOPKey == null) ? "<NONE>" : currentOPKey.getId()),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION,
					new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText((currentOP == null) ? "<NONE>"
					: currentOP.getDescription());
		} else {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null,
					Text.Matrix.matrix + ": " + parentMatrixMetadata.getFriendlyName(),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION,
					new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(parentMatrixMetadata.getDescription());
		}
		scrl_MatrixDesc.setViewportView(txtA_Description);

		btn_DeleteOperation.setAction(new DeleteOperationAction(currentOPKey, this));

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

		Action backAction = new BackAction(new DataSetKey(parentMatrixKey));
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
