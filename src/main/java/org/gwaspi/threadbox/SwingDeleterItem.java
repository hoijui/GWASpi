/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.threadbox;

import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingDeleterItem extends CommonRunnable {

	private String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private String description;
	private final boolean deleteReports;
	private final StudyKey studyKey;
	private final MatrixKey matrixKey;
	private final OperationKey operationKey;
	private final ProgressHandler progressHandler;

	SwingDeleterItem(
			StudyKey studyKey,
			boolean deleteReports)
	{
		this(studyKey, null, null, deleteReports);
	}

	SwingDeleterItem(
			MatrixKey matrixKey,
			boolean deleteReports)
	{
		this(null, matrixKey, null, deleteReports);
	}

	SwingDeleterItem(
			OperationKey operationKey,
			boolean deleteReports)
	{
		this(null, null, operationKey, deleteReports);
	}

	private SwingDeleterItem(
			StudyKey studyKey,
			MatrixKey matrixKey,
			OperationKey operationKey,
			boolean deleteReports)
	{
		super("Delete", getToDeleteShortDescription(studyKey, matrixKey, operationKey, deleteReports));

		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.queueState = QueueState.QUEUED;
		this.deleteReports = deleteReports;
		this.studyKey = studyKey;
		this.matrixKey = matrixKey;
		this.operationKey = operationKey;
		this.progressHandler = new IndeterminateProgressHandler(new DefaultProcessInfo(
				"Delete " + getToDeleteShortDescription(
						studyKey, matrixKey, operationKey, deleteReports), null));
	}

	private static String getToDeleteShortDescription(
			StudyKey studyKey,
			MatrixKey matrixKey,
			OperationKey operationKey,
			boolean deleteReports)
	{
		StringBuilder description = new StringBuilder();

		if (studyKey != null) {
			description.append("Study ").append(studyKey.toRawIdString());
		} else if (matrixKey != null) {
			description.append("Matrix ").append(matrixKey.toRawIdString());
		} else if (operationKey != null) {
			description.append("Operation ").append(operationKey.toRawIdString());
		}

		if (deleteReports) {
			description.append(" with reports");
		}

		return description.toString();
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

	public MatrixKey getMatrixKey() {
		return matrixKey;
	}

	public OperationKey getOperationKey() {
		return operationKey;
	}

	public StudyKey getStudyKey() {
		return studyKey;
	}

	public boolean isDeleteReports() {
		return deleteReports;
	}

	public String getDescription() {

		if (description == null) {
			description = "Delete " + getToDeleteShortDescription(
					getStudyKey(), getMatrixKey(), getOperationKey(), isDeleteReports());
		}

		return description;

	}

	public void setQueueState(QueueState queueState) {

		this.queueState = queueState;
		progressHandler.setNewStatus(SwingWorkerItem.toProcessStatus(queueState));
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(SwingDeleterItem.class);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressHandler;
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {
		throw new UnsupportedOperationException("This is implemented in SwingDeleterItemList. Maybe change to here?"); // TODO
	}
}
