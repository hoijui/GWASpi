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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
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
import javax.swing.table.TableCellRenderer;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.gui.utils.OperationsTableModel;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentMatrixPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(CurrentMatrixPanel.class);

	// Variables declaration
	private final MatrixKey matrix;
	private final JButton btn_Back;
	private final JButton btn_DeleteMatrix;
	private final JButton btn_DeleteOperation;
	private final JButton btn_Help;
	private final JButton btn_Operation1_1;
	private final JButton btn_Operation1_2;
	private final JButton btn_Operation1_3;
	private final JButton btn_Operation1_4;
	private final JButton btn_Operation1_5;
	private final JButton btn_Operation1_6;
	private final JButton btn_SaveDesc;
	private final JPanel pnl_Spacer;
	private final JPanel pnl_Buttons;
	private final JPanel pnl_Footer;
	private final JPanel pnl_MatrixDesc;
	private final JPanel pnl_NewOperation;
	private final JScrollPane scrl_MatrixDesc;
	private final JScrollPane scrl_MatrixOperations;
	private final JTable tbl_MatrixOperations;
	private final JTextArea txtA_MatrixDesc;
	// End of variables declaration

	public CurrentMatrixPanel(MatrixKey matrixKey) throws IOException {

		matrix = matrixKey;
		MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);

		pnl_MatrixDesc = new JPanel();
		scrl_MatrixDesc = new JScrollPane();
		txtA_MatrixDesc = new JTextArea();
		btn_DeleteMatrix = new JButton();
		btn_SaveDesc = new JButton();
		scrl_MatrixOperations = new JScrollPane();
		tbl_MatrixOperations = new JTable() {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false; // Renders column 0 uneditable.
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


		pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.currentMatrix + " " + matrixMetadata.getMatrixFriendlyName() + ", " + Text.Matrix.matrixID + ": mx" + matrix.getMatrixId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setColumns(20);
		txtA_MatrixDesc.setRows(5);
		txtA_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setDocument(new LimitedLengthDocument(1999));
		txtA_MatrixDesc.setText(matrixMetadata.getDescription());
		scrl_MatrixDesc.setViewportView(txtA_MatrixDesc);
		btn_DeleteMatrix.setAction(new DeleteMatrixAction(matrix, this));
		btn_SaveDesc.setAction(new SaveDescriptionAction(matrix, txtA_MatrixDesc));

		tbl_MatrixOperations.setModel(new OperationsTableModel(OperationsList.getOperationsTable(matrixKey)));
		scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
		btn_DeleteOperation.setAction(new DeleteOperationAction(matrix, this, tbl_MatrixOperations));


		pnl_NewOperation.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.newOperation, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		btn_Operation1_1.setAction(new AnalyseDataAction(matrix));

		btn_Operation1_2.setAction(new ExtractMatrixAction(matrix));

		btn_Operation1_3.setAction(new MergeMatricesAction(matrix));

		btn_Operation1_4.setAction(new ExportMatrixAction(matrix));

		btn_Operation1_5.setAction(new TransformMatrixAction(matrix));

		btn_Operation1_6.setAction(new TranslateMatricesAction(matrix));

		// <editor-fold defaultstate="expanded" desc="LAYOUT MATRIX DESCRIPTION">
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT BUTTONS">
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT NEW OPERATION">
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

		//<editor-fold defaultstate="expanded" desc="FOOTER">
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

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	private static class ExtractMatrixAction extends AbstractAction {

		private final MatrixKey matrix;

		ExtractMatrixAction(MatrixKey matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.extractData);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixExtractPanel(matrix, "", ""));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class TransformMatrixAction extends AbstractAction {

		private final MatrixKey matrix;

		TransformMatrixAction(MatrixKey matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.transformMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Goto Trafo Pane
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixTrafoPanel(matrix));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class AnalyseDataAction extends AbstractAction {

		private final MatrixKey matrix;

		AnalyseDataAction(MatrixKey matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Operation.analyseData);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Goto Matrix Analysis Panel
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(matrix, null));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class ExportMatrixAction extends AbstractAction {

		private final MatrixKey matrixKey;

		ExportMatrixAction(MatrixKey matrix) {

			this.matrixKey = matrix;
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
						List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey);
						OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);
						if (markersQAOpKey != null) {
							MultiOperations.doExportMatrix(matrixKey, format, expPhenotype);
						} else {
							Dialogs.showWarningDialogue(Text.Operation.warnOperationsMissing + " Marker QA");
						}
					} catch (IOException ex) {
						log.error(null, ex);
					}
				} else {
					MultiOperations.doExportMatrix(matrixKey, format, expPhenotype);
				}
			}
		}
	}

	private static class MergeMatricesAction extends AbstractAction {

		private final MatrixKey matrix;

		MergeMatricesAction(MatrixKey matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.Trafo.mergeMatrices);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixMergePanel(matrix));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class TranslateMatricesAction extends AbstractAction {

		private final MatrixKey matrix;

		TranslateMatricesAction(MatrixKey matrix) {

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

		private final MatrixKey matrixKey;
		private final JTextArea matrixDesc;

		SaveDescriptionAction(MatrixKey matrixKey, JTextArea matrixDesc) {

			this.matrixKey = matrixKey;
			this.matrixDesc = matrixDesc;
			putValue(NAME, Text.All.saveDescription);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				MatrixMetadata matrixMetadataById = MatricesList.getMatrixMetadataById(matrixKey);
				matrixMetadataById.setDescription(matrixDesc.getText());
				MatricesList.updateMatrix(matrixMetadataById);
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class DeleteMatrixAction extends AbstractAction {

		private final MatrixKey matrixKey;
		private final Component dialogParent;

		DeleteMatrixAction(MatrixKey matrixKey, Component dialogParent) {

			this.matrixKey = matrixKey;
			this.dialogParent = dialogParent;
			putValue(NAME, Text.Matrix.deleteMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// TODO TEST IF THE DELETED ITEM IS REQUIRED FOR A QUEUED WORKER
			if (SwingWorkerItemList.permitsDeletionOfMatrixId(matrixKey.getMatrixId())) {
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

						boolean deleteReport = false;
						if (deleteReportOption == JOptionPane.YES_OPTION) {
							deleteReport = true;
						}
						MultiOperations.deleteMatrix(matrixKey, deleteReport);
					}
				}
			} else {
				Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
			}
		}
	}

	private static class DeleteOperationAction extends AbstractAction {

		private final MatrixKey matrixKey;
		private final JTable matrixOperationsTable;
		private final Component dialogParent;

		DeleteOperationAction(MatrixKey matrixKey, Component dialogParent, JTable matrixOperationsTable) {

			this.matrixKey = matrixKey;
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
										MultiOperations.deleteOperationsByOpId(matrixKey, opId, deleteReport);

										//OperationManager.deleteOperationAndChildren(matrix.getStudyKey(), opId, deleteReport);
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

		private final MatrixKey matrix;

		BackAction(MatrixKey matrix) {

			this.matrix = matrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentStudyPanel(matrix.getStudyKey()));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>
}
