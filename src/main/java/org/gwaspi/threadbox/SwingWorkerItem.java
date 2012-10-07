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

	private final String swingWorkerName;
	private final SwingWorker swingWorker;
	private final String timeStamp;
	private final String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private final Collection<Integer> parentStudyIds;
	private final Collection<Integer> parentMatricesIds;
	private final Collection<Integer> parentOperationsIds;

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyIds)
	{
		this(swingWorkerName, swingWorker, timeStamp, parentStudyIds, new Integer[] {});
	}

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds)
	{
		this(swingWorkerName, swingWorker, timeStamp, parentStudyIds, parentMatricesIds, new Integer[] {});
	}

	SwingWorkerItem(
			String swingWorkerName,
			SwingWorker swingWorker,
			String timeStamp,
			Integer[] parentStudyIds,
			Integer[] parentMatricesIds,
			Integer[] parentOperationsIds)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.timeStamp = timeStamp;
		this.swingWorkerName = swingWorkerName;
		this.swingWorker = swingWorker;
		this.queueState = QueueState.QUEUED;
		this.parentStudyIds = Collections.unmodifiableCollection(Arrays.asList(parentStudyIds));
		this.parentMatricesIds = Collections.unmodifiableCollection(Arrays.asList(parentMatricesIds));
		this.parentOperationsIds = Collections.unmodifiableCollection(Arrays.asList(parentOperationsIds));
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
