package org.gwaspi.gui;


import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF.Defaults.*;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;
import ucar.ma2.InvalidRangeException;



/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class LoadDataPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_File1;
    private javax.swing.JButton btn_File2;
    private javax.swing.JButton btn_FileSampleInfo;
    private javax.swing.JButton btn_Go;
    private javax.swing.JButton btn_Help;
    private javax.swing.JComboBox cmb_Format;
    private javax.swing.JLabel lbl_File1;
    private javax.swing.JLabel lbl_File2;
    private javax.swing.JLabel lbl_FileSampleInfo;
    private javax.swing.JLabel lbl_Format;
    private javax.swing.JLabel lbl_NewMatrixName;
    private javax.swing.JPanel pnl_Footer;
    public javax.swing.JPanel pnl_Input;
    private javax.swing.JPanel pnl_NameAndDesc;
    private javax.swing.JPanel pnl_Gif;
    private javax.swing.JPanel pnl_GifCenter;
    private javax.swing.JPanel pnl_GifLeft;
    private javax.swing.JPanel pnl_GifRight;
    private javax.swing.JScrollPane scrl_Gif;
    private javax.swing.JScrollPane scrl_NewMatrixDescription;
    private javax.swing.JTextArea txtA_NewMatrixDescription;
    private javax.swing.JTextField txt_File1;
    private javax.swing.JTextField txt_File2;
    private javax.swing.JTextField txt_FileSampleInfo;
    private javax.swing.JTextField txt_NewMatrixName;
    // End of variables declaration


    private boolean dummySamples=true;
    private int studyId;
    private boolean[] fieldObligatoryState;

    public GWASinOneGOParams gwasParams = new GWASinOneGOParams();
    // End of variables declaration

    @SuppressWarnings("unchecked")
    public LoadDataPanel(int _studyId) {

        studyId=_studyId;

        
        pnl_NameAndDesc = new javax.swing.JPanel();
        lbl_NewMatrixName = new javax.swing.JLabel();
        txt_NewMatrixName = new javax.swing.JTextField();
        scrl_NewMatrixDescription = new javax.swing.JScrollPane();
        txtA_NewMatrixDescription = new javax.swing.JTextArea();
        pnl_Input = new javax.swing.JPanel();
        lbl_Format = new javax.swing.JLabel();
        cmb_Format = new javax.swing.JComboBox();
        lbl_File1 = new javax.swing.JLabel();
        txt_File1 = new javax.swing.JTextField();
        btn_File1 = new javax.swing.JButton();
        lbl_File2 = new javax.swing.JLabel();
        txt_File2 = new javax.swing.JTextField();
        btn_File2 = new javax.swing.JButton();
        lbl_FileSampleInfo = new javax.swing.JLabel();
        txt_FileSampleInfo = new javax.swing.JTextField();
        btn_FileSampleInfo = new javax.swing.JButton();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Go = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();
        pnl_Gif = new javax.swing.JPanel();
        pnl_GifLeft = new javax.swing.JPanel();
        pnl_GifCenter = new javax.swing.JPanel();
        scrl_Gif = new javax.swing.JScrollPane();
        pnl_GifRight = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.importGenotypes, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

        pnl_NameAndDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.All.nameAndDescription, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
        txt_NewMatrixName.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(63));
        txt_NewMatrixName.requestFocus();
        txtA_NewMatrixDescription.setColumns(20);
        txtA_NewMatrixDescription.setLineWrap(true);
        txtA_NewMatrixDescription.setRows(5);
        txtA_NewMatrixDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.All.description));
        txtA_NewMatrixDescription.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(1999));
        txtA_NewMatrixDescription.setText(Text.All.optional);
        txtA_NewMatrixDescription.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        if(txtA_NewMatrixDescription.getText().equals(Text.All.optional)){
                            txtA_NewMatrixDescription.selectAll();
                        }
                    }
                });
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                            txtA_NewMatrixDescription.select(0,0);
                    }
                });
            }
        });
        scrl_NewMatrixDescription.setViewportView(txtA_NewMatrixDescription);


        //<editor-fold defaultstate="collapsed" desc="LAYOUT NAME & DESC">
        javax.swing.GroupLayout pnl_NameAndDescLayout = new javax.swing.GroupLayout(pnl_NameAndDesc);
        pnl_NameAndDesc.setLayout(pnl_NameAndDescLayout);
        pnl_NameAndDescLayout.setHorizontalGroup(
            pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NameAndDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrl_NewMatrixDescription, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
                    .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                        .addComponent(lbl_NewMatrixName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnl_NameAndDescLayout.setVerticalGroup(
            pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_NameAndDescLayout.createSequentialGroup()
                .addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_NewMatrixName)
                    .addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrl_NewMatrixDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        //</editor-fold>

        
        pnl_Input.setBorder(javax.swing.BorderFactory.createTitledBorder(null,Text.Matrix.input, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

        lbl_Format.setText(Text.Matrix.format);

        cmb_Format.setModel(new javax.swing.DefaultComboBoxModel(org.gwaspi.constants.cImport.ImportFormat.values()));
        cmb_Format.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmb_FormatActionPerformed();
            }
        });
        cmb_Format.actionPerformed(null);

        lbl_File1.setText(Text.All.file1);
        txt_File1.setEnabled(false);
        txt_File1.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        txt_File1.selectAll();
                    }
                });
            }
        });
        btn_File1.setText(Text.All.browse);
        btn_File1.setEnabled(false);
        btn_File1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionBrowse1(evt);
            }
        });
        
        
        lbl_File2.setText(Text.All.file2);
        txt_File2.setEnabled(false);
        txt_File2.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        txt_File2.selectAll();
                    }
                });
            }
        });
        btn_File2.setText(Text.All.browse);
        btn_File2.setEnabled(false);
        btn_File2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionBrowse2(evt);
            }
        });
        
        lbl_FileSampleInfo.setText(Text.All.file3);
        txt_FileSampleInfo.setEnabled(false);
        txt_FileSampleInfo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        txt_FileSampleInfo.selectAll();
                    }
                });
            }
        });

        btn_FileSampleInfo.setText(Text.All.browse);
        btn_FileSampleInfo.setEnabled(false);
        btn_FileSampleInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionBrowseSampleInfo(evt);
            }
        });

        
        //<editor-fold defaultstate="collapsed" desc="LAYOUT InputLayout">
        javax.swing.GroupLayout pnl_InputLayout = new javax.swing.GroupLayout(pnl_Input);
        pnl_Input.setLayout(pnl_InputLayout);
        pnl_InputLayout.setHorizontalGroup(
                pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_InputLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lbl_Format)
                .addComponent(lbl_File1)
                .addComponent(lbl_File2)
                .addComponent(lbl_FileSampleInfo))
                .addGap(130, 130, 130)
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(txt_File2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                .addGroup(pnl_InputLayout.createSequentialGroup()
                .addComponent(cmb_Format, 0, 294, Short.MAX_VALUE)
                .addGap(161, 161, 161))
                .addComponent(txt_File1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
                .addComponent(txt_FileSampleInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(btn_File1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_File2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_FileSampleInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );


        pnl_InputLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_File1, btn_File2, btn_FileSampleInfo});

        pnl_InputLayout.setVerticalGroup(
                pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_InputLayout.createSequentialGroup()
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lbl_Format)
                .addComponent(cmb_Format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(lbl_File1)
                .addComponent(txt_File1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_File1))
                .addGap(18, 18, 18)
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_File2)
                .addComponent(txt_File2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_File2))
                .addGap(18, 18, 18)
                .addGroup(pnl_InputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lbl_FileSampleInfo)
                .addComponent(txt_FileSampleInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_FileSampleInfo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        //</editor-fold>



        btn_Back.setText(Text.All.Back);
        btn_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_BackActionPerformed(evt);
            }
        });


        btn_Go.setText(Text.All.go);
        btn_Go.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionLoadGenotypes();
                } catch (InterruptedException ex) {

                    try {
                        org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.App.warnProcessInterrupted);
                        System.out.println(org.gwaspi.global.Text.App.warnProcessInterrupted);
                        
                        //DELETE BROKEN NEW MATRIX AND REPORTS
                        MatrixMetadata deleteMxMetaData = org.gwaspi.netCDF.matrices.MatrixManager.getLatestMatrixId();
                        if(deleteMxMetaData.getMatrixFriendlyName().equals(txt_NewMatrixName.getText())){
                            System.out.println("Deleting orphan files and references");
                            org.gwaspi.netCDF.matrices.MatrixManager.deleteMatrix(deleteMxMetaData.getMatrixId(), true);
                        }

                        org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
                    } catch (IOException ex1) {
                        Logger.getLogger(LoadDataPanel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    //Logger.getLogger(LoadDataPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    try {
                        ex.printStackTrace();
                        org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.All.warnLoadError+"\n"+global.Text.All.warnWrongFormat);
                        System.out.println(org.gwaspi.global.Text.All.warnLoadError);
                        System.out.println(org.gwaspi.global.Text.All.warnWrongFormat);

                        //DELETE BROKEN NEW MATRIX AND REPORTS
                        MatrixMetadata deleteMxMetaData = org.gwaspi.netCDF.matrices.MatrixManager.getLatestMatrixId();
                        if(deleteMxMetaData.getMatrixFriendlyName().equals(txt_NewMatrixName.getText())){
                            System.out.println("Deleting orphan files and references");
                            org.gwaspi.netCDF.matrices.MatrixManager.deleteMatrix(deleteMxMetaData.getMatrixId(), true);
                        }

                        org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
                    } catch (IOException ex1) {
                        Logger.getLogger(LoadDataPanel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    //Logger.getLogger(LoadDataPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        btn_Help.setText(Text.Help.help);
        btn_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionGWASHelp();
            }
        });

        
        //<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
            pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_Help)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 409, Short.MAX_VALUE)
                .addComponent(btn_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});

        pnl_FooterLayout.setVerticalGroup(
            pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_Back)
                    .addComponent(btn_Help)))
        );
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="LAYOUT GIF">
        javax.swing.GroupLayout pnl_GifLeftLayout = new javax.swing.GroupLayout(pnl_GifLeft);
