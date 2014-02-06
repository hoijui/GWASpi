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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.utils.AbsolutePercentageComponentRelation;
import org.gwaspi.gui.utils.ComboBoxDefaultAction;
import org.gwaspi.gui.utils.MinMaxDoubleVerifier;
import org.gwaspi.gui.utils.SpinnerDefaultAction;
import org.gwaspi.gui.utils.TextDefaultAction;
import org.gwaspi.gui.utils.ValueContainer;
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

	private final JLabel censusOperationLabel; // TODO This is not yet setup correctly! see hw stuff below for example!
	private final JComboBox censusOperationValue;

	private final JLabel hwOperationLabel;
	private final JComboBox hwOperationValue;

	private final JLabel hwThresholdLabel;
	private final JPanel hwThresholdP;
	private final JFormattedTextField hwThresholdValue;
//	private final JSpinner hwThresholdValue;
	private final JSpinner hwThresholdPercentage;
	private final JLabel hwThresholdPercentageLabel;
	private final JCheckBox hwThresholdDefault;
	private AbsolutePercentageComponentRelation hwThresholdComponentRelation;

	private final JLabel genotypeEncoderLabel;
	private final JPanel genotypeEncoderP;
	private final JComboBox genotypeEncoderValue;
	private final JCheckBox genotypeEncoderDefault;

	private final JLabel markersToKeepLabel;
	private final JPanel markersToKeepP;
	private final JSpinner markersToKeepValue;
	private final JSpinner markersToKeepPercentage;
	private final JLabel markersToKeepPercentageLabel;
	private final JCheckBox markersToKeepDefault;
	private AbsolutePercentageComponentRelation markersToKeepComponentRelation;

	private final JLabel useThresholdCalibrationLabel;
	private final JPanel useThresholdCalibrationP;
	private final JCheckBox useThresholdCalibrationValue;
	private final JLabel useThresholdCalibrationDefault;

	private final JLabel resultMatrixLabel;
	private final JPanel resultMatrixP;
	private final JTextField resultMatrixValue;
	private final JCheckBox resultMatrixDefault;

	public CombiTestParamsGUI() {

		this.originalCombiTestParams = null;

		// init the GUI components
		this.parentMatrixLabel = new JLabel();
		this.parentMatrixValue = new JTextField();

		this.censusOperationLabel = new JLabel();
		this.censusOperationValue = new JComboBox();

		this.hwOperationLabel = new JLabel();
		this.hwOperationValue = new JComboBox();

		this.hwThresholdLabel = new JLabel();
		this.hwThresholdP = new JPanel();
		this.hwThresholdValue = new JFormattedTextField(NumberFormat.getNumberInstance());
//		this.hwThresholdValue = new JSpinner();
		this.hwThresholdPercentage = new JSpinner();
		this.hwThresholdPercentageLabel = new JLabel();
		this.hwThresholdDefault = new JCheckBox();
		this.hwThresholdComponentRelation = null;

		this.genotypeEncoderLabel = new JLabel();
		this.genotypeEncoderP = new JPanel();
		this.genotypeEncoderValue = new JComboBox();
		this.genotypeEncoderDefault = new JCheckBox();

		this.markersToKeepLabel = new JLabel();
		this.markersToKeepP = new JPanel();
		this.markersToKeepValue = new JSpinner();
		this.markersToKeepPercentage = new JSpinner();
		this.markersToKeepPercentageLabel = new JLabel();
		this.markersToKeepDefault = new JCheckBox();
		this.markersToKeepComponentRelation = null;

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
		this.hwThresholdP.add(this.hwThresholdPercentageLabel);
		this.hwThresholdP.add(this.hwThresholdDefault);

		this.genotypeEncoderP.add(this.genotypeEncoderValue);
		this.genotypeEncoderP.add(this.genotypeEncoderDefault);

		this.markersToKeepP.add(this.markersToKeepValue);
		this.markersToKeepP.add(this.markersToKeepPercentage);
		this.markersToKeepP.add(this.markersToKeepPercentageLabel);
		this.markersToKeepP.add(this.markersToKeepDefault);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationDefault);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);


		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixLabel, parentMatrixValue);
		labelsAndComponents.put(censusOperationLabel, censusOperationValue);
		labelsAndComponents.put(hwOperationLabel, hwOperationValue);
		labelsAndComponents.put(hwThresholdLabel, hwThresholdP);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(markersToKeepLabel, markersToKeepP);
		labelsAndComponents.put(useThresholdCalibrationLabel, useThresholdCalibrationP);
		labelsAndComponents.put(resultMatrixLabel, resultMatrixP);
		createLayout(this, labelsAndComponents);

		FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);

		this.parentMatrixLabel.setText("parent matrix");
		this.hwOperationLabel.setLabelFor(this.parentMatrixValue);
		this.parentMatrixValue.setEditable(false);

		this.hwOperationLabel.setText("Hardy-Weinberg operation");
		this.hwOperationLabel.setLabelFor(this.hwOperationValue);

		this.hwThresholdLabel.setText("Hardy-Weinberg threshold");
		this.hwThresholdLabel.setLabelFor(this.hwThresholdValue);
		this.hwThresholdP.setLayout(contentPanelLayout);
		this.hwThresholdValue.setToolTipText("Discard markers with Hardy-Weinberg p-value smaller then this value");
		this.hwThresholdValue.setInputVerifier(new MinMaxDoubleVerifier(0.0000000000001, 1.0));
