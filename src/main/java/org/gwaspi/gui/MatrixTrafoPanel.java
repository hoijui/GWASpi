package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.JTextFieldLimit;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.gwaspi.model.Matrix;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixTrafoPanel extends JPanel {

	// Variables declaration - do not modify
	private Matrix parentMatrix;
	private JButton btn_1_1;
	private JButton btn_1_2;
	private JButton btn_2_1;
	private JButton btn_Back;
	private JButton btn_Help;
	private JLabel lbl_NewMatrixName;
	private JPanel pnl_ButtonsContainer;
	private JPanel pnl_ButtonsSpacer;
	private JPanel pnl_Buttons;
	private JPanel pnl_Footer;
	private JPanel pnl_ParentMatrixDesc;
	private JPanel pnl_TrafoMatrixDesc;
	private JScrollPane scrl_ParentMatrixDesc;
	private JScrollPane scroll_TrafoMatrixDescription;
	private JTextArea txtA_NewMatrixDescription;
	private JTextArea txtA_ParentMatrixDesc;
	private JTextField txt_NewMatrixName;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	public MatrixTrafoPanel(int _matrixId) throws IOException {

		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);

		pnl_ParentMatrixDesc = new JPanel();
		scrl_ParentMatrixDesc = new JScrollPane();
		txtA_ParentMatrixDesc = new JTextArea();
		pnl_ButtonsContainer = new JPanel();
		pnl_ButtonsSpacer = new JPanel();
		pnl_Buttons = new JPanel();
		btn_1_1 = new JButton();
		btn_1_2 = new JButton();
		btn_2_1 = new JButton();
		pnl_TrafoMatrixDesc = new JPanel();
		lbl_NewMatrixName = new JLabel();
		txt_NewMatrixName = new JTextField();
		scroll_TrafoMatrixDescription = new JScrollPane();
		txtA_NewMatrixDescription = new JTextArea();
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Help = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.transformMatrix, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

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
				.addComponent(scrl_ParentMatrixDesc, GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ParentMatrixDescLayout.setVerticalGroup(
				pnl_ParentMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ParentMatrixDescLayout.createSequentialGroup()
				.addComponent(scrl_ParentMatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(18, Short.MAX_VALUE)));

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
				.addComponent(txt_NewMatrixName, GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE))
				.addComponent(scroll_TrafoMatrixDescription, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
				.addContainerGap()));
		pnl_TrafoMatrixDescLayout.setVerticalGroup(
				pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_TrafoMatrixDescLayout.createSequentialGroup()
				.addGroup(pnl_TrafoMatrixDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(scroll_TrafoMatrixDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

		pnl_ButtonsContainer.setBorder(BorderFactory.createEtchedBorder());
		pnl_ButtonsContainer.setPreferredSize(new Dimension(240, 289));
		btn_1_1.setAction(new TranslateAB12ToACGTAction());

		btn_1_2.setAction(new Translate1234ToACGTAction());

		btn_2_1.setAction(new MatrixStrandFlipAction());

		//<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
		GroupLayout pnl_ButtonsLayout = new GroupLayout(pnl_Buttons);
		pnl_Buttons.setLayout(pnl_ButtonsLayout);
		pnl_ButtonsLayout.setHorizontalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				.addComponent(btn_2_1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(btn_1_1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE))
				.addGap(166, 166, 166)
				.addComponent(btn_1_2, GroupLayout.PREFERRED_SIZE, 252, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(75, Short.MAX_VALUE)));

		pnl_ButtonsLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_1_1, btn_1_2});

		pnl_ButtonsLayout.setVerticalGroup(
				pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_1_1, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_1_2, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_2_1, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pnl_ButtonsLayout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_1_1, btn_1_2});

		GroupLayout pnl_ButtonsContainerLayout = new GroupLayout(pnl_ButtonsContainer);
		pnl_ButtonsContainer.setLayout(pnl_ButtonsContainerLayout);
		pnl_ButtonsContainerLayout.setHorizontalGroup(
				pnl_ButtonsContainerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ButtonsSpacer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Buttons, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap()));
		pnl_ButtonsContainerLayout.setVerticalGroup(
				pnl_ButtonsContainerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_ButtonsContainerLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_ButtonsContainerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(pnl_ButtonsSpacer, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_Buttons, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));
		//</editor-fold>

		btn_Back.setAction(new BackAction(parentMatrix));
		btn_Help.setAction(new HelpAction());

		//<editor-fold defaultstate="collapsed" desc="FOOTER">
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
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_ButtonsContainer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
				.addComponent(pnl_ParentMatrixDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_TrafoMatrixDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(14, 14, 14)))));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_ParentMatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_TrafoMatrixDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_ButtonsContainer, GroupLayout.PREFERRED_SIZE, 157, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(77, Short.MAX_VALUE)));
		//</editor-fold>
	}

	//<editor-fold defaultstate="collapsed" desc="TRAFO">
	private class TranslateAB12ToACGTAction extends AbstractAction { // FIXME make static

		TranslateAB12ToACGTAction() {

			putValue(NAME, Text.Trafo.htmlTranslate1);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			String newMatrixName = checkNewMatrixData();
			if (!newMatrixName.isEmpty()) {
				try {
					MatrixMetadata parentMatrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());
					String description = txtA_NewMatrixDescription.getText();
					if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
						description = "";
					}

					if (parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.AB0.toString())
							|| parentMatrixMetadata.getGenotypeEncoding().equals(cNetCDF.Defaults.GenotypeEncoding.O12.toString()))
					{
						if (parentMatrixMetadata.getHasDictionray() == 1) {

							MultiOperations.doTranslateAB12ToACGT(parentMatrix.getStudyId(),
									parentMatrix.getMatrixId(),
									cNetCDF.Defaults.GenotypeEncoding.AB0, //No matter if AB or 12, works the same here
									newMatrixName,
									description);
						} else {
							Dialogs.showWarningDialogue(Text.Trafo.warnNoDictionary);
						}
					} else {
						Dialogs.showWarningDialogue(Text.Trafo.warnNotAB12);
					}
				} catch (IOException ex) {
					Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
			}
		}
	}

	private class Translate1234ToACGTAction extends AbstractAction { // FIXME make static

		Translate1234ToACGTAction() {

			putValue(NAME, Text.Trafo.htmlTranslate2);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
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
						Dialogs.showWarningDialogue(Text.Trafo.warnNot1234);
					}
				} catch (IOException ex) {
					Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
			}
		}
	}

	private class MatrixStrandFlipAction extends AbstractAction { // FIXME make static

		MatrixStrandFlipAction() {

			putValue(NAME, Text.Trafo.flipStrand);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
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

						File flipMarkersFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION);
						MultiOperations.doStrandFlipMatrix(parentMatrix.getStudyId(),
								parentMatrix.getMatrixId(),
								org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET,
								flipMarkersFile,
								newMatrixName,
								description);
					} else {
						Dialogs.showWarningDialogue(Text.Trafo.warnNotACGTor1234);
					}
				} catch (Exception ex) {
					Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
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
				GWASpiExplorerPanel.scrl_Content.setViewportView(GWASpiExplorerPanel.pnl_Content);
			} catch (IOException ex) {
				Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
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
				URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixTranslate);
			} catch (IOException ex) {
				Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
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
	//</editor-fold>
}
