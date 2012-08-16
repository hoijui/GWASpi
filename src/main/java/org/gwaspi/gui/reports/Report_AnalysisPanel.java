package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.gwaspi.model.ReportsList;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Report_AnalysisPanel extends javax.swing.JPanel {

	private org.gwaspi.model.Matrix parentMatrix;
	private org.gwaspi.model.Operation currentOP;
	// Variables declaration - do not modify
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_DeleteOperation;
	private javax.swing.JButton btn_Help;
	private javax.swing.JPanel pnl_OperationDesc;
	private javax.swing.JPanel pnl_Report;
	private javax.swing.JScrollPane scrl_OpDesc;
	private javax.swing.JTextArea txtA_OpDesc;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public Report_AnalysisPanel(final int _studyId, final int _matrixId, final int _opId, String NRows) throws IOException {

		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);
		if (_opId != Integer.MIN_VALUE) {
			currentOP = new org.gwaspi.model.Operation(_opId);
		}

		pnl_OperationDesc = new javax.swing.JPanel();
		scrl_OpDesc = new javax.swing.JScrollPane();
		txtA_OpDesc = new javax.swing.JTextArea();
		btn_DeleteOperation = new javax.swing.JButton();
		pnl_Report = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		if (currentOP != null) {
			setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Reports.report + ": " + currentOP.getOperationFriendlyName() + ", OperationID: " + currentOP.getOperationId(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N
		} else {
			setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.allelicAssocTest, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N
		}

		//pnl_OperationDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.operation+": "+ tata, OperationID: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

		txtA_OpDesc.setColumns(20);
		txtA_OpDesc.setRows(5);
		txtA_OpDesc.setEditable(false);
		txtA_OpDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.All.description));
		txtA_OpDesc.setText(currentOP.getDescription());
		scrl_OpDesc.setViewportView(txtA_OpDesc);

		btn_DeleteOperation.setText(Text.Operation.deleteOperation);
		btn_DeleteOperation.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteOperation(evt);
			}
		});

		//<editor-fold defaultstate="collapsed" desc="LAYOUT OPERATION DESC">
		javax.swing.GroupLayout pnl_OperationDescLayout = new javax.swing.GroupLayout(pnl_OperationDesc);
		pnl_OperationDesc.setLayout(pnl_OperationDescLayout);
		pnl_OperationDescLayout.setHorizontalGroup(
				pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OperationDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_OpDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
				.addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_OperationDescLayout.setVerticalGroup(
				pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_OperationDescLayout.createSequentialGroup()
				.addComponent(scrl_OpDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap(6, Short.MAX_VALUE)));
		//</editor-fold>


		ReportsList rpList = new ReportsList(_opId, _matrixId);
		if (rpList.reportsListAL.size() == 3) {
			String reportFile = rpList.reportsListAL.get(2).getReportFileName();
			if (currentOP.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
				pnl_Report = new Report_AnalysisAllelicTestImpl(_studyId, reportFile, _opId, NRows);
			} else if (currentOP.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())) {
				pnl_Report = new Report_AnalysisGenotypicTestImpl(_studyId, reportFile, _opId, NRows);
			} else if (currentOP.getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
				pnl_Report = new Report_AnalysisTrendTestImpl(_studyId, reportFile, _opId, NRows);
			}
		}
		pnl_Report.setBorder(javax.swing.BorderFactory.createTitledBorder("Report"));



		btn_Back.setText("Back");

		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			}
		});



		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_OperationDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Report, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_OperationDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Report, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>


	}

	//<editor-fold defaultstate="collapsed" desc="METHODS">
	private void actionDeleteOperation(java.awt.event.ActionEvent evt) {
		//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
		if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(null, currentOP.getOperationId(), null)) {
			int option = JOptionPane.showConfirmDialog(this, Text.Operation.confirmDelete1);
			if (option == JOptionPane.YES_OPTION) {
				int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
				if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
					if (option == JOptionPane.YES_OPTION) {
						boolean deleteReport = false;
						if (deleteReportOption == JOptionPane.YES_OPTION) {
							deleteReport = true;
						}
						MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), currentOP.getOperationId(), deleteReport);
					}
				}
			}
		} else {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
		}


	}
	//</editor-fold>
}
