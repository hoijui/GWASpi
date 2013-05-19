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

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.gwaspi.global.Config;
import org.gwaspi.model.GWASpiExplorer;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GWASpiExplorerPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(GWASpiExplorerPanel.class);

	// Variables declaration - do not modify
	private JTree tree;
	private boolean refreshContentPanel = true;
	private JPanel pnl_Content;
	private JScrollPane scrl_Content;
	private JScrollPane scrl_Tree;
	private JSplitPane splt_MoapiPanel;
	private static GWASpiExplorerPanel singleton = null;
	// End of variables declaration

	private GWASpiExplorerPanel() throws IOException {

		splt_MoapiPanel = new JSplitPane();
		scrl_Tree = new JScrollPane();
		scrl_Content = new JScrollPane();
		pnl_Content = new JPanel();

		scrl_Tree.setMinimumSize(new Dimension(200, 600));
		scrl_Tree.setPreferredSize(new Dimension(200, 600));
		splt_MoapiPanel.setLeftComponent(scrl_Tree);

		initContentPanel();

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(splt_MoapiPanel, GroupLayout.DEFAULT_SIZE, 1035, Short.MAX_VALUE)
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(splt_MoapiPanel, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>
	}

	public static GWASpiExplorerPanel getSingleton() {

		if (singleton == null) {
			try {
				singleton = new GWASpiExplorerPanel();
				singleton.updateTreePanel(true);
			} catch (IOException ex) {
				log.error(null, ex);
				System.exit(-1);
			}
		}

		return singleton;
	}

	public void refreshContentPanel() {
		try {
			String lastSelectedNode = Config.getConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, "0");
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
			log.warn(null, ex);
		}
	}

	public void updateTreePanel(boolean _refreshContentPanel) throws IOException {

		// TOGGLE CONTENT PANEL REFRESH BEHAVIOUR FOR CURRENT METHOD RUN
		if (!_refreshContentPanel) {
			refreshContentPanel = !refreshContentPanel;
		}

		JTree tmpTree = new JTree();
		tmpTree.setEnabled(false);

		int X = scrl_Tree.getHorizontalScrollBar().getValue();
		int Y = scrl_Tree.getVerticalScrollBar().getValue();
		int width = splt_MoapiPanel.getDividerLocation();

		GWASpiExplorer gwaspiExplorer = new GWASpiExplorer();
		tmpTree = gwaspiExplorer.getGWASpiTree();
		//tmpTree = GWASpiExplorer.getGWASpiTree();

		scrl_Tree.setViewportView(tmpTree);
		splt_MoapiPanel.setLeftComponent(scrl_Tree);

		String lastSelectedNode = Config.getConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, "0");

		// Find out what paths are expanded
		List<TreePath> expandedNodesAL = null;
		if (tree != null) {
			TreePath rootPath = tree.getPathForRow(0);
			Enumeration e = tree.getExpandedDescendants(rootPath);
			List<TreePath> expanded = new ArrayList<TreePath>();
			if (e != null) {
				while (e.hasMoreElements()) {
					expanded.add((TreePath) e.nextElement());
				}
			}
			expandedNodesAL = expanded;
		}

		tree = tmpTree;

		if (expandedNodesAL != null) { // HAPPENS WHEN REFRESHING
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
		} else { // HAPPENS AT INIT OF APPLICATION
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

			setAllNodesCollapsable();

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

	private void initContentPanel() {
		pnl_Content = new IntroPanel();
		pnl_Content.setVisible(true);
		scrl_Content.setViewportView(pnl_Content);
		splt_MoapiPanel.setRightComponent(scrl_Content);
	}

	public void setAllNodesCollapsable() {
		if (StartGWASpi.guiMode) {
			for (int i = 0; i < getTree().getRowCount(); i++) {
				TreePath treePath = getTree().getPathForRow(i);
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
				Object currentElement = currentNode.getUserObject();
				NodeElementInfo currentNodeInfo = (NodeElementInfo) currentElement;
				if (!currentNodeInfo.isCollapsable()) {
					currentNodeInfo.setCollapsable(true);
				}
			}
		}
	}

	public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		this.tree = tree;
	}

	public boolean isRefreshContentPanel() {
		return refreshContentPanel;
	}

	public void setRefreshContentPanel(boolean refreshContentPanel) {
		this.refreshContentPanel = refreshContentPanel;
	}

	public JPanel getPnl_Content() {
		return pnl_Content;
	}

	public void setPnl_Content(JPanel pnl_Content) {
		this.pnl_Content = pnl_Content;
	}

	public JScrollPane getScrl_Content() {
		return scrl_Content;
	}

	public void setScrl_Content(JScrollPane scrl_Content) {
		this.scrl_Content = scrl_Content;
	}
}
