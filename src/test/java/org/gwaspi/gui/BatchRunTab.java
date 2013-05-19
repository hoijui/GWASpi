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

package org.gwaspi.gui;

public class BatchRunTab extends javax.swing.JFrame {

	/**
	 * Creates new form BatchRunTab
	 */
	public BatchRunTab() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents

        radioGroup_Samples = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panel_BatchRun = new javax.swing.JPanel();
        title_Progress = new javax.swing.JLabel();
        progressBar_progress = new javax.swing.JProgressBar();
        button_RunBatch = new javax.swing.JButton();
        scrollPane_Operations = new javax.swing.JScrollPane();
        panel_Operations = new javax.swing.JPanel();
        title_Study = new javax.swing.JLabel();
        combo_Study = new javax.swing.JComboBox();
        separator_Study = new javax.swing.JSeparator();
        title_Samples = new javax.swing.JLabel();
        radio_LoadSamples = new javax.swing.JRadioButton();
        textField_LoadSamples = new javax.swing.JTextField();
        button_LoadSamples = new javax.swing.JButton();
        radio_DummySamples = new javax.swing.JRadioButton();
        separator_Samples = new javax.swing.JSeparator();
        title_DNAArrays = new javax.swing.JLabel();
        label_Format = new javax.swing.JLabel();
        combo_Format = new javax.swing.JComboBox();
        textField_DNAArraysDir = new javax.swing.JTextField();
        button_DNAArrays = new javax.swing.JButton();
        separator_DNAArrays = new javax.swing.JSeparator();
        title_Analysis = new javax.swing.JLabel();
        checkBox_Analysis = new javax.swing.JCheckBox();
        combo_Analysis = new javax.swing.JComboBox();
        separator_Progress = new javax.swing.JSeparator();
        checkBox_ExportPlink = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        title_Progress.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        title_Progress.setText("General Progress");

        button_RunBatch.setText("Run Batch");

        title_Study.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        title_Study.setText("Study");

        combo_Study.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        title_Samples.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        title_Samples.setText("Samples");

