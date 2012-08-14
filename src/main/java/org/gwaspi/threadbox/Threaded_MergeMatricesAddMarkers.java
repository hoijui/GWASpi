/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.threadbox;

import org.gwaspi.global.Text;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.netCDF.operations.MatrixMergeMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QAMarkers_opt;
import org.gwaspi.netCDF.operations.OP_QASamples_opt;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_MergeMatricesAddMarkers  implements Runnable {

    Thread runner;
    protected String timeStamp="";

    protected static int resultMatrixId;
    protected static int studyId;
    protected static int parentMatrixId1;
    protected static int parentMatrixId2;
    protected static String newMatrixName;
    protected static String description;

    public Threaded_MergeMatricesAddMarkers(String threadName,
                                           String _timeStamp,
                                           int _studyId,
                                           int _parentMatrixId1,
                                           int _parentMatrixId2,
                                           String _newMatrixName,
                                           String _description) {
        try {
            timeStamp=_timeStamp;
            org.gwaspi.global.Utils.sysoutStart("Merging Matrices");
            org.gwaspi.global.Config.initPreferences(false, null);

            studyId = _studyId;
            parentMatrixId1 = _parentMatrixId1;
            parentMatrixId2 = _parentMatrixId2;
            newMatrixName = _newMatrixName;
            description = _description;

            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
            runner.join();
            
        } catch (InterruptedException ex) {
            //Logger.getLogger(Threaded_ExtractMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("static-access")
    public void run() {
        SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

        try {

            if(thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)){
                MatrixMergeMarkers_opt jointedMatrix = new MatrixMergeMarkers_opt(studyId,
                        parentMatrixId1,
                        parentMatrixId2,
                        newMatrixName,
                        description);

                resultMatrixId = jointedMatrix.mingleMarkersKeepSamplesConstant();
                GWASpiExplorerNodes.insertMatrixNode(studyId, resultMatrixId);
            }

            if(thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)){
                int sampleQAOpId = OP_QASamples_opt.processMatrix(resultMatrixId);
                GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, sampleQAOpId);
                org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
                GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
            }

            if(thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.PROCESSING)){
                int markersQAOpId = OP_QAMarkers_opt.processMatrix(resultMatrixId);
                GWASpiExplorerNodes.insertOperationUnderMatrixNode(resultMatrixId, markersQAOpId);
                org.gwaspi.reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
                GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
                MultiOperations.printCompleted("Matrix Quality Control");
            }

            //FINISH OFF
            if(!thisSwi.getQueueState().equals(org.gwaspi.threadbox.QueueStates.ABORT)){
                MultiOperations.printFinished("Merging Data");
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
            Logger.getLogger(Threaded_MergeMatricesAddMarkers.class.getName()).log(Level.SEVERE, null, ex);
            MultiOperations.printError("Merging Data");
            try {
                MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (Exception ex1) {
            }
        }
    }


}
