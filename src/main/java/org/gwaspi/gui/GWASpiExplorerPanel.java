package org.gwaspi.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.gwaspi.model.GWASpiExplorerNodes;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiExplorerPanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	public static JTree tree;
	public static boolean refreshContentPanel = true;
	public static javax.swing.JPanel pnl_Content;
	public static javax.swing.JScrollPane scrl_Content;
	private static javax.swing.JScrollPane scrl_Tree;
	private static javax.swing.JSplitPane splt_MoapiPanel;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="expanded" desc="Generated Code">
	public GWASpiExplorerPanel() throws IOException {


		splt_MoapiPanel = new javax.swing.JSplitPane();
		scrl_Tree = new javax.swing.JScrollPane();
		scrl_Content = new javax.swing.JScrollPane();
		pnl_Content = new javax.swing.JPanel();

		scrl_Tree.setMinimumSize(new java.awt.Dimension(200, 600));
		scrl_Tree.setPreferredSize(new java.awt.Dimension(200, 600));
		splt_MoapiPanel.setLeftComponent(scrl_Tree);


		initContentPanel();
		updateTreePanel(true);

		//<editor-fold defaultstate="collapsed/expanded" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(splt_MoapiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE)
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(splt_MoapiPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>



	}

	public static void refreshContentPanel() {
		try {
			String lastSelectedNode = org.gwaspi.global.Config.getConfigValue("LAST_SELECTED_NODE", "0");
			tree.setSelectionPath(null);
			if (!lastSelectedNode.equals("0")) {
				for (int i = 0; i < tree.getRowCount(); i++) {
					TreePath tp = tree.getPathForRow(i);
					Object tmpTP = tp.getLastPathComponent();
					if (lastSelectedNode.equals(tmpTP.toString())) {
						tree.setSelectionPath(tp);
					}
				}
			} else {
				TreePath tp = tree.getPathForRow(0);
				tree.setSelectionPath(tp);
			}
		} catch (IOException ex) {
		}
	}

	public static void updateTreePanel(boolean _refreshContentPanel) throws IOException {

		//TOGGLE CONTENT PANEL REFRESH BEHAVIOUR FOR CURRENT METHOD RUN
		if (!_refreshContentPanel) {
			refreshContentPanel = !refreshContentPanel;
		}


		JTree tmpTree = new JTree();
		tmpTree.setEnabled(false);

		int X = scrl_Tree.getHorizontalScrollBar().getValue();
		int Y = scrl_Tree.getVerticalScrollBar().getValue();
		int width = splt_MoapiPanel.getDividerLocation();

		org.gwaspi.model.GWASpiExplorer gwaspiExplorer = new org.gwaspi.model.GWASpiExplorer();
		tmpTree = gwaspiExplorer.getGWASpiTree();
		//tmpTree = org.gwaspi.model.GWASpiExplorer.getGWASpiTree();

		scrl_Tree.setViewportView(tmpTree);
		splt_MoapiPanel.setLeftComponent(scrl_Tree);

		String lastSelectedNode = org.gwaspi.global.Config.getConfigValue("LAST_SELECTED_NODE", "0");

		//Find out what paths are expanded
		ArrayList<TreePath> expandedNodesAL = null;
		if (tree != null) {
			TreePath rootPath = tree.getPathForRow(0);
			Enumeration e = tree.getExpandedDescendants(rootPath);
			ArrayList expanded = new ArrayList();
			if (e != null) {
				while (e.hasMoreElements()) {
					expanded.add((TreePath) e.nextElement());
				}
			}
			expandedNodesAL = expanded;
		}

		tree = tmpTree;

		if (expandedNodesAL != null) { //HAPPENS WHEN REFRESHING
			for (int i = 0; i < tmpTree.getRowCount(); i++) {
				TreePath ntp = tmpTree.getPathForRow(i);
				Object lastNTP = ntp.getLastPathComponent();
				for (TreePath tp : expandedNodesAL) {
					Object lastTP = tp.getLastPathComponent();
					if (lastNTP.toString().equals(lastTP.toString())) {
						tmpTree.expandRow(i);
					}
				}
			}

			if (!lastSelectedNode.equals("0")) {
				for (int i = 0; i < tmpTree.getRowCount(); i++) {
					TreePath tp = tmpTree.getPathForRow(i);
					Object tmpTP = tp.getLastPathComponent();
					if (lastSelectedNode.equals(tmpTP.toString())) {
						tmpTree.setSelectionPath(tp);
					}
				}
			} else {
				TreePath tp = tmpTree.getPathForRow(0);
				tmpTree.setSelectionPath(tp);
			}

		} else { //HAPPENS AT INIT OF APPLICATION
			int row = 0;
			while (row < tmpTree.getRowCount()) {
				tmpTree.expandRow(row);
				row++;
			}

			if (!lastSelectedNode.equals("0")) {
				for (int i = 0; i < tmpTree.getRowCount(); i++) {
					TreePath tp = tmpTree.getPathForRow(i);
					Object tmpTP = tp.getLastPathComponent();
					if (lastSelectedNode.equals(tmpTP.toString())) {
						tmpTree.setSelectionPath(tp);
					}
				}
			} else {
				TreePath tp = tmpTree.getPathForRow(0);
				tmpTree.setSelectionPath(tp);
			}

			GWASpiExplorerNodes.setAllNodesCollapsable();

			row--;
			while (row >= 0) {
				TreePath tp = tmpTree.getPathForRow(row);
				if (!tp.isDescendant(tmpTree.getSelectionPath())) {
					tmpTree.collapseRow(row);
				}
				row--;
			}

		}

		// TOGGLE CONTENT PANEL REFRESH BEHAVIOUR FOR CURRENT METHOD RUN
		if (!_refreshContentPanel) {
			refreshContentPanel = !refreshContentPanel;
		}

		splt_MoapiPanel.setDividerLocation(width);
		scrl_Tree.getHorizontalScrollBar().setValue(X);
		scrl_Tree.getVerticalScrollBar().setValue(Y);

		tmpTree.setEnabled(true);
	}

	public static void initContentPanel() {
		pnl_Content = new IntroPanel();
		pnl_Content.setVisible(true);
		scrl_Content.setViewportView(pnl_Content);
		splt_MoapiPanel.setRightComponent(scrl_Content);
	}
	// </editor-fold>
}
