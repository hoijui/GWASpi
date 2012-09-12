package org.gwaspi.gui;

import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u56124
 */
public class GWASinOneGoDialog extends JFrame {

	private final static Logger log
			= LoggerFactory.getLogger(GWASinOneGoDialog.class);

	// Variables declaration - do not modify
	private static JButton btn_Go;
	private static JButton btn_Help;
	private static JCheckBox chkB_1;
	private static JCheckBox chkB_2;
	private static JCheckBox chkB_3;
	private static JRadioButton rdioB_1;
	private static JLabel lbl_1;
	private static JRadioButton rdioB_2;
	private static JTextField txtF_1;
	private static JTextField txtF_2;
	private static JTextField txtF_3;
	private static ButtonGroup rdiogrp_HW;
	private static JLabel lbl_Chromosome;
	private static JComboBox cmb_Chromosome;
	private static JLabel lbl_Strand;
	private static JComboBox cmb_Strand;
	private static JLabel lbl_GTCode;
	private static JComboBox cmb_GTCode;
	private static JDialog dialog;
	private static JFrame myFrame = new JFrame("GridBagLayout Test");
	private static String format = "";

	// End of variables declaration
	public static void main(String[] a) {



//        myFrame = new JFrame("GridBagLayout Test");
//        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        Container myPane = myFrame.getContentPane();
//        myPane.setLayout(new GridBagLayout());
//        GridBagConstraints c = new GridBagConstraints();
//        setMyConstraints(c,0,0,GridBagConstraints.CENTER);
//        myPane.add(getQuestionsPanel(),c);
//        setMyConstraints(c,0,1,GridBagConstraints.CENTER);
//        myPane.add(getFooterPanel(),c);
//        myFrame.pack();
//        myFrame.setVisible(true);

		// Create a modal dialog
		dialog = new JDialog(myFrame, "Alert", true);
		format = "BEAGLE";

		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container myPane = dialog.getContentPane();
		myPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setMyConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(getFooterPanel(), c);
		dialog.setSize(400, 400);
		dialog.pack();
		dialog.setVisible(true);

	}

	public static JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		chkB_1 = new JCheckBox();
		chkB_2 = new JCheckBox();
		chkB_3 = new JCheckBox();
		rdioB_1 = new JRadioButton();
		lbl_1 = new JLabel();
		lbl_Chromosome = new JLabel();
		lbl_Strand = new JLabel();
		lbl_GTCode = new JLabel();
		rdioB_2 = new JRadioButton();
		txtF_1 = new JTextField();
		txtF_2 = new JTextField();
		txtF_3 = new JTextField();
		rdiogrp_HW = new ButtonGroup();

		cmb_Chromosome = new JComboBox();
		cmb_Strand = new JComboBox();
		cmb_GTCode = new JComboBox();


		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;
		//<editor-fold defaultstate="collapsed" desc="FORMAT DEPENDANT">
		lbl_Chromosome.setText("  " + Text.Dialog.chromosome);
		cmb_Chromosome.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.Chromosomes));
		cmb_Chromosome.setSelectedIndex(0);

		lbl_Strand.setText("  " + Text.Dialog.strand);
		cmb_Strand.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.StrandType.values()));
		cmb_Strand.setSelectedIndex(6);

		lbl_GTCode.setText("  " + Text.Dialog.genotypeEncoding);
		cmb_GTCode.setModel(new DefaultComboBoxModel(cNetCDF.Defaults.GenotypeEncoding.values()));
		cmb_GTCode.setSelectedIndex(0);

		switch (cImport.ImportFormat.compareTo(format)) {
			case BEAGLE:
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Chromosome, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Chromosome, c);
				rowNb++;
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Strand, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Strand, c);
				rowNb++;
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_GTCode, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_GTCode, c);
				rowNb++;
				break;
			default:
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Strand, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Strand, c);
				rowNb++;
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_GTCode, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_GTCode, c);
				rowNb++;
				break;
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
		chkB_1.setSelected(true);
		chkB_2.setSelected(true);
		chkB_3.setSelected(true);

		chkB_1.setText(Text.Operation.discardMismatch);

		chkB_2.setText(Text.Operation.discardMarkerMissing);
		txtF_1.setText("0.05");

		rdiogrp_HW.add(rdioB_1);
		rdioB_1.setText(Text.Operation.discardMarkerHWCalc1);
		lbl_1.setText(Text.Operation.discardMarkerHWCalc2);
		rdiogrp_HW.add(rdioB_2);
		rdioB_2.setText(Text.Operation.discardMarkerHWFree);
		txtF_2.setText("0.0000005");

		chkB_3.setText(Text.Operation.discardSampleMissing);
		txtF_3.setText("0.05");



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
		pnl_Questions.add(rdioB_1, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(lbl_1, c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_2, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_2, c);
		rowNb++;

		setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_3, c);
		setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_3, c);
		//</editor-fold>

		pnl_Questions.setVisible(true);

		return pnl_Questions;
	}

	public static JPanel getFooterPanel() {

		JPanel pnl_Footer = new JPanel(new GridBagLayout());

		btn_Go = new JButton();
		btn_Help = new JButton();

		btn_Help.setText("Help");
		btn_Help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionHelp(evt);
			}
		});

		btn_Go.setText("Go");
		btn_Go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionGo(evt);
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Help, c);
		setMyConstraints(c, 1, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setMyConstraints(c, 2, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(btn_Go, c);

		pnl_Footer.setVisible(true);

		return pnl_Footer;
	}

	private static void actionHelp(ActionEvent evt) {
		try {
			URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.fileFormats);
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

	private static void actionGo(ActionEvent evt) {
		try {
//            parent.discardGTMismatches = chkB_1.isSelected();
//            parent.discardMarkerByMisRat = chkB_2.isSelected();
//            parent.discardMarkerMisRatVal = Integer.parseInt(txtF_1.getText());
//            parent.discardMarkerHWCalc = rdioB_1.isSelected();
//            parent.discardMarkerHWFree = rdioB_2.isSelected();
//            parent.discardMarkerHWTreshold = Integer.parseInt(txtF_2.getText());
//            parent.discardSampleByMisRat = chkB_3.isSelected();
//            parent.discardSampleMisRatVal = Integer.parseInt(txtF_3.getText());
		} catch (NumberFormatException numberFormatException) {
		}
		myFrame.setVisible(false);
	}

	private static void setMyConstraints(GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor) {
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}

	public static void showGWASInOneGo_Modal(String _format) {
		// Create a modal dialog
		dialog = new JDialog(myFrame, Text.Operation.gwasInOneGo, true);
		format = _format;

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

	}

	public static void showGWASInOneGo_NonModal(String _format) {
		format = _format;

		myFrame = new JFrame(Text.Operation.gwasInOneGo);
		myFrame.setSize(400, 400);
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container myPane = myFrame.getContentPane();
		myPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setMyConstraints(c, 0, 0, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setMyConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(getFooterPanel(), c);
		myFrame.pack();
		myFrame.setVisible(true);

	}
}