//        pnl_GifLeft.setLayout(pnl_GifLeftLayout);
//        pnl_GifLeftLayout.setHorizontalGroup(
//                pnl_GifLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 308, Short.MAX_VALUE)
//                );
//        pnl_GifLeftLayout.setVerticalGroup(
//                pnl_GifLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 100, Short.MAX_VALUE)
//                );
//
//        javax.swing.GroupLayout pnl_GifCenterLayout = new javax.swing.GroupLayout(pnl_GifCenter);
//        pnl_GifCenter.setLayout(pnl_GifCenterLayout);
//        pnl_GifCenterLayout.setHorizontalGroup(
//                pnl_GifCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addComponent(scrl_Gif, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
//                );
//        pnl_GifCenterLayout.setVerticalGroup(
//                pnl_GifCenterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addComponent(scrl_Gif, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
//                );
//
//        javax.swing.GroupLayout pnl_GifRightLayout = new javax.swing.GroupLayout(pnl_GifRight);
//        pnl_GifRight.setLayout(pnl_GifRightLayout);
//        pnl_GifRightLayout.setHorizontalGroup(
//                pnl_GifRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 284, Short.MAX_VALUE)
//                );
//        pnl_GifRightLayout.setVerticalGroup(
//                pnl_GifRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 100, Short.MAX_VALUE)
//                );
//
//        javax.swing.GroupLayout pnl_GifLayout = new javax.swing.GroupLayout(pnl_Gif);
//        pnl_Gif.setLayout(pnl_GifLayout);
//        pnl_GifLayout.setHorizontalGroup(
//                pnl_GifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(pnl_GifLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(pnl_GifLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGap(18, 18, 18)
//                .addComponent(pnl_GifCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addGap(18, 18, 18)
//                .addComponent(pnl_GifRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGap(21, 21, 21))
//                );
//        pnl_GifLayout.setVerticalGroup(
//                pnl_GifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGroup(pnl_GifLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
//                .addComponent(pnl_GifRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addComponent(pnl_GifLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addComponent(pnl_GifCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                );
        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="LAYOUT">
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnl_Gif, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Input, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Input, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_Gif, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        //</editor-fold>

        cmb_Format.setSelectedIndex(2);
        cmb_FormatActionPerformed();
    }


        //<editor-fold defaultstate="collapsed" desc="SET FIELD NAMES & DEFAULTS">

        private void cmb_FormatActionPerformed() {
            lbl_File1.setForeground(Color.black);
            lbl_File2.setForeground(Color.black);
            lbl_FileSampleInfo.setForeground(Color.black);

            switch(cImport.ImportFormat.compareTo(cmb_Format.getSelectedItem().toString())){
                case Affymetrix_GenomeWide6:
                    fieldObligatoryState = new boolean[]{true,true,false};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.annotationFile);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText(Text.Matrix.genotypes+" "+Text.All.folder);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case PLINK:
                    fieldObligatoryState = new boolean[]{true,true,false};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.mapFile);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText(Text.Matrix.pedFile);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case PLINK_Binary:
                    fieldObligatoryState = new boolean[]{true,true,true};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.bedFile);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfoOrFam);
                    lbl_File2.setText(Text.Matrix.bimFile);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case HAPMAP:
                    fieldObligatoryState = new boolean[]{true,false,false};
                    lbl_File1.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File2.setEnabled(false);
                    lbl_File1.setText(Text.Matrix.genotypes);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText("");
                    txt_File1.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_File2.setEnabled(false);
                    txt_File2.setText("");
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    btn_File2.setEnabled(false);
                    break;
                case BEAGLE:
                    fieldObligatoryState = new boolean[]{true,true,false};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.genotypes);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText(Text.Matrix.markerFile);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case HGDP1:
                    fieldObligatoryState = new boolean[]{true,true,false};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.genotypes);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText(Text.Matrix.markerFile);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case GWASpi:
                    fieldObligatoryState = new boolean[]{true,false,true};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(false);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.matrix);
                    lbl_File2.setText("");
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(false);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_File2.setText("");
                    txt_FileSampleInfo.setText("");
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(false);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case Illumina_LGEN:
                    fieldObligatoryState = new boolean[]{true,true,false};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.mapFile);
                    lbl_File2.setText(Text.Matrix.lgenFile);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    txt_File2.setText("");
                    txt_FileSampleInfo.setText(Text.All.optional);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                case Sequenom:
                    fieldObligatoryState = new boolean[]{true,true,true};
                    lbl_File1.setEnabled(true);
                    lbl_File2.setEnabled(true);
                    lbl_FileSampleInfo.setEnabled(true);
                    lbl_File1.setText(Text.Matrix.genotypes);
                    lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
                    lbl_File2.setText(Text.Matrix.markerFile);
                    txt_File1.setEnabled(true);
                    txt_File2.setEnabled(true);
                    txt_FileSampleInfo.setEnabled(true);
                    btn_File1.setEnabled(true);
                    btn_File2.setEnabled(true);
                    btn_FileSampleInfo.setEnabled(true);
                    break;
                default:
                    fieldObligatoryState = new boolean[]{false,false,false};
                    lbl_File1.setEnabled(false);
                    lbl_File1.setText("");
                    lbl_File2.setEnabled(false);
                    lbl_File2.setText("");
                    lbl_FileSampleInfo.setEnabled(false);
                    lbl_FileSampleInfo.setText("");
                    txt_File1.setEnabled(false);
                    txt_File2.setEnabled(false);
                    txt_FileSampleInfo.setEnabled(false);
                    btn_File1.setEnabled(false);
                    btn_File2.setEnabled(false);
                    btn_FileSampleInfo.setEnabled(false);
            }
        }

        //</editor-fold>

        private void actionLoadGenotypes() throws IOException, FileNotFoundException, InvalidRangeException, InterruptedException {
            String newMatrixName = txt_NewMatrixName.getText().trim();
            if (!newMatrixName.isEmpty()) {
                lbl_NewMatrixName.setForeground(Color.black);
                boolean[] filesOK = validateFiles();
                if(filesOK[0]==true && filesOK[1]==true && filesOK[2]==true){
                    lbl_File1.setForeground(Color.black);
                    lbl_File2.setForeground(Color.black);
                    lbl_FileSampleInfo.setForeground(Color.black);
                    
                    File sampleInfoDir = new File(txt_FileSampleInfo.getText());
                    if (sampleInfoDir.isFile()) {
                        dummySamples = false;
                    }

                    int decision = JOptionPane.NO_OPTION;
                    decision = Dialogs.showOptionDialogue(Text.Matrix.gwasInOne, Text.Matrix.ifCaseCtrlDetected, Text.All.yes, Text.Matrix.noJustLoad, Text.All.cancel);
                    
                    if (decision == JOptionPane.YES_OPTION) {
                        //ASK MORE QUESTIONS
                        gwasParams = org.gwaspi.gui.utils.MoreGWASinOneGoInfo.showGWASInOneGo_Modal(cmb_Format.getSelectedItem().toString());
                        if (gwasParams.proceed == true) {
                            gwasParams.friendlyName = org.gwaspi.gui.utils.Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
                        }
                    } else if (decision != JOptionPane.CANCEL_OPTION){
                        gwasParams.proceed = true;
                    }

                    //<editor-fold defaultstate="collapsed" desc="DATA LOAD">
                    if(txtA_NewMatrixDescription.getText().equals(Text.All.optional)){
                        txtA_NewMatrixDescription.setText("");
                    }
                    if (gwasParams.proceed) {
                        //DO LOAD & GWAS
                        MultiOperations.loadMatrixDoGWASifOK(cmb_Format.getSelectedItem().toString(),
                                                          dummySamples,
                                                          decision,
                                                          newMatrixName,
                                                          txtA_NewMatrixDescription.getText(),
                                                          txt_File1.getText(),
                                                          txt_FileSampleInfo.getText(),
                                                          txt_File2.getText(),
                                                          gwasParams.chromosome, //Chr
                                                          gwasParams.strandType.toString(), //strandType
                                                          gwasParams.gtCode.toString(), //GtCode
                                                          studyId,
                                                          gwasParams);

                        org.gwaspi.gui.ProcessTab.showTab();
                    }
                    //</editor-fold>
                    
                }

                

                if(filesOK[0]==false){
                    lbl_File1.setForeground(Color.red);
                    Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField+lbl_File1.getText()+"!");
                }
                if(filesOK[1]==false){
                    lbl_File2.setForeground(Color.red);
                    Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField+lbl_File2.getText()+"!");
                }
                if(filesOK[2]==false){
                    lbl_FileSampleInfo.setForeground(Color.red);
                    Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField+lbl_FileSampleInfo.getText()+"!");
                }
            } else {
                lbl_NewMatrixName.setForeground(Color.red);
                setCursor(org.gwaspi.gui.utils.CursorUtils.defaultCursor);
                Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
            }

        }


        //<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
        private void actionBrowse1(java.awt.event.ActionEvent evt) {
            //CHECK IF HOMONYM .PED FILE EXISTS IN PLINK CASE
            if(cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK)){
                //Use standard file opener
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File1, txt_File1, "");
                if (!txt_File1.getText().isEmpty()) {
                    File pedFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".ped");
                    if (txt_File2.getText().isEmpty() && pedFile.exists()) {
                        txt_File2.setText(pedFile.getPath());
                    } else {
                        if (pedFile.exists()) {
                            int option = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(org.gwaspi.global.Text.Matrix.findComplementaryPlink);
                            if (option == JOptionPane.YES_OPTION) {
                                txt_File2.setText(pedFile.getPath());
                            }
                        }
                    }
                }
            } else if(cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK_Binary)){
                //Use standard file opener
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File1, txt_File1, "");
                if (!txt_File1.getText().isEmpty()) {
                    File bimFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".bim");
                    File famFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".fam");
                    int option = -1;
                    if (txt_File2.getText().isEmpty() && bimFile.exists()) {
                        txt_File2.setText(bimFile.getPath());
                    } else {
                        if (bimFile.exists()) {
                            option = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(org.gwaspi.global.Text.Matrix.findComplementaryPlinkBinary);
                            if (option == JOptionPane.YES_OPTION) {
                                txt_File2.setText(bimFile.getPath());
                            }
                        }
                    }
                    if (txt_FileSampleInfo.getText().isEmpty() && famFile.exists()) {
                        txt_FileSampleInfo.setText(famFile.getPath());
                    } else {
                        if (famFile.exists()) {
                            if (option == JOptionPane.YES_OPTION) {
                                txt_FileSampleInfo.setText(famFile.getPath());
                            }
                        }
                    }
                }
            } else if(cmb_Format.getSelectedItem().equals(cImport.ImportFormat.Sequenom)){
                //Use directory selector
                org.gwaspi.gui.utils.Dialogs.selectAndSetDirectoryDialogue(evt, btn_File1, txt_File1, "", ""); //only dirs
            } else {
                //Use standard file opener
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File1, txt_File1, "");
            }

        }

        private void actionBrowse2(java.awt.event.ActionEvent evt) {
            //Use standard file opener
            if(cmb_Format.getSelectedItem().equals(org.gwaspi.constants.cImport.ImportFormat.PLINK)){
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File2, txt_File2, "");
                if (!txt_File2.getText().isEmpty()) {
                    File mapFile = new File(txt_File2.getText().substring(0, txt_File2.getText().length() - 4) + ".map");
                    if (txt_File1.getText().isEmpty() && mapFile.exists()) {
                        txt_File1.setText(mapFile.getPath());
                    } else {
                        if (mapFile.exists()) {
                            int option = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(org.gwaspi.global.Text.Matrix.findComplementaryPlink);
                            if (option == JOptionPane.YES_OPTION) {
                                txt_File1.setText(mapFile.getPath());
                            }
                        }
                    }
                }
            } else if(cmb_Format.getSelectedItem().equals(org.gwaspi.constants.cImport.ImportFormat.PLINK_Binary)){
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File2, txt_File2, "");
                if (!txt_File2.getText().isEmpty()) {
                    File bedFile = new File(txt_File2.getText().substring(0, txt_File2.getText().length() - 4) + ".bed");
                    File famFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".fam");
                    int option = -1;
                    if (txt_File1.getText().isEmpty() && bedFile.exists()) {
                        txt_File1.setText(bedFile.getPath());
                    } else {
                        if (bedFile.exists()) {
                            option = org.gwaspi.gui.utils.Dialogs.showConfirmDialogue(org.gwaspi.global.Text.Matrix.findComplementaryPlinkBinary);
                            if (option == JOptionPane.YES_OPTION) {
                                txt_File1.setText(bedFile.getPath());
                            }
                        }
                    }
                    if (txt_FileSampleInfo.getText().isEmpty() && famFile.exists()) {
                        txt_FileSampleInfo.setText(famFile.getPath());
                    } else {
                        if (famFile.exists()) {
                            if (option == JOptionPane.YES_OPTION) {
                                txt_FileSampleInfo.setText(famFile.getPath());
                            }
                        }
                    }
                }
            } else if(cmb_Format.getSelectedItem().equals(org.gwaspi.constants.cImport.ImportFormat.BEAGLE)) {
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File2, txt_File2, "");
            } else if(cmb_Format.getSelectedItem().equals(org.gwaspi.constants.cImport.ImportFormat.HGDP1)) {
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File2, txt_File2, "");
            } else if(cmb_Format.getSelectedItem().equals(cImport.ImportFormat.Sequenom)){
                org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_File2, txt_File2, "");
            } else {
                org.gwaspi.gui.utils.Dialogs.selectAndSetDirectoryDialogue(evt, btn_File2, txt_File2, "", ""); //only dirs
            }
        }

        private void actionBrowseSampleInfo(java.awt.event.ActionEvent evt) {
            //Use standard file opener
            org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_FileSampleInfo, txt_FileSampleInfo, "");
        }

        private boolean[] validateFiles(){
            lbl_File1.setForeground(Color.black);
            lbl_File2.setForeground(Color.black);
            lbl_FileSampleInfo.setForeground(Color.black);
            
            boolean[] buttonsOK = new boolean[]{false, false, false};

            File file1 = new File(txt_File1.getText());
            File file2 = new File(txt_File2.getText());
            File file3 = new File(txt_FileSampleInfo.getText());

            if(txt_File1.isEnabled()){
                if (file1.exists()) {
                    buttonsOK[0] = true;
                } else {
                    if(fieldObligatoryState[0]==false){
                        if(txt_File1.getText().contains(org.gwaspi.global.Text.All.optional) ||
                           txt_File1.getText().isEmpty()){
                            buttonsOK[0] = true;
                        }
                    } else {
                        buttonsOK[0] = false;
                    }
                }
            } else {
                buttonsOK[0] = true;
            }
            if(txt_File2.isEnabled()){
                if(file2.exists()){
                    buttonsOK[1] = true;
                } else {
                    if(fieldObligatoryState[1]==false){
                        if(txt_File2.getText().contains(org.gwaspi.global.Text.All.optional) ||
                           txt_File2.getText().isEmpty()){
                            buttonsOK[1] = true;
                        }
                    } else {
                        buttonsOK[1] = false;
                    }
                }
            } else {
                buttonsOK[1] = true;
            }
            if (txt_FileSampleInfo.isEnabled()) {
                if (file3.exists()) {
                    buttonsOK[2] = true;
                } else {
                    if(fieldObligatoryState[2]==false){
                        if(txt_FileSampleInfo.getText().contains(org.gwaspi.global.Text.All.optional) ||
                           txt_FileSampleInfo.getText().isEmpty()){
                            buttonsOK[2] = true;
                        }
                    } else {
                        buttonsOK[2] = false;
                    }
                }
            } else {
                buttonsOK[2] = true;
            }

            return buttonsOK;
        }

        //</editor-fold>


        private void btn_BackActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentStudyPanel(studyId);
                org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
            } catch (IOException ex) {
                Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private void actionGWASHelp() {
            try {
                org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.loadGts);
            } catch (IOException ex) {
                Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }





}
