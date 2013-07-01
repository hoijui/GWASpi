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
package org.gwaspi.operations.combi;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.utils.MinMaxDoubleVerifier;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;

/**
 * TODO
 */
public class CombiTestParamsGUI extends JPanel {

	public static final String TITLE = "Edit Combi-Test parameters";

	private CombiTestParams originalCombiTestParams;

	private final JLabel parentMatrixLabel;
	private final JTextField parentMatrixValue;

	private final JLabel hwOperationLabel;
	private final JComboBox hwOperationValue;

	private final JLabel hwThresholdLabel;
	private final JPanel hwThresholdP;
	private final JFormattedTextField hwThresholdValue;
	private final JTextField hwThresholdPercentage;
	private final JCheckBox hwThresholdDefault;

	private final JLabel genotypeEncoderLabel;
	private final JPanel genotypeEncoderP;
	private final JComboBox genotypeEncoderValue;
	private final JCheckBox genotypeEncoderDefault;

	private final JLabel markersToKeepLabel;
	private final JPanel markersToKeepP;
	private final JSpinner markersToKeepValue;
	private final JTextField markersToKeepPercentage;
	private final JButton markersToKeepDefault;

	private final JLabel useThresholdCalibrationLabel;
	private final JPanel useThresholdCalibrationP;
	private final JCheckBox useThresholdCalibrationValue;
	private final JLabel useThresholdCalibrationDefault;

	private final JLabel resultMatrixLabel;
	private final JPanel resultMatrixP;
	private final JTextField resultMatrixValue;
	private final JCheckBox resultMatrixDefault;

	/**
	 * Allows to reset the value of a text component to its default.
	 * In case of a toggle-button, it also buffers the custom value,
	 * and later goes back to that value.
	 */
	private static class DefaultAction extends AbstractAction implements DocumentListener {

		private final JTextComponent valueComponent;
		private final String defaultValue;
		private String customValue;

		DefaultAction(JTextComponent valueComponent, String defaultValue) {

			this.valueComponent = valueComponent;
			this.defaultValue = defaultValue;
			this.customValue = valueComponent.getText();

			final boolean isDefaultValue = this.customValue.equals(this.defaultValue);

			putValue(NAME, "Use default");
			putValue(SELECTED_KEY, isDefaultValue);

			this.valueComponent.getDocument().addDocumentListener(this);
			this.valueComponent.setEditable(!isDefaultValue);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			if (evt.getSource() instanceof JToggleButton) {
				// if our source is a JToggleButton, JCheckBox or a JRadioButton ...
				JToggleButton sourceToggleButton = (JToggleButton) evt.getSource();
				valueComponent.setEditable(!sourceToggleButton.isSelected());
				if (sourceToggleButton.isSelected()) {
					// put the custom value into a buffer,
					// if the user wants to use the default value
					customValue = valueComponent.getText();
				} else {
					// or restore it from that buffer,
					// if the user wants to use a non default value again
					valueComponent.setText(customValue);
					return;
				}
			}
			// else (e.g. if our source is a JButton),
			// always restore the default value

			valueComponent.setText(defaultValue);
		}

		@Override
		public void insertUpdate(DocumentEvent evt) {
			textValueChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent evt) {
			textValueChanged();
		}

		@Override
		public void changedUpdate(DocumentEvent evt) {
			textValueChanged();
		}

