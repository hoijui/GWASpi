package org.gwaspi.gui;

/**
 *
 * @author u56124
 */
public class Report_AssociationPanel extends javax.swing.JPanel {

	/**
	 * Creates new form MatrixAnalysePanel
	 */
	public Report_AssociationPanel() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        
        pnl_OperationDesc = new javax.swing.JPanel();
        scrl_OpDesc = new javax.swing.JScrollPane();
        txtA_OpDesc = new javax.swing.JTextArea();
        btn_DeleteOperation = new javax.swing.JButton();
        pnl_Report = new javax.swing.JPanel();
        pnl_Summary = new javax.swing.JPanel();
        rdio_ShowNRows = new javax.swing.JRadioButton();
        txt_NRows = new javax.swing.JTextField();
        lbl_suffix1 = new javax.swing.JLabel();
        btn_Get = new javax.swing.JButton();
        scrl_ReportTable = new javax.swing.JScrollPane();
        tbl_ReportTable = new javax.swing.JTable();
        
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Allelic Association Test", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 24))); // NOI18N
        setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        
        pnl_OperationDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Operation:  tata, OperationID: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        
        txtA_OpDesc.setColumns(20);
        txtA_OpDesc.setRows(5);
        txtA_OpDesc.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scrl_OpDesc.setViewportView(txtA_OpDesc);
        
        btn_DeleteOperation.setText("Delete Operation");
        
        javax.swing.GroupLayout pnl_OperationDescLayout = new javax.swing.GroupLayout(pnl_OperationDesc);
        pnl_OperationDesc.setLayout(pnl_OperationDescLayout);
        pnl_OperationDescLayout.setHorizontalGroup(
                pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_OperationDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scrl_OpDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
                .addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );
        pnl_OperationDescLayout.setVerticalGroup(
                pnl_OperationDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_OperationDescLayout.createSequentialGroup()
                .addComponent(scrl_OpDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_DeleteOperation))
                );
        
        pnl_Report.setBorder(javax.swing.BorderFactory.createTitledBorder("Report"));
        
        pnl_Summary.setBorder(javax.swing.BorderFactory.createTitledBorder("Summary"));
        
        rdio_ShowNRows.setText("Show");
        
        txt_NRows.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txt_NRows.setText("100");
        
        lbl_suffix1.setText("most significant p-Values.");
        
        btn_Get.setText("Get");
        
        javax.swing.GroupLayout pnl_SummaryLayout = new javax.swing.GroupLayout(pnl_Summary);
        pnl_Summary.setLayout(pnl_SummaryLayout);
        pnl_SummaryLayout.setHorizontalGroup(
                pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdio_ShowNRows)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_suffix1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 374, Short.MAX_VALUE)
                .addComponent(btn_Get, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
        pnl_SummaryLayout.setVerticalGroup(
                pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SummaryLayout.createSequentialGroup()
                .addGroup(pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(rdio_ShowNRows)
                .addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbl_suffix1)
                .addComponent(btn_Get))
                .addContainerGap())
                );
        
        tbl_ReportTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null}
        },
                new String [] {
            "Title 1", "Title 2", "Title 3", "Title 4"
        }
        ));
        scrl_ReportTable.setViewportView(tbl_ReportTable);
        
        javax.swing.GroupLayout pnl_ReportLayout = new javax.swing.GroupLayout(pnl_Report);
        pnl_Report.setLayout(pnl_ReportLayout);
        pnl_ReportLayout.setHorizontalGroup(
                pnl_ReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ReportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(scrl_ReportTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
                .addComponent(pnl_Summary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        pnl_ReportLayout.setVerticalGroup(
                pnl_ReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ReportLayout.createSequentialGroup()
                .addComponent(pnl_Summary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrl_ReportTable, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
                );
        
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
                .addContainerGap())))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_OperationDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Report, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
    }//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_DeleteOperation;
    private javax.swing.JButton btn_Get;
    private javax.swing.JLabel lbl_suffix1;
    private javax.swing.JPanel pnl_OperationDesc;
    private javax.swing.JPanel pnl_Report;
    private javax.swing.JPanel pnl_Summary;
    private javax.swing.JRadioButton rdio_ShowNRows;
    private javax.swing.JScrollPane scrl_OpDesc;
    private javax.swing.JScrollPane scrl_ReportTable;
    private javax.swing.JTable tbl_ReportTable;
    private javax.swing.JTextArea txtA_OpDesc;
    private javax.swing.JTextField txt_NRows;
    // End of variables declaration//GEN-END:variables
}
