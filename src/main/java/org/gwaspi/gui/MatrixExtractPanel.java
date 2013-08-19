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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.BrowserHelpUrlAction;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import org.gwaspi.gui.utils.LimitedLengthDocument;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.threadbox.MultiOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixExtractPanel extends JPanel {

	private static final Logger log
			= LoggerFactory.getLogger(MatrixExtractPanel.class);

	// Variables declaration - do not modify
	private final MatrixKey parentMatrixKey;
	private final List<Object[]> markerPickerTable = new ArrayList<Object[]>();
	private final List<Object[]> samplePickerTable = new ArrayList<Object[]>();
	private final JButton btn_Back;
	private final JButton btn_Go;
	private final JButton btn_Help;
	private final JButton btn_MarkersCriteriaBrowse;
	private final JButton btn_SamplesCriteriaBrowse;
	private final JComboBox cmb_MarkersVariable;
	private final JComboBox cmb_SamplesVariable;
	private final JLabel lbl_MarkersCriteria;
	private final JLabel lbl_MarkersCriteriaFile;
	private final JLabel lbl_MarkersVariable;
	private final JLabel lbl_NewMatrixName;
	private final JLabel lbl_ParentMatrix;
	private final JLabel lbl_ParentMatrixName;
	private final JLabel lbl_SamplesCriteria;
	private final JLabel lbl_SamplesCriteriaFile;
	private final JLabel lbl_SamplesVariable;
	private final JPanel pnl_Footer;
	private final JPanel pnl_MarkerZone;
	private final JPanel pnl_NameAndDesc;
	private final JPanel pnl_SampleZone;
	private final JScrollPane scrl_MarkersCriteria;
	private final JScrollPane scrl_NewMatrixDescription;
	private final JScrollPane scrl_SamplesCriteria;
	private final JTextArea txtA_MarkersCriteria;
	private final JTextArea txtA_NewMatrixDescription;
	private final JTextArea txtA_SamplesCriteria;
	private final JTextField txt_MarkersCriteriaFile;
	private final JTextField txt_NewMatrixName;
	private final JTextField txt_SamplesCriteriaFile;
	// End of variables declaration

	public MatrixExtractPanel(MatrixKey parentMatrixKey, String newMatrixName, String newMatrixDesc) throws IOException {

		this.parentMatrixKey = parentMatrixKey;
		MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataById(parentMatrixKey);

		pnl_NameAndDesc = new JPanel();
		lbl_ParentMatrix = new JLabel();
		lbl_ParentMatrixName = new JLabel();
		lbl_NewMatrixName = new JLabel();
		txt_NewMatrixName = new JTextField(newMatrixName);
		scrl_NewMatrixDescription = new JScrollPane();
		txtA_NewMatrixDescription = new JTextArea(newMatrixDesc);
		pnl_MarkerZone = new JPanel();
		lbl_MarkersVariable = new JLabel();
		cmb_MarkersVariable = new JComboBox();
		btn_Help = new JButton();
		lbl_MarkersCriteria = new JLabel();
		scrl_MarkersCriteria = new JScrollPane();
		txtA_MarkersCriteria = new JTextArea();
		lbl_MarkersCriteriaFile = new JLabel();
		txt_MarkersCriteriaFile = new JTextField();
		btn_MarkersCriteriaBrowse = new JButton();
		pnl_SampleZone = new JPanel();
		lbl_SamplesVariable = new JLabel();
		cmb_SamplesVariable = new JComboBox();
		lbl_SamplesCriteria = new JLabel();
		scrl_SamplesCriteria = new JScrollPane();
		txtA_SamplesCriteria = new JTextArea();
		lbl_SamplesCriteriaFile = new JLabel();
		txt_SamplesCriteriaFile = new JTextField();
		btn_SamplesCriteriaBrowse = new JButton();
		pnl_Footer = new JPanel();
		btn_Back = new JButton();
		btn_Go = new JButton();

		setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.extractData, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N

		markerPickerTable.add(new Object[]{"All Markers", SetMarkerPickCase.ALL_MARKERS, null});
		markerPickerTable.add(new Object[]{"Exclude by Chromosomes", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_CHR});
		markerPickerTable.add(new Object[]{"Exclude by MarkerId", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERSET});
		markerPickerTable.add(new Object[]{"Exclude by RsId", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_RSID});
		markerPickerTable.add(new Object[]{"Include by Chromosomes", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_CHR});
		markerPickerTable.add(new Object[]{"Include by MarkerId", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERSET});
		markerPickerTable.add(new Object[]{"Include by RsId", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_RSID});

		//markerPickerTable.add(new Object[]{"Exclude by Position Window", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_POS});
		//markerPickerTable.add(new Object[]{"Exclude by Strand", SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_GT_STRAND});
		//markerPickerTable.add(new Object[]{"Include by Position Window", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_MARKERS_POS});
		//markerPickerTable.add(new Object[]{"Include by Strand", SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, cNetCDF.Variables.VAR_GT_STRAND});


		samplePickerTable.add(new Object[]{"All Samples", SetSamplePickCase.ALL_SAMPLES, null});
		samplePickerTable.add(new Object[]{"Exclude by Affection", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_AFFECTION});
		samplePickerTable.add(new Object[]{"Exclude by Age", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_AGE});
		samplePickerTable.add(new Object[]{"Exclude by Category", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_CATEGORY});
		samplePickerTable.add(new Object[]{"Exclude by Disease", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_DISEASE});
		samplePickerTable.add(new Object[]{"Exclude by FamilyID", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_FAMILY_ID});
		samplePickerTable.add(new Object[]{"Exclude by Population", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_POPULATION});
		samplePickerTable.add(new Object[]{"Exclude by SampleID", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_SAMPLE_ID});
		samplePickerTable.add(new Object[]{"Exclude by Sex", SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, cDBSamples.f_SEX});

		samplePickerTable.add(new Object[]{"Include by Affection", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_AFFECTION});
		samplePickerTable.add(new Object[]{"Include by Age", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_AGE});
		samplePickerTable.add(new Object[]{"Include by Category", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_CATEGORY});
		samplePickerTable.add(new Object[]{"Include by Disease", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_DISEASE});
		samplePickerTable.add(new Object[]{"Include by FamilyID", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_FAMILY_ID});
		samplePickerTable.add(new Object[]{"Include by Population", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_POPULATION});
		samplePickerTable.add(new Object[]{"Include by SampleID", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_SAMPLE_ID});
		samplePickerTable.add(new Object[]{"Include by Sex", SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, cDBSamples.f_SEX});


		pnl_NameAndDesc.setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.extratedMatrixDetails, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		pnl_MarkerZone.setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.markerSelectZone, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
		pnl_SampleZone.setBorder(BorderFactory.createTitledBorder(null, Text.Trafo.sampleSelectZone, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N

		lbl_ParentMatrix.setText(Text.Matrix.parentMatrix);
		lbl_ParentMatrixName.setText(matrixMetadata.getMatrixFriendlyName());
		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
		txt_NewMatrixName.setDocument(new LimitedLengthDocument(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(BorderFactory.createTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new LimitedLengthDocument(1999));
		txtA_NewMatrixDescription.setText(Text.All.optional);
		txtA_NewMatrixDescription.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
							txtA_NewMatrixDescription.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_NewMatrixDescription.select(0, 0);
					}
				});
			}
		});
		scrl_NewMatrixDescription.setViewportView(txtA_NewMatrixDescription);

		//<editor-fold defaultstate="expanded" desc="LAYOUT NAME&DESC">
		GroupLayout pnl_NameAndDescLayout = new GroupLayout(pnl_NameAndDesc);
		pnl_NameAndDesc.setLayout(pnl_NameAndDescLayout);
		pnl_NameAndDescLayout.setHorizontalGroup(
				pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(scrl_NewMatrixDescription, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addComponent(lbl_NewMatrixName)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(txt_NewMatrixName, GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addComponent(lbl_ParentMatrix)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(lbl_ParentMatrixName)))
				.addContainerGap()));
		pnl_NameAndDescLayout.setVerticalGroup(
				pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_ParentMatrix)
				.addComponent(lbl_ParentMatrixName))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_NewMatrixDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		lbl_MarkersVariable.setText(Text.Trafo.variable);
		String[] markerPickerVars = new String[]{markerPickerTable.get(0)[0].toString(),
			markerPickerTable.get(1)[0].toString(),
			markerPickerTable.get(2)[0].toString(),
			markerPickerTable.get(3)[0].toString(),
			markerPickerTable.get(4)[0].toString(),
			markerPickerTable.get(5)[0].toString(),
			markerPickerTable.get(6)[0].toString()};
		cmb_MarkersVariable.setModel(new DefaultComboBoxModel(markerPickerVars));
		// PREFILL CRITERIA TXT WITH CHROMOSOME CODES IF NECESSARY
		cmb_MarkersVariable.setAction(new MarkersVariableAction(parentMatrixKey));

		lbl_MarkersCriteria.setText(Text.Trafo.criteria);
		txtA_MarkersCriteria.setColumns(20);
		txtA_MarkersCriteria.setRows(5);
		txtA_MarkersCriteria.setText(Text.All.optional);
		txtA_MarkersCriteria.setDocument(new LimitedLengthDocument(999));
		txtA_MarkersCriteria.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_MarkersCriteria.getText().equals(Text.All.optional)) {
							txtA_MarkersCriteria.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_MarkersCriteria.select(0, 0);
					}
				});
			}
		});
		scrl_MarkersCriteria.setViewportView(txtA_MarkersCriteria);

		lbl_MarkersCriteriaFile.setText(Text.Trafo.criteriaFile);
		txt_MarkersCriteriaFile.setText(Text.All.optional);
		txt_MarkersCriteriaFile.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_MarkersCriteriaFile.selectAll();
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_MarkersCriteriaFile.select(0, 0);
					}
				});
			}
		});

		btn_MarkersCriteriaBrowse.setAction(new MarkersCriteriaBrowseAction());

		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.matrixExtract));

		//<editor-fold defaultstate="expanded" desc="LAYOUT MARKERZONE">
		GroupLayout pnl_MarkerZoneLayout = new GroupLayout(pnl_MarkerZone);
		pnl_MarkerZone.setLayout(pnl_MarkerZoneLayout);
		pnl_MarkerZoneLayout.setHorizontalGroup(
				pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_MarkersCriteriaFile)
				.addComponent(lbl_MarkersCriteria)
				.addComponent(lbl_MarkersVariable))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(cmb_MarkersVariable, GroupLayout.PREFERRED_SIZE, 464, GroupLayout.PREFERRED_SIZE)
				.addComponent(scrl_MarkersCriteria, GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
				.addComponent(txt_MarkersCriteriaFile, GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_MarkersCriteriaBrowse, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));


		pnl_MarkerZoneLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{cmb_MarkersVariable, scrl_MarkersCriteria, txt_MarkersCriteriaFile});

		pnl_MarkerZoneLayout.setVerticalGroup(
				pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_MarkerZoneLayout.createSequentialGroup()
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_MarkersVariable)
				.addComponent(cmb_MarkersVariable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_MarkersCriteria)
				.addComponent(scrl_MarkersCriteria, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_MarkersCriteriaFile)
				.addComponent(txt_MarkersCriteriaFile)
				.addComponent(btn_MarkersCriteriaBrowse))
				.addContainerGap()));
		//</editor-fold>

		lbl_SamplesVariable.setText(Text.Trafo.variable);
		String[] samplePickerVars = new String[]{samplePickerTable.get(0)[0].toString(),
			samplePickerTable.get(1)[0].toString(),
			samplePickerTable.get(2)[0].toString(),
			samplePickerTable.get(3)[0].toString(),
			samplePickerTable.get(4)[0].toString(),
			samplePickerTable.get(5)[0].toString(),
			samplePickerTable.get(6)[0].toString(),
			samplePickerTable.get(7)[0].toString(),
			samplePickerTable.get(8)[0].toString(),
			samplePickerTable.get(9)[0].toString(),
			samplePickerTable.get(10)[0].toString(),
			samplePickerTable.get(11)[0].toString(),
			samplePickerTable.get(12)[0].toString(),
			samplePickerTable.get(13)[0].toString(),
			samplePickerTable.get(14)[0].toString(),
			samplePickerTable.get(15)[0].toString(),
			samplePickerTable.get(16)[0].toString()};
		cmb_SamplesVariable.setModel(new DefaultComboBoxModel(samplePickerVars));

		lbl_SamplesCriteria.setText(Text.Trafo.criteria);
		txtA_SamplesCriteria.setColumns(20);
		txtA_SamplesCriteria.setRows(5);
		txtA_SamplesCriteria.setText(Text.All.optional);
		txtA_SamplesCriteria.setDocument(new LimitedLengthDocument(999));
		txtA_SamplesCriteria.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (txtA_SamplesCriteria.getText().equals(Text.All.optional)) {
							txtA_SamplesCriteria.selectAll();
						}
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_SamplesCriteria.select(0, 0);
					}
				});
			}
		});
		scrl_SamplesCriteria.setViewportView(txtA_SamplesCriteria);

		lbl_SamplesCriteriaFile.setText(Text.Trafo.criteriaFile);
		txt_SamplesCriteriaFile.setText(Text.All.optional);
		txt_SamplesCriteriaFile.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_SamplesCriteriaFile.selectAll();
					}
				});
			}

			@Override
			public void focusLost(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_SamplesCriteriaFile.select(0, 0);
					}
				});
			}
		});

		btn_SamplesCriteriaBrowse.setAction(new SamplesCriteriaBrowseAction());

		//<editor-fold defaultstate="expanded" desc="LAYOUT SAMPLEZONE">
		GroupLayout pnl_SampleZoneLayout = new GroupLayout(pnl_SampleZone);
		pnl_SampleZone.setLayout(pnl_SampleZoneLayout);
		pnl_SampleZoneLayout.setHorizontalGroup(
				pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_SampleZoneLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_SamplesVariable)
				.addComponent(lbl_SamplesCriteria)
				.addComponent(lbl_SamplesCriteriaFile))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(cmb_SamplesVariable, 0, 461, Short.MAX_VALUE)
				.addComponent(scrl_SamplesCriteria, GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
				.addComponent(txt_SamplesCriteriaFile, GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_SamplesCriteriaBrowse, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(15, Short.MAX_VALUE)));

		pnl_SampleZoneLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{cmb_SamplesVariable, scrl_SamplesCriteria, txt_SamplesCriteriaFile});

		pnl_SampleZoneLayout.setVerticalGroup(
				pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SampleZoneLayout.createSequentialGroup()
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_SamplesVariable)
				.addComponent(cmb_SamplesVariable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_SamplesCriteria)
				.addComponent(scrl_SamplesCriteria, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(txt_SamplesCriteriaFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_SamplesCriteriaBrowse)
				.addComponent(lbl_SamplesCriteriaFile))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>

		btn_Back.setAction(new BackAction(parentMatrixKey));

		btn_Go.setAction(new ExtractAction());

		//<editor-fold defaultstate="expanded" desc="FOOTER">
		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Help, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 331, Short.MAX_VALUE)
				.addComponent(btn_Go, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addGap(0, 0, 0)
				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Go, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="LAYOUT">
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_NameAndDesc, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MarkerZone, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_SampleZone, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(45, 45, 45)))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_NameAndDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_MarkerZone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_SampleZone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(44, Short.MAX_VALUE)));
		//</editor-fold>
	}

	private class ExtractAction extends AbstractAction { // FIXME make static

		ExtractAction() {

			putValue(NAME, Text.Trafo.extract);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				String newMatrixName = checkNewMatrixData();
				if (!newMatrixName.isEmpty()) {
					ProcessTab.getSingleton().showTab();

					SetMarkerPickCase markerPickCase = (SetMarkerPickCase) markerPickerTable.get(cmb_MarkersVariable.getSelectedIndex())[1];
					SetSamplePickCase samplePickCase = (SetSamplePickCase) samplePickerTable.get(cmb_SamplesVariable.getSelectedIndex())[1];

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
					String[] mVals = mi_marker_criteria.split(cImport.Separators.separators_CommaSpaceTabLf_rgxp);
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
					String[] sVals = mi_sample_criteria.split(cImport.Separators.separators_CommaSpaceTabLf_rgxp);
					for (String sampleCrit : sVals) {
						if (!sampleCrit.isEmpty()) {
							if ((samplePickCase == SetSamplePickCase.SAMPLES_INCLUDE_BY_ID)
									|| (samplePickCase == SetSamplePickCase.SAMPLES_EXCLUDE_BY_ID))
							{
								sampleCriteria.add(SampleKey.valueOf(
										parentMatrixKey.getStudyKey(), sampleCrit));
							} else {
								sampleCriteria.add(sampleCrit.toCharArray());
							}
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
						markerPickVar = markerPickerTable.get(cmb_MarkersVariable.getSelectedIndex())[2].toString();
					}

					String samplePickVar = "";
					if (!samplePickCase.equals(SetSamplePickCase.ALL_SAMPLES)) {
						samplePickVar = samplePickerTable.get(cmb_SamplesVariable.getSelectedIndex())[2].toString();
					}

					String description = txtA_NewMatrixDescription.getText();
					if (description.equals(Text.All.optional)) {
						description = "";
					}

					MultiOperations.doExtractData(
							parentMatrixKey,
							newMatrixName,
							description,
							markerPickCase,
							samplePickCase,
							markerPickVar,
							samplePickVar,
							markerCriteria,
							sampleCriteria,
							markerCriteriaFile,
							sampleCriteriaFile);
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
	private class MarkersCriteriaBrowseAction extends AbstractAction { // FIXME make static

		MarkersCriteriaBrowseAction() {

			putValue(NAME, Text.All.browse);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			txtA_MarkersCriteria.setText("");
			// Use standard file opener
			Dialogs.selectAndSetFileDialog(evt, btn_MarkersCriteriaBrowse, txt_MarkersCriteriaFile, "");
		}
	}

	private class SamplesCriteriaBrowseAction extends AbstractAction { // FIXME make static

		SamplesCriteriaBrowseAction() {

			putValue(NAME, Text.All.browse);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			txtA_SamplesCriteria.setText("");
			// Use standard file opener
			Dialogs.selectAndSetFileDialog(evt, btn_SamplesCriteriaBrowse, txt_SamplesCriteriaFile, "");
		}
	}

	private class MarkersVariableAction extends AbstractAction { // FIXME make static

		private final Set<MarkerKey> markerKeys;

		MarkersVariableAction(MatrixKey parentMatrixKey) throws IOException {

			MarkerSet parentMarkerSet = new MarkerSet(parentMatrixKey);
			markerKeys = parentMarkerSet.getMarkerKeys();

			putValue(NAME, Text.Trafo.variable);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			final int selectedIndex = cmb_MarkersVariable.getSelectedIndex();
			if (selectedIndex == 1 || selectedIndex == 4) {
				// Chromosome variables

				StringBuilder sb = new StringBuilder();
				for (MarkerKey key : markerKeys) {
					sb.append(key.getMarkerId());
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);

				txtA_MarkersCriteria.setText(sb.toString());
			}
		}
	}

	private static class BackAction extends AbstractAction {

		private final MatrixKey parentMatrix;

		BackAction(MatrixKey parentMatrix) {

			this.parentMatrix = parentMatrix;
			putValue(NAME, Text.All.Back);
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentMatrixPanel(parentMatrix));
				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
			} catch (IOException ex) {
				log.error(null, ex);
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
