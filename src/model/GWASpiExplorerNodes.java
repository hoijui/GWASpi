/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import constants.cNetCDF;
import global.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import samples.SampleManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiExplorerNodes {


    //<editor-fold defaultstate="collapsed" desc="NODE DEFINITION">
    public static class NodeElementInfo {
        public int nodeId;
        public int parentNodeId;
        public String nodeType;
        public String nodeUniqueName;
        public boolean isCollapsable = true;

        public NodeElementInfo(int _parentNodeId,
                               int _nodeId,
                               String _nodeType,
                               String _nodeName) {
            parentNodeId=_parentNodeId;
            nodeId=_nodeId;
            nodeType=_nodeType;
            nodeUniqueName=_nodeName;
            isCollapsable = false;

        }

        @Override
        public String toString() {
            return nodeUniqueName;
        }
    }


    protected static DefaultMutableTreeNode createStudyTreeNode(int studyId){
        DefaultMutableTreeNode tn = null;
        try {
            Study study = new Study(studyId);

//            parentNodeId
//            nodeId
//            nodeType
//            nodeUniqueName => will be rsult of toString() call of DefaultMutableTreeNode
//            friendlyName

            tn = new DefaultMutableTreeNode(new NodeElementInfo(0,
                                                                study.getStudyId(),
                                                                global.Text.App.treeStudy,
                                                                "SID: "+study.getStudyId()+" - "+study.getStudyName()));

        } catch (IOException ex) {
            Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tn;
    }

    protected static DefaultMutableTreeNode createMatrixTreeNode(int matrixId){
        DefaultMutableTreeNode tn = null;
        try {
            Matrix mx = new Matrix(matrixId);
            tn = new DefaultMutableTreeNode(new NodeElementInfo(mx.getStudyId(),
                                                                matrixId,
                                                                global.Text.App.treeMatrix,
                                                                "MX: "+matrixId+" - "+mx.matrixMetadata.getMatrixFriendlyName()));
        } catch (IOException ex) {
            Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tn;
    }


    protected static DefaultMutableTreeNode createSampleInfoTreeNode(int studyId) throws IOException {
        DefaultMutableTreeNode tn = null;
        //CHECK IF STUDY EXISTS
        List<Map<String, Object>> rs = SampleManager.getAllSampleInfoFromDBByPoolID(studyId);
        if(!rs.isEmpty()){

            tn = new DefaultMutableTreeNode(new NodeElementInfo(studyId,    //parentNodeId
                                                                studyId,                    //nodeId
                                                                global.Text.App.treeSampleInfo,   //nodeType
                                                                global.Text.App.treeSampleInfo)); //nodeUniqueName
        }
        return tn;
    }


    protected static DefaultMutableTreeNode createOperationTreeNode(int opId){
        DefaultMutableTreeNode tn = null;
        try {
            Operation op = new Operation(opId);
            tn = new DefaultMutableTreeNode(new NodeElementInfo(op.getParentMatrixId(),
                                                                opId,
                                                                global.Text.App.treeOperation,
                                                                "OP: "+opId+" - "+op.getOperationFriendlyName()));
        } catch (IOException ex) {
            Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tn;
    }

    protected static DefaultMutableTreeNode createSubOperationTreeNode(int opId){
        DefaultMutableTreeNode tn = null;
        try {

//            parentNodeId
//            nodeId
//            pathNodeIds
//            nodeType
//            studyNodeName
//            nodeUniqueName

            Operation op = new Operation(opId);
            int[] pathIds = new int[]{0,op.getStudyId(),op.getParentMatrixId(),op.getParentOperationId(),opId};
            tn = new DefaultMutableTreeNode(new NodeElementInfo(op.getParentOperationId(),
                                                                opId,
                                                                global.Text.App.treeOperation,
                                                                "OP: "+opId+" - "+op.getOperationFriendlyName()));
        } catch (IOException ex) {
            Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tn;
    }

    protected static DefaultMutableTreeNode createReportTreeNode(int rpId){
        DefaultMutableTreeNode tn = null;
        try {
            Report rp = new Report(rpId);
            int[] pathIds = new int[]{0,rp.getStudyId(),rp.getParentMatrixId(),rp.getParentOperationId(),rpId};
            tn = new DefaultMutableTreeNode(new NodeElementInfo(rpId,
                                                                rpId,
                                                                global.Text.App.treeReport,
                                                                "RP: "+rpId+" - "+rp.getReportFriendlyName()));
        } catch (IOException ex) {
            Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tn;
    }

    //</editor-fold>


    //<editor-fold defaultstate="expanded" desc="NODE MANAGEMENT">


        //<editor-fold defaultstate="collapsed" desc="STUDY NODES">

        public static void insertLatestStudyNode() throws IOException {
            try {
                //GET LATEST ADDED STUDY
                model.StudyList studiesMod = new model.StudyList();
                TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch(Text.App.treeStudyManagement, 0, Position.Bias.Forward);

                DefaultMutableTreeNode newNode = createStudyTreeNode(studiesMod.studyList.get(studiesMod.studyList.size() - 1).getStudyId());

                if (parentPath != null) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                    addNode(parentNode, newNode, true);
                }
            } catch (Exception exception) {
            }
        }

        public static void insertStudyNode(int studyId) throws IOException {
            try {
                //GET STUDY
                TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch(Text.App.treeStudyManagement, 0, Position.Bias.Forward);

                DefaultMutableTreeNode newNode = createStudyTreeNode(studyId);

                if (parentPath != null) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                    addNode(parentNode, newNode, true);
                }
            } catch (Exception e) {
            }
        }

        public static void deleteStudyNode(int studyId) {
            try {
                //GET DELETE PATH BY PREFIX ONLY
                TreePath deletePath = gui.GWASpiExplorerPanel.tree.getNextMatch("SID: " + studyId + " - ", 0, Position.Bias.Forward);

                if (deletePath != null) {
                    DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
                    deleteNode(deleteNode);
                }
            } catch (Exception e) {
            }
        }

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="MATRIX NODES">

        public static void insertMatrixNode(int studyId, int matrixId) throws IOException {
            if (gui.StartGWASpi.guiMode) {
                try {
                    //GET STUDY
                    model.Study study = new model.Study(studyId);
                    TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch("SID: " + study.getStudyId() + " - " + study.getStudyName(), 0, Position.Bias.Forward);

                    DefaultMutableTreeNode newNode = createMatrixTreeNode(matrixId);

                    if (parentPath != null) {
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                        addNode(parentNode, newNode, true);
                    }
                } catch (IOException iOException) {
                }
            } else {
            }
        }

        public static void deleteMatrixNode(int matrixId) {
            try {
                //GET DELETE PATH BY PREFIX ONLY
                TreePath deletePath = gui.GWASpiExplorerPanel.tree.getNextMatch("MX: " + matrixId + " - ", 0, Position.Bias.Forward);

                if (deletePath != null) {
                    DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
                    deleteNode(deleteNode);
                }
            } catch (Exception e) {
            }
        }

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="OPERATION NODES">

        public static void insertOperationUnderMatrixNode(int matrixId, int opId) throws IOException {
            try {
                //GET MATRIX
                model.Matrix matrix = new model.Matrix(matrixId);
                TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch("MX: " + matrixId + " - " + matrix.matrixMetadata.getMatrixFriendlyName(), 0, Position.Bias.Forward);

                DefaultMutableTreeNode newNode = createOperationTreeNode(opId);

                if (parentPath != null) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                    addNode(parentNode, newNode, true);
                }
            } catch (Exception exception) {
            }
        }


        public static void insertSubOperationUnderOperationNode(int parentOpId, int opId) throws IOException {
            try {
                //GET MATRIX
                model.Operation parentOP = new model.Operation(parentOpId);
                TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch("OP: " + parentOpId + " - " + parentOP.getOperationFriendlyName(), 0, Position.Bias.Forward);

                DefaultMutableTreeNode newNode = createOperationTreeNode(opId);

                if (parentPath != null) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                    addNode(parentNode, newNode, true);
                }
            } catch (Exception exception) {
            }
        }

        public static void deleteOperationNode(int opId) {
            try {
                //GET DELETE PATH BY PREFIX ONLY
                TreePath deletePath = gui.GWASpiExplorerPanel.tree.getNextMatch("OP: " + opId + " - ", 0, Position.Bias.Forward);

                if (deletePath != null) {
                    DefaultMutableTreeNode deleteNode = (DefaultMutableTreeNode) deletePath.getLastPathComponent();
                    deleteNode(deleteNode);
                }
            } catch (Exception e) {
            }
        }

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="REPORT NODES">
        public static void insertReportsUnderOperationNode(int parentOpId) throws IOException {
            try {
                //GET OPERATION
                model.Operation parentOP = new model.Operation(parentOpId);
                TreePath parentPath = gui.GWASpiExplorerPanel.tree.getNextMatch("OP: "+parentOpId+" - "+parentOP.getOperationFriendlyName(), 0, Position.Bias.Forward);
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();

                //GET ALL REPORTS UNDER THIS OPERATION
                model.ReportsList reportsMod = new model.ReportsList(parentOpId, Integer.MIN_VALUE);
                for (int n = 0; n < reportsMod.reportsListAL.size(); n++) {
                    Report rp = reportsMod.reportsListAL.get(n);

                    if (!parentOP.getOperationType().equals(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString()) && //DON'T SHOW SUPERFLUOUS OPEARATION INFO
                            !parentOP.getOperationType().equals(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
                        if (!rp.getReportType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
                            DefaultMutableTreeNode newNode = createReportTreeNode(reportsMod.reportsListAL.get(n).getReportId());
                            addNode(parentNode, newNode, true);
                        }
                    }

                }
            } catch (Exception exception) {
            }

        }
        //</editor-fold>


        public static DefaultMutableTreeNode addNode(DefaultMutableTreeNode parentNode,
                                                DefaultMutableTreeNode child,
                                                boolean shouldBeVisible) {

            DefaultTreeModel treeModel = (DefaultTreeModel) gui.GWASpiExplorerPanel.tree.getModel();
            treeModel.insertNodeInto(child, parentNode, parentNode.getChildCount());

            gui.GWASpiExplorerPanel.tree.expandPath(new TreePath(parentNode.getPath()));


            return child;
        }


        public static DefaultMutableTreeNode deleteNode(DefaultMutableTreeNode child) {

            DefaultTreeModel treeModel = (DefaultTreeModel) gui.GWASpiExplorerPanel.tree.getModel();
            treeModel.removeNodeFromParent(child);

            return child;
        }


        public static void setAllNodesCollapsable(){
            if (gui.StartGWASpi.guiMode) {
                for (int i = 0; i < gui.GWASpiExplorerPanel.tree.getRowCount(); i++) {
                    TreePath treePath = gui.GWASpiExplorerPanel.tree.getPathForRow(i);
                    DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                    Object currentElement = currentNode.getUserObject();
                    NodeElementInfo currentNodeInfo = null;
                    try {
                        currentNodeInfo = (NodeElementInfo) currentElement;
                        if (!currentNodeInfo.isCollapsable) {
                            currentNodeInfo.isCollapsable = true;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

    
    //</editor-fold>

}
