package gui;

public class TestJFrame extends javax.swing.JFrame {

    public TestJFrame() {
        initComponents();
    }

    private void initComponents() {//GEN-BEGIN:initComponents
        
        lblTitle = new javax.swing.JLabel();
        lbl_CurrentMatrix = new javax.swing.JLabel();
        lbl_NewMatrixName = new javax.swing.JLabel();
        txt_NewMatrixName = new javax.swing.JTextField();
        lbl_Description = new javax.swing.JLabel();
        scroll_Description = new javax.swing.JScrollPane();
        textArea_Description = new javax.swing.JTextArea();
        lbl_MarkerZone = new javax.swing.JLabel();
        lbl_MarkersVariable = new javax.swing.JLabel();
        cmb_MarkersVariable = new javax.swing.JComboBox();
        lbl_MarkersCriteria = new javax.swing.JLabel();
        scroll_MarkersCriteria = new javax.swing.JScrollPane();
        txtArea_MarkersCriteria = new javax.swing.JTextArea();
        lbl_MarkersCriteriaFile = new javax.swing.JLabel();
        txt_MarkersCriteriaFile = new javax.swing.JTextField();
        button_MarkersCriteriaBrowse = new javax.swing.JButton();
        button_Help1 = new javax.swing.JButton();
        button_Help2 = new javax.swing.JButton();
        button_Help3 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        lbl_SampleZone = new javax.swing.JLabel();
        lbl_SamplesVariable = new javax.swing.JLabel();
        cmb_SamplesVariable = new javax.swing.JComboBox();
        scroll_SamplesCriteria = new javax.swing.JScrollPane();
        txtArea_SamplesCriteria = new javax.swing.JTextArea();
        lbl_SamplesCriteria = new javax.swing.JLabel();
        lbl_SamplesCriteriaFile = new javax.swing.JLabel();
        txt_SamplesCriteriaFile = new javax.swing.JTextField();
        button_SamplesCriteriaBrowse = new javax.swing.JButton();
        button_Help4 = new javax.swing.JButton();
        button_Help5 = new javax.swing.JButton();
        button_Help6 = new javax.swing.JButton();
        button_Back = new javax.swing.JButton();
        button_Go = new javax.swing.JButton();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        lblTitle.setText("Extract From Matrix: ");
        
        lbl_CurrentMatrix.setText(" ");
        
        lbl_NewMatrixName.setText("New Matrix Name:");
        
        lbl_Description.setText("Add Description:");
        
        textArea_Description.setColumns(20);
        textArea_Description.setRows(5);
        scroll_Description.setViewportView(textArea_Description);
        
        lbl_MarkerZone.setFont(new java.awt.Font("DejaVu Sans", 1, 14));
        lbl_MarkerZone.setText("Marker Selection");
        
        lbl_MarkersVariable.setText("Property / Variable: ");
        
        cmb_MarkersVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        
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
        
        button_Help1.setText("?");
        
        button_Help2.setText("?");
        
        button_Help3.setText("?");
        
        lbl_SampleZone.setFont(new java.awt.Font("DejaVu Sans", 1, 14));
        lbl_SampleZone.setText("Sample Selection");
        
        lbl_SamplesVariable.setText("Property / Variable: ");
        
        cmb_SamplesVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        
        txtArea_SamplesCriteria.setColumns(20);
        txtArea_SamplesCriteria.setRows(5);
        scroll_SamplesCriteria.setViewportView(txtArea_SamplesCriteria);
        
        lbl_SamplesCriteria.setText("Criteria:");
        
        lbl_SamplesCriteriaFile.setText("Criteria File:");
        
        button_SamplesCriteriaBrowse.setText("Browse");
        button_SamplesCriteriaBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_SamplesCriteriaBrowseActionPerformed(evt);
            }
        });
        
        button_Help4.setText("?");
        
        button_Help5.setText("?");
        
        button_Help6.setText("?");
        
        button_Back.setText("Back");
        button_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_BackActionPerformed(evt);
            }
        });
        
        button_Go.setText("Go!");
        button_Go.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_GoActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkerZone)
                .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_CurrentMatrix)
                .addContainerGap(2830, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                .addComponent(lbl_Description)
                .addContainerGap(2874, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2503, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                .addComponent(lbl_SampleZone)
                .addContainerGap(2844, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(lbl_SamplesVariable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmb_SamplesVariable, 0, 418, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkersCriteria)
                .addComponent(lbl_MarkersCriteriaFile))
                .addGap(63, 63, 63)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txt_MarkersCriteriaFile, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addComponent(scroll_MarkersCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(lbl_MarkersVariable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmb_MarkersVariable, 0, 418, Short.MAX_VALUE))
                .addComponent(scroll_Description, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(lbl_NewMatrixName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(button_Help3, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addComponent(button_MarkersCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_Help2, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                .addComponent(button_Help1, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
                .addContainerGap())
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(button_Help4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2345, 2345, 2345))))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(button_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 350, Short.MAX_VALUE)
                .addComponent(button_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_SamplesCriteria)
                .addComponent(lbl_SamplesCriteriaFile))
                .addGap(63, 63, 63)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txt_SamplesCriteriaFile, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                .addComponent(scroll_SamplesCriteria, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(button_SamplesCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_Help6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(button_Help5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2286, 2286, 2286))))
                );
        
        
        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {button_Help1, button_Help2, button_Help3, button_Help4, button_Help5, button_Help6});
        
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblTitle)
                .addComponent(lbl_CurrentMatrix))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_NewMatrixName)
                .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(lbl_Description)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll_Description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_MarkerZone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_MarkersVariable)
                .addComponent(cmb_MarkersVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_MarkersCriteria)
                .addComponent(scroll_MarkersCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txt_MarkersCriteriaFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbl_MarkersCriteriaFile)))
                .addGroup(layout.createSequentialGroup()
                .addComponent(button_Help1)
                .addGap(64, 64, 64)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(button_MarkersCriteriaBrowse)
                .addComponent(button_Help2))))
                .addGap(39, 39, 39)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_SampleZone)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_SamplesVariable)
                .addComponent(cmb_SamplesVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_SamplesCriteria)
                .addComponent(scroll_SamplesCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_Help5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txt_SamplesCriteriaFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbl_SamplesCriteriaFile)
                .addComponent(button_SamplesCriteriaBrowse)
                .addComponent(button_Help6))
                .addGap(70, 70, 70)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(button_Back)
                .addComponent(button_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(164, 164, 164))
                );
        
        
        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {button_Help1, button_Help2, button_Help3, button_Help4, button_Help5, button_Help6});
        
        
        pack();
    }//GEN-END:initComponents

    private void button_MarkersCriteriaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_MarkersCriteriaBrowseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_button_MarkersCriteriaBrowseActionPerformed

    private void button_SamplesCriteriaBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_SamplesCriteriaBrowseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_button_SamplesCriteriaBrowseActionPerformed

    private void button_BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_BackActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_button_BackActionPerformed

    private void button_GoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_GoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_button_GoActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_Back;
    private javax.swing.JButton button_Go;
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
    private javax.swing.JSeparator jSeparator1;
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
