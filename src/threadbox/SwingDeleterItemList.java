/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package threadbox;

import database.DbManager;
import global.ServiceLocator;
import global.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.GWASpiExplorerNodes;
import threadbox.SwingDeleterItem.DeleteTarget;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingDeleterItemList extends SwingWorkerItemList{

    protected static ArrayList<SwingDeleterItem> swingDeleterItemsAL = new ArrayList();

    SwingDeleterItemList(){

    }


    public void add(SwingDeleterItem sdi) {

        //SwingDeleterItemList.purgeDoneDeletes();
        boolean addMe = true;
        for(SwingDeleterItem allreadySdi:swingDeleterItemsAL){
            if(allreadySdi.getStudyId() == sdi.getStudyId() &&
               allreadySdi.getMatrixId() == sdi.getMatrixId() &&
               allreadySdi.getOpId() == sdi.getOpId()){
               addMe = false;
            }
        }
        if(addMe){
            SwingDeleterItemList.swingDeleterItemsAL.add(SwingDeleterItemList.swingDeleterItemsAL.size(),sdi); //Add at start of list
        }
        

        //CHECK IF ANY ITEM IS RUNNING, START PROCESSING NEWLY ADDED SwingDeleter
        if(SwingWorkerItemList.getSwingWorkerPendingItemsNb()==0){
            deleteAllListed();
        }

    }

    public static void deleteAllListed() {
        if(gui.StartGWASpi.guiMode){
            gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.waitCursor);
        }
        
        for(SwingDeleterItem currentSdi:swingDeleterItemsAL){
            if(currentSdi.queueState.equals(QueueStates.QUEUED)){
                String deleteTarget = currentSdi.getDeleteTarget();

                //DELETE STUDY
                if(deleteTarget.equals(DeleteTarget.STUDY)){
                    try {
                        currentSdi.setStartTime(global.Utils.getShortDateTimeAsString());
                        currentSdi.setQueueState(QueueStates.PROCESSING);
                        
                        database.StudyGenerator.deleteStudy(currentSdi.getStudyId(), currentSdi.isDeleteReports());
                        MultiOperations.printCompleted("deleting Study ID: "+currentSdi.getStudyId());

                        GWASpiExplorerNodes.deleteStudyNode(currentSdi.getStudyId());
                        flagCurrentItemDeleted();
                    } catch (IOException ex) {
                        Logger.getLogger(SwingDeleterItemList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //DELETE MATRIX
                if(deleteTarget.equals(DeleteTarget.MATRIX)){
                    currentSdi.setStartTime(global.Utils.getShortDateTimeAsString());
                    currentSdi.setQueueState(QueueStates.PROCESSING);

                    netCDF.matrices.MatrixManager.deleteMatrix(currentSdi.getMatrixId(), currentSdi.isDeleteReports());
                    MultiOperations.printCompleted("deleting Matrix ID:"+currentSdi.getMatrixId());

                    GWASpiExplorerNodes.deleteMatrixNode(currentSdi.getMatrixId());
                    flagCurrentItemDeleted();
                }
                //DELETE OPERATION BY OPID
                if(deleteTarget.equals(DeleteTarget.OPERATION_BY_OPID)){
                    try {
                        currentSdi.setStartTime(global.Utils.getShortDateTimeAsString());
                        currentSdi.setQueueState(QueueStates.PROCESSING);

                        netCDF.operations.OperationManager.deleteOperationBranch(currentSdi.getStudyId(), currentSdi.getOpId(), currentSdi.isDeleteReports());
                        MultiOperations.printCompleted("deleting Operation ID: "+currentSdi.getOpId());

                        GWASpiExplorerNodes.deleteOperationNode(currentSdi.getOpId());
                        flagCurrentItemDeleted();
                    } catch (IOException ex) {
                        Logger.getLogger(SwingDeleterItemList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //DELETE REPORTS BY MATRIXID -- NOT IN USAGE???
                if(deleteTarget.equals(DeleteTarget.REPORTS_BY_MATRIXID)){
                    try {
                        currentSdi.setStartTime(global.Utils.getShortDateTimeAsString());
                        currentSdi.setQueueState(QueueStates.PROCESSING);

                        reports.ReportManager.deleteReportByMatrixId(currentSdi.getMatrixId());
                        MultiOperations.printCompleted("deleting Reports from Matrix ID: "+currentSdi.getMatrixId());

                        flagCurrentItemDeleted();
                    } catch (IOException ex) {
                        Logger.getLogger(SwingDeleterItemList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        //IF WE ARE IN GUI MODE, UPDATE TREE. ELSE EXIT PROGRAM
        if (gui.StartGWASpi.guiMode) {
            gui.StartGWASpi.mainGUIFrame.setCursor(gui.utils.CursorUtils.defaultCursor);
            gui.ProcessTab.updateProcessOverview();
            try {
                MultiOperations.updateTreeAndPanel();
            } catch (IOException ex) {
            }
            GWASpiExplorerNodes.setAllNodesCollapsable();
        } else {
            System.out.println(Text.Cli.doneExiting);
            gui.StartGWASpi.exit();
        }
        

    }


    public static ArrayList<SwingDeleterItem> getSwingDeleterItemsAL() {
        return swingDeleterItemsAL;
    }
    


    public static void flagCurrentItemAborted(){
        boolean idle = false;
        for(SwingDeleterItem currentSdi:swingDeleterItemsAL){
            if(!idle && currentSdi.queueState.equals(QueueStates.PROCESSING)){
                idle = true;
                currentSdi.setQueueState(QueueStates.ABORT);
                currentSdi.setEndTime(global.Utils.getShortDateTimeAsString());
            }
        }
    }

    public static void flagCurrentItemError(){
        boolean idle = false;
        for(SwingDeleterItem currentSdi:swingDeleterItemsAL){
            if(!idle && currentSdi.queueState.equals(QueueStates.PROCESSING)){
                idle = true;
                currentSdi.setQueueState(QueueStates.ERROR);
                currentSdi.setEndTime(global.Utils.getShortDateTimeAsString());
            }
        }
    }

    public static void flagCurrentItemDeleted(){
        boolean idle = false;
        for(SwingDeleterItem currentSdi:swingDeleterItemsAL){
            if(!idle && currentSdi.queueState.equals(QueueStates.PROCESSING)){
                idle = true;
                currentSdi.setQueueState(QueueStates.DELETED);
                currentSdi.setEndTime(global.Utils.getShortDateTimeAsString());
            }
        }
    }


    public static int getSwingDeleterItemsALsize(){
        return swingDeleterItemsAL.size();
    }

    public static int getSwingDeleterPendingItemsNb(){
        int result=0;
        for(SwingDeleterItem currentSdi:SwingDeleterItemList.getSwingDeleterItemsAL()){
            if(currentSdi.queueState.equals(QueueStates.PROCESSING)){
                result++;
            }
            if(currentSdi.queueState.equals(QueueStates.QUEUED)){
                result++;
            }
        }
        return result;
    }

    public static void purgeDoneDeletes(){
        for(int i=swingDeleterItemsAL.size();i>0;i--){
            if(swingDeleterItemsAL.get(i-1).getQueueState().equals(QueueStates.DELETED)){
                swingDeleterItemsAL.remove(i-1);
            }
        }
    }

    public static void abortSwingWorker(int idx){
        String queueState = swingDeleterItemsAL.get(idx).getQueueState();
        if(queueState.equals(threadbox.QueueStates.PROCESSING) || queueState.equals(threadbox.QueueStates.QUEUED)){
            swingDeleterItemsAL.get(idx).setQueueState(threadbox.QueueStates.ABORT);

            System.out.println("\n\n");
            System.out.println(Text.Processes.abortingProcess);
            System.out.println(swingDeleterItemsAL.get(idx).getDescription());
            System.out.println("Delete Launch Time: "+swingDeleterItemsAL.get(idx).getLaunchTime());
            System.out.println("\n");
            gui.ProcessTab.updateProcessOverview();
        }
    }

}
