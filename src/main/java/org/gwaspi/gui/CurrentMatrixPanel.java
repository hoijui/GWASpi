package org.gwaspi.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.gui.utils.NodeToPathCorrespondence;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CurrentMatrixPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(CurrentMatrixPanel.class);

	// Variables declaration
	private Matrix matrix;
	private Map<Integer, Object> treeChildrenMap;
	private JButton btn_Back;
	private JButton btn_DeleteMatrix;
	private JButton btn_DeleteOperation;
	private JButton btn_Help;
	private JButton btn_Operation1_1;
	private JButton btn_Operation1_2;
	private JButton btn_Operation1_3;
	private JButton btn_Operation1_4;
	private JButton btn_Operation1_5;
	private JButton btn_Operation1_6;
	private JButton btn_SaveDesc;
	private JPanel pnl_Spacer;
	private JPanel pnl_Buttons;
	private JPanel pnl_Footer;
	private JPanel pnl_MatrixDesc;
	private JPanel pnl_NewOperation;
	private JScrollPane scrl_MatrixDesc;
	private JScrollPane scrl_MatrixOperations;
	private JTable tbl_MatrixOperations;
	private JTextArea txtA_MatrixDesc;

	// End of variables declaration
	@SuppressWarnings("unchecked")
	public CurrentMatrixPanel(int _matrixId) throws IOException {

		matrix = MatricesList.getById(_matrixId);
		DefaultMutableTreeNode matrixNode = (DefaultMutableTreeNode) GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent();
		treeChildrenMap = NodeToPathCorrespondence.buildNodeToPathCorrespondence(matrixNode, true);

		pnl_MatrixDesc = new JPanel();
		scrl_MatrixDesc = new JScrollPane();
		txtA_MatrixDesc = new JTextArea();
		btn_DeleteMatrix = new JButton();
		btn_SaveDesc = new JButton();
		scrl_MatrixOperations = new JScrollPane();
		tbl_MatrixOperations = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false; //Renders column 0 uneditable.
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
		tbl_MatrixOperations.setDefaultRenderer(Object.class, new RowRendererDefault());

		btn_DeleteOperation = new JButton();
		pnl_NewOperation = new JPanel();
		btn_Operation1_1 = new JButton();
		btn_Operation1_2 = new JButton();
		btn_Operation1_3 = new JButton();
		btn_Operation1_4 = new JButton();
		btn_Operation1_5 = new JButton();
		btn_Operation1_6 = new JButton();
		pnl_Spacer = new JPanel();
		pnl_Buttons = new JPanel();
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrix, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N


		pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.currentMatrix + " " + matrix.getMatrixMetadata().getMatrixFriendlyName() + ", " + Text.Matrix.matrixID + ": mx" + matrix.getId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setColumns(20);
		txtA_MatrixDesc.setRows(5);
		txtA_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setDocument(new LimitedLengthDocument(1999));
		txtA_MatrixDesc.setText(matrix.getMatrixMetadata().getDescription());
		scrl_MatrixDesc.setViewportView(txtA_MatrixDesc);
		btn_DeleteMatrix.setAction(new DeleteMatrixAction(matrix, this));
		btn_SaveDesc.setAction(new SaveDescriptionAction(matrix, txtA_MatrixDesc));

		tbl_MatrixOperations.setModel(new DefaultTableModel(
				OperationsList.getOperationsTable(_matrixId),
				new String[]{
					Text.Operation.operationId, Text.Operation.operationName, Text.All.description, Text.All.createDate
				}));
		scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
		btn_DeleteOperation.setAction(new DeleteOperationAction(matrix, this, tbl_MatrixOperations));


		pnl_NewOperation.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.newOperation, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		btn_Operation1_1.setAction(new AnalyseDataAction(matrix));

		btn_Operation1_2.setAction(new ExtractMatrixAction(matrix));

		btn_Operation1_3.setAction(new MergeMatricesAction(matrix));

		btn_Operation1_4.setAction(new ExportMatrixAction(matrix));

		btn_Operation1_5.setAction(new TransformMatrixAction(matrix));

		btn_Operation1_6.setAction(new TranslateMatricesAction(matrix));

		// <editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESCRIPTION">
		GroupLayout pnl_MatrixDescLayout = new GroupLayout(pnl_MatrixDesc);
		pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
		pnl_MatrixDescLayout.setHorizontalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixOperations, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addComponent(scrl_MatrixDesc, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(btn_DeleteMatrix, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
				.addComponent(btn_SaveDesc))
				.addComponent(btn_DeleteOperation, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));


		pnl_MatrixDescLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_DeleteMatrix, btn_DeleteOperation, btn_SaveDesc});

		pnl_MatrixDescLayout.setVerticalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_MatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_SaveDesc)
				.addComponent(btn_DeleteMatrix))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(scrl_MatrixOperations, GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap()));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
		GroupLayout pnl_ButtonsLayout = new GroupLayout(pnl_Buttons);
		pnl_Buttons.setLayout(pnl_ButtonsLayout);
		pnl_ButtonsLayout.setHorizontalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Operation1_1, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_2, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_3, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_4, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_5, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(20, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4});

		pnl_ButtonsLayout.setVerticalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addGap(12, 12, 12)
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Operation1_1, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_2, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_3, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_4, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_5, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4});


		GroupLayout pnl_SpacerLayout = new GroupLayout(pnl_Spacer);
		pnl_Spacer.setLayout(pnl_SpacerLayout);
		pnl_SpacerLayout.setHorizontalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));
		pnl_SpacerLayout.setVerticalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 77, Short.MAX_VALUE));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="LAYOUT NEW OPERATION">
		GroupLayout pnl_NewOperationLayout = new GroupLayout(pnl_NewOperation);
		pnl_NewOperation.setLayout(pnl_NewOperationLayout);
		pnl_NewOperationLayout.setHorizontalGroup(
				pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Spacer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Buttons, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_NewOperationLayout.setVerticalGroup(
				pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addGroup(pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Buttons, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Spacer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		btn_Back.setAction(new BackAction(matrix));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.currentMatrix));

		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 692, Short.MAX_VALUE)
				.addComponent(btn_Help)));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(pnl_NewOperation, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap())
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(8, 8, 8)))));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_MatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_NewOperation, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		// </editor-fold>
	}

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private static class ExtractMatrixAction extends AbstractAction {

		private Matrix matrix;

		ExtractMatrixAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.extractData);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixExtractPanel(matrix.getId(), "", ""));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class TransformMatrixAction extends AbstractAction {

		private Matrix matrix;

		TransformMatrixAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.transformMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Goto Trafo Pane
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixTrafoPanel(matrix.getId()));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class AnalyseDataAction extends AbstractAction {

		private Matrix matrix;

		AnalyseDataAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Operation.analyseData);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Goto Matrix Analysis Panel
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(matrix.getId(), Integer.MIN_VALUE));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class ExportMatrixAction extends AbstractAction {

		private Matrix matrix;

		ExportMatrixAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.exportMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Export Matrix Data
			ExportFormat format = Dialogs.showExportFormatsSelectCombo();

			if (format != null) {
				String expPhenotype = cDBSamples.f_AFFECTION;
				if (format.equals(cExport.ExportFormat.PLINK_Binary) || format.equals(cExport.ExportFormat.Eigensoft_Eigenstrat)) {
					try {
						// SELECT PHENOTYPE COLUMN TO USE

						if (format.equals(cExport.ExportFormat.Eigensoft_Eigenstrat)) {
							expPhenotype = Dialogs.showPhenotypeColumnsSelectCombo();
						}

						// CHECK IF MARKER QA EXISTS FOR EXPORT TO BE PERMITTED
						List<Operation> operations = OperationsList.getOperationsList(matrix.getId());
						int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);
						if (markersQAOpId != Integer.MIN_VALUE) {
							MultiOperations.doExportMatrix(matrix.getStudyId(), matrix.getId(), format, expPhenotype);
						} else {
							Dialogs.showWarningDialogue(Text.Operation.warnOperationsMissing + " Marker QA");
						}
					} catch (IOException ex) {
						log.error(null, ex);
					}
				} else {
					MultiOperations.doExportMatrix(matrix.getStudyId(), matrix.getId(), format, expPhenotype);
				}
			}
		}
	}

	private static class MergeMatricesAction extends AbstractAction {

		private Matrix matrix;

		MergeMatricesAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.mergeMatrices);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixMergePanel(matrix.getId()));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class TranslateMatricesAction extends AbstractAction {

		private Matrix matrix;

		TranslateMatricesAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.translateMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
			} catch (Exception ex) {
				log.error(null, ex);
			}
			throw new UnsupportedOperationException("Not yet implemented!");
		}
	}

	private static class SaveDescriptionAction extends AbstractAction {

		private Matrix matrix;
		private JTextArea matrixDesc;

		SaveDescriptionAction(Matrix matrix, JTextArea matrixDesc) {

			this.matrix = matrix;
			this.matrixDesc = matrixDesc;
			putValue(NAME, Text.All.saveDescription);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				MatricesList.saveMatrixDescription(matrix.getId(), matrixDesc.getText());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class DeleteMatrixAction extends AbstractAction {

		private Matrix matrix;
		private Component dialogParent;

		DeleteMatrixAction(Matrix matrix, Component dialogParent) {

			this.matrix = matrix;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.Matrix.deleteMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// TODO TEST IF THE DELETED ITEM IS REQUIRED FOR A QUEUED WORKER
			if (SwingWorkerItemList.permitsDeletionOfMatrixId(matrix.getId())) {
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

						boolean deleteReport = false;
						if (deleteReportOption == JOptionPane.YES_OPTION) {
							deleteReport = true;
						}
						MultiOperations.deleteMatrix(matrix.getStudyId(), matrix.getId(), deleteReport);
					}
				}
			} else {
				Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
			}
		}
	}

	private static class DeleteOperationAction extends AbstractAction {

		private Matrix matrix;
		private JTable matrixOperationsTable;
		private Component dialogParent;

		DeleteOperationAction(Matrix matrix, Component dialogParent, JTable matrixOperationsTable) {

			this.matrix = matrix;
			this.dialogParent = dialogParent;
			this.matrixOperationsTable = matrixOperationsTable;
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] selectedOPs = matrixOperationsTable.getSelectedRows();
			if (selectedOPs.length > 0) {
				try {
					int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
					if (option == JOptionPane.YES_OPTION) {
						int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
						if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
							for (int i = 0; i < selectedOPs.length; i++) {
								int tmpOPRow = selectedOPs[i];
								int opId = (Integer) matrixOperationsTable.getModel().getValueAt(tmpOPRow, 0);
								//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
								if (SwingWorkerItemList.permitsDeletionOfOperationId(opId)) {
									if (option == JOptionPane.YES_OPTION) {
										boolean deleteReport = false;
										if (deleteReportOption == JOptionPane.YES_OPTION) {
											deleteReport = true;
										}
										MultiOperations.deleteOperationsByOpId(matrix.getStudyId(), matrix.getId(), opId, deleteReport);

										//OperationManager.deleteOperationAndChildren(matrix.getStudyId(), opId, deleteReport);
									}
								} else {
									Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
								}
							}
							GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
						}
					}
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private Matrix matrix;

		BackAction(Matrix matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentStudyPanel(matrix.getStudyId()));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>
}