		private void textValueChanged() {
//			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	public CombiTestParamsGUI() {

		this.originalCombiTestParams = null;

		// init the GUI components
		this.parentMatrixLabel = new JLabel();
		this.parentMatrixValue = new JTextField();

		this.hwOperationLabel = new JLabel();
		this.hwOperationValue = new JComboBox();

		this.hwThresholdLabel = new JLabel();
		this.hwThresholdP = new JPanel();
		this.hwThresholdValue = new JFormattedTextField(NumberFormat.getNumberInstance());
		this.hwThresholdPercentage = new JTextField();
		this.hwThresholdDefault = new JCheckBox();

		this.genotypeEncoderLabel = new JLabel();
		this.genotypeEncoderP = new JPanel();
		this.genotypeEncoderValue = new JComboBox();
		this.genotypeEncoderDefault = new JCheckBox();

		this.markersToKeepLabel = new JLabel();
		this.markersToKeepP = new JPanel();
		this.markersToKeepValue = new JSpinner();
		this.markersToKeepPercentage = new JTextField();
		this.markersToKeepDefault = new JButton();

		this.useThresholdCalibrationLabel = new JLabel();
		this.useThresholdCalibrationP = new JPanel();
		this.useThresholdCalibrationValue = new JCheckBox();
		this.useThresholdCalibrationDefault = new JLabel();

		this.resultMatrixLabel = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixValue = new JTextField();
		this.resultMatrixDefault = new JCheckBox();

		// pre-configure the GUI components
		this.hwThresholdP.add(this.hwThresholdValue);
		this.hwThresholdP.add(this.hwThresholdPercentage);
		this.hwThresholdP.add(this.hwThresholdDefault);

		this.genotypeEncoderP.add(this.genotypeEncoderValue);
		this.genotypeEncoderP.add(this.genotypeEncoderDefault);

		this.markersToKeepP.add(this.markersToKeepValue);
		this.markersToKeepP.add(this.markersToKeepPercentage);
		this.markersToKeepP.add(this.markersToKeepDefault);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationDefault);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);


		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixLabel, parentMatrixValue);
		labelsAndComponents.put(hwOperationLabel, hwOperationValue);
		labelsAndComponents.put(hwThresholdLabel, hwThresholdP);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(markersToKeepLabel, markersToKeepP);
		labelsAndComponents.put(useThresholdCalibrationLabel, useThresholdCalibrationP);
		labelsAndComponents.put(resultMatrixLabel, resultMatrixP);
		createLayout(this, labelsAndComponents);


		this.parentMatrixLabel.setText("parent matrix");
		this.hwOperationLabel.setLabelFor(this.parentMatrixValue);
		this.parentMatrixValue.setEditable(false);

		this.hwOperationLabel.setText("Hardy-Weinberg operation");
		this.hwOperationLabel.setLabelFor(this.hwOperationValue);

		this.hwThresholdLabel.setText("Hardy-Weinberg threshold");
		this.hwThresholdLabel.setLabelFor(this.hwThresholdValue);
		this.hwThresholdValue.setInputVerifier(new MinMaxDoubleVerifier(0.0000000000001, 1.0));
