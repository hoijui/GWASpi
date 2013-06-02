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

package org.gwaspi.gui.reports;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.Report;
import org.gwaspi.model.ReportsList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;

public class Report_AnalysisPanel extends JPanel {

	private final MatrixKey parentMatrix;
	private final OperationMetadata currentOP;
	// Variables declaration - do not modify
	private final JButton btn_Back;
	private final JButton btn_DeleteOperation;
	private final JButton btn_Help;
	private final JPanel pnl_OperationDesc;
	private final JPanel pnl_Report;
	private final JScrollPane scrl_OpDesc;
	private final JTextArea txtA_OpDesc;
	// End of variables declaration

	public Report_AnalysisPanel(final int _studyId, final int _matrixId, final int _opId, final Integer nRows) throws IOException {

		parentMatrix = new MatrixKey(_studyId, _matrixId);
		if (_opId != Integer.MIN_VALUE) {
			currentOP = OperationsList.getById(_opId);
		} else {
			currentOP = null;
		}

		pnl_OperationDesc = new JPanel();
		scrl_OpDesc = new JScrollPane();
		txtA_OpDesc = new JTextArea();
		btn_DeleteOperation = new JButton();
		btn_Back = new JButton();
		btn_Help = new JButton();

		if (currentOP != null) {
			setBorder(BorderFactory.createTitledBorder(null, Text.Reports.report + ": " + currentOP.getFriendlyName() + ", OperationID: " + currentOP.getId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N
		} else {
			setBorder(BorderFactory.createTitledBorder(null, Text.Operation.allelicAssocTest, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N
		}

		//pnl_OperationDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.operation+": "+ tata, OperationID: ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N

		txtA_OpDesc.setColumns(20);
		txtA_OpDesc.setRows(5);
		txtA_OpDesc.setEditable(false);
		txtA_OpDesc.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_OpDesc.setText((currentOP == null) ? "<NONE>"
					: currentOP.getDescription());
		scrl_OpDesc.setViewportView(txtA_OpDesc);

		btn_DeleteOperation.setAction(new DeleteOperationAction(parentMatrix, this, currentOP));

		//<editor-fold defaultstate="expanded" desc="LAYOUT OPERATION DESC">
		GroupLayout pnl_OperationDescLayout = new GroupLayout(pnl_OperationDesc);
		pnl_OperationDesc.setLayout(pnl_OperationDescLayout);
		pnl_OperationDescLayout.setHorizontalGroup(
				pnl_OperationDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OperationDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_OperationDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_OpDesc, GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
				.addComponent(btn_DeleteOperation, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_OperationDescLayout.setVerticalGroup(
				pnl_OperationDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OperationDescLayout.createSequentialGroup()
				.addComponent(scrl_OpDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap(6, Short.MAX_VALUE)));
		//</editor-fold>

		List<Report> reportsList = ReportsList.getReportsList(_opId, _matrixId);
		JPanel pnl_ReportTmp = null;
		if (reportsList.size() == 3) {
			String reportFile = reportsList.get(2).getFileName();
			if (currentOP.getOperationType().equals(OPType.ALLELICTEST)) {
				pnl_ReportTmp = new Report_AnalysisAllelicTestImpl(_studyId, reportFile, _opId, nRows);
			} else if (currentOP.getOperationType().equals(OPType.GENOTYPICTEST)) {
				pnl_ReportTmp = new Report_AnalysisGenotypicTestImpl(_studyId, reportFile, _opId, nRows);
			} else if (currentOP.getOperationType().equals(OPType.TRENDTEST)) {
				pnl_ReportTmp = new Report_AnalysisTrendTestImpl(_studyId, reportFile, _opId, nRows);
			}
		}
		if (pnl_ReportTmp == null) {
			pnl_ReportTmp = new JPanel();
		}
		pnl_Report = pnl_ReportTmp;
		pnl_Report.setBorder(BorderFactory.createTitledBorder("Report"));

		btn_Back.setAction(new BackAction());

		btn_Help.setAction(new BrowserHelpUrlAction(null)); // FIXME no help implemented yet

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_OperationDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Report, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()))));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_OperationDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Report, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>
	}

	//<editor-fold defaultstate="expanded" desc="METHODS">
	private static class DeleteOperationAction extends AbstractAction {

		private final MatrixKey parentMatrix;
		private final Component dialogParent;
		private final OperationMetadata currentOP;

		DeleteOperationAction(MatrixKey parentMatrix, Component dialogParent, OperationMetadata currentOP) {

			this.parentMatrix = parentMatrix;
			this.dialogParent = dialogParent;
			this.currentOP = currentOP;
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
			if (SwingWorkerItemList.permitsDeletionOfMatrixId(currentOP.getId())) { // XXX FIXME? should it be permitsDeletionOfOperationId
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if ((deleteReportOption != JOptionPane.CANCEL_OPTION)
							&& (option == JOptionPane.YES_OPTION))
					{
						final boolean deleteReport = (deleteReportOption == JOptionPane.YES_OPTION);
						MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), currentOP.getId(), deleteReport);
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
			throw new UnsupportedOperationException("Not yet implemented!");
		}
	}
	//</editor-fold>
}
