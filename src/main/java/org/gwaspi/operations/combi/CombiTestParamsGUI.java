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
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
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
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
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

	private final JLabel parentMatrixL;
	private final JTextField parentMatrixTF;

	private final JLabel hwOperationL;
	private final JComboBox hwOperationCB;

	private final JLabel hwThresholdL;
	private final JFormattedTextField hwThresholdTF;

	private final JLabel genotypeEncoderL;
	private final JComboBox genotypeEncoderCB;

	private final JLabel markersToKeepL;
	private final JSpinner markersToKeepS;

	private final JLabel useThresholdCalibrationL;
	private final JCheckBox useThresholdCalibrationCB;

	private final JLabel resultMatrixL;
	private final JTextField resultMatrixTF;

	public CombiTestParamsGUI() {

		this.originalCombiTestParams = null;

		this.parentMatrixL = new JLabel();
		this.parentMatrixTF = new JTextField();

		this.hwOperationL = new JLabel();
		this.hwOperationCB = new JComboBox();

		this.hwThresholdL = new JLabel();
		this.hwThresholdTF = new JFormattedTextField(NumberFormat.getNumberInstance());

		this.genotypeEncoderL = new JLabel();
		this.genotypeEncoderCB = new JComboBox();

		this.markersToKeepL = new JLabel();
		this.markersToKeepS = new JSpinner();

		this.useThresholdCalibrationL = new JLabel();
		this.useThresholdCalibrationCB = new JCheckBox();

		this.resultMatrixL = new JLabel();
		this.resultMatrixTF = new JTextField();
	}

	public void setCombiTestParams(CombiTestParams combiTestParams) {

		this.parentMatrixL.setText("parent matrix");
		this.hwOperationL.setLabelFor(this.parentMatrixTF);
		this.parentMatrixTF.setText(combiTestParams.getMatrixKey().toString());
		this.parentMatrixTF.setEditable(false);

		this.hwOperationL.setText("Hardy-Weinberg operation");
		this.hwOperationL.setLabelFor(this.hwOperationCB);
		this.hwOperationCB.setModel(new DefaultComboBoxModel(getAllHWOperationKeys(combiTestParams.getMatrixKey(), null)));
		this.hwOperationCB.setSelectedItem(combiTestParams.getHardyWeinbergOperationKey());

		this.hwThresholdL.setText("Hardy-Weinberg threshold");
		this.hwThresholdL.setLabelFor(this.hwThresholdTF);
		this.hwThresholdTF.setInputVerifier(new MinMaxDoubleVerifier(0.0000000000001, 1.0));
//		this.hwThresholdTF.setColumns(10);
//		this.hwThresholdTF.addPropertyChangeListener("value", this);
		this.hwThresholdTF.setValue(combiTestParams.getHardyWeinbergThreshold());

		this.genotypeEncoderL.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderL.setLabelFor(this.genotypeEncoderCB);
		this.genotypeEncoderCB.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		this.genotypeEncoderCB.setSelectedItem(combiTestParams.getEncoder());

		this.markersToKeepL.setText("number of markers to keep");
		this.markersToKeepL.setLabelFor(this.markersToKeepS);
		SpinnerModel model = new SpinnerNumberModel(
				2, // initial value
				1, // min
				currentYear + 100, // max
				1); // step
		this.markersToKeepS.setModel(model);

		this.useThresholdCalibrationL.setText("use resampling based threshold calibration");
		this.useThresholdCalibrationL.setLabelFor(this.useThresholdCalibrationCB);
//		this.useThresholdCalibrationCB;

		this.resultMatrixL.setText("Result matrix name");
		this.resultMatrixL.setLabelFor(this.resultMatrixTF);
		this.resultMatrixTF.setText(combiTestParams.getResultMatrixName());

		this.add(this.parentMatrixL);
		this.add(this.parentMatrixTF);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixL, parentMatrixTF);
		labelsAndComponents.put(hwOperationL, hwOperationCB);
		labelsAndComponents.put(hwThresholdL, hwThresholdTF);
		labelsAndComponents.put(genotypeEncoderL, genotypeEncoderCB);
		labelsAndComponents.put(markersToKeepL, markersToKeepS);
		labelsAndComponents.put(useThresholdCalibrationL, useThresholdCalibrationCB);
		labelsAndComponents.put(resultMatrixL, resultMatrixTF);
		createLayout(this, labelsAndComponents);
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
			labelAndComponent.getKey().setLabelFor(labelAndComponent.getValue());
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
				(OperationKey) hwOperationCB.getSelectedItem(),
				(Double) hwThresholdTF.getValue(),
				(GenotypeEncoder) genotypeEncoderCB.getSelectedItem(),
				(Integer) markersToKeepS.getValue(),
				useThresholdCalibrationCB.isSelected(),
				resultMatrixTF.getText()
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
			// return the original parameters,
			// if the user clicked on the [Cancel] button
			returnCombiTestParams = combiTestParams;
		}

		return returnCombiTestParams;
	}
}
