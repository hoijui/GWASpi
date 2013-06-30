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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.gwaspi.cli.CombiTestScriptCommand;
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
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.combi.CombiTestParams;
import org.gwaspi.operations.combi.CombiTestParamsGUI;
import org.gwaspi.samples.SamplesParserManager;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixAnalysePanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(MatrixAnalysePanel.class);

	// Variables declaration - do not modify
	private final MatrixKey parentMatrixKey;
	private final OperationKey operationKey;
	private final OperationMetadata currentOP;
	private final JButton btn_1_1;
	private final JButton btn_1_2;
	private final JButton btn_1_3;
	private final JButton btn_1_4;
	private final JButton btn_1_5;
	private final JButton btn_1_6;
	private final JButton btn_Back;
	private final JButton btn_DeleteOperation;
	private final JButton btn_Help;
	private final JPanel pnl_Spacer;
	private final JPanel pnl_NewOperation;
	private final JPanel pnl_Buttons;
	private final JPanel pnl_Footer;
	private final JPanel pnl_MatrixDesc;
	private final JScrollPane scrl_MatrixDesc;
	private final JScrollPane scrl_MatrixOperations;
	private final JTable tbl_MatrixOperations;
	private final JTextArea txtA_Description;
	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private final Action gwasInOneGoAction;
	// End of variables declaration

	public MatrixAnalysePanel(MatrixKey parentMatrixKey, OperationKey operationKey) throws IOException {

		this.parentMatrixKey = parentMatrixKey;
		this.operationKey = operationKey;
		MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);

		if (operationKey != null) {
			currentOP = OperationsList.getOperation(operationKey);
		} else {
			currentOP = null;
		}

		pnl_MatrixDesc = new JPanel();
		scrl_MatrixDesc = new JScrollPane();
		txtA_Description = new JTextArea();
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
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Help = new JButton();
		pnl_Spacer = new JPanel();
		pnl_Buttons = new JPanel();
		pnl_NewOperation = new JPanel();
		btn_1_1 = new JButton();
		btn_1_2 = new JButton();
		btn_1_3 = new JButton();
		btn_1_4 = new JButton();
		btn_1_5 = new JButton();
		btn_1_6 = new JButton();

		gwasInOneGoAction = new GwasInOneGoAction(parentMatrixKey, gwasParams);
		gwasInOneGoAction.setEnabled(currentOP == null);

		setBorder(BorderFactory.createTitledBorder(null, Text.Operation.analyseData, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		if (operationKey != null) {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.operation + ": " + currentOP.getFriendlyName() + ", " + Text.Operation.operationId + ": " + currentOP.getId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(currentOP.getDescription().toString());
		} else {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrix + ": " + matrixMetadata.getMatrixFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(matrixMetadata.getDescription().toString());
		}

		scrl_MatrixDesc.setViewportView(txtA_Description);

		Collection<OperationMetadata> tableMatrix;
		if (currentOP != null) {
			tableMatrix = OperationsList.getOperationAndSubOperations(operationKey);
		} else {
			tableMatrix = OperationsList.getOperationsTable(parentMatrixKey);
		}

		tbl_MatrixOperations.setModel(new OperationsTableModel(tableMatrix));
		scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
		btn_DeleteOperation.setAction(new DeleteOperationAction(operationKey, this, parentMatrixKey, tbl_MatrixOperations));

		//<editor-fold defaultstate="expanded" desc="LAYOUT MATRIX DESC">
		GroupLayout pnl_MatrixDescLayout = new GroupLayout(pnl_MatrixDesc);
		pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
		pnl_MatrixDescLayout.setHorizontalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_MatrixDesc, GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
				.addComponent(scrl_MatrixOperations, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
				.addComponent(btn_DeleteOperation, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_MatrixDescLayout.setVerticalGroup(
				pnl_MatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_MatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_MatrixOperations, GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_DeleteOperation)
				.addContainerGap()));
		//</editor-fold>

		pnl_NewOperation.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.newOperation, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		pnl_NewOperation.setMaximumSize(new Dimension(32767, 100));
		pnl_NewOperation.setPreferredSize(new Dimension(926, 100));

		btn_1_1.setAction(gwasInOneGoAction);

		Action genFreqAndHWAction = new GenFreqAndHWAction(parentMatrixKey, gwasParams);
		genFreqAndHWAction.setEnabled(currentOP == null);
		btn_1_2.setAction(genFreqAndHWAction);

		btn_1_3.setAction(new AssociationTestsAction(parentMatrixKey, gwasParams, currentOP, this, true, false));

		btn_1_4.setAction(new AssociationTestsAction(parentMatrixKey, gwasParams, currentOP, this, false, false));

		btn_1_5.setAction(new TrendTestsAction(parentMatrixKey, gwasParams, currentOP));

		btn_1_6.setAction(new AssociationTestsAction(parentMatrixKey, gwasParams, currentOP, this, true, true));

		//<editor-fold defaultstate="expanded" desc="LAYOUT BUTTONS">
		GroupLayout pnl_SpacerLayout = new GroupLayout(pnl_Spacer);
		pnl_Spacer.setLayout(pnl_SpacerLayout);
		pnl_SpacerLayout.setHorizontalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 46, Short.MAX_VALUE));
		pnl_SpacerLayout.setVerticalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 124, Short.MAX_VALUE));

		pnl_Buttons.setLayout(new GridLayout(0, 3, 18, 18));
		pnl_Buttons.add(btn_1_1);
		pnl_Buttons.add(btn_1_3);
		pnl_Buttons.add(btn_1_5);
		pnl_Buttons.add(btn_1_2);
		pnl_Buttons.add(btn_1_4);
		pnl_Buttons.add(btn_1_6);

		GroupLayout pnl_NewOperationLayout = new GroupLayout(pnl_NewOperation);
		pnl_NewOperation.setLayout(pnl_NewOperationLayout);
		pnl_NewOperationLayout.setHorizontalGroup(
				pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_Spacer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Buttons, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_NewOperationLayout.setVerticalGroup(
				pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
				.addGroup(pnl_NewOperationLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_Buttons, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Spacer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		//</editor-fold>

		btn_Back.setAction(new BackAction(parentMatrixKey, currentOP));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixAnalyse));

		//<editor-fold defaultstate="expanded" desc="LAYOUT FOOTER">
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 565, Short.MAX_VALUE)
				.addComponent(btn_Help)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(53, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_NewOperation, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MatrixDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(0, 0, 0)));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_MatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_NewOperation, GroupLayout.PREFERRED_SIZE, 159, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		//</editor-fold>
	}

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
	public static class AssociationTestsAction extends AbstractAction {

		private final MatrixKey parentMatrixKey;
		private GWASinOneGOParams gwasParams;
		private final OperationMetadata currentOP;
		private final boolean allelic;
		private final boolean combi;
		private final String testName;
		private final String testNameHtml;
		private final Component dialogParent;

		AssociationTestsAction(MatrixKey parentMatrixKey, GWASinOneGOParams gwasParams, OperationMetadata currentOP, Component dialogParent, boolean allelic, boolean combi) {

			this.parentMatrixKey = parentMatrixKey;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			this.allelic = allelic;
			this.combi = combi;
			if (combi) {
				this.testName = "Combi Association Test";
				this.testNameHtml = "<html><div align='center'>Combi Association Test<div></html>";
			} else {
				this.testName = (allelic ?  Text.Operation.allelicAssocTest : Text.Operation.genoAssocTest);
				this.testNameHtml = (allelic ? Text.Operation.htmlAllelicAssocTest : Text.Operation.htmlGenotypicTest);
			}
			this.dialogParent = dialogParent;
			putValue(NAME, testNameHtml);
		}

		public static OperationKey evaluateCensusOPId(OperationMetadata currentOP, MatrixKey parentMatrixKey) throws IOException {

			int censusOPId = Integer.MIN_VALUE;

			if (currentOP != null) {
				censusOPId = currentOP.getId();
			} else {
				// REQUEST WHICH CENSUS TO USE
				List<String> censusTypes = new ArrayList<String>();
				censusTypes.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());
				censusTypes.add(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
				OperationMetadata markerCensusOP = Dialogs.showOperationCombo(parentMatrixKey, censusTypes, Text.Operation.GTFreqAndHW);
				if (markerCensusOP != null) {
					censusOPId = markerCensusOP.getId();
				}
			}

			return new OperationKey(parentMatrixKey, censusOPId);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				OperationKey censusOPKey = evaluateCensusOPId(currentOP, parentMatrixKey);

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.WAIT_CURSOR);
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(parentMatrixKey);
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.DEFAULT_CURSOR);

				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					List<OPType> necessaryOPs = new ArrayList<OPType>();
					necessaryOPs.add(OPType.SAMPLE_QA);
					necessaryOPs.add(OPType.MARKER_QA);
					necessaryOPs.add(OPType.MARKER_CENSUS_BY_PHENOTYPE);
					necessaryOPs.add(OPType.MARKER_CENSUS_BY_AFFECTION);
					necessaryOPs.add(OPType.HARDY_WEINBERG);
					List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentMatrixKey);

					// WHAT TO DO IF OPs ARE MISSING
					boolean performTest = true;
					if (missingOPs.size() > 0) {
						if (missingOPs.contains(OPType.SAMPLE_QA)
								|| missingOPs.contains(OPType.MARKER_QA))
						{
							Dialogs.showWarningDialogue("Before performing the " + testName + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doMatrixQAs(parentMatrixKey);
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
							MultiOperations.doHardyWeinberg(censusOPKey);
							performTest = false;
						}
					}

					// DO TEST
					if (performTest) {
						boolean reProceed = true;
						if (censusOPKey == null) {
							reProceed = false;
						}

						OperationKey hwOPKey = null;
						// GET HW OPERATION
						List<OperationMetadata> hwOperations = OperationsList.getOperationsList(parentMatrixKey.getMatrixId(), censusOPKey.getId(), OPType.HARDY_WEINBERG);
						for (OperationMetadata currentHWop : hwOperations) {
							// REQUEST WHICH HW TO USE
							// FIXME this looks strange.. just use the last one?
							if (currentHWop != null) {
								hwOPKey = OperationKey.valueOf(currentHWop);
							} else {
								reProceed = false;
							}
						}

						CombiTestParams combiTestParams = null;
						if (reProceed) {
							if (combi) {
								combiTestParams = new CombiTestParams(
										parentMatrixKey,
										hwOPKey
										);
								combiTestParams = CombiTestParamsGUI.chooseCombiTestParams(dialogParent, combiTestParams);
								gwasParams.setProceed(true);
//								genotypeEncoder = GenotypeEncoderChooserGUI.chooseGenotypeEncoder(dialogParent);
							} else {
								gwasParams = new MoreAssocInfo().showMoreInfo();
							}
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();

							if (reProceed && censusOPKey != null && hwOPKey != null) {

								// >>>>>> START THREADING HERE <<<<<<<
								if (combi) {
									if (combiTestParams != null) {
										MultiOperations.doCombiTest(combiTestParams);
									}
								} else {
									MultiOperations.doAssociationTest(
											parentMatrixKey,
											censusOPKey,
											hwOPKey,
											gwasParams,
											allelic);
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

	private static class TrendTestsAction extends AbstractAction {

		private final MatrixKey parentMatrixKey;
		private GWASinOneGOParams gwasParams;
		private final OperationMetadata currentOP;

		TrendTestsAction(MatrixKey parentMatrixKey, GWASinOneGOParams gwasParams, OperationMetadata currentOP) {

			this.parentMatrixKey = parentMatrixKey;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			putValue(NAME, Text.Operation.htmlTrendTest);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				OperationKey censusOPKey = AssociationTestsAction.evaluateCensusOPId(currentOP, parentMatrixKey);
				OperationKey hwOPKey = null;

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.WAIT_CURSOR);
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(parentMatrixKey);
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.DEFAULT_CURSOR);

				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					List<OPType> necessaryOPs = new ArrayList<OPType>();
					necessaryOPs.add(OPType.SAMPLE_QA);
					necessaryOPs.add(OPType.MARKER_QA);
					necessaryOPs.add(OPType.MARKER_CENSUS_BY_PHENOTYPE);
					necessaryOPs.add(OPType.MARKER_CENSUS_BY_AFFECTION);
					necessaryOPs.add(OPType.HARDY_WEINBERG);
					List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentMatrixKey);

					// WHAT TO DO IF OPs ARE MISSING
					boolean performTest = true;
					if (missingOPs.size() > 0) {
						if (missingOPs.contains(OPType.SAMPLE_QA)
								|| missingOPs.contains(OPType.MARKER_QA))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doMatrixQAs(parentMatrixKey);
							performTest = false;
						} else if (missingOPs.contains(OPType.MARKER_CENSUS_BY_AFFECTION)
								&& missingOPs.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							performTest = false;
						} else if (missingOPs.contains(OPType.HARDY_WEINBERG)
								&& !(missingOPs.contains(OPType.MARKER_CENSUS_BY_AFFECTION)
								&& missingOPs.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE)))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.hardyWeiberg + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doHardyWeinberg(censusOPKey);
							performTest = false;
						}
					}

					// DO TEST
					if (performTest) {
						boolean reProceed = true;
						if (censusOPKey == null) {
							reProceed = false;
						}

						if (reProceed) {
							gwasParams = new MoreAssocInfo().showMoreInfo();
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();
							// GET HW OPERATION
							List<OperationMetadata> hwOperations = OperationsList.getOperationsList(parentMatrixKey.getMatrixId(), censusOPKey.getId(), OPType.HARDY_WEINBERG);
							for (OperationMetadata currentHWop : hwOperations) {
								// REQUEST WHICH HW TO USE
								if (currentHWop != null) {
									hwOPKey = OperationKey.valueOf(currentHWop);
								} else {
									reProceed = false;
								}
							}

							if (reProceed && censusOPKey != null && hwOPKey != null) {

								// >>>>>> START THREADING HERE <<<<<<<
								MultiOperations.doTrendTest(
										parentMatrixKey,
										censusOPKey,
										hwOPKey,
										gwasParams);
							}
						}
					}
				} else {
					Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
				}
			} catch (Exception ex) {
				log.error(Text.Operation.warnOperationError, ex);
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
			}
		}
	}

	private static class GenFreqAndHWAction extends AbstractAction {

		private MatrixKey parentMatrixKey;
		private GWASinOneGOParams gwasParams;

		GenFreqAndHWAction(MatrixKey parentMatrixKey, GWASinOneGOParams gwasParams) {

			this.parentMatrixKey = parentMatrixKey;
			this.gwasParams = gwasParams;
			putValue(NAME, Text.Operation.htmlGTFreqAndHW);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				List<OPType> necessaryOPs = new ArrayList<OPType>();
				necessaryOPs.add(OPType.SAMPLE_QA);
				necessaryOPs.add(OPType.MARKER_QA);
				List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentMatrixKey);

				int choice = Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB, Text.Operation.htmlAffectionFromFile, Text.All.cancel);
				File phenotypeFile = null;
				if (choice == JOptionPane.NO_OPTION) { //BY EXTERNAL PHENOTYPE FILE
					phenotypeFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
					if (phenotypeFile != null) {
						gwasParams = new MoreInfoForGtFreq().showMoreInfo();
						if (choice != JOptionPane.CANCEL_OPTION) {
							gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
						}
					}
				} else if (choice != JOptionPane.CANCEL_OPTION) {
					gwasParams = new MoreInfoForGtFreq().showMoreInfo();
					if (choice != JOptionPane.CANCEL_OPTION) {
						gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
					}
				}

				if (!gwasParams.isDiscardMarkerByMisRat()) {
					gwasParams.setDiscardMarkerMisRatVal(1);
				}
				if (!gwasParams.isDiscardMarkerByHetzyRat()) {
					gwasParams.setDiscardMarkerHetzyRatVal(1);
				}
				if (!gwasParams.isDiscardSampleByMisRat()) {
					gwasParams.setDiscardSampleMisRatVal(1);
				}
				if (!gwasParams.isDiscardSampleByHetzyRat()) {
					gwasParams.setDiscardSampleHetzyRatVal(1);
				}

				if (gwasParams.isProceed()) {
					ProcessTab.getSingleton().showTab();
				}

				// <editor-fold defaultstate="expanded" desc="QA BLOCK">
				if (gwasParams.isProceed() && missingOPs.size() > 0) {
					gwasParams.setProceed(false);
					gwasParams.setProceed(false);
					Dialogs.showWarningDialogue(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(parentMatrixKey);
				}
				// </editor-fold>

				// <editor-fold defaultstate="expanded" desc="GENOTYPE FREQ. & HW BLOCK">
			if (gwasParams.isProceed()) {
				MultiOperations.doGTFreqDoHW(
						new MatrixKey(parentMatrixKey.getStudyKey(), parentMatrixKey.getMatrixId()),
						phenotypeFile,
						gwasParams);
			}
			// </editor-fold>
			} catch (Exception ex) {
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
				log.error(Text.All.warnLoadError, ex);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	public static class DeleteOperationAction extends AbstractAction implements ListSelectionListener {

		private final OperationKey currentlySelectedOPKey;
		private final MatrixKey parentMatrixKey;
		private final JTable matrixOperationsTable;
		private final Component dialogParent;

		DeleteOperationAction(OperationKey currentlySelectedOPKey, Component dialogParent, MatrixKey parentMatrixKey, JTable matrixOperationsTable) {

			this.currentlySelectedOPKey = currentlySelectedOPKey; // NOTE this might be null
			this.parentMatrixKey = parentMatrixKey;
			this.dialogParent = dialogParent;
			this.matrixOperationsTable = matrixOperationsTable;
			this.matrixOperationsTable.getSelectionModel().addListSelectionListener(this);
			setEnabled(!this.matrixOperationsTable.getSelectionModel().isSelectionEmpty());
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void valueChanged(ListSelectionEvent evt) {
			setEnabled(!this.matrixOperationsTable.getSelectionModel().isSelectionEmpty());
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] selectedOPs = matrixOperationsTable.getSelectedRows();
			if (selectedOPs.length > 0) {
				try {
					int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
					if (option == JOptionPane.YES_OPTION) {
						int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
						OperationKey operationKey = null;
						if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
							OperationsTableModel tableModel = (OperationsTableModel) matrixOperationsTable.getModel();
							for (int i = selectedOPs.length - 1; i >= 0; i--) {
								int tmpOPRow = selectedOPs[i];
								int operationId = (Integer) matrixOperationsTable.getModel().getValueAt(tmpOPRow, 0);
								operationKey = new OperationKey(parentMatrixKey, operationId);
								// TEST IF THE DELETED ITEM IS REQUIRED FOR A QUEUED WORKER
								if (SwingWorkerItemList.permitsDeletionOf(operationKey)) {
									if (option == JOptionPane.YES_OPTION) {
										boolean deleteReport = false;
										if (deleteReportOption == JOptionPane.YES_OPTION) {
											deleteReport = true;
										}
										MultiOperations.deleteOperation(
												operationKey,
												deleteReport);
										tableModel.removeRow(selectedOPs[i]);

										//OperationManager.deleteOperationAndChildren(parentMatrixKey.getStudyKey(), opId, deleteReport);
									}
								} else {
									Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
								}
							}

							if (currentlySelectedOPKey == operationKey) {
								GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
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

	private static class GwasInOneGoAction extends AbstractAction {

		private final MatrixKey parentMatrixKey;
		private GWASinOneGOParams gwasParams;

		GwasInOneGoAction(MatrixKey parentMatrix, GWASinOneGOParams gwasParams) {

			this.parentMatrixKey = parentMatrix;
			this.gwasParams = gwasParams;
			setEnabled(false);
			putValue(NAME, Text.Operation.gwasInOneGo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
//				List<String> blackListOPsAL = new ArrayList<String>();
//				blackListOPsAL.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());

				List<OPType> necessaryOPs = new ArrayList<OPType>();
				necessaryOPs.add(OPType.SAMPLE_QA);
				necessaryOPs.add(OPType.MARKER_QA);
				List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, parentMatrixKey);

				MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);

				int choice = Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB, Text.Operation.htmlAffectionFromFile, Text.All.cancel);
				File phenotypeFile = null;
				if (choice == JOptionPane.NO_OPTION) { // BY EXTERNAL PHENOTYPE FILE
					phenotypeFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
					if (phenotypeFile != null) {
						gwasParams = new MoreGWASinOneGoInfo().showMoreInfo(matrixMetadata.getTechnology().toString());
						if (choice != JOptionPane.CANCEL_OPTION && gwasParams.isProceed()) {
							gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
						}
					}
				} else if (choice != JOptionPane.CANCEL_OPTION) {
					gwasParams = new MoreGWASinOneGoInfo().showMoreInfo(matrixMetadata.getTechnology().toString());
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
					MultiOperations.doMatrixQAs(parentMatrixKey);
				}

				// GWAS BLOCK
				if (gwasParams.isProceed()
						&& choice != JOptionPane.CANCEL_OPTION
						&& (gwasParams.isPerformAllelicTests() || gwasParams.isPerformTrendTests())) { // At least one test has been picked
					log.info(Text.All.processing);
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.WAIT_CURSOR);
					Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(parentMatrixKey); // use Sample Info file affection state
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.DEFAULT_CURSOR);
					if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
							&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
					{
						MultiOperations.doGWASwithAlterPhenotype(
								new MatrixKey(
								parentMatrixKey.getStudyKey(),
								parentMatrixKey.getMatrixId()),
								phenotypeFile,
								gwasParams);
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

	private static class BackAction extends AbstractAction {

		private final MatrixKey parentMatrix;
		private final OperationMetadata currentOP;

		BackAction(MatrixKey parentMatrix, OperationMetadata currentOP) {

			this.parentMatrix = parentMatrix;
			this.currentOP = currentOP;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				if (currentOP != null && currentOP.getParentOperationId() != -1) {
					GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(new MatrixKey(currentOP.getStudyKey(), currentOP.getParentMatrixId()), OperationKey.valueOf(currentOP)));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				} else if (currentOP != null) {
					GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				} else {
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>
}
