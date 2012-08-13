/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.reports;

import global.Text;
import gui.MatrixAnalysePanel;
import gui.utils.HelpURLs;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.Operation;
import netCDF.operations.OperationSet;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public abstract class Report_Analysis extends javax.swing.JPanel {

    // Variables declaration - do not modify
    public static int studyId;
    public static int opId;
    public static String analysisFileName;
    public static String NRows;
    public static LinkedHashMap chrSetInfoLHM = new LinkedHashMap();

    protected File reportFile;

    protected javax.swing.JButton btn_Get;
    protected javax.swing.JButton btn_Save;
    protected javax.swing.JButton btn_Back;
    protected javax.swing.JButton btn_Help;
    protected javax.swing.JPanel pnl_Footer;
    protected javax.swing.JLabel lbl_suffix1;
    public static javax.swing.JPanel pnl_Summary;
    protected javax.swing.JPanel pnl_SearchDB;
    public static javax.swing.JComboBox cmb_SearchDB;
    protected javax.swing.JScrollPane scrl_ReportTable;
    protected javax.swing.JTable tbl_ReportTable;
    protected javax.swing.JTextField txt_NRows;
    protected javax.swing.JTextField txt_PvalThreshold;
    // End of variables declaration

    //protected Report_Analysis_old() {
    protected Report_Analysis() {

        String reportName = gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent().toString();
        reportName = reportName.substring(reportName.indexOf("-")+2);

        String reportPath = "";
        try {
            reportPath = global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        reportFile = new File(reportPath+analysisFileName);


        pnl_Summary = new javax.swing.JPanel();
        txt_NRows = new javax.swing.JTextField();
        txt_NRows.setInputVerifier(new gui.utils.IntegerInputVerifier());
        txt_NRows.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater( new Runnable() {
                    @Override
                    public void run() {
                        txt_NRows.selectAll();
                    }
                });
            }
        });
        lbl_suffix1 = new javax.swing.JLabel();
        txt_PvalThreshold = new javax.swing.JTextField();
        btn_Get = new javax.swing.JButton();
        
        pnl_SearchDB = new javax.swing.JPanel();
        pnl_SearchDB.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.Reports.externalResourceDB));
        cmb_SearchDB = new javax.swing.JComboBox();
        cmb_SearchDB.setModel(new javax.swing.DefaultComboBoxModel(gui.utils.LinksExternalResouces.getLinkNames()));
        
        scrl_ReportTable = new javax.swing.JScrollPane();
        tbl_ReportTable = new javax.swing.JTable(){
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tbl_ReportTable.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent me) {
                displayColumnCursor(me);
            }
        });
        
        //TO DISABLE COLUMN MOVING (DON'T WANT TO MOVE BEHIND COLUMN 9)
        tbl_ReportTable.getTableHeader().setReorderingAllowed(false);

        pnl_Footer = new javax.swing.JPanel();
        btn_Save = new javax.swing.JButton();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Reports.report+": "+reportName, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N

        pnl_Summary.setBorder(javax.swing.BorderFactory.createTitledBorder(Text.Reports.summary));

        try {
            Integer.parseInt(NRows);
            txt_NRows.setText(NRows);
        } catch (NumberFormatException numberFormatException) {
            txt_NRows.setText("100");
        }

        txt_NRows.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txt_NRows.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                int key = e.getKeyChar();
                if (key == 10) {
                    actionLoadReport();
                }
            }
        });
        lbl_suffix1.setText(Text.Reports.radio1Suffix_pVal);
        txt_PvalThreshold.setEnabled(false);


        btn_Get.setText(Text.All.get);
        btn_Get.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //actionLoadReport(evt);
                actionLoadReport();
            }
        });

        //<editor-fold defaultstate="collapsed" desc="LAYOUT SUMMARY">
        javax.swing.GroupLayout pnl_SearchDBLayout = new javax.swing.GroupLayout(pnl_SearchDB);
        pnl_SearchDB.setLayout(pnl_SearchDBLayout);
        pnl_SearchDBLayout.setHorizontalGroup(
                pnl_SearchDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SearchDBLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmb_SearchDB, 0, 357, Short.MAX_VALUE)
                .addContainerGap())
                );
        pnl_SearchDBLayout.setVerticalGroup(
                pnl_SearchDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SearchDBLayout.createSequentialGroup()
                .addComponent(cmb_SearchDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
                );

        javax.swing.GroupLayout pnl_SummaryLayout = new javax.swing.GroupLayout(pnl_Summary);
        pnl_Summary.setLayout(pnl_SummaryLayout);
        pnl_SummaryLayout.setHorizontalGroup(
                pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_suffix1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_Get, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        pnl_SummaryLayout.setVerticalGroup(
                pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_SummaryLayout.createSequentialGroup()
                .addGroup(pnl_SummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txt_NRows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lbl_suffix1)
                .addComponent(btn_Get))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        //</editor-fold>


        tbl_ReportTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null, null, null, "Go!"}
                },
                new String [] {"","","",""}
        ));


        scrl_ReportTable.setViewportView(tbl_ReportTable);


        //<editor-fold defaultstate="collapsed" desc="FOOTER">

        btn_Save.setText(global.Text.All.save);
        btn_Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int decision = gui.utils.Dialogs.showOptionDialogue(global.Text.All.save, global.Text.Reports.selectSaveMode, global.Text.Reports.currentReportView, global.Text.Reports.completeReport, global.Text.All.cancel);

                switch (decision){
                    case 0:
                        actionSaveReportViewAs(studyId, analysisFileName);
                        break;
                    case 1:
                        actionSaveCompleteReportAs(studyId, analysisFileName);
                        break;
                    default:
                        break;
                }
            }
        });

        btn_Back.setText(Text.All.Back);
        btn_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionBack();
                } catch (IOException ex) {
                    Logger.getLogger(Report_QASamplesSummary.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        btn_Help.setText(Text.Help.help);
        btn_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionHelp();
            }
        });

        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_Help, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 321, Short.MAX_VALUE)
                .addComponent(btn_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
                );

        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});

        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_Save)
                .addComponent(btn_Back)
                .addComponent(btn_Help))
                );

        //</editor-fold>


        //<editor-fold defaultstate="collapsed" desc="LAYOUT">
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_Summary, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_SearchDB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(scrl_ReportTable, javax.swing.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE))
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(pnl_SearchDB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Summary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20)
                .addComponent(scrl_ReportTable, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                .addContainerGap())
                );


        try {
            if(chrSetInfoLHM==null){
                initChrSetInfo();
            }
        } catch (IOException ex) {
            Logger.getLogger(Report_Analysis.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected abstract void actionLoadReport();

    protected static void initChrSetInfo() throws IOException{
        OperationSet opSet = new OperationSet(studyId, opId);
        chrSetInfoLHM = opSet.getChrInfoSetLHM(); //Nb of markers, first physical position, last physical position, start index number in MarkerSet,
    }

    Comparator<String> comparator = new Comparator<String>() {
        public int compare(String s1, String s2) {
            String[] strings1 = s1.split("\\s");
            String[] strings2 = s2.split("\\s");
            return strings1[strings1.length - 1]
                .compareTo(strings2[strings2.length - 1]);
        }
    };

   private void actionSaveCompleteReportAs(int studyId, String chartPath) {
        try {
            String reportPath = global.Config.getConfigValue("ReportsDir", "") + "/STUDY_" + studyId + "/";
            File origFile = new File(reportPath+chartPath);
            File newFile = new File(gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath()+"/"+chartPath);
            if (origFile.exists()) {
                global.Utils.copyFile(origFile, newFile);
            }
        } catch (IOException ex) {
            gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
            Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            //gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
            //Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
            Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void actionSaveReportViewAs(int studyId, String chartPath) {
        try
        {
            String newPath = gui.utils.Dialogs.selectDirectoryDialogue(JOptionPane.OK_OPTION).getPath()+"/"+txt_NRows.getText()+"rows_"+chartPath;
            File newFile = new File(newPath);
            FileWriter writer = new FileWriter(newFile);

            StringBuilder tableData = new StringBuilder();
            //HEADER
            for(int k=0;k<tbl_ReportTable.getColumnCount()-3;k++){ //Don't want last 3 columns
                tableData.append(tbl_ReportTable.getColumnName(k));
                if(k!=tbl_ReportTable.getColumnCount()-4){
                    tableData.append("\t");
                }
            }
            tableData.append("\n");
            writer.write(tableData.toString());

            //TABLE CONTENT
            for(int rowNb = 0; rowNb < tbl_ReportTable.getModel().getRowCount(); rowNb++) {
                tableData = new StringBuilder();

                for(int colNb = 0; colNb < tbl_ReportTable.getModel().getColumnCount()-3; colNb++) { //Don't want last 3 columns
                    String curVal = tbl_ReportTable.getValueAt(rowNb,colNb).toString();

                    if(curVal == null) {
                        curVal = "";
                    }

                    tableData.append(curVal);
                    if(colNb!=tbl_ReportTable.getModel().getColumnCount()-4){
                        tableData.append("\t");
                    }
                }
                tableData.append("\n");
                writer.write(tableData.toString());
            }

            writer.flush();
            writer.close();

        } catch (NullPointerException ex) {
            //gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
            //Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
            //Logger.getLogger(ChartDefaultDisplay.class.getName()).log(Level.SEVERE, null, e);
        }
    }


    private void actionBack() throws IOException {
        Operation op = new Operation(opId);
        gui.GWASpiExplorerPanel.tree.setSelectionPath(gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
        gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(op.getParentMatrixId(), opId);
        gui.GWASpiExplorerPanel.scrl_Content.setViewportView(gui.GWASpiExplorerPanel.pnl_Content);
    }

    private void actionHelp() {
        try {
            gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.assocReport);
        } catch (Exception ex) {
        }
    }


    /**
     * Method to change cursor based on some arbitrary rule.
     */
    protected void displayColumnCursor( MouseEvent me ) {
        Point p = me.getPoint();
        int column =tbl_ReportTable.columnAtPoint(p);
        int row = tbl_ReportTable.rowAtPoint(p);
        String columnName = tbl_ReportTable.getColumnName(column);
        if(!getCursor().equals(gui.utils.CursorUtils.waitCursor)){
            if (columnName.equals(Text.Reports.zoom)) {
                setCursor(gui.utils.CursorUtils.handCursor);
            } else if (columnName.equals(Text.Reports.externalResource)) {
                setCursor(gui.utils.CursorUtils.handCursor);
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
        
    }

}
