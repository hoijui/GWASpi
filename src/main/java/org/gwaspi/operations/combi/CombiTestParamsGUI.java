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
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.gui.utils.CheckBoxDefaultAction;
import org.gwaspi.gui.utils.ComboBoxDefaultAction;
import org.gwaspi.gui.utils.SpinnerDefaultAction;
import org.gwaspi.gui.utils.TextDefaultAction;
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

	public static final String TITLE = "Edit COMBI Test parameters";

	private CombiTestOperationParams originalParams;

	private final JLabel parentMatrixLabel;
	private final JTextField parentMatrixValue;

	private final JLabel qaMarkersOperationLabel;
	private final JComboBox qaMarkersOperationValue;

	private final JLabel genotypeEncoderLabel;
	private final JPanel genotypeEncoderP;
	private final JComboBox genotypeEncoderValue;
	private final JCheckBox genotypeEncoderDefault;

	private final JLabel featureScalingPLabel;
	private final JPanel featureScalingPPanel;
	private final JSpinner featureScalingPValue;
	private final JCheckBox featureScalingPDefault;

	private final JLabel weightsDecodingPLabel;
	private final JPanel weightsDecodingPPanel;
	private final JSpinner weightsDecodingPValue;
	private final JCheckBox weightsDecodingPDefault;

	private final JLabel useThresholdCalibrationLabel;
	private final JPanel useThresholdCalibrationP;
	private final JCheckBox useThresholdCalibrationValue;
	private final JLabel useThresholdCalibrationWarning;

	private final JLabel perChromosomeLabel;
	private final JPanel perChromosomeP;
	private final JCheckBox perChromosomeValue;
	private final JCheckBox perChromosomeDefault;

	private final JLabel svmLibraryLabel;
	private final JPanel svmLibraryP;
	private final JComboBox svmLibraryValue;
	private final JCheckBox svmLibraryDefault;

	private final JLabel svmEpsLabel;
	private final JPanel svmEpsP;
	private final JSpinner svmEpsValue;
	private final JCheckBox svmEpsDefault;

	private final JLabel svmCLabel;
	private final JPanel svmCP;
	private final JSpinner svmCValue;
	private final JCheckBox svmCDefault;

	private final JLabel resultMatrixLabel;
	private final JPanel resultMatrixP;
	private final JTextField resultMatrixValue;
	private final JCheckBox resultMatrixDefault;

	public CombiTestParamsGUI() {

		this.originalParams = null;

		// init the GUI components
		this.parentMatrixLabel = new JLabel();
		this.parentMatrixValue = new JTextField();

		this.qaMarkersOperationLabel = new JLabel();
		this.qaMarkersOperationValue = new JComboBox();

		this.genotypeEncoderLabel = new JLabel();
		this.genotypeEncoderP = new JPanel();
		this.genotypeEncoderValue = new JComboBox();
		this.genotypeEncoderDefault = new JCheckBox();

		this.featureScalingPLabel = new JLabel();
		this.featureScalingPPanel = new JPanel();
		this.featureScalingPValue = new JSpinner();
		this.featureScalingPDefault = new JCheckBox();

		this.weightsDecodingPLabel = new JLabel();
		this.weightsDecodingPPanel = new JPanel();
		this.weightsDecodingPValue = new JSpinner();
		this.weightsDecodingPDefault = new JCheckBox();

		this.useThresholdCalibrationLabel = new JLabel();
		this.useThresholdCalibrationP = new JPanel();
		this.useThresholdCalibrationValue = new JCheckBox();
		this.useThresholdCalibrationWarning = new JLabel();

		this.perChromosomeLabel = new JLabel();
		this.perChromosomeP = new JPanel();
		this.perChromosomeValue = new JCheckBox();
		this.perChromosomeDefault = new JCheckBox();

		this.svmLibraryLabel = new JLabel();
		this.svmLibraryP = new JPanel();
		this.svmLibraryValue = new JComboBox();
		this.svmLibraryDefault = new JCheckBox();

		this.svmEpsLabel = new JLabel();
		this.svmEpsP = new JPanel();
		this.svmEpsValue = new JSpinner();
		this.svmEpsDefault = new JCheckBox();

		this.svmCLabel = new JLabel();
		this.svmCP = new JPanel();
		this.svmCValue = new JSpinner();
		this.svmCDefault = new JCheckBox();

		this.resultMatrixLabel = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixValue = new JTextField();
		this.resultMatrixDefault = new JCheckBox();

		// pre-configure the GUI components
		this.genotypeEncoderP.add(this.genotypeEncoderValue);
		this.genotypeEncoderP.add(this.genotypeEncoderDefault);

		this.featureScalingPPanel.add(this.featureScalingPValue);
		this.featureScalingPPanel.add(this.featureScalingPDefault);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationWarning);

		this.perChromosomeP.add(this.perChromosomeValue);
		this.perChromosomeP.add(this.perChromosomeDefault);

		this.svmLibraryP.add(this.svmLibraryValue);
		this.svmLibraryP.add(this.svmLibraryDefault);

		this.svmEpsP.add(this.svmEpsValue);
		this.svmEpsP.add(this.svmEpsDefault);

		this.svmCP.add(this.svmCValue);
		this.svmCP.add(this.svmCDefault);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixLabel, parentMatrixValue);
		labelsAndComponents.put(qaMarkersOperationLabel, qaMarkersOperationValue);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(featureScalingPLabel, featureScalingPPanel);
		labelsAndComponents.put(weightsDecodingPLabel, weightsDecodingPPanel);
		labelsAndComponents.put(useThresholdCalibrationLabel, useThresholdCalibrationP);
		labelsAndComponents.put(perChromosomeLabel, perChromosomeP);
		labelsAndComponents.put(svmLibraryLabel, svmLibraryP);
		labelsAndComponents.put(svmEpsLabel, svmEpsP);
		labelsAndComponents.put(svmCLabel, svmCP);
		labelsAndComponents.put(resultMatrixLabel, resultMatrixP);
		GroupLayout layout = new GroupLayout(this);
		createLayout(layout, labelsAndComponents);
		this.setLayout(layout);

		FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);

		this.parentMatrixLabel.setText("parent matrix");
		this.parentMatrixLabel.setLabelFor(this.parentMatrixValue);
		this.parentMatrixValue.setEditable(false);

		this.qaMarkersOperationLabel.setText("Parent data source");
		this.qaMarkersOperationLabel.setToolTipText("As this has to be a QA Markers operation, only these are displayed in this list");

