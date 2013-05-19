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

package org.gwaspi.gui.utils;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeToPathCorrespondence {

	private static final Logger log = LoggerFactory.getLogger(NodeToPathCorrespondence.class);

	private NodeToPathCorrespondence() {
	}

	public static Map<Integer, TreePath> buildNodeToPathCorrespondence(DefaultMutableTreeNode currentNode, boolean getSubChildren) {

		Map<Integer, TreePath> nodeToPathChildrenMap = getChildren(currentNode);

		if (getSubChildren) {
			Enumeration enumTN = currentNode.children();
			while (enumTN.hasMoreElements()) {
				DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) enumTN.nextElement();
				Map<Integer, TreePath> subNodeToPathChildrenMap = getChildren(subNode);
				nodeToPathChildrenMap.putAll(subNodeToPathChildrenMap);
			}
		}

		return nodeToPathChildrenMap;
	}

	private static Map<Integer, TreePath> getChildren(DefaultMutableTreeNode currentNode) {

		Map<Integer, TreePath> nodeToPathChildrenMap = new LinkedHashMap<Integer, TreePath>();

		Enumeration enumTN = currentNode.children();
		while (enumTN.hasMoreElements()) {
			DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) enumTN.nextElement();
			Object tmpElement = tmpNode.getUserObject();
			NodeElementInfo currentElementInfo = (NodeElementInfo) tmpElement;

			for (int i = 0; i < GWASpiExplorerPanel.getSingleton().getTree().getRowCount(); i++) {
				if (GWASpiExplorerPanel.getSingleton().getTree().getPathForRow(i).getLastPathComponent().toString().equals(currentElementInfo.getNodeUniqueName())) {
					nodeToPathChildrenMap.put(currentElementInfo.getNodeId(), GWASpiExplorerPanel.getSingleton().getTree().getPathForRow(i));
				}
			}
		}

		return nodeToPathChildrenMap;
	}
}
