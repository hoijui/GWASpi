/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.threadbox;

import java.util.ArrayList;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItemList {

    protected static ArrayList<SwingWorkerItem> swingWorkerItemsAL = new ArrayList();
    protected static ArrayList parentStudyIds = new ArrayList();
    protected static ArrayList parentMatricesIds = new ArrayList();
    protected static ArrayList parentOperationsIds = new ArrayList();


    SwingWorkerItemList(){
//        parentStudyIds = new ArrayList();
//        parentMatricesIds = new ArrayList();
//        parentOperationsIds = new ArrayList();
    }

    public void add(SwingWorkerItem swi,
                    Integer[] _parentStudyId,
                    Integer[] _parentMatricesIds,
                    Integer[] _parentOperationsIds){

        SwingDeleterItemList.purgeDoneDeletes();
        SwingWorkerItemList.swingWorkerItemsAL.add(swi);

        //LOCK PARENT ITEMS
        if (_parentStudyId!=null) {
            for (Integer id : _parentStudyId) {
                parentStudyIds.add(id);
            }
        }
        if (_parentMatricesIds!=null) {
            for (Integer id : _parentMatricesIds) {
                parentMatricesIds.add(id);
            }
        }
        if (_parentOperationsIds!=null) {
            for (Integer id : _parentOperationsIds) {
                parentOperationsIds.add(id);
            }
        }

        //CHECK IF ANY ITEM IS ALLREADY RUNNING
        boolean kickStart = true;
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(currentSwi.queueState.equals(QueueStates.PROCESSING)){
                kickStart = false;
            }
        }

        //START PROCESSING NEWLY ADDED SwingWorker
        if(kickStart){
            swi.getSwingWorker().start();
            swi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
            swi.setQueueState(QueueStates.PROCESSING);
        }
    }

    public static void startNext(){
        boolean idle = true;
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(idle && currentSwi.getQueueState().equals(QueueStates.QUEUED)){
                idle = false;
                currentSwi.swingWorker.start();
                currentSwi.setStartTime(org.gwaspi.global.Utils.getShortDateTimeAsString());
                currentSwi.setQueueState(QueueStates.PROCESSING);
            }
        }
        if(idle){
            SwingDeleterItemList.deleteAllListed(); //This will also update the tree
        }
    }


    public static ArrayList<SwingWorkerItem> getSwingWorkerItemsAL() {
        return swingWorkerItemsAL;
    }
    


    @SuppressWarnings("static-access")
    public static void flagCurrentItemDone(String timeStamp){
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(currentSwi.getTimeStamp().equals(timeStamp)){
                currentSwi.setQueueState(QueueStates.DONE);
                currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

                unlockParentItems(currentSwi.parentStudyIds,
                             currentSwi.parentMatricesIds,
                             currentSwi.parentOperationsIds);
            }
        }
    }

    @SuppressWarnings("static-access")
    public static void flagCurrentItemAborted(String timeStamp){
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(currentSwi.getTimeStamp().equals(timeStamp)){
                currentSwi.setQueueState(QueueStates.ABORT);
                currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

                unlockParentItems(currentSwi.parentStudyIds,
                             currentSwi.parentMatricesIds,
                             currentSwi.parentOperationsIds);
            }
        }
    }

    public static void flagCurrentItemAborted(int rowIdx){
        SwingWorkerItem currentSwi = swingWorkerItemsAL.get(rowIdx);
        String queueState = currentSwi.getQueueState();
        if(queueState.equals(org.gwaspi.threadbox.QueueStates.PROCESSING) || queueState.equals(org.gwaspi.threadbox.QueueStates.QUEUED)){
            swingWorkerItemsAL.get(rowIdx).setQueueState(org.gwaspi.threadbox.QueueStates.ABORT);
            org.gwaspi.gui.ProcessTab.updateProcessOverview();
            
            unlockParentItems(currentSwi.parentStudyIds,
                         currentSwi.parentMatricesIds,
                         currentSwi.parentOperationsIds);
        }
    }

    @SuppressWarnings("static-access")
    public static void flagCurrentItemError(String timeStamp){
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(currentSwi.getTimeStamp().equals(timeStamp)){
                currentSwi.setQueueState(QueueStates.ERROR);
                currentSwi.setEndTime(org.gwaspi.global.Utils.getShortDateTimeAsString());

                unlockParentItems(currentSwi.parentStudyIds,
                             currentSwi.parentMatricesIds,
                             currentSwi.parentOperationsIds);
            }
        }
    }

    
    public static boolean permitsDeletion(Integer studyId, Integer matrixId, Integer opId){
        boolean result = true;
        if (studyId!=null && parentStudyIds.contains(studyId)) {
            result = false;
        }
        if (matrixId!=null && parentMatricesIds.contains(matrixId)) {
            result = false;
        }
        if (opId!=null && parentOperationsIds.contains(opId)) {
            result = false;
        }
        return result;
    }


    public static void unlockParentItems(Integer[] studyIds, Integer[] matrixIds, Integer[] opIds){
        if (studyIds!=null) {
            for(Integer id:studyIds){
                parentStudyIds.remove(id);
            }
        }
        if (matrixIds!=null) {
            for(Integer id:matrixIds){
                parentMatricesIds.remove(id);
            }
        }
        if (opIds!=null) {
            for(Integer id:opIds){
                parentOperationsIds.remove(id);
            }
        }
    }


    public static int getSwingWorkerItemsALsize(){
        return swingWorkerItemsAL.size();
    }
    
    public static int getSwingWorkerPendingItemsNb(){
        int result=0;
        for(SwingWorkerItem currentSwi:SwingWorkerItemList.getSwingWorkerItemsAL()){
            if(currentSwi.queueState.equals(QueueStates.PROCESSING)){
                result++;
            }
            if(currentSwi.queueState.equals(QueueStates.QUEUED)){
                result++;
            }
        }
        return result;
    }

    @SuppressWarnings("static-access")
    public static SwingWorkerItem getSwingWorkerItemByTimeStamp(String timeStamp){
        SwingWorkerItem result=null;
        for(SwingWorkerItem currentSwi:swingWorkerItemsAL){
            if(currentSwi.getTimeStamp().equals(timeStamp)){
               result = currentSwi;
            }
        }
        return result;
    }



}
