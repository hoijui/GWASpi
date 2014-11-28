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

package org.gwaspi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.gwaspi.constants.ImportConstants;
import org.gwaspi.constants.NetCDFConstants.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.NetCDFConstants.Defaults.SetSamplePickCase;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.gui.utils.SelectAllTextFocusListener;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.dataextractor.MatrixDataExtractorParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_ExtractMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetExtractPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(DataSetExtractPanel.class);

	/**
	 * These must match the DB fields == SampleInfo property names.
	 */
	private static final String[] PICKABLE_SAMPLE_FIELDS = new String[] {
		"affection",
		"age",
		"category",
		"disease",
		"familyID",
		"population",
		"sampleID",
		"sex"
	};
	/**
	 * These must match the DB fields == MarkerMetadata property names.
	 */
	private static final String[] PICKABLE_MARKER_FIELDS = new String[] {
		"chr",
		"markerId",
		"rsId"
	};

	private final DataSetKey parentDataSetKey;
	private final List<PickMethod<SetMarkerPickCase>> markerPickerTable;
	private final List<PickMethod<SetSamplePickCase>> samplePickerTable;
	private final JComboBox cmb_MarkersVariable;
	private final JComboBox cmb_SamplesVariable;
	private final JLabel lbl_NewMatrixName;
	private final JTextArea txtA_MarkersCriteria;
	private final JTextArea txtA_NewMatrixDescription;
	private final JTextArea txtA_SamplesCriteria;
	private final JTextField txt_MarkersCriteriaFile;
	private final JTextField txt_NewMatrixName;
	private final JTextField txt_SamplesCriteriaFile;

	public static void main(String[] args) throws IOException {

		final DataSetExtractPanel pane = new DataSetExtractPanel(null, "fictional-new-matrix-name", "fictional-new-matrix-description");
		final JFrame frame = new JFrame("DataSetExtractPanel tester");
		final JScrollPane scrl_container = new JScrollPane();
		scrl_container.setViewportView(pane);
		frame.setLayout(new BorderLayout());
		frame.add(scrl_container, BorderLayout.CENTER);
		frame.setSize(1024, 600);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private static class PickMethod<PCT> {

		private final String name;
		private final PCT pickCase;
		private final String var;

		PickMethod(final String name, final PCT pickCase, final String var) {

			this.name = name;
			this.pickCase = pickCase;
			this.var = var;
		}

		@Override
		public String toString() {
			return getName();
		}

		public String getName() {
			return name;
		}

		public PCT getPickCase() {
			return pickCase;
		}

		public String getVar() {
			return var;
		}
	}

	public DataSetExtractPanel(final DataSetKey parentDataSetKey, String newMatrixName, String newMatrixDesc) throws IOException {

		this.parentDataSetKey = parentDataSetKey;
		final DataSetMetadata dataSetMetadata;
		if (parentDataSetKey == null) {
			dataSetMetadata = null;
		} else {
			dataSetMetadata = MatricesList.getDataSetMetadata(parentDataSetKey);
		}

		this.markerPickerTable = new ArrayList<PickMethod<SetMarkerPickCase>>();
		markerPickerTable.add(new PickMethod<SetMarkerPickCase>(
				"All Markers",
				SetMarkerPickCase.ALL_MARKERS,
				null));
		for (final String pickableMarkerField : PICKABLE_MARKER_FIELDS) {
			markerPickerTable.add(new PickMethod<SetMarkerPickCase>(
				"Exclude by " + pickableMarkerField,
				SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA,
				pickableMarkerField));
		}
		for (final String pickableMarkerField : PICKABLE_MARKER_FIELDS) {
			markerPickerTable.add(new PickMethod<SetMarkerPickCase>(
				"Include by " + pickableMarkerField,
				SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA,
				pickableMarkerField));
		}
		//markerPickerTable.add(new PickMethod<SetMarkerPickCase>("Exclude by Position Window", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_POS));
		//markerPickerTable.add(new PickMethod<SetMarkerPickCase>("Exclude by Strand", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_GT_STRAND));
		//markerPickerTable.add(new PickMethod<SetMarkerPickCase>("Include by Position Window", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_POS));
		//markerPickerTable.add(new PickMethod<SetMarkerPickCase>("Include by Strand", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_GT_STRAND));

		this.samplePickerTable = new ArrayList<PickMethod<SetSamplePickCase>>();
		samplePickerTable.add(new PickMethod<SetSamplePickCase>("All Samples", SetSamplePickCase.ALL_SAMPLES, null));
		for (final String pickableSampleField : PICKABLE_SAMPLE_FIELDS) {
			samplePickerTable.add(new PickMethod<SetSamplePickCase>(
				"Exclude by " + pickableSampleField,
				SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD,
				pickableSampleField));
		}
		for (final String pickableSampleField : PICKABLE_SAMPLE_FIELDS) {
			samplePickerTable.add(new PickMethod<SetSamplePickCase>(
				"Include by " + pickableSampleField,
				SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD,
				pickableSampleField));
		}

		final JPanel pnl_NameAndDesc = new JPanel();
		final JLabel lbl_ParentMatrix = new JLabel();
		final JLabel lbl_ParentMatrixName = new JLabel();
		this.lbl_NewMatrixName = new JLabel();
		this.txt_NewMatrixName = new JTextField(newMatrixName);
		final JScrollPane scrl_NewMatrixDescription = new JScrollPane();
		this.txtA_NewMatrixDescription = new JTextArea(newMatrixDesc);
		final JPanel pnl_MarkerZone = new JPanel();
		final JLabel lbl_MarkersVariable = new JLabel();
		this.cmb_MarkersVariable = new JComboBox();
		final JButton btn_Help = new JButton();
		final JLabel lbl_MarkersCriteria = new JLabel();
		final JScrollPane scrl_MarkersCriteria = new JScrollPane();
		this.txtA_MarkersCriteria = new JTextArea();
		final JLabel lbl_MarkersCriteriaFile = new JLabel();
		this.txt_MarkersCriteriaFile = new JTextField();
		final JButton btn_MarkersCriteriaBrowse = new JButton();
		final JPanel pnl_SampleZone = new JPanel();
		final JLabel lbl_SamplesVariable = new JLabel();
		this.cmb_SamplesVariable = new JComboBox();
		final JLabel lbl_SamplesCriteria = new JLabel();
		final JScrollPane scrl_SamplesCriteria = new JScrollPane();
		this.txtA_SamplesCriteria = new JTextArea();
		final JLabel lbl_SamplesCriteriaFile = new JLabel();
		this.txt_SamplesCriteriaFile = new JTextField();
		final JButton btn_SamplesCriteriaBrowse = new JButton();
		final JPanel pnl_Footer = new JPanel();
		final JButton btn_Back = new JButton();
		final JButton btn_Go = new JButton();

		setBorder(GWASpiExplorerPanel.createMainTitledBorder(Text.Trafo.extractData)); // NOI18N

		pnl_NameAndDesc.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Trafo.extratedMatrixDetails)); // NOI18N
		pnl_MarkerZone.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Trafo.markerSelectZone)); // NOI18N
		pnl_SampleZone.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.Trafo.sampleSelectZone)); // NOI18N

		final GridBagLayout layout = new GridBagLayout();
		final int gapSpace = 5;
		// we use every second row and column as a spacer
		layout.columnWidths = new int[] {0};
		layout.rowHeights = new int[] {0, gapSpace, 0, gapSpace, 0, gapSpace, 0};
		setLayout(layout);

		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.3;

		lbl_ParentMatrix.setText(Text.Matrix.parentMatrix);
		lbl_ParentMatrix.setLabelFor(lbl_ParentMatrixName);
		lbl_ParentMatrixName.setText((dataSetMetadata == null) ? "<no data-set given>" : dataSetMetadata.getFriendlyName());
		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
		lbl_NewMatrixName.setLabelFor(txt_NewMatrixName);
		txt_NewMatrixName.setDocument(new LimitedLengthDocument(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(GWASpiExplorerPanel.createRegularTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new LimitedLengthDocument(1999));
		txtA_NewMatrixDescription.setText(Text.All.optional);
		txtA_NewMatrixDescription.addFocusListener(new SelectAllTextFocusListener(Text.All.optional));
		scrl_NewMatrixDescription.setViewportView(txtA_NewMatrixDescription);

		//<editor-fold defaultstate="expanded" desc="LAYOUT NAME&DESC">
		GridBagLayout subLayout = new GridBagLayout();
		// we use every second row and column as a spacer
		subLayout.columnWidths = new int[] {0, gapSpace, 0};
		subLayout.rowHeights = new int[] {0, gapSpace, 0, gapSpace, 0};
		pnl_NameAndDesc.setLayout(subLayout);

		GridBagConstraints subGridBagConstraints = new GridBagConstraints();
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.gridheight = 1;
		subGridBagConstraints.anchor = GridBagConstraints.CENTER;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		subGridBagConstraints.weighty = 0.0;

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		pnl_NameAndDesc.add(lbl_ParentMatrix, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 2;
		pnl_NameAndDesc.add(lbl_NewMatrixName, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.anchor = GridBagConstraints.WEST;
		subGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		subGridBagConstraints.weightx = 1.0;
		pnl_NameAndDesc.add(lbl_ParentMatrixName, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 2;
		pnl_NameAndDesc.add(txt_NewMatrixName, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 4;
		subGridBagConstraints.gridwidth = 3;
		subGridBagConstraints.fill = GridBagConstraints.BOTH;
		subGridBagConstraints.weightx = 1.0;
		subGridBagConstraints.weighty = 1.0;
		pnl_NameAndDesc.add(scrl_NewMatrixDescription, subGridBagConstraints);
		//</editor-fold>
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		add(pnl_NameAndDesc, gridBagConstraints);

		lbl_MarkersVariable.setText(Text.Trafo.variable);
		lbl_MarkersVariable.setLabelFor(cmb_MarkersVariable);
		cmb_MarkersVariable.setModel(new DefaultComboBoxModel(markerPickerTable.toArray()));
		// PREFILL CRITERIA TXT WITH CHROMOSOME CODES IF NECESSARY
		cmb_MarkersVariable.setAction(new MarkersVariableAction(parentDataSetKey, txtA_MarkersCriteria));

		lbl_MarkersCriteria.setText(Text.Trafo.criteria);
		lbl_MarkersVariable.setLabelFor(txtA_MarkersCriteria);
		txtA_MarkersCriteria.setColumns(20);
		txtA_MarkersCriteria.setRows(5);
		txtA_MarkersCriteria.setText(Text.All.optional);
		txtA_MarkersCriteria.setDocument(new LimitedLengthDocument(999));
		txtA_MarkersCriteria.addFocusListener(new SelectAllTextFocusListener(Text.All.optional));
		scrl_MarkersCriteria.setViewportView(txtA_MarkersCriteria);

		lbl_MarkersCriteriaFile.setText(Text.Trafo.criteriaFile);
		txt_MarkersCriteriaFile.setText(Text.All.optional);
		txt_MarkersCriteriaFile.addFocusListener(new SelectAllTextFocusListener());

		btn_MarkersCriteriaBrowse.setAction(new MarkersCriteriaBrowseAction());

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixExtract));

		//<editor-fold defaultstate="expanded" desc="LAYOUT MARKERZONE">
		subLayout = new GridBagLayout();
		// we use every second row and column as a spacer
		subLayout.columnWidths = new int[] {0, gapSpace, 0, gapSpace, 0};
		subLayout.rowHeights = new int[] {0, gapSpace, 0, gapSpace, 0};
		pnl_MarkerZone.setLayout(subLayout);

		subGridBagConstraints = new GridBagConstraints();
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.gridheight = 1;
		subGridBagConstraints.anchor = GridBagConstraints.CENTER;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		subGridBagConstraints.weighty = 0.0;

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		pnl_MarkerZone.add(lbl_MarkersCriteriaFile, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 2;
		pnl_MarkerZone.add(lbl_MarkersCriteria, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 4;
		pnl_MarkerZone.add(lbl_MarkersVariable, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.gridwidth = 3;
		subGridBagConstraints.anchor = GridBagConstraints.WEST;
		subGridBagConstraints.weightx = 1.0;
		pnl_MarkerZone.add(cmb_MarkersVariable, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 2;
		subGridBagConstraints.fill = GridBagConstraints.BOTH;
		subGridBagConstraints.weighty = 1.0;
		pnl_MarkerZone.add(scrl_MarkersCriteria, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 4;
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		subGridBagConstraints.weighty = 0.0;
		pnl_MarkerZone.add(txt_MarkersCriteriaFile, subGridBagConstraints);

		subGridBagConstraints.gridx = 4;
		subGridBagConstraints.gridy = 4;
		subGridBagConstraints.weightx = 0.0;
		pnl_MarkerZone.add(btn_MarkersCriteriaBrowse, subGridBagConstraints);
		//</editor-fold>
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		add(pnl_MarkerZone, gridBagConstraints);

		lbl_SamplesVariable.setText(Text.Trafo.variable);
		lbl_SamplesVariable.setLabelFor(cmb_SamplesVariable);
		cmb_SamplesVariable.setModel(new DefaultComboBoxModel(samplePickerTable.toArray()));

		lbl_SamplesCriteria.setText(Text.Trafo.criteria);
		lbl_SamplesCriteria.setLabelFor(txtA_SamplesCriteria);
		txtA_SamplesCriteria.setColumns(20);
		txtA_SamplesCriteria.setRows(5);
		txtA_SamplesCriteria.setText(Text.All.optional);
		txtA_SamplesCriteria.setDocument(new LimitedLengthDocument(999));
		txtA_SamplesCriteria.addFocusListener(new SelectAllTextFocusListener(Text.All.optional));
		scrl_SamplesCriteria.setViewportView(txtA_SamplesCriteria);

		lbl_SamplesCriteriaFile.setText(Text.Trafo.criteriaFile);
		lbl_SamplesCriteriaFile.setLabelFor(txt_SamplesCriteriaFile);
		txt_SamplesCriteriaFile.setText(Text.All.optional);
		txt_SamplesCriteriaFile.addFocusListener(new SelectAllTextFocusListener());

		btn_SamplesCriteriaBrowse.setAction(new SamplesCriteriaBrowseAction());

		//<editor-fold defaultstate="expanded" desc="LAYOUT SAMPLEZONE">
		subLayout = new GridBagLayout();
		// we use every second row and column as a spacer
		subLayout.columnWidths = new int[] {0, gapSpace, 0, gapSpace, 0};
		subLayout.rowHeights = new int[] {0, gapSpace, 0, gapSpace, 0};
		pnl_SampleZone.setLayout(subLayout);

		subGridBagConstraints = new GridBagConstraints();
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.gridheight = 1;
		subGridBagConstraints.anchor = GridBagConstraints.CENTER;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		subGridBagConstraints.weighty = 0.0;

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		pnl_SampleZone.add(lbl_SamplesCriteriaFile, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 2;
		pnl_SampleZone.add(lbl_SamplesCriteria, subGridBagConstraints);

		subGridBagConstraints.gridx = 0;
		subGridBagConstraints.gridy = 4;
		pnl_SampleZone.add(lbl_SamplesVariable, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 0;
		subGridBagConstraints.gridwidth = 3;
		subGridBagConstraints.anchor = GridBagConstraints.WEST;
		subGridBagConstraints.weightx = 1.0;
		pnl_SampleZone.add(cmb_SamplesVariable, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 2;
		subGridBagConstraints.fill = GridBagConstraints.BOTH;
		subGridBagConstraints.weighty = 1.0;
		pnl_SampleZone.add(scrl_SamplesCriteria, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.gridy = 4;
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		subGridBagConstraints.weighty = 0.0;
		pnl_SampleZone.add(txt_SamplesCriteriaFile, subGridBagConstraints);

		subGridBagConstraints.gridx = 4;
		subGridBagConstraints.gridy = 4;
		subGridBagConstraints.weightx = 0.0;
		pnl_SampleZone.add(btn_SamplesCriteriaBrowse, subGridBagConstraints);
		//</editor-fold>
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		add(pnl_SampleZone, gridBagConstraints);

		btn_Back.setAction(new BackAction(parentDataSetKey));

		btn_Go.setAction(new ExtractAction());

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		subLayout = new GridBagLayout();
		// we use every second row and column as a spacer
		subLayout.columnWidths = new int[] {0, gapSpace, 0, gapSpace, 0};
		subLayout.rowHeights = new int[] {0};
		pnl_Footer.setLayout(subLayout);

		subGridBagConstraints = new GridBagConstraints();
		subGridBagConstraints.gridwidth = 1;
		subGridBagConstraints.gridheight = 1;
		subGridBagConstraints.anchor = GridBagConstraints.CENTER;
		subGridBagConstraints.fill = GridBagConstraints.NONE;
		subGridBagConstraints.weightx = 0.0;
		subGridBagConstraints.weighty = 0.0;

		subGridBagConstraints.gridx = 0;
		pnl_Footer.add(btn_Back, subGridBagConstraints);

		subGridBagConstraints.gridx = 2;
		subGridBagConstraints.weightx = 0.5;
		subGridBagConstraints.anchor = GridBagConstraints.WEST;
		pnl_Footer.add(btn_Help, subGridBagConstraints);

		subGridBagConstraints.gridx = 4;
		subGridBagConstraints.anchor = GridBagConstraints.EAST;
		pnl_Footer.add(btn_Go, subGridBagConstraints);
		//</editor-fold>
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.weighty = 0.0;
		add(pnl_Footer, gridBagConstraints);
	}

	private class ExtractAction extends AbstractAction { // XXX make static

		ExtractAction() {

			putValue(NAME, Text.Trafo.extract);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				String newMatrixName = checkNewMatrixData();
				if (!newMatrixName.isEmpty()) {
					ProcessTab.getSingleton().showTab();

					final PickMethod<SetMarkerPickCase> markerPickMethod
							= (PickMethod<SetMarkerPickCase>) cmb_MarkersVariable.getSelectedItem();
					final PickMethod<SetSamplePickCase> samplePickMethod
							= (PickMethod<SetSamplePickCase>) cmb_SamplesVariable.getSelectedItem();

					final SetMarkerPickCase markerPickCase = markerPickMethod.getPickCase();
					final SetSamplePickCase samplePickCase = samplePickMethod.getPickCase();

					String mi_marker_criteria_file = txt_MarkersCriteriaFile.getText();
					if (mi_marker_criteria_file.equals(Text.All.optional)) {
						mi_marker_criteria_file = "";
					}
					String mi_sample_criteria_file = txt_SamplesCriteriaFile.getText();
					if (mi_sample_criteria_file.equals(Text.All.optional)) {
						mi_sample_criteria_file = "";
					}

					String mi_marker_criteria = txtA_MarkersCriteria.getText();
					if (mi_marker_criteria.equals(Text.All.optional)) {
						mi_marker_criteria = "";
					}
					Set<Object> markerCriteria = new HashSet<Object>();
					String[] mVals = mi_marker_criteria.split(ImportConstants.Separators.separators_CommaSpaceTabLf_rgxp);
					for (String markerCrit : mVals) {
						if (!markerCrit.isEmpty()) {
							if ((markerPickCase == SetMarkerPickCase.MARKERS_INCLUDE_BY_ID)
									|| (markerPickCase == SetMarkerPickCase.MARKERS_EXCLUDE_BY_ID))
							{
								markerCriteria.add(MarkerKey.valueOf(markerCrit));
							} else {
								markerCriteria.add(markerCrit.toCharArray());
							}
						}
					}

					String mi_sample_criteria = txtA_SamplesCriteria.getText();
					if (mi_sample_criteria.equals(Text.All.optional)) {
						mi_sample_criteria = "";
					}
					Set<Object> sampleCriteria = new HashSet<Object>();
					String[] sVals = mi_sample_criteria.split(ImportConstants.Separators.separators_CommaSpaceTabLf_rgxp);
					for (String sampleCrit : sVals) {
						if (!sampleCrit.isEmpty()) {
//							if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
//									|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
//							{
//								sampleCriteria.add(SampleKey.valueOf(
//										parentMatrixKey.getStudyKey(), sampleCrit));
//							} else {
								sampleCriteria.add(sampleCrit.toCharArray());
//							}
						}
					}

					File markerCriteriaFile = new File(mi_marker_criteria_file);
					if (markerCriteria.isEmpty()
							&& (cmb_MarkersVariable.getSelectedIndex() != 0) // NOT ALL MARKERS
							&& !markerCriteriaFile.isFile())
					{
						throw new IllegalArgumentException("Marker criteria file missing!");
					}
					File sampleCriteriaFile = new File(mi_sample_criteria_file);
					if (sampleCriteria.isEmpty()
							&& (cmb_SamplesVariable.getSelectedIndex() != 0) // NOT ALL SAMPLES
							&& !sampleCriteriaFile.isFile())
					{
						throw new IllegalArgumentException("Sample criteria file missing!");
					}

					String markerPickVar = "";
					if (!markerPickCase.equals(SetMarkerPickCase.ALL_MARKERS)) {
						markerPickVar = markerPickMethod.getVar();
					}

					String samplePickVar = "";
					if (!samplePickCase.equals(SetSamplePickCase.ALL_SAMPLES)) {
						samplePickVar = samplePickMethod.getVar();
					}

					String description = txtA_NewMatrixDescription.getText();
					if (description.equals(Text.All.optional)) {
						description = "";
					}

					final MatrixDataExtractorParams params = new MatrixDataExtractorParams(
							parentDataSetKey,
							description,
							newMatrixName,
							markerCriteriaFile,
							sampleCriteriaFile,
							markerPickCase,
							markerPickVar,
							samplePickCase,
							samplePickVar,
							Integer.MIN_VALUE, // Filter pos, not used now
							markerCriteria,
							sampleCriteria);

					final CommonRunnable extractDataTask = new Threaded_ExtractMatrix(params);
					MultiOperations.queueTask(extractDataTask);
				} else {
					Dialogs.showWarningDialogue(Text.Matrix.pleaseInsertMatrixName);
				}
			} catch (Exception ex) {
				log.error(null, ex);
				Dialogs.showWarningDialogue(ex.getMessage());
			}
		}
	}

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	private class MarkersCriteriaBrowseAction extends AbstractAction { // XXX make static

		MarkersCriteriaBrowseAction() {

			putValue(NAME, Text.All.browse);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			txtA_MarkersCriteria.setText("");
			// Use standard file opener
			Dialogs.selectAndSetFileDialog(txt_MarkersCriteriaFile, "");
		}
	}

	private class SamplesCriteriaBrowseAction extends AbstractAction { // XXX make static

		SamplesCriteriaBrowseAction() {

			putValue(NAME, Text.All.browse);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			txtA_SamplesCriteria.setText("");
			// Use standard file opener
			Dialogs.selectAndSetFileDialog(txt_SamplesCriteriaFile, "");
		}
	}

	private static class MarkersVariableAction extends AbstractAction {

		private final DataSetKey parentDataSetKey;
		private final JTextArea txtA_MarkersCriteria;

		MarkersVariableAction(final DataSetKey parentDataSetKey, JTextArea txtA_MarkersCriteria) throws IOException {

			this.parentDataSetKey = parentDataSetKey;
			this.txtA_MarkersCriteria = txtA_MarkersCriteria;
			putValue(NAME, Text.Trafo.variable);
		}

		private static String createMarkerIdsList(DataSetSource dataSetSource) throws IOException {

			StringBuilder markerIdsList = new StringBuilder();
			for (MarkerKey key : dataSetSource.getMarkersKeysSource()) {
				markerIdsList
						.append(key.getMarkerId())
						.append(',');
			}
			markerIdsList.deleteCharAt(markerIdsList.length() - 1);

			return markerIdsList.toString();
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			JComboBox cmb_MarkersVariable = (JComboBox) evt.getSource();
			final int selectedIndex = cmb_MarkersVariable.getSelectedIndex();
			if (selectedIndex == 1 || selectedIndex == 4) {
				// Chromosome variables
				// NOTE The here created String (list of marker IDs) may easily be 10MB+ large!
				final DataSetSource dataSetSource = MatrixFactory.generateDataSetSource(parentDataSetKey);
				String markerIdsList;
				try {
					markerIdsList = createMarkerIdsList(dataSetSource);
					txtA_MarkersCriteria.setText(markerIdsList);
				} catch (IOException ex) {
					log.error("Failed to create list of marker IDs", ex);
				}
			}
		}
	}

	private String checkNewMatrixData() {

		String study_name = txt_NewMatrixName.getText().trim();
		Color labelColor;
		if (!study_name.isEmpty()) {
			labelColor = Color.BLACK;
		} else {
			labelColor = Color.RED;
		}
		lbl_NewMatrixName.setForeground(labelColor);

		return study_name;
	}
	//</editor-fold>
}
