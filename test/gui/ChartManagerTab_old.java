/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.event.ActionEvent;

/**
 *
 * @author u56124
 */
public class ChartManagerTab_old extends javax.swing.JPanel {
    
    // Variables declaration - do not modify
    private javax.swing.JButton button_ReportFile;
    private javax.swing.JButton button_SaveCompleteChart;
    private javax.swing.JButton button_SaveZoomedChart;
    private javax.swing.JComboBox combo_SelectPlot;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labe_SelectPlot;
    private javax.swing.JLabel label_ReportFile;
    private javax.swing.JPanel panel_ChartTab;
    private javax.swing.JPanel panel_Plot;
    private javax.swing.JTextField textField_ReportFile;
    // End of variables declaration
    
    public ChartManagerTab_old() {

        panel_ChartTab = new javax.swing.JPanel();
        labe_SelectPlot = new javax.swing.JLabel();
        combo_SelectPlot = new javax.swing.JComboBox();
        label_ReportFile = new javax.swing.JLabel();
        textField_ReportFile = new javax.swing.JTextField();
        button_ReportFile = new javax.swing.JButton();
        button_SaveCompleteChart = new javax.swing.JButton();
        button_SaveZoomedChart = new javax.swing.JButton();
        panel_Plot = new javax.swing.JPanel();


        labe_SelectPlot.setText("Select Plot type:");
        combo_SelectPlot.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-Log(p) / position", "Chi² Q/Q plot" }));

        label_ReportFile.setText("Report File:");
        button_ReportFile.setText("Browse");

        button_SaveCompleteChart.setText("Save Complete Chart");
        button_SaveCompleteChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_SaveCompleteChartActionPerformed(evt);
            }
        });

        button_SaveZoomedChart.setText("Save Zoomed Chart");

        javax.swing.GroupLayout panel_PlotLayout = new javax.swing.GroupLayout(panel_Plot);
        panel_Plot.setLayout(panel_PlotLayout);
        panel_PlotLayout.setHorizontalGroup(
            panel_PlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 866, Short.MAX_VALUE)
        );
        panel_PlotLayout.setVerticalGroup(
            panel_PlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 401, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panel_ChartTabLayout = new javax.swing.GroupLayout(panel_ChartTab);
        panel_ChartTab.setLayout(panel_ChartTabLayout);
        panel_ChartTabLayout.setHorizontalGroup(
            panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ChartTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panel_Plot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panel_ChartTabLayout.createSequentialGroup()
                        .addComponent(button_SaveZoomedChart)
                        .addGap(18, 18, 18)
                        .addComponent(button_SaveCompleteChart))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel_ChartTabLayout.createSequentialGroup()
                        .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labe_SelectPlot)
                            .addComponent(label_ReportFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(combo_SelectPlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ChartTabLayout.createSequentialGroup()
                                .addComponent(textField_ReportFile, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(button_ReportFile)))))
                .addContainerGap())
        );
        panel_ChartTabLayout.setVerticalGroup(
            panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ChartTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labe_SelectPlot)
                    .addComponent(combo_SelectPlot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_ReportFile)
                    .addComponent(textField_ReportFile, 26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_ReportFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panel_Plot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(panel_ChartTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_SaveCompleteChart)
                    .addComponent(button_SaveZoomedChart))
                .addContainerGap())
        );
        
        this.add(panel_ChartTab);
        this.setVisible(true);
    }// </editor-fold>
    
    
    
    private void button_SaveCompleteChartActionPerformed(ActionEvent evt) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