//		this.hwThresholdValue.setToolTipText("Discard markers with Hardy-Weinberg p-value smaller then this value");
//		this.hwThresholdPercentage.setToolTipText("Discard markers with Hardy-Weinberg p-value smaller then this value / #markers");

		this.genotypeEncoderLabel.setText("geno-type to SVN feature encoding");
		this.genotypeEncoderLabel.setLabelFor(this.genotypeEncoderValue);
		this.genotypeEncoderP.setLayout(contentPanelLayout);
		this.genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));

		this.featureScalingPLabel.setText("feature scaling 'p'");
		this.featureScalingPLabel.setLabelFor(this.featureScalingPValue);
		this.featureScalingPPanel.setLayout(contentPanelLayout);
		this.featureScalingPValue.setModel(new SpinnerNumberModel(
				CombiTestOperationParams.getEncodingParamsDefault().getFeatureScalingP(),
				-100.0, 100.0, 1.0));
		final String featureScalingPTooltip
				= "p parameter used to calculate the standard deviation "
				+ "used for whitening the feature matrix";
		this.featureScalingPLabel.setToolTipText(featureScalingPTooltip);
		this.featureScalingPValue.setToolTipText(featureScalingPTooltip);
		this.featureScalingPPanel.setToolTipText(featureScalingPTooltip);

		this.weightsDecodingPLabel.setText("weights decoding 'p'");
		this.weightsDecodingPLabel.setLabelFor(this.weightsDecodingPValue);
		this.weightsDecodingPPanel.setLayout(contentPanelLayout);
		this.weightsDecodingPValue.setModel(new SpinnerNumberModel(
				CombiTestOperationParams.getEncodingParamsDefault().getWeightsDecodingP(),
				-100.0, 100.0, 1.0));
		final String weightsDecodingPTooltip
				= "p parameter used to calculate the standard deviation "
				+ "used when decoding the weights from feature- into marker-space";
		this.weightsDecodingPLabel.setToolTipText(weightsDecodingPTooltip);
		this.weightsDecodingPValue.setToolTipText(weightsDecodingPTooltip);
		this.weightsDecodingPPanel.setToolTipText(weightsDecodingPTooltip);

		this.useThresholdCalibrationLabel.setText("use resampling based threshold calibration");
		this.useThresholdCalibrationLabel.setLabelFor(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.setLayout(contentPanelLayout);
		this.useThresholdCalibrationWarning.setText("");
		this.useThresholdCalibrationWarning.setForeground(Color.RED);
		this.useThresholdCalibrationValue.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent evt) {
				if (useThresholdCalibrationValue.isSelected()) {
					useThresholdCalibrationWarning.setText("very slow!");
				} else {
					useThresholdCalibrationWarning.setText("");
				}
			}
		});
		// TODO XXX implement resampling based threshold calibration and remove these lines!
		final String notYetImplemented = "Not yet implemented!";
		this.useThresholdCalibrationLabel.setToolTipText(notYetImplemented);
		this.useThresholdCalibrationValue.setToolTipText(notYetImplemented);
		this.useThresholdCalibrationP.setToolTipText(notYetImplemented);
		this.useThresholdCalibrationLabel.setEnabled(false);
		this.useThresholdCalibrationValue.setEnabled(false);
		this.useThresholdCalibrationWarning.setEnabled(false);

		this.perChromosomeLabel.setText("run per chromosome");
		this.perChromosomeLabel.setLabelFor(this.perChromosomeValue);
		this.perChromosomeP.setLayout(contentPanelLayout);
		this.perChromosomeDefault.setEnabled(false);
		final String perChromosomeTooltip = "train SVM once per chromosome (or genome wide)";
		this.perChromosomeLabel.setToolTipText(perChromosomeTooltip);
		this.perChromosomeValue.setToolTipText(perChromosomeTooltip);
		this.perChromosomeP.setToolTipText(perChromosomeTooltip);

		this.svmLibraryLabel.setText("Solver library");
		this.svmLibraryLabel.setLabelFor(this.svmLibraryValue);
		this.svmLibraryP.setLayout(contentPanelLayout);
		final String svmLibraryTooltip
				= "which (SVM) solver library to use for generating the marker weights";
		this.svmLibraryLabel.setToolTipText(svmLibraryTooltip);
		this.svmLibraryValue.setToolTipText(svmLibraryTooltip);
		this.svmLibraryP.setToolTipText(svmLibraryTooltip);

		final SolverParams solverParamsDefault
				= CombiTestOperationParams.getSolverParamsDefault(
						CombiTestOperationParams.getSolverLibraryDefault());
		this.svmEpsLabel.setText("SVM 'epsilon'");
		this.svmEpsLabel.setLabelFor(this.svmEpsValue);
		this.svmEpsP.setLayout(contentPanelLayout);
		this.svmEpsValue.setModel(new SpinnerNumberModel(
				solverParamsDefault.getEps(), 1E-20, 1E-2, 1E-7));
		this.svmEpsValue.setEditor(new JSpinner.NumberEditor(this.svmEpsValue, "0.0######E0"));
		final String svmEpsTooltip
				= "which epsilon value to use to train the SVM solver; "
				+ "how closely should the data be fitted with the model";
		this.svmEpsLabel.setToolTipText(svmEpsTooltip);
		this.svmEpsValue.setToolTipText(svmEpsTooltip);
		this.svmEpsP.setToolTipText(svmEpsTooltip);

		this.svmCLabel.setText("SVM 'C'");
		this.svmCLabel.setLabelFor(this.svmCValue);
		this.svmCP.setLayout(contentPanelLayout);
		this.svmCValue.setModel(new SpinnerNumberModel(
				solverParamsDefault.getC(), 1E-20, 1.0, 1E-5));
		this.svmCValue.setEditor(new JSpinner.NumberEditor(this.svmCValue, "0.0######E0"));
		final String svmCTooltip = "which C value to use to train the SVM solver";
		this.svmCLabel.setToolTipText(svmCTooltip);
		this.svmCValue.setToolTipText(svmCTooltip);
		this.svmCP.setToolTipText(svmCTooltip);

		this.resultMatrixLabel.setText("Result matrix name");
		this.resultMatrixLabel.setLabelFor(this.resultMatrixValue);
		this.resultMatrixP.setLayout(contentPanelLayout);
	}

	public void setParentCandidates(List<OperationKey> parentCandidates) {

		// reset from whatever was set before
		OperationKey previouslySelected = (OperationKey) qaMarkersOperationValue.getSelectedItem();
		qaMarkersOperationValue.removeAllItems();

		if ((parentCandidates == null) || parentCandidates.isEmpty()) {
			qaMarkersOperationValue.setEnabled(false);
			return;
		}

		// set the new candidates
		qaMarkersOperationValue.setEnabled(true);
		for (OperationKey parentCandidate : parentCandidates) {
			qaMarkersOperationValue.addItem(parentCandidate);
		}
		if (previouslySelected != null) {
			qaMarkersOperationValue.setSelectedItem(previouslySelected);
		}
		qaMarkersOperationValue.setEnabled(parentCandidates.size() > 0);
		qaMarkersOperationValue.setEditable(parentCandidates.size() > 1);
	}

	public void setCombiTestParams(CombiTestOperationParams params) {

		this.originalParams = params;

		qaMarkersOperationValue.setSelectedItem(params.getQAMarkerOperationKey());

		genotypeEncoderValue.setModel(new DefaultComboBoxModel(
				CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		genotypeEncoderValue.setSelectedItem(params.getEncoder());
		genotypeEncoderDefault.setAction(new ComboBoxDefaultAction(genotypeEncoderValue,
				CombiTestOperationParams.getEncoderDefault()));

		featureScalingPValue.setValue(params.getEncodingParams().getFeatureScalingP());
		featureScalingPDefault.setAction(new SpinnerDefaultAction(featureScalingPValue,
				CombiTestOperationParams.getEncodingParamsDefault().getFeatureScalingP()));

		weightsDecodingPValue.setValue(params.getEncodingParams().getWeightsDecodingP());
		weightsDecodingPDefault.setAction(new SpinnerDefaultAction(weightsDecodingPValue,
				CombiTestOperationParams.getEncodingParamsDefault().getWeightsDecodingP()));

		useThresholdCalibrationValue.setSelected(params.isUseThresholdCalibration());

		perChromosomeValue.setSelected(params.isPerChromosome());
		perChromosomeDefault.setAction(new CheckBoxDefaultAction(perChromosomeValue,
				CombiTestOperationParams.isPerChromosomeDefault()));
		perChromosomeDefault.setEnabled(false);

		svmLibraryValue.setModel(new DefaultComboBoxModel(SolverLibrary.values()));
		svmLibraryValue.setSelectedItem(params.getSolverLibrary());
		svmLibraryValue.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent evt) {

				final SpinnerDefaultAction actionEps
						= (SpinnerDefaultAction) svmEpsDefault.getAction();
				final SpinnerDefaultAction actionC
						= (SpinnerDefaultAction) svmCDefault.getAction();

				final SolverParams solverParamsDefault
						= CombiTestOperationParams.getSolverParamsDefault(
								(SolverLibrary) svmLibraryValue.getSelectedItem());

				actionEps.setDefault(solverParamsDefault.getEps());
				actionC.setDefault(solverParamsDefault.getC());
			}

		});
		svmLibraryDefault.setAction(new ComboBoxDefaultAction(svmLibraryValue,
				CombiTestOperationParams.getSolverLibraryDefault()));

		final SolverParams solverParamsDefault
				= CombiTestOperationParams.getSolverParamsDefault(params.getSolverLibrary());
		svmEpsValue.setValue(params.getSolverParams().getEps());
		svmEpsDefault.setAction(new SpinnerDefaultAction(svmEpsValue, solverParamsDefault.getEps()));

		svmCValue.setValue(params.getSolverParams().getC());
		svmCDefault.setAction(new SpinnerDefaultAction(svmCValue, solverParamsDefault.getC()));

		resultMatrixValue.setText(params.getName());
		resultMatrixDefault.setAction(new TextDefaultAction(resultMatrixValue, params.getNameDefault()));

		validate();
		repaint();
	}

	static void createLayout(GroupLayout layout, Map<JLabel, JComponent> labelsAndComponents) {

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup horizontalG = layout.createSequentialGroup();
		GroupLayout.ParallelGroup horizontalLabelsG = layout.createParallelGroup();
		GroupLayout.ParallelGroup horizontalComponentsG = layout.createParallelGroup();
		for (Map.Entry<JLabel, JComponent> labelAndComponent : labelsAndComponents.entrySet()) {
			horizontalLabelsG.addComponent(labelAndComponent.getKey(),
					GroupLayout.Alignment.TRAILING);
			// The following is better done manually, earlier
//			labelAndComponent.getKey().setLabelFor(labelAndComponent.getValue());
			horizontalComponentsG.addComponent(labelAndComponent.getValue(),
					GroupLayout.Alignment.LEADING);
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

	public CombiTestOperationParams getParams() {

		CombiTestOperationParams params = new CombiTestOperationParams(
				(OperationKey) qaMarkersOperationValue.getSelectedItem(),
				(GenotypeEncoder) genotypeEncoderValue.getSelectedItem(),
				new GenotypeEncodingParams(
						(Double) featureScalingPValue.getValue(),
						(Double) weightsDecodingPValue.getValue()),
				useThresholdCalibrationValue.isSelected(),
				perChromosomeValue.isSelected(),
				(SolverLibrary) svmLibraryValue.getSelectedItem(),
				new SolverParams(
						(Double) svmEpsValue.getValue(),
						(Double) svmCValue.getValue()),
				resultMatrixValue.getText()
				);

		return params;
	}

	public static CombiTestOperationParams chooseParams(
			final Component parentComponent,
			final CombiTestOperationParams paramsInitialValues,
			final List<OperationKey> parentCandidates)
	{
		CombiTestParamsGUI paramsEditor = new CombiTestParamsGUI();
		paramsEditor.setParentCandidates(parentCandidates);
		paramsEditor.setCombiTestParams(paramsInitialValues);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				paramsEditor,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		CombiTestOperationParams returnParams;
		if (selectedValue == JOptionPane.OK_OPTION) {
			returnParams = paramsEditor.getParams();
		} else {
//			// return the original parameters,
//			// if the user clicked on the [Cancel] button
//			returnParams = paramsInitialValues;
			returnParams = null;
		}

		return returnParams;
	}

	public static void main(String[] args) {

		Config.createSingleton(true);
		Config.setDBSystemDir(System.getProperty("user.home") + "/Projects/GWASpi/var/dataStore/testing/datacenter"); // HACK

		List<OperationKey> parentCandidates = new ArrayList<OperationKey>();
		try {
			// Look for ANY QA-Markers operation in the DB
			List<StudyKey> studies = StudyList.getStudies();
			studiesLoop : for (StudyKey studyKey : studies) {
				List<MatrixKey> matrices = MatricesList.getMatrixList(studyKey);
				for (MatrixKey matrixKey : matrices) {
					List<OperationMetadata> parentCandidatesMetadatas = OperationsList.getOffspringOperationsMetadata(matrixKey, OPType.MARKER_QA);
					for (OperationMetadata qaOperationsMetadata : parentCandidatesMetadatas) {
						parentCandidates.add(OperationKey.valueOf(qaOperationsMetadata));
					}
				}
			}
		} catch (IOException ex) {
			LOG.error("Failed to look for QA Marker operations", ex);
		}

		final OperationKey parentOperationKey;
		if (parentCandidates.isEmpty()) {
			LOG.warn("No suitable QA Marker operation found that could be used as parent");
			parentOperationKey = new OperationKey(new MatrixKey(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID), OperationKey.NULL_ID);
		} else {
			parentOperationKey = parentCandidates.get(0);
		}

		CombiTestOperationParams inputParams = new CombiTestOperationParams(
				parentOperationKey,
				NominalGenotypeEncoder.SINGLETON,
				new GenotypeEncodingParams(),
				Boolean.TRUE, // useThresholdCalibration
				Boolean.TRUE, // perChromosome
				SolverLibrary.LIB_SVM,
				new SolverParams(),
				"my name is... my name is... my name is ..");
		CombiTestOperationParams outputParams = chooseParams(null, inputParams, parentCandidates);
	}
}
