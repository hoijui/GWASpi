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
import org.gwaspi.global.Config;
import org.gwaspi.gui.utils.AbsolutePercentageComponentRelation;
import org.gwaspi.gui.utils.ComboBoxDefaultAction;
import org.gwaspi.gui.utils.MinMaxDoubleVerifier;
import org.gwaspi.gui.utils.SpinnerDefaultAction;
import org.gwaspi.gui.utils.TextDefaultAction;
import org.gwaspi.gui.utils.ValueContainer;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
public class CombiTestParamsGUI extends JPanel {

	private static final Logger LOG = LoggerFactory.getLogger(CombiTestParamsGUI.class);

	public static final String TITLE = "Edit Combi-Test parameters";

	private CombiTestOperationParams originalCombiTestParams;

	private final JLabel parentMatrixLabel;
	private final JTextField parentMatrixValue;

	private final JLabel qaMarkersOperationLabel; // TODO This is not yet setup correctly! see hw stuff below for example!
	private final JComboBox qaMarkersOperationValue;

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

	private final JLabel weightsFilterWidthLabel;
	private final JPanel weightsFilterWidthP;
	private final JSpinner weightsFilterWidthValue;
	private final JSpinner weightsFilterWidthPercentage;
	private final JLabel weightsFilterWidthPercentageLabel;
	private final JCheckBox weightsFilterWidthDefault;
	private AbsolutePercentageComponentRelation weightsFilterWidthComponentRelation;

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

		this.qaMarkersOperationLabel = new JLabel();
		this.qaMarkersOperationValue = new JComboBox();

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

		this.weightsFilterWidthLabel = new JLabel();
		this.weightsFilterWidthP = new JPanel();
		this.weightsFilterWidthValue = new JSpinner();
		this.weightsFilterWidthPercentage = new JSpinner();
		this.weightsFilterWidthPercentageLabel = new JLabel();
		this.weightsFilterWidthDefault = new JCheckBox();
		this.weightsFilterWidthComponentRelation = null;

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

		this.weightsFilterWidthP.add(this.weightsFilterWidthValue);
		this.weightsFilterWidthP.add(this.weightsFilterWidthPercentage);
		this.weightsFilterWidthP.add(this.weightsFilterWidthPercentageLabel);
		this.weightsFilterWidthP.add(this.weightsFilterWidthDefault);

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
		labelsAndComponents.put(qaMarkersOperationLabel, qaMarkersOperationValue);
		labelsAndComponents.put(hwOperationLabel, hwOperationValue);
		labelsAndComponents.put(hwThresholdLabel, hwThresholdP);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(weightsFilterWidthLabel, weightsFilterWidthP);
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

		this.weightsFilterWidthLabel.setText("weights filter kernel width");
		this.weightsFilterWidthLabel.setLabelFor(this.weightsFilterWidthValue);
		this.weightsFilterWidthP.setLayout(contentPanelLayout);
		this.weightsFilterWidthPercentageLabel.setText("%");

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

