/* Copyright (c) Ian Schneider, OSGeo Foundation (LGPL 2 or later) */

package org.gwaspi.trastero;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Ian Schneider
 */
public class TreeMemory extends JPanel {

	private final JTree tree;
	private final TreeModel model1;
	private final TreeModel model2;
	private List<TreePath> lastExpanded;

	/**
	 * Creates a new instance of TreeMemory
	 */
	public TreeMemory() {
		super(new BorderLayout());
		JScrollPane scroller = new JScrollPane(tree = new JTree());
		add(scroller, BorderLayout.CENTER);
		JButton button = new JButton("Change Model");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				changeModel();
			}
		});
		add(button, BorderLayout.SOUTH);
		model1 = model(1);
		model2 = model(2);
		tree.setModel(model1);
	}

	void changeModel() {

		TreeModel currentModel = tree.getModel();
		TreeModel nextModel = currentModel == model1 ? model2 : model1;

		TreePath rootPath = tree.getPathForRow(0);
		Enumeration<TreePath> e = tree.getExpandedDescendants(rootPath);
		List<TreePath> expanded = new ArrayList<TreePath>();
		while (e.hasMoreElements()) {
			expanded.add(e.nextElement());
		}
		tree.setModel(nextModel);
		if (lastExpanded != null) {
			for (int i = 0, ii = lastExpanded.size(); i < ii; i++) {
				tree.expandPath(lastExpanded.get(i));
			}
		}
		lastExpanded = expanded;
	}

	private TreeModel model(int n) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root " + n);
		addNodes(root, 3);
		return new DefaultTreeModel(root);
	}

	private void addNodes(MutableTreeNode node, int depth) {
		if (depth == 0) {
			return;
		}
		int r = (int) (10 * Math.random()) + 1;
		for (int i = 0; i < r; i++) {
			MutableTreeNode n = new DefaultMutableTreeNode("node" + i);
			addNodes(n, depth - 1);
			node.insert(n, 0);
		}
	}

	public static void main(String[] args) throws Exception {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new TreeMemory());
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
