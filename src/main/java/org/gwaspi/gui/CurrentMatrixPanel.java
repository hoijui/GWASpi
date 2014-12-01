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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import org.gwaspi.constants.DBSamplesConstants;
import org.gwaspi.constants.ExportConstants.ExportFormat;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.OperationsTableModel;
import org.gwaspi.gui.utils.RowRendererDefault;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Deleter;
import org.gwaspi.threadbox.Threaded_ExportMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentMatrixPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(CurrentMatrixPanel.class);

	private final MatrixKey matrix;

	public CurrentMatrixPanel(MatrixKey matrixKey) throws IOException {

		matrix = matrixKey;
		final DataSetKey abstractMatrix = new DataSetKey(matrixKey);
		final MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);

//		final List<OperationMetadata> subOperations = OperationsList.getOffspringOperationsMetadata(abstractMatrix);
		final List<OperationMetadata> subOperations = OperationsList.getChildrenOperationsMetadata(abstractMatrix);

		JPanel pnl_desc = new JPanel();
		JScrollPane scrl_desc = new JScrollPane();
		JTextArea txtA_desc = new JTextArea();
		final String title
				= Text.Matrix.currentMatrix + " " + matrixMetadata.getFriendlyName()
				+ ", " + Text.Matrix.matrixID
				+ ": mx" + matrix.getMatrixId(); // NOI18N
		pnl_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(title));
		txtA_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.All.description)); // NOI18N
		txtA_desc.setColumns(20);
		txtA_desc.setRows(5);
		txtA_desc.setText(matrixMetadata.getDescription());
		scrl_desc.setViewportView(txtA_desc);
		pnl_desc.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP_SMALL));
		JButton btn_Delete = new JButton();
		JButton btn_SaveDesc = new JButton();
		JPanel pnl_StudyDescButtons = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_Delete},
				new JComponent[] {btn_SaveDesc});
		pnl_desc.add(scrl_desc, BorderLayout.CENTER);
		pnl_desc.add(pnl_StudyDescButtons, BorderLayout.SOUTH);

		JPanel pnl_operationsTable = new JPanel();
		JScrollPane scrl_operationsTable = new JScrollPane();
		JTable tbl_OperationsTable = new MatrixTable();
		JButton btn_DeleteOperation = new JButton();
		pnl_operationsTable.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Operation.operations)); // NOI18N
		tbl_OperationsTable.setModel(new OperationsTableModel(subOperations));
		tbl_OperationsTable.setDefaultRenderer(Object.class, new RowRendererDefault());
		scrl_operationsTable.setViewportView(tbl_OperationsTable);
		btn_DeleteOperation.setBackground(CurrentStudyPanel.DANGER_RED);
		pnl_operationsTable.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP_SMALL));
		pnl_operationsTable.add(scrl_operationsTable, BorderLayout.CENTER);
		JPanel pnl_MatrixTableButtons = GWASpiExplorerPanel.createButtonsPanel(btn_DeleteOperation);
		pnl_operationsTable.add(pnl_MatrixTableButtons, BorderLayout.SOUTH);

		JButton btn_Operation1_1 = new JButton();
		JButton btn_Operation1_2 = new JButton();
		JButton btn_Operation1_3 = new JButton();
		JButton btn_Operation1_4 = new JButton();
		JButton btn_Operation1_5 = new JButton();
		final Insets bigButtonInsets = new Insets(20, 30, 20, 30);
		btn_Operation1_1.setMargin(bigButtonInsets);
		btn_Operation1_2.setMargin(bigButtonInsets);
		btn_Operation1_3.setMargin(bigButtonInsets);
		btn_Operation1_4.setMargin(bigButtonInsets);
		btn_Operation1_5.setMargin(bigButtonInsets);
		JPanel pnl_matrixOperations = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_Operation1_1, btn_Operation1_2, btn_Operation1_3, btn_Operation1_4, btn_Operation1_5},
				new JComponent[] {});
		pnl_matrixOperations.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Operation.newOperation)); // NOI18N

		JButton btn_Help = new JButton();
		JButton btn_Back = new JButton();
		JPanel pnl_Footer = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_Back},
				new JComponent[] {btn_Help});

		JPanel pnl_Bottom = new JPanel();
		pnl_Bottom.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP));
		pnl_Bottom.add(pnl_matrixOperations, BorderLayout.NORTH);
		pnl_Bottom.add(pnl_Footer, BorderLayout.SOUTH);

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(Text.Matrix.matrix)); // NOI18N
		this.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP));
		this.add(pnl_desc, BorderLayout.NORTH);
		this.add(pnl_operationsTable, BorderLayout.CENTER);
		this.add(pnl_Bottom, BorderLayout.SOUTH);

		btn_Delete.setAction(new DeleteMatrixAction(matrix, this));
		btn_SaveDesc.setAction(new SaveDescriptionAction(matrix, txtA_desc));
		btn_DeleteOperation.setAction(new MatrixAnalysePanel.DeleteOperationAction(this, matrix, tbl_OperationsTable));
		btn_Operation1_1.setAction(new AnalyseDataAction(abstractMatrix));
		btn_Operation1_2.setAction(new ExtractMatrixAction(matrix));
		btn_Operation1_3.setAction(new MergeMatricesAction(matrix));
		btn_Operation1_4.setAction(new ExportMatrixAction(matrixMetadata));
		btn_Operation1_5.setAction(new TransformMatrixAction(matrix));
		btn_Back.setAction(new BackAction(matrix.getStudyKey()));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.currentMatrix));
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

		private final DataSetKey parentKey;

		AnalyseDataAction(final DataSetKey parentKey) {

			this.parentKey = parentKey;
			putValue(NAME, Text.Operation.analyseData);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			// Goto Matrix Analysis Panel
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(parentKey));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	private static class ExportMatrixAction extends AbstractAction {

		private final DataSetMetadata dataSetMetadata;

		ExportMatrixAction(DataSetMetadata dataSetMetadata) {

			this.dataSetMetadata = dataSetMetadata;
			putValue(NAME, Text.Trafo.exportMatrix);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			// Export Matrix Data
			final ExportFormat format = Dialogs.showExportFormatsSelectCombo();

			if (format == null) {
				// the user chose to "Cancel" the operation
				return;
			}

			String expPhenotype = DBSamplesConstants.f_AFFECTION;
			if (format.equals(ExportFormat.PLINK_Binary)
					|| format.equals(ExportFormat.Eigensoft_Eigenstrat))
			{
				// SELECT PHENOTYPE COLUMN TO USE
				if (format.equals(ExportFormat.Eigensoft_Eigenstrat)) {
					expPhenotype = Dialogs.showPhenotypeColumnsSelectCombo();
				}

				try {
					// CHECK IF MARKER QA EXISTS FOR EXPORT TO BE PERMITTED
					final List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(dataSetMetadata.getDataSetKey());
					final OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA, dataSetMetadata.getNumMarkers());
					if (markersQAOpKey == null) {
						Dialogs.showWarningDialogue(Text.Operation.warnOperationsMissing + " Marker QA");
						return;
					}
				} catch (IOException ex) {
					log.error("Failed to check for Markers QA operation existence, which is required for exporting to the " + format.toString() + " format", ex);
				}
			}

			// execute the exporting operation
			final MatrixExporterParams matrixExporterParams
					= new MatrixExporterParams(dataSetMetadata.getDataSetKey(), format, expPhenotype);
			final CommonRunnable exportTask = new Threaded_ExportMatrix(matrixExporterParams);
			MultiOperations.queueTask(exportTask);
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

			// TODO test if the deleted item is required for a queued worker
			if (MultiOperations.permitsDeletionOf(matrixKey)) {
				int option = JOptionPane.showConfirmDialog(dialogParent, Text.Matrix.confirmDelete1 + Text.Matrix.confirmDelete2);
				if (option == JOptionPane.YES_OPTION) {
					final int deleteReportsOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
					if (option == JOptionPane.YES_OPTION && deleteReportsOption != JOptionPane.CANCEL_OPTION) {
						final boolean deleteReports = (deleteReportsOption == JOptionPane.YES_OPTION);
						final Deleter matrixDeleter = new Deleter(matrixKey, deleteReports);
						MultiOperations.queueTask(matrixDeleter);
					}
				}
			} else {
				Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
			}
		}
	}
	//</editor-fold>
}
