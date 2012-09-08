package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItem {

	private String swingWorkerName;
	protected SwingWorker swingWorker = null;
	private String timeStamp = "";
	private String launchTime;
	private String startTime;
	private String endTime;
	protected String queueState;
	protected Integer[] parentStudyIds;
	protected Integer[] parentMatricesIds;
	protected Integer[] parentOperationsIds;

	SwingWorkerItem(String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyId)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.timeStamp = timeStamp;
		this.swingWorkerName = swingWorkerName;
		this.swingWorker = swingWorker;
		this.queueState = QueueStates.QUEUED;
		this.parentStudyIds = parentStudyId;
	}

	SwingWorkerItem(String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyId,
			Integer[] parentMatricesIds)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.timeStamp = timeStamp;
		this.swingWorkerName = swingWorkerName;
		this.swingWorker = swingWorker;
		this.queueState = QueueStates.QUEUED;
		this.parentStudyIds = parentStudyId;
		this.parentMatricesIds = parentMatricesIds;
	}

	SwingWorkerItem(String swingWorkerName,
			SwingWorker swingWorker,
			String _timeStamp,
			Integer[] parentStudyId,
			Integer[] parentMatricesIds,
			Integer[] parentOperationsIds)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.timeStamp = _timeStamp;
		this.swingWorkerName = swingWorkerName;
		this.swingWorker = swingWorker;
		this.queueState = QueueStates.QUEUED;
		this.parentStudyIds = parentStudyId;
		this.parentMatricesIds = parentMatricesIds;
		this.parentOperationsIds = parentOperationsIds;
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