	public void setCombiTestParams(CombiTestOperationParams combiTestParams) {

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
		this.genotypeEncoderDefault.setAction(new ComboBoxDefaultAction(this.genotypeEncoderValue, CombiTestOperationParams.getEncoderDefault()));

		final int totalMarkers = combiTestParams.getTotalMarkers();

		final SpinnerModel weightsFilterWidthValueModel;
		if (totalMarkers < 1) { // HACK This will only kick in when the parameters are invalid (total markers == -1)
			weightsFilterWidthValueModel = new SpinnerNumberModel(
					35, // initial value
					1, // min
					100000, // max
					1); // step
		} else {
			weightsFilterWidthValueModel = new SpinnerNumberModel(
					combiTestParams.getWeightsFilterWidth(), // initial value
					1, // min
					totalMarkers - 1, // max
					1); // step
		}
		this.weightsFilterWidthValue.setModel(weightsFilterWidthValueModel);
		this.weightsFilterWidthDefault.setAction(new SpinnerDefaultAction(this.weightsFilterWidthValue, combiTestParams.getMarkersToKeepDefault()));
		final SpinnerModel weightsFilterWidthPercentageModel;
		if (totalMarkers < 1) { // HACK This will only kick in when the parameters are invalid (total markers == -1)
			this.weightsFilterWidthDefault.setAction(new SpinnerDefaultAction(this.weightsFilterWidthValue, 20));
			weightsFilterWidthPercentageModel = new SpinnerNumberModel(
					0.0035, // initial value
					0.0001, // min
					20.0, // max
					0.0001); // step
		} else {
			weightsFilterWidthPercentageModel = new SpinnerNumberModel(
					(double) combiTestParams.getWeightsFilterWidth() / totalMarkers * 100.0, // initial value
					0.0001, // min
					20.0, // max
					0.0001); // step
		}
		this.weightsFilterWidthPercentage.setModel(weightsFilterWidthPercentageModel);
		this.weightsFilterWidthComponentRelation
				= new AbsolutePercentageComponentRelation(
				new ValueContainer<Number>(weightsFilterWidthValue),
				new ValueContainer<Number>(weightsFilterWidthPercentage),
				totalMarkers);

		final SpinnerModel markersToKeepValueModel;
		if (totalMarkers < 1) { // HACK This will only kick in when the parameters are invalid (total markers == -1)
			markersToKeepValueModel = new SpinnerNumberModel(
					20, // initial value
					1, // min
					100000, // max
					1); // step
		} else {
			markersToKeepValueModel = new SpinnerNumberModel(
					combiTestParams.getMarkersToKeep(), // initial value
					1, // min
					totalMarkers - 1, // max
					1); // step
		}
		this.markersToKeepValue.setModel(markersToKeepValueModel);
		this.markersToKeepDefault.setAction(new SpinnerDefaultAction(this.markersToKeepValue, combiTestParams.getMarkersToKeepDefault()));
		final SpinnerModel markersToKeepPercentageModel;
		if (totalMarkers < 1) { // HACK This will only kick in when the parameters are invalid (total markers == -1)
			this.markersToKeepDefault.setAction(new SpinnerDefaultAction(this.markersToKeepValue, 20));
			markersToKeepPercentageModel = new SpinnerNumberModel(
					0.5, // initial value
					0.1, // min
					100.0, // max
					0.5); // step
		} else {
			markersToKeepPercentageModel = new SpinnerNumberModel(
					(double) combiTestParams.getMarkersToKeep() / totalMarkers * 100.0, // initial value
					0.1, // min
					100.0, // max
					0.5); // step
		}
		this.markersToKeepPercentage.setModel(markersToKeepPercentageModel);
		this.markersToKeepComponentRelation
				= new AbsolutePercentageComponentRelation(
				new ValueContainer<Number>(markersToKeepValue),
				new ValueContainer<Number>(markersToKeepPercentage),
				totalMarkers);

		this.useThresholdCalibrationValue.setSelected(combiTestParams.isUseThresholdCalibration());

		this.resultMatrixValue.setText(combiTestParams.getName());
		this.resultMatrixDefault.setAction(new TextDefaultAction(this.resultMatrixValue, combiTestParams.getNameDefault()));

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
			List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(parentMatrixKey);
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

	public CombiTestOperationParams getCombiTestParams() {

		CombiTestOperationParams combiTestParams = new CombiTestOperationParams(
//				originalCombiTestParams.getMatrixKey(), // cause it is not editable
				(OperationKey) qaMarkersOperationValue.getSelectedItem(),
				(GenotypeEncoder) genotypeEncoderValue.getSelectedItem(),
				(Integer) weightsFilterWidthValue.getValue(),
				(Integer) markersToKeepValue.getValue(),
				useThresholdCalibrationValue.isSelected(),
				resultMatrixValue.getText()
				);

		return combiTestParams;
	}

	public static CombiTestOperationParams chooseCombiTestParams(Component parentComponent, CombiTestOperationParams combiTestParams) {

		CombiTestParamsGUI combiTestParamsGUI = new CombiTestParamsGUI();
		combiTestParamsGUI.setCombiTestParams(combiTestParams);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				combiTestParamsGUI,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		CombiTestOperationParams returnCombiTestParams;
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

	public static void main(String[] args) {

		Config.setDBSystemDir(System.getProperty("user.home") + "/Projects/GWASpi/var/dataStore/testing/datacenter"); // HACK

		OperationKey parentOperationKey = null;
		try {
			// Look for ANY QA-Markers operation in the DB
			List<StudyKey> studies = StudyList.getStudies();
			studiesLoop : for (StudyKey studyKey : studies) {
				List<MatrixKey> matrices = MatricesList.getMatrixList(studyKey);
				for (MatrixKey matrixKey : matrices) {
					List<OperationMetadata> qaOperationsMetadatas = OperationsList.getOffspringOperationsMetadata(matrixKey, OPType.MARKER_QA);
					if (!qaOperationsMetadatas.isEmpty()) {
						parentOperationKey = OperationKey.valueOf(qaOperationsMetadatas.get(0));
						break studiesLoop;
					}
				}
			}
		} catch (IOException ex) {
			LOG.error("Failed to look for QA Marker operations", ex);
		}

		if (parentOperationKey == null) {
			LOG.warn("No suitable QA Marker operation found that could be used as parent");
			parentOperationKey = new OperationKey(new MatrixKey(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID), OperationKey.NULL_ID);
		}

//		CombiTestOperationParams inputParams = new CombiTestOperationParams(parentOperationKey);
		CombiTestOperationParams inputParams = new CombiTestOperationParams(
				parentOperationKey,
				NominalGenotypeEncoder.SINGLETON,
				35, // weightsFIlterWidth
				20, // markersToKeep
				Boolean.TRUE, // useThresholdCalibration
				"my name is... my name is... my name is ..");
		CombiTestOperationParams outputParams = chooseCombiTestParams(null, inputParams);
	}
}
