package org.gwaspi.gui;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.threadbox.MultiOperations;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixExtractPanel extends javax.swing.JPanel {

	public MatrixExtractPanel(int _matrixId, String newMatrixName, String newMatrixDesc) throws IOException {
		initComponents(_matrixId, newMatrixName, newMatrixDesc);
	}
	// Variables declaration - do not modify
	private org.gwaspi.model.Matrix parentMatrix;
	public static List<Object[]> markerPickerTable = new ArrayList<Object[]>();
	public static List<Object[]> samplePickerTable = new ArrayList<Object[]>();
	private javax.swing.JButton btn_Back;
	private javax.swing.JButton btn_Go;
	private javax.swing.JButton btn_Help;
	private javax.swing.JButton btn_MarkersCriteriaBrowse;
	private javax.swing.JButton btn_SamplesCriteriaBrowse;
	private javax.swing.JComboBox cmb_MarkersVariable;
	private javax.swing.JComboBox cmb_SamplesVariable;
	private javax.swing.JLabel lbl_MarkersCriteria;
	private javax.swing.JLabel lbl_MarkersCriteriaFile;
	private javax.swing.JLabel lbl_MarkersVariable;
	private javax.swing.JLabel lbl_NewMatrixName;
	private javax.swing.JLabel lbl_ParentMatrix;
	private javax.swing.JLabel lbl_ParentMatrixName;
	private javax.swing.JLabel lbl_SamplesCriteria;
	private javax.swing.JLabel lbl_SamplesCriteriaFile;
	private javax.swing.JLabel lbl_SamplesVariable;
	private javax.swing.JPanel pnl_Footer;
	private javax.swing.JPanel pnl_MarkerZone;
	private javax.swing.JPanel pnl_NameAndDesc;
	private javax.swing.JPanel pnl_SampleZone;
	private javax.swing.JScrollPane scrl_MarkersCriteria;
	private javax.swing.JScrollPane scrl_NewMatrixDescription;
	private javax.swing.JScrollPane scrl_SamplesCriteria;
	private javax.swing.JTextArea txtA_MarkersCriteria;
	private javax.swing.JTextArea txtA_NewMatrixDescription;
	private javax.swing.JTextArea txtA_SamplesCriteria;
	private javax.swing.JTextField txt_MarkersCriteriaFile;
	private javax.swing.JTextField txt_NewMatrixName;
	private javax.swing.JTextField txt_SamplesCriteriaFile;
	// End of variables declaration

	@SuppressWarnings("unchecked")
	private void initComponents(int _matrixId, String newMatrixName, String newMatrixDesc) throws IOException {
		parentMatrix = new org.gwaspi.model.Matrix(_matrixId);

		MarkerSet_opt parentMarkerSet = new MarkerSet_opt(parentMatrix.getStudyId(), _matrixId);
		final Map<String, Object> rdChrInfoSetLHM = parentMarkerSet.getChrInfoSetLHM();

		pnl_NameAndDesc = new javax.swing.JPanel();
		lbl_ParentMatrix = new javax.swing.JLabel();
		lbl_ParentMatrixName = new javax.swing.JLabel();
		lbl_NewMatrixName = new javax.swing.JLabel();
		txt_NewMatrixName = new javax.swing.JTextField(newMatrixName);
		scrl_NewMatrixDescription = new javax.swing.JScrollPane();
		txtA_NewMatrixDescription = new javax.swing.JTextArea(newMatrixDesc);
		pnl_MarkerZone = new javax.swing.JPanel();
		lbl_MarkersVariable = new javax.swing.JLabel();
		cmb_MarkersVariable = new javax.swing.JComboBox();
		btn_Help = new javax.swing.JButton();
		lbl_MarkersCriteria = new javax.swing.JLabel();
		scrl_MarkersCriteria = new javax.swing.JScrollPane();
		txtA_MarkersCriteria = new javax.swing.JTextArea();
		lbl_MarkersCriteriaFile = new javax.swing.JLabel();
		txt_MarkersCriteriaFile = new javax.swing.JTextField();
		btn_MarkersCriteriaBrowse = new javax.swing.JButton();
		pnl_SampleZone = new javax.swing.JPanel();
		lbl_SamplesVariable = new javax.swing.JLabel();
		cmb_SamplesVariable = new javax.swing.JComboBox();
		lbl_SamplesCriteria = new javax.swing.JLabel();
		scrl_SamplesCriteria = new javax.swing.JScrollPane();
		txtA_SamplesCriteria = new javax.swing.JTextArea();
		lbl_SamplesCriteriaFile = new javax.swing.JLabel();
		txt_SamplesCriteriaFile = new javax.swing.JTextField();
		btn_SamplesCriteriaBrowse = new javax.swing.JButton();
		pnl_Footer = new javax.swing.JPanel();
		btn_Back = new javax.swing.JButton();
		btn_Go = new javax.swing.JButton();

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.extractData, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

		markerPickerTable.add(new Object[]{"All Markers", cNetCDF.Defaults.SetMarkerPickCase.ALL_MARKERS, null});
		markerPickerTable.add(new Object[]{"Exclude by Chromosomes", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR});
		markerPickerTable.add(new Object[]{"Exclude by MarkerId", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET});
		markerPickerTable.add(new Object[]{"Exclude by RsId", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID});
		markerPickerTable.add(new Object[]{"Include by Chromosomes", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR});
		markerPickerTable.add(new Object[]{"Include by MarkerId", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET});
		markerPickerTable.add(new Object[]{"Include by RsId", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID});

		//markerPickerTable.add(new Object[]{"Exclude by Position Window", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS});
		//markerPickerTable.add(new Object[]{"Exclude by Strand", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_EXCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_GT_STRAND});
		//markerPickerTable.add(new Object[]{"Include by Position Window", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS});
		//markerPickerTable.add(new Object[]{"Include by Strand", cNetCDF.Defaults.SetMarkerPickCase.MARKERS_INCLUDE_BY_NETCDF_CRITERIA, org.gwaspi.constants.cNetCDF.Variables.VAR_GT_STRAND});


		samplePickerTable.add(new Object[]{"All Samples", cNetCDF.Defaults.SetSamplePickCase.ALL_SAMPLES, null});
		samplePickerTable.add(new Object[]{"Exclude by Affection", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_AFFECTION});
		samplePickerTable.add(new Object[]{"Exclude by Age", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_AGE});
		samplePickerTable.add(new Object[]{"Exclude by Category", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_CATEGORY});
		samplePickerTable.add(new Object[]{"Exclude by Disease", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_DISEASE});
		samplePickerTable.add(new Object[]{"Exclude by FamilyID", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_FAMILY_ID});
		samplePickerTable.add(new Object[]{"Exclude by Population", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_POPULATION});
		samplePickerTable.add(new Object[]{"Exclude by SampleID", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_SAMPLE_ID});
		samplePickerTable.add(new Object[]{"Exclude by Sex", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_EXCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_SEX});

		samplePickerTable.add(new Object[]{"Include by Affection", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_AFFECTION});
		samplePickerTable.add(new Object[]{"Include by Age", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_AGE});
		samplePickerTable.add(new Object[]{"Include by Category", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_CATEGORY});
		samplePickerTable.add(new Object[]{"Include by Disease", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_DISEASE});
		samplePickerTable.add(new Object[]{"Include by FamilyID", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_FAMILY_ID});
		samplePickerTable.add(new Object[]{"Include by Population", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_POPULATION});
		samplePickerTable.add(new Object[]{"Include by SampleID", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_SAMPLE_ID});
		samplePickerTable.add(new Object[]{"Include by Sex", cNetCDF.Defaults.SetSamplePickCase.SAMPLES_INCLUDE_BY_DB_FIELD, org.gwaspi.constants.cDBSamples.f_SEX});


		pnl_NameAndDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.extratedMatrixDetails, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		pnl_MarkerZone.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.markerSelectZone, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
		pnl_SampleZone.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Trafo.sampleSelectZone, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N

		lbl_ParentMatrix.setText(Text.Matrix.parentMatrix);
		lbl_ParentMatrixName.setText(parentMatrix.matrixMetadata.getMatrixFriendlyName());
		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
		txt_NewMatrixName.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(63));
		txtA_NewMatrixDescription.setColumns(20);
		txtA_NewMatrixDescription.setLineWrap(true);
		txtA_NewMatrixDescription.setRows(5);
		txtA_NewMatrixDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.All.description));
		txtA_NewMatrixDescription.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(1999));
		txtA_NewMatrixDescription.setText(Text.All.optional);
		txtA_NewMatrixDescription.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
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
			public void focusLost(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txtA_NewMatrixDescription.select(0, 0);
					}
				});
			}
		});
		scrl_NewMatrixDescription.setViewportView(txtA_NewMatrixDescription);

		//<editor-fold defaultstate="collapsed" desc="LAYOUT NAME&DESC">
		javax.swing.GroupLayout pnl_NameAndDescLayout = new javax.swing.GroupLayout(pnl_NameAndDesc);
		pnl_NameAndDesc.setLayout(pnl_NameAndDescLayout);
		pnl_NameAndDescLayout.setHorizontalGroup(
				pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrl_NewMatrixDescription, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addComponent(lbl_NewMatrixName)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addComponent(lbl_ParentMatrix)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(lbl_ParentMatrixName)))
				.addContainerGap()));
		pnl_NameAndDescLayout.setVerticalGroup(
				pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_ParentMatrix)
				.addComponent(lbl_ParentMatrixName))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_NameAndDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_NewMatrixName)
				.addComponent(txt_NewMatrixName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(scrl_NewMatrixDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		lbl_MarkersVariable.setText(Text.Trafo.variable);
		String[] markerPickerVars = new String[]{markerPickerTable.get(0)[0].toString(),
			markerPickerTable.get(1)[0].toString(),
			markerPickerTable.get(2)[0].toString(),
			markerPickerTable.get(3)[0].toString(),
			markerPickerTable.get(4)[0].toString(),
			markerPickerTable.get(5)[0].toString(),
			markerPickerTable.get(6)[0].toString()};
		cmb_MarkersVariable.setModel(new javax.swing.DefaultComboBoxModel(markerPickerVars));
		//PREFILL CRITERIA TXT WITH CHROMOSOME CODES IF NECESSARY
		cmb_MarkersVariable.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (cmb_MarkersVariable.getSelectedIndex() == 1 || cmb_MarkersVariable.getSelectedIndex() == 4) { //Chromosome variables
					cmb_MarkersVariableActionPerformed(evt);
				}
			}

			private void cmb_MarkersVariableActionPerformed(ActionEvent evt) {
				StringBuilder sb = new StringBuilder();
				for (Iterator it = rdChrInfoSetLHM.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					sb.append(key.toString());
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);

				txtA_MarkersCriteria.setText(sb.toString());
			}
		});

		lbl_MarkersCriteria.setText(Text.Trafo.criteria);
		txtA_MarkersCriteria.setColumns(20);
		txtA_MarkersCriteria.setRows(5);
		txtA_MarkersCriteria.setText(Text.All.optional);
		txtA_MarkersCriteria.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(999));
		txtA_MarkersCriteria.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
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
			public void focusLost(java.awt.event.FocusEvent evt) {
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
		txt_MarkersCriteriaFile.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_MarkersCriteriaFile.selectAll();
					}
				});
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_MarkersCriteriaFile.select(0, 0);
					}
				});
			}
		});

		btn_MarkersCriteriaBrowse.setText(Text.All.browse);
		btn_MarkersCriteriaBrowse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				txtA_MarkersCriteria.setText("");
				actionMarkersCriteriaBrowse(evt);
			}
		});

		btn_Help.setText(Text.Help.help);
		btn_Help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionHelp(evt);
			}
		});

		//<editor-fold defaultstate="collapsed" desc="LAYOUT MARKERZONE">
		javax.swing.GroupLayout pnl_MarkerZoneLayout = new javax.swing.GroupLayout(pnl_MarkerZone);
		pnl_MarkerZone.setLayout(pnl_MarkerZoneLayout);
		pnl_MarkerZoneLayout.setHorizontalGroup(
				pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_MarkerZoneLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_MarkersCriteriaFile)
				.addComponent(lbl_MarkersCriteria)
				.addComponent(lbl_MarkersVariable))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(cmb_MarkersVariable, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(scrl_MarkersCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
				.addComponent(txt_MarkersCriteriaFile, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_MarkersCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));


		pnl_MarkerZoneLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{cmb_MarkersVariable, scrl_MarkersCriteria, txt_MarkersCriteriaFile});

		pnl_MarkerZoneLayout.setVerticalGroup(
				pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MarkerZoneLayout.createSequentialGroup()
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_MarkersVariable)
				.addComponent(cmb_MarkersVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_MarkersCriteria)
				.addComponent(scrl_MarkersCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_MarkerZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
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
		cmb_SamplesVariable.setModel(new javax.swing.DefaultComboBoxModel(samplePickerVars));

		lbl_SamplesCriteria.setText(Text.Trafo.criteria);
		txtA_SamplesCriteria.setColumns(20);
		txtA_SamplesCriteria.setRows(5);
		txtA_SamplesCriteria.setText(Text.All.optional);
		txtA_SamplesCriteria.setDocument(new org.gwaspi.gui.utils.JTextFieldLimit(999));
		txtA_SamplesCriteria.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
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
			public void focusLost(java.awt.event.FocusEvent evt) {
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
		txt_SamplesCriteriaFile.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_SamplesCriteriaFile.selectAll();
					}
				});
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						txt_SamplesCriteriaFile.select(0, 0);
					}
				});
			}
		});

		btn_SamplesCriteriaBrowse.setText(Text.All.browse);
		btn_SamplesCriteriaBrowse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				txtA_SamplesCriteria.setText("");
				actionSamplesCriteriaBrowse(evt);
			}
		});



		//<editor-fold defaultstate="collapsed" desc="LAYOUT SAMPLEZONE">
		javax.swing.GroupLayout pnl_SampleZoneLayout = new javax.swing.GroupLayout(pnl_SampleZone);
		pnl_SampleZone.setLayout(pnl_SampleZoneLayout);
		pnl_SampleZoneLayout.setHorizontalGroup(
				pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_SampleZoneLayout.createSequentialGroup()
				.addContainerGap()
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_SamplesVariable)
				.addComponent(lbl_SamplesCriteria)
				.addComponent(lbl_SamplesCriteriaFile))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(cmb_SamplesVariable, 0, 461, Short.MAX_VALUE)
				.addComponent(scrl_SamplesCriteria, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
				.addComponent(txt_SamplesCriteriaFile, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(btn_SamplesCriteriaBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(15, Short.MAX_VALUE)));


		pnl_SampleZoneLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{cmb_SamplesVariable, scrl_SamplesCriteria, txt_SamplesCriteriaFile});

		pnl_SampleZoneLayout.setVerticalGroup(
				pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_SampleZoneLayout.createSequentialGroup()
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(lbl_SamplesVariable)
				.addComponent(cmb_SamplesVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(lbl_SamplesCriteria)
				.addComponent(scrl_SamplesCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(pnl_SampleZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(txt_SamplesCriteriaFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_SamplesCriteriaBrowse)
				.addComponent(lbl_SamplesCriteriaFile))
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		//</editor-fold>


		btn_Back.setText(Text.All.Back);
		btn_Back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionBack(evt);
			}
		});

		btn_Go.setText(Text.Trafo.extract);
		btn_Go.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				actionExtract(evt);
			}
		});

		//<editor-fold defaultstate="collapsed" desc="FOOTER">
		javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
		pnl_Footer.setLayout(pnl_FooterLayout);
		pnl_FooterLayout.setHorizontalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
				.addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(18, 18, 18)
				.addComponent(btn_Help, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 331, Short.MAX_VALUE)
				.addComponent(btn_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));
		pnl_FooterLayout.setVerticalGroup(
				pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(pnl_FooterLayout.createSequentialGroup()
				.addGap(0, 0, 0)
				.addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
				.addComponent(btn_Go, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(btn_Back)
				.addComponent(btn_Help))));
		//</editor-fold>


		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_MarkerZone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(pnl_SampleZone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(45, 45, 45)))
				.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(pnl_NameAndDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_MarkerZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_SampleZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(pnl_Footer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addContainerGap(44, Short.MAX_VALUE)));
		//</editor-fold>


	}

	private boolean actionExtract(java.awt.event.ActionEvent evt) {
		boolean result = false;
		String newMatrixName = checkNewMatrixData();
		if (!newMatrixName.isEmpty()) {
			org.gwaspi.gui.ProcessTab.showTab();


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
			HashSet markerCriteria = new HashSet();
			String[] mVals = mi_marker_criteria.split(org.gwaspi.constants.cImport.Separators.separators_CommaSpaceTabLf_rgxp);
			for (String s : mVals) {
				if (!s.isEmpty()) {
					markerCriteria.add(s);
				}
			}

			String mi_sample_criteria = txtA_SamplesCriteria.getText();
			if (mi_sample_criteria.equals(Text.All.optional)) {
				mi_sample_criteria = "";
			}
			HashSet sampleCriteria = new HashSet();
			String[] sVals = mi_sample_criteria.split(org.gwaspi.constants.cImport.Separators.separators_CommaSpaceTabLf_rgxp);
			for (String s : sVals) {
				if (!s.isEmpty()) {
					sampleCriteria.add(s);
				}
			}


			File markerCriteriaFile = new File(mi_marker_criteria_file);
			if (markerCriteria.isEmpty()) {
				if (cmb_MarkersVariable.getSelectedIndex() != 0) { //NOT ALL MARKERS
					if (!markerCriteriaFile.isFile()) {
						Dialogs.showWarningDialogue("Marker criteria file missing!");
						return result;
					}
				}
			}
			File sampleCriteriaFile = new File(mi_sample_criteria_file);
			if (sampleCriteria.isEmpty()) {
				if (cmb_SamplesVariable.getSelectedIndex() != 0) { //NOT ALL SAMPLES
					if (!sampleCriteriaFile.isFile()) {
						Dialogs.showWarningDialogue("Sample criteria file missing!");
						return result;
					}
				}
			}

			String markerPickVar = "";
			cNetCDF.Defaults.SetMarkerPickCase markerPickCase = (cNetCDF.Defaults.SetMarkerPickCase) markerPickerTable.get(cmb_MarkersVariable.getSelectedIndex())[1];
			if (!markerPickCase.equals(cNetCDF.Defaults.SetMarkerPickCase.ALL_MARKERS)) {
				markerPickVar = markerPickerTable.get(cmb_MarkersVariable.getSelectedIndex())[2].toString();
			}

			String samplePickVar = "";
			cNetCDF.Defaults.SetSamplePickCase samplePickCase = (cNetCDF.Defaults.SetSamplePickCase) samplePickerTable.get(cmb_SamplesVariable.getSelectedIndex())[1];
			if (!samplePickCase.equals(cNetCDF.Defaults.SetSamplePickCase.ALL_SAMPLES)) {
				samplePickVar = samplePickerTable.get(cmb_SamplesVariable.getSelectedIndex())[2].toString();
			}

			String description = txtA_NewMatrixDescription.getText();
			if (description.equals(Text.All.optional)) {
				description = "";
			}


			MultiOperations.doExtractData(parentMatrix.getStudyId(),
					parentMatrix.getMatrixId(),
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
			Dialogs.showWarningDialogue(org.gwaspi.global.Text.Matrix.pleaseInsertMatrixName);
		}

		return result;

	}

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	private void actionMarkersCriteriaBrowse(java.awt.event.ActionEvent evt) {
		//Use standard file opener
		org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_MarkersCriteriaBrowse, txt_MarkersCriteriaFile, "");
	}

	private void actionSamplesCriteriaBrowse(java.awt.event.ActionEvent evt) {
		//Use standard file opener
		org.gwaspi.gui.utils.Dialogs.selectAndSetFileDialogue(evt, btn_SamplesCriteriaBrowse, txt_SamplesCriteriaFile, "");
	}

	private void actionBack(java.awt.event.ActionEvent evt) {
		try {
			org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
			org.gwaspi.gui.GWASpiExplorerPanel.scrl_Content.setViewportView(org.gwaspi.gui.GWASpiExplorerPanel.pnl_Content);
		} catch (IOException ex) {
			Logger.getLogger(MatrixExtractPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actionHelp(java.awt.event.ActionEvent evt) {
		try {
			org.gwaspi.gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixExtract);
		} catch (IOException ex) {
			Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private String checkNewMatrixData() {

		String study_name = txt_NewMatrixName.getText().trim();
		if (!study_name.isEmpty()) {
			lbl_NewMatrixName.setForeground(Color.black);
		} else {
			lbl_NewMatrixName.setForeground(Color.red);
		}

		return study_name;
	}
	//</editor-fold>
}
