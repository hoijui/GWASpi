package org.gwaspi.gui.utils;

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

		Map<Integer, Object> nodeToPathChildrenLHM = getChildren(currentNode);
		if (getSubChildren) {
			Enumeration enumTN = currentNode.children();
			while (enumTN.hasMoreElements()) {
				DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) enumTN.nextElement();
				Map<Integer, Object> subNodeToPathChildrenLHM = getChildren(subNode);
				nodeToPathChildrenLHM.putAll(subNodeToPathChildrenLHM);
			}
		}
		return nodeToPathChildrenLHM;

	}

	private static Map<Integer, Object> getChildren(DefaultMutableTreeNode currentNode) {
		Map<Integer, Object> nodeToPathChildrenLHM = new LinkedHashMap<Integer, Object>();
		Enumeration enumTN = currentNode.children();
		while (enumTN.hasMoreElements()) {
			DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) enumTN.nextElement();
			Object tmpElement = tmpNode.getUserObject();
			try {
				NodeElementInfo currentElementInfo = (org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo) tmpElement;

				for (int i = 0; i < org.gwaspi.gui.GWASpiExplorerPanel.tree.getRowCount(); i++) {
					if (org.gwaspi.gui.GWASpiExplorerPanel.tree.getPathForRow(i).getLastPathComponent().toString().equals(currentElementInfo.nodeUniqueName)) {
						nodeToPathChildrenLHM.put(currentElementInfo.nodeId, org.gwaspi.gui.GWASpiExplorerPanel.tree.getPathForRow(i));
					}
				}

			} catch (Exception ex) {
			}
		}
		return nodeToPathChildrenLHM;
	}
}
