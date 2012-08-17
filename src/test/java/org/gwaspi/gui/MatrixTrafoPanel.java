/*
 * MatrixTrafoPanel.java
 *
 * Created on Nov 13, 2009, 2:08:31 PM
 */
package org.gwaspi.gui;

/**
 *
 * @author u56124
 */
public class MatrixTrafoPanel extends javax.swing.JPanel {

	/**
	 * Creates new form MatrixTrafoPanel
	 */
	public MatrixTrafoPanel() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        
        pnl_ParentMatrixDesc = new javax.swing.JPanel();
        scrl_ParentMatrixDesc = new javax.swing.JScrollPane();
        txtA_ParentMatrixDesc = new javax.swing.JTextArea();
        pnl_ButtonsContainer = new javax.swing.JPanel();
        pnl_ButtonsSpacer = new javax.swing.JPanel();
        pnl_Buttons = new javax.swing.JPanel();
        btn_1_1 = new javax.swing.JButton();
        btn_1_2 = new javax.swing.JButton();
        btn_2_1 = new javax.swing.JButton();
        pnl_TrafoMatrixDesc = new javax.swing.JPanel();
        lbl_NewMatrixName = new javax.swing.JLabel();
        txt_NewMatrixName = new javax.swing.JTextField();
        scroll_TrafoMatrixDescription = new javax.swing.JScrollPane();
        textArea_TrafoMatrixDescription = new javax.swing.JTextArea();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();
        
        pnl_ParentMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Parent Matrix: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        
        txtA_ParentMatrixDesc.setColumns(20);
        txtA_ParentMatrixDesc.setRows(5);
        txtA_ParentMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scrl_ParentMatrixDesc.setViewportView(txtA_ParentMatrixDesc);
        
        javax.swing.GroupLayout pnl_ParentMatrixDescLayout = new javax.swing.GroupLayout(pnl_ParentMatrixDesc);
        pnl_ParentMatrixDesc.setLayout(pnl_ParentMatrixDescLayout);
        pnl_ParentMatrixDescLayout.setHorizontalGroup(
                pnl_ParentMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
                .addContainerGap())
                );
        pnl_ParentMatrixDescLayout.setVerticalGroup(
                pnl_ParentMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
                .addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
                );
        
        pnl_ButtonsContainer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnl_ButtonsContainer.setPreferredSize(new java.awt.Dimension(240, 289));
        
        javax.swing.GroupLayout pnl_ButtonsSpacerLayout = new javax.swing.GroupLayout(pnl_ButtonsSpacer);
        pnl_ButtonsSpacer.setLayout(pnl_ButtonsSpacerLayout);
        pnl_ButtonsSpacerLayout.setHorizontalGroup(
                pnl_ButtonsSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 32, Short.MAX_VALUE)
                );
        pnl_ButtonsSpacerLayout.setVerticalGroup(
                pnl_ButtonsSpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 130, Short.MAX_VALUE)
                );
        
        pnl_Buttons.setBorder(null);
        
        btn_1_1.setText("jButton1");
        
        btn_1_2.setText("jButton1");
        
        btn_2_1.setText("jButton1");
        
        javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
        pnl_Buttons.setLayout(pnl_ButtonsLayout);
        pnl_ButtonsLayout.setHorizontalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(btn_2_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_1_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
                .addGap(166, 166, 166)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
                );
        
        
        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_1_1, btn_1_2});
        
        pnl_ButtonsLayout.setVerticalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_2_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        
        
        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btn_1_1, btn_1_2});
        
        
        javax.swing.GroupLayout pnl_ButtonsContainerLayout = new javax.swing.GroupLayout(pnl_ButtonsContainer);
        pnl_ButtonsContainer.setLayout(pnl_ButtonsContainerLayout);
        pnl_ButtonsContainerLayout.setHorizontalGroup(
                pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_ButtonsSpacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        pnl_ButtonsContainerLayout.setVerticalGroup(
                pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnl_ButtonsSpacer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        
        pnl_TrafoMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Transformed Matrix Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        
        lbl_NewMatrixName.setText("New Matrix Name:");
        
        txt_NewMatrixName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_NewMatrixNameActionPerformed(evt);
            }
        });
        
        textArea_TrafoMatrixDescription.setColumns(20);
        textArea_TrafoMatrixDescription.setLineWrap(true);
        textArea_TrafoMatrixDescription.setRows(5);
        textArea_TrafoMatrixDescription.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scroll_TrafoMatrixDescription.setViewportView(textArea_TrafoMatrixDescription);
        
        javax.swing.GroupLayout pnl_TrafoMatrixDescLayout = new javax.swing.GroupLayout(pnl_TrafoMatrixDesc);
        pnl_TrafoMatrixDesc.setLayout(pnl_TrafoMatrixDescLayout);
        pnl_TrafoMatrixDescLayout.setHorizontalGroup(
                pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
                .addComponent(lbl_NewMatrixName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE))
                .addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE))
                .addContainerGap())
                );
        pnl_TrafoMatrixDescLayout.setVerticalGroup(
                pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
                .addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_NewMatrixName)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 649, Short.MAX_VALUE)
                .addComponent(btn_Help)
                .addContainerGap())
                );
        
        
        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});
        
        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
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
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_ButtonsContainer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
                .addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_ButtonsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
                );
    }//GEN-END:initComponents

    private void txt_NewMatrixNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_NewMatrixNameActionPerformed
		// TODO add your handling code here:
}//GEN-LAST:event_txt_NewMatrixNameActionPerformed

    private void btn_HelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_HelpActionPerformed
		// TODO add your handling code here:
}//GEN-LAST:event_btn_HelpActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_1_1;
    private javax.swing.JButton btn_1_2;
    private javax.swing.JButton btn_2_1;
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_Help;
    private javax.swing.JLabel lbl_NewMatrixName;
    private javax.swing.JPanel pnl_Buttons;
    private javax.swing.JPanel pnl_ButtonsContainer;
    private javax.swing.JPanel pnl_ButtonsSpacer;
    private javax.swing.JPanel pnl_Footer;
    private javax.swing.JPanel pnl_ParentMatrixDesc;
    private javax.swing.JPanel pnl_TrafoMatrixDesc;
    private javax.swing.JScrollPane scrl_ParentMatrixDesc;
    private javax.swing.JScrollPane scroll_TrafoMatrixDescription;
    private javax.swing.JTextArea textArea_TrafoMatrixDescription;
    private javax.swing.JTextArea txtA_ParentMatrixDesc;
    private javax.swing.JTextField txt_NewMatrixName;
    // End of variables declaration//GEN-END:variables
}
