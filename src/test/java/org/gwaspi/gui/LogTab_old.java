
package org.gwaspi.gui;

import java.io.IOException;


/**
 *
 * @author  u56124
 */
public class LogTab_old  extends javax.swing.JPanel {

    // Variables declaration - do not modify
    private javax.swing.JPanel panel_LogTab;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JTextArea textArea_log;
    // End of variables declaration

    @SuppressWarnings("unchecked")
    public LogTab_old() throws IOException {

        panel_LogTab = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        //textArea_log = new javax.swing.JTextArea(org.gwaspi.model.Study.getStudyLog().toString());

        textArea_log.setColumns(20);
        textArea_log.setRows(5);
        //textArea_log.setText(org.gwaspi.model.Study.getStudyLog().toString());
        jScrollPane1.setViewportView(textArea_log);

       javax.swing.GroupLayout panel_LogTabLayout = new javax.swing.GroupLayout(panel_LogTab);
        panel_LogTab.setLayout(panel_LogTabLayout);
        panel_LogTabLayout.setHorizontalGroup(
            panel_LogTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_LogTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 866, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel_LogTabLayout.setVerticalGroup(
            panel_LogTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_LogTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                .addContainerGap())
        );

                
        this.add(panel_LogTab);
        this.setVisible(true);
        
    }

    public static void refreshLogInfo() throws IOException{
        textArea_log.setText(null);
        //textArea_log.setText(org.gwaspi.model.Study.getStudyLog().toString());
    }
    
    public static void showLogTab() throws IOException{
//        org.gwaspi.gui.StartGUI.allTabs.setSelectedIndex(org.gwaspi.gui.StartGUI.allTabs.getTabCount()-1);
    }
    
}

