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
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.gui.utils.AbsolutePercentageComponent;
import org.gwaspi.gui.utils.AbsolutePercentageModel;
import org.gwaspi.gui.utils.TextDefaultAction;
import org.gwaspi.model.DataSetKey;
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
public class ByCombiWeightsFilterOperationParamsEditor extends JPanel {

	private static final Logger LOG = LoggerFactory.getLogger(ByCombiWeightsFilterOperationParamsEditor.class);

	public static final String TITLE = "Edit Filter-By-COMBI-Test-Weights parameters";

	private ByCombiWeightsFilterOperationParams originalParams;

	private final JLabel combiOperationLabel;
	private final JComboBox combiOperationValue;

	private final JLabel perChromosomeLabel;
	private final JPanel perChromosomeP;
	private final JCheckBox perChromosomeValue;

	private final JLabel weightsFilterWidthLabel;
	private final AbsolutePercentageComponent weightsFilterWidthValue;

	private final JLabel markersToKeepLabel;
	private final AbsolutePercentageComponent markersToKeepValue;

	private final JLabel resultMatrixLabel;
	private final JPanel resultMatrixP;
	private final JTextField resultMatrixValue;
	private final JCheckBox resultMatrixDefault;

	public ByCombiWeightsFilterOperationParamsEditor() {

		this.originalParams = null;

		// init the GUI components
		this.combiOperationLabel = new JLabel();
		this.combiOperationValue = new JComboBox();

		this.perChromosomeLabel = new JLabel();
		this.perChromosomeP = new JPanel();
		this.perChromosomeValue = new JCheckBox();

		this.weightsFilterWidthLabel = new JLabel();
		this.weightsFilterWidthValue = new AbsolutePercentageComponent();

		this.markersToKeepLabel = new JLabel();
		this.markersToKeepValue = new AbsolutePercentageComponent();

		this.resultMatrixLabel = new JLabel();
		this.resultMatrixP = new JPanel();
		this.resultMatrixValue = new JTextField();
		this.resultMatrixDefault = new JCheckBox();

		// pre-configure the GUI components
		this.perChromosomeP.add(this.perChromosomeValue);

		this.resultMatrixP.add(this.resultMatrixValue);
		this.resultMatrixP.add(this.resultMatrixDefault);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(combiOperationLabel, combiOperationValue);
		labelsAndComponents.put(perChromosomeLabel, perChromosomeP);
		labelsAndComponents.put(weightsFilterWidthLabel, weightsFilterWidthValue);
		labelsAndComponents.put(markersToKeepLabel, markersToKeepValue);
		labelsAndComponents.put(resultMatrixLabel, resultMatrixP);
		GroupLayout layout = new GroupLayout(this);
		CombiTestParamsGUI.createLayout(layout, labelsAndComponents);
		this.setLayout(layout);

		FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);

		this.combiOperationLabel.setText("Parent data source");
		this.combiOperationLabel.setToolTipText("As this has to be a QA Markers operation, only these are displayed in this list");

		this.perChromosomeLabel.setText("run per chromosome");
		this.perChromosomeLabel.setLabelFor(this.perChromosomeValue);
		this.perChromosomeP.setLayout(contentPanelLayout);
		final String perChromosomeTooltip = "smooth and filter once per chromosome (or genome wide)";
		this.perChromosomeLabel.setToolTipText(perChromosomeTooltip);
		this.perChromosomeValue.setToolTipText(perChromosomeTooltip);
		this.perChromosomeP.setToolTipText(perChromosomeTooltip);

		this.weightsFilterWidthLabel.setText("weights filter kernel width");
		this.weightsFilterWidthLabel.setLabelFor(this.weightsFilterWidthValue);

		this.markersToKeepLabel.setText("number of markers to keep");
		this.markersToKeepLabel.setLabelFor(this.markersToKeepValue);

