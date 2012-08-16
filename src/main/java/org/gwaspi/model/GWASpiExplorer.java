package org.gwaspi.model;

import org.gwaspi.gui.reports.ChartDefaultDisplay;
import org.gwaspi.global.Text;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.gui.*;
import org.gwaspi.gui.reports.ManhattanChartDisplay;
import org.gwaspi.gui.reports.Report_AnalysisPanel;
import org.gwaspi.gui.reports.Report_HardyWeinbergSummary;
import org.gwaspi.gui.reports.Report_QAMarkersSummary;
import org.gwaspi.gui.reports.Report_QASamplesSummary;
import org.gwaspi.gui.reports.Report_SampleInfoPanel;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiExplorer {

//    public TreeModel treeModel;
	public static ArrayList lastExpanded;
	private static JTree tree;
	//Possible values are "Angled" (the default), "Horizontal", and "None".
	private static boolean playWithLineStyle = false;
	private static String lineStyle = "Horizontal";
	//Optionally set the look and feel.
	private static Icon customOpenIcon = initIcon("hex_open.png");
	private static Icon customClosedIcon = initIcon("hex_closed.png");
	private static Icon customLeafIcon = initIcon("leaf_sepia.png");
//

	public GWASpiExplorer() {
	}

	public JTree getGWASpiTree() throws IOException {

		//Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(Text.App.appName);
		growTree(top);

		//Create a tree that allows one selection at a time.
		tree = new JTree(top);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setClosedIcon(customClosedIcon);
		renderer.setOpenIcon(customOpenIcon);
		renderer.setLeafIcon(customLeafIcon);
		tree.setCellRenderer(renderer);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.setSelectionRow(0);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(treeListener);

		// Add pre-expansion event listener
		tree.addTreeWillExpandListener(new MyTreeWillExpandListener());


		if (playWithLineStyle) {
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}

		return tree;
	}

	private static void growTree(DefaultMutableTreeNode top) throws IOException {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode studyItem = null;
		DefaultMutableTreeNode matrixItem = null;
		DefaultMutableTreeNode sampleInfoItem = null;
		DefaultMutableTreeNode operationItem = null;
		DefaultMutableTreeNode subOperationItem = null;
		DefaultMutableTreeNode reportItem = null;


		//<editor-fold defaultstate="expanded" desc="STUDY MANAGEMENT">

		category = new DefaultMutableTreeNode(org.gwaspi.global.Text.App.treeStudyManagement);
		top.add(category);


		//LOAD ALL STUDIES
		org.gwaspi.model.StudyList studiesMod = new org.gwaspi.model.StudyList();
		for (int i = 0; i < studiesMod.studyList.size(); i++) {

			//LOAD CURRENT STUDY
			studyItem = GWASpiExplorerNodes.createStudyTreeNode(studiesMod.studyList.get(i).getStudyId());

			//LOAD SAMPLE INFO FOR CURRENT STUDY
			sampleInfoItem = GWASpiExplorerNodes.createSampleInfoTreeNode(studiesMod.studyList.get(i).getStudyId());
			if (sampleInfoItem != null) {
				studyItem.add(sampleInfoItem);
			}

			//LOAD MATRICES FOR CURRENT STUDY
			org.gwaspi.model.MatricesList matrixMod = new org.gwaspi.model.MatricesList(studiesMod.studyList.get(i).getStudyId());
			for (int j = 0; j < matrixMod.matrixList.size(); j++) {

				matrixItem = GWASpiExplorerNodes.createMatrixTreeNode(matrixMod.matrixList.get(j).getMatrixId());

				//LOAD Parent OPERATIONS ON CURRENT MATRIX
				org.gwaspi.model.OperationsList parentOpsMod = new org.gwaspi.model.OperationsList(matrixMod.matrixList.get(j).getMatrixId(), -1);
				org.gwaspi.model.OperationsList allOpsMod = new org.gwaspi.model.OperationsList(matrixMod.matrixList.get(j).getMatrixId());
				for (int k = 0; k < parentOpsMod.operationsListAL.size(); k++) {
					//LOAD SUB OPERATIONS ON CURRENT MATRIX
					Operation currentOP = parentOpsMod.operationsListAL.get(k);
					operationItem = GWASpiExplorerNodes.createOperationTreeNode(currentOP.getOperationId());


					ArrayList<Operation> childrenOpAL = getChildrenOperations(allOpsMod.operationsListAL, currentOP.getOperationId());
					for (int m = 0; m < childrenOpAL.size(); m++) {
						Operation subOP = childrenOpAL.get(m);
						subOperationItem = GWASpiExplorerNodes.createSubOperationTreeNode(subOP.getOperationId());

						//LOAD REPORTS ON CURRENT SUB-OPERATION
						if (!subOP.getOperationType().equals(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString())) { //NOT IF HW
							org.gwaspi.model.ReportsList reportsMod = new org.gwaspi.model.ReportsList(subOP.getOperationId(), Integer.MIN_VALUE);
							for (int n = 0; n < reportsMod.reportsListAL.size(); n++) {
								Report rp = reportsMod.reportsListAL.get(n);
								if (!rp.getReportType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())
										&& !rp.getReportType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())
										&& !rp.getReportType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
									reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsMod.reportsListAL.get(n).getReportId());
									subOperationItem.add(reportItem);
								}

							}
						}
						operationItem.add(subOperationItem);
					}

					/////////////////////////////////////////////////////////////////////////////////////
					////////////////////////// START TESTING /////////////////////////////
					////////////////////////////////////////////////////////////////////////////////////
					//LOAD REPORTS ON CURRENT OPERATION
					org.gwaspi.model.ReportsList reportsMod = new org.gwaspi.model.ReportsList(currentOP.getOperationId(), Integer.MIN_VALUE);
					if (!currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) { //SAMPLE_QA MUST BE DEALT DIFFERENTLY
						for (int n = 0; n < reportsMod.reportsListAL.size(); n++) {
							reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsMod.reportsListAL.get(n).getReportId());
							operationItem.add(reportItem);
						}
					} else {
						//DEAL WITH SAMPLE_HTZYPLOT
						for (int n = 0; n < reportsMod.reportsListAL.size(); n++) {
							Report rp = reportsMod.reportsListAL.get(n);
							if (rp.getReportType().equals(cNetCDF.Defaults.OPType.SAMPLE_HTZYPLOT.toString())) {
								reportItem = GWASpiExplorerNodes.createReportTreeNode(reportsMod.reportsListAL.get(n).getReportId());
								operationItem.add(reportItem);
							}
						}
					}
					/////////////////////////////////////////////////////////////////////////////////
					////////////////////////// END TESTING /////////////////////////////
					////////////////////////////////////////////////////////////////////////////////


					matrixItem.add(operationItem);

				}
				studyItem.add(matrixItem);
			}

			//ADD ALL TREE-NODES INTO TREE
			category.add(studyItem);
		}

		top.add(category);
		//</editor-fold>

	}
	//<editor-fold defaultstate="collapsed" desc="LISTENER">
	//TREE SELECTION LISTENER
	private static TreeSelectionListener treeListener = new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent arg0) {

			tree.setEnabled(false);

			//CHECK IF LISTENER IS ALLOWED TO UPDATE CONTENT PANEL
			if (!org.gwaspi.gui.GWASpiExplorerPanel.refreshContentPanel) {
				tree.setEnabled(true);
				return;
			}

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (currentNode == null) {
				tree.setEnabled(true);
				return;
			}

			//Check first if we are at the GWASpi root
			if (currentNode.isRoot()) { //We are in GWASpi node
				org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new IntroPanel();
				org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
			}

			//Check where we are in tree and show appropiate content panel
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentElementInfo = null;
			try {
				currentElementInfo = (NodeElementInfo) currentElement;
			} catch (Exception e) {
			}


			TreePath treePath = arg0.getPath();
			if (treePath != null && currentElementInfo != null) {
				try {
					org.gwaspi.global.Config.setConfigValue("LAST_SELECTED_NODE", currentElementInfo.nodeUniqueName);
				} catch (IOException ex) {
					Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else if (currentElement.equals(org.gwaspi.global.Text.App.treeStudyManagement)) {
				try {
					org.gwaspi.global.Config.setConfigValue("LAST_SELECTED_NODE", org.gwaspi.global.Text.App.treeStudyManagement);
				} catch (IOException ex) {
					Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				try {
					org.gwaspi.global.Config.setConfigValue("LAST_SELECTED_NODE", org.gwaspi.global.Text.App.appName);
				} catch (IOException ex) {
					Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			//Get parent node of currently selected node
			DefaultMutableTreeNode parentNode = null;
			NodeElementInfo parentElementInfo = null;
			Object parentElement = null;
			if (treePath.getParentPath() != null) {
				parentNode = (DefaultMutableTreeNode) treePath.getParentPath().getLastPathComponent();
				parentElement = parentNode.getUserObject();
				try {
					parentElementInfo = (NodeElementInfo) parentElement;
				} catch (Exception e) {
				}
			}

			//Reference Databse Branch
			if (currentNode.toString().equals(org.gwaspi.global.Text.App.treeReferenceDBs)) {
			} //Study Management Branch
			//else if(currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeStudyManagement)){
			else if (currentNode.toString().equals(org.gwaspi.global.Text.App.treeStudyManagement)) {
				try {
					//We are in StudyList node
					org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new StudyManagementPanel();
					org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
				} catch (IOException ex) {
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} //Study Branch
			//else if(parentNode!=null && parentNode.toString().equals(org.gwaspi.global.Text.App.treeStudyManagement)){
			else if (currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeStudy)) {
				try {
					org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentStudyPanel(currentElementInfo.nodeId);
					org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
				} catch (IOException ex) {
					//System.out.println("StudyID: "+currentElementInfo.nodeId);
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} //Sample Info Branch
			else if (currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeSampleInfo)) {
				try {
					org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_SampleInfoPanel(parentElementInfo.nodeId);
					org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
				} catch (IOException ex) {
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} //Matrix Branch
			else if (currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeMatrix)) {
				try {
					//We are in MatrixItemAL node
					tree.expandPath(treePath);
					org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(currentElementInfo.nodeId);
					org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
				} catch (IOException ex) {
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} //Operations Branch
			else if (currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeOperation)) {
				try {
					if (parentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeOperation)) {
						//Display SubOperation analysis panel
						tree.expandPath(treePath);
						Operation currentOP = new Operation(currentElementInfo.nodeId);
						Operation parentOP = new Operation(parentElementInfo.nodeId);
						if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString())) {
							//Display HW Report
							ReportsList reportList = new ReportsList(currentOP.getOperationId(), currentOP.getParentMatrixId());
							if (reportList.reportsListAL.size() > 0) {
								Report hwReport = reportList.reportsListAL.get(0);
								String reportFile = hwReport.getReportFileName();
								org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_HardyWeinbergSummary(hwReport.getStudyId(), reportFile, hwReport.getParentOperationId());
								org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
							}
						} else if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
							//Display Association Report
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getOperationId(), null);
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						} else if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.GENOTYPICTEST.toString())) {
							//Display Association Report
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getOperationId(), null);
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						} else if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.TRENDTEST.toString())) {
							//Display Trend Test Report
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_AnalysisPanel(currentOP.getStudyId(), currentOP.getParentMatrixId(), currentOP.getOperationId(), null);
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						} else {
							//gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(parentOP.getParentMatrixId(), currentElementInfo.parentNodeId);
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(parentOP.getParentMatrixId(), currentElementInfo.nodeId);
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						}
					} else {
						//Display Operation
						tree.expandPath(treePath);
						Operation currentOP = new Operation(currentElementInfo.nodeId);
						if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
							//Display MarkerQA panel
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixMarkerQAPanel(currentOP.getParentMatrixId(), currentOP.getOperationId());
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						} else if (currentOP.getOperationType().equals(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
							//Display SampleQA Report
							ReportsList reportList = new ReportsList(currentOP.getOperationId(), currentOP.getParentMatrixId());
							if (reportList.reportsListAL.size() > 0) {
								Report sampleQAReport = reportList.reportsListAL.get(0);
								String reportFile = sampleQAReport.getReportFileName();
								org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_QASamplesSummary(sampleQAReport.getStudyId(), reportFile, sampleQAReport.getParentOperationId());
								org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
							}
						} else {
							//Display Operation analysis panel
							org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(parentElementInfo.nodeId, currentElementInfo.nodeId);
							org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
						}

					}

				} catch (IOException ex) {
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} //Reports Branch
			else if (currentElementInfo != null && currentElementInfo.nodeType.toString().equals(org.gwaspi.global.Text.App.treeReport)) {
				try {
					//Display report summary
					tree.expandPath(treePath);
					Report rp = new Report(currentElementInfo.nodeId);
					String reportFile = rp.getReportFileName();
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.SAMPLE_HTZYPLOT.toString())) {
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new org.gwaspi.gui.reports.SampleQAHetzygPlotZoom(rp.getParentOperationId());
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.ALLELICTEST.toString())) {
						//gui.GWASpiExplorerPanel.pnl_Content = new Report_AssociationSummary(rp.getStudyId(), reportFile, rp.getParentOperationId(), null);
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_AnalysisPanel(rp.getStudyId(), rp.getParentMatrixId(), rp.getParentOperationId(), null);
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.QQPLOT.toString())) {
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new ChartDefaultDisplay(rp.getStudyId(), reportFile, rp.getParentOperationId());
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.MANHATTANPLOT.toString())) {
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new ManhattanChartDisplay(rp.getStudyId(), reportFile, rp.getParentOperationId());
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_QAMarkersSummary(rp.getStudyId(), reportFile, rp.getParentOperationId());
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
//                    if(rp.getReportType().equals(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())){
//                        org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_QASamplesSummary(rp.getStudyId(), reportFile, rp.getParentOperationId());
//                        org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
//                    }
					if (rp.getReportType().equals(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString())) {
						org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new Report_HardyWeinbergSummary(rp.getStudyId(), reportFile, rp.getParentOperationId());
						org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
					}
				} catch (IOException ex) {
					//Logger.getLogger(GWASpiExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new IntroPanel();
				org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
			}

			//THIS TO AVOID RANDOM MONKEY CLICKER BUG
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
			}

			tree.setEnabled(true);

		}
	};

	//PRE-EXPANSION/COLLAPSE LISTENER
	public class MyTreeWillExpandListener implements TreeWillExpandListener {

		public void treeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {
			// Get the path that will be expanded
			TreePath treePath = evt.getPath();

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentNodeInfo = null;
			try {
				currentNodeInfo = (NodeElementInfo) currentElement;
				if (currentNodeInfo.isCollapsable) {
					//ALLWAYS ALLOW EXPANSION
				}
			} catch (Exception e) {
			}
		}

		public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException {
			// Get the path that will be expanded
			TreePath treePath = evt.getPath();

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentNodeInfo = null;
			try {
				currentNodeInfo = (NodeElementInfo) currentElement;
			} catch (Exception e) {
			}
			if (currentNodeInfo != null && !currentNodeInfo.isCollapsable) {
				//VETO EXPANSION
				throw new ExpandVetoException(evt);
			}

		}
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	protected static ArrayList getChildrenOperations(ArrayList<Operation> opAL, int parentOpId) {

		ArrayList childrednOperationsAL = new ArrayList();

		for (int i = 0; i < opAL.size(); i++) {
			int currentParentOPId = (Integer) opAL.get(i).getParentOperationId();
			if (currentParentOPId == parentOpId) {
				childrednOperationsAL.add(opAL.get(i));
			}
		}

		return childrednOperationsAL;
	}

	protected static Icon initIcon(String iconName) {
		Icon logo = null;
		URL logoPath = GWASpiExplorer.class.getClass().getResource("/resources/" + iconName);
		//String logoPath = org.gwaspi.global.Config.getConfigValue("ConfigDir", "") + "/" +iconName;
		logo = new ImageIcon(logoPath);
		return logo;
	}
	//</editor-fold>
}
