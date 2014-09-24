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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.MoreAssocInfo;
import org.gwaspi.gui.utils.MoreGWASinOneGoInfo;
import org.gwaspi.gui.utils.MoreInfoForGtFreq;
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
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParamsEditor;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.combi.CombiTestParamsGUI;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.samples.SamplesParserManager;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixAnalysePanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(MatrixAnalysePanel.class);

	private final OperationMetadata currentOP;

	public MatrixAnalysePanel(DataSetKey observedElementKey) throws IOException {

//		MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);
		DataSetMetadata observedElementMetadata = MatricesList.getDataSetMetadata(observedElementKey);

		final DataSetKey parent;
		if (observedElementKey.isOperation()) {
			currentOP = OperationsList.getOperationMetadata(observedElementKey.getOperationParent());
			parent = currentOP.getParent();
		} else {
			currentOP = null;
			parent = null;
		}

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();
		Action gwasInOneGoAction = new GwasInOneGoAction(observedElementKey, gwasParams);
		gwasInOneGoAction.setEnabled(currentOP == null);

//		final List<OperationMetadata> subOperations = OperationsList.getOffspringOperationsMetadata(observedElementKey);
		final List<OperationMetadata> subOperations = OperationsList.getChildrenOperationsMetadata(observedElementKey);

		JPanel pnl_desc = new JPanel();
		JScrollPane scrl_desc = new JScrollPane();
		JTextArea txtA_desc = new JTextArea();
		final String title;
		if (observedElementKey.isOperation()) {
			title
					= Text.Operation.operation + ": " + observedElementMetadata.getFriendlyName()
					+ ", " + Text.Operation.operationId + ": " + observedElementMetadata.getDataSetKey().getOperationParent().getId();
		} else {
			title
					= Text.Matrix.matrix + ": " + observedElementMetadata.getFriendlyName()
					+ ", " + "MatrixID" + ": " + observedElementMetadata.getDataSetKey().getMatrixParent().getMatrixId();
		}
		pnl_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(title));
		txtA_desc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.All.description)); // NOI18N
		txtA_desc.setColumns(20);
		txtA_desc.setRows(5);
		txtA_desc.setText(observedElementMetadata.getDescription());
		scrl_desc.setViewportView(txtA_desc);
		pnl_desc.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP_SMALL));
		pnl_desc.add(scrl_desc, BorderLayout.CENTER);

		JPanel pnl_operationsTable = new JPanel();
		JScrollPane scrl_operationsTable = new JScrollPane();
		JTable tbl_operationsTable = new MatrixTable();
		JButton btn_DeleteOperation = new JButton();
		pnl_operationsTable.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Operation.operations)); // NOI18N
		tbl_operationsTable.setModel(new OperationsTableModel(subOperations));
		tbl_operationsTable.setDefaultRenderer(Object.class, new RowRendererDefault());
		scrl_operationsTable.setViewportView(tbl_operationsTable);
		btn_DeleteOperation.setBackground(CurrentStudyPanel.DANGER_RED);
		pnl_operationsTable.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP_SMALL));
		pnl_operationsTable.add(scrl_operationsTable, BorderLayout.CENTER);
		JPanel pnl_MatrixTableButtons = GWASpiExplorerPanel.createButtonsPanel(btn_DeleteOperation);
		pnl_operationsTable.add(pnl_MatrixTableButtons, BorderLayout.SOUTH);

		JButton btn_1_1 = new JButton();
		JButton btn_1_2 = new JButton();
		JButton btn_1_3 = new JButton();
		JButton btn_1_4 = new JButton();
		JButton btn_1_5 = new JButton();
		JButton btn_1_6 = new JButton();
		final Insets bigButtonInsets = new Insets(20, 30, 20, 30);
		btn_1_1.setMargin(bigButtonInsets);
		btn_1_2.setMargin(bigButtonInsets);
		btn_1_3.setMargin(bigButtonInsets);
		btn_1_4.setMargin(bigButtonInsets);
		btn_1_5.setMargin(bigButtonInsets);
		btn_1_6.setMargin(bigButtonInsets);
		JPanel pnl_NewOperation = new JPanel();
		pnl_NewOperation.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Operation.newOperation));
		pnl_NewOperation.setLayout(new GridLayout(0, 3, 18, 18));
		pnl_NewOperation.add(btn_1_1);
		pnl_NewOperation.add(btn_1_3);
		pnl_NewOperation.add(btn_1_5);
		pnl_NewOperation.add(btn_1_2);
		pnl_NewOperation.add(btn_1_4);
		pnl_NewOperation.add(btn_1_6);

		JButton btn_Help = new JButton();
		JButton btn_Back = new JButton();
		JPanel pnl_Footer = GWASpiExplorerPanel.createButtonsPanel(
				new JComponent[] {btn_Back},
				new JComponent[] {btn_Help});

		JPanel pnl_Bottom = new JPanel();
		pnl_Bottom.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP));
		pnl_Bottom.add(pnl_NewOperation, BorderLayout.NORTH);
		pnl_Bottom.add(pnl_Footer, BorderLayout.SOUTH);

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(Text.Operation.analyseData)); // NOI18N
		this.setLayout(new BorderLayout(CurrentStudyPanel.GAP, CurrentStudyPanel.GAP));
		this.add(pnl_desc, BorderLayout.NORTH);
		this.add(pnl_operationsTable, BorderLayout.CENTER);
		this.add(pnl_Bottom, BorderLayout.SOUTH);

		btn_DeleteOperation.setAction(new DeleteOperationAction(this, observedElementKey.getOrigin(), tbl_operationsTable));
		btn_1_1.setAction(gwasInOneGoAction);
		Action genFreqAndHWAction = new GenFreqAndHWAction(observedElementKey, gwasParams);
		genFreqAndHWAction.setEnabled(currentOP == null);
		btn_1_2.setAction(genFreqAndHWAction);
		btn_1_3.setAction(new AssociationTestsAction(observedElementKey, gwasParams, currentOP, this, OPType.ALLELICTEST));
		btn_1_4.setAction(new AssociationTestsAction(observedElementKey, gwasParams, currentOP, this, OPType.GENOTYPICTEST));
		btn_1_5.setAction(new AssociationTestsAction(observedElementKey, gwasParams, currentOP, this, OPType.TRENDTEST));
		btn_1_6.setAction(new AssociationTestsAction(observedElementKey, gwasParams, currentOP, this, OPType.COMBI_ASSOC_TEST));
		btn_Back.setAction(new BackAction(parent));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixAnalyse));
	}

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
	public static class AssociationTestsAction extends AbstractAction {

		private final DataSetKey parentKey;
		private GWASinOneGOParams gwasParams;
		private final OperationMetadata currentOP;
		private final OPType testType;
		private final String testName;
		private final Component dialogParent;

		AssociationTestsAction(final DataSetKey parentKey, GWASinOneGOParams gwasParams, OperationMetadata currentOP, Component dialogParent, OPType testType) {

			this.parentKey = parentKey;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			this.testType = testType;
			this.testName = OutputTest.createTestName(testType) + " Test";
			this.dialogParent = dialogParent;

			final String testNameHtml = "<html><div align='center'>" + testName + "<div></html>";
			putValue(NAME, testNameHtml);
		}

		private static OperationKey evaluateCensusOPId(OperationMetadata currentOP, DataSetKey parentKey) throws IOException {

			int censusOPId = OperationKey.NULL_ID;

			if (currentOP != null) {
				censusOPId = currentOP.getId();
			} else {
				// REQUEST WHICH CENSUS TO USE
				List<OPType> censusTypes = new ArrayList<OPType>();
				censusTypes.add(OPType.MARKER_CENSUS_BY_AFFECTION);
				censusTypes.add(OPType.MARKER_CENSUS_BY_PHENOTYPE);
				OperationMetadata markerCensusOP = Dialogs.showOperationCombo(parentKey, censusTypes, Text.Operation.GTFreqAndHW);
				if (markerCensusOP != null) {
					censusOPId = markerCensusOP.getId();
				}
			}

			return new OperationKey(parentKey.getOrigin(), censusOPId);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				OperationKey censusOPKey = evaluateCensusOPId(currentOP, parentKey);

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.WAIT_CURSOR);
				Set<Affection> affectionStates = SamplesParserManager.collectAffectionStates(parentKey);
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.DEFAULT_CURSOR);

				if (affectionStates.contains(Affection.UNAFFECTED)
						&& affectionStates.contains(Affection.AFFECTED))
				{
					List<OPType> necessaryOPs = new ArrayList<OPType>();
					necessaryOPs.add(OPType.MARKER_QA);
					if (testType != OPType.COMBI_ASSOC_TEST) {
						necessaryOPs.add(OPType.SAMPLE_QA);
						necessaryOPs.add(OPType.MARKER_CENSUS_BY_PHENOTYPE);
						necessaryOPs.add(OPType.MARKER_CENSUS_BY_AFFECTION);
						necessaryOPs.add(OPType.HARDY_WEINBERG);
					}
					List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentKey, false); // FIXME some of the operatiosn (the QA ones at least) shoudl be direct children!

					// WHAT TO DO IF OPs ARE MISSING
					boolean performTest = true;
					if (missingOPs.size() > 0) {
						if (missingOPs.contains(OPType.SAMPLE_QA)
								|| missingOPs.contains(OPType.MARKER_QA))
						{
							Dialogs.showWarningDialogue("Before performing the " + testName + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
//							MultiOperations.doMatrixQAs(parentMatrixKey);
							performTest = false;
						} else if (missingOPs.contains(OPType.MARKER_CENSUS_BY_AFFECTION)
								&& missingOPs.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE))
						{
							Dialogs.showWarningDialogue("Before performing the " + testName + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							performTest = false;
						} else if (missingOPs.contains(OPType.HARDY_WEINBERG)
								&& !(missingOPs.contains(OPType.MARKER_CENSUS_BY_AFFECTION)
								&& missingOPs.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE)))
						{
							Dialogs.showWarningDialogue("Before performing the " + testName + " you must launch\n a '" + Text.Operation.hardyWeiberg + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
//							MultiOperations.doHardyWeinberg(censusOPKey);
							performTest = false;
						}
					}

					// DO TEST
					if (performTest) {
						boolean reProceed = true;
						if (censusOPKey == null) {
							reProceed = false;
						}

						List<OperationKey> qaMarkersOffspringKeys = null;
						OperationKey hwOPKey = null;
						if (testType == OPType.COMBI_ASSOC_TEST) {
							List<OperationMetadata> qaMarkersOffspring
									= OperationsList.getOffspringOperationsMetadata(parentKey, OPType.MARKER_QA);
							qaMarkersOffspringKeys = new ArrayList<OperationKey>(qaMarkersOffspring.size());
							for (OperationMetadata qaMarkersOp : qaMarkersOffspring) {
								qaMarkersOffspringKeys.add(OperationKey.valueOf(qaMarkersOp));
							}
						} else {
							// GET HW OPERATION
							List<OperationMetadata> hwOperations = OperationsList.getChildrenOperationsMetadata(censusOPKey, OPType.HARDY_WEINBERG);
							for (OperationMetadata currentHWop : hwOperations) {
								// REQUEST WHICH HW TO USE
								// FIXME this looks strange.. just use the last one?
								if (currentHWop != null) {
									hwOPKey = OperationKey.valueOf(currentHWop);
								} else {
									reProceed = false;
								}
							}
						}

						CombiTestOperationParams combiTestParams = null;
						ByCombiWeightsFilterOperationParams combiFilterParams = null;
						if (reProceed) {
							if (testType == OPType.COMBI_ASSOC_TEST) {
								combiTestParams = new CombiTestOperationParams(qaMarkersOffspringKeys.get(0));
								combiTestParams = CombiTestParamsGUI.chooseParams(dialogParent, combiTestParams, qaMarkersOffspringKeys); // HACK FIXME for COMBI
								if (combiTestParams != null) {
									OperationMetadata testParentOperation = OperationsList.getOperationMetadata(combiTestParams.getParent().getOperationParent());
									final int totalMarkers = testParentOperation.getNumMarkers();
									combiFilterParams = new ByCombiWeightsFilterOperationParams(totalMarkers);
									combiFilterParams = ByCombiWeightsFilterOperationParamsEditor.chooseParams(dialogParent, combiFilterParams, null); // HACK FIXME for COMBI
									if (combiFilterParams != null) {
										gwasParams.setProceed(true);
									}
								}
							} else {
								gwasParams = new MoreAssocInfo().showMoreInfo();
							}
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();

							if (reProceed) {
								// >>>>>> START THREADING HERE <<<<<<<
								if (testType == OPType.COMBI_ASSOC_TEST) {
									MultiOperations.doCombiTest(combiTestParams, combiFilterParams);
								} else if (censusOPKey != null && hwOPKey != null) {
									MultiOperations.doTest(
											censusOPKey,
											hwOPKey,
											gwasParams,
											testType);
								}
							}
						}
					}
				} else {
					Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
				}
			} catch (IOException ex) {
				log.error(Text.Operation.warnOperationError, ex);
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
			}
		}
	}

	private static class GenFreqAndHWAction extends AbstractAction {

		private final DataSetKey parentKey;
		private GWASinOneGOParams gwasParams;

		GenFreqAndHWAction(final DataSetKey parentKey, GWASinOneGOParams gwasParams) {

			this.parentKey = parentKey;
			this.gwasParams = gwasParams;
			putValue(NAME, Text.Operation.htmlGTFreqAndHW);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				List<OPType> necessaryOPs = new ArrayList<OPType>();
				necessaryOPs.add(OPType.SAMPLE_QA);
				necessaryOPs.add(OPType.MARKER_QA);
				List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentKey, true);

				List<OperationMetadata> qaMarkersOps = OperationsList.getChildrenOperationsMetadata(parentKey, OPType.MARKER_QA);
				if (qaMarkersOps.isEmpty()) {
					Dialogs.showWarningDialogue("You must perform a Markers Quality Assurance before running a Marker Census operation!");
					return;
				}
				final OperationKey qaMarkersOpKey = OperationKey.valueOf(qaMarkersOps.get(0));

				List<OperationMetadata> qaSamplesOps = OperationsList.getChildrenOperationsMetadata(parentKey, OPType.SAMPLE_QA);
				if (qaMarkersOps.isEmpty()) {
					Dialogs.showWarningDialogue("You must perform a Samples Quality Assurance before running a Marker Census operation!");
					return;
				}
				final OperationKey qaSamplesOpKey = OperationKey.valueOf(qaSamplesOps.get(0));

				gwasParams.setMarkerCensusOperationParams(new MarkerCensusOperationParams(parentKey, qaSamplesOpKey, qaMarkersOpKey));

				int choice = Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB, Text.Operation.htmlAffectionFromFile, Text.All.cancel);
				if (choice == JOptionPane.NO_OPTION) {
					// BY EXTERNAL PHENOTYPE FILE
					final File phenotypeFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
					gwasParams.getMarkerCensusOperationParams().setPhenotypeFile(phenotypeFile);
					if (phenotypeFile != null) {
						gwasParams = new MoreInfoForGtFreq().showMoreInfo(gwasParams);
						if (choice != JOptionPane.CANCEL_OPTION) {
							gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
						}
					}
				} else if (choice != JOptionPane.CANCEL_OPTION) {
					gwasParams = new MoreInfoForGtFreq().showMoreInfo(gwasParams);
					if (choice != JOptionPane.CANCEL_OPTION) {
						gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
					}
				}