		this.resultMatrixLabel.setText("Result matrix name");
		this.resultMatrixLabel.setLabelFor(this.resultMatrixValue);
		this.resultMatrixP.setLayout(contentPanelLayout);
	}

	public void setParentCandidates(List<OperationKey> parentCandidates) {

		// reset from whatever was set before
		OperationKey previouslySelected = (OperationKey) combiOperationValue.getSelectedItem();
		combiOperationValue.removeAllItems();

		if ((parentCandidates == null) || parentCandidates.isEmpty()) {
			combiOperationValue.setEnabled(false);
			return;
		}

		// set the new candidates
		for (OperationKey parentCandidate : parentCandidates) {
			combiOperationValue.addItem(parentCandidate);
		}
		if (previouslySelected != null) {
			combiOperationValue.setSelectedItem(previouslySelected);
		}
		combiOperationValue.setEnabled(parentCandidates.size() > 0);
		combiOperationValue.setEditable(parentCandidates.size() > 1);
	}

	public void setParams(ByCombiWeightsFilterOperationParams params) {

		this.originalParams = params;

		final int totalMarkers = (params.getTotalMarkers() < 1) ? 100000 : params.getTotalMarkers(); // HACK for testing purposes only, we shoudl probably rather produce an exception here

		combiOperationValue.setSelectedItem(params.getParent().getOperationParent());

		perChromosomeValue.setSelected(params.isPerChromosome());

		weightsFilterWidthValue.setModel(new AbsolutePercentageModel(
				params.getWeightsFilterWidth(),
				params.getWeightsFilterWidthDefault(),
				totalMarkers,
				1,
				1,
				totalMarkers - 1));

		markersToKeepValue.setModel(new AbsolutePercentageModel(
				params.getMarkersToKeep(totalMarkers),
				params.getMarkersToKeepDefault(),
				totalMarkers,
				1,
				1,
				totalMarkers - 1));

		perChromosomeValue.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent evt) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			}
		});

		resultMatrixValue.setText(params.getName());
		resultMatrixDefault.setAction(new TextDefaultAction(resultMatrixValue, params.getNameDefault()));

		validate();
		repaint();
	}

	public ByCombiWeightsFilterOperationParams getParams() {

		ByCombiWeightsFilterOperationParams params = new ByCombiWeightsFilterOperationParams(
				originalParams.getTotalMarkers(),
				(OperationKey) combiOperationValue.getSelectedItem(),
				perChromosomeValue.isSelected(),
				(Integer) weightsFilterWidthValue.getValue(),
				null, // markersToKeep absolute
				(Double) markersToKeepValue.getFractionValue(),
				resultMatrixValue.getText()
				);

		return params;
	}

	public static ByCombiWeightsFilterOperationParams chooseParams(Component parentComponent, ByCombiWeightsFilterOperationParams paramsInitialValues, List<OperationKey> parentCandidates) {

		ByCombiWeightsFilterOperationParamsEditor paramsEditor = new ByCombiWeightsFilterOperationParamsEditor();
		paramsEditor.setParentCandidates(parentCandidates);
		paramsEditor.setParams(paramsInitialValues);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				paramsEditor,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		ByCombiWeightsFilterOperationParams returnParams;
		if (selectedValue == JOptionPane.OK_OPTION) {
			returnParams = paramsEditor.getParams();
		} else {
//			// return the original parameters,
//			// if the user clicked on the [Cancel] button
//			returnParams = params;
			returnParams = null;
		}

		return returnParams;
	}

	public static void main(String[] args) throws IOException {

		Config.createSingleton(true);
		Config.setDBSystemDir(System.getProperty("user.home") + "/Projects/GWASpi/var/dataStore/testing/datacenter"); // HACK

		List<OperationKey> parentCandidates = new ArrayList<OperationKey>();
		try {
			// Look for ANY QA-Markers operation in the DB
			List<StudyKey> studies = StudyList.getStudyService().getStudies();
			studiesLoop : for (StudyKey studyKey : studies) {
				List<MatrixKey> matrices = MatricesList.getMatrixService().getMatrixKeys(studyKey);
				for (MatrixKey matrixKey : matrices) {
					List<OperationMetadata> parentCandidatesMetadatas = OperationsList.getOperationService().getOffspringOperationsMetadata(new DataSetKey(matrixKey), OPType.COMBI_ASSOC_TEST);
					for (OperationMetadata qaOperationsMetadata : parentCandidatesMetadatas) {
						parentCandidates.add(OperationKey.valueOf(qaOperationsMetadata));
					}
				}
			}
		} catch (IOException ex) {
			LOG.error("Failed to look for COMBI Test operations", ex);
		}

		final OperationKey parentOperationKey;
		final int totalMarkers;
		if (parentCandidates.isEmpty()) {
			LOG.warn("No suitable COMBI Test operation found that could be used as parent");
			parentOperationKey = new OperationKey(new MatrixKey(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID), OperationKey.NULL_ID);
			totalMarkers = 100000;
		} else {
			parentOperationKey = parentCandidates.get(0);
			totalMarkers = OperationsList.getOperationService().getOperationMetadata(parentOperationKey).getNumMarkers();
		}

		ByCombiWeightsFilterOperationParams inputParams = new ByCombiWeightsFilterOperationParams(
				totalMarkers,
				parentOperationKey,
				true, // perChromosome
				35, // weightsFilterWidth
				null, // markersToKeep
				null, // markersToKeepFraction
				"my name is... my name is... my name is ..");
		ByCombiWeightsFilterOperationParams outputParams = chooseParams(null, inputParams, parentCandidates);
	}
}
