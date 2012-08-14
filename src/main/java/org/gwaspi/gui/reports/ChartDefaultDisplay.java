/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.gui.reports;

import org.gwaspi.global.Text;
import org.gwaspi.gui.MatrixAnalysePanel;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.gwaspi.model.Operation;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ChartDefaultDisplay extends javax.swing.JPanel {

    // Variables declaration - do not modify
    private static javax.swing.JPanel pnl_Chart;
    private static javax.swing.JPanel pnl_Footer;
    private static javax.swing.JScrollPane scrl_Chart;
    private static javax.swing.JButton btn_Save;
    private javax.swing.JButton btn_Back;

    private int opId;
    // End of variables declaration


    public ChartDefaultDisplay(final int studyId, final String chartPath, int _opId) {

        opId = _opId;

        scrl_Chart = new javax.swing.JScrollPane();
        pnl_Chart = new javax.swing.JPanel();
        pnl_Footer = new javax.swing.JPanel();
        btn_Save = new javax.swing.JButton();
        btn_Back = new javax.swing.JButton();

        //<editor-fold defaultstate="collapsed/expanded" desc="">

        btn_Save.setText(org.gwaspi.global.Text.All.save);
        btn_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionSaveAs(studyId, chartPath);
            }
        });

        btn_Back.setText(Text.All.Back);
        btn_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionBack(evt);
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 431, Short.MAX_VALUE)
                .addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_Save)
                .addComponent(btn_Back))
                );

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="LAYOUT">
        javax.swing.GroupLayout pnl_ChartLayout = new javax.swing.GroupLayout(pnl_Chart);
        pnl_Chart.setLayout(pnl_ChartLayout);
        pnl_ChartLayout.setHorizontalGroup(
                pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 819, Short.MAX_VALUE)
                .addGroup(pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ChartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
                .addContainerGap()))
                );
        pnl_ChartLayout.setVerticalGroup(
                pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 544, Short.MAX_VALUE)
                .addGroup(pnl_ChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ChartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addContainerGap()))
                );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Chart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Chart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );
        //</editor-fold>

        diplayChart(studyId, chartPath);
    }

    private static void diplayChart(int studyId, String chartPath){
        try {
            String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
            File testF = new File(reportPath+chartPath);
            if (testF.exists()) {
                Icon image = new ImageIcon(testF.getPath());
                JLabel label = new JLabel(image);
                // Creating a Scroll pane component
                scrl_Chart.getViewport().add(label);
                pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void actionSaveAs(int studyId, String chartPath) {
        try {
            String reportPath = org.gwaspi.global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
            File origFile = new File(reportPath+chartPath);
            File newFile = new File(org.gwaspi.gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath()+"/"+chartPath);
            if (origFile.exists()) {
                org.gwaspi.global.Utils.copyFile(origFile, newFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            //Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void actionBack(java.awt.event.ActionEvent evt) throws IOException {
        Operation op = new Operation(opId);
        org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
        org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(op.getParentMatrixId(), opId);
        org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
    }
}
