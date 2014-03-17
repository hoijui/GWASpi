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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.StartGWASpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GWASpiExplorerNodes {

	private static final Logger log
			= LoggerFactory.getLogger(GWASpiExplorerNodes.class);

	private static final Map<Integer, DefaultMutableTreeNode> nodeIdToNode = new HashMap<Integer, DefaultMutableTreeNode>();

	private GWASpiExplorerNodes() {
	}

	public static class NodeElementInfo {

		public static enum NodeType {
			ROOT,
			STUDY_MANAGEMENT,
			STUDY,
			SAMPLE_INFO,
			MATRIX,
			OPERATION,
			REPORT;
		}

		public static final int NODE_ID_NONE = 0;

		private final int nodeId;
		private final int parentNodeId;
		private final NodeType nodeType;
		private final String nodeName;
		/** This may contain a StudyKey, MatrixKey, OperationKey, ReportKey, ... */
		private final Object contentKey;
		private boolean collapsable;

		public NodeElementInfo(
				int parentNodeId,
//				int nodeId,
				NodeType nodeType,
				String nodeName,
				Object contentKey)
		{
			this.parentNodeId = parentNodeId;
//			this.nodeId = nodeId;
			this.nodeId = createUniqueId(nodeType, contentKey);
			this.nodeType = nodeType;
			this.nodeName = nodeName;
			this.contentKey = contentKey;
			this.collapsable = false;
		}

		public static int createUniqueId(NodeType nodeType, Object contentKey) {
			return (nodeType + " " + contentKey).hashCode();
		}

		@Override
		public String toString() {
			return nodeName;
		}

		public int getParentNodeId() {
			return parentNodeId;
		}

		public int getNodeId() {
			return nodeId;
		}

		public NodeType getNodeType() {
			return nodeType;
		}

		public String getNodeName() {
			return nodeName;
		}

		public Object getContentKey() {
			return contentKey;
		}

		public boolean isCollapsable() {
			return collapsable;
		}

		public void setCollapsable(boolean collapsable) {
			this.collapsable = collapsable;
		}
	}

	public static class UncollapsableNodeElementInfo extends NodeElementInfo {

		public UncollapsableNodeElementInfo(
				int parentNodeId,
				NodeType nodeType,
				String nodeName,
				Object contentKey)
		{
			super(parentNodeId, nodeType, nodeName, contentKey);
		}

		@Override
		public boolean isCollapsable() {
			return false;
		}
	}

	protected static DefaultMutableTreeNode createStudyTreeNode(Study study) {

		DefaultMutableTreeNode tn = new DefaultMutableTreeNode(
				new NodeElementInfo(
				NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY_MANAGEMENT, null),
				NodeElementInfo.NodeType.STUDY,
				"SID: " + study.getId() + " - " + study.getName(), // will be result of toString() call of DefaultMutableTreeNode
				StudyKey.valueOf(study)));

		return tn;
	}

	protected static DefaultMutableTreeNode createMatrixTreeNode(MatrixKey matrixKey) {

		DefaultMutableTreeNode tn = null;
		try {
			MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);
			tn = new DefaultMutableTreeNode(new NodeElementInfo(
					NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, matrixKey.getStudyKey()),
					NodeElementInfo.NodeType.MATRIX,
					"MX: " + matrixKey.getMatrixId() + " - " + matrixMetadata.getFriendlyName(),
					matrixKey));
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return tn;
	}

	protected static DefaultMutableTreeNode createSampleInfoTreeNode(StudyKey studyKey) throws IOException {

		DefaultMutableTreeNode tn = null;

		// CHECK IF STUDY EXISTS
		List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(studyKey);
		if (!sampleInfos.isEmpty()) {
			tn = new DefaultMutableTreeNode(new NodeElementInfo(
					NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, studyKey),
					NodeElementInfo.NodeType.SAMPLE_INFO, // nodeType
					Text.App.treeSampleInfo,
					studyKey)); // nodeUniqueName
		}

		return tn;
	}

	protected static DefaultMutableTreeNode createOperationTreeNode(OperationKey operationKey) {

		DefaultMutableTreeNode tn = null;
		try {
			OperationMetadata op = OperationsList.getOperationMetadata(operationKey);

			DataSetKey parent = op.getParent();
			final int parentNodeId;
			if (parent.isMatrix()) {
				parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, parent.getMatrixParent());
			} else {
				parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, parent.getOperationParent());
			}

			tn = new DefaultMutableTreeNode(new NodeElementInfo(
					parentNodeId,
					NodeElementInfo.NodeType.OPERATION,
					"OP: " + operationKey.getId() + " - " + op.getFriendlyName(),
					operationKey));
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return tn;
	}

	protected static DefaultMutableTreeNode createSubOperationTreeNode(OperationKey operationKey) {
		return createOperationTreeNode(operationKey);
	}

	protected static DefaultMutableTreeNode createReportTreeNode(ReportKey reportKey) {

		DefaultMutableTreeNode tn = null;
		try {
			Report rp = ReportsList.getReport(reportKey);

			final int parentNodeId;
			if (rp.getParentMatrixId() == MatrixKey.NULL_ID) {
				parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, rp.getStudyKey());
			} else {
				if (rp.getParentOperationId() == OperationKey.NULL_ID) {
					parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, rp.getParentMatrixKey());
				} else {
					parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, rp.getParentOperationKey());
				}
			}

			tn = new DefaultMutableTreeNode(new NodeElementInfo(
					parentNodeId,
					NodeElementInfo.NodeType.REPORT,
					"RP: " + reportKey.getId() + " - " + rp.getFriendlyName(),
					reportKey));
		} catch (IOException ex) {
			log.error(null, ex);
		}

		return tn;
	}

	//<editor-fold defaultstate="expanded" desc="NODE MANAGEMENT">
	//<editor-fold defaultstate="expanded" desc="STUDY NODES">
	public static void insertStudyNode(StudyKey studyKey) throws IOException {

		if (StartGWASpi.guiMode) {
			try {
				// GET STUDY
				final int parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY_MANAGEMENT, null);
				DefaultMutableTreeNode parentNode = findTreeNode(parentNodeId);

				if (parentNode == null) {
					throw new IOException("failed to find parent node");
				}

				Study study = StudyList.getStudy(studyKey);
				DefaultMutableTreeNode newNode = createStudyTreeNode(study);
				addNode(parentNode, newNode, true);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	public static void deleteStudyNode(StudyKey studyKey) {

		try {
			// GET DELETE PATH BY PREFIX ONLY
			final int nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, studyKey);
			DefaultMutableTreeNode node = findTreeNode(nodeId);

			if (node != null) {
				deleteNode(node);
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="MATRIX NODES">
	public static void insertMatrixNode(MatrixKey matrixKey) throws IOException {

		if (StartGWASpi.guiMode) {
			try {
				// GET STUDY
				final int parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.STUDY, matrixKey.getStudyKey());
				DefaultMutableTreeNode parentNode = findTreeNode(parentNodeId);

				if (parentNode == null) {
					throw new IOException("failed to find parent node");
				}

				DefaultMutableTreeNode newNode = createMatrixTreeNode(matrixKey);
				addNode(parentNode, newNode, true);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	public static void deleteMatrixNode(MatrixKey matrixKey) {

		try {
			// GET DELETE PATH BY PREFIX ONLY
			final int nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, matrixKey);
			DefaultMutableTreeNode node = findTreeNode(nodeId);

			if (node != null) {
				deleteNode(node);
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="OPERATION NODES">
	public static void insertOperationUnderMatrixNode(OperationKey operationKey) throws IOException {

		if (StartGWASpi.guiMode) {
			try {
				// GET MATRIX
				final int parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.MATRIX, operationKey.getParentMatrixKey());
				DefaultMutableTreeNode parentNode = findTreeNode(parentNodeId);

				if (parentNode == null) {
					throw new IOException("failed to find parent node");
				}

				DefaultMutableTreeNode newNode = createOperationTreeNode(operationKey);
				addNode(parentNode, newNode, true);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	public static void insertSubOperationUnderOperationNode(OperationKey parentOpKey, OperationKey operationKey) throws IOException {

		if (StartGWASpi.guiMode) {
			try {
				final int parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, parentOpKey);
				DefaultMutableTreeNode parentNode = findTreeNode(parentNodeId);

				if (parentNode == null) {
					throw new IOException("failed to find parent node");
				}

				DefaultMutableTreeNode newNode = createOperationTreeNode(operationKey);
				addNode(parentNode, newNode, true);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	public static void deleteOperationNode(OperationKey operationKey) {

		try {
			// GET DELETE PATH BY PREFIX ONLY
			final int nodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, operationKey);
			DefaultMutableTreeNode node = findTreeNode(nodeId);

			if (node != null) {
				deleteNode(node);
			}
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="REPORT NODES">
	public static void insertReportsUnderOperationNode(OperationKey parentOpKey) throws IOException {

		if (StartGWASpi.guiMode) {
			try {
				// GET OPERATION
				final int parentNodeId = NodeElementInfo.createUniqueId(NodeElementInfo.NodeType.OPERATION, parentOpKey);
				DefaultMutableTreeNode parentNode = findTreeNode(parentNodeId);

				if (parentNode == null) {
					throw new IOException("failed to find parent node");
				}

//				OperationMetadata parentOP = OperationsList.getOperationMetadata(parentOpKey);

				// GET ALL REPORTS UNDER THIS OPERATION
				List<Report> reports = ReportsList.getReportsList(parentOpKey);
				for (Report report : reports) {
//					// DON'T SHOW SUPERFLUOUS OPEARATION INFO
//					if (!parentOP.getOperationType().equals(OPType.HARDY_WEINBERG)
//							&& !parentOP.getOperationType().equals(OPType.SAMPLE_QA)
//							&& !report.getReportType().equals(OPType.ALLELICTEST))
//					{
					DefaultMutableTreeNode newNode = createReportTreeNode(ReportKey.valueOf(report));
					addNode(parentNode, newNode, true);
//					}
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>

	public static DefaultMutableTreeNode findTreeNode(final int nodeId) {
		return nodeIdToNode.get(nodeId);
	}

	public static DefaultMutableTreeNode addNode(
			DefaultMutableTreeNode parentNode,
			DefaultMutableTreeNode child,
			boolean shouldBeVisible)
	{
		final JTree tree = GWASpiExplorerPanel.getSingleton().getTree();

		if (parentNode == null) {
			if (((NodeElementInfo) child.getUserObject()).getNodeType() != NodeElementInfo.NodeType.ROOT) {
				throw new IllegalArgumentException("Code is faulty! Only the root may have a null parent.");
			}
		} else if (tree == null) {
			// this happens during initial tree creation, on application startup
			parentNode.add(child);
		} else {
			DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
			treeModel.insertNodeInto(child, parentNode, parentNode.getChildCount());

			tree.expandPath(new TreePath(parentNode.getPath()));
		}

		nodeIdToNode.put(((NodeElementInfo) child.getUserObject()).getNodeId(), child);

		return child;
	}

	public static DefaultMutableTreeNode deleteNode(DefaultMutableTreeNode child) {

		DefaultTreeModel treeModel = (DefaultTreeModel) GWASpiExplorerPanel.getSingleton().getTree().getModel();
		treeModel.removeNodeFromParent(child);

		nodeIdToNode.remove(((NodeElementInfo) child.getUserObject()).getNodeId());

		return child;
	}
	//</editor-fold>
}
