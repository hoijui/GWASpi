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
public class MoreGWASinOneGoInfo extends javax.swing.JFrame {

	// Variables declaration - do not modify
	private static javax.swing.JButton btn_Go;
	private static javax.swing.JButton btn_Help;
	private static javax.swing.JButton btn_Cancel;
	private static javax.swing.JCheckBox chkB_allelic;
	private static javax.swing.JCheckBox chkB_geno;
	private static javax.swing.JCheckBox chkB_trend;
	private static javax.swing.JCheckBox chkB_MMM;
	private static javax.swing.JCheckBox chkB_MMS;
	private static javax.swing.JCheckBox chkB_MHZ;
	private static javax.swing.JCheckBox chkB_SMS;
	private static javax.swing.JCheckBox chkB_SHZ;
	private static javax.swing.JRadioButton rdioB_HW_Calc;
	private static javax.swing.JLabel lbl_HW;
	private static javax.swing.JRadioButton rdioB_HW_free;
	private static javax.swing.JTextField txtF_MMS;
	private static javax.swing.JTextField txtF_MHZ;
	private static javax.swing.JTextField txtF_HW_free;
	private static javax.swing.JTextField txtF_SMS;
	private static javax.swing.JTextField txtF_SHZ;
	private static javax.swing.ButtonGroup rdiogrp_HW;
	private static javax.swing.JLabel lbl_Chromosome;
	private static javax.swing.JComboBox cmb_Chromosome;
	private static javax.swing.JLabel lbl_Strand;
	private static javax.swing.JComboBox cmb_Strand;
	private static javax.swing.JLabel lbl_GTCode;
	private static javax.swing.JComboBox cmb_GTCode;
	private static JFrame myFrame = new JFrame("GridBagLayout Test");
	public static GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private static JDialog dialog;
	private static String format = "";

	// End of variables declaration
	public static GWASinOneGOParams showGWASInOneGo_Modal(String _format) {
		// Create a modal dialog
		gwasParams.setProceed(false);
		dialog = new JDialog(myFrame, Text.Operation.gwasInOneGo, true);
		format = _format;

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
		myPane.add(getHeaderPanel(), c);
		setMyConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setMyConstraints(c, 0, 2, GridBagConstraints.CENTER);
		myPane.add(getFooterPanel(), c);
		dialog.pack();
		dialog.setVisible(true);

		return gwasParams;
	}

	public static JPanel getHeaderPanel() {

		JPanel pnl_Header = new JPanel(new GridBagLayout());
		pnl_Header.setBorder(BorderFactory.createTitledBorder("Analysis to perform"));

		chkB_allelic = new javax.swing.JCheckBox();
		chkB_allelic.setText("  " + Text.Operation.performAllelicTests + "  ");
		chkB_allelic.setSelected(true);
		chkB_geno = new javax.swing.JCheckBox();
		chkB_geno.setText("  " + Text.Operation.performGenotypicTests + "  ");
		chkB_geno.setSelected(true);
		chkB_trend = new javax.swing.JCheckBox();
		chkB_trend.setText("  " + Text.Operation.performTrendTests + "  ");
		chkB_trend.setSelected(true);

		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("      "), c);
		setMyConstraints(c, 1, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_allelic, c);
		setMyConstraints(c, 2, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("    "), c);
		setMyConstraints(c, 3, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_geno, c);
		pnl_Header.add(new JLabel("        "), c);
		setMyConstraints(c, 4, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_trend, c);
		setMyConstraints(c, 5, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("      "), c);

		pnl_Header.setVisible(true);
		return pnl_Header;
	}

	public static JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		chkB_MMM = new javax.swing.JCheckBox();
		chkB_MMS = new javax.swing.JCheckBox();
		chkB_MHZ = new javax.swing.JCheckBox();
		chkB_SMS = new javax.swing.JCheckBox();
		chkB_SHZ = new javax.swing.JCheckBox();
		rdioB_HW_Calc = new javax.swing.JRadioButton();
		lbl_HW = new javax.swing.JLabel();
		lbl_Chromosome = new javax.swing.JLabel();
		lbl_Strand = new javax.swing.JLabel();
		lbl_GTCode = new javax.swing.JLabel();
		rdioB_HW_free = new javax.swing.JRadioButton();
		txtF_MMS = new javax.swing.JTextField();
		txtF_MMS.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_MHZ = new javax.swing.JTextField();
		txtF_MHZ.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_HW_free = new javax.swing.JTextField();
		txtF_HW_free.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_SMS = new javax.swing.JTextField();
		txtF_SMS.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		txtF_SHZ = new javax.swing.JTextField();
		txtF_SHZ.setInputVerifier(new org.gwaspi.gui.utils.DoubleInputVerifier());
		rdiogrp_HW = new javax.swing.ButtonGroup();

		cmb_Chromosome = new javax.swing.JComboBox();
		cmb_Strand = new javax.swing.JComboBox();
		cmb_GTCode = new javax.swing.JComboBox();


		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;
		//<editor-fold defaultstate="collapsed" desc="FORMAT DEPENDENT">
		lbl_Chromosome.setText("  " + Text.Dialog.chromosome);
		lbl_Strand.setText("  " + Text.Dialog.strand);
		lbl_GTCode.setText("  " + Text.Dialog.genotypeEncoding);

		switch (org.gwaspi.constants.cImport.ImportFormat.compareTo(format)) {
			case BEAGLE:
				cmb_Chromosome.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.Chromosomes));
				cmb_Chromosome.setSelectedIndex(0);
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Chromosome, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Chromosome, c);
				rowNb++;
				break;
			default:
