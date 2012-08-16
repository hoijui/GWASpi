package org.gwaspi.gui;

import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import org.gwaspi.model.OperationsList;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class CurrentMatrixPanel extends javax.swing.JPanel {

	// Variables declaration
	private org.gwaspi.model.Matrix matrix;
	private LinkedHashMap treeChildrenLHM;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_DeleteMatrix;
	private javax.swing.JButton btn_DeleteOperation;
	private javax.swing.JButton btn_Help;
	private javax.swing.JButton btn_Operation1_1;
	private javax.swing.JButton btn_Operation1_2;
	private javax.swing.JButton btn_Operation1_3;
	private javax.swing.JButton btn_Operation1_4;
	private javax.swing.JButton btn_Operation1_5;
	private javax.swing.JButton btn_Operation1_6;
	private javax.swing.JButton btn_SaveDesc;
	private javax.swing.JPanel pnl_Spacer;
	private javax.swing.JPanel pnl_Buttons;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JPanel pnl_MatrixDesc;
	private javax.swing.JPanel pnl_NewOperation;
	private javax.swing.JScrollPane scrl_MatrixDesc;
	private javax.swing.JScrollPane scrl_MatrixOperations;
	private javax.swing.JTable tbl_MatrixOperations;
	private javax.swing.JTextArea txtA_MatrixDesc;

	// End of variables declaration
	@SuppressWarnings("unchecked")
	public CurrentMatrixPanel(int _matrixId) throws IOException {

		matrix = new org.gwaspi.model.Matrix(_matrixId);
		DefaultMutableTreeNode matrixNode = (DefaultMutableTreeNode) org.gwaspi.gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent();
		treeChildrenLHM = org.gwaspi.gui.utils.NodeToPathCorrespondence.buildNodeToPathCorrespondence(matrixNode, true);

		pnl_MatrixDesc = new javax.swing.JPanel();
		scrl_MatrixDesc = new javax.swing.JScrollPane();
		txtA_MatrixDesc = new javax.swing.JTextArea();
		btn_DeleteMatrix = new javax.swing.JButton();
		btn_SaveDesc = new javax.swing.JButton();
		scrl_MatrixOperations = new javax.swing.JScrollPane();
		tbl_MatrixOperations = new javax.swing.JTable() {
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
		tbl_MatrixOperations.setDefaultRenderer(Object.class, new org.gwaspi.gui.utils.RowRendererDefault());

		btn_DeleteOperation = new javax.swing.JButton();
		pnl_NewOperation = new javax.swing.JPanel();
		btn_Operation1_1 = new javax.swing.JButton();
		btn_Operation1_2 = new javax.swing.JButton();
		btn_Operation1_3 = new javax.swing.JButton();
		btn_Operation1_4 = new javax.swing.JButton();
		btn_Operation1_5 = new javax.swing.JButton();
		btn_Operation1_6 = new javax.swing.JButton();
		pnl_Spacer = new javax.swing.JPanel();
		pnl_Buttons = new javax.swing.JPanel();
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.matrix, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N


		pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.currentMatrix + " " + matrix.matrixMetadata.getMatrixFriendlyName() + ", " + Text.Matrix.matrixID + ": mx" + matrix.getMatrixId(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setColumns(20);
		txtA_MatrixDesc.setRows(5);
		txtA_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.All.description, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_MatrixDesc.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(1999));
		txtA_MatrixDesc.setText(matrix.matrixMetadata.getDescription());
		scrl_MatrixDesc.setViewportView(txtA_MatrixDesc);
		btn_DeleteMatrix.setText(Text.Matrix.deleteMatrix);
		btn_DeleteMatrix.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteMatrix();
			}
		});
		btn_SaveDesc.setText(Text.All.saveDescription);
		btn_SaveDesc.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveDescription();
			}
		});

		tbl_MatrixOperations.setModel(new javax.swing.table.DefaultTableModel(
				org.gwaspi.model.OperationsList.getOperationsTable(_matrixId),
				new String[]{
					Text.Operation.operationId, Text.Operation.operationName, Text.All.description, Text.All.createDate
				}));
		scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
		btn_DeleteOperation.setText(Text.Operation.deleteOperation);
		btn_DeleteOperation.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionDeleteOperation(evt);
			}
		});


		pnl_NewOperation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.newOperation, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		btn_Operation1_1.setText(Text.Operation.analyseData);
		btn_Operation1_1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixAnalyse(evt);
			}
		});

		btn_Operation1_2.setText(Text.Trafo.extractData);
		btn_Operation1_2.setEnabled(true);
		btn_Operation1_2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionExtractMatrix(evt);
			}
		});

		btn_Operation1_3.setText(Text.Trafo.mergeMatrices);
		btn_Operation1_3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixMerge(evt);
			}
		});

		btn_Operation1_4.setText(Text.Trafo.exportMatrix);
		btn_Operation1_4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixExport(evt);
			}
		});


		btn_Operation1_5.setText(Text.Trafo.transformMatrix);
		btn_Operation1_5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixTrafo(evt);
			}
		});

		btn_Operation1_6.setText(Text.Trafo.translateMatrix);
		btn_Operation1_6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixStrandFlip(evt);
			}
		});

		// <editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESCRIPTION">
		javax.swing.GroupLayout pnl_MatrixDescLayout = new javax.swing.GroupLayout(pnl_MatrixDesc);
		pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
		pnl_MatrixDescLayout.setHorizontalGroup(
				pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(btn_DeleteMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
				.addComponent(btn_SaveDesc))
				.addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));


		pnl_MatrixDescLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_DeleteMatrix, btn_DeleteOperation, btn_SaveDesc});

		pnl_MatrixDescLayout.setVerticalGroup(
				pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_SaveDesc)
				.addComponent(btn_DeleteMatrix))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap()));
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
		javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
		pnl_Buttons.setLayout(pnl_ButtonsLayout);
		pnl_ButtonsLayout.setHorizontalGroup(
				pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Operation1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Operation1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(20, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4});

		pnl_ButtonsLayout.setVerticalGroup(
				pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addGap(12, 12, 12)
				.addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Operation1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Operation1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4});


		javax.swing.GroupLayout pnl_SpacerLayout = new javax.swing.GroupLayout(pnl_Spacer);
		pnl_Spacer.setLayout(pnl_SpacerLayout);
		pnl_SpacerLayout.setHorizontalGroup(
				pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));
		pnl_SpacerLayout.setVerticalGroup(
				pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 77, Short.MAX_VALUE));
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT NEW OPERATION">
		javax.swing.GroupLayout pnl_NewOperationLayout = new javax.swing.GroupLayout(pnl_NewOperation);
		pnl_NewOperation.setLayout(pnl_NewOperationLayout);
		pnl_NewOperationLayout.setHorizontalGroup(
				pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_NewOperationLayout.setVerticalGroup(
				pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addGroup(pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Buttons, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		btn_Back.setText("Back");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_BackActionPerformed(evt);
			}
		});
		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btn_HelpActionPerformed(evt);
			}
		});


		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 692, Short.MAX_VALUE)
				.addComponent(btn_Help)));


		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>



		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnl_NewOperation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap())
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(8, 8, 8)))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_NewOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		// </editor-fold>

	}

	private void actionExtractMatrix(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixExtractPanel(matrix.getMatrixId(), "", "");
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//Goto Trafo Pane
	private void actionMatrixTrafo(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixTrafoPanel(matrix.getMatrixId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//Goto Matrix Analysis Panel
	private void actionMatrixAnalyse(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(matrix.getMatrixId(), Integer.MIN_VALUE);
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//Export Matrix Data
	private void actionMatrixExport(ActionEvent evt) {
		ExportFormat format = Dialogs.showExportFormatsSelectCombo();

		if (format != null) {
			String expPhenotype = org.gwaspi.constants.cDBSamples.f_AFFECTION;
			if (format.equals(org.gwaspi.constants.cExport.ExportFormat.PLINK_Binary) || format.equals(org.gwaspi.constants.cExport.ExportFormat.Eigensoft_Eigenstrat)) {
				try {
					//SELECT PHENOTYPE COLUMN TO USE

					if (format.equals(org.gwaspi.constants.cExport.ExportFormat.Eigensoft_Eigenstrat)) {
						expPhenotype = org.gwaspi.gui.utils.Dialogs.showPhenotypeColumnsSelectCombo();
					}

					//CHECK IF MARKER QA EXISTS FOR EXPORT TO BE PERMITTED
					OperationsList opList = new OperationsList(matrix.getMatrixId());
					int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(OPType.MARKER_QA);
					if (markersQAOpId != Integer.MIN_VALUE) {
						MultiOperations.doExportMatrix(matrix.getStudyId(), matrix.getMatrixId(), format, expPhenotype);
					} else {
						org.gwaspi.gui.utils.Dialogs.showWarningDialogue(org.gwaspi.global.Text.Operation.warnOperationsMissing + " Marker QA");
					}

				} catch (IOException ex) {
					Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				MultiOperations.doExportMatrix(matrix.getStudyId(), matrix.getMatrixId(), format, expPhenotype);
			}
		}

	}

	private void actionMatrixMerge(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new MatrixMergePanel(matrix.getMatrixId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionMatrixStrandFlip(ActionEvent evt) {
		try {
		} catch (Exception ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private void saveDescription() {
		boolean result = false;
		try {
			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			result = db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_MATRICES,
					org.gwaspi.constants.cDBMatrix.T_MATRICES,
					new String[]{constants.cDBMatrix.f_DESCRIPTION},
					new Object[]{txtA_MatrixDesc.getText()},
					new String[]{constants.cDBMatrix.f_ID},
					new Object[]{matrix.getMatrixId()});

		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionDeleteMatrix() {
		//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
		if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(null, matrix.getMatrixId(), null)) {
			int option = JOptionPane.showConfirmDialog(this, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
			if (option == JOptionPane.YES_OPTION) {
				int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
				if (option == JOptionPane.YES_OPTION && deleteReportOption != JOptionPane.CANCEL_OPTION) {

					boolean deleteReport = false;
					if (deleteReportOption == JOptionPane.YES_OPTION) {
						deleteReport = true;
					}
					MultiOperations.deleteMatrix(matrix.getStudyId(), matrix.getMatrixId(), deleteReport);
				}
			}
		} else {
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
		}

	}

	private void actionDeleteOperation(ActionEvent evt) {
		int[] selectedOPs = tbl_MatrixOperations.getSelectedRows();
		if (selectedOPs.length > 0) {
			try {
				int option = JOptionPane.showConfirmDialog(this, Text.Operation.confirmDelete1);
				if (option == JOptionPane.YES_OPTION) {
					int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
					if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
						for (int i = 0; i < selectedOPs.length; i++) {
							int tmpOPRow = selectedOPs[i];
							int opId = (Integer) tbl_MatrixOperations.getModel().getValueAt(tmpOPRow, 0);
							//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
							if (org.gwaspi.threadbox.SwingWorkerItemList.permitsDeletion(null, null, opId)) {
								if (option == JOptionPane.YES_OPTION) {
									boolean deleteReport = false;
									if (deleteReportOption == JOptionPane.YES_OPTION) {
										deleteReport = true;
									}
									MultiOperations.deleteOperationsByOpId(matrix.getStudyId(), matrix.getMatrixId(), opId, deleteReport);

									//netCDF.operations.OperationManager.deleteOperationAndChildren(matrix.getStudyId(), opId, deleteReport);
								}
							} else {
								org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
							}
						}
						org.gwaspi.gui.GWASpiExplorerPanel.updateTreePanel(true);
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void btn_BackActionPerformed(ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.tree.setSelectionPath(org.gwaspi.gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentStudyPanel(matrix.getStudyId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void btn_HelpActionPerformed(ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.currentMatrix);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	//</editor-fold>
}