        radio_LoadSamples.setText("Use your Sample info");
        radio_LoadSamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radio_LoadSamplesActionPerformed(evt);
            }
        });

        textField_LoadSamples.setText("Location of Sample Info file...");

        button_LoadSamples.setText("Browse");

        radio_DummySamples.setText("Generate dummy Samples and info");
        radio_DummySamples.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radio_DummySamplesActionPerformed(evt);
            }
        });

        title_DNAArrays.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        title_DNAArrays.setText("DNA Arrays");

        label_Format.setText("Format");

        combo_Format.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        textField_DNAArraysDir.setText("Location of DNA array files...");
        textField_DNAArraysDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField_DNAArraysDirActionPerformed(evt);
            }
        });

        button_DNAArrays.setText("Browse");

        title_Analysis.setFont(new java.awt.Font("DejaVu Sans", 1, 13));
        title_Analysis.setText("Analysis");

        checkBox_Analysis.setText("Perform Analysis");
        checkBox_Analysis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBox_AnalysisStateChanged(evt);
            }
        });
        checkBox_Analysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBox_AnalysisActionPerformed(evt);
            }
        });

        combo_Analysis.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        checkBox_ExportPlink.setText("Export Plink files");

        javax.swing.GroupLayout panel_OperationsLayout = new javax.swing.GroupLayout(panel_Operations);
        panel_Operations.setLayout(panel_OperationsLayout);
        panel_OperationsLayout.setHorizontalGroup(
                panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(title_Study)
                .addComponent(title_Samples)
                .addComponent(title_DNAArrays)
                .addComponent(title_Analysis))
                .addGap(808, 808, 808))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(combo_Analysis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(802, 802, 802))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(radio_DummySamples)
                .addGap(622, 622, 622))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(radio_LoadSamples)
                .addGap(713, 713, 713))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(combo_Study, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(802, Short.MAX_VALUE))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(textField_DNAArraysDir, javax.swing.GroupLayout.PREFERRED_SIZE, 691, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_DNAArrays))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addComponent(label_Format)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combo_Format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(113, 113, 113))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(separator_Samples, javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel_OperationsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(textField_LoadSamples, javax.swing.GroupLayout.PREFERRED_SIZE, 703, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_LoadSamples)))
                .addGap(10, 10, 10))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(separator_Progress, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
                .addComponent(separator_Study, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE))
                .addGap(10, 10, 10))))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(separator_DNAArrays, javax.swing.GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE)
                .addGap(113, 113, 113))
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(checkBox_ExportPlink)
                .addComponent(checkBox_Analysis))
                .addGap(749, 749, 749))
                );


        panel_OperationsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {textField_DNAArraysDir, textField_LoadSamples});



        panel_OperationsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {separator_DNAArrays, separator_Progress, separator_Samples, separator_Study});

        panel_OperationsLayout.setVerticalGroup(
                panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_OperationsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title_Study)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combo_Study, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator_Study, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(title_Samples)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radio_LoadSamples)
                .addGap(2, 2, 2)
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(textField_LoadSamples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(button_LoadSamples))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radio_DummySamples)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator_Samples, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(title_DNAArrays)
                .addGap(8, 8, 8)
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(label_Format)
                .addComponent(combo_Format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel_OperationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(button_DNAArrays)
                .addComponent(textField_DNAArraysDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator_DNAArrays, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(title_Analysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBox_ExportPlink)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBox_Analysis)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combo_Analysis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(separator_Progress, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
                );


        panel_OperationsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {separator_DNAArrays, separator_Progress, separator_Samples, separator_Study});


        scrollPane_Operations.setViewportView(panel_Operations);

        javax.swing.GroupLayout panel_BatchRunLayout = new javax.swing.GroupLayout(panel_BatchRun);
        panel_BatchRun.setLayout(panel_BatchRunLayout);
        panel_BatchRunLayout.setHorizontalGroup(
                panel_BatchRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_BatchRunLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_BatchRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scrollPane_Operations, javax.swing.GroupLayout.PREFERRED_SIZE, 906, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(panel_BatchRunLayout.createSequentialGroup()
                .addGap(330, 330, 330)
                .addComponent(button_RunBatch, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(title_Progress)
                .addGroup(panel_BatchRunLayout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar_progress, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
                );


        panel_BatchRunLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {progressBar_progress, scrollPane_Operations});

        panel_BatchRunLayout.setVerticalGroup(
                panel_BatchRunLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_BatchRunLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane_Operations, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(title_Progress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar_progress, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(button_RunBatch, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
                );

        jTabbedPane1.addTab("tab1", panel_BatchRun);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 941, javax.swing.GroupLayout.PREFERRED_SIZE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE)
                );

        pack();
    }//GEN-END:initComponents

private void textField_DNAArraysDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField_DNAArraysDirActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_textField_DNAArraysDirActionPerformed

private void radio_LoadSamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_LoadSamplesActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_radio_LoadSamplesActionPerformed

private void radio_DummySamplesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_DummySamplesActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_radio_DummySamplesActionPerformed

private void checkBox_AnalysisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkBox_AnalysisStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_checkBox_AnalysisStateChanged

private void checkBox_AnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBox_AnalysisActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_checkBox_AnalysisActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				//new BatchRunTab().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_DNAArrays;
    private javax.swing.JButton button_LoadSamples;
    private javax.swing.JButton button_RunBatch;
    private javax.swing.JCheckBox checkBox_Analysis;
    private javax.swing.JCheckBox checkBox_ExportPlink;
    private javax.swing.JComboBox combo_Analysis;
    private javax.swing.JComboBox combo_Format;
    private javax.swing.JComboBox combo_Study;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel label_Format;
    private javax.swing.JPanel panel_BatchRun;
    private javax.swing.JPanel panel_Operations;
    private javax.swing.JProgressBar progressBar_progress;
    private javax.swing.ButtonGroup radioGroup_Samples;
    private javax.swing.JRadioButton radio_DummySamples;
    private javax.swing.JRadioButton radio_LoadSamples;
    private javax.swing.JScrollPane scrollPane_Operations;
    private javax.swing.JSeparator separator_DNAArrays;
    private javax.swing.JSeparator separator_Progress;
    private javax.swing.JSeparator separator_Samples;
    private javax.swing.JSeparator separator_Study;
    private javax.swing.JTextField textField_DNAArraysDir;
    private javax.swing.JTextField textField_LoadSamples;
    private javax.swing.JLabel title_Analysis;
    private javax.swing.JLabel title_DNAArrays;
    private javax.swing.JLabel title_Progress;
    private javax.swing.JLabel title_Samples;
    private javax.swing.JLabel title_Study;
    // End of variables declaration//GEN-END:variables
}
