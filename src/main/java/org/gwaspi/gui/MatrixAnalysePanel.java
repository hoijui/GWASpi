package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.MoreAssocInfo;
import org.gwaspi.gui.utils.MoreGWASinOneGoInfo;
import org.gwaspi.gui.utils.MoreInfoForGtFreq;
import org.gwaspi.gui.utils.NodeToPathCorrespondence;
import org.gwaspi.gui.utils.RowRendererDefault;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import org.gwaspi.model.Matrix;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SamplesParser;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.SwingWorkerItemList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixAnalysePanel extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(MatrixAnalysePanel.class);

	// Variables declaration - do not modify
	private Matrix parentMatrix;
	private final Operation currentOP;
	private Map<Integer, Object> treeChildrenMap = new LinkedHashMap<Integer, Object>();
	private JButton btn_1_1;
	private JButton btn_1_2;
	private JButton btn_1_3;
	private JButton btn_1_4;
	private JButton btn_1_5;
	private JButton btn_Back;
	private JButton btn_DeleteOperation;
	private JButton btn_Help;
	private JPanel pnl_Spacer;
	private JPanel pnl_NewOperation;
	private JPanel pnl_Buttons;
	private JPanel pnl_Footer;
	private JPanel pnl_MatrixDesc;
	private JScrollPane scrl_MatrixDesc;
	private JScrollPane scrl_MatrixOperations;
	private JTable tbl_MatrixOperations;
	private JTextArea txtA_Description;
	public GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private final Action gwasInOneGoAction;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixAnalysePanel(int _matrixId, int _opId) throws IOException {

		parentMatrix = new Matrix(_matrixId);
		if (_opId != Integer.MIN_VALUE) {
			currentOP = new Operation(_opId);
		} else {
			currentOP = null;
		}
		DefaultMutableTreeNode matrixNode = (DefaultMutableTreeNode) GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent();
		treeChildrenMap = NodeToPathCorrespondence.buildNodeToPathCorrespondence(matrixNode, true);

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

		gwasInOneGoAction = new GwasInOneGoAction(parentMatrix, gwasParams);
		gwasInOneGoAction.setEnabled(currentOP == null);

		setBorder(BorderFactory.createTitledBorder(null, Text.Operation.analyseData, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		txtA_Description.setColumns(20);
		txtA_Description.setRows(5);
		txtA_Description.setEditable(false);
		txtA_Description.setBorder(BorderFactory.createTitledBorder(null, Text.All.description, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		if (_opId != Integer.MIN_VALUE) {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Operation.operation + ": " + currentOP.getOperationFriendlyName() + ", " + Text.Operation.operationId + ": " + currentOP.getOperationId(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(currentOP.getDescription().toString());
		} else {
			pnl_MatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.matrix + ": " + parentMatrix.matrixMetadata.getMatrixFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
			txtA_Description.setText(parentMatrix.matrixMetadata.getDescription().toString());
		}

		scrl_MatrixDesc.setViewportView(txtA_Description);

		Object[][] tableMatrix;
		if (currentOP != null) {
			tableMatrix = org.gwaspi.model.OperationsList.getOperationsTable(_matrixId, currentOP.getOperationId());
		} else {
			tableMatrix = org.gwaspi.model.OperationsList.getOperationsTable(_matrixId);
		}

		tbl_MatrixOperations.setModel(new DefaultTableModel(
				tableMatrix,
				new String[]{
					Text.Operation.operationId, Text.Operation.operationName, Text.All.description, Text.All.createDate
				}));
		scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
		btn_DeleteOperation.setAction(new DeleteOperationAction(currentOP, this, parentMatrix, tbl_MatrixOperations));

		//<editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESC">
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

		Action genFreqAndHWAction = new GenFreqAndHWAction(parentMatrix, gwasParams);
		genFreqAndHWAction.setEnabled(currentOP == null);
		btn_1_2.setAction(genFreqAndHWAction);

		btn_1_3.setAction(new AllelicTestsAction(parentMatrix, gwasParams, currentOP));

		btn_1_4.setAction(new GenotypicTestsAction(parentMatrix, gwasParams, currentOP));

		btn_1_5.setAction(new TrendTestsAction(parentMatrix, gwasParams, currentOP));

		//<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
		GroupLayout pnl_SpacerLayout = new GroupLayout(pnl_Spacer);
		pnl_Spacer.setLayout(pnl_SpacerLayout);
		pnl_SpacerLayout.setHorizontalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 46, Short.MAX_VALUE));
		pnl_SpacerLayout.setVerticalGroup(
				pnl_SpacerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(0, 124, Short.MAX_VALUE));

		GroupLayout pnl_ButtonsLayout = new GroupLayout(pnl_Buttons);
		pnl_Buttons.setLayout(pnl_ButtonsLayout);
		pnl_ButtonsLayout.setHorizontalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_1_2, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_1, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_1_3, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_4, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addComponent(btn_1_5, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
				.addGap(108, 108, 108)));

		pnl_ButtonsLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_1_1, btn_1_2, btn_1_3, btn_1_4});

		pnl_ButtonsLayout.setVerticalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_1_1, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_3, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_5, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_1_2, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_4, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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

		btn_Back.setAction(new BackAction(parentMatrix, currentOP));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixAnalyse));

		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
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

		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
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

	//<editor-fold defaultstate="collapsed" desc="ANALYSIS">
	private static class AllelicTestsAction extends AbstractAction {

		private Matrix parentMatrix;
		private GWASinOneGOParams gwasParams;
		private final Operation currentOP;

		AllelicTestsAction(Matrix parentMatrix, GWASinOneGOParams gwasParams, Operation currentOP) {

			this.parentMatrix = parentMatrix;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			putValue(NAME, Text.Operation.htmlAllelicAssocTest);
		}

		public static int evaluateCensusOPId(Operation currentOP, Matrix parentMatrix) throws IOException {

			int censusOPId = Integer.MIN_VALUE;

			if (currentOP != null) {
				censusOPId = currentOP.getOperationId();
			} else {
				// REQUEST WHICH CENSUS TO USE
				List<String> censusTypesAL = new ArrayList<String>();
				censusTypesAL.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());
				censusTypesAL.add(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
				Operation markerCensusOP = Dialogs.showOperationCombo(parentMatrix.getMatrixId(), censusTypesAL, Text.Operation.GTFreqAndHW);
				if (markerCensusOP != null) {
					censusOPId = markerCensusOP.getOperationId();
				}
			}

			return censusOPId;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int censusOPId = evaluateCensusOPId(currentOP, parentMatrix);
				int hwOPId = Integer.MIN_VALUE;

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
				Set<Object> affectionStates = org.gwaspi.samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.defaultCursor);

				if (affectionStates.contains("1") && affectionStates.contains("2")) {

					List<String> necessaryOPsAL = new ArrayList<String>();
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
					List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

					// WHAT TO DO IF OPs ARE MISSING
					int decision = JOptionPane.YES_OPTION;
					if (missingOPsAL.size() > 0) {
						if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
								|| missingOPsAL.contains(OPType.MARKER_QA.toString())) {
							Dialogs.showWarningDialogue("Before performing an " + Text.Operation.allelicAssocTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())) {
							Dialogs.showWarningDialogue("Before performing an " + Text.Operation.allelicAssocTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
								&& !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))) {
							Dialogs.showWarningDialogue("Before performing an " + Text.Operation.allelicAssocTest + " you must launch\n a '" + Text.Operation.hardyWeiberg + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									censusOPId);
							decision = JOptionPane.NO_OPTION;
						}
					}

					// DO ALLELIC TEST
					if (decision == JOptionPane.YES_OPTION) {

						boolean reProceed = true;
						if (censusOPId == Integer.MIN_VALUE) {
							reProceed = false;
						}

						if (reProceed) {
							gwasParams = new MoreAssocInfo().showMoreInfo();
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();
							// GET HW OPERATION
							OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
							for (Operation currentHWop : hwOPList.operationsListAL) {
								// REQUEST WHICH HW TO USE
								if (currentHWop != null) {
									hwOPId = currentHWop.getOperationId();
								} else {
									reProceed = false;
								}
							}

							if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

								//>>>>>> START THREADING HERE <<<<<<<
								MultiOperations.doAllelicAssociationTest(parentMatrix.getStudyId(),
										parentMatrix.getMatrixId(),
										censusOPId,
										hwOPId,
										gwasParams);
							}
						}
					}
				} else {
					Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
				}
			} catch (Exception ex) {
				//Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
			}
		}
	}

	private static class GenotypicTestsAction extends AbstractAction {

		private Matrix parentMatrix;
		private GWASinOneGOParams gwasParams;
		private final Operation currentOP;

		GenotypicTestsAction(Matrix parentMatrix, GWASinOneGOParams gwasParams, Operation currentOP) {

			this.parentMatrix = parentMatrix;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			putValue(NAME, Text.Operation.htmlGenotypicTest);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int censusOPId = AllelicTestsAction.evaluateCensusOPId(currentOP, parentMatrix);
				int hwOPId = Integer.MIN_VALUE;

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
				Set<Object> affectionStates = org.gwaspi.samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.defaultCursor);

				if (affectionStates.contains("1") && affectionStates.contains("2")) {

					List<String> necessaryOPsAL = new ArrayList<String>();
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
					List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

					// WHAT TO DO IF OPs ARE MISSING
					int decision = JOptionPane.YES_OPTION;
					if (missingOPsAL.size() > 0) {
						if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
								|| missingOPsAL.contains(OPType.MARKER_QA.toString())) {
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.genoAssocTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())) {
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.genoAssocTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
								&& !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))) {
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.genoAssocTest + " you must launch\n a '" + Text.Operation.hardyWeiberg + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									censusOPId);
							decision = JOptionPane.NO_OPTION;
						}
					}

					// DO TEST
					if (decision == JOptionPane.YES_OPTION) {

						boolean reProceed = true;
						if (censusOPId == Integer.MIN_VALUE) {
							reProceed = false;
						}

						if (reProceed) {
							gwasParams = new MoreAssocInfo().showMoreInfo();
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();
							// GET HW OPERATION
							OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
							for (Operation currentHWop : hwOPList.operationsListAL) {
								// REQUEST WHICH HW TO USE
								if (currentHWop != null) {
									hwOPId = currentHWop.getOperationId();
								} else {
									reProceed = false;
								}
							}

							if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

								//>>>>>> START THREADING HERE <<<<<<<
								MultiOperations.doGenotypicAssociationTest(parentMatrix.getStudyId(),
										parentMatrix.getMatrixId(),
										censusOPId,
										hwOPId,
										gwasParams);
							}
						}
					}
				} else {
					Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
				}
			} catch (Exception ex) {
				//Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
			}
		}
	}

	private static class TrendTestsAction extends AbstractAction {

		private Matrix parentMatrix;
		private GWASinOneGOParams gwasParams;
		private final Operation currentOP;

		TrendTestsAction(Matrix parentMatrix, GWASinOneGOParams gwasParams, Operation currentOP) {

			this.parentMatrix = parentMatrix;
			this.gwasParams = gwasParams;
			this.currentOP = currentOP;
			putValue(NAME, Text.Operation.htmlTrendTest);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int censusOPId = AllelicTestsAction.evaluateCensusOPId(currentOP, parentMatrix);
				int hwOPId = Integer.MIN_VALUE;

				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
				Set<Object> affectionStates = org.gwaspi.samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
				StartGWASpi.mainGUIFrame.setCursor(CursorUtils.defaultCursor);

				if (affectionStates.contains("1") && affectionStates.contains("2")) {

					List<String> necessaryOPsAL = new ArrayList<String>();
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
					necessaryOPsAL.add(cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
					List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

					// WHAT TO DO IF OPs ARE MISSING
					int decision = JOptionPane.YES_OPTION;
					if (missingOPsAL.size() > 0) {
						if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
								|| missingOPsAL.contains(OPType.MARKER_QA.toString()))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.GTFreqAndHW + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							decision = JOptionPane.NO_OPTION;
						} else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
								&& !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
								&& missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())))
						{
							Dialogs.showWarningDialogue("Before performing a " + Text.Operation.trendTest + " you must launch\n a '" + Text.Operation.hardyWeiberg + "' first or perform a '" + Text.Operation.gwasInOneGo + "' instead.");
							MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									censusOPId);
							decision = JOptionPane.NO_OPTION;
						}
					}

					// DO TEST
					if (decision == JOptionPane.YES_OPTION) {

						boolean reProceed = true;
						if (censusOPId == Integer.MIN_VALUE) {
							reProceed = false;
						}

						if (reProceed) {
							gwasParams = new MoreAssocInfo().showMoreInfo();
						}

						if (gwasParams.isProceed()) {
							ProcessTab.getSingleton().showTab();
							//GET HW OPERATION
							OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
							for (Operation currentHWop : hwOPList.operationsListAL) {
								//REQUEST WHICH HW TO USE
								if (currentHWop != null) {
									hwOPId = currentHWop.getOperationId();
								} else {
									reProceed = false;
								}
							}

							if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

								//>>>>>> START THREADING HERE <<<<<<<
								MultiOperations.doTrendTest(parentMatrix.getStudyId(),
										parentMatrix.getMatrixId(),
										censusOPId,
										hwOPId,
										gwasParams);
							}
						}
					}
				} else {
					Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
				}
			} catch (Exception ex) {
				//Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
				Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
			}
		}
	}

	private static class GenFreqAndHWAction extends AbstractAction {

		private Matrix parentMatrix;
		private GWASinOneGOParams gwasParams;

		GenFreqAndHWAction(Matrix parentMatrix, GWASinOneGOParams gwasParams) {

			this.parentMatrix = parentMatrix;
			this.gwasParams = gwasParams;
			putValue(NAME, Text.Operation.htmlGTFreqAndHW);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				List<String> necessaryOPsAL = new ArrayList<String>();
				necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
				List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

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

				// <editor-fold defaultstate="collapsed" desc="QA BLOCK">
				if (gwasParams.isProceed() && missingOPsAL.size() > 0) {
					gwasParams.setProceed(false);
					gwasParams.setProceed(false);
					Dialogs.showWarningDialogue(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
				}
				// </editor-fold>

				// <editor-fold defaultstate="collapsed" desc="GENOTYPE FREQ. & HW BLOCK">
			if (gwasParams.isProceed()) {
				MultiOperations.doGTFreqDoHW(parentMatrix.getStudyId(),
						parentMatrix.getMatrixId(),
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

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private static class DeleteOperationAction extends AbstractAction {

		private Operation currentOP;
		private Component dialogParent;
		private Matrix parentMatrix;
		private JTable table;

		DeleteOperationAction(Operation currentOP, Component dialogParent, Matrix parentMatrix, JTable table) {

			this.currentOP = currentOP;
			this.dialogParent = dialogParent;
			this.parentMatrix = parentMatrix;
			this.table = table;
			putValue(NAME, Text.Operation.deleteOperation);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			int[] selectedOPs = table.getSelectedRows();
			if (selectedOPs.length > 0) {
				try {
					int option = JOptionPane.showConfirmDialog(dialogParent, Text.Operation.confirmDelete1);
					if (option == JOptionPane.YES_OPTION) {
						int deleteReportOption = JOptionPane.showConfirmDialog(dialogParent, Text.Reports.confirmDelete);
						int opId = Integer.MIN_VALUE;
						if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
							for (int i = selectedOPs.length - 1; i >= 0; i--) {
								int tmpOPRow = selectedOPs[i];
								opId = (Integer) table.getModel().getValueAt(tmpOPRow, 0);
								//TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
								if (SwingWorkerItemList.permitsDeletion(null, null, opId)) {
									if (option == JOptionPane.YES_OPTION) {
										boolean deleteReport = false;
										if (deleteReportOption == JOptionPane.YES_OPTION) {
											deleteReport = true;
										}
										MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), opId, deleteReport);

										//OperationManager.deleteOperationBranch(parentMatrix.getStudyId(), opId, deleteReport);
									}
								} else {
									Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
								}
							}

							if (currentOP.getOperationId() == opId) {
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

		private Matrix parentMatrix;
		private GWASinOneGOParams gwasParams;

		GwasInOneGoAction(Matrix parentMatrix, GWASinOneGOParams gwasParams) {

			this.parentMatrix = parentMatrix;
			this.gwasParams = gwasParams;
			setEnabled(false);
			putValue(NAME, Text.Operation.gwasInOneGo);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
//				List<String> blackListOPsAL = new ArrayList<String>();
//				blackListOPsAL.add(cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());

				List<String> necessaryOPsAL = new ArrayList<String>();
				necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
				necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
				List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

				MatrixMetadata matrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());

				int choice = Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB, Text.Operation.htmlAffectionFromFile, Text.All.cancel);
				File phenotypeFile = null;
				if (choice == JOptionPane.NO_OPTION) { //BY EXTERNAL PHENOTYPE FILE
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

				//QA BLOCK
				if (gwasParams.isProceed() && missingOPsAL.size() > 0) {
					gwasParams.setProceed(false);
					Dialogs.showWarningDialogue(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
					MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
				}

				//GWAS BLOCK
				if (gwasParams.isProceed()
						&& choice != JOptionPane.CANCEL_OPTION
						&& (gwasParams.isPerformAllelicTests() || gwasParams.isPerformTrendTests())) { //At least one test has been picked
					log.info(Text.All.processing);
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.waitCursor);
					Set<Object> affectionStates = SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId()); //use Sample Info file affection state
					StartGWASpi.mainGUIFrame.setCursor(CursorUtils.defaultCursor);
					if (affectionStates.contains("1") && affectionStates.contains("2")) {
						MultiOperations.doGWASwithAlterPhenotype(parentMatrix.getStudyId(),
								parentMatrix.getMatrixId(),
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

		private Matrix parentMatrix;
		private final Operation currentOP;

		BackAction(Matrix parentMatrix, Operation currentOP) {

			this.parentMatrix = parentMatrix;
			this.currentOP = currentOP;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				if (currentOP != null && currentOP.getParentOperationId() != -1) {
					GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(currentOP.getParentMatrixId(), currentOP.getParentOperationId()));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				} else if (currentOP != null) {
					GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix.getMatrixId()));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				} else {
					GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix.getMatrixId()));
					GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>
}
