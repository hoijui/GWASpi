package org.gwaspi.gui;

/**
 *
 * @author u56124
 */
public class TestGWASpiExplorerPanel extends javax.swing.JPanel {

	/**
	 * Creates new form TestGWASpiExplorerPanel
	 */
	public TestGWASpiExplorerPanel() {
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

        splt_MOAPIPanel = new javax.swing.JSplitPane();
        scrl_Tree = new javax.swing.JScrollPane();
        scrl_Content = new javax.swing.JScrollPane();
        pnl_Content = new javax.swing.JPanel();

        scrl_Tree.setMinimumSize(new java.awt.Dimension(200, 600));
        scrl_Tree.setPreferredSize(new java.awt.Dimension(200, 600));
        splt_MOAPIPanel.setLeftComponent(scrl_Tree);

        javax.swing.GroupLayout pnl_ContentLayout = new javax.swing.GroupLayout(pnl_Content);
        pnl_Content.setLayout(pnl_ContentLayout);
        pnl_ContentLayout.setHorizontalGroup(
            pnl_ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 807, Short.MAX_VALUE)
        );
        pnl_ContentLayout.setVerticalGroup(
            pnl_ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );

        scrl_Content.setViewportView(pnl_Content);

        splt_MOAPIPanel.setRightComponent(scrl_Content);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splt_MOAPIPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1015, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splt_MOAPIPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnl_Content;
    private javax.swing.JScrollPane scrl_Content;
    private javax.swing.JScrollPane scrl_Tree;
    private javax.swing.JSplitPane splt_MOAPIPanel;
    // End of variables declaration//GEN-END:variables
}
