package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.JTextFieldLimit;
import org.gwaspi.gui.utils.NodeToPathCorrespondence;
import org.gwaspi.gui.utils.RowRendererDefault;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.model.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class StudyManagementPanel extends JPanel {

	private final Logger log
			= LoggerFactory.getLogger(StudyManagementPanel.class);

	// Variables declaration
	private Study study;
	private Map<Integer, Object> treeChildrenLHM = new LinkedHashMap<Integer, Object>();
	private JButton btn_AddStudy;
	private JButton btn_DeleteStudy;
	private JLabel lbl_Desc;
	private JLabel lbl_NewStudyName;
	private JPanel pnl_StudiesTable;
	private JPanel pnl_StudyDesc;
	private JPanel pnl_Footer;
	private JButton btn_Back;
	private JButton btn_Help;
	private JScrollPane scrl_Desc;
	private JScrollPane scrl_StudiesTable;
	private JTable tbl_StudiesTable;
	private JTextArea txtA_Desc;
	private JTextField txtF_NewStudyName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public StudyManagementPanel() throws IOException {

		DefaultMutableTreeNode studyManagementNode = (DefaultMutableTreeNode) GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent();
		treeChildrenLHM = NodeToPathCorrespondence.buildNodeToPathCorrespondence(studyManagementNode, false);

		pnl_StudyDesc = new JPanel();
		lbl_NewStudyName = new JLabel();
		txtF_NewStudyName = new JTextField();
		txtF_NewStudyName.setDocument(new org.gwaspi.model.JTextFieldLimited(64));
		lbl_Desc = new JLabel();
		scrl_Desc = new JScrollPane();
		txtA_Desc = new JTextArea();
		btn_DeleteStudy = new JButton();
		btn_AddStudy = new JButton();
		pnl_StudiesTable = new JPanel();
		pnl_Footer = new JPanel();
		btn_Help = new JButton();
		btn_Back = new JButton();
		scrl_StudiesTable = new JScrollPane();
		tbl_StudiesTable = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent && getValueAt(rowIndex, vColIndex) != null) {
					JComponent jc = (JComponent) c;
					jc.setToolTipText("<html>" + getValueAt(rowIndex, vColIndex).toString().replaceAll("\n", "<br>") + "</html>");
				}
				return c;
			}
		};
		tbl_StudiesTable.setDefaultRenderer(Object.class, new RowRendererDefault());

		setBorder(BorderFactory.createTitledBorder(null, Text.Study.studies, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_StudyDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Study.createNewStudy, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		lbl_NewStudyName.setText(Text.Study.studyName);
		txtF_NewStudyName.setDocument(new JTextFieldLimit(63));

		lbl_Desc.setText(Text.All.description);
		txtA_Desc.setColumns(20);
		txtA_Desc.setRows(5);
		txtA_Desc.setText(Text.All.optional);
		txtA_Desc.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_Desc.getText().equals(Text.All.optional)) {
							txtA_Desc.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_Desc.select(0, 0);
					}
				});
			}
		});

		scrl_Desc.setViewportView(txtA_Desc);
		btn_AddStudy.setAction(new AddStudyAction());

		pnl_StudiesTable.setBorder(BorderFactory.createTitledBorder(null, Text.Study.availableStudies, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		tbl_StudiesTable.setModel(new DefaultTableModel(
				org.gwaspi.model.StudyList.getStudyTable(),
				new String[]{
					Text.Study.studyID, Text.Study.studyName, Text.All.description, Text.All.createDate
				}));
		scrl_StudiesTable.setViewportView(tbl_StudiesTable);
		btn_DeleteStudy.setAction(new DeleteStudyAction());

		//<editor-fold defaultstate="collapsed" desc="LAYOUT STUDY TABLE">
		GroupLayout pnl_StudiesTableLayout = new GroupLayout(pnl_StudiesTable);
		pnl_StudiesTable.setLayout(pnl_StudiesTableLayout);
		pnl_StudiesTableLayout.setHorizontalGroup(
				pnl_StudiesTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudiesTableLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudiesTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_StudiesTable, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addComponent(btn_DeleteStudy, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_StudiesTableLayout.setVerticalGroup(
				pnl_StudiesTableLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudiesTableLayout.createSequentialGroup()
				.addComponent(scrl_StudiesTable, GroupLayout.PREFERRED_SIZE, 288, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(btn_DeleteStudy)
				.addContainerGap()));
		//</editor-fold>

		// <editor-fold defaultstate="collapsed" desc="LAYOUT DESCRIPTION">
		GroupLayout pnl_StudyDescLayout = new GroupLayout(pnl_StudyDesc);
		pnl_StudyDesc.setLayout(pnl_StudyDescLayout);
		pnl_StudyDescLayout.setHorizontalGroup(
				pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_NewStudyName)
				.addComponent(lbl_Desc))
				.addGap(18, 18, 18)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_Desc, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
				.addComponent(txtF_NewStudyName, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
				.addContainerGap())
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_StudyDescLayout.createSequentialGroup()
				.addContainerGap(605, Short.MAX_VALUE)
				.addComponent(btn_AddStudy, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
				.addGap(14, 14, 14)));
		pnl_StudyDescLayout.setVerticalGroup(
				pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewStudyName)
				.addComponent(txtF_NewStudyName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(pnl_StudyDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_Desc)
				.addGroup(pnl_StudyDescLayout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(scrl_Desc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_AddStudy)));
		// </editor-fold>

		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		btn_Back.setAction(new BackAction());
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.createStudy));
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 437, Short.MAX_VALUE)
				.addComponent(btn_Help)));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(0, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(pnl_StudiesTable, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_StudyDesc, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_StudiesTable, GroupLayout.PREFERRED_SIZE, 362, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_StudyDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>
	}

	private class AddStudyAction extends AbstractAction { // FIXME make static

		AddStudyAction() {

			putValue(NAME, Text.Study.addStudy);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				String study_name = txtF_NewStudyName.getText();
				if (!study_name.isEmpty()) {
					lbl_NewStudyName.setForeground(Color.black);
					String study_description = txtA_Desc.getText();
					if (txtA_Desc.getText().equals(Text.All.optional)) {
						study_description = "";
					}

					org.gwaspi.database.StudyGenerator.insertNewStudy(study_name, study_description);
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new StudyManagementPanel());
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
					GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
					//model.GWASpiExplorer.insertLatestStudyNode();
				} else {
					Dialogs.showWarningDialogue(Text.Study.warnNoStudyName);
					lbl_NewStudyName.setForeground(Color.red);
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private class DeleteStudyAction extends AbstractAction { // FIXME make static

		DeleteStudyAction() {

			putValue(NAME, Text.Study.deleteStudy);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (tbl_StudiesTable.getSelectedRow() != -1) {

				int[] selectedStudyRows = tbl_StudiesTable.getSelectedRows();
				int[] selectedStudyIds = new int[selectedStudyRows.length];
				for (int i = 0; i < selectedStudyRows.length; i++) {
					selectedStudyIds[i] = (Integer) tbl_StudiesTable.getModel().getValueAt(selectedStudyRows[i], 0);
				}

				int option = JOptionPane.showConfirmDialog(StudyManagementPanel.this, Text.Study.confirmDelete1 + Text.Study.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(StudyManagementPanel.this, Text.Reports.confirmDelete);
					for (int i = 0; i < selectedStudyIds.length; i++) {
						int studyId = selectedStudyIds[i];
						//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
						if (SwingWorkerItemList.permitsDeletion(studyId, null, null)) {
							if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

								boolean deleteReport = false;
								if (deleteReportOption == JOptionPane.YES_OPTION) {
									deleteReport = true;
								}
								MultiOperations.deleteStudy(studyId, deleteReport);

	//							try {
	//								org.gwaspi.database.StudyGenerator.deleteStudy(studyId, deleteReport);
	//								try {
	//									GWASpiExplorerPanel.getSingleton().pnl_Content = new StudyManagementPanel();
	//									GWASpiExplorerPanel.getSingleton().scrl_Content.setViewportView(GWASpiExplorerPanel.getSingleton().pnl_Content);
	//									GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
	//								} catch (IOException ex) {
	//									log.error(null, ex);
	//								}
	//
	//							} catch (IOException ex) {
	//								log.error(null, ex);
	//							}
							}
						} else {
							Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
						}
					}
					try {
						GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				}
			}
		}
	}

	private static class BackAction extends AbstractAction {

		BackAction() {

			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
			GWASpiExplorerPanel.getSingleton().setPnl_Content(new IntroPanel());
			GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
		}
	}
}
