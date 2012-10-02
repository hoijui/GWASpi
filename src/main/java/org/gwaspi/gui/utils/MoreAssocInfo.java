package org.gwaspi.gui.utils;

import org.gwaspi.global.Text;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MoreAssocInfo extends JFrame {

	private final Logger log = LoggerFactory.getLogger(MoreAssocInfo.class);

	// Variables declaration - do not modify
	private JButton btn_Go;
	private JButton btn_Help;
	private JButton btn_Cancel;
	private JRadioButton rdioB_1;
	private JLabel lbl_1;
	private JRadioButton rdioB_2;
	private JTextField txtF_1;
	private JTextField txtF_2;
	private JTextField txtF_3;
	private ButtonGroup rdiogrp_HW;
	private JFrame myFrame = new JFrame("GridBagLayout Test");
	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	private JDialog dialog;
	// End of variables declaration

	public GWASinOneGOParams showMoreInfo() {
		gwasParams.setProceed(false);
		// Create a modal dialog
		dialog = new JDialog(myFrame, Text.Operation.gwasInOneGo, true);

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
		myPane.add(getFooterPanel(), c);
		dialog.pack();
		dialog.setVisible(true);

		return gwasParams;
	}

	private JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		rdioB_1 = new JRadioButton();
		lbl_1 = new JLabel();
		rdioB_2 = new JRadioButton();
		txtF_1 = new JTextField();
		txtF_1.setInputVerifier(new DoubleInputVerifier());
		txtF_2 = new JTextField();
		txtF_2.setInputVerifier(new DoubleInputVerifier());
		txtF_3 = new JTextField();
		txtF_3.setInputVerifier(new DoubleInputVerifier());
		rdiogrp_HW = new ButtonGroup();

		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;

		//<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
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

	private JPanel getFooterPanel() {

		JPanel pnl_Footer = new JPanel(new GridBagLayout());

		btn_Go = new JButton();
		btn_Help = new JButton();
		btn_Cancel = new JButton();

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.GWASinOneGo));

		btn_Go.setAction(new GoAction());

		btn_Cancel.setAction(new CancelAction());

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

	private void setConstraints(GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor)
	{
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}
}
