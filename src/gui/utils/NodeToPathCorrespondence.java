/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.utils;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class NodeToPathCorrespondence {

    public static LinkedHashMap buildNodeToPathCorrespondence(DefaultMutableTreeNode currentNode, boolean getSubChildren){

        LinkedHashMap nodeToPathChildrenLHM = getChildren(currentNode);
        if(getSubChildren){
            Enumeration enumTN = currentNode.children();
            while (enumTN.hasMoreElements()){
                DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) enumTN.nextElement();
                LinkedHashMap subNodeToPathChildrenLHM = getChildren(subNode);
                nodeToPathChildrenLHM.putAll(subNodeToPathChildrenLHM);
            }
        }
        return nodeToPathChildrenLHM;

    }

    protected static LinkedHashMap getChildren(DefaultMutableTreeNode currentNode){
        LinkedHashMap nodeToPathChildrenLHM = new LinkedHashMap();
        Enumeration enumTN = currentNode.children();
        while (enumTN.hasMoreElements()){
            DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) enumTN.nextElement();
            Object tmpElement = tmpNode.getUserObject();
            model.GWASpiExplorerNodes.NodeElementInfo currentElementInfo = null;
            try {
                currentElementInfo = (model.GWASpiExplorerNodes.NodeElementInfo) tmpElement;

                for(int i=0;i<gui.GWASpiExplorerPanel.tree.getRowCount();i++){
                    if(gui.GWASpiExplorerPanel.tree.getPathForRow(i).getLastPathComponent().toString().equals(currentElementInfo.nodeUniqueName)){
                        nodeToPathChildrenLHM.put(currentElementInfo.nodeId, gui.GWASpiExplorerPanel.tree.getPathForRow(i));
                    }
                }

            } catch (Exception ex) {
            }
        }
        return nodeToPathChildrenLHM;
    }

}
