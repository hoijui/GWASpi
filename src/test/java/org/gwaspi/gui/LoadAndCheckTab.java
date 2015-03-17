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

public class LoadAndCheckTab extends javax.swing.JFrame {

	/**
	 * Creates new form LoadAndCheckTab
	 */
	public LoadAndCheckTab() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panel_DataLoad = new javax.swing.JPanel();
        label_inputFormat = new javax.swing.JLabel();
        combo_inputFormat = new javax.swing.JComboBox();
        button_AddData = new javax.swing.JButton();
        label_progress = new javax.swing.JLabel();
        progressBar_progress = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        label_inputFormat.setText("Input format: ");

        combo_inputFormat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        button_AddData.setText("Load Data");
        button_AddData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_AddDataActionPerformed(evt);
            }
        });

        label_progress.setText("Progress: ");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("Important! \nRead this before you load any genotypes into the Database!\n\nGenotypes should not be loaded unless you have added the files from all the chips \nyou need to proceed with your study! Loading new genotypes from a given technology \nalready loaded in a previous process will generate repeated entries and will be \nflagged as duplicate, inconsistent or erroneous data, depending on the contents of\nthe genotyping experiments.\n\nInitiating a Load will start a series of integrity and quality checks upon your data \nand will take a long time to perform, depending on your hardware. Be sure to \nkeep your workstation safe from crashes, as it may stop a process in midst and \ngenerate corrupted data. This would imply that the process has to be started \nagain or even that your dataset may become unusable and has to be recreated.\n\nPlease take all this factors in considerations before starting this task!");
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout panel_DataLoadLayout = new javax.swing.GroupLayout(panel_DataLoad);
        panel_DataLoad.setLayout(panel_DataLoadLayout);
        panel_DataLoadLayout.setHorizontalGroup(
                panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_DataLoadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel_DataLoadLayout.createSequentialGroup()
                .addComponent(label_inputFormat)
                .addGap(32, 32, 32)
                .addComponent(combo_inputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
                .addGroup(panel_DataLoadLayout.createSequentialGroup()
                .addComponent(label_progress, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(button_AddData, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(progressBar_progress, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE))))
                .addContainerGap())
                );
        panel_DataLoadLayout.setVerticalGroup(
                panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_DataLoadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(label_inputFormat)
                .addComponent(combo_inputFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(button_AddData)
                .addGap(18, 18, 18)
                .addGroup(panel_DataLoadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(label_progress)
                .addComponent(progressBar_progress, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59))
                );

        jTabbedPane1.addTab("tab2", panel_DataLoad);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                .addContainerGap())
                );

        pack();
    }//GEN-END:initComponents

private void button_AddDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_AddDataActionPerformed
}//GEN-LAST:event_button_AddDataActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new LoadAndCheckTab().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_AddData;
    private javax.swing.JComboBox combo_inputFormat;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel label_inputFormat;
    private javax.swing.JLabel label_progress;
    private javax.swing.JPanel panel_DataLoad;
    private javax.swing.JProgressBar progressBar_progress;
    // End of variables declaration//GEN-END:variables
}
