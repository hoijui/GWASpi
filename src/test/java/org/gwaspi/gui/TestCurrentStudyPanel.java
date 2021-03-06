/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.gui;

public class TestCurrentStudyPanel extends javax.swing.JPanel {

	/**
	 * Creates new form TestCurrentStudyPanel
	 */
	public TestCurrentStudyPanel() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="expanded" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_StudyDesc = new javax.swing.JPanel();
        scrl_Desc = new javax.swing.JScrollPane();
        txtA_Desc = new javax.swing.JTextArea();
        btn_SaveDesc = new javax.swing.JButton();
        btn_DeleteStudy = new javax.swing.JButton();
        btn_LoadGenotypes = new javax.swing.JButton();
        btn_UpdateSampleInfo = new javax.swing.JButton();
        pnl_MatrixTable = new javax.swing.JPanel();
        scrl_MatrixTable = new javax.swing.JScrollPane();
        tbl_MatrixTable = new javax.swing.JTable();
        btn_DeleteMatrix = new javax.swing.JButton();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();

        pnl_StudyDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Study: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

        txtA_Desc.setColumns(20);
        txtA_Desc.setRows(5);
        txtA_Desc.setBorder(javax.swing.BorderFactory.createTitledBorder("Description"));
        scrl_Desc.setViewportView(txtA_Desc);

        btn_SaveDesc.setText("Save Description");

        btn_DeleteStudy.setText("Delete Study");

        btn_LoadGenotypes.setText("Load Genotype Data");

        btn_UpdateSampleInfo.setText("Update Sample Info");

        javax.swing.GroupLayout pnl_StudyDescLayout = new javax.swing.GroupLayout(pnl_StudyDesc);
        pnl_StudyDesc.setLayout(pnl_StudyDescLayout);
        pnl_StudyDescLayout.setHorizontalGroup(
            pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrl_Desc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_StudyDescLayout.createSequentialGroup()
                        .addComponent(btn_DeleteStudy, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                        .addComponent(btn_LoadGenotypes, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_UpdateSampleInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_SaveDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnl_StudyDescLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});

        pnl_StudyDescLayout.setVerticalGroup(
            pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
                .addComponent(scrl_Desc, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_StudyDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_DeleteStudy)
                    .addComponent(btn_SaveDesc)
                    .addComponent(btn_UpdateSampleInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_LoadGenotypes, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pnl_StudyDescLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btn_LoadGenotypes, btn_SaveDesc, btn_UpdateSampleInfo});

        pnl_MatrixTable.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Matrices", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

        tbl_MatrixTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrl_MatrixTable.setViewportView(tbl_MatrixTable);

        btn_DeleteMatrix.setBackground(new java.awt.Color(242, 138, 121));
        btn_DeleteMatrix.setText("Delete Matrix");
        btn_DeleteMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DeleteMatrixActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_MatrixTableLayout = new javax.swing.GroupLayout(pnl_MatrixTable);
        pnl_MatrixTable.setLayout(pnl_MatrixTableLayout);
        pnl_MatrixTableLayout.setHorizontalGroup(
            pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_MatrixTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrl_MatrixTable, javax.swing.GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
                    .addComponent(btn_DeleteMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnl_MatrixTableLayout.setVerticalGroup(
            pnl_MatrixTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_MatrixTableLayout.createSequentialGroup()
                .addComponent(scrl_MatrixTable, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_DeleteMatrix)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btn_Back.setText("Back");

        btn_Help.setText("Help");

        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
            pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 652, Short.MAX_VALUE)
                .addComponent(btn_Help, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});

        pnl_FooterLayout.setVerticalGroup(
            pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Back)
                    .addComponent(btn_Help)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnl_MatrixTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnl_StudyDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_StudyDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_MatrixTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btn_DeleteMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DeleteMatrixActionPerformed
		// TODO add your handling code here:
    }//GEN-LAST:event_btn_DeleteMatrixActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_DeleteMatrix;
    private javax.swing.JButton btn_DeleteStudy;
    private javax.swing.JButton btn_Help;
    private javax.swing.JButton btn_LoadGenotypes;
    private javax.swing.JButton btn_SaveDesc;
    private javax.swing.JButton btn_UpdateSampleInfo;
    private javax.swing.JPanel pnl_Footer;
    private javax.swing.JPanel pnl_MatrixTable;
    private javax.swing.JPanel pnl_StudyDesc;
    private javax.swing.JScrollPane scrl_Desc;
    private javax.swing.JScrollPane scrl_MatrixTable;
    private javax.swing.JTable tbl_MatrixTable;
    private javax.swing.JTextArea txtA_Desc;
    // End of variables declaration//GEN-END:variables
}
