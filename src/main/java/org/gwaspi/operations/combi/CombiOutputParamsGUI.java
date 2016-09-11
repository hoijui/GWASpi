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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.global.Config;
import org.gwaspi.gui.utils.SpinnerDefaultAction;
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
public class CombiOutputParamsGUI extends JPanel {

	private static final Logger LOG = LoggerFactory.getLogger(CombiOutputParamsGUI.class);

	public static final String TITLE = "Edit COMBI Output parameters";

	private CombiOutputOperationParams originalParams;

	private final JLabel minPeakDistanceLabel;
	private final JPanel minPeakDistancePanel;
	private final JSpinner minPeakDistanceValue;
	private final JCheckBox minPeakDistanceDefault;

	private final JLabel pValueThresholdLabel;
	private final JPanel pValueThresholdPanel;
	private final JSpinner pValueThresholdValue;
	private final JCheckBox pValueThresholdDefault;

	public CombiOutputParamsGUI() {

		this.originalParams = null;

		// init GUI components
		this.minPeakDistanceLabel = new JLabel();
		this.minPeakDistancePanel = new JPanel();
		this.minPeakDistanceValue = new JSpinner();
		this.minPeakDistanceDefault = new JCheckBox();

		this.pValueThresholdLabel = new JLabel();
		this.pValueThresholdPanel = new JPanel();
		this.pValueThresholdValue = new JSpinner();
		this.pValueThresholdDefault = new JCheckBox();

		// pre-configure the GUI components
		this.minPeakDistancePanel.add(this.minPeakDistanceValue);
		this.minPeakDistancePanel.add(this.minPeakDistanceDefault);

		this.pValueThresholdPanel.add(this.pValueThresholdValue);
		this.pValueThresholdPanel.add(this.pValueThresholdDefault);

		Map<JLabel, JComponent> labelsAndComponents = new LinkedHashMap<JLabel, JComponent>();
		labelsAndComponents.put(minPeakDistanceLabel, minPeakDistancePanel);
		labelsAndComponents.put(pValueThresholdLabel, pValueThresholdPanel);
		GroupLayout layout = new GroupLayout(this);
		CombiTestParamsGUI.createLayout(layout, labelsAndComponents);
		this.setLayout(layout);

		final FlowLayout contentPanelLayout = new FlowLayout();
		contentPanelLayout.setAlignment(FlowLayout.LEADING);

		this.minPeakDistanceLabel.setText("Minimum distance between peaks");
		this.minPeakDistanceLabel.setLabelFor(this.minPeakDistanceValue);
		this.minPeakDistancePanel.setLayout(contentPanelLayout);
		this.minPeakDistanceValue.setModel(new SpinnerNumberModel(
				(int) CombiOutputOperationParams.getMinPeakDistanceDefault(),
				1, 999999, 1));
		final String minPeakDistancePTooltip
				= "Minimum distance between markers for them to be counted as separate peaks";
		this.minPeakDistanceLabel.setToolTipText(minPeakDistancePTooltip);
		this.minPeakDistanceValue.setToolTipText(minPeakDistancePTooltip);
		this.minPeakDistancePanel.setToolTipText(minPeakDistancePTooltip);

		this.pValueThresholdLabel.setText("P-value threashold");
		this.pValueThresholdLabel.setLabelFor(this.pValueThresholdValue);
		this.pValueThresholdPanel.setLayout(contentPanelLayout);
		this.pValueThresholdValue.setModel(new SpinnerNumberModel(
				(int) CombiOutputOperationParams.getMinPeakDistanceDefault(),
				1, 999999, 1));
		final String pValueThresholdPTooltip
				= "Maximum P-value above which we do not consider any values as output-worthy";
		this.pValueThresholdLabel.setToolTipText(pValueThresholdPTooltip);
		this.pValueThresholdValue.setToolTipText(pValueThresholdPTooltip);
		this.pValueThresholdPanel.setToolTipText(pValueThresholdPTooltip);
	}

