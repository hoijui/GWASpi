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
import org.gwaspi.gui.utils.AbsolutePercentageComponent;
import org.gwaspi.gui.utils.AbsolutePercentageModel;
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

	public static final String TITLE = "Edit Combi-Test parameters";

	private CombiTestOperationParams originalCombiTestParams;

	private final JLabel parentMatrixLabel;
	private final JTextField parentMatrixValue;

	private final JLabel qaMarkersOperationLabel; // TODO This is not yet setup correctly! see hw* stuff below for example!
	private final JComboBox qaMarkersOperationValue;

	private final JLabel genotypeEncoderLabel;
	private final JPanel genotypeEncoderP;
	private final JComboBox genotypeEncoderValue;
	private final JCheckBox genotypeEncoderDefault;

	private final JLabel weightsFilterWidthLabel;
	private final AbsolutePercentageComponent weightsFilterWidthValue;

	private final JLabel markersToKeepLabel;
	private final AbsolutePercentageComponent markersToKeepValue;

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

		this.genotypeEncoderLabel = new JLabel();
		this.genotypeEncoderP = new JPanel();
		this.genotypeEncoderValue = new JComboBox();
		this.genotypeEncoderDefault = new JCheckBox();

		this.weightsFilterWidthLabel = new JLabel();
		this.weightsFilterWidthValue = new AbsolutePercentageComponent();

		this.markersToKeepLabel = new JLabel();
		this.markersToKeepValue = new AbsolutePercentageComponent();

		this.useThresholdCalibrationLabel = new JLabel();
		this.useThresholdCalibrationP = new JPanel();
		this.useThresholdCalibrationValue = new JCheckBox();
		this.useThresholdCalibrationDefault = new JLabel();

		this.resultMatrixLabel = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixValue = new JTextField();
		this.resultMatrixDefault = new JCheckBox();

		// pre-configure the GUI components
		this.genotypeEncoderP.add(this.genotypeEncoderValue);
		this.genotypeEncoderP.add(this.genotypeEncoderDefault);

		this.useThresholdCalibrationP.add(this.useThresholdCalibrationValue);
		this.useThresholdCalibrationP.add(this.useThresholdCalibrationDefault);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(parentMatrixLabel, parentMatrixValue);
		labelsAndComponents.put(qaMarkersOperationLabel, qaMarkersOperationValue);
		labelsAndComponents.put(genotypeEncoderLabel, genotypeEncoderP);
		labelsAndComponents.put(weightsFilterWidthLabel, weightsFilterWidthValue);
		labelsAndComponents.put(markersToKeepLabel, markersToKeepValue);
		labelsAndComponents.put(useThresholdCalibrationLabel, useThresholdCalibrationP);
		labelsAndComponents.put(resultMatrixLabel, resultMatrixP);
		createLayout(this, labelsAndComponents);

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

		this.weightsFilterWidthLabel.setText("weights filter kernel width");
		this.weightsFilterWidthLabel.setLabelFor(this.weightsFilterWidthValue);

		this.markersToKeepLabel.setText("number of markers to keep");
		this.markersToKeepLabel.setLabelFor(this.markersToKeepValue);

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

	public void setParentCandidates(List<OperationKey> parentCandidates) {

		OperationKey previouslySelected = (OperationKey) qaMarkersOperationValue.getSelectedItem();
		qaMarkersOperationValue.removeAllItems();
		for (OperationKey parentCandidate : parentCandidates) {
			qaMarkersOperationValue.addItem(parentCandidate);
		}
		if (previouslySelected != null) {
			qaMarkersOperationValue.setSelectedItem(previouslySelected);
		}
		qaMarkersOperationValue.setEnabled(parentCandidates.size() > 0);
		qaMarkersOperationValue.setEditable(parentCandidates.size() > 1);
	}

	public void setCombiTestParams(CombiTestOperationParams combiTestParams) {

		this.originalCombiTestParams = combiTestParams;

		final int totalMarkers = (combiTestParams.getTotalMarkers() < 1) ? 100000 : combiTestParams.getTotalMarkers(); // HACK for testing purposes only, we shoudl probably rather produce an exception here

		qaMarkersOperationValue.setSelectedItem(combiTestParams.getQAMarkerOperationKey());

		genotypeEncoderValue.setModel(new DefaultComboBoxModel(CombiTestScriptCommand.GENOTYPE_ENCODERS.values().toArray()));
		genotypeEncoderValue.setSelectedItem(combiTestParams.getEncoder());
		genotypeEncoderDefault.setAction(new ComboBoxDefaultAction(genotypeEncoderValue, CombiTestOperationParams.getEncoderDefault()));

		weightsFilterWidthValue.setModel(new AbsolutePercentageModel(
				combiTestParams.getWeightsFilterWidth(),
				combiTestParams.getWeightsFilterWidthDefault(),
				totalMarkers,
				1,
				1,
				totalMarkers - 1));

		markersToKeepValue.setModel(new AbsolutePercentageModel(
				combiTestParams.getMarkersToKeep(),
				combiTestParams.getMarkersToKeepDefault(),
				totalMarkers,
				1,
				1,
				totalMarkers - 1));

		useThresholdCalibrationValue.setSelected(combiTestParams.isUseThresholdCalibration());

		resultMatrixValue.setText(combiTestParams.getName());
		resultMatrixDefault.setAction(new TextDefaultAction(resultMatrixValue, combiTestParams.getNameDefault()));

		validate();
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

	public CombiTestOperationParams getCombiTestParams() {

		CombiTestOperationParams combiTestParams = new CombiTestOperationParams(
				(OperationKey) qaMarkersOperationValue.getSelectedItem(),
				(GenotypeEncoder) genotypeEncoderValue.getSelectedItem(),
				(Integer) weightsFilterWidthValue.getValue(),
				(Integer) markersToKeepValue.getValue(),
				useThresholdCalibrationValue.isSelected(),
				resultMatrixValue.getText()
				);

		return combiTestParams;
	}

	public static CombiTestOperationParams chooseCombiTestParams(Component parentComponent, CombiTestOperationParams combiTestParams, List<OperationKey> parentCandidates) {

		CombiTestParamsGUI combiTestParamsGUI = new CombiTestParamsGUI();
		combiTestParamsGUI.setParentCandidates(parentCandidates);
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

		List<OperationKey> parentCandidates = new ArrayList<OperationKey>();
		try {
			// Look for ANY QA-Markers operation in the DB
			List<StudyKey> studies = StudyList.getStudies();
			studiesLoop : for (StudyKey studyKey : studies) {
				List<MatrixKey> matrices = MatricesList.getMatrixList(studyKey);
				for (MatrixKey matrixKey : matrices) {
					List<OperationMetadata> qaOperationsMetadatas = OperationsList.getOffspringOperationsMetadata(matrixKey, OPType.MARKER_QA);
					for (OperationMetadata qaOperationsMetadata : qaOperationsMetadatas) {
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

//		CombiTestOperationParams inputParams = new CombiTestOperationParams(parentOperationKey);
		CombiTestOperationParams inputParams = new CombiTestOperationParams(
				parentOperationKey,
				NominalGenotypeEncoder.SINGLETON,
				35, // weightsFIlterWidth
				20, // markersToKeep
				Boolean.TRUE, // useThresholdCalibration
				"my name is... my name is... my name is ..");
		CombiTestOperationParams outputParams = chooseCombiTestParams(null, inputParams, parentCandidates);
	}
}