//		this.hwThresholdTF.setColumns(10);
//		this.hwThresholdValue.addPropertyChangeListener("value", this); // TODO use this!
		this.hwThresholdPercentage.setToolTipText("Discard markers with Hardy-Weinberg p-value smaller then this value / #markers");
		this.hwThresholdPercentageLabel.setText("%");

		this.genotypeEncoderLabel.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderLabel.setLabelFor(this.genotypeEncoderValue);
		this.genotypeEncoderP.setLayout(contentPanelLayout);
		this.genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));

		this.markersToKeepLabel.setText("number of markers to keep");
		this.markersToKeepLabel.setLabelFor(this.markersToKeepValue);
		this.markersToKeepP.setLayout(contentPanelLayout);
		this.markersToKeepPercentageLabel.setText("%");

		this.useThresholdCalibrationLabel.setText("use resampling based threshold calibration");
		this.useThresholdCalibrationLabel.setLabelFor(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.setLayout(contentPanelLayout);
		this.useThresholdCalibrationDefault.setText("");
		this.useThresholdCalibrationDefault.setForeground(Color.RED);
		this.useThresholdCalibrationValue.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				if (useThresholdCalibrationValue.isSelected()) {
					useThresholdCalibrationDefault.setText("very slow!");
				} else {
					useThresholdCalibrationDefault.setText("");
				}
			}
		});

		this.resultMatrixLabel.setText("Result matrix name");
		this.resultMatrixLabel.setLabelFor(this.resultMatrixValue);
		this.resultMatrixP.setLayout(contentPanelLayout);
	}

	public void setCombiTestParams(CombiTestParams combiTestParams) {

		this.originalCombiTestParams = combiTestParams;

//		this.parentMatrixValue.setText(combiTestParams.getMatrixKey().toString());
//
//		this.hwOperationValue.setModel(new DefaultComboBoxModel(getAllHWOperationKeys(combiTestParams.getMatrixKey(), null)));
//		this.hwOperationValue.setSelectedItem(combiTestParams.getHardyWeinbergOperationKey());
//
//		this.hwThresholdValue.setValue(combiTestParams.getHardyWeinbergThreshold());
//		this.hwThresholdDefault.setAction(new TextDefaultAction(this.hwThresholdValue, String.valueOf(combiTestParams.getHardyWeinbergThresholdDefault())));
//		SpinnerModel hwThresholdPercentageModel = new SpinnerNumberModel(
//				combiTestParams.getHardyWeinbergThreshold() * combiTestParams.getTotalMarkers() / 100.0, // initial value
//				0.1, // min
//				100.0, // max
//				0.5); // step
//		this.hwThresholdPercentage.setModel(hwThresholdPercentageModel);
		this.hwThresholdComponentRelation
				= new AbsolutePercentageComponentRelation(
				new ValueContainer<Number>(hwThresholdValue),
				new ValueContainer<Number>(hwThresholdPercentage),
				1.0 / combiTestParams.getTotalMarkers(),
				new AbsolutePercentageComponentRelation.RoundingConstrainer(combiTestParams.getTotalMarkers() * 100.0),
				new AbsolutePercentageComponentRelation.RoundingConstrainer(10.0));

		this.genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		this.genotypeEncoderValue.setSelectedItem(combiTestParams.getEncoder());
		this.genotypeEncoderDefault.setAction(new ComboBoxDefaultAction(this.genotypeEncoderValue, CombiTestParams.getEncoderDefault()));

		SpinnerModel markersToKeepValueModel = new SpinnerNumberModel(
				combiTestParams.getMarkersToKeep(), // initial value
				1, // min
				combiTestParams.getTotalMarkers() - 1, // max
				1); // step
		this.markersToKeepValue.setModel(markersToKeepValueModel);
		this.markersToKeepDefault.setAction(new SpinnerDefaultAction(this.markersToKeepValue, combiTestParams.getMarkersToKeepDefault()));
		SpinnerModel markersToKeepPercentageModel = new SpinnerNumberModel(
				(double) combiTestParams.getMarkersToKeep() / combiTestParams.getTotalMarkers() * 100.0, // initial value
				0.1, // min
				100.0, // max
				0.5); // step
		this.markersToKeepPercentage.setModel(markersToKeepPercentageModel);
		this.markersToKeepComponentRelation
				= new AbsolutePercentageComponentRelation(
				new ValueContainer<Number>(markersToKeepValue),
				new ValueContainer<Number>(markersToKeepPercentage),
				combiTestParams.getTotalMarkers());

		this.useThresholdCalibrationValue.setSelected(combiTestParams.isUseThresholdCalibration());

		this.resultMatrixValue.setText(combiTestParams.getResultOperationName());
		this.resultMatrixDefault.setAction(new TextDefaultAction(this.resultMatrixValue, combiTestParams.getResultOperationNameDefault()));

		this.validate();
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
			horizontalLabelsG.addComponent(labelAndComponent.getKey(), GroupLayout.Alignment.TRAILING);
			// The following is better done manually, earlier
//			labelAndComponent.getKey().setLabelFor(labelAndComponent.getValue());
			horizontalComponentsG.addComponent(labelAndComponent.getValue(), GroupLayout.Alignment.LEADING);
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
//				originalCombiTestParams.getMatrixKey(), // cause it is not editable
				(OperationKey) censusOperationValue.getSelectedItem(),
//				(OperationKey) hwOperationValue.getSelectedItem(),
//				(Double) hwThresholdValue.getValue(),
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
