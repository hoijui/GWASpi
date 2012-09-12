package org.gwaspi.gui;

import org.gwaspi.constants.cGlobal;
import org.gwaspi.global.Config;
import org.gwaspi.global.SysCommandExecutor;
import org.gwaspi.gui.utils.Dialogs;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u56124
 */
public class AnalyserTab extends JPanel {

	private final static Logger log
			= LoggerFactory.getLogger(AnalyserTab.class);

	// Variables declaration - do not modify
	private JPanel panel_Analysis;
	private JButton button_Go;
	private JButton button_PathToTfam;
	private JButton button_PathToTped;
	private JComboBox combo_Analysis;
	private JLabel label_Analysis;
	private JLabel label_PathToTfam;
	private JLabel label_PathToTped;
	private JLabel label_commandLine;
	private JScrollPane scrollPane_cliResult;
	private static JTextArea textArea_cliResult;
	private static JTextArea textArea_commandLine;
	private JTextField textField_PathToTfam;
	private JTextField textField_PathToTped;
	private JScrollPane jScrollPane1;
	// End of variables declaration

	// <editor-fold defaultstate="expanded" desc="Init">
	public AnalyserTab() {

		panel_Analysis = new JPanel();
		combo_Analysis = new JComboBox();
		label_Analysis = new JLabel();
		label_PathToTfam = new JLabel();
		label_PathToTped = new JLabel();
		textField_PathToTfam = new JTextField();
		textField_PathToTped = new JTextField();
		button_PathToTfam = new JButton();
		button_PathToTped = new JButton();
		button_Go = new JButton();
		scrollPane_cliResult = new JScrollPane();
		textArea_cliResult = new JTextArea();
		textArea_commandLine = new JTextArea();
		label_commandLine = new JLabel();
		jScrollPane1 = new JScrollPane();


		// Populate the combobox list
//		for( int iCtr = 0; iCtr < analysis.PlinkBase.analysisList.length; iCtr++ ){
//			combo_Analysis.addItem( analysis.PlinkBase.analysisList[iCtr] );
//		}
		combo_Analysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					combo_AnalysisActionPerformed(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		label_Analysis.setText("Analysis to perform");

		label_PathToTfam.setText("Path to .tfam file");

		label_PathToTped.setText("Path to .tped file");

		button_PathToTfam.setText("Browse");
		button_PathToTfam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					button_PathToTfamActionPerformed(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		button_PathToTped.setText("Browse");
		button_PathToTped.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					button_PathToTpedActionPerformed(evt);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		button_Go.setText("Go!");
		button_Go.setEnabled(true);
		button_Go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
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

		GroupLayout panel_AnalysisLayout = new GroupLayout(panel_Analysis);
		panel_Analysis.setLayout(panel_AnalysisLayout);
		panel_AnalysisLayout.setHorizontalGroup(
				panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(label_PathToTfam)
				.addComponent(label_PathToTped))
				.addGap(31, 31, 31)
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(button_PathToTfam)
				.addComponent(button_PathToTped)
				.addComponent(textField_PathToTfam, GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)
				.addComponent(textField_PathToTped, GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE)))
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addComponent(label_Analysis)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(combo_Analysis, GroupLayout.PREFERRED_SIZE, 188, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrollPane_cliResult, GroupLayout.DEFAULT_SIZE, 981, Short.MAX_VALUE)
				.addComponent(label_commandLine)
				.addGroup(panel_AnalysisLayout.createSequentialGroup()
				.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 863, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(button_Go, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));
		panel_AnalysisLayout.setVerticalGroup(
				panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, panel_AnalysisLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(label_PathToTfam)
				.addComponent(textField_PathToTfam, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(5, 5, 5)
				.addComponent(button_PathToTfam)
				.addGap(18, 18, 18)
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(label_PathToTped)
				.addComponent(textField_PathToTped, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(button_PathToTped)
				.addGap(18, 18, 18)
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(label_Analysis)
				.addComponent(combo_Analysis, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(23, 23, 23)
				.addComponent(label_commandLine)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(panel_AnalysisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(button_Go, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
				.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrollPane_cliResult, GroupLayout.PREFERRED_SIZE, 293, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		this.add(panel_Analysis);
		this.setVisible(true);
	}
	// </editor-fold>

	// <editor-fold defaultstate="expanded" desc="Actions codeblock ">
	private void button_GoActionPerformed(ActionEvent evt) {
		SysCommandExecutor.sysAnalyserCommandExecute();
	}

	private void button_PathToTfamActionPerformed(ActionEvent evt) throws IOException {
		// Use standard file opener
		Dialogs.selectAndSetFileInCurrentDirDialog(evt, button_PathToTfam, Config.getConfigValue(Config.PROPERTY_EXPORT_DIR, cGlobal.HOMEDIR), textField_PathToTfam, ".tfam");
	}

	private void button_PathToTpedActionPerformed(ActionEvent evt) throws IOException {
		// Use standard file opener
		Dialogs.selectAndSetFileInCurrentDirDialog(evt, button_PathToTped, Config.getConfigValue(Config.PROPERTY_EXPORT_DIR, cGlobal.HOMEDIR), textField_PathToTped, ".tped");
	}

	private void combo_AnalysisActionPerformed(ActionEvent evt) throws IOException {
//		JOptionPane.showMessageDialog(base.ApipelineGUI.getFrames()[0], "combo_AnalysisActionPerformed");
//		String commandLine = analysis.PlinkBase.getCommandLine(combo_Analysis.getSelectedIndex(),textField_PathToTfam.getText(),textField_PathToTped.getText());
//		SysCommandExecutor.sysAnalyserCommandPost(commandLine);
	}
	// </editor-fold>
}
