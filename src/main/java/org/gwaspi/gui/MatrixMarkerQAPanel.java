package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
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
import org.gwaspi.model.Matrix;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMarkerQAPanel extends JPanel {

	private final static Logger log
			= LoggerFactory.getLogger(MatrixMarkerQAPanel.class);

	// Variables declaration - do not modify
	private Matrix parentMatrix;
	private final Operation currentOP;
	private JButton btn_Back;
	private JButton btn_DeleteOperation;
	private JButton btn_Help;
	private JPanel pnl_Footer;
	private JPanel pnl_MatrixDesc;
	private JScrollPane scrl_MatrixDesc;
	private JTextArea txtA_Description;
	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixMarkerQAPanel(int _matrixId, int _opId) throws IOException {

		parentMatrix = new Matrix(_matrixId);
		if (_opId != Integer.MIN_VALUE) {
			currentOP = new Operation(_opId);
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

		setBorder(BorderFactory.createTitledBorder(null, Text.Operation.operation + ": " + currentOP.getOperationFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		if (_opId != Integer.MIN_VALUE) {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.operationId + ": " + currentOP.getOperationId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(currentOP.getDescription().toString());
		} else {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrix + ": " + parentMatrix.getMatrixMetadata().getMatrixFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(parentMatrix.getMatrixMetadata().getDescription().toString());
		}
		scrl_MatrixDesc.setViewportView(txtA_Description);

		btn_DeleteOperation.setAction(new DeleteOperationAction(currentOP, this, parentMatrix));

		//<editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESC">
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

		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
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

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
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

		private Operation currentOP;
		private Component dialogParent;
		private Matrix parentMatrix;

		DeleteOperationAction(Operation currentOP, Component dialogParent, Matrix parentMatrix) {

			this.currentOP = currentOP;
			this.dialogParent = dialogParent;
			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int opId = currentOP.getOperationId();
				// TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
				if (SwingWorkerItemList.permitsDeletion(null, null, opId)) {
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
							if (currentOP.getOperationId() == opId) {
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

		private Matrix parentMatrix;

		BackAction(Matrix parentMatrix) {

			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix.getMatrixId()));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
}
