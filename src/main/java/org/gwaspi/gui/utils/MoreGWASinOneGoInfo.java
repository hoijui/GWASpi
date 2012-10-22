package org.gwaspi.gui.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MoreGWASinOneGoInfo extends JFrame {

	private final Logger log = LoggerFactory.getLogger(MoreGWASinOneGoInfo.class);

	// Variables declaration - do not modify
	private JButton btn_Go;
	private JButton btn_Help;
	private JButton btn_Cancel;
	private JCheckBox chkB_allelic;
	private JCheckBox chkB_geno;
	private JCheckBox chkB_trend;
	private JCheckBox chkB_MMM;
	private JCheckBox chkB_MMS;
	private JCheckBox chkB_MHZ;
	private JCheckBox chkB_SMS;
	private JCheckBox chkB_SHZ;
	private JRadioButton rdioB_HW_Calc;
	private JLabel lbl_HW;
	private JRadioButton rdioB_HW_free;
	private JTextField txtF_MMS;
	private JTextField txtF_MHZ;
	private JTextField txtF_HW_free;
	private JTextField txtF_SMS;
	private JTextField txtF_SHZ;
	private ButtonGroup rdiogrp_HW;
	private JLabel lbl_Chromosome;
	private JComboBox cmb_Chromosome;
	private JLabel lbl_Strand;
	private JComboBox cmb_Strand;
	private JLabel lbl_GTCode;
	private JComboBox cmb_GTCode;
	private JFrame myFrame = new JFrame("GridBagLayout Test");
	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private JDialog dialog;
	private String format = "";
	// End of variables declaration

	public GWASinOneGOParams showMoreInfo(String _format) {
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
		setConstraints(c, 0, 0, GridBagConstraints.CENTER);
		myPane.add(getHeaderPanel(), c);
		setConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setConstraints(c, 0, 2, GridBagConstraints.CENTER);
		myPane.add(getFooterPanel(), c);
		dialog.pack();
		dialog.setVisible(true);

		return gwasParams;
	}

	private JPanel getHeaderPanel() {

		JPanel pnl_Header = new JPanel(new GridBagLayout());
		pnl_Header.setBorder(BorderFactory.createTitledBorder("Analysis to perform"));

		chkB_allelic = new JCheckBox();
		chkB_allelic.setText("  " + Text.Operation.performAllelicTests + "  ");
		chkB_allelic.setSelected(true);
		chkB_geno = new JCheckBox();
		chkB_geno.setText("  " + Text.Operation.performGenotypicTests + "  ");
		chkB_geno.setSelected(true);
		chkB_trend = new JCheckBox();
		chkB_trend.setText("  " + Text.Operation.performTrendTests + "  ");
		chkB_trend.setSelected(true);

		GridBagConstraints c = new GridBagConstraints();
		setConstraints(c, 0, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("      "), c);
		setConstraints(c, 1, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_allelic, c);
		setConstraints(c, 2, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("    "), c);
		setConstraints(c, 3, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_geno, c);
		pnl_Header.add(new JLabel("        "), c);
		setConstraints(c, 4, 0, GridBagConstraints.LINE_START);
		pnl_Header.add(chkB_trend, c);
		setConstraints(c, 5, 0, GridBagConstraints.LINE_END);
		pnl_Header.add(new JLabel("      "), c);

		pnl_Header.setVisible(true);
		return pnl_Header;
	}

	private JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		chkB_MMM = new JCheckBox();
		chkB_MMS = new JCheckBox();
		chkB_MHZ = new JCheckBox();
		chkB_SMS = new JCheckBox();
		chkB_SHZ = new JCheckBox();
		rdioB_HW_Calc = new JRadioButton();
		lbl_HW = new JLabel();
		lbl_Chromosome = new JLabel();
		lbl_Strand = new JLabel();
		lbl_GTCode = new JLabel();
		rdioB_HW_free = new JRadioButton();
		txtF_MMS = new JTextField();
		txtF_MMS.setInputVerifier(new DoubleInputVerifier());
		txtF_MHZ = new JTextField();
		txtF_MHZ.setInputVerifier(new DoubleInputVerifier());
		txtF_HW_free = new JTextField();
		txtF_HW_free.setInputVerifier(new DoubleInputVerifier());
		txtF_SMS = new JTextField();
		txtF_SMS.setInputVerifier(new DoubleInputVerifier());
		txtF_SHZ = new JTextField();
		txtF_SHZ.setInputVerifier(new DoubleInputVerifier());
		rdiogrp_HW = new ButtonGroup();

		cmb_Chromosome = new JComboBox();
		cmb_Strand = new JComboBox();
		cmb_GTCode = new JComboBox();

		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;

		//<editor-fold defaultstate="collapsed" desc="FORMAT DEPENDENT">
		lbl_Chromosome.setText("  " + Text.Dialog.chromosome);
		lbl_Strand.setText("  " + Text.Dialog.strand);
		lbl_GTCode.setText("  " + Text.Dialog.genotypeEncoding);

		switch (cImport.ImportFormat.compareTo(format)) {
			case BEAGLE:
				cmb_Chromosome.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.Chromosomes));
				cmb_Chromosome.setSelectedIndex(0);
				setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Chromosome, c);
				setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Chromosome, c);
				rowNb++;
				break;
			default:
