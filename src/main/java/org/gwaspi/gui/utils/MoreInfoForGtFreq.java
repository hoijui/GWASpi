package org.gwaspi.gui.utils;

import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MoreInfoForGtFreq extends JFrame {

	// Variables declaration - do not modify
	private static JButton btn_Go;
	private static JButton btn_Help;
	private static JButton btn_Cancel;
	private static JCheckBox chkB_1;
	private static JCheckBox chkB_2;
	private static JCheckBox chkB_SMS;
	private static JCheckBox chkB_SHZ;
	private static JTextField txtF_1;
	private static JTextField txtF_SMS;
	private static JTextField txtF_MHZ;
	private static JTextField txtF_SHZ;
	private static JFrame myFrame = new JFrame("GridBagLayout Test");
	private static JDialog dialog;
	public static GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	// End of variables declaration

	public static GWASinOneGOParams showMoreInfoForQA_Modal() {

		gwasParams.setProceed(false);
		// Create a modal dialog
		dialog = new JDialog(myFrame, "Genotype freq. info", true);

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

	private static JPanel getQuestionsPanel() {

		JPanel pnl_Questions = new JPanel(new GridBagLayout());
		pnl_Questions.setBorder(BorderFactory.createTitledBorder("A few questions..."));

		chkB_1 = new JCheckBox();
		chkB_2 = new JCheckBox();
		chkB_SMS = new JCheckBox();
		chkB_SHZ = new JCheckBox();
		txtF_1 = new JTextField();
		txtF_1.setInputVerifier(new DoubleInputVerifier());
		txtF_SMS = new JTextField();
		txtF_SMS.setInputVerifier(new DoubleInputVerifier());
		txtF_MHZ = new JTextField();
		txtF_MHZ.setInputVerifier(new DoubleInputVerifier());
		txtF_SHZ = new JTextField();
		txtF_SHZ.setInputVerifier(new DoubleInputVerifier());

		GridBagConstraints c = new GridBagConstraints();
		int rowNb = 0;

		//<editor-fold defaultstate="collapsed" desc="FORMAT INDEPENDENT">
		chkB_1.setSelected(true);
		chkB_2.setSelected(true);
		chkB_SMS.setSelected(true);
		chkB_SHZ.setSelected(false);

		chkB_1.setText(Text.Operation.discardMismatch);
		chkB_1.setEnabled(false);

		chkB_2.setText(Text.Operation.discardMarkerMissing);
		txtF_1.setText("0.050");

		chkB_SMS.setText(Text.Operation.discardSampleMissing);
		try {
			txtF_SMS.setText(Config.getConfigValue("CHART_SAMPLEQA_MISSING_THRESHOLD", "0.500"));
		} catch (IOException ex) {
			txtF_SMS.setText("0.050");
		}

		chkB_SHZ.setText(Text.Operation.discardSampleHetzy);
		try {
			txtF_SHZ.setText(Config.getConfigValue("CHART_SAMPLEQA_HETZYG_THRESHOLD", "0.5"));
		} catch (IOException ex) {
			txtF_SHZ.setText("1.000");
		}
		chkB_SHZ.setEnabled(true);

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_1, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(new JLabel(""), c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_2, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_1, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SMS, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SMS, c);
		rowNb++;

		setConstraints(c, 0, rowNb, GridBagConstraints.LINE_START);
		pnl_Questions.add(chkB_SHZ, c);
		setConstraints(c, 1, rowNb, GridBagConstraints.WEST);
		pnl_Questions.add(txtF_SHZ, c);
		rowNb++;
		//</editor-fold>

		pnl_Questions.setVisible(true);

		return pnl_Questions;
	}

	private static JPanel getFooterPanel() {

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

	private static class GoAction extends AbstractAction {

		GoAction() {

			putValue(NAME, Text.All.go);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			if (!txtF_1.getText().isEmpty() && !txtF_SMS.getText().isEmpty()) {
				try {
					gwasParams.setDiscardGTMismatches(chkB_1.isSelected());
					gwasParams.setDiscardMarkerByMisRat(chkB_2.isSelected());
					gwasParams.setDiscardMarkerMisRatVal(Double.parseDouble(txtF_1.getText()));
					gwasParams.setDiscardSampleByMisRat(chkB_SMS.isSelected());
					gwasParams.setDiscardSampleMisRatVal(Double.parseDouble(txtF_SMS.getText()));
					gwasParams.setDiscardSampleByHetzyRat(chkB_SHZ.isSelected());
					gwasParams.setDiscardSampleHetzyRatVal(Double.parseDouble(txtF_SHZ.getText()));
					gwasParams.setProceed(true);
				} catch (NumberFormatException numberFormatException) {
				}
				dialog.dispose();
			} else {
				gwasParams.setProceed(false);
			}
		}
	}

	private static class CancelAction extends AbstractAction {

		CancelAction() {

			putValue(NAME, Text.All.cancel);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			dialog.setVisible(false);
		}
	}

	private static void setConstraints(GridBagConstraints c,
			int gridx,
			int gridy,
			int anchor)
	{
		c.gridx = gridx;
		c.gridy = gridy;
		c.anchor = anchor;
	}
}
