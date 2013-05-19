package org.gwaspi.threadbox;

public class SwingDeleterItem {

	private String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private String description;
	private final DeleteTarget deleteTarget;
	private final boolean deleteReports;
	private final int studyId;
	private final int matrixId;
	private final int opId;
	private final int rpId;

	SwingDeleterItem(
			DeleteTarget deleteTarget,
			int studyId,
			boolean deleteReports)
	{
		this(deleteTarget, studyId, Integer.MIN_VALUE, deleteReports);
	}

	SwingDeleterItem(
			DeleteTarget deleteTarget,
			int studyId,
			int matrixId,
			boolean deleteReports)
	{
		this(deleteTarget, studyId, matrixId, Integer.MIN_VALUE, deleteReports);
	}

	SwingDeleterItem(
			DeleteTarget deleteTarget,
			int studyId,
			int matrixId,
			int opId,
			boolean deleteReports)
	{
		this(deleteTarget, studyId, matrixId, opId, Integer.MIN_VALUE, deleteReports);
	}

	SwingDeleterItem(
			DeleteTarget deleteTarget,
			int studyId,
			int matrixId,
			int opId,
			int rpId,
			boolean deleteReports)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.queueState = QueueState.QUEUED;
		this.deleteTarget = deleteTarget;
		this.deleteReports = deleteReports;
		this.studyId = studyId;
		this.matrixId = matrixId;
		this.opId = opId;
		this.rpId = rpId;
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

	public DeleteTarget getDeleteTarget() {
		return deleteTarget;
	}

	public int getMatrixId() {
		return matrixId;
	}

	public int getOpId() {
		return opId;
	}

	public int getRpId() {
		return rpId;
	}

	public int getStudyId() {
		return studyId;
	}

	public boolean isDeleteReports() {
		return deleteReports;
	}

	public String getDescription() {

		if (description == null) {
			StringBuilder sb = new StringBuilder();
			// DELETE STUDY
			if (getDeleteTarget().equals(DeleteTarget.STUDY)) {
				sb.append("Delete Study (ID): ").append(getStudyId());
			}
			// DELETE MATRIX
			if (getDeleteTarget().equals(DeleteTarget.MATRIX)) {
				sb.append("Delete Matrix (ID): ").append(getMatrixId());
			}
			// DELETE OPERATION BY MATRIX-ID
			if (getDeleteTarget().equals(DeleteTarget.REPORTS_BY_MATRIXID)) {
				sb.append("Delete Operations from Matrix (ID): ").append(getMatrixId());
			}
			// DELETE OPERATION BY OPERATION-ID
			if (getDeleteTarget().equals(DeleteTarget.OPERATION_BY_OPID)) {
				sb.append("Delete Operation (ID): ").append(getOpId());
			}
			description = sb.toString();
		}

		return description;

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

	public static enum DeleteTarget {
		STUDY,
		MATRIX,
		/**
		 * NOTE had String value: "OPERATION_BY_MATRIXID"
		 * @deprecated unused!
		 */
		REPORTS_BY_MATRIXID,
		OPERATION_BY_OPID,
		/** @deprecated unused! */
		REPORT
	}
}
