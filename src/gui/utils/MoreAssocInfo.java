/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.utils;

import global.Text;
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
import netCDF.operations.GWASinOneGOParams;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MoreAssocInfo extends javax.swing.JFrame {


    // Variables declaration - do not modify
    private static javax.swing.JButton btn_Go;
    private static javax.swing.JButton btn_Help;
    private static javax.swing.JButton btn_Cancel;
    private static javax.swing.JRadioButton rdioB_1;
    private static javax.swing.JLabel lbl_1;
    private static javax.swing.JRadioButton rdioB_2;
    private static javax.swing.JTextField txtF_1;
    private static javax.swing.JTextField txtF_2;
    private static javax.swing.JTextField txtF_3;
    private static javax.swing.ButtonGroup rdiogrp_HW;

    private static JFrame myFrame = new JFrame("GridBagLayout Test");
    public static GWASinOneGOParams gwasParams = new GWASinOneGOParams();
    private static JDialog dialog;

    // End of variables declaration

    public static GWASinOneGOParams showAssocInfo_Modal(){
        gwasParams.proceed=false;
        // Create a modal dialog
        dialog = new JDialog(myFrame, Text.Operation.gwasInOneGo, true);

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        dialog.setLocation(screenWidth / 4, screenHeight / 4);

        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container myPane = dialog.getContentPane();
        myPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        setMyConstraints(c,0,0,GridBagConstraints.CENTER);
        myPane.add(getQuestionsPanel(),c);
        setMyConstraints(c,0,1,GridBagConstraints.CENTER);
        myPane.add(getFooterPanel(),c);
        dialog.pack();
        dialog.setVisible(true);

        return gwasParams;
    }





    public static JPanel getQuestionsPanel() {

        JPanel pnl_Questions = new JPanel(new GridBagLayout());
        pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

        rdioB_1 = new javax.swing.JRadioButton();
        lbl_1 = new javax.swing.JLabel();
        rdioB_2 = new javax.swing.JRadioButton();
        txtF_1 = new javax.swing.JTextField();
        txtF_1.setInputVerifier(new gui.utils.DoubleInputVerifier());
        txtF_2 = new javax.swing.JTextField();
        txtF_2.setInputVerifier(new gui.utils.DoubleInputVerifier());
        txtF_3 = new javax.swing.JTextField();
        txtF_3.setInputVerifier(new gui.utils.DoubleInputVerifier());
        rdiogrp_HW = new javax.swing.ButtonGroup();



        GridBagConstraints c = new GridBagConstraints();
        int rowNb=0;

        //<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
        rdioB_1.setSelected(true);
        rdiogrp_HW.add(rdioB_1);
        rdioB_1.setText(Text.Operation.discardMarkerHWCalc1);
        lbl_1.setText(Text.Operation.discardMarkerHWCalc2);
        rdiogrp_HW.add(rdioB_2);
        rdioB_2.setText(Text.Operation.discardMarkerHWFree);
        txtF_2.setText("0.0000005");

        setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
        pnl_Questions.add(rdioB_1,c);
        setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
        pnl_Questions.add(lbl_1,c);
        rowNb++;

        setMyConstraints(c,0,rowNb,GridBagConstraints.LINE_START);
        pnl_Questions.add(rdioB_2,c);
        setMyConstraints(c,1,rowNb,GridBagConstraints.WEST);
        pnl_Questions.add(txtF_2,c);
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

        btn_Help.setText("  "+Text.Help.help+"  ");
        btn_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionHelp(evt);
            }
        });

        btn_Go.setText("   "+Text.All.go+"   ");
        btn_Go.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionGo(evt);
            }
        });

        btn_Cancel.setText("   "+Text.All.cancel+"   ");
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
        setMyConstraints(c,0,0,GridBagConstraints.LINE_START);
        pnl_Footer.add(btn_Cancel,c);
        setMyConstraints(c,1,0,GridBagConstraints.LINE_END);
        pnl_Footer.add(new JLabel("    "),c);
        setMyConstraints(c,2,0,GridBagConstraints.LINE_START);
        pnl_Footer.add(btn_Help,c);
        setMyConstraints(c,3,0,GridBagConstraints.LINE_END);
        pnl_Footer.add(new JLabel("    "),c);
        setMyConstraints(c,4,0,GridBagConstraints.LINE_END);
        pnl_Footer.add(btn_Go,c);

        pnl_Footer.setVisible(true);

        return pnl_Footer;
    }


    private static void actionHelp(ActionEvent evt) {
        try {
            gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.GWASinOneGo);
        } catch (IOException ex) {
            Logger.getLogger(MoreAssocInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static GWASinOneGOParams actionGo(ActionEvent evt) {
        if(!txtF_2.getText().isEmpty()){
            try {
                gwasParams.discardMarkerHWCalc = rdioB_1.isSelected();
                gwasParams.discardMarkerHWFree = rdioB_2.isSelected();
                gwasParams.discardMarkerHWTreshold = Double.parseDouble(txtF_2.getText());
                gwasParams.proceed = true;
            } catch (NumberFormatException numberFormatException) {
            }
            dialog.dispose();
        } else {
            gwasParams.proceed=false;
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
