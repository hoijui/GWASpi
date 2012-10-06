package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItem {

	private final String swingWorkerName;
	private final SwingWorker swingWorker;
	private final String timeStamp;
	private final String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private final Integer[] parentStudyIds;
	private final Integer[] parentMatricesIds;
	private final Integer[] parentOperationsIds;

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyId)
	{
		this(swingWorkerName, swingWorker, timeStamp, parentStudyId, new Integer[] {});
	}

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyId,
			Integer[] parentMatricesIds)
	{
		this(swingWorkerName, swingWorker, timeStamp, parentStudyId, parentMatricesIds, new Integer[] {});
	}

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyId,
			Integer[] parentMatricesIds,
			Integer[] parentOperationsIds)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.timeStamp = timeStamp;
		this.swingWorkerName = swingWorkerName;
		this.swingWorker = swingWorker;
		this.queueState = QueueState.QUEUED;
		this.parentStudyIds = parentStudyId;
		this.parentMatricesIds = parentMatricesIds;
		this.parentOperationsIds = parentOperationsIds;
	}

	public QueueState getQueueState() {
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

	public void setQueueState(QueueState queueState) {
		this.queueState = queueState;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
}
