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

import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.CurrentMatrixPanel;
import org.gwaspi.gui.CurrentStudyPanel;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.IntroPanel;
import org.gwaspi.gui.DataSetAnalysePanel;
import org.gwaspi.gui.MatrixMarkerQAPanel;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.gui.StudyManagementPanel;
import org.gwaspi.gui.reports.ChartDefaultDisplay;
import org.gwaspi.gui.reports.ManhattanChartDisplay;
import org.gwaspi.gui.reports.Report_AnalysisAllelicTestImpl;
import org.gwaspi.gui.reports.Report_AnalysisGenotypicTestImpl;
import org.gwaspi.gui.reports.Report_AnalysisPanel;
import org.gwaspi.gui.reports.Report_AnalysisTrendTestImpl;
import org.gwaspi.gui.reports.Report_HardyWeinbergSummary;
import org.gwaspi.gui.reports.Report_QAMarkersSummary;
import org.gwaspi.gui.reports.Report_QASamplesSummary;
import org.gwaspi.gui.reports.Report_SampleInfoPanel;
import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
import org.gwaspi.model.GWASpiExplorerNodes.NodeElementInfo;
import org.gwaspi.model.GWASpiExplorerNodes.UncollapsableNodeElementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main data-structure GUI tree of the application
 */
public class GWASpiExplorerTree {

	private static final Logger log = LoggerFactory.getLogger(GWASpiExplorerTree.class);

	private JTree tree;
	private final boolean playWithLineStyle;
	/** Possible values are "Angled" (the default), "Horizontal", and "None". */
	private final String lineStyle;
	// Optionally set the look and feel.
	private static final Icon customOpenIcon = initIcon("hex_open.png");
	private static final Icon customClosedIcon = initIcon("hex_closed.png");
	private static final Icon customLeafIcon = initIcon("leaf_sepia.png");

