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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
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
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.operations.genotypesflipper.MatrixGenotypesFlipperParams;
import org.gwaspi.operations.genotypestranslator.MatrixGenotypesTranslatorParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_TranslateMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixTrafoPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixTrafoPanel.class);

	// Variables declaration - do not modify
	private final MatrixKey parentMatrixKey;
	private final JButton btn_1_1;
	private final JButton btn_1_2;
	private final JButton btn_2_1;
	private final JButton btn_Back;
	private final JButton btn_Help;
	private final JLabel lbl_NewMatrixName;
	private final JPanel pnl_ButtonsContainer;
	private final JPanel pnl_ButtonsSpacer;
	private final JPanel pnl_Buttons;
	private final JPanel pnl_Footer;
	private final JPanel pnl_ParentMatrixDesc;
	private final JPanel pnl_TrafoMatrixDesc;
	private final JScrollPane scrl_ParentMatrixDesc;
	private final JScrollPane scroll_TrafoMatrixDescription;
	private final JTextArea txtA_NewMatrixDescription;
	private final JTextArea txtA_ParentMatrixDesc;
	private final JTextField txt_NewMatrixName;
	// End of variables declaration

	public MatrixTrafoPanel(MatrixKey parentMatrixKey) throws IOException {

		this.parentMatrixKey = parentMatrixKey;
		MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);

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

		pnl_ParentMatrixDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.parentMatrix + " " + parentMatrixMetadata.getFriendlyName(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
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

		//<editor-fold defaultstate="expanded" desc="LAYOUT BUTTONS">
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

		btn_Back.setAction(new MatrixAnalysePanel.BackAction(new DataSetKey(parentMatrixKey)));
		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixTranslate));

		//<editor-fold defaultstate="expanded" desc="FOOTER">
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

	//<editor-fold defaultstate="expanded" desc="TRAFO">
	private class TranslateAB12ToACGTAction extends AbstractAction { // FIXME make static

		TranslateAB12ToACGTAction() throws IOException {

			putValue(NAME, Text.Trafo.htmlTranslate1);
			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);
			GenotypeEncoding genotypeEncoding = parentMatrixMetadata.getGenotypeEncoding();
			final boolean sourceGTEncodingIsABor12
					= (genotypeEncoding.equals(GenotypeEncoding.AB0)
					|| genotypeEncoding.equals(GenotypeEncoding.O12));
			setEnabled(sourceGTEncodingIsABor12);
			if (!sourceGTEncodingIsABor12) {
				putValue(SHORT_DESCRIPTION,
						"can not translate AB/12 -> ACGT: the source is "
						+ genotypeEncoding.toString() + " encoded");
			}
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			String newMatrixName = checkNewMatrixData();
			if (!newMatrixName.isEmpty()) {
//				try {
					String description = txtA_NewMatrixDescription.getText();
					if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
						description = "";
					}

					final MatrixGenotypesTranslatorParams params = new MatrixGenotypesTranslatorParams(
							new DataSetKey(parentMatrixKey),
							description,
							newMatrixName);
//					DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey);
//					MatrixOperation validationMatrixOperation = new MatrixTranslator(
//								dataSetSource,
//								null);
//
//					if (validationMatrixOperation.isValid()) {
						final CommonRunnable translateTask = new Threaded_TranslateMatrix(params);
						MultiOperations.queueTask(translateTask);
//					} else {
//						Dialogs.showWarningDialogue(validationMatrixOperation.getProblemDescription());
//					}
//				} catch (IOException ex) {
//					log.error(null, ex);
//				}
			} else {
				Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
			}
		}
	}

	private class Translate1234ToACGTAction extends TranslateAB12ToACGTAction { // FIXME make static

		Translate1234ToACGTAction() throws IOException {

			putValue(NAME, Text.Trafo.htmlTranslate2);
			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);
			GenotypeEncoding genotypeEncoding = parentMatrixMetadata.getGenotypeEncoding();
			final boolean sourceGTEncodingIs1234
					= genotypeEncoding.equals(GenotypeEncoding.O1234);
			setEnabled(sourceGTEncodingIs1234);
			if (!sourceGTEncodingIs1234) {
				putValue(SHORT_DESCRIPTION,
						"can not translate 1234 -> ACGT: the source is "
						+ genotypeEncoding.toString() + " encoded");
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
//				try {
					String description = txtA_NewMatrixDescription.getText();
					if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
						description = "";
					}

//					DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey);
//					MatrixGenotypesFlipperParams params = new MatrixGenotypesFlipperParams(
//							new DataSetKey(parentMatrixKey),
//							description,
//							newMatrixName,
//							null);
//					MatrixOperation validationMatrixOperation = new MatrixGenotypesFlipper(params, dataSet);
//
//					if (validationMatrixOperation.isValid()) {
						final File flipMarkersFile = Dialogs.selectFilesAndDirectoriesDialog(JOptionPane.OK_OPTION, "Choose a flip-markers file", txtA_NewMatrixDescription);
						if (flipMarkersFile == null) {
							// the user chose "Cancel"
							return;
						}
						final MatrixGenotypesFlipperParams params = new MatrixGenotypesFlipperParams(
							new DataSetKey(parentMatrixKey),
							description,
							newMatrixName,
							flipMarkersFile);
						// HACK use doMatrixOperation instead!
						MultiOperations.doStrandFlipMatrix(params);
//					} else {
//						Dialogs.showWarningDialogue(validationMatrixOperation.getProblemDescription());
//					}
//				} catch (IOException ex) {
//					log.error(null, ex);
//				}
			} else {
				Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
			}
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	private String checkNewMatrixData() {

		String newMatrixName = txt_NewMatrixName.getText().trim();
		if (!newMatrixName.isEmpty()) {
			lbl_NewMatrixName.setForeground(Color.black);
		} else {
			lbl_NewMatrixName.setForeground(Color.red);
		}

		return newMatrixName;
	}
	//</editor-fold>
}
