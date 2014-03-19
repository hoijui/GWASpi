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

package org.gwaspi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.List;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyManagementPanel extends JPanel {

	private final Logger log
			= LoggerFactory.getLogger(StudyManagementPanel.class);

	// Variables declaration
	private final JButton btn_AddStudy;
	private final JButton btn_DeleteStudy;
	private final JLabel lbl_Desc;
	private final JLabel lbl_NewStudyName;
	private final JPanel pnl_StudiesTable;
	private final JPanel pnl_StudyDesc;
	private final JPanel pnl_Footer;
	private final JButton btn_Back;
	private final JButton btn_Help;
	private final JScrollPane scrl_Desc;
	private final JScrollPane scrl_StudiesTable;
	private final JTable tbl_StudiesTable;
	private final JTextArea txtA_Desc;
	private final JTextField txtF_NewStudyName;
	// End of variables declaration

	private static final class StudyTableModel extends AbstractTableModel {

		private static final String[] COLUMN_NAMES = new String[] {
				Text.Study.studyID,
				Text.Study.studyName,
				Text.All.description,
				Text.All.createDate};

		private final List<Study> studies;

		StudyTableModel(final List<Study> studies) {

			this.studies = studies;
		}

		@Override
		public int getRowCount() {
			return studies.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			Study study = studies.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return study.getId();
				case 1:
					return study.getName();
				case 2:
					return study.getDescription();
				case 3:
					return study.getCreationDate();
				default:
					return null;
			}
		}
	}

	public StudyManagementPanel() throws IOException {

		pnl_StudyDesc = new JPanel();
		lbl_NewStudyName = new JLabel();
		txtF_NewStudyName = new JTextField();
		txtF_NewStudyName.setDocument(new LimitedLengthDocument(64));
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
		txtF_NewStudyName.setDocument(new LimitedLengthDocument(63));

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
		tbl_StudiesTable.setModel(new StudyTableModel(StudyList.getStudyList()));
		scrl_StudiesTable.setViewportView(tbl_StudiesTable);
		btn_DeleteStudy.setAction(new DeleteStudyAction());

		//<editor-fold defaultstate="expanded" desc="LAYOUT STUDY TABLE">
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

		// <editor-fold defaultstate="expanded" desc="LAYOUT DESCRIPTION">
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

		//<editor-fold defaultstate="expanded" desc="FOOTER">
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
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

					StudyKey newStudy = StudyList.insertNewStudy(new Study(study_name, study_description));
					GWASpiExplorerNodes.insertStudyNode(newStudy);
					GWASpiExplorerPanel.getSingleton().selectNode(newStudy);
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
						StudyKey studyKey = new StudyKey(studyId);
						//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
						if (SwingWorkerItemList.permitsDeletionOf(studyKey)) {
							if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

								boolean deleteReport = false;
								if (deleteReportOption == JOptionPane.YES_OPTION) {
									deleteReport = true;
								}
								MultiOperations.deleteStudy(studyKey, deleteReport);
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

	public static class BackAction extends AbstractAction {

		public BackAction() {

			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().selectNode(0);
		}
	}
}
