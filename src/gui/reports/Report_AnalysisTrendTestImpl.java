/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.reports;

import global.Text;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public final class Report_AnalysisTrendTestImpl extends Report_Analysis {


    public Report_AnalysisTrendTestImpl(final int _studyId, final String _analysisFileName, final int _opId, String _NRows) {
        studyId = _studyId;
        opId = _opId;
        NRows = _NRows;
        analysisFileName = _analysisFileName;

        tbl_ReportTable.setDefaultRenderer(Object.class, new gui.utils.RowRendererTrendTestWithZoomQueryDB());
        tbl_ReportTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    int rowIndex = tbl_ReportTable.getSelectedRow();
                    int colIndex = tbl_ReportTable.getSelectedColumn();
                    if(chrSetInfoLHM==null || chrSetInfoLHM.isEmpty()){
                        initChrSetInfo();
                    }

                    if (colIndex == 8) {    //Zoom
                        setCursor(gui.utils.CursorUtils.waitCursor);
                        long markerPhysPos = (Long) tbl_ReportTable.getValueAt(rowIndex, 3); //marker physical position in chromosome
                        String chr = tbl_ReportTable.getValueAt(rowIndex, 2).toString(); //Chromosome

                        int[] chrInfo = (int[]) chrSetInfoLHM.get(chr); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
                        int nbMarkers = (Integer) chrInfo[0];
                        int startPhysPos = (Integer) chrInfo[1];
                        int maxPhysPos = (Integer) chrInfo[2];
                        double avgMarkersPerPhysPos = (double) nbMarkers / (maxPhysPos-startPhysPos);
                        int requestedWindowSize = Math.abs((int) Math.round(ManhattanPlotZoom.defaultMarkerNb / avgMarkersPerPhysPos));

                        gui.GWASpiExplorerPanel.pnl_Content = new gui.reports.ManhattanPlotZoom(opId,
                                                                                             chr,
                                                                                             tbl_ReportTable.getValueAt(rowIndex, 0).toString(), //MarkerID
                                                                                             markerPhysPos,
                                                                                             requestedWindowSize, //requested window size in phys positions
                                                                                             txt_NRows.getText());
                        gui.GWASpiExplorerPanel.scrl_Content.setViewportView(gui.GWASpiExplorerPanel.pnl_Content);
                    }
                    if (colIndex == 9) {    //Show selected resource database
                        gui.utils.URLInDefaultBrowser.browseGenericURL(gui.utils.LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
                                                                                                                       tbl_ReportTable.getValueAt(rowIndex, 2).toString(), //chr
                                                                                                                       tbl_ReportTable.getValueAt(rowIndex, 1).toString(), //rsId
                                                                                                                       (Long) tbl_ReportTable.getValueAt(rowIndex, 3)) //pos
                                                                                                                       );
                    }
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
        });

        String reportName = gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent().toString();
        reportName = reportName.substring(reportName.indexOf("-")+2);
        String reportPath = "";
        try {
            reportPath = global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        reportFile = new File(reportPath+analysisFileName);

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
                ArrayList tableRowAL = new ArrayList();
                String l;
                String[] cVals = null;
                String header = inputBufferReader.readLine();
                int count = 0;
                while ((l = inputBufferReader.readLine()) != null && count<getRowsNb) {
                    Object[] row = new Object[10];

                    cVals = l.split(constants.cImport.Separators.separators_SpaceTab_rgxp);

                    String markerId = cVals[0];
                    String rsId = cVals[1];
                    String chr = cVals[2];
                    long position = Long.parseLong(cVals[3]);
                    String minAllele = cVals[4];
                    String majAllele = cVals[5];
                    Double chiSqr = cVals[6]!=null ? Double.parseDouble(cVals[6]) : Double.NaN;
                    Double pVal = cVals[7]!=null ? Double.parseDouble(cVals[7]) : Double.NaN;

                    row[0]=markerId;
                    row[1]=rsId;
                    row[2]=chr;
                    row[3]=position;
                    row[4]=minAllele;
                    row[5]=majAllele;
                    

//                    if (!constants.cGlobal.OSNAME.contains("Windows")){
                        Double chiSqr_f=Double.NaN;
                        Double pVal_f=Double.NaN;
                        try {
                            chiSqr_f = Double.parseDouble(dfRound.format(chiSqr));
                        } catch (NumberFormatException numberFormatException) {
                            chiSqr_f = chiSqr;
                        }
                        try {
                            pVal_f = Double.parseDouble(dfSci.format(pVal));
                        } catch (NumberFormatException numberFormatException) {
                            pVal_f = pVal;
                        }

                        row[6]=chiSqr_f;
                        row[7]=pVal_f;

//                    } else {
//                        row[6]=dfRound.format(chiSqr);
//                        row[7]=dfSci.format(pVal);
//                        row[8]=dfRound.format(or);
//                    }

                    row[8]="";
                    row[9]=Text.Reports.queryDB;

                    tableRowAL.add(row);
                    count++;
                }

                Object[][] tableMatrix = new Object[tableRowAL.size()][11];
                for(int i=0;i<tableRowAL.size();i++){
                    tableMatrix[i] = (Object[]) tableRowAL.get(i);
                }

                String [] columns = new String [] {Text.Reports.markerId,
                                                   Text.Reports.rsId,
                                                   Text.Reports.chr,
                                                   Text.Reports.pos,
                                                   Text.Reports.minAallele,
                                                   Text.Reports.majAallele,
                                                   Text.Reports.trendTest,
                                                   Text.Reports.pVal,
                                                   Text.Reports.zoom,
                                                   Text.Reports.externalResource};


                TableModel model = new DefaultTableModel(tableMatrix, columns);
                tbl_ReportTable.setModel(model);

                //<editor-fold defaultstate="collapsed" desc="Linux Sorter">
//                if (!constants.cGlobal.OSNAME.contains("Windows")){
                    //RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
                    TableRowSorter sorter = new TableRowSorter(model)
                    {

                        Comparator<Object> comparator = new Comparator<Object>()
                        {
                            public int compare(Object o1, Object o2) {
                                try {
                                    Double d1 = Double.parseDouble(o1.toString());
                                    Double d2 = Double.parseDouble(o2.toString());
                                    return d1.compareTo(d2);
                                } catch (NumberFormatException numberFormatException) {
                                    try {
                                        Integer i1 = Integer.parseInt(o1.toString());
                                        Integer i2 = Integer.parseInt(o2.toString());
                                        return i1.compareTo(i2);
                                    } catch (Exception e) {
                                        return o1.toString().compareTo(o2.toString());
                                    }
                                }
                            }
                        };

                        public Comparator getComparator(int column)
                        {
                            return comparator;
                        }

                        public boolean useToString(int column)
                        {
                            return false;
                        }
                    };

                    tbl_ReportTable.setRowSorter(sorter);
//                }
                //</editor-fold>

            }
            inputFileReader.close();
        } catch (IOException ex) {
            //Logger.getLogger(Report_AnalysisAllelicTestImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            //Logger.getLogger(Report_QAMarkersSummary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    Comparator<String> comparator = new Comparator<String>() {
        public int compare(String s1, String s2) {
            String[] strings1 = s1.split("\\s");
            String[] strings2 = s2.split("\\s");
            return strings1[strings1.length - 1]
                .compareTo(strings2[strings2.length - 1]);
        }
    };

}
