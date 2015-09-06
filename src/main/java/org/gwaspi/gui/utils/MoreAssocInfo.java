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

package org.gwaspi.gui.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.operations.GWASinOneGOParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreAssocInfo extends JFrame {

	private final Logger log = LoggerFactory.getLogger(MoreAssocInfo.class);

	// Variables declaration - do not modify
	private JRadioButton rdioB_1;
	private JRadioButton rdioB_2;
	private JTextField txtF_1;
	private JTextField txtF_2;
	private final GWASinOneGOParams gwasParams;
	private JDialog dialog;
	// End of variables declaration

	public MoreAssocInfo(final GWASinOneGOParams gwasParams) {

		this.gwasParams = gwasParams;
	}

	public GWASinOneGOParams showMoreInfo() {
		gwasParams.setProceed(false);
		// Create a modal dialog
		dialog = new JDialog((JFrame) null, Text.Operation.gwasInOneGo, true);

		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		dialog.setLocation(screenWidth / 4, screenHeight / 4);

		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container myPane = dialog.getContentPane();
		myPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setConstraints(c, 0, 0, GridBagConstraints.CENTER);
		myPane.add(getQuestionsPanel(), c);
		setConstraints(c, 0, 1, GridBagConstraints.CENTER);
		myPane.add(createFooterPanel(new GoAction(), new CancelAction()), c);
		dialog.pack();
		dialog.setVisible(true);

		return gwasParams;
	}

	private JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(GWASpiExplorerPanel.createRegularTitledBorder("A few questions..."));

		rdioB_1 = new JRadioButton();
		final JLabel lbl_1 = new JLabel();
		rdioB_2 = new JRadioButton();
		txtF_1 = new JTextField();
		txtF_1.setInputVerifier(new DoubleInputVerifier());
		txtF_2 = new JTextField();
		txtF_2.setInputVerifier(new DoubleInputVerifier());
		final JTextField txtF_3 = new JTextField();
		txtF_3.setInputVerifier(new DoubleInputVerifier());
		final ButtonGroup rdiogrp_HW = new ButtonGroup();

		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;

		//<editor-fold defaultstate="expanded" desc="FORMAT INDEPENDENT">
		rdioB_1.setSelected(true);
		rdiogrp_HW.add(rdioB_1);
		rdioB_1.setText(Text.Operation.discardMarkerHWCalc1);
		lbl_1.setText(Text.Operation.discardMarkerHWCalc2);
		rdiogrp_HW.add(rdioB_2);
		rdioB_2.setText(Text.Operation.discardMarkerHWFree);
		txtF_2.setText("0.0000005");

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_1, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(lbl_1, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(rdioB_2, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_2, c);
		rowNb++;
		//</editor-fold>

		pnl_Questions.setVisible(true);

		return pnl_Questions;
	}

	static JPanel createFooterPanel(final Action goAction, final Action cancelAction) {

		JPanel pnl_Footer = new JPanel(new GridBagLayout());

		final JButton btn_Go = new JButton();
		final JButton btn_Help = new JButton();
		final JButton btn_Cancel = new JButton();

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.GWASinOneGo));

		btn_Go.setAction(goAction);

		btn_Cancel.setAction(cancelAction);

		GridBagConstraints c = new GridBagConstraints();
		setConstraints(c, 0, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Cancel, c);
		setConstraints(c, 1, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setConstraints(c, 2, 0, GridBagConstraints.LINE_START);
		pnl_Footer.add(btn_Help, c);
		setConstraints(c, 3, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(new JLabel("    "), c);
		setConstraints(c, 4, 0, GridBagConstraints.LINE_END);
		pnl_Footer.add(btn_Go, c);

		pnl_Footer.setVisible(true);

		return pnl_Footer;
	}

	private class GoAction extends AbstractAction {

		GoAction() {

			putValue(NAME, Text.All.go);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (!txtF_2.getText().isEmpty()) {
				try {
					gwasParams.setDiscardMarkerHWCalc(rdioB_1.isSelected());
					gwasParams.setDiscardMarkerHWFree(rdioB_2.isSelected());
					gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(txtF_2.getText()));
					gwasParams.setProceed(true);
				} catch (NumberFormatException ex) {
					log.warn(null, ex);
				}
				dialog.dispose();
			} else {
				gwasParams.setProceed(false);
			}
		}
	}

	private class CancelAction extends AbstractAction {

		CancelAction() {

			putValue(NAME, Text.All.cancel);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			dialog.setVisible(false);
		}
	}

	private static void setConstraints(
			GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor)
	{
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}
}
