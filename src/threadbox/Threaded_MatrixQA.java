/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package threadbox;

import global.Text;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.GWASpiExplorerNodes;
import netCDF.operations.OP_QAMarkers_opt;
import netCDF.operations.OP_QASamples_opt;
import netCDF.operations.OperationManager;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_MatrixQA  implements Runnable {

    Thread runner;
    protected String timeStamp="";

    public int sampleQAOpId;
    public int markersQAOpId;
    protected static int matrixId;

    public Threaded_MatrixQA(String threadName,
                             String _timeStamp,
                             int _matrixId) {
        try {
            timeStamp=_timeStamp;
            global.Config.initPreferences(false, null);
            matrixId = _matrixId;
            runner = new Thread(this, threadName); // (1) Create a new thread.
            runner.start(); // (2) Start the thread.
            runner.join();
        } catch (InterruptedException ex) {
            //Logger.getLogger(Threaded_MatrixQA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @SuppressWarnings("static-access")
    public void run() {
        SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

        try {

            ArrayList necessaryOPsAL = new ArrayList();
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
            necessaryOPsAL.add(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString());
            ArrayList missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

            if (missingOPsAL.size() > 0) {
                if (missingOPsAL.contains(constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
                    sampleQAOpId = OP_QASamples_opt.processMatrix(matrixId);
                    GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, sampleQAOpId);
                    reports.OutputQASamples.writeReportsForQASamplesData(sampleQAOpId, true);
                    GWASpiExplorerNodes.insertReportsUnderOperationNode(sampleQAOpId);
                }
                if (missingOPsAL.contains(constants.cNetCDF.Defaults.OPType.MARKER_QA.toString())) {
                    markersQAOpId = OP_QAMarkers_opt.processMatrix(matrixId);
                    GWASpiExplorerNodes.insertOperationUnderMatrixNode(matrixId, markersQAOpId);
                    reports.OutputQAMarkers_opt.writeReportsForQAMarkersData(markersQAOpId);
                    GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpId);
                }
                MultiOperations.printCompleted("Matrix Quality Control");
            }
            MultiOperations.swingWorkerItemList.flagCurrentItemDone(timeStamp);

            MultiOperations.updateProcessOverviewStartNext();

        } catch (OutOfMemoryError e) {
                System.out.println(Text.App.outOfMemoryError);
        } catch (Exception ex) {
            Logger.getLogger(Threaded_MatrixQA.class.getName()).log(Level.SEVERE, null, ex);
            MultiOperations.printError("Matrix Quality Control");
            try {
                MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (Exception ex1) {
            }
        }
    }

}
