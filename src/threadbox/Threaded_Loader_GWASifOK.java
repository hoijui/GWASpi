/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package threadbox;

import constants.cNetCDF;
import global.Text;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.GWASpiExplorerNodes;
import netCDF.loader.LoadManager;
import netCDF.loader.SampleInfoCollectorSwitch;
import netCDF.operations.GWASinOneGOParams;
import netCDF.operations.OP_QAMarkers_opt;
import netCDF.operations.OP_QASamples_opt;
import netCDF.operations.OperationManager;
import netCDF.operations.OperationMetadata;
import reports.OutputAllelicAssociation_opt;
import reports.OutputGenotypicAssociation_opt;
import reports.OutputTrendTest_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_Loader_GWASifOK  implements Runnable {
    Thread runner;
    protected static String timeStamp="";

    protected static int resultMatrixId;
    protected static int samplesQAOpId;
    protected static int markersQAOpId;

    protected static String format;
    protected static boolean dummySamples;
    protected static int decision;
    protected static String newMatrixName;
    protected static String newMatrixDescription;
    protected static String file1;
    protected static String fileSampleInfo;
    protected static String file2;
    protected static String chromosome;
    protected static String strandType;
    protected static String gtCode;
    protected static int studyId;
    protected static GWASinOneGOParams gwasParams;

    public Threaded_Loader_GWASifOK(String threadName,
                           String _timeStamp,
                           String _format,
                           boolean _dummySamples,
                           int _decision,
                           String _newMatrixName,
                           String _newMatrixDescription,
                           String _file1,
                           String _fileSampleInfo,
                           String _file2,
                           String _chromosome,
                           String _strandType,
                           String _gtCode,
                           int _studyId,
                           GWASinOneGOParams _gwasParams) {

        try {
            timeStamp = _timeStamp;
            global.Config.initPreferences(false, null);

            format = _format;
            dummySamples = _dummySamples;
            decision = _decision;
            newMatrixName = _newMatrixName;
            newMatrixDescription = _newMatrixDescription;
            file1 = _file1;
            fileSampleInfo = _fileSampleInfo;
            file2 = _file2;
            chromosome = _chromosome;
            strandType = _strandType;
            gtCode = _gtCode;
            studyId = _studyId;
            gwasParams = _gwasParams;

            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
            runner.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(Threaded_Loader_GWAS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("static-access")
    public void run() {
        String currentTimeStamp = timeStamp;
        SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

        try {

            LinkedHashMap sampleInfoLHM = SampleInfoCollectorSwitch.collectSampleInfo(format, dummySamples, fileSampleInfo, file1, file2);
            HashSet affectionStates = SampleInfoCollectorSwitch.collectAffectionStates(sampleInfoLHM);

            //<editor-fold defaultstate="collapsed" desc="LOAD PROCESS">
            if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                resultMatrixId = LoadManager.dispatchLoadByFormat(format,
                        sampleInfoLHM,
                        newMatrixName,
                        newMatrixDescription,
                        file1,
                        fileSampleInfo,
                        file2,
                        chromosome,
                        strandType,
                        gtCode,
                        studyId);
                MultiOperations.printCompleted("Loading Genotypes");
                GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
            }
            //</editor-fold>


            //<editor-fold defaultstate="collapsed" desc="QA PROCESS">
            if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                samplesQAOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
                GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, samplesQAOpId);
                reports.OutputQASamples.writeReportsForQASamplesData(samplesQAOpId, true);
                GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpId);
            }

            if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                markersQAOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
                GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
                reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
                GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
                MultiOperations.printCompleted("Matrix Quality Control");
            }
            //</editor-fold>


            
            if (decision == JOptionPane.YES_OPTION &&
                affectionStates.contains("1") &&
                affectionStates.contains("2")) { //CHECK IF GWAS WAS REQUIRED AND IF AFFECTIONS AVAILABLE

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

                //<editor-fold defaultstate="collapsed" desc="PRE-GWAS PROCESS">
                //GENOTYPE FREQ.
                int censusOpId = Integer.MIN_VALUE;
                if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                    censusOpId = OperationManager.censusCleanMatrixMarkers(resultMatrixId,
                            samplesQAOpId,
                            markersQAOpId,
                            gwasParams.discardMarkerMisRatVal,
                            gwasParams.discardGTMismatches,
                            gwasParams.discardSampleMisRatVal,
                            gwasParams.discardSampleHetzyRatVal,
                            cNetCDF.Defaults.DEFAULT_AFFECTION);
                    GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, censusOpId);
                }

                //HW ON GENOTYPE FREQ.
                int hwOpId = Integer.MIN_VALUE;
                if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING) &&
                   censusOpId!=Integer.MIN_VALUE){
                    hwOpId = netCDF.operations.OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
                    GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
                }
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="GWAS TESTS & REPORTS">
                //ALLELIC ASSOCIATION (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
                if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING) &&
                   censusOpId!=Integer.MIN_VALUE &&
                   hwOpId!=Integer.MIN_VALUE){
                    OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

                    if (gwasParams.discardMarkerHWCalc) {
                        gwasParams.discardMarkerHWTreshold = (double) 0.05 / markerQAMetadata.getOpSetSize();
                    }

                    int assocOpId = netCDF.operations.OperationManager.performCleanAllelicTests(resultMatrixId,
                                                                                                    censusOpId,
                                                                                                    hwOpId,
                                                                                                    gwasParams.discardMarkerHWTreshold);
                    GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

                    //////Make Reports (needs newMatrixId, QAopId, AssocOpId)
                    if (assocOpId != Integer.MIN_VALUE) {
                        OutputAllelicAssociation_opt.writeReportsForAssociationData(assocOpId);
                        GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
                    }

                }

                //GENOTYPIC TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
                if(gwasParams.performGenotypicTests &&
                   thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING) &&
                   censusOpId!=Integer.MIN_VALUE &&
                   hwOpId!=Integer.MIN_VALUE){

                    OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

                    if (gwasParams.discardMarkerHWCalc) {
                        gwasParams.discardMarkerHWTreshold = (double) 0.05 / markerQAMetadata.getOpSetSize();
                    }

                    int assocOpId = netCDF.operations.OperationManager.performCleanGenotypicTests(resultMatrixId,
                                                                                                    censusOpId,
                                                                                                    hwOpId,
                                                                                                    gwasParams.discardMarkerHWTreshold);
                    GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, assocOpId);

                    //////Make Reports (needs newMatrixId, QAopId, AssocOpId)
                    if (assocOpId != Integer.MIN_VALUE) {
                        OutputGenotypicAssociation_opt.writeReportsForAssociationData(assocOpId);
                        GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpId);
                    }
                }

                //TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
                if(gwasParams.performTrendTests &&
                   thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING) &&
                   censusOpId!=Integer.MIN_VALUE &&
                   hwOpId!=Integer.MIN_VALUE){

                    OperationMetadata markerQAMetadata = new OperationMetadata(markersQAOpId);

                    if (gwasParams.discardMarkerHWCalc) {
                        gwasParams.discardMarkerHWTreshold = (double) 0.05 / markerQAMetadata.getOpSetSize();
                    }

                    int trendOpId = netCDF.operations.OperationManager.performCleanTrendTests(resultMatrixId,
                                                                                            censusOpId,
                                                                                            hwOpId,
                                                                                            gwasParams.discardMarkerHWTreshold);
                    GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, trendOpId);

                    //////Make Reports (needs newMatrixId, QAopId, AssocOpId)
                    if (trendOpId != Integer.MIN_VALUE) {
                        OutputTrendTest_opt.writeReportsForTrendTestData(trendOpId);
                        GWASpiExplorerNodes.insertReportsUnderOperationNode(trendOpId);
                    }
                }
                //</editor-fold>
            }

            

            //FINISH OFF
            if(!thisSwi.getQueueState().equals(threadbox.QueueStates.ABORT)){
                MultiOperations.printFinished("Loading Genotypes");
                MultiOperations.swingWorkerItemList.flagCurrentItemDone(timeStamp);
            } else {
                System.out.println("\n");
                System.out.println(Text.Processes.abortingProcess);
                System.out.println("Process Name: "+thisSwi.getSwingWorkerName());
                System.out.println("Process Launch Time: "+thisSwi.getLaunchTime());
                System.out.println("\n\n");
            }

            MultiOperations.updateProcessOverviewStartNext();

        } catch (OutOfMemoryError e) {
                System.out.println(Text.App.outOfMemoryError);
        } catch (Exception ex) {
            MultiOperations.printError("Loading Genotypes & Performing GWAS");
            Logger.getLogger(Threaded_Loader_GWASifOK.class.getName()).log(Level.SEVERE, null, ex);
            try {
                MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (Exception ex1) {
            }
        }
    }
}
