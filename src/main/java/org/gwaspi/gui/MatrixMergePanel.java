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
import java.util.ArrayList;
import java.util.List;
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
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.StudyKey;
import org.gwaspi.threadbox.MultiOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixMergePanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixMergePanel.class);

	// Variables declaration - do not modify
	private final MatrixKey parentMatrix;
	private final List<Object[]> matrixItems;
	private final JButton btn_Back;
	private final JButton btn_Help;
	private final JButton btn_Merge;
	private final ButtonGroup mergeMethod;
	private final JComboBox cmb_SelectMatrix;
	private final JRadioButton rdio_MergeMarkers;
	private final JRadioButton rdio_MergeSamples;
	private final JRadioButton rdio_MergeAll;
	private final JScrollPane scrl_Notes;
	private final JTextArea txtA_Notes;
	private final JLabel lbl_NewMatrixName;
	private final JLabel lbl_SelectMatrix;
	private final JPanel pnl_Footer;
	private final JPanel pnl_ParentMatrixDesc;
	private final JPanel pnl_TrafoMatrixDesc;
	private final JPanel pnl_addedMatrix;
	private final JScrollPane scrl_ParentMatrixDesc;
	private final JScrollPane scroll_TrafoMatrixDescription;
	private final JTextArea txtA_NewMatrixDescription;
	private final JTextArea txtA_ParentMatrixDesc;
	private final JTextField txt_NewMatrixName;
	// End of variables declaration

	public MatrixMergePanel(MatrixKey parentMatrixKey) throws IOException {

		parentMatrix = parentMatrixKey;
		MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrix.getMatrixId());
		matrixItems = getMatrixItems(parentMatrixKey.getStudyKey());

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

		pnl_ParentMatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.parentMatrix + " " + parentMatrixMetadata.getMatrixFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_ParentMatrixDesc.setColumns(20);
		txtA_ParentMatrixDesc.setRows(5);
		txtA_ParentMatrixDesc.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_ParentMatrixDesc.setText(parentMatrixMetadata.getDescription());
		txtA_ParentMatrixDesc.setEditable(false);
		scrl_ParentMatrixDesc.setViewportView(txtA_ParentMatrixDesc);

		//<editor-fold defaultstate="expanded" desc="LAYOUT PARENT MATRIX DESC">
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

		String[] matricesNames = new String[matrixItems.size()];
		for (int i = 0; i < matrixItems.size(); i++) {
			Object[] matrixItem = matrixItems.get(i);
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT ADD MATRIX">
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
		txt_NewMatrixName.setDocument(new LimitedLengthDocument(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new LimitedLengthDocument(1999));
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
		btn_Merge.setAction(new MergeAction(parentMatrix, txtA_NewMatrixDescription, txt_NewMatrixName, matrixItems, cmb_SelectMatrix, rdio_MergeMarkers, rdio_MergeSamples, rdio_MergeAll));

		//<editor-fold defaultstate="expanded" desc="LAYOUT NEW MATRIX DESC">
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
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixMerge));

		//<editor-fold defaultstate="expanded" desc="LAYOUT FOOTER">
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
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

	//<editor-fold defaultstate="expanded" desc="MERGE">
	private static class MergeAction extends AbstractAction {

		private final MatrixKey parentMatrix;
		private final JTextArea newMatrixDescription;
		private final JTextField newMatrixName;
		private final List<Object[]> matrixItems;
		private final JComboBox selectMatrix;
		private final JRadioButton mergeMarkers;
		private final JRadioButton mergeSamples;
		private final JRadioButton mergeAll;

		MergeAction(
				MatrixKey parentMatrix,
				JTextArea newMatrixDescription,
				JTextField newMatrixName,
				List<Object[]> matrixItems,
				JComboBox selectMatrix,
				JRadioButton mergeMarkers,
				JRadioButton mergeSamples,
				JRadioButton mergeAll)
		{
			this.parentMatrix = parentMatrix;
			this.newMatrixDescription = newMatrixDescription;
			this.newMatrixName = newMatrixName;
			this.matrixItems = matrixItems;
			this.selectMatrix = selectMatrix;
			this.mergeMarkers = mergeMarkers;
			this.mergeSamples = mergeSamples;
			this.mergeAll = mergeAll;
			putValue(NAME, Text.Trafo.merge);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				int addMatrixId = (Integer) matrixItems.get(selectMatrix.getSelectedIndex())[0];

				MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrix.getMatrixId());
				MatrixMetadata addMatrixMetadata = MatricesList.getMatrixMetadataById(addMatrixId);
				if (parentMatrixMetadata.getGenotypeEncoding().equals(addMatrixMetadata.getGenotypeEncoding())) {
					if (parentMatrixMetadata.getGenotypeEncoding().equals(GenotypeEncoding.UNKNOWN)) {
						// UNKOWN ENCODING, PROBABLY NOT A GOOD IDEA TO PROCEED
						Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncUnknown);
					} else {
						// ALL GOOD: MERGE!
						ProcessTab.getSingleton().showTab();

						String description = newMatrixDescription.getText();
						if (description.equals(Text.All.optional)) {
							description = "";
						}

						if (mergeMarkers.isSelected()) {
							MultiOperations.doMergeMatrix(
									parentMatrix.getStudyKey(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description,
									false);
						}

						if (mergeSamples.isSelected()) {
							MultiOperations.doMergeMatrixAddSamples(
									parentMatrix.getStudyKey(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description);
						}

						if (mergeAll.isSelected()) {
							MultiOperations.doMergeMatrix(
									parentMatrix.getStudyKey(),
									parentMatrix.getMatrixId(),
									addMatrixId,
									newMatrixName.getText(),
									description,
									true);
						}
					}
				} else { // GENOTYPE ENCODING IS NOT EQUAL!! CAN'T PERFORM MERGER
					Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncMismatch);
				}
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	private static class BackAction extends AbstractAction {

		private final MatrixKey parentMatrix;

		BackAction(MatrixKey parentMatrix) {

			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
			}
		}
	}

	/**
	 * @deprecated unused
	 */
	private String checkNewMatrixData() {

		String study_name = txt_NewMatrixName.getText().trim();
		if (!study_name.isEmpty()) {
			lbl_NewMatrixName.setForeground(Color.black);
		} else {
			lbl_NewMatrixName.setForeground(Color.red);
		}

		return study_name;
	}

	private static List<Object[]> getMatrixItems(StudyKey studyKey) throws IOException {

		List<Object[]> result = new ArrayList<Object[]>();

		List<MatrixMetadata> matrices = MatricesList.getMatricesTable(studyKey);
		if (!matrices.isEmpty()) {
			for (int i = matrices.size() - 1; i >= 0; i--) {
				MatrixMetadata currentMatrix = matrices.get(i);
				StringBuilder sb = new StringBuilder();
				sb.append("SID: ");
				sb.append(currentMatrix.getStudyId());
				sb.append(" - MX: ");
				sb.append(currentMatrix.getMatrixId());
				sb.append(" - ");
				sb.append(currentMatrix.getMatrixFriendlyName());

				Object[] matrixItem = new Object[2];
				matrixItem[0] = currentMatrix.getMatrixId();
				matrixItem[1] = sb.toString();

				result.add(matrixItem);
			}
		}
		return result;
	}
	//</editor-fold>
}
