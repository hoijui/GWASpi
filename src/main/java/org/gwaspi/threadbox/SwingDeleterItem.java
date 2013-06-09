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

public class SwingDeleterItem {

	private String launchTime;
	private String startTime;
	private String endTime;
	private QueueState queueState;
	private String description;
	private final boolean deleteReports;
	private final StudyKey studyKey;
	private final MatrixKey matrixKey;
	private final OperationKey operationKey;

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

	SwingDeleterItem(
			StudyKey studyKey,
			MatrixKey matrixKey,
			OperationKey operationKey,
			boolean deleteReports)
	{
		this.launchTime = org.gwaspi.global.Utils.getShortDateTimeAsString();
		this.queueState = QueueState.QUEUED;
		this.deleteReports = deleteReports;
		this.studyKey = studyKey;
		this.matrixKey = matrixKey;
		this.operationKey = operationKey;
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
			StringBuilder sb = new StringBuilder();
			if (getStudyKey() != null) {
				sb.append("Delete Study (ID): ").append(getStudyKey());
			} else if (getMatrixKey() != null) {
				sb.append("Delete Matrix (ID): ").append(getMatrixKey());
			} else if (getOperationKey() != null) {
				sb.append("Delete Operation (ID): ").append(getOperationKey());
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
}
