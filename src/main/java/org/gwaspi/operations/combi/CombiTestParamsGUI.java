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
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gwaspi.cli.CombiTestScriptCommand;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.gui.utils.ComboBoxDefaultAction;
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

	private final JLabel useThresholdCalibrationLabel;
	private final JPanel useThresholdCalibrationP;
	private final JCheckBox useThresholdCalibrationValue;
	private final JLabel useThresholdCalibrationWarning;

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

		this.useThresholdCalibrationLabel = new JLabel();
		this.useThresholdCalibrationP = new JPanel();
		this.useThresholdCalibrationValue = new JCheckBox();
		this.useThresholdCalibrationWarning = new JLabel();

		this.resultMatrixLabel = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixValue = new JTextField();
		this.resultMatrixDefault = new JCheckBox();

		// pre-configure the GUI components
		this.genotypeEncoderP.add(this.genotypeEncoderValue);
		this.genotypeEncoderP.add(this.genotypeEncoderDefault);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationWarning);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixLabel, parentMatrixValue);
		labelsAndComponents.put(qaMarkersOperationLabel, qaMarkersOperationValue);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(useThresholdCalibrationLabel, useThresholdCalibrationP);
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

		genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		genotypeEncoderValue.setSelectedItem(params.getEncoder());
		genotypeEncoderDefault.setAction(new ComboBoxDefaultAction(genotypeEncoderValue, CombiTestOperationParams.getEncoderDefault()));

		useThresholdCalibrationValue.setSelected(params.isUseThresholdCalibration());

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

	public CombiTestOperationParams getParams() {

		CombiTestOperationParams params = new CombiTestOperationParams(
				(OperationKey) qaMarkersOperationValue.getSelectedItem(),
				(GenotypeEncoder) genotypeEncoderValue.getSelectedItem(),
				useThresholdCalibrationValue.isSelected(),
				resultMatrixValue.getText()
				);

		return params;
	}

	public static CombiTestOperationParams chooseParams(Component parentComponent, CombiTestOperationParams paramsInitialValues, List<OperationKey> parentCandidates) {

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
				Boolean.TRUE, // useThresholdCalibration
				"my name is... my name is... my name is ..");
		CombiTestOperationParams outputParams = chooseParams(null, inputParams, parentCandidates);
	}
}
