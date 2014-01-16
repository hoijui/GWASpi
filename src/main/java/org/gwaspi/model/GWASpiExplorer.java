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

package org.gwaspi.model;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.CurrentMatrixPanel;
import org.gwaspi.gui.CurrentStudyPanel;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.IntroPanel;
import org.gwaspi.gui.MatrixAnalysePanel;
import org.gwaspi.gui.MatrixMarkerQAPanel;
import org.gwaspi.gui.StudyManagementPanel;
import org.gwaspi.gui.reports.ChartDefaultDisplay;
import org.gwaspi.gui.reports.ManhattanChartDisplay;
import org.gwaspi.gui.reports.Report_AnalysisPanel;
import org.gwaspi.gui.reports.Report_HardyWeinbergSummary;
import org.gwaspi.gui.reports.Report_QAMarkersSummary;
import org.gwaspi.gui.reports.Report_QASamplesSummary;
import org.gwaspi.gui.reports.Report_SampleInfoPanel;
import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.gwaspi.model.GWASpiExplorerNodes.UncollapsableNodeElementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GWASpiExplorer {

	private static final Logger log = LoggerFactory.getLogger(GWASpiExplorer.class);

	private JTree tree;
	private final boolean playWithLineStyle;
	/** Possible values are "Angled" (the default), "Horizontal", and "None". */
	private final String lineStyle;
	// Optionally set the look and feel.
	private static final Icon customOpenIcon = initIcon("hex_open.png");
	private static final Icon customClosedIcon = initIcon("hex_closed.png");
	private static final Icon customLeafIcon = initIcon("leaf_sepia.png");

	public GWASpiExplorer() {

		this.tree = null;
		this.playWithLineStyle = false;
		this.lineStyle = "Horizontal";
	}

	public JTree getGWASpiTree() throws IOException {

		if (tree == null) {
			createGWASpiTree();
		}

		return tree;
	}

	private void createGWASpiTree() throws IOException {

		// Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new UncollapsableNodeElementInfo(
				NodeElementInfo.NODE_ID_NONE,
				NodeElementInfo.NODE_ID_NONE,
				Text.App.treeParent,
				Text.App.appName,
				null));
		growTree(top);

		// Create a tree that allows one selection at a time.
		tree = new JTree(top);

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setClosedIcon(customClosedIcon);
		renderer.setOpenIcon(customOpenIcon);
		renderer.setLeafIcon(customLeafIcon);
		tree.setCellRenderer(renderer);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.setSelectionRow(0);

		// Listen for when the selection changes.
		tree.addTreeSelectionListener(treeListener);

		// Add pre-expansion event listener
		tree.addTreeWillExpandListener(new MyTreeWillExpandListener());

		if (playWithLineStyle) {
			tree.putClientProperty("JTree.lineStyle", lineStyle);
		}
	}

	private static void growTree(DefaultMutableTreeNode top) throws IOException {

		//<editor-fold defaultstate="expanded" desc="STUDY MANAGEMENT">
		DefaultMutableTreeNode category = new DefaultMutableTreeNode(new UncollapsableNodeElementInfo(
				NodeElementInfo.NODE_ID_NONE,
				NodeElementInfo.NODE_ID_NONE,
				Text.App.treeParent,
				Text.App.treeStudyManagement,
				null));
		top.add(category);

		// LOAD ALL STUDIES
		List<Study> studyList = StudyList.getStudyList();
		for (int i = 0; i < studyList.size(); i++) {

			// LOAD CURRENT STUDY
			DefaultMutableTreeNode studyItem = GWASpiExplorerNodes.createStudyTreeNode(studyList.get(i));

			// LOAD SAMPLE INFO FOR CURRENT STUDY
			DefaultMutableTreeNode sampleInfoItem = GWASpiExplorerNodes.createSampleInfoTreeNode(StudyKey.valueOf(studyList.get(i)));
			if (sampleInfoItem != null) {
				studyItem.add(sampleInfoItem);
			}

			// LOAD MATRICES FOR CURRENT STUDY
			List<MatrixKey> matrixList = MatricesList.getMatrixList(StudyKey.valueOf(studyList.get(i)));
			for (int j = 0; j < matrixList.size(); j++) {

				DefaultMutableTreeNode matrixItem = GWASpiExplorerNodes.createMatrixTreeNode(matrixList.get(j));

				// LOAD Parent OPERATIONS ON CURRENT MATRIX
				List<OperationMetadata> parentOperations = OperationsList.getOperationsList(matrixList.get(j).getMatrixId(), -1);
				List<OperationMetadata> allOperations = OperationsList.getOperationsList(matrixList.get(j));
				for (int k = 0; k < parentOperations.size(); k++) {
					// LOAD SUB OPERATIONS ON CURRENT MATRIX
					OperationMetadata currentOP = parentOperations.get(k);
					DefaultMutableTreeNode operationItem = GWASpiExplorerNodes.createOperationTreeNode(OperationKey.valueOf(currentOP));


					List<OperationMetadata> childrenOpAL = getChildrenOperations(allOperations, currentOP.getId());
					for (int m = 0; m < childrenOpAL.size(); m++) {
						OperationMetadata subOP = childrenOpAL.get(m);
						DefaultMutableTreeNode subOperationItem = GWASpiExplorerNodes.createSubOperationTreeNode(OperationKey.valueOf(subOP));

						// LOAD REPORTS ON CURRENT SUB-OPERATION
						if (!subOP.getOperationType().equals(OPType.HARDY_WEINBERG)) { // NOT IF HW
							List<Report> reportsList = ReportsList.getReportsList(subOP.getId(), MatrixKey.NULL_ID);
							for (int n = 0; n < reportsList.size(); n++) {
								Report rp = reportsList.get(n);
								if (!rp.getReportType().equals(OPType.ALLELICTEST)
										&& !rp.getReportType().equals(OPType.GENOTYPICTEST)
										&& !rp.getReportType().equals(OPType.TRENDTEST)) {
									DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(ReportKey.valueOf(reportsList.get(n)));
									subOperationItem.add(reportItem);
								}

							}
						}
						operationItem.add(subOperationItem);
					}

					// START TESTING
					// LOAD REPORTS ON CURRENT OPERATION
					List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), MatrixKey.NULL_ID);
					if (!currentOP.getOperationType().equals(OPType.SAMPLE_QA)) { // SAMPLE_QA MUST BE DEALT DIFFERENTLY
						for (int n = 0; n < reportsList.size(); n++) {
							DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(ReportKey.valueOf(reportsList.get(n)));
							operationItem.add(reportItem);
						}
					} else {
						// DEAL WITH SAMPLE_HTZYPLOT
						for (int n = 0; n < reportsList.size(); n++) {
							Report rp = reportsList.get(n);
							if (rp.getReportType().equals(OPType.SAMPLE_HTZYPLOT)) {
								DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(ReportKey.valueOf(reportsList.get(n)));
								operationItem.add(reportItem);
							}
						}
					}
					// END TESTING

					matrixItem.add(operationItem);

				}
				studyItem.add(matrixItem);
			}

			// ADD ALL TREE-NODES INTO TREE
			category.add(studyItem);
		}

		top.add(category);
		//</editor-fold>
	}

	//<editor-fold defaultstate="expanded" desc="LISTENER">
	// TREE SELECTION LISTENER
	private static final TreeSelectionListener treeListener = new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent evt) {

			JTree tree = (JTree) evt.getSource();
			GWASpiExplorerPanel gwasPiExplorerPanel = GWASpiExplorerPanel.getSingleton();

			tree.setEnabled(false);

			// CHECK IF LISTENER IS ALLOWED TO UPDATE CONTENT PANEL
			if (!gwasPiExplorerPanel.isRefreshContentPanel()) {
				tree.setEnabled(true);
				return;
			}

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (currentNode == null) {
				tree.setEnabled(true);
				return;
			}

			// Check first if we are at the GWASpi root
			if (currentNode.isRoot()) { // We are in GWASpi node
				gwasPiExplorerPanel.setPnl_Content(new IntroPanel());
				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
			}

			// Check where we are in tree and show appropiate content panel
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentElementInfo = (NodeElementInfo) currentElement;

			TreePath treePath = evt.getPath();
			if ((treePath != null) && !currentElementInfo.getNodeType().equals(Text.App.treeParent)) {
				try {
					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, currentElementInfo.getNodeUniqueName());
				} catch (IOException ex) {
					log.error(null, ex);
				}
			} else if (currentElement.equals(Text.App.treeStudyManagement)) {
				try {
					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, Text.App.treeStudyManagement);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			} else {
				try {
					Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, Text.App.appName);
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}

			// Get parent node of currently selected node
			NodeElementInfo parentElementInfo = null;
			if ((treePath != null) && (treePath.getParentPath() != null)) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePath.getParentPath().getLastPathComponent();
				Object parentElement = parentNode.getUserObject();
				parentElementInfo = (NodeElementInfo) parentElement;
			}

			// Reference Databse Branch
			if (currentElementInfo.getNodeUniqueName().equals(Text.App.treeReferenceDBs)) {
			} // Study Management Branch
			//else if(currentElementInfo.getNodeType().equals(Text.App.treeStudyManagement)) { // XXX
			else if (currentElementInfo.getNodeUniqueName().equals(Text.App.treeStudyManagement)) {
				try {
					// We are in StudyList node
					gwasPiExplorerPanel.setPnl_Content(new StudyManagementPanel());
					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			} // Study Branch
			//else if(parentNode != null && parentNode.toString().equals(Text.App.treeStudyManagement)){
			else if (currentElementInfo.getNodeType().equals(Text.App.treeStudy)) {
				try {
					gwasPiExplorerPanel.setPnl_Content(new CurrentStudyPanel((StudyKey) currentElementInfo.getContentKey()));
					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
				} catch (IOException ex) {
					log.warn("StudyID: " + currentElementInfo.getNodeId(), ex);
				}
			} // Sample Info Branch
			else if (currentElementInfo.getNodeType().equals(Text.App.treeSampleInfo)) {
				try {
					gwasPiExplorerPanel.setPnl_Content(new Report_SampleInfoPanel(new StudyKey(parentElementInfo.getNodeId())));
					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			} // Matrix Branch
			else if (currentElementInfo.getNodeType().equals(Text.App.treeMatrix)) {
				try {
					// We are in MatrixItems node
					tree.expandPath(treePath);
					gwasPiExplorerPanel.setPnl_Content(new CurrentMatrixPanel(new MatrixKey(new StudyKey(currentElementInfo.getParentNodeId()), currentElementInfo.getNodeId())));
					gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			} // Operations Branch
			else if (currentElementInfo.getNodeType().equals(Text.App.treeOperation)) {
				try {
					if (parentElementInfo.getNodeType().toString().equals(Text.App.treeOperation)) {
						// Display SubOperation analysis panel
						tree.expandPath(treePath);
						OperationMetadata currentOP = OperationsList.getById(currentElementInfo.getNodeId());
						OperationMetadata parentOP = OperationsList.getById(parentElementInfo.getNodeId());
						if (currentOP.getOperationType().equals(OPType.HARDY_WEINBERG)) {
							// Display HW Report
							List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), currentOP.getParentMatrixId());
							if (reportsList.size() > 0) {
								Report hwReport = reportsList.get(0);
								String reportFile = hwReport.getFileName();
								gwasPiExplorerPanel.setPnl_Content(new Report_HardyWeinbergSummary(hwReport.getParentOperationKey(), reportFile));
								gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
							}
						} else if (currentOP.getOperationType().equals(OPType.ALLELICTEST)) {
							// Display Association Report
							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getParentMatrixKey(), OperationKey.valueOf(currentOP), null));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						} else if (currentOP.getOperationType().equals(OPType.GENOTYPICTEST)) {
							// Display Association Report
							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getParentMatrixKey(), OperationKey.valueOf(currentOP), null));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						} else if (currentOP.getOperationType().equals(OPType.TRENDTEST)) {
							// Display Trend Test Report
							gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(currentOP.getParentMatrixKey(), OperationKey.valueOf(currentOP), null));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						} else {
							//gwasPiExplorerPanel.pnl_Content = new MatrixAnalysePanel(parentOP.getParentMatrixId(), currentElementInfo.parentNodeId);
							MatrixKey matrixKey = new MatrixKey(parentOP.getStudyKey(), parentOP.getParentMatrixId());
							OperationKey operationKey = new OperationKey(matrixKey, currentElementInfo.getNodeId());
							gwasPiExplorerPanel.setPnl_Content(new MatrixAnalysePanel(matrixKey, operationKey));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						}
					} else {
						// Display Operation
						tree.expandPath(treePath);
						OperationMetadata currentOP = OperationsList.getById(currentElementInfo.getNodeId());
						if (currentOP.getOperationType().equals(OPType.MARKER_QA)) {
							// Display MarkerQA panel
							gwasPiExplorerPanel.setPnl_Content(new MatrixMarkerQAPanel(new MatrixKey(currentOP.getStudyKey(), currentOP.getParentMatrixId()), currentOP.getId()));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						} else if (currentOP.getOperationType().equals(OPType.SAMPLE_QA)) {
							// Display SampleQA Report
							List<Report> reportsList = ReportsList.getReportsList(currentOP.getId(), currentOP.getParentMatrixId());
							if (reportsList.size() > 0) {
								Report sampleQAReport = reportsList.get(0);
								String reportFile = sampleQAReport.getFileName();
								gwasPiExplorerPanel.setPnl_Content(new Report_QASamplesSummary(sampleQAReport.getParentOperationKey(), reportFile));
								gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
							}
						} else {
							// Display Operation analysis panel
							MatrixKey matrixKey = new MatrixKey(currentOP.getStudyKey(), parentElementInfo.getNodeId());
							OperationKey operationKey = new OperationKey(matrixKey, currentElementInfo.getNodeId());
							gwasPiExplorerPanel.setPnl_Content(new MatrixAnalysePanel(matrixKey, operationKey));
							gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
						}
					}
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			} // Reports Branch
			else if (currentElementInfo.getNodeType().equals(Text.App.treeReport)) {
				try {
					// Display report summary
					tree.expandPath(treePath);
					Report rp = ReportsList.getReport((ReportKey) currentElementInfo.getContentKey());
					String reportFile = rp.getFileName();
					if (rp.getReportType().equals(OPType.SAMPLE_HTZYPLOT)) {
						gwasPiExplorerPanel.setPnl_Content(new SampleQAHetzygPlotZoom(rp.getParentOperationKey()));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
					if (rp.getReportType().equals(OPType.ALLELICTEST)) {
						//gwasPiExplorerPanel.pnl_Content = new Report_AssociationSummary(rp.getId(), reportFile, rp.getParentOperationId(), null);
						gwasPiExplorerPanel.setPnl_Content(new Report_AnalysisPanel(rp.getParentMatrixKey(), rp.getParentOperationKey(), null));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
					if (rp.getReportType().equals(OPType.QQPLOT)) {
						gwasPiExplorerPanel.setPnl_Content(new ChartDefaultDisplay(reportFile, rp.getParentOperationKey()));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
					if (rp.getReportType().equals(OPType.MANHATTANPLOT)) {
						gwasPiExplorerPanel.setPnl_Content(new ManhattanChartDisplay(reportFile, rp.getParentOperationKey()));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
					if (rp.getReportType().equals(OPType.MARKER_QA)) {
						gwasPiExplorerPanel.setPnl_Content(new Report_QAMarkersSummary(rp.getStudyKey(), reportFile, rp.getParentOperationKey()));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
//					if(rp.getReportType().equals(OPType.SAMPLE_QA.toString())){
//						gwasPiExplorerPanel.pnl_Content = new Report_QASamplesSummary(rp.getId(), reportFile, rp.getParentOperationId());
//						gwasPiExplorerPanel.scrl_Content.setViewportView(gwasPiExplorerPanel.pnl_Content);
//					}
					if (rp.getReportType().equals(OPType.HARDY_WEINBERG)) {
						gwasPiExplorerPanel.setPnl_Content(new Report_HardyWeinbergSummary(rp.getParentOperationKey(), reportFile));
						gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
					}
				} catch (IOException ex) {
					log.warn(null, ex);
				}
			} else {
				gwasPiExplorerPanel.setPnl_Content(new IntroPanel());
				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
			}

			// THIS IS TO AVOID RANDOM MONKEY CLICKER BUG
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				log.warn(null, ex);
			}

			tree.setEnabled(true);
		}
	};

	// PRE-EXPANSION/COLLAPSE LISTENER
	private static class MyTreeWillExpandListener implements TreeWillExpandListener {

		public void treeWillExpand(TreeExpansionEvent evt) throws ExpandVetoException {
			// Get the path that will be expanded
			TreePath treePath = evt.getPath();

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentNodeInfo = (NodeElementInfo) currentElement;
			if (currentNodeInfo.isCollapsable()) {
				// ALLWAYS ALLOW EXPANSION
			}
		}

		public void treeWillCollapse(TreeExpansionEvent evt) throws ExpandVetoException {
			// Get the path that will be expanded
			TreePath treePath = evt.getPath();

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			Object currentElement = currentNode.getUserObject();
			NodeElementInfo currentNodeInfo = (NodeElementInfo) currentElement;
			if (!currentNodeInfo.isCollapsable()) {
				// VETO EXPANSION
				throw new ExpandVetoException(evt);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	private static List<OperationMetadata> getChildrenOperations(List<OperationMetadata> operations, int parentOpId) {

		List<OperationMetadata> childrenOperations = new ArrayList<OperationMetadata>();

		for (int i = 0; i < operations.size(); i++) {
			int currentParentOPId = (Integer) operations.get(i).getParentOperationId();
			if (currentParentOPId == parentOpId) {
				childrenOperations.add(operations.get(i));
			}
		}

		return childrenOperations;
	}

	// XXX this function could be used in other classes too!
	private static Icon initIcon(String iconName) {
		URL logoPath = GWASpiExplorer.class.getResource("/img/icon/" + iconName);
		//String logoPath = Config.getConfigValue("ConfigDir", "") + "/" +iconName;
		Icon logo = new ImageIcon(logoPath);
		return logo;
	}
	//</editor-fold>
}
