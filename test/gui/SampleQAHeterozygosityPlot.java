/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SampleQAHeterozygosityPlot.java
 *
 * Created on Feb 18, 2011, 11:38:30 AM
 */

package gui;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleQAHeterozygosityPlot extends javax.swing.JFrame {

    /** Creates new form SampleQAHeterozygosityPlot */
    public SampleQAHeterozygosityPlot() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        
        pnl_Footer = new javax.swing.JPanel();
        pnl_FooterGroup0 = new javax.swing.JPanel();
        lbl_thresholds = new javax.swing.JLabel();
        lbl_hetzy = new javax.swing.JLabel();
        txt_hetzy = new javax.swing.JTextField();
        btn_redraw = new javax.swing.JButton();
        lbl_missing = new javax.swing.JLabel();
        txt_missing = new javax.swing.JTextField();
        pnl_FooterGroup1 = new javax.swing.JPanel();
        btn_Reset = new javax.swing.JToggleButton();
        btn_Save = new javax.swing.JToggleButton();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        lbl_thresholds.setText("Thresholds");
        
        lbl_hetzy.setText("Heterozygosity");
        
        txt_hetzy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_hetzyActionPerformed(evt);
            }
        });
        
        btn_redraw.setText("Redraw");
        
        lbl_missing.setText("Missingness");
        
        txt_missing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_missingActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout pnl_FooterGroup0Layout = new javax.swing.GroupLayout(pnl_FooterGroup0);
        pnl_FooterGroup0.setLayout(pnl_FooterGroup0Layout);
        pnl_FooterGroup0Layout.setHorizontalGroup(
                pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
                .addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_hetzy)
                .addComponent(lbl_missing))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txt_hetzy, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnl_FooterGroup0Layout.createSequentialGroup()
                .addComponent(txt_missing, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(btn_redraw, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addComponent(lbl_thresholds))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        pnl_FooterGroup0Layout.setVerticalGroup(
                pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_thresholds)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_hetzy)
                .addComponent(txt_hetzy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(btn_redraw, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterGroup0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_missing)
                .addComponent(txt_missing))))
                );
        
        btn_Reset.setText("Save");
        btn_Reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ResetActionPerformed(evt);
            }
        });
        
        btn_Save.setText("Reset");
        
        javax.swing.GroupLayout pnl_FooterGroup1Layout = new javax.swing.GroupLayout(pnl_FooterGroup1);
        pnl_FooterGroup1.setLayout(pnl_FooterGroup1Layout);
        pnl_FooterGroup1Layout.setHorizontalGroup(
                pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterGroup1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_Reset, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        
        
        pnl_FooterGroup1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Reset, btn_Save});
        
        pnl_FooterGroup1Layout.setVerticalGroup(
                pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterGroup1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_Reset)
                .addComponent(btn_Save))
                );
        
        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addComponent(pnl_FooterGroup0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 174, Short.MAX_VALUE)
                .addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_FooterGroup0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(pnl_FooterGroup1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(326, Short.MAX_VALUE)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
        
        pack();
    }//GEN-END:initComponents

    private void txt_hetzyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_hetzyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_hetzyActionPerformed

    private void btn_ResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ResetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_ResetActionPerformed

    private void txt_missingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_missingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_missingActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SampleQAHeterozygosityPlot().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btn_Reset;
    private javax.swing.JToggleButton btn_Save;
    private javax.swing.JButton btn_redraw;
    private javax.swing.JLabel lbl_hetzy;
    private javax.swing.JLabel lbl_missing;
    private javax.swing.JLabel lbl_thresholds;
    private javax.swing.JPanel pnl_Footer;
    private javax.swing.JPanel pnl_FooterGroup0;
    private javax.swing.JPanel pnl_FooterGroup1;
    private javax.swing.JTextField txt_hetzy;
    private javax.swing.JTextField txt_missing;
    // End of variables declaration//GEN-END:variables

}