//				cmb_Chromosome.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.Chromosomes));
//				cmb_Chromosome.setSelectedIndex(0);
//
//				cmb_Strand.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.StrandType.values()));
//				cmb_Strand.setSelectedIndex(6);
//
//				cmb_GTCode.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.GenotypeCode.values()));
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

		txtF_HW_free.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				rdioB_HW_free.setEnabled(true);
			}
		});

		chkB_SMS.setText(Text.Operation.discardSampleMissing);
		try {
			txtF_SMS.setText(Config.getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SMS.setText("0.050");
		}

		chkB_SHZ.setText(Text.Operation.discardSampleHetzy);
		try {
			txtF_SHZ.setText(Config.getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SHZ.setText("1.000");
		}
		chkB_SHZ.setEnabled(true);

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SMS, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SMS, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SHZ, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SHZ, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_MMS, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_MMS, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_MMM, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(new JLabel(""), c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_HW_Calc, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(lbl_HW, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_HW_free, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_HW_free, c);
		//</editor-fold>

		pnl_Questions.setVisible(true);

		return pnl_Questions;
	}

	private JPanel getFooterPanel() {

		JPanel pnl_Footer = new JPanel(new GridBagLayout());

		btn_Go = new JButton();
		btn_Help = new JButton();
		btn_Cancel = new JButton();

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.GWASinOneGo));

		btn_Go.setAction(new GoAction());

		btn_Cancel.setAction(new CancelAction());

		GridBagConstraints c = new GridBagConstraints();
		setConstraints(c, 0, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Cancel, c);
		setConstraints(c, 1, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setConstraints(c, 2, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Help, c);
		setConstraints(c, 3, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setConstraints(c, 4, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(btn_Go, c);

		pnl_Footer.setVisible(true);

		return pnl_Footer;
	}

	private class GoAction extends AbstractAction {

		GoAction() {

			putValue(NAME, Text.All.go);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (!txtF_MMS.getText().isEmpty() && !txtF_HW_free.getText().isEmpty() && !txtF_SMS.getText().isEmpty()) {
				try {

					gwasParams.setPerformAllelicTests(chkB_allelic.isSelected());
					gwasParams.setPerformGenotypicTests(chkB_geno.isSelected());
					gwasParams.setPerformTrendTests(chkB_trend.isSelected());

					if (cmb_Chromosome.getSelectedItem() != null) {
						gwasParams.setChromosome(cmb_Chromosome.getSelectedItem().toString());
					}
					if (cmb_Strand.getSelectedItem() != null) {
						gwasParams.setStrandType((StrandType) cmb_Strand.getSelectedItem());
					}
					if (cmb_GTCode.getSelectedItem() != null) {
						gwasParams.setGtCode((GenotypeEncoding) cmb_GTCode.getSelectedItem());
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
				} catch (NumberFormatException ex) {
					log.warn(null, ex);
				}
				dialog.dispose();
			} else {
				gwasParams.setProceed(false);
			}
		}
	}

	private class CancelAction extends AbstractAction {

		CancelAction() {

			putValue(NAME, Text.All.cancel);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			dialog.setVisible(false);
		}
	}

	private void setConstraints(GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor)
	{
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}
}
