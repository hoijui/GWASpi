package gui;

import global.Text;
import constants.cNetCDF.Defaults.OPType;
import gui.utils.HelpURLs;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import model.Operation;
import model.OperationsList;
import netCDF.matrices.MatrixMetadata;
import netCDF.operations.OperationManager;
import netCDF.operations.GWASinOneGOParams;
import samples.SamplesParser;
import threadbox.MultiOperations;
import ucar.ma2.InvalidRangeException;


/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixAnalysePanel extends javax.swing.JPanel {

    // Variables declaration - do not modify
    private model.Matrix parentMatrix;
    private model.Operation currentOP;
    private LinkedHashMap treeChildrenLHM = new LinkedHashMap();

    private javax.swing.JButton btn_1_1;
    private javax.swing.JButton btn_1_2;
    private javax.swing.JButton btn_1_3;
    private javax.swing.JButton btn_1_4;
    private javax.swing.JButton btn_1_5;
    
    private javax.swing.JButton btn_Back;
    private javax.swing.JButton btn_DeleteOperation;
    private javax.swing.JButton btn_Help;
    private javax.swing.JPanel pnl_Spacer;
    private javax.swing.JPanel pnl_NewOperation;
    private javax.swing.JPanel pnl_Buttons;
    private javax.swing.JPanel pnl_Footer;
    private javax.swing.JPanel pnl_MatrixDesc;
    private javax.swing.JScrollPane scrl_MatrixDesc;
    private javax.swing.JScrollPane scrl_MatrixOperations;
    private javax.swing.JTable tbl_MatrixOperations;
    private javax.swing.JTextArea txtA_Description;

    public GWASinOneGOParams gwasParams = new GWASinOneGOParams();
    private Object op;
    // End of variables declaration


    @SuppressWarnings("unchecked")
    public MatrixAnalysePanel(int _matrixId, int _opId) throws IOException {

        parentMatrix = new model.Matrix(_matrixId);
        if(_opId!=Integer.MIN_VALUE){
            currentOP = new model.Operation(_opId);
        }
        DefaultMutableTreeNode matrixNode = (DefaultMutableTreeNode) gui.GWASpiExplorerPanel.tree.getLastSelectedPathComponent();
        treeChildrenLHM = gui.utils.NodeToPathCorrespondence.buildNodeToPathCorrespondence(matrixNode, true);
        

        pnl_MatrixDesc = new javax.swing.JPanel();
        scrl_MatrixDesc = new javax.swing.JScrollPane();
        txtA_Description = new javax.swing.JTextArea();
        scrl_MatrixOperations = new javax.swing.JScrollPane();
        tbl_MatrixOperations = new javax.swing.JTable(){
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; //Renders column 0 uneditable.
            }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent && getValueAt(rowIndex, vColIndex)!=null){
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText("<html>"+getValueAt(rowIndex, vColIndex).toString().replaceAll("\n", "<br>")+"</html>");
                }
                return c;
            }
        };
        tbl_MatrixOperations.setDefaultRenderer(Object.class, new gui.utils.RowRendererDefault());


        btn_DeleteOperation = new javax.swing.JButton();
        pnl_Footer = new javax.swing.JPanel();
        btn_Back = new javax.swing.JButton();
        btn_Help = new javax.swing.JButton();
        pnl_Spacer = new javax.swing.JPanel();
        pnl_Buttons = new javax.swing.JPanel();
        pnl_NewOperation = new javax.swing.JPanel();
        btn_1_1 = new javax.swing.JButton();
        btn_1_2 = new javax.swing.JButton();
        btn_1_3 = new javax.swing.JButton();
        btn_1_4 = new javax.swing.JButton();
        btn_1_5 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.analyseData, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("FreeSans", 1, 18))); // NOI18N


        txtA_Description.setColumns(20);
        txtA_Description.setRows(5);
        txtA_Description.setEditable(false);
        txtA_Description.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.All.description, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        if(_opId!=Integer.MIN_VALUE){
            pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.operation+": "+currentOP.getOperationFriendlyName()+", "+Text.Operation.operationId+": "+currentOP.getOperationId(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
            txtA_Description.setText(currentOP.getDescription().toString());
        } else {
            pnl_MatrixDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Matrix.matrix+": "+parentMatrix.matrixMetadata.getMatrixFriendlyName(), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
            txtA_Description.setText(parentMatrix.matrixMetadata.getDescription().toString());
        }


        scrl_MatrixDesc.setViewportView(txtA_Description);

        Object[][] tableMatrix;
        if(currentOP!=null){
            tableMatrix = model.OperationsList.getOperationsTable(_matrixId, currentOP.getOperationId());
        } else {
            tableMatrix = model.OperationsList.getOperationsTable(_matrixId);
        }

        tbl_MatrixOperations.setModel(new javax.swing.table.DefaultTableModel(
            tableMatrix,
            new String [] {
                Text.Operation.operationId,  Text.Operation.operationName, Text.All.description, Text.All.createDate
            }
        ));
        scrl_MatrixOperations.setViewportView(tbl_MatrixOperations);
        btn_DeleteOperation.setText(Text.Operation.deleteOperation);
        btn_DeleteOperation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionDeleteOperation(evt);
            }
        });

        //<editor-fold defaultstate="collapsed" desc="LAYOUT MATRIX DESC">
        javax.swing.GroupLayout pnl_MatrixDescLayout = new javax.swing.GroupLayout(pnl_MatrixDesc);
        pnl_MatrixDesc.setLayout(pnl_MatrixDescLayout);
        pnl_MatrixDescLayout.setHorizontalGroup(
                pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_MatrixDescLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addComponent(btn_DeleteOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
                );
        pnl_MatrixDescLayout.setVerticalGroup(
                pnl_MatrixDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_MatrixDescLayout.createSequentialGroup()
                .addComponent(scrl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrl_MatrixOperations, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_DeleteOperation)
                .addContainerGap())
                );
        //</editor-fold>

        pnl_NewOperation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, Text.Operation.newOperation, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("DejaVu Sans", 1, 13))); // NOI18N
        pnl_NewOperation.setMaximumSize(new java.awt.Dimension(32767, 100));
        pnl_NewOperation.setPreferredSize(new java.awt.Dimension(926, 100));

        btn_1_1.setText(Text.Operation.gwasInOneGo);
        if(currentOP==null){
            btn_1_1.setEnabled(true);
        } else {
            btn_1_1.setEnabled(false);
        }

        btn_1_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionGWASInOneGo();
                } catch (InvalidRangeException ex) {
                    gui.utils.Dialogs.showWarningDialogue(global.Text.Operation.warnOperationError);
                    System.out.println(global.Text.All.warnLoadError);
                    Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    gui.utils.Dialogs.showWarningDialogue(global.Text.Operation.warnOperationError);
                    System.out.println(global.Text.All.warnLoadError);
                    Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        btn_1_2.setText(Text.Operation.htmlGTFreqAndHW);
        if(currentOP==null){
            btn_1_2.setEnabled(true);
        } else {
            btn_1_2.setEnabled(false);
            btn_1_2.setForeground(Color.LIGHT_GRAY);
        }
        btn_1_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionGenFreqAndHW();
                } catch (Exception ex) {
                    gui.utils.Dialogs.showWarningDialogue(global.Text.Operation.warnOperationError);
                    System.out.println(global.Text.All.warnLoadError);
                    Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });


        btn_1_3.setText(Text.Operation.htmlAllelicAssocTest);
        btn_1_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionAllelicTests();
                } catch (Exception ex) {
                    //Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                    gui.utils.Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
                }
            }
        });

        btn_1_4.setText(Text.Operation.htmlGenotypicTest);
        btn_1_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionGenotypicTests();
                } catch (Exception ex) {
                    //Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                    gui.utils.Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
                }
            }
        });


        btn_1_5.setText(Text.Operation.htmlTrendTest);
        btn_1_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionTrendTests();
                    
                } catch (Exception ex) {
                    //Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
                    gui.utils.Dialogs.showWarningDialogue(Text.Operation.warnOperationError);
                }
            }
        });



        //<editor-fold defaultstate="collapsed" desc="LAYOUT BUTTONS">
        javax.swing.GroupLayout pnl_SpacerLayout = new javax.swing.GroupLayout(pnl_Spacer);
        pnl_Spacer.setLayout(pnl_SpacerLayout);
        pnl_SpacerLayout.setHorizontalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 46, Short.MAX_VALUE)
                );
        pnl_SpacerLayout.setVerticalGroup(
                pnl_SpacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 124, Short.MAX_VALUE)
                );


        javax.swing.GroupLayout pnl_ButtonsLayout = new javax.swing.GroupLayout(pnl_Buttons);
        pnl_Buttons.setLayout(pnl_ButtonsLayout);
        pnl_ButtonsLayout.setHorizontalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btn_1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(108, 108, 108))
                );


        pnl_ButtonsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_1_1, btn_1_2, btn_1_3, btn_1_4});

        pnl_ButtonsLayout.setVerticalGroup(
                pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_ButtonsLayout.createSequentialGroup()
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnl_ButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(btn_1_2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_1_4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

        javax.swing.GroupLayout pnl_NewOperationLayout = new javax.swing.GroupLayout(pnl_NewOperation);
        pnl_NewOperation.setLayout(pnl_NewOperationLayout);
        pnl_NewOperationLayout.setHorizontalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        pnl_NewOperationLayout.setVerticalGroup(
                pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NewOperationLayout.createSequentialGroup()
                .addGroup(pnl_NewOperationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_Buttons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_Spacer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
                );
        //</editor-fold>


        btn_Back.setText("Back");
        btn_Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    actionGoBack(evt);
                } catch (IOException ex) {
                    Logger.getLogger(MatrixTrafoPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        btn_Help.setText("Help");
        btn_Help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionHelp(evt);
            }
        });


        //<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
        javax.swing.GroupLayout pnl_FooterLayout = new javax.swing.GroupLayout(pnl_Footer);
        pnl_Footer.setLayout(pnl_FooterLayout);
        pnl_FooterLayout.setHorizontalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnl_FooterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_Back, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 565, Short.MAX_VALUE)
                .addComponent(btn_Help)
                .addContainerGap())
                );


        pnl_FooterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_Back, btn_Help});

        pnl_FooterLayout.setVerticalGroup(
                pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
                .addContainerGap(53, Short.MAX_VALUE)
                .addGroup(pnl_FooterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_Back)
                .addComponent(btn_Help)))
                );
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="LAYOUT">
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(pnl_MatrixDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_NewOperation, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_Footer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        //</editor-fold>
    }


    //<editor-fold defaultstate="collapsed" desc="ANALYSIS">

    private void actionAllelicTests() throws IOException, InvalidRangeException {

        int censusOPId = Integer.MIN_VALUE;
        Operation markerCensusOP = null;
        if(currentOP!=null){
            censusOPId = currentOP.getOperationId();
        } else {
            //REQUEST WHICH CENSUS TO USE
            ArrayList censusTypesAL = new ArrayList();
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            markerCensusOP = gui.utils.Dialogs.showOperationCombo(parentMatrix.getMatrixId(), censusTypesAL, Text.Operation.GTFreqAndHW);
            if (markerCensusOP != null) {
                censusOPId = markerCensusOP.getOperationId();
            }
        }
	int hwOPId = Integer.MIN_VALUE;

        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.waitCursor);
        HashSet affectionStates = samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.defaultCursor);

        if (affectionStates.contains("1") && affectionStates.contains("2")){

            ArrayList necessaryOPsAL = new ArrayList();
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
            ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

            //WHAT TO DO IF OPs ARE MISSING
            int decision = JOptionPane.YES_OPTION;
            if (missingOPsAL.size() > 0) {
                if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
                    || missingOPsAL.contains(OPType.MARKER_QA.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing an "+Text.Operation.allelicAssocTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                        && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing an "+Text.Operation.allelicAssocTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
                           && !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                           && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing an "+Text.Operation.allelicAssocTest+" you must launch\n a '"+Text.Operation.hardyWeiberg+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(), 
                                                    parentMatrix.getMatrixId(),
                                                    censusOPId);
                    decision = JOptionPane.NO_OPTION;
                }
            }


            //DO ALLELIC TEST
            if (decision == JOptionPane.YES_OPTION) {

                boolean reProceed = true;
                if (censusOPId == Integer.MIN_VALUE) {
                    reProceed = false;
                }

                if (reProceed) {
                    gwasParams = gui.utils.MoreAssocInfo.showAssocInfo_Modal();
                }

                if (gwasParams.proceed) {
                    gui.ProcessTab.showTab();
                    //GET HW OPERATION
                    OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
                    for(Operation currentHWop : hwOPList.operationsListAL) {
                        //REQUEST WHICH HW TO USE
                        if (currentHWop != null) {
                            hwOPId = currentHWop.getOperationId();
                        } else {
                            reProceed = false;
                        }
                    }

                    if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

                        //>>>>>> START THREADING HERE <<<<<<<
                        MultiOperations.doAllelicAssociationTest(parentMatrix.getStudyId(),
                                                          parentMatrix.getMatrixId(),
                                                          censusOPId,
                                                          hwOPId,
                                                          gwasParams);

                    }
                }

            }
        } else {
            gui.utils.Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
        }
    }


    private void actionGenotypicTests() throws IOException, InvalidRangeException {
        int censusOPId = Integer.MIN_VALUE;
        Operation markerCensusOP = null;
        if(currentOP!=null){
            censusOPId = currentOP.getOperationId();
        } else {
            //REQUEST WHICH CENSUS TO USE
            ArrayList censusTypesAL = new ArrayList();
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            markerCensusOP = gui.utils.Dialogs.showOperationCombo(parentMatrix.getMatrixId(), censusTypesAL, Text.Operation.GTFreqAndHW);
            if (markerCensusOP != null) {
                censusOPId = markerCensusOP.getOperationId();
            }
        }
	int hwOPId = Integer.MIN_VALUE;

        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.waitCursor);
        HashSet affectionStates = samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.defaultCursor);

        if (affectionStates.contains("1") && affectionStates.contains("2")){

            ArrayList necessaryOPsAL = new ArrayList();
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
            ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

            //WHAT TO DO IF OPs ARE MISSING
            int decision = JOptionPane.YES_OPTION;
            if (missingOPsAL.size() > 0) {
                if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
                    || missingOPsAL.contains(OPType.MARKER_QA.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.genoAssocTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                        && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.genoAssocTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
                           && !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                           && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.genoAssocTest+" you must launch\n a '"+Text.Operation.hardyWeiberg+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(),
                                                    parentMatrix.getMatrixId(),
                                                    censusOPId);
                    decision = JOptionPane.NO_OPTION;
                }
            }


            //DO TEST
            if (decision == JOptionPane.YES_OPTION) {

                boolean reProceed = true;
                if (censusOPId == Integer.MIN_VALUE) {
                    reProceed = false;
                }

                if (reProceed) {
                    gwasParams = gui.utils.MoreAssocInfo.showAssocInfo_Modal();
                }

                if (gwasParams.proceed) {
                    gui.ProcessTab.showTab();
                    //GET HW OPERATION
                    OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
                    for(Operation currentHWop : hwOPList.operationsListAL) {
                        //REQUEST WHICH HW TO USE
                        if (currentHWop != null) {
                            hwOPId = currentHWop.getOperationId();
                        } else {
                            reProceed = false;
                        }
                    }

                    if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

                        //>>>>>> START THREADING HERE <<<<<<<
                        MultiOperations.doGenotypicAssociationTest(parentMatrix.getStudyId(),
                                                          parentMatrix.getMatrixId(),
                                                          censusOPId,
                                                          hwOPId,
                                                          gwasParams);

                    }
                }

            }
        } else {
            gui.utils.Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
        }
    }


    private void actionTrendTests() throws IOException, InvalidRangeException {

        int censusOPId = Integer.MIN_VALUE;
        Operation markerCensusOP = null;
        if(currentOP!=null){
            censusOPId = currentOP.getOperationId();
        } else {
            //REQUEST WHICH CENSUS TO USE
            ArrayList censusTypesAL = new ArrayList();
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            censusTypesAL.add(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            markerCensusOP = gui.utils.Dialogs.showOperationCombo(parentMatrix.getMatrixId(), censusTypesAL, Text.Operation.GTFreqAndHW);
            if (markerCensusOP != null) {
                censusOPId = markerCensusOP.getOperationId();
            }
        }
	int hwOPId = Integer.MIN_VALUE;

        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.waitCursor);
        HashSet affectionStates = samples.SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId());
        gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.defaultCursor);

        if (affectionStates.contains("1") && affectionStates.contains("2")){

            ArrayList necessaryOPsAL = new ArrayList();
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_PHENOTYPE.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString());
            ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

            //WHAT TO DO IF OPs ARE MISSING
            int decision = JOptionPane.YES_OPTION;
            if (missingOPsAL.size() > 0) {
                if (missingOPsAL.contains(OPType.SAMPLE_QA.toString())
                    || missingOPsAL.contains(OPType.MARKER_QA.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.trendTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                        && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString())) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.trendTest+" you must launch\n a '"+Text.Operation.GTFreqAndHW+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    decision = JOptionPane.NO_OPTION;
                } else if (missingOPsAL.contains(OPType.HARDY_WEINBERG.toString())
                           && !(missingOPsAL.contains(OPType.MARKER_CENSUS_BY_AFFECTION.toString())
                           && missingOPsAL.contains(OPType.MARKER_CENSUS_BY_PHENOTYPE.toString()))) {
                    gui.utils.Dialogs.showWarningDialogue("Before performing a "+Text.Operation.trendTest+" you must launch\n a '"+Text.Operation.hardyWeiberg+"' first or perform a '"+Text.Operation.gwasInOneGo+"' instead.");
                    MultiOperations.doHardyWeinberg(parentMatrix.getStudyId(),
                                                    parentMatrix.getMatrixId(),
                                                    censusOPId);
                    decision = JOptionPane.NO_OPTION;
                }
            }


            //DO TEST
            if (decision == JOptionPane.YES_OPTION) {

                boolean reProceed = true;
                if (censusOPId == Integer.MIN_VALUE) {
                    reProceed = false;
                }

                if (reProceed) {
                    gwasParams = gui.utils.MoreAssocInfo.showAssocInfo_Modal();
                }

                if (gwasParams.proceed) {
                    gui.ProcessTab.showTab();
                    //GET HW OPERATION
                    OperationsList hwOPList = new OperationsList(parentMatrix.getMatrixId(), censusOPId, OPType.HARDY_WEINBERG);
                    for(Operation currentHWop : hwOPList.operationsListAL) {
                        //REQUEST WHICH HW TO USE
                        if (currentHWop != null) {
                            hwOPId = currentHWop.getOperationId();
                        } else {
                            reProceed = false;
                        }
                    }

                    if (reProceed && censusOPId != Integer.MIN_VALUE && hwOPId != Integer.MIN_VALUE) {

                        //>>>>>> START THREADING HERE <<<<<<<
                        MultiOperations.doTrendTest(parentMatrix.getStudyId(),
                                                  parentMatrix.getMatrixId(),
                                                  censusOPId,
                                                  hwOPId,
                                                  gwasParams);

                    }
                }

            }
        } else {
            gui.utils.Dialogs.showInfoDialogue(Text.Operation.warnAffectionMissing);
        }
    }
  
    private void actionGenFreqAndHW() throws InvalidRangeException {
        int choice = JOptionPane.YES_OPTION;

        ArrayList necessaryOPsAL = new ArrayList();
        necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
        necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
        ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

        choice = gui.utils.Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB , Text.Operation.htmlAffectionFromFile, Text.All.cancel);
        File phenotypeFile = null;
        if (choice == JOptionPane.NO_OPTION) { //BY EXTERNAL PHENOTYPE FILE
            phenotypeFile = gui.utils.Dialogs.selectFilesAndDirertoriesDialogue(JOptionPane.OK_OPTION);
            if(phenotypeFile!=null){
                gwasParams = gui.utils.MoreInfoForGtFreq.showMoreInfoForQA_Modal();
                if (choice != JOptionPane.CANCEL_OPTION) {
                    gwasParams.friendlyName = gui.utils.Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
                }
            }
        } else if (choice != JOptionPane.CANCEL_OPTION) {
            gwasParams = gui.utils.MoreInfoForGtFreq.showMoreInfoForQA_Modal();
            if (choice != JOptionPane.CANCEL_OPTION) {
                gwasParams.friendlyName = gui.utils.Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
            }
        }

            if(!gwasParams.discardMarkerByMisRat){
                gwasParams.discardMarkerMisRatVal = 1;
            }
            if(!gwasParams.discardMarkerByHetzyRat){
                gwasParams.discardMarkerHetzyRatVal = 1;
            }
            if(!gwasParams.discardSampleByMisRat){
                gwasParams.discardSampleMisRatVal = 1;
            }
            if(!gwasParams.discardSampleByHetzyRat){
                gwasParams.discardSampleHetzyRatVal = 1;
            }

        if (gwasParams.proceed) {
            gui.ProcessTab.showTab();
        }


        // <editor-fold defaultstate="collapsed" desc="QA BLOCK">
        if(gwasParams.proceed && missingOPsAL.size()>0) {
            gwasParams.proceed = false;
            gwasParams.proceed = false;
            gui.utils.Dialogs.showWarningDialogue(global.Text.Operation.warnQABeforeAnything+"\n"+global.Text.Operation.willPerformOperation);
            MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
        }
        // </editor-fold>


        // <editor-fold defaultstate="collapsed" desc="GENOTYPE FREQ. & HW BLOCK">
        if(gwasParams.proceed) {
            MultiOperations.doGTFreqDoHW(parentMatrix.getStudyId(),
                                         parentMatrix.getMatrixId(),
                                         phenotypeFile,
                                         gwasParams);
        }
        // </editor-fold>


    }


    private void actionGWASInOneGo() throws InvalidRangeException {
        int choice = JOptionPane.YES_OPTION;
        try {
            ArrayList blackListOPsAL = new ArrayList();
            blackListOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_CENSUS_BY_AFFECTION.toString());

            ArrayList necessaryOPsAL = new ArrayList();
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
            ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, parentMatrix.getMatrixId());

            MatrixMetadata matrixMetadata = new MatrixMetadata(parentMatrix.getMatrixId());
            
            choice = gui.utils.Dialogs.showOptionDialogue(Text.Operation.chosePhenotype, Text.Operation.genotypeFreqAndHW, Text.Operation.htmlCurrentAffectionFromDB , Text.Operation.htmlAffectionFromFile, Text.All.cancel);
            File phenotypeFile = null;
            if (choice == JOptionPane.NO_OPTION) { //BY EXTERNAL PHENOTYPE FILE
                phenotypeFile = gui.utils.Dialogs.selectFilesAndDirertoriesDialogue(JOptionPane.OK_OPTION);
                if(phenotypeFile!=null){
                    gwasParams = gui.utils.MoreGWASinOneGoInfo.showGWASInOneGo_Modal(matrixMetadata.getTechnology().toString());
                    if (choice != JOptionPane.CANCEL_OPTION && gwasParams.proceed) {
                        gwasParams.friendlyName = gui.utils.Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
                    }
                }
            } else if (choice != JOptionPane.CANCEL_OPTION) {
                gwasParams = gui.utils.MoreGWASinOneGoInfo.showGWASInOneGo_Modal(matrixMetadata.getTechnology().toString());
                if (choice != JOptionPane.CANCEL_OPTION && gwasParams.proceed) {
                    gwasParams.friendlyName = gui.utils.Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName);
                }
            }

            if (gwasParams.proceed) {
                gui.ProcessTab.showTab();
            }

            //QA BLOCK
            if(gwasParams.proceed && missingOPsAL.size()>0) {
                gwasParams.proceed = false;
                gui.utils.Dialogs.showWarningDialogue(global.Text.Operation.warnQABeforeAnything+"\n"+global.Text.Operation.willPerformOperation);
                MultiOperations.doMatrixQAs(parentMatrix.getStudyId(), parentMatrix.getMatrixId());
            }

            //GWAS BLOCK
            if (gwasParams.proceed &&
                choice != JOptionPane.CANCEL_OPTION &&
                (gwasParams.performAllelicTests || gwasParams.performTrendTests)) { //At least one test has been picked
                System.out.println(global.Text.All.processing);
                gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.waitCursor);
                HashSet affectionStates = SamplesParser.getDBAffectionStates(parentMatrix.getMatrixId()); //use Sample Info file affection state
                gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.defaultCursor);
                if (affectionStates.contains("1") && affectionStates.contains("2")) {
                    MultiOperations.doGWASwithAlterPhenotype(parentMatrix.getStudyId(),
                                                             parentMatrix.getMatrixId(),
                                                             phenotypeFile,
                                                             gwasParams);
                } else {
                    gui.utils.Dialogs.showWarningDialogue(Text.Operation.warnAffectionMissing);
                    threadbox.MultiOperations.updateProcessOverviewStartNext();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MatrixAnalysePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HELPERS">

    private void actionDeleteOperation(java.awt.event.ActionEvent evt) {
        int[] selectedOPs = tbl_MatrixOperations.getSelectedRows();
        if (selectedOPs.length>0) {
            try {
                int option = JOptionPane.showConfirmDialog(this, Text.Operation.confirmDelete1);
                if (option == JOptionPane.YES_OPTION) {
                    int deleteReportOption = JOptionPane.showConfirmDialog(this, Text.Reports.confirmDelete);
                    int opId = Integer.MIN_VALUE;
                    if (deleteReportOption != JOptionPane.CANCEL_OPTION) {
                        for (int i = selectedOPs.length-1; i >= 0; i--) {
                            int tmpOPRow = selectedOPs[i];
                            opId = (Integer) tbl_MatrixOperations.getModel().getValueAt(tmpOPRow, 0);
                            //TEST IF THE DELETED ITEM IS REQUIRED FOR A QUED WORKER
                            if (threadbox.SwingWorkerItemList.permitsDeletion(null, null, opId)) {
                                if (option == JOptionPane.YES_OPTION) {
                                    boolean deleteReport = false;
                                    if (deleteReportOption == JOptionPane.YES_OPTION) {
                                        deleteReport = true;
                                    }
                                    MultiOperations.deleteOperationsByOpId(parentMatrix.getStudyId(), parentMatrix.getMatrixId(), opId, deleteReport);
                                    
                                    //netCDF.operations.OperationManager.deleteOperationBranch(parentMatrix.getStudyId(), opId, deleteReport);
                                }
                            } else {
                                gui.utils.Dialogs.showWarningDialogue(Text.Processes.cantDeleteRequiredItem);
                            }
                        }

                        if(currentOP.getOperationId()==opId){
                            gui.GWASpiExplorerPanel.tree.setSelectionPath(gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
                        }
                        gui.GWASpiExplorerPanel.updateTreePanel(true);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CurrentStudyPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void actionGoBack(java.awt.event.ActionEvent evt) throws IOException {
        if(currentOP!=null && currentOP.getParentOperationId()!=-1){
            gui.GWASpiExplorerPanel.tree.setSelectionPath(gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
            gui.GWASpiExplorerPanel.pnl_Content = new MatrixAnalysePanel(currentOP.getParentMatrixId(), currentOP.getParentOperationId());
            gui.GWASpiExplorerPanel.scrl_Content.setViewportView(gui.GWASpiExplorerPanel.pnl_Content);
        } else if(currentOP!=null){
            gui.GWASpiExplorerPanel.tree.setSelectionPath(gui.GWASpiExplorerPanel.tree.getSelectionPath().getParentPath());
            gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
            gui.GWASpiExplorerPanel.scrl_Content.setViewportView(gui.GWASpiExplorerPanel.pnl_Content);
        } else {
            gui.GWASpiExplorerPanel.pnl_Content = new CurrentMatrixPanel(parentMatrix.getMatrixId());
            gui.GWASpiExplorerPanel.scrl_Content.setViewportView(gui.GWASpiExplorerPanel.pnl_Content);
        }
    }

    private void actionHelp(java.awt.event.ActionEvent evt) {
        try {
            gui.utils.URLInDefaultBrowser.browseHelpURL(HelpURLs.QryURL.matrixAnalyse);
        } catch (IOException ex) {
            Logger.getLogger(CurrentMatrixPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //</editor-fold>
}
