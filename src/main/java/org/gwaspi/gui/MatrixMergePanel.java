package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.JTextFieldLimit;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Matrix;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixMergePanel extends JPanel {

	// Variables declaration - do not modify
	private Matrix parentMatrix;
	private List<Object[]> matrixItemsAL;
	private JButton btn_Back;
	private JButton btn_Help;
	private JButton btn_Merge;
	private ButtonGroup mergeMethod;
	private JComboBox cmb_SelectMatrix;
	private JRadioButton rdio_MergeMarkers;
	private JRadioButton rdio_MergeSamples;
	private JRadioButton rdio_MergeAll;
	private JScrollPane scrl_Notes;
	private JTextArea txtA_Notes;
	private JLabel lbl_NewMatrixName;
	private JLabel lbl_SelectMatrix;
	private JPanel pnl_Footer;
	private JPanel pnl_ParentMatrixDesc;
	private JPanel pnl_TrafoMatrixDesc;
	private JPanel pnl_addedMatrix;
	private JScrollPane scrl_ParentMatrixDesc;
	private JScrollPane scroll_TrafoMatrixDescription;
	private JTextArea txtA_NewMatrixDescription;
	private JTextArea txtA_ParentMatrixDesc;
	private JTextField txt_NewMatrixName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixMergePanel(int _matrixId) throws IOException {

		parentMatrix = new Matrix(_matrixId);
		matrixItemsAL = getMatrixItemsAL();

		mergeMethod = new ButtonGroup();
		pnl_ParentMatrixDesc = new JPanel();
		scrl_ParentMatrixDesc = new JScrollPane();
		txtA_ParentMatrixDesc = new JTextArea();
		pnl_addedMatrix = new JPanel();
		cmb_SelectMatrix = new JComboBox();
		lbl_SelectMatrix = new JLabel();
		rdio_MergeMarkers = new JRadioButton();
		rdio_MergeSamples = new JRadioButton();
		rdio_MergeAll = new JRadioButton();
		scrl_Notes = new JScrollPane();
		txtA_Notes = new JTextArea();
		pnl_TrafoMatrixDesc = new JPanel();
		lbl_NewMatrixName = new JLabel();
		txt_NewMatrixName = new JTextField();
		scroll_TrafoMatrixDescription = new JScrollPane();
		txtA_NewMatrixDescription = new JTextArea();
		btn_Merge = new JButton();
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.mergeMatrices, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		pnl_ParentMatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.parentMatrix + " " + parentMatrix.matrixMetadata.getMatrixFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_ParentMatrixDesc.setColumns(20);
		txtA_ParentMatrixDesc.setRows(5);
		txtA_ParentMatrixDesc.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_ParentMatrixDesc.setText(parentMatrix.matrixMetadata.getDescription());
		txtA_ParentMatrixDesc.setEditable(false);
		scrl_ParentMatrixDesc.setViewportView(txtA_ParentMatrixDesc);

		//<editor-fold defaultstate="collapsed" desc="LAYOUT PARENT MATRIX DESC">
		GroupLayout pnl_ParentMatrixDescLayout = new GroupLayout(pnl_ParentMatrixDesc);
		pnl_ParentMatrixDesc.setLayout(pnl_ParentMatrixDescLayout);
		pnl_ParentMatrixDescLayout.setHorizontalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_ParentMatrixDesc, GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ParentMatrixDescLayout.setVerticalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_ParentMatrixDesc, GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
				.addContainerGap()));

		//</editor-fold>

		pnl_addedMatrix.setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.mergeWithMatrix, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", 1, 12))); // NOI18N

		String[] matricesNames = new String[matrixItemsAL.size()];
		for (int i = 0; i < matrixItemsAL.size(); i++) {
			Object[] matrixItem = matrixItemsAL.get(i);
			matricesNames[i] = matrixItem[1].toString();
		}
		cmb_SelectMatrix.setModel(new DefaultComboBoxModel(matricesNames));


		lbl_SelectMatrix.setText(Text.Trafo.selectMatrix);

		rdio_MergeMarkers.setText(Text.Trafo.mergeMarkersOnly);
		rdio_MergeMarkers.setToolTipText(Text.Trafo.mergeMethodMarkerJoin);
		mergeMethod.add(rdio_MergeMarkers);

		rdio_MergeSamples.setText(Text.Trafo.mergeSamplesOnly);
		rdio_MergeMarkers.setToolTipText(Text.Trafo.mergeMethodSampleJoin);
		mergeMethod.add(rdio_MergeSamples);

		rdio_MergeAll.setText(Text.Trafo.mergeAll);
		rdio_MergeMarkers.setToolTipText(Text.Trafo.mergeMethodMergeAll);
		mergeMethod.add(rdio_MergeAll);

		rdio_MergeMarkers.setSelected(true);

		txtA_Notes.setColumns(20);
		txtA_Notes.setRows(5);
		txtA_Notes.setFont(new Font("DejaVu Sans", 0, 12));
		txtA_Notes.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6)); //Top, left, bottom, right
		txtA_Notes.setEditable(false);
		scrl_Notes.setViewportView(txtA_Notes);

		//<editor-fold defaultstate="collapsed" desc="LAYOUT ADD MATRIX">
		GroupLayout pnl_addedMatrixLayout = new GroupLayout(pnl_addedMatrix);
		pnl_addedMatrix.setLayout(pnl_addedMatrixLayout);
		pnl_addedMatrixLayout.setHorizontalGroup(
				pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addComponent(lbl_SelectMatrix)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cmb_SelectMatrix, 0, 690, Short.MAX_VALUE))
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(rdio_MergeMarkers)
				.addComponent(rdio_MergeSamples)
				.addComponent(rdio_MergeAll))
				.addGap(21, 21, 21)
				.addComponent(scrl_Notes, GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)))
				.addContainerGap()));
		pnl_addedMatrixLayout.setVerticalGroup(
				pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(cmb_SelectMatrix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_SelectMatrix))
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addGap(12, 12, 12)
				.addComponent(rdio_MergeMarkers, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(rdio_MergeSamples)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(rdio_MergeAll))
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_Notes)))
				.addGap(15, 15, 15)));
		//</editor-fold>

		pnl_TrafoMatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.trafoMatrixDetails, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
		txt_NewMatrixName.setDocument(new JTextFieldLimit(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new JTextFieldLimit(1999));
		txtA_NewMatrixDescription.setText(Text.All.optional);
		txtA_NewMatrixDescription.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
							txtA_NewMatrixDescription.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_NewMatrixDescription.select(0, 0);
					}
				});
			}
		});
		scroll_TrafoMatrixDescription.setViewportView(txtA_NewMatrixDescription);
		btn_Merge.setAction(new MergeAction(parentMatrix, txtA_NewMatrixDescription, txt_NewMatrixName, matrixItemsAL, cmb_SelectMatrix, rdio_MergeMarkers, rdio_MergeSamples, rdio_MergeAll));

		//<editor-fold defaultstate="collapsed" desc="LAYOUT NEW MATRIX DESC">
		GroupLayout pnl_TrafoMatrixDescLayout = new GroupLayout(pnl_TrafoMatrixDesc);
		pnl_TrafoMatrixDesc.setLayout(pnl_TrafoMatrixDescLayout);
		pnl_TrafoMatrixDescLayout.setHorizontalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addComponent(lbl_NewMatrixName)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(txt_NewMatrixName, GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE))
				.addComponent(scroll_TrafoMatrixDescription, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
				.addComponent(btn_Merge, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_TrafoMatrixDescLayout.setVerticalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_TrafoMatrixDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Merge, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

		btn_Back.setAction(new BackAction(parentMatrix));
		btn_Help.setAction(new HelpAction());

		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 600, Short.MAX_VALUE)
				.addComponent(btn_Help)
				.addContainerGap()));

		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_TrafoMatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(pnl_ParentMatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(14, 14, 14))
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_addedMatrix, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(14, 14, 14))
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()))));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ParentMatrixDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_addedMatrix, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_TrafoMatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>
	}

	//<editor-fold defaultstate="collapsed" desc="MERGE">
	private static class MergeAction extends AbstractAction {

		private Matrix parentMatrix;
		private JTextArea newMatrixDescription;
		private JTextField newMatrixName;
		private List<Object[]> matrixItemsAL;
		private JComboBox selectMatrix;
		private JRadioButton mergeMarkers;
		private JRadioButton mergeSamples;
		private JRadioButton mergeAll;

		MergeAction(
				Matrix parentMatrix,
				JTextArea newMatrixDescription,
				JTextField newMatrixName,
				List<Object[]> matrixItemsAL,
				JComboBox selectMatrix,
				JRadioButton mergeMarkers,
				JRadioButton mergeSamples,
				JRadioButton mergeAll)
		{
			this.parentMatrix = parentMatrix;
			this.newMatrixDescription = newMatrixDescription;
			this.newMatrixName = newMatrixName;
			this.matrixItemsAL = matrixItemsAL;
			this.selectMatrix = selectMatrix;
			this.mergeMarkers = mergeMarkers;
			this.mergeSamples = mergeSamples;
			this.mergeAll = mergeAll;
			putValue(NAME, Text.Trafo.merge);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int addMatrixId = (Integer) matrixItemsAL.get(selectMatrix.getSelectedIndex())[0];

				MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());
				MatrixMetadata addMatrixMetadata = new MatrixMetadata(addMatrixId);
				if (parentMatrixMetadata.getGenotypeEncoding().equals(addMatrixMetadata.getGenotypeEncoding())) {
					if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString())) {
						// UNKOWN ENCODING, PROBABLY NOT A GOOD IDEA TO PROCEED
						Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncUnknown);
					} else {
						// ALL GOOD: MERGE!
						org.gwaspi.gui.ProcessTab.showTab();

						String description = newMatrixDescription.getText();
						if (description.equals(Text.All.optional)) {
							description = "";
						}

						if (mergeMarkers.isSelected()) {
							MultiOperations.doMergeMatrixAddMarkers(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description);
						}

						if (mergeSamples.isSelected()) {
							MultiOperations.doMergeMatrixAddSamples(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description);
						}

						if (mergeAll.isSelected()) {
							MultiOperations.doMergeMatrixAll(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description);
						}
					}
				} else { // GENOTYPE ENCODING IS NOT EQUAL!! CAN'T PERFORM MERGER
					Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncMismatch);
				}
			} catch (IOException ex) {
				Logger.getLogger(MatrixMergePanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private static class BackAction extends AbstractAction {

		private Matrix parentMatrix;

		BackAction(Matrix parentMatrix) {

			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
				GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
			} catch (IOException ex) {
				Logger.getLogger(BackAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static class HelpAction extends AbstractAction {

		HelpAction() {

			putValue(NAME, Text.Help.help);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixMerge);
			} catch (IOException ex) {
				Logger.getLogger(HelpAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private String checkNewMatrixData() {

		String study_name = txt_NewMatrixName.getText().trim();
		if (!study_name.isEmpty()) {
			lbl_NewMatrixName.setForeground(Color.black);
		} else {
			lbl_NewMatrixName.setForeground(Color.red);
		}

		return study_name;
	}

	public static List<Object[]> getMatrixItemsAL() throws IOException {

		List<Object[]> resultAL = new ArrayList<Object[]>();

		List<Map<String, Object>> rsMatrices = MatricesList.getAllMatricesList();
		int rowcount = rsMatrices.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				//PREVENT PHANTOM-DB READS EXCEPTIONS
				if (!rsMatrices.isEmpty() && rsMatrices.get(i).size() == org.gwaspi.constants.cDBMatrix.T_CREATE_MATRICES.length) {
					int currentMatrixId = (Integer) rsMatrices.get(i).get(org.gwaspi.constants.cDBMatrix.f_ID);
					Matrix currentMatrix = new Matrix(currentMatrixId);
					StringBuilder sb = new StringBuilder();
					sb.append("SID: ");
					sb.append(currentMatrix.getStudyId());
					sb.append(" - MX: ");
					sb.append(currentMatrix.getMatrixId());
					sb.append(" - ");
					sb.append(currentMatrix.matrixMetadata.getMatrixFriendlyName());

					Object[] matrixItem = new Object[2];
					matrixItem[0] = currentMatrix.getMatrixId();
					matrixItem[1] = sb.toString();

					resultAL.add(matrixItem);
				}
			}
		}
		return resultAL;
	}
	//</editor-fold>
}
