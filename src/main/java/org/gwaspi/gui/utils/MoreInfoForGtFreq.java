package org.gwaspi.gui.utils;

import org.gwaspi.global.Text;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MoreInfoForGtFreq extends javax.swing.JFrame {

	// Variables declaration - do not modify
	private static javax.swing.JButton btn_Go;
	private static javax.swing.JButton btn_Help;
	private static javax.swing.JButton btn_Cancel;
	private static javax.swing.JCheckBox chkB_1;
	private static javax.swing.JCheckBox chkB_2;
	private static javax.swing.JCheckBox chkB_SMS;
	private static javax.swing.JCheckBox chkB_SHZ;
	private static javax.swing.JTextField txtF_1;
	private static javax.swing.JTextField txtF_SMS;
	private static javax.swing.JTextField txtF_MHZ;
	private static javax.swing.JTextField txtF_SHZ;
	private static JFrame myFrame = new JFrame("GridBagLayout Test");
	private static JDialog dialog;
	public static GWASinOneGOParams gwasParams = new GWASinOneGOParams();

	// End of variables declaration
	public static GWASinOneGOParams showMoreInfoForQA_Modal() {

		gwasParams.setProceed(false);
		// Create a modal dialog
		dialog = new JDialog(myFrame, "Genotype freq. info", true);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		dialog.setLocation(screenWidth / 4, screenHeight / 4);

		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container myPane = dialog.getContentPane();
		myPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setMyConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(getFooterPanel(), c);
		dialog.pack();
		dialog.setVisible(true);

		return gwasParams;
	}

	public static JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		chkB_1 = new javax.swing.JCheckBox();
		chkB_2 = new javax.swing.JCheckBox();
		chkB_SMS = new javax.swing.JCheckBox();
		chkB_SHZ = new javax.swing.JCheckBox();
		txtF_1 = new javax.swing.JTextField();
		txtF_1.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_SMS = new javax.swing.JTextField();
		txtF_SMS.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_MHZ = new javax.swing.JTextField();
		txtF_MHZ.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_SHZ = new javax.swing.JTextField();
		txtF_SHZ.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());

		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;
		//<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
		chkB_1.setSelected(true);
		chkB_2.setSelected(true);
		chkB_SMS.setSelected(true);
		chkB_SHZ.setSelected(false);


		chkB_1.setText(Text.Operation.discardMismatch);
		chkB_1.setEnabled(false);

		chkB_2.setText(Text.Operation.discardMarkerMissing);
		txtF_1.setText("0.050");

		chkB_SMS.setText(Text.Operation.discardSampleMissing);
		try {
			txtF_SMS.setText(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SMS.setText("0.050");
		}

		chkB_SHZ.setText(Text.Operation.discardSampleHetzy);
		try {
			txtF_SHZ.setText(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5"));
		} catch (IOException ex) {
			txtF_SHZ.setText("1.000");
		}
		chkB_SHZ.setEnabled(true);



		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_1, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(new JLabel(""), c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_2, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_1, c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SMS, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SMS, c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SHZ, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SHZ, c);
		rowNb++;
		//</editor-fold>

		pnl_Questions.setVisible(true);

		return pnl_Questions;
	}

	public static JPanel getFooterPanel() {

		JPanel pnl_Footer = new JPanel(new GridBagLayout());

		btn_Go = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();
		btn_Cancel = new javax.swing.JButton();

		btn_Help.setText("  " + Text.Help.help + "  ");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp(evt);
			}
		});

		btn_Go.setText("   " + Text.All.go + "   ");
		btn_Go.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionGo(evt);
			}
		});

		btn_Cancel.setText("   " + Text.All.cancel + "   ");
		btn_Cancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionCancel(evt);
			}
		});

//		GridBagConstraints c = new GridBagConstraints();
//		setMyConstraints(c,0,0,GridBagConstraints.LINE_START);
//		pnl_Footer.add(btn_Help,c);
//		setMyConstraints(c,1,0,GridBagConstraints.LINE_END);
//		pnl_Footer.add(new JLabel("    "),c);
//		setMyConstraints(c,2,0,GridBagConstraints.LINE_END);
//		pnl_Footer.add(btn_Go,c);

		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Cancel, c);
		setMyConstraints(c, 1, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setMyConstraints(c, 2, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Help, c);
		setMyConstraints(c, 3, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setMyConstraints(c, 4, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(btn_Go, c);

		pnl_Footer.setVisible(true);

		return pnl_Footer;
	}

	private static void actionHelp(ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.GWASinOneGo);
		} catch (IOException ex) {
			Logger.getLogger(MoreInfoForGtFreq.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void actionGo(ActionEvent evt) {
		if (!txtF_1.getText().isEmpty() && !txtF_SMS.getText().isEmpty()) {
			try {
				gwasParams.setDiscardGTMismatches(chkB_1.isSelected());
				gwasParams.setDiscardMarkerByMisRat(chkB_2.isSelected());
				gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(txtF_1.getText()));
				gwasParams.setDiscardSampleByMisRat(chkB_SMS.isSelected());
				gwasParams.setDiscardSampleMisRatVal(Double.parseDouble(txtF_SMS.getText()));
				gwasParams.setDiscardSampleByHetzyRat(chkB_SHZ.isSelected());
				gwasParams.setDiscardSampleHetzyRatVal(Double.parseDouble(txtF_SHZ.getText()));
				gwasParams.setProceed(true);
			} catch (NumberFormatException numberFormatException) {
			}
			dialog.dispose();
		} else {
			gwasParams.setProceed(false);
		}
	}

	private static void actionCancel(ActionEvent evt) {
		dialog.setVisible(false);
	}

	private static void setMyConstraints(GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor) {
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}
}
