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
public class Threaded_HardyWeinberg  implements Runnable {
    Thread runner;
    protected String timeStamp="";

    protected static int matrixId;
    protected static int censusOpId;
    protected static File phenotypeFile;

    public Threaded_HardyWeinberg(String threadName,
                        String _timeStamp,
                        int _matrixId,
                        int _censusOpId) {
        try {
            timeStamp=_timeStamp;
            global.Utils.sysoutStart("Hardy-Weinberg");
            global.Config.initPreferences(false, null);

            matrixId = _matrixId;
            censusOpId = _censusOpId;

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

            //<editor-fold defaultstate="collapsed" desc="HW PROCESS">

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
                MultiOperations.printFinished("Performing Hardy-Weinberg test");
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
            MultiOperations.printError("Performing Hardy-Weinberg test");
            Logger.getLogger(Threaded_HardyWeinberg.class.getName()).log(Level.SEVERE, null, ex);
            try {
                MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
                MultiOperations.updateTree();
                MultiOperations.updateProcessOverviewStartNext();
            } catch (Exception ex1) {
            }
        }
    }
}
