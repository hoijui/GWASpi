package org.gwaspi.threadbox;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SwingDeleterItem {

	private String launchTime;
	private String startTime;
	private String endTime;
	protected String queueState;
	private String description;
	private Integer[] parentStudyIds;
	private Integer[] parentMatricesIds;
	private Integer[] parentOperationsIds;
	private String deleteTarget;
	private boolean deleteReports;
	private int studyId;
	private int matrixId;
	private int opId;
	private int rpId;

	SwingDeleterItem(String _deleteTarget,
			int _studyId,
			boolean _deleteReports) {

		launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		queueState = QueueStates.QUEUED;

		deleteTarget = _deleteTarget;
		deleteReports = _deleteReports;
		studyId = _studyId;
		matrixId = Integer.MIN_VALUE;
		opId = Integer.MIN_VALUE;
		rpId = Integer.MIN_VALUE;

	}

	SwingDeleterItem(String _deleteTarget,
			int _studyId,
			int _matrixId,
			boolean _deleteReports) {

		launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		queueState = QueueStates.QUEUED;

		deleteTarget = _deleteTarget;
		deleteReports = _deleteReports;
		studyId = _studyId;
		matrixId = _matrixId;
		opId = Integer.MIN_VALUE;
		rpId = Integer.MIN_VALUE;

	}

	SwingDeleterItem(String _deleteTarget,
			int _studyId,
			int _matrixId,
			int _opId,
			boolean _deleteReports) {

		launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		queueState = QueueStates.QUEUED;

		deleteTarget = _deleteTarget;
		deleteReports = _deleteReports;
		studyId = _studyId;
		matrixId = _matrixId;
		opId = _opId;
		rpId = Integer.MIN_VALUE;

	}

	SwingDeleterItem(String _deleteTarget,
			int _studyId,
			int _matrixId,
			int _opId,
			int _rpId,
			boolean _deleteReports) {

		launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		queueState = QueueStates.QUEUED;

		deleteTarget = _deleteTarget;
		deleteReports = _deleteReports;
		studyId = _studyId;
		matrixId = _matrixId;
		opId = _opId;
		rpId = _rpId;

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

	public Integer[] getParentMatricesIds() {
		return parentMatricesIds;
	}

	public Integer[] getParentOperationsIds() {
		return parentOperationsIds;
	}

	public Integer[] getParentStudyIds() {
		return parentStudyIds;
	}

	public String getDeleteTarget() {
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
		if (this.description == null) {
			StringBuilder sb = new StringBuilder();
			//DELETE STUDY
			if (this.deleteTarget.equals(DeleteTarget.STUDY)) {
				sb.append("Delete Study ID: " + this.getStudyId());
			}
			//DELETE MATRIX
			if (this.deleteTarget.equals(DeleteTarget.MATRIX)) {
				sb.append("Delete Matrix ID: " + this.getMatrixId());
			}
			//DELETE OPERATION BY MATRIXID
			if (this.deleteTarget.equals(DeleteTarget.REPORTS_BY_MATRIXID)) {
				sb.append("Delete Operations from Matrix ID: " + this.getMatrixId());
			}
			//DELETE OPERATION BY OPID
			if (this.deleteTarget.equals(DeleteTarget.OPERATION_BY_OPID)) {
				sb.append("Delete Operation ID: " + this.getOpId());
			}
			description = sb.toString();
		}

		return description;

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

	public static class DeleteTarget {

		public static String STUDY = "STUDY";
		public static String MATRIX = "MATRIX";
		public static String REPORTS_BY_MATRIXID = "OPERATION_BY_MATRIXID";
		public static String OPERATION_BY_OPID = "OPERATION_BY_OPID";
		public static String REPORT = "REPORT";
	}
}
