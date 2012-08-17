/*
 * TestJFrame2.java
 *
 * Created on Nov 11, 2009, 11:30:36 AM
 */
package org.gwaspi.gui;

/**
 *
 * @author fernando
 */
public class TestJFrame2 extends javax.swing.JFrame {

	/**
	 * Creates new form TestJFrame2
	 */
	public TestJFrame2() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        
        pnl_MatrixExtractPanel = new javax.swing.JPanel();
        pnl_NameAndDesc = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lbl_CurrentMatrix = new javax.swing.JLabel();
        lbl_NewMatrixName = new javax.swing.JLabel();
        txt_NewMatrixName = new javax.swing.JTextField();
        lbl_Description = new javax.swing.JLabel();
        scroll_Description = new javax.swing.JScrollPane();
        textArea_Description = new javax.swing.JTextArea();
        pnl_MarkerZone = new javax.swing.JPanel();
        lbl_MarkerZone = new javax.swing.JLabel();
        lbl_MarkersVariable = new javax.swing.JLabel();
        cmb_MarkersVariable = new javax.swing.JComboBox();
        button_Help3 = new javax.swing.JButton();
        button_Help1 = new javax.swing.JButton();
        lbl_MarkersCriteria = new javax.swing.JLabel();
        scroll_MarkersCriteria = new javax.swing.JScrollPane();
        txtArea_MarkersCriteria = new javax.swing.JTextArea();
        lbl_MarkersCriteriaFile = new javax.swing.JLabel();
        txt_MarkersCriteriaFile = new javax.swing.JTextField();
        button_MarkersCriteriaBrowse = new javax.swing.JButton();
        button_Help2 = new javax.swing.JButton();
        pnl_SampleZone = new javax.swing.JPanel();
        lbl_SampleZone = new javax.swing.JLabel();
        lbl_SamplesVariable = new javax.swing.JLabel();
        cmb_SamplesVariable = new javax.swing.JComboBox();
        lbl_SamplesCriteria = new javax.swing.JLabel();
        scroll_SamplesCriteria = new javax.swing.JScrollPane();
        txtArea_SamplesCriteria = new javax.swing.JTextArea();
        lbl_SamplesCriteriaFile = new javax.swing.JLabel();
        txt_SamplesCriteriaFile = new javax.swing.JTextField();
        button_SamplesCriteriaBrowse = new javax.swing.JButton();
        button_Help6 = new javax.swing.JButton();
        button_Help5 = new javax.swing.JButton();
        button_Help4 = new javax.swing.JButton();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        lblTitle.setText("Extract From Matrix: ");
        
        lbl_CurrentMatrix.setText(" ");
        
        lbl_NewMatrixName.setText("New Matrix Name:");
        
        lbl_Description.setText("Add Description:");
        
        textArea_Description.setColumns(20);
        textArea_Description.setRows(5);
        scroll_Description.setViewportView(textArea_Description);
        