	public void setParams(final CombiOutputOperationParams params) {

		this.originalParams = params;

		minPeakDistanceValue.setValue(params.getMinPeakDistance());
		minPeakDistanceDefault.setAction(new SpinnerDefaultAction(minPeakDistanceValue,
				CombiOutputOperationParams.getMinPeakDistanceDefault()));

		pValueThresholdValue.setValue(params.getPValueThreasholds().get(0));
		pValueThresholdDefault.setAction(new SpinnerDefaultAction(pValueThresholdValue,
				CombiOutputOperationParams.getPValueThreasholdsDefault().get(0)));

		validate();
		repaint();
	}

	public CombiOutputOperationParams getParams() {

		final CombiOutputOperationParams params = new CombiOutputOperationParams(
				originalParams.getTrendTestOperationKey(),
				originalParams.getCombiOperationKey(),
				(Integer) minPeakDistanceValue.getValue(),
				Collections.singletonList((Double) pValueThresholdValue.getValue()),
				originalParams.getName()
				);

		return params;
	}

	public static CombiOutputOperationParams chooseParams(
			final Component parentComponent,
			final CombiOutputOperationParams paramsInitialValues)
	{
		final CombiOutputParamsGUI paramsEditor = new CombiOutputParamsGUI();
		paramsEditor.setParams(paramsInitialValues);

		int selectedValue = JOptionPane.showConfirmDialog(
				parentComponent,
				paramsEditor,
				TITLE,
				JOptionPane.OK_CANCEL_OPTION);

		CombiOutputOperationParams returnParams;
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

		final List<List<OperationKey>> parentCandidates = new ArrayList<List<OperationKey>>();
		try {
			// Look for ANY COMBI operation in the DB
			final List<StudyKey> studies = StudyList.getStudyService().getStudies();
			studiesLoop : for (final StudyKey studyKey : studies) {
				final List<MatrixKey> matrices = MatricesList.getMatrixService().getMatrixKeys(studyKey);
				for (final MatrixKey matrixKey : matrices) {
					final List<OperationMetadata> combiTests
							= OperationsList.getOperationService().getOffspringOperationsMetadata(new DataSetKey(matrixKey), OPType.COMBI_ASSOC_TEST);
					for (final OperationMetadata combiTest : combiTests) {
						final List<OperationMetadata> trendTests
							= OperationsList.getOperationService().getOffspringOperationsMetadata(combiTest.getDataSetKey(), OPType.TRENDTEST);
						for (final OperationMetadata trendTest : trendTests) {
							final List<OperationKey> combiAndTrendTest = new ArrayList<OperationKey>(2);
							combiAndTrendTest.add(OperationKey.valueOf(combiTest));
							combiAndTrendTest.add(OperationKey.valueOf(trendTest));
							parentCandidates.add(combiAndTrendTest);
						}
					}
				}
			}
		} catch (final IOException ex) {
			LOG.error("Failed to look for QA Marker operations", ex);
		}

		final List<OperationKey> parentOperationKeys;
		if (parentCandidates.isEmpty()) {
			LOG.warn("No suitable QA Marker operation found that could be used as parent");
			final OperationKey nullOpKey = new OperationKey(new MatrixKey(new StudyKey(StudyKey.NULL_ID), MatrixKey.NULL_ID), OperationKey.NULL_ID);
			final List<OperationKey> nullParents = new ArrayList<OperationKey>(2);
			nullParents.add(nullOpKey);
			nullParents.add(nullOpKey);
			parentOperationKeys = nullParents;
		} else {
			parentOperationKeys = parentCandidates.get(0);
		}

		final CombiOutputOperationParams inputParams = new CombiOutputOperationParams(
				parentOperationKeys.get(1),
				parentOperationKeys.get(0),
				10,
				CombiOutputOperationParams.getPValueThreasholdsDefault(),
				"my name is... my name is... my name is ..");
		final CombiOutputOperationParams outputParams = chooseParams(null, inputParams);
	}
}
