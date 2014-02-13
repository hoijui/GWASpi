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
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreInfoForGtFreq extends JFrame {

	private final Logger log = LoggerFactory.getLogger(MoreInfoForGtFreq.class);

	// Variables declaration - do not modify
	private JButton btn_Go;
	private JButton btn_Help;
	private JButton btn_Cancel;
	private JCheckBox chkB_1;
	private JCheckBox chkB_2;
	private JCheckBox chkB_SMS;
	private JCheckBox chkB_SHZ;
	private JTextField txtF_1;
	private JTextField txtF_SMS;
	private JTextField txtF_MHZ;
	private JTextField txtF_SHZ;
	private JFrame myFrame = new JFrame("GridBagLayout Test");
	private JDialog dialog;
	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
	// End of variables declaration

	public GWASinOneGOParams showMoreInfo() {

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

	private JPanel getQuestionsPanel() {

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

		//<editor-fold defaultstate="expanded" desc="FORMAT INDEPENDENT">
		chkB_1.setSelected(true);
		chkB_2.setSelected(true);
		chkB_SMS.setSelected(true);
		chkB_SHZ.setSelected(false);

		chkB_1.setText(Text.Operation.discardMismatch);
		chkB_1.setEnabled(false);

		chkB_2.setText(Text.Operation.discardMarkerMissing);
		double markerMissingRatioThreshold;
		try {
			markerMissingRatioThreshold = Double.parseDouble(Config.getConfigValue(
					"markerMissingRatioThreshold",
					String.valueOf(MarkerCensusOperationParams.DEFAULT_MARKER_MISSING_RATIO)));
		} catch (IOException ex) {
			markerMissingRatioThreshold = MarkerCensusOperationParams.DISABLE_MARKER_MISSING_RATIO;
		}
		txtF_1.setText(String.valueOf(markerMissingRatioThreshold));

		chkB_SMS.setText(Text.Operation.discardSampleMissing);
		double sampleMissingRatioThreshold;
		try {
			sampleMissingRatioThreshold = Double.parseDouble(Config.getConfigValue(
					SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_MISSING_THRESHOLD_CONFIG,
					String.valueOf(MarkerCensusOperationParams.DEFAULT_SAMPLE_MISSING_RATIO)));
		} catch (IOException ex) {
			sampleMissingRatioThreshold = MarkerCensusOperationParams.DISABLE_SAMPLE_MISSING_RATIO;
		}
		txtF_SMS.setText(String.valueOf(sampleMissingRatioThreshold));

		chkB_SHZ.setText(Text.Operation.discardSampleHetzy);
		double hetzygThreshold;
		try {
			hetzygThreshold = Double.parseDouble(Config.getConfigValue(
					SampleQAHetzygPlotZoom.PLOT_SAMPLEQA_HETZYG_THRESHOLD_CONFIG,
					String.valueOf(MarkerCensusOperationParams.DEFAULT_SAMPLE_HETZY_RATIO)));
		} catch (IOException ex) {
			hetzygThreshold = MarkerCensusOperationParams.DISABLE_SAMPLE_HETZY_RATIO;
		}
		txtF_SHZ.setText(String.valueOf(hetzygThreshold));
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

			if (!txtF_1.getText().isEmpty() && !txtF_SMS.getText().isEmpty()) {
				try {
					MarkerCensusOperationParams markerCensusOperationParams = gwasParams.getMarkerCensusOperationParams();

					markerCensusOperationParams.setDiscardMismatches(chkB_1.isSelected());

					final double markerMissingRatio;
					if (chkB_2.isSelected()) {
						markerMissingRatio = Double.parseDouble(txtF_1.getText());
					} else {
						markerMissingRatio = MarkerCensusOperationParams.DISABLE_MARKER_MISSING_RATIO;
					}
					markerCensusOperationParams.setMarkerMissingRatio(markerMissingRatio);

					final double sampleMissingRatio;
					if (chkB_SMS.isSelected()) {
						sampleMissingRatio = Double.parseDouble(txtF_SMS.getText());
					} else {
						sampleMissingRatio = MarkerCensusOperationParams.DISABLE_SAMPLE_MISSING_RATIO;
					}
					markerCensusOperationParams.setSampleMissingRatio(sampleMissingRatio);

					final double sampleHetzyRatio;
					if (chkB_SHZ.isSelected()) {
						sampleHetzyRatio = Double.parseDouble(txtF_SHZ.getText());
					} else {
						sampleHetzyRatio = MarkerCensusOperationParams.DISABLE_SAMPLE_HETZY_RATIO;
					}
					markerCensusOperationParams.setSampleHetzygRatio(sampleHetzyRatio);

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