        javax.swing.GroupLayout pnl_NameAndDescLayout = new javax.swing.GroupLayout(pnl_NameAndDesc);
        pnl_NameAndDesc.setLayout(pnl_NameAndDescLayout);
        pnl_NameAndDescLayout.setHorizontalGroup(
                pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_CurrentMatrix))
                .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_NewMatrixName)
                .addComponent(lbl_Description))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scroll_Description, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(126, Short.MAX_VALUE))
                );
        pnl_NameAndDescLayout.setVerticalGroup(
                pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblTitle)
                .addComponent(lbl_CurrentMatrix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_NewMatrixName)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_Description)
                .addComponent(scroll_Description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(239, 239, 239))
                );
        
        lbl_MarkerZone.setFont(new java.awt.Font("DejaVu Sans", 1, 14));
        lbl_MarkerZone.setText("Marker Selection");
        
        lbl_MarkersVariable.setText("Property / Variable: ");
        
        cmb_MarkersVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        
        button_Help3.setText("?");
        
        button_Help1.setText("?");
        
        lbl_MarkersCriteria.setText("Criteria:");
        
        txtArea_MarkersCriteria.setColumns(20);
        txtArea_MarkersCriteria.setRows(5);
        scroll_MarkersCriteria.setViewportView(txtArea_MarkersCriteria);
        
        lbl_MarkersCriteriaFile.setText("Criteria File:");
        
        button_MarkersCriteriaBrowse.setText("Browse");
        button_MarkersCriteriaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_MarkersCriteriaBrowseActionPerformed(evt);
            }
        });
        
        button_Help2.setText("?");
        
        javax.swing.GroupLayout pnl_MarkerZoneLayout = new javax.swing.GroupLayout(pnl_MarkerZone);
        pnl_MarkerZone.setLayout(pnl_MarkerZoneLayout);
        pnl_MarkerZoneLayout.setHorizontalGroup(
                pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkerZone)
                .addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_MarkerZoneLayout.createSequentialGroup()
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkersCriteria)
                .addComponent(lbl_MarkersCriteriaFile))
                .addGap(63, 63, 63)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txt_MarkersCriteriaFile, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                .addComponent(scroll_MarkersCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_MarkerZoneLayout.createSequentialGroup()
                .addComponent(lbl_MarkersVariable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmb_MarkersVariable, 0, 386, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_MarkersCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(button_Help3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(button_Help1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(button_Help2, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))))
                .addContainerGap())
                );
        pnl_MarkerZoneLayout.setVerticalGroup(
                pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MarkerZoneLayout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addComponent(lbl_MarkerZone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_MarkersVariable)
                .addComponent(cmb_MarkersVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkersCriteria)
                .addComponent(scroll_MarkersCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txt_MarkersCriteriaFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbl_MarkersCriteriaFile)))
                .addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
                .addComponent(button_Help1)
                .addGap(64, 64, 64)
                .addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(button_MarkersCriteriaBrowse)
                .addComponent(button_Help2))))
                .addContainerGap())
                );
        
        lbl_SampleZone.setFont(new java.awt.Font("DejaVu Sans", 1, 14));
        lbl_SampleZone.setText("Sample Selection");
        
        lbl_SamplesVariable.setText("Property / Variable: ");
        
        cmb_SamplesVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        
        lbl_SamplesCriteria.setText("Criteria:");
        
        txtArea_SamplesCriteria.setColumns(20);
        txtArea_SamplesCriteria.setRows(5);
        scroll_SamplesCriteria.setViewportView(txtArea_SamplesCriteria);
        
        lbl_SamplesCriteriaFile.setText("Criteria File:");
        
        button_SamplesCriteriaBrowse.setText("Browse");
        button_SamplesCriteriaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_SamplesCriteriaBrowseActionPerformed(evt);
            }
        });
        
        button_Help6.setText("?");
        
        button_Help5.setText("?");
        
        button_Help4.setText("?");
        
        javax.swing.GroupLayout pnl_SampleZoneLayout = new javax.swing.GroupLayout(pnl_SampleZone);
        pnl_SampleZone.setLayout(pnl_SampleZoneLayout);
        pnl_SampleZoneLayout.setHorizontalGroup(
                pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SampleZoneLayout.createSequentialGroup()
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_SampleZoneLayout.createSequentialGroup()
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_SamplesCriteria)
                .addGroup(pnl_SampleZoneLayout.createSequentialGroup()
                .addComponent(lbl_SamplesVariable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(txt_SamplesCriteriaFile)
                .addComponent(scroll_SamplesCriteria)
                .addComponent(cmb_SamplesVariable, 0, 385, Short.MAX_VALUE)))
                .addComponent(lbl_SamplesCriteriaFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_SamplesCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(button_Help4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addComponent(lbl_SampleZone))
                .addContainerGap())
                );
        pnl_SampleZoneLayout.setVerticalGroup(
                pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SampleZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_SampleZone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_SamplesVariable)
                .addComponent(cmb_SamplesVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SampleZoneLayout.createSequentialGroup()
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scroll_SamplesCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_SamplesCriteriaFile)
                .addComponent(txt_SamplesCriteriaFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_SamplesCriteriaBrowse)
                .addComponent(button_Help6)))
                .addComponent(lbl_SamplesCriteria))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        
        javax.swing.GroupLayout pnl_MatrixExtractPanelLayout = new javax.swing.GroupLayout(pnl_MatrixExtractPanel);
        pnl_MatrixExtractPanel.setLayout(pnl_MatrixExtractPanelLayout);
        pnl_MatrixExtractPanelLayout.setHorizontalGroup(
                pnl_MatrixExtractPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 706, Short.MAX_VALUE)
                .addGroup(pnl_MatrixExtractPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MatrixExtractPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_MatrixExtractPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_SampleZone, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                .addGroup(pnl_MatrixExtractPanelLayout.createSequentialGroup()
                .addComponent(pnl_MarkerZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12)))
                .addContainerGap()))
                );
        pnl_MatrixExtractPanelLayout.setVerticalGroup(
                pnl_MatrixExtractPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 631, Short.MAX_VALUE)
                .addGroup(pnl_MatrixExtractPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MatrixExtractPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_MarkerZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addComponent(pnl_SampleZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                );
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_MatrixExtractPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_MatrixExtractPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
                );
        
        pack();
    }//GEN-END:initComponents

    private void button_MarkersCriteriaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_MarkersCriteriaBrowseActionPerformed
		// TODO add your handling code here:
}//GEN-LAST:event_button_MarkersCriteriaBrowseActionPerformed

    private void button_SamplesCriteriaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_SamplesCriteriaBrowseActionPerformed
		// TODO add your handling code here:
}//GEN-LAST:event_button_SamplesCriteriaBrowseActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new TestJFrame2().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_Help1;
    private javax.swing.JButton button_Help2;
    private javax.swing.JButton button_Help3;
    private javax.swing.JButton button_Help4;
    private javax.swing.JButton button_Help5;
    private javax.swing.JButton button_Help6;
    private javax.swing.JButton button_MarkersCriteriaBrowse;
    private javax.swing.JButton button_SamplesCriteriaBrowse;
    private javax.swing.JComboBox cmb_MarkersVariable;
    private javax.swing.JComboBox cmb_SamplesVariable;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lbl_CurrentMatrix;
    private javax.swing.JLabel lbl_Description;
    private javax.swing.JLabel lbl_MarkerZone;
    private javax.swing.JLabel lbl_MarkersCriteria;
    private javax.swing.JLabel lbl_MarkersCriteriaFile;
    private javax.swing.JLabel lbl_MarkersVariable;
    private javax.swing.JLabel lbl_NewMatrixName;
    private javax.swing.JLabel lbl_SampleZone;
    private javax.swing.JLabel lbl_SamplesCriteria;
    private javax.swing.JLabel lbl_SamplesCriteriaFile;
    private javax.swing.JLabel lbl_SamplesVariable;
    private javax.swing.JPanel pnl_MarkerZone;
    private javax.swing.JPanel pnl_MatrixExtractPanel;
    private javax.swing.JPanel pnl_NameAndDesc;
    private javax.swing.JPanel pnl_SampleZone;
    private javax.swing.JScrollPane scroll_Description;
    private javax.swing.JScrollPane scroll_MarkersCriteria;
    private javax.swing.JScrollPane scroll_SamplesCriteria;
    private javax.swing.JTextArea textArea_Description;
    private javax.swing.JTextArea txtArea_MarkersCriteria;
    private javax.swing.JTextArea txtArea_SamplesCriteria;
    private javax.swing.JTextField txt_MarkersCriteriaFile;
    private javax.swing.JTextField txt_NewMatrixName;
    private javax.swing.JTextField txt_SamplesCriteriaFile;
    // End of variables declaration//GEN-END:variables
}
