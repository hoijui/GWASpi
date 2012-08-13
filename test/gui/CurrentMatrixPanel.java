/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * CurrentMatrixPanel.java
 *
 * Created on Nov 12, 2009, 3:53:16 PM
 */

package gui;

/**
 *
 * @author fernando
 */
public class CurrentMatrixPanel extends javax.swing.JPanel {

    /** Creates new form CurrentMatrixPanel */
    public CurrentMatrixPanel() {
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
        btn_DeleteMatrix = new javax.swing.JButton();
        btn_SaveDesc = new javax.swing.JButton();
        scrl_MatrixOperations = new javax.swing.JScrollPane();
        tbl_MatrixOperations = new javax.swing.JTable();
        btn_DeleteOperation = new javax.swing.JButton();
        pnl_NewOperation = new javax.swing.JPanel();
        pnl_Buttons = new javax.swing.JPanel();
        btn_Operation1_1 = new javax.swing.JButton();
        btn_Operation1_2 = new javax.swing.JButton();
        btn_Operation1_5 = new javax.swing.JButton();
        btn_Operation1_4 = new javax.swing.JButton();
        btn_Operation1_6 = new javax.swing.JButton();
        btn_Operation1_3 = new javax.swing.JButton();
        pnl_Spacer = new javax.swing.JPanel();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();
        
        setMinimumSize(new java.awt.Dimension(649, 300));
        setPreferredSize(new java.awt.Dimension(800, 600));
        
        pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Matrix: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        
        txtA_MatrixDesc.setColumns(20);
        txtA_MatrixDesc.setRows(5);
        txtA_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scrl_MatrixDesc.setViewportView(txtA_MatrixDesc);
        
        btn_DeleteMatrix.setText("Delete Matrix");
        
        btn_SaveDesc.setText("Save Description");
        
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
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 933, Short.MAX_VALUE)
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 933, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
                .addComponent(btn_DeleteMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 609, Short.MAX_VALUE)
                .addComponent(btn_SaveDesc))
                .addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );
        
        
        pnl_MatrixDescLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_DeleteMatrix, btn_DeleteOperation, btn_SaveDesc});
        
        pnl_MatrixDescLayout.setVerticalGroup(
                pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_SaveDesc)
                .addComponent(btn_DeleteMatrix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_DeleteOperation)
                .addContainerGap())
                );
        
        pnl_NewOperation.setBorder(javax.swing.BorderFactory.createTitledBorder("New Operation"));
        
        btn_Operation1_1.setText("Analyse Data");
        
        btn_Operation1_2.setText("Extract Data");
        
        btn_Operation1_5.setText("Merge Matrix");
        btn_Operation1_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Operation1_5ActionPerformed(evt);
            }
        });
        
        btn_Operation1_4.setText("Export Matrix");
        
        btn_Operation1_6.setText("Flip Strand");
        btn_Operation1_6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Operation1_6ActionPerformed(evt);
            }
        });
        
        btn_Operation1_3.setText("Translate Matrix");
        btn_Operation1_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_Operation1_3ActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
        pnl_Buttons.setLayout(pnl_ButtonsLayout);
        pnl_ButtonsLayout.setHorizontalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_Operation1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_Operation1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(btn_Operation1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(btn_Operation1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(btn_Operation1_6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_Operation1_4, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        
        
        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4, btn_Operation1_6});
        
        pnl_ButtonsLayout.setVerticalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addComponent(btn_Operation1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_Operation1_6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addComponent(btn_Operation1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_Operation1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(btn_Operation1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_Operation1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
                );
        
        
        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4, btn_Operation1_6});
        
        
        javax.swing.GroupLayout pnl_SpacerLayout = new javax.swing.GroupLayout(pnl_Spacer);
        pnl_Spacer.setLayout(pnl_SpacerLayout);
        pnl_SpacerLayout.setHorizontalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 33, Short.MAX_VALUE)
                );
        pnl_SpacerLayout.setVerticalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 151, Short.MAX_VALUE)
                );
        
        javax.swing.GroupLayout pnl_NewOperationLayout = new javax.swing.GroupLayout(pnl_NewOperation);
        pnl_NewOperation.setLayout(pnl_NewOperationLayout);
        pnl_NewOperationLayout.setHorizontalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(224, 224, 224))
                );
        pnl_NewOperationLayout.setVerticalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addGroup(pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(pnl_NewOperationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        
        btn_Back.setText("Back");
        
        btn_Help.setText("Help");
        
        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 773, Short.MAX_VALUE)
                .addComponent(btn_Help))
                );
        
        
        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});
        
        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_Back)
                .addComponent(btn_Help)))
                );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
    }//GEN-END:initComponents

    private void btn_Operation1_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Operation1_3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_Operation1_3ActionPerformed

    private void btn_Operation1_5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Operation1_5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_Operation1_5ActionPerformed

    private void btn_Operation1_6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_Operation1_6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_Operation1_6ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_DeleteMatrix;
    private javax.swing.JButton btn_DeleteOperation;
    private javax.swing.JButton btn_Help;
    private javax.swing.JButton btn_Operation1_1;
    private javax.swing.JButton btn_Operation1_2;
    private javax.swing.JButton btn_Operation1_3;
    private javax.swing.JButton btn_Operation1_4;
    private javax.swing.JButton btn_Operation1_5;
    private javax.swing.JButton btn_Operation1_6;
    private javax.swing.JButton btn_SaveDesc;
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
