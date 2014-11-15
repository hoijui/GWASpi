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

import java.io.IOException;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Deleter extends CommonRunnable {

	private static final Logger log = LoggerFactory.getLogger(Deleter.class);
	private static final TaskLockProperties EMPTY_TASK_LOCK_PROPERTIES = new TaskLockProperties();

	private final boolean deleteReports;
	private final StudyKey studyKey;
	private final MatrixKey matrixKey;
	private final OperationKey operationKey;
	private final ProgressHandler progressHandler;

	public Deleter(
			StudyKey studyKey,
			boolean deleteReports)
	{
		this(studyKey, null, null, deleteReports);
	}

	public Deleter(
			MatrixKey matrixKey,
			boolean deleteReports)
	{
		this(null, matrixKey, null, deleteReports);
	}

	public Deleter(
			OperationKey operationKey,
			boolean deleteReports)
	{
		this(null, null, operationKey, deleteReports);
	}

	private Deleter(
			StudyKey studyKey,
			MatrixKey matrixKey,
			OperationKey operationKey,
			boolean deleteReports)
	{
		super("Delete", getToDeleteShortDescription(studyKey, matrixKey, operationKey, deleteReports));

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

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Deleter.class);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressHandler;
	}

	@Override
	protected ProgressHandler getProgressHandler() {
		return progressHandler;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return EMPTY_TASK_LOCK_PROPERTIES;
	}

	@Override
	protected void runInternal() throws IOException {

		// we are actually going to delete the item
		if (getStudyKey() != null) {
			try {
				progressHandler.setNewStatus(ProcessStatus.RUNNING);

				StudyList.deleteStudy(getStudyKey(), isDeleteReports());
				MultiOperations.printCompleted("deleting Study ID: " + getStudyKey());

				GWASpiExplorerNodes.deleteStudyNode(getStudyKey());
				progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
			} catch (IOException ex) {
				log.error("Failed to delete study with ID: " + getStudyKey().toRawIdString(), ex);
				progressHandler.setNewStatus(ProcessStatus.FAILED);
			}
		} else if (getMatrixKey() != null) {
			try {
				progressHandler.setNewStatus(ProcessStatus.RUNNING);

				MatricesList.deleteMatrix(getMatrixKey(), isDeleteReports());
				MultiOperations.printCompleted("deleting Matrix ID:" + getMatrixKey());

				GWASpiExplorerNodes.deleteMatrixNode(getMatrixKey());
				progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
			} catch (IOException ex) {
				log.error("Failed to delete matrix with ID: " + getMatrixKey().toRawIdString(), ex);
				progressHandler.setNewStatus(ProcessStatus.FAILED);
			}
		} else if (getOperationKey() != null) {
			try {
				progressHandler.setNewStatus(ProcessStatus.RUNNING);

				OperationsList.deleteOperation(
						getOperationKey(),
						isDeleteReports());
				MultiOperations.printCompleted("deleting Operation ID: " + getOperationKey());

				GWASpiExplorerNodes.deleteOperationNode(getOperationKey());
				progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
			} catch (IOException ex) {
				log.error("Failed to delete operation with ID: " + getOperationKey().toRawIdString(), ex);
				progressHandler.setNewStatus(ProcessStatus.FAILED);
			}
		} else {
			// DELETE REPORTS BY MATRIX-ID -- NOT IN USE!
			try {
				progressHandler.setNewStatus(ProcessStatus.RUNNING);

				ReportsList.deleteReportByMatrixKey(getMatrixKey());
				MultiOperations.printCompleted("deleting Reports from Matrix ID: " + getMatrixKey());

				progressHandler.setNewStatus(ProcessStatus.COMPLEETED);
			} catch (IOException ex) {
				log.error("Failed to delete reports for matrix with ID: " + getMatrixKey().toRawIdString(), ex);
				progressHandler.setNewStatus(ProcessStatus.FAILED);
			}
			throw new RuntimeException("We should never end up in this code branch!"); // why? it is currently not supported.. (in other parts of the code too), but why?
		}
	}
}
