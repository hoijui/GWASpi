package org.gwaspi.gui.utils;

import org.gwaspi.gui.GWASpiExplorerPanel;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class NodeToPathCorrespondence {

	private NodeToPathCorrespondence() {
	}

	public static Map<Integer, Object> buildNodeToPathCorrespondence(DefaultMutableTreeNode currentNode, boolean getSubChildren) {

		Map<Integer, Object> nodeToPathChildrenMap = getChildren(currentNode);

		if (getSubChildren) {
			Enumeration enumTN = currentNode.children();
			while (enumTN.hasMoreElements()) {
				DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) enumTN.nextElement();
				Map<Integer, Object> subNodeToPathChildrenMap = getChildren(subNode);
				nodeToPathChildrenMap.putAll(subNodeToPathChildrenMap);
			}
		}

		return nodeToPathChildrenMap;
	}

	private static Map<Integer, Object> getChildren(DefaultMutableTreeNode currentNode) {

		Map<Integer, Object> nodeToPathChildrenMap = new LinkedHashMap<Integer, Object>();

		Enumeration enumTN = currentNode.children();
		while (enumTN.hasMoreElements()) {
			DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) enumTN.nextElement();
			Object tmpElement = tmpNode.getUserObject();
			try {
				NodeElementInfo currentElementInfo = (org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo) tmpElement;

				for (int i = 0; i < GWASpiExplorerPanel.getSingleton().getTree().getRowCount(); i++) {
					if (GWASpiExplorerPanel.getSingleton().getTree().getPathForRow(i).getLastPathComponent().toString().equals(currentElementInfo.nodeUniqueName)) {
						nodeToPathChildrenMap.put(currentElementInfo.nodeId, GWASpiExplorerPanel.getSingleton().getTree().getPathForRow(i));
					}
				}

			} catch (Exception ex) {
			}
		}

		return nodeToPathChildrenMap;
	}
}
