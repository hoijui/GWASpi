/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MatrixAnalysePanel.java
 *
 * Created on Nov 13, 2009, 4:53:04 PM
 */

package gui;

/**
 *
 * @author u56124
 */
public class MatrixAnalysePanel extends javax.swing.JPanel {

    /** Creates new form MatrixAnalysePanel */
    public MatrixAnalysePanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        
        pnl_MatrixDesc = new javax.swing.JPanel();
        scrl_MatrixDesc = new javax.swing.JScrollPane();
        txtA_MatrixDesc = new javax.swing.JTextArea();
        scrl_MatrixOperations = new javax.swing.JScrollPane();
        tbl_MatrixOperations = new javax.swing.JTable();
        btn_DeleteOperation = new javax.swing.JButton();
        pnl_NewOperation = new javax.swing.JPanel();
        pnl_Spacer = new javax.swing.JPanel();
        pnl_Buttons = new javax.swing.JPanel();
        btn_1_1 = new javax.swing.JButton();
        btn_1_2 = new javax.swing.JButton();
        btn_1_3 = new javax.swing.JButton();
        btn_1_4 = new javax.swing.JButton();
        btn_1_5 = new javax.swing.JButton();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();
        
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Analysis", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 24))); // NOI18N
        setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        
        pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Matrix: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        
        txtA_MatrixDesc.setColumns(20);
        txtA_MatrixDesc.setRows(5);
        txtA_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scrl_MatrixDesc.setViewportView(txtA_MatrixDesc);
        
        tbl_MatrixOperations.setModel(new javax.swing.table.DefaultTableModel(
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
        scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
        
        btn_DeleteOperation.setText("Delete Operation");
        
        javax.swing.GroupLayout pnl_MatrixDescLayout = new javax.swing.GroupLayout(pnl_MatrixDesc);
        pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
        pnl_MatrixDescLayout.setHorizontalGroup(
                pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MatrixDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );
        pnl_MatrixDescLayout.setVerticalGroup(
                pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_DeleteOperation)
                .addContainerGap())
                );
        
        pnl_NewOperation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New Operation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        pnl_NewOperation.setMaximumSize(new java.awt.Dimension(32767, 96));
        pnl_NewOperation.setPreferredSize(new java.awt.Dimension(926, 96));
        
        javax.swing.GroupLayout pnl_SpacerLayout = new javax.swing.GroupLayout(pnl_Spacer);
        pnl_Spacer.setLayout(pnl_SpacerLayout);
        pnl_SpacerLayout.setHorizontalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 46, Short.MAX_VALUE)
                );
        pnl_SpacerLayout.setVerticalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 124, Short.MAX_VALUE)
                );
        
        btn_1_1.setText("btn_1_1");
        
        btn_1_2.setText("btn_1_2");
        
        btn_1_3.setText("btn_1_3");
        
        btn_1_4.setText("btn_1_4");
        
        btn_1_5.setText("btn_1_5");
        
        javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
        pnl_Buttons.setLayout(pnl_ButtonsLayout);
        pnl_ButtonsLayout.setHorizontalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btn_1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(108, 108, 108))
                );
        
        
        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_1_1, btn_1_2, btn_1_3, btn_1_4});
        
        pnl_ButtonsLayout.setVerticalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        
        javax.swing.GroupLayout pnl_NewOperationLayout = new javax.swing.GroupLayout(pnl_NewOperation);
        pnl_NewOperation.setLayout(pnl_NewOperationLayout);
        pnl_NewOperationLayout.setHorizontalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        pnl_NewOperationLayout.setVerticalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addGroup(pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        
        btn_Back.setText("Back");
        
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
                .addContainerGap()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 565, Short.MAX_VALUE)
                .addComponent(btn_Help)
                .addContainerGap())
                );
        
        
        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});
        
        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addContainerGap(53, Short.MAX_VALUE)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_Back)
                .addComponent(btn_Help)))
                );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
    }//GEN-END:initComponents

    private void btn_HelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_HelpActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_btn_HelpActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_1_1;
    private javax.swing.JButton btn_1_2;
    private javax.swing.JButton btn_1_3;
    private javax.swing.JButton btn_1_4;
    private javax.swing.JButton btn_1_5;
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_DeleteOperation;
    private javax.swing.JButton btn_Help;
    private javax.swing.JPanel pnl_Buttons;
    private javax.swing.JPanel pnl_Footer;
    private javax.swing.JPanel pnl_MatrixDesc;
    private javax.swing.JPanel pnl_NewOperation;
    private javax.swing.JPanel pnl_Spacer;
    private javax.swing.JScrollPane scrl_MatrixDesc;
    private javax.swing.JScrollPane scrl_MatrixOperations;
    private javax.swing.JTable tbl_MatrixOperations;
    private javax.swing.JTextArea txtA_MatrixDesc;
    // End of variables declaration//GEN-END:variables

}
