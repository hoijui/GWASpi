/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package threadbox;

import constants.cNetCDF;
import global.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.GWASpiExplorerNodes;
import model.Operation;
import model.OperationsList;
import netCDF.operations.GWASinOneGOParams;
import netCDF.operations.OperationManager;
import samples.SamplesParser;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_GTFreq_HW  implements Runnable {
    Thread runner;
    protected String timeStamp="";

    protected static int matrixId;
    protected static File phenotypeFile;
    protected static GWASinOneGOParams gwasParams;

    public Threaded_GTFreq_HW(String threadName,
                        String _timeStamp,
                        int _matrixId,
                        File _phenotypeFile,
                        GWASinOneGOParams _gwasParams) {
        try {
            timeStamp=_timeStamp;
            global.Utils.sysoutStart("Genotype Frequency count & Hardy-Weinberg");
            global.Config.initPreferences(false, null);

            matrixId = _matrixId;
            phenotypeFile = _phenotypeFile;
            gwasParams = _gwasParams;

            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
            runner.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(Threaded_GTFreq_HW.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("static-access")
    public void run() {
        SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

        try {

            OperationsList opList = new OperationsList(matrixId);
            int sampleQAOpId = opList.getIdOfLastOperationTypeOccurance(constants.cNetCDF.Defaults.OPType.SAMPLE_QA);
            int markersQAOpId = opList.getIdOfLastOperationTypeOccurance(constants.cNetCDF.Defaults.OPType.MARKER_QA);

            //<editor-fold defaultstate="collapsed" desc="GT FREQ. & HW PROCESS">
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

            

            //GT FREQ. BY PHENOFILE OR DB AFFECTION
            int censusOpId = Integer.MIN_VALUE;
            if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) { //BY EXTERNAL PHENOTYPE FILE

                    HashSet affectionStates = SamplesParser.scanSampleInfoAffectionStates(phenotypeFile.getPath()); //use Sample Info file affection state

                    if (affectionStates.contains("1") && affectionStates.contains("2")) {
                        System.out.println("Updating Sample Info in DB");
                        LinkedHashMap sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(phenotypeFile.getPath());
                        samples.InsertSampleInfo.processData(matrixId, sampleInfoLHM);

                        censusOpId = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(matrixId,
                                sampleQAOpId,
                                markersQAOpId,
                                gwasParams.discardMarkerMisRatVal,
                                gwasParams.discardGTMismatches,
                                gwasParams.discardSampleMisRatVal,
                                gwasParams.discardSampleHetzyRatVal,
                                new StringBuilder().append(gwasParams.friendlyName).append(" using ").append(phenotypeFile.getName()).toString(),
                                phenotypeFile);

                        global.Utils.sysoutCompleted("Genotype Frequency Count");
                        //MultiOperations.updateTree();
                    } else {
                        System.out.println(Text.Operation.warnAffectionMissing);
                    }
                } else { // BY DB AFFECTION
                    HashSet affectionStates = SamplesParser.getDBAffectionStates(matrixId); //use Sample Info file affection state
                    if (affectionStates.contains("1") && affectionStates.contains("2")) {
                        censusOpId = OperationManager.censusCleanMatrixMarkers(matrixId,
                                sampleQAOpId,
                                markersQAOpId,
                                gwasParams.discardMarkerMisRatVal,
                                gwasParams.discardGTMismatches,
                                gwasParams.discardSampleMisRatVal,
                                gwasParams.discardSampleHetzyRatVal,
                                new StringBuilder().append(gwasParams.friendlyName).append(" using ").append(cNetCDF.Defaults.DEFAULT_AFFECTION).toString());


                        global.Utils.sysoutCompleted("Genotype Frequency Count");
                        //MultiOperations.updateTree();
                    } else {
                        System.out.println(Text.Operation.warnAffectionMissing);
                    }
                }
                GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, censusOpId);
            }


            //HW ON GENOTYPE FREQ.
            int hwOpId = Integer.MIN_VALUE;
            if(thisSwi.getQueueState().equals(threadbox.QueueStates.PROCESSING)){
                if (censusOpId != Integer.MIN_VALUE) {
                    hwOpId = netCDF.operations.OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
                    GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpId);
                }
            }
            //</editor-fold>

            //FINISH OFF
            if(!thisSwi.getQueueState().equals(threadbox.QueueStates.ABORT)){
                MultiOperations.printFinished("Performing Genotype Frequency count & Hardy-Weinberg test");
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
            MultiOperations.printError("Performing Genotype Frequency count & Hardy-Weinberg test");
            Logger.getLogger(Threaded_GTFreq_HW.class.getName()).log(Level.SEVERE, null, ex);
            try {
                MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (Exception ex1) {
            }
        }
    }
}