	public GWASpiExplorerTree() {

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
				NodeElementInfo.NodeType.ROOT,
				StartGWASpi.constructHighlyVisibleApplicationName(),
				null));
		GWASpiExplorerNodes.addNode(null, top, false);
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


	private static void addOperationToTree(DefaultMutableTreeNode parent, List<OperationMetadata> allOperations, OperationKey operationKey) throws IOException {

		DefaultMutableTreeNode operationItem = GWASpiExplorerNodes.createOperationTreeNode(operationKey);

		List<OperationMetadata> childrenOps = getChildrenOperations(allOperations, operationKey);
		for (OperationMetadata subOP : childrenOps) {
			OperationKey subOPKey = OperationKey.valueOf(subOP);
			addOperationToTree(operationItem, allOperations, subOPKey);
		}

		List<Report> reports = ReportsList.getReportsList(operationKey);
		for (Report report : reports) {
//			if (!report.getReportType().equals(OPType.ALLELICTEST)
//					&& !report.getReportType().equals(OPType.GENOTYPICTEST)
//					&& !report.getReportType().equals(OPType.TRENDTEST))
//			{
			DefaultMutableTreeNode reportItem = GWASpiExplorerNodes.createReportTreeNode(ReportKey.valueOf(report));
			GWASpiExplorerNodes.addNode(operationItem, reportItem, false);
//			}
		}

		GWASpiExplorerNodes.addNode(parent, operationItem, false);
	}

	private static void growTree(DefaultMutableTreeNode top) throws IOException {

		DefaultMutableTreeNode category = new DefaultMutableTreeNode(new UncollapsableNodeElementInfo(
				NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.ROOT, NodeElementInfo.NODE_ID_NONE),
				NodeElementInfo.NodeType.STUDY_MANAGEMENT,
				Text.App.treeStudyManagement,
				null));
		GWASpiExplorerNodes.addNode(top, category, false);

		// LOAD ALL STUDIES
		List<Study> studies = StudyList.getStudyList();
		for (Study study : studies) {
			final StudyKey studyKey = StudyKey.valueOf(study);

			// LOAD CURRENT STUDY
			DefaultMutableTreeNode studyItem = GWASpiExplorerNodes.createStudyTreeNode(study);

			// LOAD SAMPLE INFO FOR CURRENT STUDY
			DefaultMutableTreeNode sampleInfoItem = GWASpiExplorerNodes.createSampleInfoTreeNode(studyKey);
			if (sampleInfoItem != null) {
				GWASpiExplorerNodes.addNode(studyItem, sampleInfoItem, false);
			}

			// LOAD MATRICES FOR CURRENT STUDY
			List<MatrixKey> matrices = MatricesList.getMatrixList(studyKey);
			for (MatrixKey matrixKey : matrices) {
				DefaultMutableTreeNode matrixItem = GWASpiExplorerNodes.createMatrixTreeNode(matrixKey);

				// LOAD ROOT OPERATIONS (having the matrix as direct parent)
				List<OperationMetadata> rootOperations = OperationsList.getChildrenOperationsMetadata(new DataSetKey(matrixKey));
				List<OperationMetadata> allOperations = OperationsList.getOffspringOperationsMetadata(matrixKey);
				for (OperationMetadata rootOperation : rootOperations) {
					// LOAD SUB OPERATIONS (having an operation as direct parent)
					OperationMetadata currentOP = rootOperation;
					OperationKey currentOPKey = OperationKey.valueOf(currentOP);
					addOperationToTree(matrixItem, allOperations, currentOPKey);
				}

				GWASpiExplorerNodes.addNode(studyItem, matrixItem, false);
			}

			GWASpiExplorerNodes.addNode(category, studyItem, false);
		}
	}

	//<editor-fold defaultstate="expanded" desc="LISTENER">
	// TREE SELECTION LISTENER
	private static final TreeSelectionListener treeListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent evt) {

			JTree tree = (JTree) evt.getSource();
			GWASpiExplorerPanel gwasPiExplorerPanel = GWASpiExplorerPanel.getSingleton();

			tree.setEnabled(false);

			// CHECK IF LISTENER IS ALLOWED TO UPDATE CONTENT PANEL
			if (!gwasPiExplorerPanel.isRefreshContentPanel()) {
				tree.setEnabled(true);
				return;
			}

			if (tree.isSelectionEmpty()) {
				gwasPiExplorerPanel.setPnl_Content(new JPanel());
				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
			}

			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (currentNode == null) {
				// TODO Maybe, in this case, we should auto-select the root node, or something even more intelligent?
				tree.setEnabled(true);
				return;
			}

			// Check first if we are at the GWASpi root
			if (currentNode.isRoot()) { // We are in GWASpi node
				gwasPiExplorerPanel.setPnl_Content(new IntroPanel());
				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
			}

			// Check where we are in the tree,
			// and show the appropriate content panel.
			NodeElementInfo currentElementInfo = (NodeElementInfo) currentNode.getUserObject();
			TreePath treePath = evt.getPath();

			try {
				Config.setConfigValue(Config.PROPERTY_LAST_SELECTED_NODE, currentElementInfo.getNodeId());
			} catch (IOException ex) {
				log.error(null, ex);
			}

			// Get parent node of currently selected node
			NodeElementInfo parentElementInfo = null;
			if ((treePath != null) && (treePath.getParentPath() != null)) {
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treePath.getParentPath().getLastPathComponent();
				Object parentElement = parentNode.getUserObject();
				parentElementInfo = (NodeElementInfo) parentElement;
			}

			try {
				JPanel newContent = null;
				switch (currentElementInfo.getNodeType()) {
					case STUDY_MANAGEMENT: {
						newContent = new StudyManagementPanel();
					} break;
					case STUDY: {
						newContent = new CurrentStudyPanel((StudyKey) currentElementInfo.getContentKey());
					} break;
					case SAMPLE_INFO: {
						newContent = new Report_SampleInfoPanel((StudyKey) parentElementInfo.getContentKey());
					} break;
					case MATRIX: {
						tree.expandPath(treePath);
						newContent = new CurrentMatrixPanel((MatrixKey) currentElementInfo.getContentKey());
					} break;
					case OPERATION: {
						tree.expandPath(treePath);
						final OperationKey currentOPKey = (OperationKey) currentElementInfo.getContentKey();
						final OperationMetadata currentOP = OperationsList.getOperationMetadata(currentOPKey);
						switch (currentOP.getType()) {
							case HARDY_WEINBERG: {
								// Display HW Report
								List<Report> reportsList = ReportsList.getReportsList(currentOPKey);
								if (!reportsList.isEmpty()) {
									Report hwReport = reportsList.get(0);
									String reportFile = hwReport.getFileName();
									newContent = new Report_HardyWeinbergSummary(hwReport.getParentOperationKey(), reportFile);
								}
							} break;
							case ALLELICTEST: {
								// Display Association Report
								newContent = new Report_AnalysisPanel(currentOPKey.getParentMatrixKey(), currentOPKey, null);
							} break;
							case GENOTYPICTEST: {
								// Display Association Report
								newContent = new Report_AnalysisPanel(currentOPKey.getParentMatrixKey(), currentOPKey, null);
							} break;
							case TRENDTEST: {
								// Display Trend Test Report
								newContent = new Report_AnalysisPanel(currentOPKey.getParentMatrixKey(), currentOPKey, null);
							} break;
							case MARKER_QA: {
								// Display MarkerQA panel
								newContent = new MatrixMarkerQAPanel(new MatrixKey(currentOP.getStudyKey(), currentOP.getParentMatrixId()), currentOP.getId());
							} break;
							case SAMPLE_QA: {
								// Display SampleQA Report
//								final List<Report> reportsList = ReportsList.getReportsList(currentOPKey);
//								if (reportsList.isEmpty()) {
//									newContent = newErrorContent("No reports found for " + currentOPKey);
//								} else {
//									final Report sampleQAReport = reportsList.get(0);
//									final String reportFile = sampleQAReport.getFileName();
//									newContent = new Report_QASamplesSummary(sampleQAReport.getParentOperationKey(), reportFile);
//								}
								JPanel emptyPanel =  new JPanel();
								emptyPanel.setLayout(new FlowLayout());
								emptyPanel.add(new JLabel("Please select the report instead")); // HACK We do this, because we do not want to show the above (Report_QASamplesSummary), because it is already shown when selecting that report in the tree directly
								newContent = emptyPanel;
							} break;
							default: {
								newContent = new DataSetAnalysePanel(new DataSetKey(currentOPKey)); // XXX this does not make much sense, does it?
							}
						}
					} break;
					case REPORT: {
						// Display report summary
						tree.expandPath(treePath);
						final Report rp = ReportsList.getReport((ReportKey) currentElementInfo.getContentKey());
						final String reportFile = rp.getFileName();
						switch (rp.getReportType()) {
							case SAMPLE_HTZYPLOT: {
								newContent = new SampleQAHetzygPlotZoom(rp.getParentOperationKey());
							} break;
							case ALLELICTEST: {
								newContent = new Report_AnalysisAllelicTestImpl(rp.getParentOperationKey(), reportFile, null);
							} break;
							case GENOTYPICTEST: {
								newContent = new Report_AnalysisGenotypicTestImpl(rp.getParentOperationKey(), reportFile, null);
							} break;
							case TRENDTEST: {
								newContent = new Report_AnalysisTrendTestImpl(rp.getParentOperationKey(), reportFile, null);
							} break;
							case QQPLOT: {
								newContent = new ChartDefaultDisplay(reportFile, rp.getParentOperationKey());
							} break;
							case MANHATTANPLOT: {
								newContent = new ManhattanChartDisplay(reportFile, rp.getParentOperationKey());
							} break;
							case MARKER_QA: {
								newContent = new Report_QAMarkersSummary(rp.getStudyKey(), reportFile, rp.getParentOperationKey());
							} break;
							case SAMPLE_QA: {
								newContent = new Report_QASamplesSummary(rp.getParentOperationKey(), reportFile);
							} break;
							case HARDY_WEINBERG: {
								newContent = new Report_HardyWeinbergSummary(rp.getParentOperationKey(), reportFile);
							} break;
							default: {
								log.warn("Not directly supported report type: " + rp.getReportType().name() + " - showing generic report info");
								newContent = new Report_AnalysisPanel(rp.getParentMatrixKey(), rp.getParentOperationKey(), null);
//								newContent = newErrorContent("Unsupported report type: " + rp.getReportType().name());
							}
						}
					} break;
					default: {
						newContent = new IntroPanel();
					}
				}
				if (newContent == null) {
					newContent = newErrorContent("No idea what I should show!");
				}
				gwasPiExplorerPanel.setPnl_Content(newContent);
				gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());
			} catch (IOException ex) {
				log.warn(null, ex);
			}
			gwasPiExplorerPanel.getScrl_Content().setViewportView(gwasPiExplorerPanel.getPnl_Content());

			// THIS IS TO AVOID RANDOM MONKEY CLICKER BUG
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				log.warn(null, ex);
			}

			tree.setEnabled(true);
		}
	};

	private static JPanel newErrorContent(String errorMessage) {

		JPanel errorPanel =  new JPanel();
		errorPanel.setLayout(new FlowLayout());
		errorPanel.add(new JLabel(errorMessage));

		log.error(errorMessage);

		return errorPanel;
	}

	// PRE-EXPANSION/COLLAPSE LISTENER
	private static class MyTreeWillExpandListener implements TreeWillExpandListener {

		@Override
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

		@Override
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
	/**
	 * Extract operations from the given list that have the given parent as parent.
	 * @param allOperations
	 * @param parentOpKey
	 * @return
	 */
	private static List<OperationMetadata> getChildrenOperations(List<OperationMetadata> allOperations, OperationKey parentOpKey) {

		List<OperationMetadata> childrenOperations = new ArrayList<OperationMetadata>();

		for (OperationMetadata curOperation : allOperations) {
			OperationKey currentOpParentOpKey = curOperation.getParentOperationKey();
			if (currentOpParentOpKey.equals(parentOpKey)) {
				childrenOperations.add(curOperation);
			}
		}

		return childrenOperations;
	}

	// XXX this function could be used in other classes too!
	private static Icon initIcon(String iconName) {
		URL logoPath = GWASpiExplorerTree.class.getResource("/img/icon/" + iconName);
		//String logoPath = Config.getConfigValue("ConfigDir", "") + "/" +iconName;
		Icon logo = new ImageIcon(logoPath);
		return logo;
	}
	//</editor-fold>
}