//				if (!gwasParams.isDiscardMarkerByMisRat()) {
//					gwasParams.setDiscardMarkerMisRatVal(1);
//				}
//				if (!gwasParams.isDiscardMarkerByHetzyRat()) {
//					gwasParams.setDiscardMarkerHetzyRatVal(1);
//				}
//				if (!gwasParams.isDiscardSampleByMisRat()) {
//					gwasParams.setDiscardSampleMisRatVal(1);
//				}
//				if (!gwasParams.isDiscardSampleByHetzyRat()) {
//					gwasParams.setDiscardSampleHetzyRatVal(1);
//				}

				if (gwasParams.isProceed()) {
					ProcessTab.getSingleton().showTab();
				}

				// <editor-fold defaultstate="expanded" desc="QA BLOCK">
				if (gwasParams.isProceed() && missingOPs.size() > 0) {
					gwasParams.setProceed(false);
					Dialogs.showWarningDialogue(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(parentKey);
				}
				// </editor-fold>

				// <editor-fold defaultstate="expanded" desc="GENOTYPE FREQ. & HW BLOCK">
			if (gwasParams.isProceed()) {
				gwasParams.getMarkerCensusOperationParams().setParent(parentKey);
				MultiOperations.doGTFreqDoHW(gwasParams);
			}
			// </editor-fold>
			} catch (Exception ex) {
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
				log.error(Text.All.warnLoadError, ex);
			}
		}
	}
	//</editor-fold>

	public static class DeleteOperationAction extends AbstractAction implements ListSelectionListener {

		private final MatrixKey parentMatrixKey;
		private final JTable matrixOperationsTable;
		private final Component dialogParent;

		DeleteOperationAction(Component dialogParent, MatrixKey parentMatrixKey, JTable matrixOperationsTable) {

			this.parentMatrixKey = parentMatrixKey;
			this.dialogParent = dialogParent;
			this.matrixOperationsTable = matrixOperationsTable;
			this.matrixOperationsTable.getSelectionModel().addListSelectionListener(this);
			setEnabled(!this.matrixOperationsTable.getSelectionModel().isSelectionEmpty());
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void valueChanged(ListSelectionEvent evt) {

			final boolean enable = !matrixOperationsTable.getSelectionModel().isSelectionEmpty();

			setEnabled(enable);
			if (enable) {
				putValue(SHORT_DESCRIPTION, "Delete the selected operations");
				putValue(LONG_DESCRIPTION, "Delete the selected operations and optionally also all associated report data");
			} else {
				putValue(SHORT_DESCRIPTION, "Please select the operation(s) to be delete");
				putValue(LONG_DESCRIPTION, null);
			}

			final String name;
			if (matrixOperationsTable.getSelectedRowCount() > 1) {
				name = Text.Operation.deleteOperation + "s";
			} else {
				name = Text.Operation.deleteOperation;
			}
			putValue(NAME, name);
		}

		private Map<Integer, OperationKey> getSelectedOperations() {

			final int[] selectedTableRows = matrixOperationsTable.getSelectedRows();
			final Map<Integer, OperationKey> selectedTableRowAndOperations = new LinkedHashMap<Integer, OperationKey>(selectedTableRows.length);
			for (int soi = selectedTableRows.length - 1; soi >= 0; soi--) {
				final int selectedTableRow = selectedTableRows[soi];
				final int operationId = (Integer) matrixOperationsTable.getModel().getValueAt(selectedTableRow, 0);
				selectedTableRowAndOperations.put(selectedTableRow, new OperationKey(parentMatrixKey, operationId));
			}

			return selectedTableRowAndOperations;
		}

		private void deleteOperations(Map<Integer, OperationKey> operationsToDelete, boolean deleteReports) throws IOException {

			OperationKey operationKey = null;
			OperationsTableModel tableModel = (OperationsTableModel) matrixOperationsTable.getModel();
			for (Map.Entry<Integer, OperationKey> selectedOperation : operationsToDelete.entrySet()) {
				operationKey = selectedOperation.getValue();
				// TEST IF THE DELETED ITEM IS REQUIRED FOR A QUEUED WORKER
				if (SwingWorkerItemList.permitsDeletionOf(operationKey)) {
					MultiOperations.deleteOperation(
							operationKey,
							deleteReports);
					tableModel.removeRow(selectedOperation.getKey());

					//OperationManager.deleteOperationAndChildren(parentMatrixKey.getStudyKey(), opId, deleteReport);
				} else {
					Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
				}
			}

//			if (currentlySelectedOPKey == operationKey) {
//				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
//			}
			GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			Map<Integer, OperationKey> selectedOperations = getSelectedOperations();
			if (!selectedOperations.isEmpty()) {
				try {
					final int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
					if (option == JOptionPane.YES_OPTION) {
						final int deleteReportsOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
						if (deleteReportsOption != JOptionPane.CANCEL_OPTION) {
							final boolean deleteReports = (deleteReportsOption == JOptionPane.YES_OPTION);
							deleteOperations(selectedOperations, deleteReports);
						}
					}
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		}
	}

	private static class GwasInOneGoAction extends AbstractAction {

		private final DataSetKey observedElementKey;
		private GWASinOneGOParams gwasParams;

		GwasInOneGoAction(DataSetKey observedElementKey, GWASinOneGOParams gwasParams) {

			this.observedElementKey = observedElementKey;
			this.gwasParams = gwasParams;
			setEnabled(false);
			putValue(NAME, Text.Operation.gwasInOneGo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			try {
				List<OPType> necessaryOPs = new ArrayList<OPType>();
				necessaryOPs.add(OPType.SAMPLE_QA);
				necessaryOPs.add(OPType.MARKER_QA);
				List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, observedElementKey, true);

				DataSetMetadata observedElementMetadata = MatricesList.getDataSetMetadata(observedElementKey);

				int choice = Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB, Text.Operation.htmlAffectionFromFile, Text.All.cancel);
				ImportFormat technology = null;
				if (observedElementMetadata instanceof MatrixMetadata) {
					technology = ((MatrixMetadata) observedElementMetadata).getTechnology();
				}
				if (choice == JOptionPane.NO_OPTION) { // BY EXTERNAL PHENOTYPE FILE
					final File phenotypeFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
					gwasParams.getMarkerCensusOperationParams().setPhenotypeFile(phenotypeFile);
					if (phenotypeFile != null) {
						gwasParams = new MoreGWASinOneGoInfo().showMoreInfo(technology);
						if (choice != JOptionPane.CANCEL_OPTION && gwasParams.isProceed()) {
							gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
						}
					}
				} else if (choice != JOptionPane.CANCEL_OPTION) {
					gwasParams = new MoreGWASinOneGoInfo().showMoreInfo(technology);
					if (choice != JOptionPane.CANCEL_OPTION && gwasParams.isProceed()) {
						gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
					}
				}

				if (gwasParams.isProceed()) {
					ProcessTab.getSingleton().showTab();
				}

				// QA BLOCK
				if (gwasParams.isProceed() && missingOPs.size() > 0) {
					gwasParams.setProceed(false);
					Dialogs.showWarningDialogue(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(observedElementKey);
				}

				// GWAS BLOCK
				if (gwasParams.isProceed()
						&& choice != JOptionPane.CANCEL_OPTION
						&& (gwasParams.isPerformAllelicTests() || gwasParams.isPerformTrendTests()))
				{
					// At least one test has been picked
					log.info(Text.All.processing);
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.WAIT_CURSOR);
					// use Sample Info affection state from the DB
					Set<Affection> affectionStates = SamplesParserManager.collectAffectionStates(observedElementKey);
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.DEFAULT_CURSOR);
					if (affectionStates.contains(Affection.UNAFFECTED)
							&& affectionStates.contains(Affection.AFFECTED))
					{
						gwasParams.getMarkerCensusOperationParams().setParent(observedElementKey);
						MultiOperations.doGWASwithAlterPhenotype(gwasParams);
					} else {
						Dialogs.showWarningDialogue(Text.Operation.warnAffectionMissing);
						MultiOperations.updateProcessOverviewStartNext();
					}
				}
			} catch (IOException ex) {
				log.error(null, ex);
			} catch (Exception ex) {
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
				log.error(Text.All.warnLoadError, ex);
			}
		}
	}

	public static class BackAction extends AbstractAction {

		private final DataSetKey parentElementKey;

		public BackAction(DataSetKey parentElementKey) {

			this.parentElementKey = parentElementKey;
			final boolean hasParent = (parentElementKey != null);
			if (!hasParent) {
				putValue(SHORT_DESCRIPTION, "There is no parent");
			}
			setEnabled(hasParent);
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			GWASpiExplorerPanel.getSingleton().selectNode(parentElementKey);
		}
	}
}
