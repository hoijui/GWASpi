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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.gwaspi.global.Config;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.GWASpiExplorerTree;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.ReportKey;
import org.gwaspi.model.StudyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GWASpiExplorerPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(GWASpiExplorerPanel.class);
	private static final Font TITLE_FONT_REGULAR = new Font("DejaVu Sans", 1, 13); // NOI18N
	private static final Font TITLE_FONT_MAIN = new Font("FreeSans", 1, 18); // NOI18N
	private static final int GAP_BUTTONS_H = 18;
	private static final int GAP_BUTTONS_V = 5;

	// Variables declaration - do not modify
	private JTree tree;
	private boolean refreshContentPanel = true;
	private JPanel pnl_Content;
	private JScrollPane scrl_Content;
	private final JScrollPane scrl_Tree;
	private final JSplitPane splt_MoapiPanel;
	private static GWASpiExplorerPanel singleton = null;
	// End of variables declaration

	private GWASpiExplorerPanel() throws IOException {

		splt_MoapiPanel = new JSplitPane();
		scrl_Tree = new JScrollPane();
		scrl_Content = new JScrollPane();
		pnl_Content = new JPanel();

		scrl_Tree.setMinimumSize(new Dimension(200, 150));
		scrl_Tree.setPreferredSize(new Dimension(200, 600));
		splt_MoapiPanel.setLeftComponent(scrl_Tree);

		initContentPanel();

		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.add(splt_MoapiPanel, BorderLayout.CENTER);
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

	public static Border createRegularTitledBorder(String title) {
		return createTitledBorder(title, TITLE_FONT_REGULAR, new Insets(0, 10, 5, 10));
	}

	public static Border createMainTitledBorder(String title) {
		return createTitledBorder(title, TITLE_FONT_MAIN, new Insets(15, 10, 15, 10));
	}

	private static Border createTitledBorder(String title, Font font, Insets innerborderInsets) {

		return new CompoundBorder(
				BorderFactory.createTitledBorder(
						null,
						title,
						TitledBorder.DEFAULT_JUSTIFICATION,
						TitledBorder.DEFAULT_POSITION,
						font),
				new EmptyBorder(innerborderInsets));
	}

	public static JPanel createButtonsPanel(JComponent component0) {
		return createButtonsPanel(new JComponent[] {component0}, null);
	}

	public static JPanel createButtonsPanel(JComponent component0, JComponent component1) {
		return createButtonsPanel(new JComponent[] {component0, component1}, null);
	}

	public static JPanel createButtonsPanel(JComponent component0, JComponent component1, JComponent component2) {
		return createButtonsPanel(new JComponent[] {component0, component1, component2}, null);
	}

	public static JPanel createButtonsPanel(JComponent[] leadingComponents) {
		return createButtonsPanel(leadingComponents, null);
	}

	public static JPanel createButtonsPanel(JComponent[] leadingComponents, JComponent[] trailingComponents) {

		JPanel main = new JPanel();
		main.setLayout(new BorderLayout(GAP_BUTTONS_H, GAP_BUTTONS_V));

		if ((leadingComponents != null) && (leadingComponents.length > 0)) {
			JPanel subPanel = new JPanel();
			GroupLayout layout = new GroupLayout(subPanel);
			subPanel.setLayout(layout);

			GroupLayout.SequentialGroup horizontalG = layout.createSequentialGroup();
			horizontalG.addComponent(leadingComponents[0]);
			for (int lci = 1; lci < leadingComponents.length; lci++) {
				horizontalG.addGap(GAP_BUTTONS_H);
				horizontalG.addComponent(leadingComponents[lci]);
			}
			layout.setHorizontalGroup(horizontalG);

			GroupLayout.ParallelGroup verticalG = layout.createParallelGroup();
			verticalG.addComponent(leadingComponents[0]);
			for (int lci = 1; lci < leadingComponents.length; lci++) {
				verticalG.addGap(GAP_BUTTONS_V);
				verticalG.addComponent(leadingComponents[lci]);
			}
			layout.setVerticalGroup(verticalG);

			main.add(subPanel, BorderLayout.WEST);
		}

		if ((trailingComponents != null) && (trailingComponents.length > 0)) {
			JPanel subPanel = new JPanel();
			GroupLayout layout = new GroupLayout(subPanel);
			subPanel.setLayout(layout);

			GroupLayout.SequentialGroup horizontalG = layout.createSequentialGroup();
			horizontalG.addComponent(trailingComponents[0]);
			for (int lci = 1; lci < trailingComponents.length; lci++) {
				horizontalG.addGap(GAP_BUTTONS_H);
				horizontalG.addComponent(trailingComponents[lci]);
			}
			layout.setHorizontalGroup(horizontalG);

			GroupLayout.ParallelGroup verticalLeadingG = layout.createParallelGroup();
			verticalLeadingG.addComponent(trailingComponents[0]);
			for (int lci = 1; lci < trailingComponents.length; lci++) {
				verticalLeadingG.addGap(GAP_BUTTONS_V);
				verticalLeadingG.addComponent(trailingComponents[lci]);
			}
			layout.setVerticalGroup(verticalLeadingG);

			main.add(subPanel, BorderLayout.EAST);
		}

		return main;
	}

	public void refreshContentPanel() {

		try {
			Integer lastSelectedNodeId = Integer.valueOf(Config.getConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, "0"));
			selectNode(lastSelectedNodeId);
		} catch (IOException ex) {
			log.warn(null, ex);
		}
	}

	private TreePath getPathForNode(final int nodeId) {

		final TreePath path;
		if (nodeId == 0) {
			path = tree.getPathForRow(0);
		} else {
			DefaultMutableTreeNode node = GWASpiExplorerNodes.findTreeNode(nodeId);
			if (node == null) {
				throw new IllegalArgumentException("Could not find node with ID: " + nodeId);
			}
			path = new TreePath(node.getPath());
		}

		return path;
	}

	void selectNode(final int nodeId) {

		tree.setSelectionPath(null);
		tree.setSelectionPath(getPathForNode(nodeId));
	}

	public void setNodeSelected(final int nodeId, final boolean select) {

		final TreePath nodePath = getPathForNode(nodeId);

		List<TreePath> selectionPaths = new ArrayList<TreePath>(Arrays.asList(tree.getSelectionPaths()));
		final boolean isSelected = selectionPaths.contains(nodePath);
		if (select && !isSelected) {
			selectionPaths.add(nodePath);
		} else if (!select && isSelected) {
			selectionPaths.remove(nodePath);
		}
		tree.setSelectionPaths(selectionPaths.toArray(new TreePath[selectionPaths.size()]));
	}

	public void selectNode(final Object nodeKey) {

		final int nodeId;
		if (nodeKey == null) {
			nodeId = 0;
		} else if (nodeKey instanceof Integer) {
			nodeId = (Integer) nodeKey;
		} else if (nodeKey instanceof StudyKey) {
			nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, (StudyKey) nodeKey);
		} else if (nodeKey instanceof DataSetKey) {
			final DataSetKey dataSetKey = (DataSetKey) nodeKey;
			if (dataSetKey.isMatrix()) {
				nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, dataSetKey.getMatrixParent());
			} else {
				nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, dataSetKey.getOperationParent());
			}
		} else if (nodeKey instanceof MatrixKey) {
			nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, (MatrixKey) nodeKey);
		} else if (nodeKey instanceof OperationKey) {
			nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, (OperationKey) nodeKey);
		} else if (nodeKey instanceof ReportKey) {
			nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.REPORT, (ReportKey) nodeKey);
		} else {
			throw new UnsupportedOperationException("(coding error) No support for selecting an elemnt of type " + nodeKey.getClass().getName());
		}

		selectNode(nodeId);
	}

	public void selectNodeStudyManagement() {
		selectNode(NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY_MANAGEMENT, null));
	}

	public void updateTreePanel(boolean refreshContentPanel) throws IOException {

		// TOGGLE CONTENT PANEL REFRESH BEHAVIOUR FOR CURRENT METHOD RUN
		if (!refreshContentPanel) {
			this.refreshContentPanel = !this.refreshContentPanel;
		}

		int X = scrl_Tree.getHorizontalScrollBar().getValue();
		int Y = scrl_Tree.getVerticalScrollBar().getValue();
		int width = splt_MoapiPanel.getDividerLocation();

		GWASpiExplorerTree gwaspiExplorer = new GWASpiExplorerTree();
		JTree tmpTree = gwaspiExplorer.getGWASpiTree();

		scrl_Tree.setViewportView(tmpTree);
		splt_MoapiPanel.setLeftComponent(scrl_Tree);

		Integer lastSelectedNodeId = Integer.valueOf(Config.getConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, "0"));

		// Find out what paths are expanded
		List<TreePath> expandedNodes = null;
		if (tree != null) {
			TreePath rootPath = tree.getPathForRow(0);
			Enumeration e = tree.getExpandedDescendants(rootPath);
			List<TreePath> expanded = new ArrayList<TreePath>();
			if (e != null) {
				while (e.hasMoreElements()) {
					expanded.add((TreePath) e.nextElement());
				}
			}
			expandedNodes = expanded;
		}

		tree = tmpTree;

		if (expandedNodes != null) { // HAPPENS WHEN REFRESHING
			for (int i = 0; i < tmpTree.getRowCount(); i++) {
				TreePath ntp = tmpTree.getPathForRow(i);
				Object lastNTP = ntp.getLastPathComponent();
				for (TreePath tp : expandedNodes) {
					Object lastTP = tp.getLastPathComponent();
					if (lastNTP.toString().equals(lastTP.toString())) {
						tmpTree.expandRow(i);
					}
				}
			}

			try {
				selectNode(lastSelectedNodeId);
			} catch (IllegalArgumentException ex) {
				// ignore
			}
		} else { // HAPPENS AT INIT OF APPLICATION
			int row = 0;
			while (row < tmpTree.getRowCount()) {
				tmpTree.expandRow(row);
				row++;
			}

			try {
				selectNode(lastSelectedNodeId);
			} catch (IllegalArgumentException ex) {
				// ignore
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
		if (!refreshContentPanel) {
			this.refreshContentPanel = !this.refreshContentPanel;
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
