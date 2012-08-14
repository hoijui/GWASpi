/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */

public class SwingWorkerItem {

    protected String swingWorkerName;
    protected SwingWorker swingWorker=null;
    protected String timeStamp = "";
    protected String launchTime;
    protected String startTime;
    protected String endTime;
    protected String queueState;
    protected Integer[] parentStudyIds;
    protected Integer[] parentMatricesIds;
    protected Integer[] parentOperationsIds;


    SwingWorkerItem(String _swingWorkerName,
                    SwingWorker _swingWorker,
                    String _timeStamp,
                    Integer[] _parentStudyId){

        launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
        timeStamp = _timeStamp;
        swingWorkerName = _swingWorkerName;
        swingWorker=_swingWorker;
        queueState = QueueStates.QUEUED;
        parentStudyIds = _parentStudyId;

    }

    SwingWorkerItem(String _swingWorkerName,
                    SwingWorker _swingWorker,
                    String _timeStamp,
                    Integer[] _parentStudyId,
                    Integer[] _parentMatricesIds){

        launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
        timeStamp = _timeStamp;
        swingWorkerName = _swingWorkerName;
        swingWorker=_swingWorker;
        queueState = QueueStates.QUEUED;
        parentStudyIds = _parentStudyId;
        parentMatricesIds = _parentMatricesIds;

    }

    SwingWorkerItem(String _swingWorkerName,
                    SwingWorker _swingWorker,
                    String _timeStamp,
                    Integer[] _parentStudyId,
                    Integer[] _parentMatricesIds,
                    Integer[] _parentOperationsIds){

        launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
        timeStamp = _timeStamp;
        swingWorkerName = _swingWorkerName;
        swingWorker=_swingWorker;
        queueState = QueueStates.QUEUED;
        parentStudyIds = _parentStudyId;
        parentMatricesIds = _parentMatricesIds;
        parentOperationsIds = _parentOperationsIds;

    }

    public String getQueueState() {
        return queueState;
    }

    public String getLaunchTime() {
        return launchTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public SwingWorker getSwingWorker() {
        return swingWorker;
    }

    public String getSwingWorkerName() {
        return swingWorkerName;
    }


    public Integer[] getParentMatricesIds() {
        return parentMatricesIds;
    }

    public Integer[] getParentOperationsIds() {
        return parentOperationsIds;
    }

    public Integer[] getParentStudyIds() {
        return parentStudyIds;
    }

    public String getTimeStamp() {
        return timeStamp;
    }






    
    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


}
