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
	private final JPanel hwThresholdP;
	private final JFormattedTextField hwThresholdTF;
	private final JTextField hwThresholdTF2;
	private final JButton hwThresholdB;

	private final JLabel genotypeEncoderL;
	private final JPanel genotypeEncoderP;
	private final JComboBox genotypeEncoderCB;
	private final JButton genotypeEncoderB;

	private final JLabel markersToKeepL;
	private final JPanel markersToKeepP;
	private final JSpinner markersToKeepS;
	private final JTextField markersToKeepTF;
	private final JButton markersToKeepB;

	private final JLabel useThresholdCalibrationL;
	private final JPanel useThresholdCalibrationP;
	private final JCheckBox useThresholdCalibrationCB;
	private final JTextField useThresholdCalibrationTF;

	private final JLabel resultMatrixL;
	private final JPanel resultMatrixP;
	private final JTextField resultMatrixTF;
	private final JCheckBox resultMatrixCB;

	public CombiTestParamsGUI() {

		this.originalCombiTestParams = null;

		// init the GUI components
		this.parentMatrixL = new JLabel();
		this.parentMatrixTF = new JTextField();

		this.hwOperationL = new JLabel();
		this.hwOperationCB = new JComboBox();

		this.hwThresholdL = new JLabel();
		this.hwThresholdP = new JPanel();
		this.hwThresholdTF = new JFormattedTextField(NumberFormat.getNumberInstance());
		this.hwThresholdTF2 = new JTextField();
		this.hwThresholdB = new JButton();

		this.genotypeEncoderL = new JLabel();
		this.genotypeEncoderP = new JPanel();
		this.genotypeEncoderCB = new JComboBox();
		this.genotypeEncoderB = new JButton();

		this.markersToKeepL = new JLabel();
		this.markersToKeepP = new JPanel();
		this.markersToKeepS = new JSpinner();
		this.markersToKeepTF = new JTextField();
		this.markersToKeepB = new JButton();

		this.useThresholdCalibrationL = new JLabel();
		this.useThresholdCalibrationP = new JPanel();
		this.useThresholdCalibrationCB = new JCheckBox();
		this.useThresholdCalibrationTF = new JTextField();

		this.resultMatrixL = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixTF = new JTextField();
		this.resultMatrixCB = new JCheckBox();

		// pre-configure the GUI components
		this.hwThresholdP.add(this.hwThresholdTF);
		this.hwThresholdP.add(this.hwThresholdTF2);
		this.hwThresholdP.add(this.hwThresholdB);

		this.genotypeEncoderP.add(this.genotypeEncoderCB);
		this.genotypeEncoderP.add(this.genotypeEncoderB);

		this.markersToKeepP.add(this.markersToKeepS);
		this.markersToKeepP.add(this.markersToKeepTF);
		this.markersToKeepP.add(this.markersToKeepB);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationCB);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationTF);

		this.resultMatrixP.add(this.resultMatrixTF);
		this.resultMatrixP.add(this.resultMatrixCB);


		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixL, parentMatrixTF);
		labelsAndComponents.put(hwOperationL, hwOperationCB);
		labelsAndComponents.put(hwThresholdL, hwThresholdP);
		labelsAndComponents.put(genotypeEncoderL, genotypeEncoderP);
		labelsAndComponents.put(markersToKeepL, markersToKeepP);
		labelsAndComponents.put(useThresholdCalibrationL, useThresholdCalibrationP);
		labelsAndComponents.put(resultMatrixL, resultMatrixP);
		createLayout(this, labelsAndComponents);


		this.parentMatrixL.setText("parent matrix");
		this.hwOperationL.setLabelFor(this.parentMatrixTF);
		this.parentMatrixTF.setEditable(false);

		this.hwOperationL.setText("Hardy-Weinberg operation");
		this.hwOperationL.setLabelFor(this.hwOperationCB);

		this.hwThresholdL.setText("Hardy-Weinberg threshold");
		this.hwThresholdL.setLabelFor(this.hwThresholdTF);
		this.hwThresholdTF.setInputVerifier(new MinMaxDoubleVerifier(0.0000000000001, 1.0));
//		this.hwThresholdTF.setColumns(10);
//		this.hwThresholdTF.addPropertyChangeListener("value", this);

		this.genotypeEncoderL.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderL.setLabelFor(this.genotypeEncoderCB);
		this.genotypeEncoderCB.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));

		this.markersToKeepL.setText("number of markers to keep");
		this.markersToKeepL.setLabelFor(this.markersToKeepS);

		this.useThresholdCalibrationL.setText("use resampling based threshold calibration");
		this.useThresholdCalibrationL.setLabelFor(this.useThresholdCalibrationCB);

		this.resultMatrixL.setText("Result matrix name");
		this.resultMatrixL.setLabelFor(this.resultMatrixTF);
	}

	public void setCombiTestParams(CombiTestParams combiTestParams) {

		this.parentMatrixTF.setText(combiTestParams.getMatrixKey().toString());

		this.hwOperationCB.setModel(new DefaultComboBoxModel(getAllHWOperationKeys(combiTestParams.getMatrixKey(), null)));
		this.hwOperationCB.setSelectedItem(combiTestParams.getHardyWeinbergOperationKey());

		this.hwThresholdTF.setValue(combiTestParams.getHardyWeinbergThreshold());

		this.genotypeEncoderCB.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		this.genotypeEncoderCB.setSelectedItem(combiTestParams.getEncoder());

		SpinnerModel model = new SpinnerNumberModel(
				combiTestParams.getMarkersToKeep(), // initial value
				1, // min
				combiTestParams.getTotalMarkers() - 1, // max
				1); // step
		this.markersToKeepS.setModel(model);

		this.useThresholdCalibrationCB.setSelected(combiTestParams.isUseThresholdCalibration());

		this.resultMatrixTF.setText(combiTestParams.getResultMatrixName());
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
