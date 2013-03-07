package org.gwaspi.gui.reports;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.gwaspi.constants.cImport;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.utils.CursorUtils;
import org.gwaspi.gui.utils.LinksExternalResouces;
import org.gwaspi.gui.utils.RowRendererGenotypicAssocWithZoomQueryDB;
import org.gwaspi.gui.utils.URLInDefaultBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class Report_AnalysisGenotypicTestImpl extends Report_Analysis {

	private static final Logger log
			= LoggerFactory.getLogger(Report_AnalysisGenotypicTestImpl.class);

	public Report_AnalysisGenotypicTestImpl(final int _studyId, final String _analysisFileName, final int _opId, String _NRows) {
		studyId = _studyId;
		opId = _opId;
		NRows = _NRows;
		analysisFileName = _analysisFileName;

		tbl_ReportTable.setDefaultRenderer(Object.class, new RowRendererGenotypicAssocWithZoomQueryDB());
		tbl_ReportTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				try {
					int rowIndex = tbl_ReportTable.getSelectedRow();
					int colIndex = tbl_ReportTable.getSelectedColumn();
					if (chrSetInfoMap == null || chrSetInfoMap.isEmpty()) {
						initChrSetInfo();
					}

					if (colIndex == 10) {    //Zoom
						setCursor(CursorUtils.WAIT_CURSOR);
						long markerPhysPos = (Long) tbl_ReportTable.getValueAt(rowIndex, 3); //marker physical position in chromosome
						String chr = tbl_ReportTable.getValueAt(rowIndex, 2).toString(); //Chromosome

						int[] chrInfo = (int[]) chrSetInfoMap.get(chr); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
						int nbMarkers = (Integer) chrInfo[0];
						int startPhysPos = (Integer) chrInfo[1];
						int maxPhysPos = (Integer) chrInfo[2];
						double avgMarkersPerPhysPos = (double) nbMarkers / (maxPhysPos - startPhysPos);
						int requestedWindowSize = Math.abs((int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPhysPos));

						GWASpiExplorerPanel.getSingleton().setPnl_Content(new ManhattanPlotZoom(opId,
								 chr,
								 tbl_ReportTable.getValueAt(rowIndex, 0).toString(), //MarkerID
								 markerPhysPos,
								 requestedWindowSize, //requested window size in phys positions
								 txt_NRows.getText()));
						GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
					}
					if (colIndex == 11) {    //Show selected resource database
						URLInDefaultBrowser.browseGenericURL(LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
								tbl_ReportTable.getValueAt(rowIndex, 2).toString(), //chr
								tbl_ReportTable.getValueAt(rowIndex, 1).toString(), //rsId
								(Long) tbl_ReportTable.getValueAt(rowIndex, 3)) //pos
								);
					}
				} catch (IOException ex) {
					log.error(null, ex);
				}
			}
		});

		String reportName = GWASpiExplorerPanel.getSingleton().getTree().getLastSelectedPathComponent().toString();
		reportName = reportName.substring(reportName.indexOf('-') + 2);
		String reportPath = "";
		try {
			reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
		} catch (IOException ex) {
			log.error(null, ex);
		}
		reportFile = new File(reportPath + analysisFileName);

		actionLoadReport();
	}

	@Override
	protected void actionLoadReport() {
		FileReader inputFileReader = null;
		try {
			if (reportFile.exists() && !reportFile.isDirectory()) {
				int getRowsNb = Integer.parseInt(txt_NRows.getText());


				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
				DecimalFormat dfRound = new DecimalFormat("0.#####");
				inputFileReader = new FileReader(reportFile);
				BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

				//Getting data from file and subdividing to series all points by chromosome
				List<Object[]> tableRowAL = new ArrayList<Object[]>();
				String header = inputBufferReader.readLine();
				int count = 0;
				while (count < getRowsNb) {
					String l = inputBufferReader.readLine();
					if (l == null) {
						break;
					}
					Object[] row = new Object[12];

					String[] cVals = l.split(cImport.Separators.separators_SpaceTab_rgxp);

					String markerId = cVals[0];
					String rsId = cVals[1];
					String chr = cVals[2];
					long position = Long.parseLong(cVals[3]);
					String minAllele = cVals[4];
					String majAllele = cVals[5];
					Double chiSqr = cVals[6] != null ? Double.parseDouble(cVals[6]) : Double.NaN;
					Double pVal = cVals[7] != null ? Double.parseDouble(cVals[7]) : Double.NaN;
					Double ORAAaa = cVals[8] != null ? Double.parseDouble(cVals[8]) : Double.NaN;
					Double ORAaaa = cVals[9] != null ? Double.parseDouble(cVals[9]) : Double.NaN;

					row[0] = markerId;
					row[1] = rsId;
					row[2] = chr;
					row[3] = position;
					row[4] = minAllele;
					row[5] = majAllele;

					Double chiSqr_f;
					Double pVal_f;
					try {
						chiSqr_f = Double.parseDouble(dfRound.format(chiSqr));
					} catch (NumberFormatException ex) {
						chiSqr_f = chiSqr;
						log.warn(null, ex);
					}
					try {
						pVal_f = Double.parseDouble(dfSci.format(pVal));
					} catch (NumberFormatException ex) {
						pVal_f = pVal;
						log.warn(null, ex);
					}

					row[6] = chiSqr_f;
					row[7] = pVal_f;
					row[8] = ORAAaa;
					row[9] = ORAaaa;

					row[10] = "";
					row[11] = Text.Reports.queryDB;

					tableRowAL.add(row);
					count++;
				}
				inputFileReader.close();

				Object[][] tableMatrix = new Object[tableRowAL.size()][11];
				for (int i = 0; i < tableRowAL.size(); i++) {
					tableMatrix[i] = tableRowAL.get(i);
				}

				String[] columns = new String[]{Text.Reports.markerId,
					Text.Reports.rsId,
					Text.Reports.chr,
					Text.Reports.pos,
					Text.Reports.minAallele,
					Text.Reports.majAallele,
					Text.Reports.chiSqr,
					Text.Reports.pVal,
					Text.Reports.ORAAaa,
					Text.Reports.ORAaaa,
					Text.Reports.zoom,
					Text.Reports.externalResource};


				TableModel model = new DefaultTableModel(tableMatrix, columns);
				tbl_ReportTable.setModel(model);

				//<editor-fold defaultstate="expanded" desc="Linux Sorter">
//				if (!cGlobal.OSNAME.contains("Windows")){
//					RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				TableRowSorter sorter = new TableRowSorter(model) {
					private Comparator<Object> comparator = new Comparator<Object>() {
						public int compare(Object o1, Object o2) {
							try {
								Double d1 = Double.parseDouble(o1.toString());
								Double d2 = Double.parseDouble(o2.toString());
								return d1.compareTo(d2);
							} catch (NumberFormatException ex) {
								log.warn(null, ex);
								try {
									Integer i1 = Integer.parseInt(o1.toString());
									Integer i2 = Integer.parseInt(o2.toString());
									return i1.compareTo(i2);
								} catch (Exception ex1) {
									log.warn(null, ex1);
									return o1.toString().compareTo(o2.toString());
								}
							}
						}
					};

					@Override
					public Comparator getComparator(int column) {
						return comparator;
					}

					@Override
					public boolean useToString(int column) {
						return false;
					}
				};

				tbl_ReportTable.setRowSorter(sorter);
//				}
				//</editor-fold>

			}
		} catch (IOException ex) {
			log.error(null, ex);
		} catch (Exception ex) {
			log.error(null, ex);
		}
	}
}
