package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
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
public class MatrixMergePanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private org.gwaspi.model.Matrix parentMatrix;
	private ArrayList<Object[]> matrixItemsAL;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JButton btn_Merge;
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.JComboBox cmb_SelectMatrix;
	private javax.swing.JRadioButton rdio_MergeMarkers;
	private javax.swing.JRadioButton rdio_MergeSamples;
	private javax.swing.JRadioButton rdio_MergeAll;
	private javax.swing.JScrollPane scrl_Notes;
	private javax.swing.JTextArea txtA_Notes;
	private javax.swing.JLabel lbl_NewMatrixName;
	private javax.swing.JLabel lbl_SelectMatrix;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JPanel pnl_ParentMatrixDesc;
	private javax.swing.JPanel pnl_TrafoMatrixDesc;
	private javax.swing.JPanel pnl_addedMatrix;
	private javax.swing.JScrollPane scrl_ParentMatrixDesc;
	private javax.swing.JScrollPane scroll_TrafoMatrixDescription;
	private javax.swing.JTextArea txtA_NewMatrixDescription;
	private javax.swing.JTextArea txtA_ParentMatrixDesc;
	private javax.swing.JTextField txt_NewMatrixName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixMergePanel(int _matrixId) throws IOException {

		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);
		matrixItemsAL = getMatrixItemsAL();

		buttonGroup1 = new javax.swing.ButtonGroup();
		pnl_ParentMatrixDesc = new javax.swing.JPanel();
		scrl_ParentMatrixDesc = new javax.swing.JScrollPane();
		txtA_ParentMatrixDesc = new javax.swing.JTextArea();
		pnl_addedMatrix = new javax.swing.JPanel();
		cmb_SelectMatrix = new javax.swing.JComboBox();
		lbl_SelectMatrix = new javax.swing.JLabel();
		rdio_MergeMarkers = new javax.swing.JRadioButton();
		rdio_MergeSamples = new javax.swing.JRadioButton();
		rdio_MergeAll = new javax.swing.JRadioButton();
		scrl_Notes = new javax.swing.JScrollPane();
		txtA_Notes = new javax.swing.JTextArea();
		pnl_TrafoMatrixDesc = new javax.swing.JPanel();
		lbl_NewMatrixName = new javax.swing.JLabel();
		txt_NewMatrixName = new javax.swing.JTextField();
		scroll_TrafoMatrixDescription = new javax.swing.JScrollPane();
		txtA_NewMatrixDescription = new javax.swing.JTextArea();
		btn_Merge = new javax.swing.JButton();
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.mergeMatrices, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		pnl_ParentMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.parentMatrix + " " + parentMatrix.matrixMetadata.getMatrixFriendlyName(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		txtA_ParentMatrixDesc.setColumns(20);
		txtA_ParentMatrixDesc.setRows(5);
		txtA_ParentMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.All.description));
		txtA_ParentMatrixDesc.setText(parentMatrix.matrixMetadata.getDescription());
		txtA_ParentMatrixDesc.setEditable(false);
		scrl_ParentMatrixDesc.setViewportView(txtA_ParentMatrixDesc);


		//<editor-fold defaultstate="collapsed" desc="LAYOUT PARENT MATRIX DESC">
		javax.swing.GroupLayout pnl_ParentMatrixDescLayout = new javax.swing.GroupLayout(pnl_ParentMatrixDesc);
		pnl_ParentMatrixDesc.setLayout(pnl_ParentMatrixDescLayout);
		pnl_ParentMatrixDescLayout.setHorizontalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ParentMatrixDescLayout.setVerticalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
				.addContainerGap()));

		//</editor-fold>


		pnl_addedMatrix.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.mergeWithMatrix, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12))); // NOI18N

		String[] matricesNames = new String[matrixItemsAL.size()];
		for (int i = 0; i < matrixItemsAL.size(); i++) {
			Object[] matrixItem = matrixItemsAL.get(i);
			matricesNames[i] = matrixItem[1].toString();
		}
		cmb_SelectMatrix.setModel(new javax.swing.DefaultComboBoxModel(matricesNames));


		lbl_SelectMatrix.setText(Text.Trafo.selectMatrix);

		rdio_MergeMarkers.setText(Text.Trafo.mergeMarkersOnly);
		rdio_MergeMarkers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rdio_MergeMarkers.setForeground(Color.black);
				rdio_MergeSamples.setForeground(Color.black);
				rdio_MergeAll.setForeground(Color.black);
				txtA_Notes.setText(Text.Trafo.mergeMethodMarkerJoin);
			}
		});
		buttonGroup1.add(rdio_MergeMarkers);

		rdio_MergeSamples.setText(Text.Trafo.mergeSamplesOnly);
		rdio_MergeSamples.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rdio_MergeMarkers.setForeground(Color.black);
				rdio_MergeSamples.setForeground(Color.black);
				rdio_MergeAll.setForeground(Color.black);
				txtA_Notes.setText(Text.Trafo.mergeMethodSampleJoin);
			}
		});
		buttonGroup1.add(rdio_MergeSamples);

		rdio_MergeAll.setText(Text.Trafo.mergeAll);
		rdio_MergeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rdio_MergeMarkers.setForeground(Color.black);
				rdio_MergeSamples.setForeground(Color.black);
				rdio_MergeAll.setForeground(Color.black);
				txtA_Notes.setText(Text.Trafo.mergeMethodMergeAll);
			}
		});
		buttonGroup1.add(rdio_MergeAll);

		txtA_Notes.setColumns(20);
		txtA_Notes.setRows(5);
		txtA_Notes.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
		txtA_Notes.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6)); //Top, left, bottom, right
		txtA_Notes.setEditable(false);
		scrl_Notes.setViewportView(txtA_Notes);


		//<editor-fold defaultstate="collapsed" desc="LAYOUT ADD MATRIX">
		javax.swing.GroupLayout pnl_addedMatrixLayout = new javax.swing.GroupLayout(pnl_addedMatrix);
		pnl_addedMatrix.setLayout(pnl_addedMatrixLayout);
		pnl_addedMatrixLayout.setHorizontalGroup(
				pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addComponent(lbl_SelectMatrix)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(cmb_SelectMatrix, 0, 690, Short.MAX_VALUE))
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(rdio_MergeMarkers)
				.addComponent(rdio_MergeSamples)
				.addComponent(rdio_MergeAll))
				.addGap(21, 21, 21)
				.addComponent(scrl_Notes, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)))
				.addContainerGap()));
		pnl_addedMatrixLayout.setVerticalGroup(
				pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(cmb_SelectMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_SelectMatrix))
				.addGroup(pnl_addedMatrixLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addGap(12, 12, 12)
				.addComponent(rdio_MergeMarkers, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(rdio_MergeSamples)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(rdio_MergeAll))
				.addGroup(pnl_addedMatrixLayout.createSequentialGroup()
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_Notes)))
				.addGap(15, 15, 15)));
		//</editor-fold>


		pnl_TrafoMatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.trafoMatrixDetails, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
		txt_NewMatrixName.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(1999));
		txtA_NewMatrixDescription.setText(Text.All.optional);
		txtA_NewMatrixDescription.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
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
			public void focusLost(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_NewMatrixDescription.select(0, 0);
					}
				});
			}
		});
		scroll_TrafoMatrixDescription.setViewportView(txtA_NewMatrixDescription);
		btn_Merge.setText(Text.Trafo.merge);
		btn_Merge.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (rdio_MergeMarkers.isSelected() || rdio_MergeSamples.isSelected() || rdio_MergeAll.isSelected()) {
					rdio_MergeMarkers.setForeground(Color.black);
					rdio_MergeSamples.setForeground(Color.black);
					rdio_MergeAll.setForeground(Color.black);
					try {
						actionMergeMatrices();
					} catch (IOException ex) {
						Logger.getLogger(MatrixMergePanel.class.getName()).log(Level.SEVERE, null, ex);
					}
				} else {
					rdio_MergeMarkers.setForeground(Color.red);
					rdio_MergeSamples.setForeground(Color.red);
					rdio_MergeAll.setForeground(Color.red);
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnSelectMergeMethod);
				}

			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT NEW MATRIX DESC">
		javax.swing.GroupLayout pnl_TrafoMatrixDescLayout = new javax.swing.GroupLayout(pnl_TrafoMatrixDesc);
		pnl_TrafoMatrixDesc.setLayout(pnl_TrafoMatrixDescLayout);
		pnl_TrafoMatrixDescLayout.setHorizontalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addComponent(lbl_NewMatrixName)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE))
				.addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
				.addComponent(btn_Merge, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		pnl_TrafoMatrixDescLayout.setVerticalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(btn_Merge, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		btn_Back.setText("Back");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionGoBack(evt);
				} catch (IOException ex) {
					Logger.getLogger(MatrixMergePanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp(evt);
			}
		});


		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 600, Short.MAX_VALUE)
				.addComponent(btn_Help)
				.addContainerGap()));


		pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_Back, btn_Help});

		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>



		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(14, 14, 14))
				.addGroup(layout.createSequentialGroup()
				.addComponent(pnl_addedMatrix, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(14, 14, 14))
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_addedMatrix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		//</editor-fold>
	}

	//<editor-fold defaultstate="collapsed" desc="MERGE">
	private void actionMergeMatrices() throws IOException {
		int addMatrixId = (Integer) matrixItemsAL.get(cmb_SelectMatrix.getSelectedIndex())[0];

		MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());
		MatrixMetadata addMatrixMetadata = new MatrixMetadata(addMatrixId);
		if (parentMatrixMetadata.getGenotypeEncoding().equals(addMatrixMetadata.getGenotypeEncoding())) {
			if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.UNKNOWN.toString())) {
				//UNKOWN ENCODING, PROBABLY NOT A GOOD IDEA TO PROCEED
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncUnknown);
			} else {
				//ALL GOOD: MERGE!
				org.gwaspi.gui.ProcessTab.showTab();

				String description = txtA_NewMatrixDescription.getText();
				if (description.equals(Text.All.optional)) {
					description = "";
				}

				if (rdio_MergeMarkers.isSelected()) {
					MultiOperations.doMergeMatrixAddMarkers(parentMatrix.getStudyId(),
							parentMatrix.getMatrixId(),
							addMatrixId,
							txt_NewMatrixName.getText(),
							description);
				}

				if (rdio_MergeSamples.isSelected()) {
					MultiOperations.doMergeMatrixAddSamples(parentMatrix.getStudyId(),
							parentMatrix.getMatrixId(),
							addMatrixId,
							txt_NewMatrixName.getText(),
							description);
				}

				if (rdio_MergeAll.isSelected()) {
					MultiOperations.doMergeMatrixAll(parentMatrix.getStudyId(),
							parentMatrix.getMatrixId(),
							addMatrixId,
							txt_NewMatrixName.getText(),
							description);
				}
			}
		} else {    //GENOTYPE ENCODING IS NOT EQUAL!! CAN'T PERFORM MERGER
			org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnMatrixEncMismatch);
		}

	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private void actionGoBack(java.awt.event.ActionEvent evt) throws IOException {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void actionHelp(java.awt.event.ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixMerge);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
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

	public static ArrayList getMatrixItemsAL() throws IOException {

		ArrayList resultAL = new ArrayList();

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
