package org.gwaspi.threadbox;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingWorkerItem {

	private final CommonRunnable task;
	private final String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private final Collection<Integer> parentStudyIds;
	private final Collection<Integer> parentMatricesIds;
	private final Collection<Integer> parentOperationsIds;

	SwingWorkerItem(
			CommonRunnable swingWorker,
			Integer[] parentStudyIds)
	{
		this(swingWorker, parentStudyIds, new Integer[] {});
	}

	SwingWorkerItem(
			CommonRunnable swingWorker,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds)
	{
		this(swingWorker, parentStudyIds, parentMatricesIds, new Integer[] {});
	}

	SwingWorkerItem(
			CommonRunnable task,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds,
			Integer[] parentOperationsIds)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.task = task;
		this.queueState = QueueState.QUEUED;
		this.parentStudyIds = Collections.unmodifiableCollection(Arrays.asList(parentStudyIds));
		this.parentMatricesIds = Collections.unmodifiableCollection(Arrays.asList(parentMatricesIds));
		this.parentOperationsIds = Collections.unmodifiableCollection(Arrays.asList(parentOperationsIds));
	}

	public QueueState getQueueState() {
		return queueState;
	}

	public boolean isCurrent() {
		return ((queueState == QueueState.QUEUED) || (queueState == QueueState.PROCESSING));
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

	public CommonRunnable getTask() {
		return task;
	}

	public Collection<Integer> getParentMatricesIds() {
		return parentMatricesIds;
	}

	public Collection<Integer> getParentOperationsIds() {
		return parentOperationsIds;
	}

	public Collection<Integer> getParentStudyIds() {
		return parentStudyIds;
	}

	public String getTimeStamp() {
		return task.getTimeStamp();
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