//				cmb_Chromosome.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.Chromosomes));
//				cmb_Chromosome.setSelectedIndex(0);
//
//				cmb_Strand.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.StrandType.values()));
//				cmb_Strand.setSelectedIndex(6);
//
//				cmb_GTCode.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.GenotypeCode.values()));
//				cmb_GTCode.setSelectedIndex(0);
//
//				setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//				pnl_Questions.add(lbl_Chromosome,c);
//				setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//				pnl_Questions.add(cmb_Chromosome,c);
//				rowNb++;
//				setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//				pnl_Questions.add(lbl_Strand,c);
//				setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//				pnl_Questions.add(cmb_Strand,c);
//				rowNb++;
//				setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//				pnl_Questions.add(lbl_GTCode,c);
//				setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//				pnl_Questions.add(cmb_GTCode,c);
//				rowNb++;
				break;
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
		chkB_MMM.setSelected(true);
		chkB_MMS.setSelected(true);
		chkB_MHZ.setSelected(false);
		chkB_SMS.setSelected(true);
		chkB_SHZ.setSelected(false);
		rdioB_HW_Calc.setSelected(true);

		chkB_MMM.setText(Text.Operation.discardMismatch);
		chkB_MMM.setEnabled(false);

		chkB_MMS.setText(Text.Operation.discardMarkerMissing);
		txtF_MMS.setText("0.050");

		chkB_MHZ.setText(Text.Operation.discardMarkerHetzy);
		txtF_MHZ.setText("1.000");
		chkB_MHZ.setEnabled(true);

		rdiogrp_HW.add(rdioB_HW_Calc);
		rdioB_HW_Calc.setText(Text.Operation.discardMarkerHWCalc1);
		lbl_HW.setText(Text.Operation.discardMarkerHWCalc2);
		rdiogrp_HW.add(rdioB_HW_free);
		rdioB_HW_free.setText(Text.Operation.discardMarkerHWFree);
		txtF_HW_free.setText("0.0000005");

//		// Listen for changes in the text
//		txtF_2.getDocument().addDocumentListener(new DocumentListener() {
//			public void changedUpdate(DocumentEvent e) {
//				rdioB_2.setEnabled(true);
//			}
//			public void removeUpdate(DocumentEvent e) {
//				rdioB_2.setEnabled(true);
//			}
//			public void insertUpdate(DocumentEvent e) {
//				rdioB_2.setEnabled(true);
//			}
//		});

		txtF_HW_free.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rdioB_HW_free.setEnabled(true);
			}
		});

		chkB_SMS.setText(Text.Operation.discardSampleMissing);
		try {
			txtF_SMS.setText(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SMS.setText("0.050");
		}


		chkB_SHZ.setText(Text.Operation.discardSampleHetzy);
		try {
			txtF_SHZ.setText(org.gwaspi.global.Config.getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SHZ.setText("1.000");
		}
		chkB_SHZ.setEnabled(true);

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

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_MMS, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_MMS, c);
		rowNb++;

//		setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//		pnl_Questions.add(chkB_MHZ,c);
//		setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//		pnl_Questions.add(txtF_MHZ,c);
//		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_MMM, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(new JLabel(""), c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_HW_Calc, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(lbl_HW, c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_HW_free, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_HW_free, c);
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
			Logger.getLogger(MoreGWASinOneGoInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static GWASinOneGOParams actionGo(ActionEvent evt) {
		if (!txtF_MMS.getText().isEmpty() && !txtF_HW_free.getText().isEmpty() && !txtF_SMS.getText().isEmpty()) {
			try {

				gwasParams.setPerformAllelicTests(chkB_allelic.isSelected());
				gwasParams.setPerformGenotypicTests(chkB_geno.isSelected());
				gwasParams.setPerformTrendTests(chkB_trend.isSelected());

				if (cmb_Chromosome.getSelectedItem() != null) {
					gwasParams.setChromosome(cmb_Chromosome.getSelectedItem().toString());
				}
				if (cmb_Strand.getSelectedItem() != null) {
					gwasParams.setStrandType(cmb_Strand.getSelectedItem().toString());
				}
				if (cmb_GTCode.getSelectedItem() != null) {
					gwasParams.setGtCode(cmb_GTCode.getSelectedItem().toString());
				}

				gwasParams.setDiscardGTMismatches(chkB_MMM.isSelected());
				gwasParams.setDiscardMarkerByMisRat(chkB_MMS.isSelected());
				gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(txtF_MMS.getText()));
//				gwasParams.discardMarkerByHetzyRat = chkB_MHZ.isSelected();
//				gwasParams.discardMarkerHetzyRatVal = Double.parseDouble(txtF_MHZ.getText());
				gwasParams.setDiscardMarkerHWCalc(rdioB_HW_Calc.isSelected());
				gwasParams.setDiscardMarkerHWFree(rdioB_HW_free.isSelected());
				gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(txtF_HW_free.getText()));
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
		return gwasParams;
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
