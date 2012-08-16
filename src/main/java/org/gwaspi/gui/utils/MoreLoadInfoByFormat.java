/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class MoreLoadInfoByFormat extends javax.swing.JFrame {

	// Variables declaration - do not modify
	private static javax.swing.JButton btn_Go;
	private static javax.swing.JButton btn_Help;
	private static javax.swing.JButton btn_Cancel;
//    private static javax.swing.JCheckBox chkB_1;
//    private static javax.swing.JCheckBox chkB_2;
//    private static javax.swing.JCheckBox chkB_3;
//    private static javax.swing.JRadioButton rdioB_1;
//    private static javax.swing.JRadioButton rdioB_2;
//    private static javax.swing.JTextField txtF_1;
//    private static javax.swing.JTextField txtF_2;
//    private static javax.swing.JTextField txtF_3;
	private static javax.swing.JLabel lbl_Chromosome;
	private static javax.swing.JComboBox cmb_Chromosome;
	private static javax.swing.JLabel lbl_Strand;
	private static javax.swing.JComboBox cmb_Strand;
	private static javax.swing.JLabel lbl_GTCode;
	private static javax.swing.JComboBox cmb_GTCode;
	private static JFrame myFrame = new JFrame("GridBagLayout Test");
	private static JDialog dialog;
	public static GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private static String format = "";

	// End of variables declaration
	public static GWASinOneGOParams showMoreInfoByFormat_Modal(String _format) {

		gwasParams.proceed = false;
		format = _format;

		switch (org.gwaspi.constants.cImport.ImportFormat.compareTo(format)) {
			case Affymetrix_GenomeWide6:
				gwasParams.proceed = true;
				break;
			default:
				// Create a modal dialog
				dialog = new JDialog(myFrame, "Additional Info", true);
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
				myPane.add(getQuestionsPanel(), c);
				setMyConstraints(c, 0, 1, GridBagConstraints.CENTER);
				myPane.add(getFooterPanel(), c);
				dialog.pack();
				dialog.setVisible(true);


		}

		return gwasParams;

	}

	public static JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("Additional Information..."));

		lbl_Chromosome = new javax.swing.JLabel();
		lbl_Strand = new javax.swing.JLabel();
		lbl_GTCode = new javax.swing.JLabel();

		cmb_Chromosome = new javax.swing.JComboBox();
		cmb_Strand = new javax.swing.JComboBox();
		cmb_GTCode = new javax.swing.JComboBox();


		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;
		//<editor-fold defaultstate="collapsed" desc="FORMAT DEPENDENT">
		lbl_Chromosome.setText("  " + Text.Dialog.chromosome + "  ");
		lbl_Strand.setText("  " + Text.Dialog.strand + "  ");
		lbl_GTCode.setText("  " + Text.Dialog.genotypeEncoding + "  ");

		switch (org.gwaspi.constants.cImport.ImportFormat.compareTo(format)) {
//            case default:
//                cmb_Chromosome.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.Chromosomes));
//                cmb_Chromosome.setSelectedIndex(0);
//                cmb_Strand.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.StrandType.values()));
//                cmb_Strand.setSelectedIndex(6);
//                cmb_GTCode.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.GenotypeCode.values()));
//                cmb_GTCode.setSelectedIndex(0);
//
//                setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//                pnl_Questions.add(lbl_Chromosome,c);
//                setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//                pnl_Questions.add(cmb_Chromosome,c);
//                rowNb++;
//                setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//                pnl_Questions.add(lbl_Strand,c);
//                setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//                pnl_Questions.add(cmb_Strand,c);
//                rowNb++;
//                setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
//                pnl_Questions.add(lbl_GTCode,c);
//                setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
//                pnl_Questions.add(cmb_GTCode,c);
//                rowNb++;
//                break;
			case BEAGLE:
				cmb_Chromosome.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cNetCDF.Defaults.Chromosomes));
				cmb_Chromosome.setSelectedIndex(0);
				setMyConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
				pnl_Questions.add(lbl_Chromosome, c);
				setMyConstraints(c, 1, rowNb, GridBagConstraints.WEST);
				pnl_Questions.add(cmb_Chromosome, c);
				rowNb++;
				break;
		}
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

//        GridBagConstraints c = new GridBagConstraints();
//        setMyConstraints(c,0,0,GridBagConstraints.LINE_START);
//        pnl_Footer.add(btn_Help,c);
//        setMyConstraints(c,1,0,GridBagConstraints.LINE_END);
//        pnl_Footer.add(new JLabel("    "),c);
//        setMyConstraints(c,2,0,GridBagConstraints.LINE_END);
//        pnl_Footer.add(btn_Go,c);

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
			Logger.getLogger(MoreLoadInfoByFormat.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void actionGo(ActionEvent evt) {
		try {
			if (cmb_Chromosome.getSelectedItem() != null) {
				gwasParams.chromosome = cmb_Chromosome.getSelectedItem().toString();
			}
			if (cmb_Strand.getSelectedItem() != null) {
				gwasParams.strandType = cmb_Strand.getSelectedItem().toString();
			}
			if (cmb_GTCode.getSelectedItem() != null) {
				gwasParams.gtCode = cmb_GTCode.getSelectedItem().toString();
			}
			gwasParams.proceed = true;

		} catch (NumberFormatException numberFormatException) {
		}
		dialog.dispose();
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

	public static void showMoreInfoByFormat(String toString) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