//		this.hwThresholdTF.setColumns(10);
//		this.hwThresholdValue.addPropertyChangeListener("value", this); // TODO use this!

		this.genotypeEncoderLabel.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderLabel.setLabelFor(this.genotypeEncoderValue);
		this.genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));

		this.markersToKeepLabel.setText("number of markers to keep");
		this.markersToKeepLabel.setLabelFor(this.markersToKeepValue);

		this.useThresholdCalibrationLabel.setText("use resampling based threshold calibration");
		this.useThresholdCalibrationLabel.setLabelFor(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationValue.addChangeListener(null);
		this.useThresholdCalibrationDefault

		this.resultMatrixLabel.setText("Result matrix name");
		this.resultMatrixLabel.setLabelFor(this.resultMatrixValue);
	}

	public void setCombiTestParams(CombiTestParams combiTestParams) {

		this.parentMatrixValue.setText(combiTestParams.getMatrixKey().toString());

		this.hwOperationValue.setModel(new DefaultComboBoxModel(getAllHWOperationKeys(combiTestParams.getMatrixKey(), null)));
		this.hwOperationValue.setSelectedItem(combiTestParams.getHardyWeinbergOperationKey());

		this.hwThresholdValue.setValue(combiTestParams.getHardyWeinbergThreshold());
		this.hwThresholdDefault.setAction(new DefaultAction(this.hwThresholdValue, String.valueOf(combiTestParams.getHardyWeinbergThresholdDefault())));

		this.genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		this.genotypeEncoderValue.setSelectedItem(combiTestParams.getEncoder());

		SpinnerModel model = new SpinnerNumberModel(
				combiTestParams.getMarkersToKeep(), // initial value
				1, // min
				combiTestParams.getTotalMarkers() - 1, // max
				1); // step
		this.markersToKeepValue.setModel(model);

		this.useThresholdCalibrationValue.setSelected(combiTestParams.isUseThresholdCalibration());

		this.resultMatrixValue.setText(combiTestParams.getResultMatrixName());
		this.resultMatrixDefault.setAction(new DefaultAction(this.resultMatrixValue, combiTestParams.getResultMatrixNameDefault()));
	}

	private static void createLayout(Container container, Map<JLabel, JComponent> labelsAndComponents) {

        GroupLayout layout = new GroupLayout(container);
        container.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup horizontalG = layout.createSequentialGroup();
		GroupLayout.ParallelGroup horizontalLabelsG = layout.createParallelGroup();
		GroupLayout.ParallelGroup horizontalComponentsG = layout.createParallelGroup();
		for (Map.Entry<JLabel, JComponent> labelAndComponent : labelsAndComponents.entrySet()) {
			horizontalLabelsG.addComponent(labelAndComponent.getKey());
			// The following is better done manually, earlier
//			labelAndComponent.getKey().setLabelFor(labelAndComponent.getValue());
			horizontalComponentsG.addComponent(labelAndComponent.getValue());
		}
        horizontalG.addGroup(horizontalLabelsG);
		horizontalG.addGroup(horizontalComponentsG);
		layout.setHorizontalGroup(horizontalG);


        GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup();
		for (Map.Entry<JLabel, JComponent> labelAndComponent : labelsAndComponents.entrySet()) {
			GroupLayout.ParallelGroup verticalLabelAndComponentG = layout.createParallelGroup();
			verticalLabelAndComponentG.addComponent(labelAndComponent.getKey());
			verticalLabelAndComponentG.addComponent(labelAndComponent.getValue());
			verticalGroup.addGroup(verticalLabelAndComponentG);
		}
		layout.setVerticalGroup(verticalGroup);
	}

	private OperationKey[] getAllHWOperationKeys(MatrixKey parentMatrixKey, OperationKey currentCensusOPKey) {

		List<OperationMetadata> hwOperations;
		try {
//			OperationKey censusOPKey = MatrixAnalysePanel.AssociationTestsAction.evaluateCensusOPId(currentCensusOPKey, parentMatrixKey);
//			hwOperations = OperationsList.getOperationsList(parentMatrixKey.getMatrixId(), censusOPKey.getId(), OPType.HARDY_WEINBERG);
			// FIXME use also censusOp?
			List<OperationMetadata> operations = OperationsList.getOperationsList(parentMatrixKey);
			hwOperations = new ArrayList<OperationMetadata>(operations.size());
			for (OperationMetadata operationMetadata : operations) {
				if (operationMetadata.getOperationType() == OPType.HARDY_WEINBERG) {
					hwOperations.add(operationMetadata);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		OperationKey[] hwOperationKeys = new OperationKey[hwOperations.size()];
		int i = 0;
		for (OperationMetadata hwOperation : hwOperations) {
			hwOperationKeys[i] = OperationKey.valueOf(hwOperation);
			i++;
		}

		 return hwOperationKeys;
	}

	public CombiTestParams getCombiTestParams() {

		CombiTestParams combiTestParams = new CombiTestParams(
				originalCombiTestParams.getMatrixKey(), // cause it is not editable
				(OperationKey) hwOperationValue.getSelectedItem(),
				(Double) hwThresholdValue.getValue(),
				(GenotypeEncoder) genotypeEncoderValue.getSelectedItem(),
				(Integer) markersToKeepValue.getValue(),
				useThresholdCalibrationValue.isSelected(),
				resultMatrixValue.getText()
				);

		return combiTestParams;
	}

	public static CombiTestParams chooseCombiTestParams(Component parentComponent, CombiTestParams combiTestParams) {

		CombiTestParamsGUI combiTestParamsGUI = new CombiTestParamsGUI();
		combiTestParamsGUI.setCombiTestParams(combiTestParams);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				combiTestParamsGUI,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		CombiTestParams returnCombiTestParams;
		if (selectedValue == JOptionPane.OK_OPTION) {
			returnCombiTestParams = combiTestParamsGUI.getCombiTestParams();
		} else {
//			// return the original parameters,
//			// if the user clicked on the [Cancel] button
//			returnCombiTestParams = combiTestParams;
			returnCombiTestParams = null;
		}

		return returnCombiTestParams;
	}
}
