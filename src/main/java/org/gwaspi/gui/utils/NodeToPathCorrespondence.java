package org.gwaspi.gui.utils;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class NodeToPathCorrespondence {

	private static final Logger log = LoggerFactory.getLogger(NodeToPathCorrespondence.class);

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
