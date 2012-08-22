package org.gwaspi.gui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author u56124
 */
public class AnalyserTab extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private javax.swing.JPanel panel_Analysis;
	private javax.swing.JButton button_Go;
	private javax.swing.JButton button_PathToTfam;
	private javax.swing.JButton button_PathToTped;
	private javax.swing.JComboBox combo_Analysis;
	private javax.swing.JLabel label_Analysis;
	private javax.swing.JLabel label_PathToTfam;
	private javax.swing.JLabel label_PathToTped;
	private javax.swing.JLabel label_commandLine;
	private javax.swing.JScrollPane scrollPane_cliResult;
	private static javax.swing.JTextArea textArea_cliResult;
	private static javax.swing.JTextArea textArea_commandLine;
	private javax.swing.JTextField textField_PathToTfam;
	private javax.swing.JTextField textField_PathToTped;
	private javax.swing.JScrollPane jScrollPane1;
	// End of variables declaration

	// <editor-fold defaultstate="expanded" desc="Init">
	public AnalyserTab() {

		panel_Analysis = new javax.swing.JPanel();
		combo_Analysis = new javax.swing.JComboBox();
		label_Analysis = new javax.swing.JLabel();
		label_PathToTfam = new javax.swing.JLabel();
		label_PathToTped = new javax.swing.JLabel();
		textField_PathToTfam = new javax.swing.JTextField();
		textField_PathToTped = new javax.swing.JTextField();
		button_PathToTfam = new javax.swing.JButton();
		button_PathToTped = new javax.swing.JButton();
		button_Go = new javax.swing.JButton();
		scrollPane_cliResult = new javax.swing.JScrollPane();
		textArea_cliResult = new javax.swing.JTextArea();
		textArea_commandLine = new javax.swing.JTextArea();
		label_commandLine = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();


		// Populate the combobox list
//		for( int iCtr = 0; iCtr < analysis.PlinkBase.analysisList.length; iCtr++ ){
//			combo_Analysis.addItem( analysis.PlinkBase.analysisList[iCtr] );
//		}
		combo_Analysis.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					combo_AnalysisActionPerformed(evt);
				} catch (IOException ex) {
					Logger.getLogger(AnalyserTab.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		label_Analysis.setText("Analysis to perform");

		label_PathToTfam.setText("Path to .tfam file");

		label_PathToTped.setText("Path to .tped file");

		button_PathToTfam.setText("Browse");
		button_PathToTfam.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					button_PathToTfamActionPerformed(evt);
				} catch (IOException ex) {
					Logger.getLogger(AnalyserTab.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		button_PathToTped.setText("Browse");
		button_PathToTped.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					button_PathToTpedActionPerformed(evt);
				} catch (IOException ex) {
					Logger.getLogger(AnalyserTab.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		button_Go.setText("Go!");
		button_Go.setEnabled(true);
		button_Go.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				button_GoActionPerformed(evt);
			}
		});

		textArea_cliResult.setColumns(20);
		textArea_cliResult.setRows(5);
		scrollPane_cliResult.setViewportView(textArea_cliResult);

		textArea_commandLine.setColumns(20);
		textArea_commandLine.setLineWrap(true);
		textArea_commandLine.setRows(5);
		jScrollPane1.setViewportView(textArea_commandLine);

		label_commandLine.setText("Command line to be executed:");

		javax.swing.GroupLayout panel_AnalysisLayout = new javax.swing.GroupLayout(panel_Analysis);
		panel_Analysis.setLayout(panel_AnalysisLayout);
		panel_AnalysisLayout.setHorizontalGroup(
				panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(label_PathToTfam)
				.addComponent(label_PathToTped))
				.addGap(31, 31, 31)
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(button_PathToTfam)
				.addComponent(button_PathToTped)
				.addComponent(textField_PathToTfam, javax.swing.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)
				.addComponent(textField_PathToTped, javax.swing.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)))
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addComponent(label_Analysis)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(combo_Analysis, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addComponent(scrollPane_cliResult, javax.swing.GroupLayout.DEFAULT_SIZE, 981, Short.MAX_VALUE)
				.addComponent(label_commandLine)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 863, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(button_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));
		panel_AnalysisLayout.setVerticalGroup(
				panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_AnalysisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(label_PathToTfam)
				.addComponent(textField_PathToTfam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addGap(5, 5, 5)
				.addComponent(button_PathToTfam)
				.addGap(18, 18, 18)
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(label_PathToTped)
				.addComponent(textField_PathToTped, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(button_PathToTped)
				.addGap(18, 18, 18)
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(label_Analysis)
				.addComponent(combo_Analysis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addGap(23, 23, 23)
				.addComponent(label_commandLine)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(panel_AnalysisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
				.addComponent(button_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrollPane_cliResult, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		this.add(panel_Analysis);
		this.setVisible(true);

	}
	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Actions codeblock ">
	private void button_GoActionPerformed(java.awt.event.ActionEvent evt) {
		org.gwaspi.global.SysCommandExecutor.sysAnalyserCommandExecute();
	}

	private void button_PathToTfamActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
		//Use standard file opener
		org.gwaspi.gui.utils.Dialogs.selectAndSetFileInCurrentDirDialogue(evt, button_PathToTfam, org.gwaspi.global.Config.getConfigValue("ExportDir", org.gwaspi.constants.cGlobal.HOMEDIR), textField_PathToTfam, ".tfam");
	}

	private void button_PathToTpedActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
		//Use standard file opener
		org.gwaspi.gui.utils.Dialogs.selectAndSetFileInCurrentDirDialogue(evt, button_PathToTped, org.gwaspi.global.Config.getConfigValue("ExportDir", org.gwaspi.constants.cGlobal.HOMEDIR), textField_PathToTped, ".tped");
	}

	private void combo_AnalysisActionPerformed(java.awt.event.ActionEvent evt) throws IOException {
//		JOptionPane.showMessageDialog(base.ApipelineGUI.getFrames()[0], "combo_AnalysisActionPerformed");
//		String commandLine = analysis.PlinkBase.getCommandLine(combo_Analysis.getSelectedIndex(),textField_PathToTfam.getText(),textField_PathToTped.getText());
//		org.gwaspi.global.SysCommandExecutor.sysAnalyserCommandPost(commandLine);
	}
	// </editor-fold>
}
