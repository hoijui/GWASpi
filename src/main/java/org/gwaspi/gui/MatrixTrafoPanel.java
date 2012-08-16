package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.netCDF.operations.MatrixMergeSamples_opt;
import org.gwaspi.threadbox.MultiOperations;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixTrafoPanel extends javax.swing.JPanel {

	// Variables declaration - do not modify
	private org.gwaspi.model.Matrix parentMatrix;
	private javax.swing.JButton btn_1_1;
	private javax.swing.JButton btn_1_2;
	private javax.swing.JButton btn_2_1;
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Help;
	private javax.swing.JLabel lbl_NewMatrixName;
	private javax.swing.JPanel pnl_ButtonsContainer;
	private javax.swing.JPanel pnl_ButtonsSpacer;
	private javax.swing.JPanel pnl_Buttons;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JPanel pnl_ParentMatrixDesc;
	private javax.swing.JPanel pnl_TrafoMatrixDesc;
	private javax.swing.JScrollPane scrl_ParentMatrixDesc;
	private javax.swing.JScrollPane scroll_TrafoMatrixDescription;
	private javax.swing.JTextArea txtA_NewMatrixDescription;
	private javax.swing.JTextArea txtA_ParentMatrixDesc;
	private javax.swing.JTextField txt_NewMatrixName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixTrafoPanel(int _matrixId) throws IOException {

		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);

		pnl_ParentMatrixDesc = new javax.swing.JPanel();
		scrl_ParentMatrixDesc = new javax.swing.JScrollPane();
		txtA_ParentMatrixDesc = new javax.swing.JTextArea();
		pnl_ButtonsContainer = new javax.swing.JPanel();
		pnl_ButtonsSpacer = new javax.swing.JPanel();
		pnl_Buttons = new javax.swing.JPanel();
		btn_1_1 = new javax.swing.JButton();
		btn_1_2 = new javax.swing.JButton();
		btn_2_1 = new javax.swing.JButton();
		pnl_TrafoMatrixDesc = new javax.swing.JPanel();
		lbl_NewMatrixName = new javax.swing.JLabel();
		txt_NewMatrixName = new javax.swing.JTextField();
		scroll_TrafoMatrixDescription = new javax.swing.JScrollPane();
		txtA_NewMatrixDescription = new javax.swing.JTextArea();
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Help = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.transformMatrix, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

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
				.addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ParentMatrixDescLayout.setVerticalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_ParentMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(18, Short.MAX_VALUE)));

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
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE))
				.addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
				.addContainerGap()));
		pnl_TrafoMatrixDescLayout.setVerticalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(scroll_TrafoMatrixDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		pnl_ButtonsContainer.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		pnl_ButtonsContainer.setPreferredSize(new java.awt.Dimension(240, 289));
		btn_1_1.setText(Text.Trafo.htmlTranslate1);
		btn_1_1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionTranslateAB12ToACGT(evt);
			}
		});

		btn_1_2.setText(Text.Trafo.htmlTranslate2);
		btn_1_2.setEnabled(true);
		btn_1_2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionTranslate1234ToACGT(evt);
			}
		});

		btn_2_1.setText(Text.Trafo.flipStrand);
		btn_2_1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionMatrixStrandFlip(evt);
			}
		});



		//<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
		javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
		pnl_Buttons.setLayout(pnl_ButtonsLayout);
		pnl_ButtonsLayout.setHorizontalGroup(
				pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
				.addComponent(btn_2_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(btn_1_1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
				.addGap(166, 166, 166)
				.addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(75, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btn_1_1, btn_1_2});

		pnl_ButtonsLayout.setVerticalGroup(
				pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_2_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));


		pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{btn_1_1, btn_1_2});


		javax.swing.GroupLayout pnl_ButtonsContainerLayout = new javax.swing.GroupLayout(pnl_ButtonsContainer);
		pnl_ButtonsContainer.setLayout(pnl_ButtonsContainerLayout);
		pnl_ButtonsContainerLayout.setHorizontalGroup(
				pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ButtonsSpacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ButtonsContainerLayout.setVerticalGroup(
				pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(pnl_ButtonsSpacer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Buttons, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		//</editor-fold>


		btn_Back.setText("Back");
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					actionGoBack(evt);
				} catch (IOException ex) {
					Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		btn_Help.setText("Help");
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp(evt);
			}
		});


		//<editor-fold defaultstate="collapsed" desc="FOOTER">
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
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_ButtonsContainer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
				.addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(14, 14, 14)))));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ParentMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_TrafoMatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_ButtonsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(77, Short.MAX_VALUE)));
		//</editor-fold>
	}

	//<editor-fold defaultstate="collapsed" desc="TRAFO">
	private void actionTranslateAB12ToACGT(java.awt.event.ActionEvent evt) {
		String newMatrixName = checkNewMatrixData();
		if (!newMatrixName.isEmpty()) {
			try {

				MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());
				String description = txtA_NewMatrixDescription.getText();
				if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
					description = "";
				}

				if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.AB0.toString())
						|| parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O12.toString())) {
					if (parentMatrixMetadata.getHasDictionray() == 1) {

						MultiOperations.doTranslateAB12ToACGT(parentMatrix.getStudyId(),
								parentMatrix.getMatrixId(),
								cNetCDF.Defaults.GenotypeEncoding.AB0, //No matter if AB or 12, works the same here
								newMatrixName,
								description);



					} else {
						org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnNoDictionary);
					}
				} else {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnNotAB12);
				}
			} catch (IOException ex) {
				Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
		}

	}

	private void actionTranslate1234ToACGT(java.awt.event.ActionEvent evt) {
		String newMatrixName = checkNewMatrixData();
		if (!newMatrixName.isEmpty()) {
			try {
				MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());

				String description = txtA_NewMatrixDescription.getText();
				if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
					description = "";
				}

				if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O1234.toString())) {


					MultiOperations.doTranslateAB12ToACGT(parentMatrix.getStudyId(),
							parentMatrix.getMatrixId(),
							cNetCDF.Defaults.GenotypeEncoding.O1234,
							newMatrixName,
							description);

				} else {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnNot1234);
				}
			} catch (IOException ex) {
				Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
		}
	}

	private void actionMatrixStrandFlip(ActionEvent evt) {
		String newMatrixName = checkNewMatrixData();
		if (!newMatrixName.isEmpty()) {
			try {
				MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());

				String description = txtA_NewMatrixDescription.getText();
				if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
					description = "";
				}

				if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O1234.toString())
						|| parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.ACGT0.toString())) {

					File flipMarkersFile = org.gwaspi.gui.utils.Dialogs.selectFilesAndDirertoriesDialogue(JOptionPane.OK_OPTION);
					MultiOperations.doStrandFlipMatrix(parentMatrix.getStudyId(),
							parentMatrix.getMatrixId(),
							org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET,
							flipMarkersFile,
							newMatrixName,
							description);

				} else {
					org.gwaspi.gui.utils.Dialogs.showWarningDialogue(Text.Trafo.warnNotACGTor1234);
				}



			} catch (Exception ex) {
				Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
		}
	}

	private void actionForceStrand(ActionEvent evt) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void actionMergeMarkers(java.awt.event.ActionEvent evt) {
		org.gwaspi.gui.ProcessTab.showTab();
	}

	//</editor-fold>
	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private void actionGoBack(java.awt.event.ActionEvent evt) throws IOException {
		org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
		org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
	}

	private void actionHelp(java.awt.event.ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixTranslate);
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
	//</editor-fold>
}
