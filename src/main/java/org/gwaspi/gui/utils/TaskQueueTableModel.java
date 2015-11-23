/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.gui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.gwaspi.global.Text;
import org.gwaspi.model.Identifier;
import org.gwaspi.threadbox.QueueState;
import org.gwaspi.threadbox.Task;
import org.gwaspi.threadbox.TaskQueue;
import org.gwaspi.threadbox.TaskQueueListener;
import org.gwaspi.threadbox.TaskQueueStatusChangedEvent;

public class TaskQueueTableModel extends AbstractTableModel implements TaskQueueListener {

	private static final String NO_DATA_AVAILABLE = "-";
	private static final List<String> COLUMN_NAMES;
	static {
		final List<String> tmpColumnNames = new ArrayList<String>(8);
		tmpColumnNames.add(Text.Processes.id);
		tmpColumnNames.add(Text.Study.studies);
		tmpColumnNames.add(Text.Processes.processeName);
		tmpColumnNames.add(Text.Processes.launchTime);
		tmpColumnNames.add(Text.Processes.startTime);
		tmpColumnNames.add(Text.Processes.endTime);
		tmpColumnNames.add(Text.Processes.queueState);
		tmpColumnNames.add(Text.All.abort);
		COLUMN_NAMES = Collections.unmodifiableList(tmpColumnNames);
	}
	private final TaskQueue dataSource;

	public TaskQueueTableModel(final TaskQueue dataSource) {

		this.dataSource = dataSource;

		dataSource.addTaskListener(this);
	}

	@Override
	public int getRowCount() {
		return dataSource.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.size();
	}

	@Override
	public String getColumnName(final int columnIndex) {
		return COLUMN_NAMES.get(columnIndex);
	}

	private static String extractParticipatingStudyIds(final Task task) {

		final StringBuilder studyIdsStr = new StringBuilder();
		for (final Identifier<?> studyIdentifiers : task.getTaskLockProperties().getRequiredStudies()) {
			studyIdsStr.append(", ");
			studyIdsStr.append(studyIdentifiers.toRawIdString());
		}
		if (studyIdsStr.length() == 0) {
			studyIdsStr.append(" - ");
		} else {
			studyIdsStr.delete(0, 2); // delete the first ", "
		}

		return studyIdsStr.toString();
	}

	private static String sanitizeAndToString(final Object obj) {

		if (obj == null) {
			return NO_DATA_AVAILABLE;
		} else if (obj instanceof Date) {
			return org.gwaspi.global.Utils.getShortDateTimeAsString((Date) obj);
		} else {
			return obj.toString();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		final Task selectedTask = dataSource.getTasks().get(rowIndex);
		switch (columnIndex) {
			case 0: return rowIndex;
			case 1: return extractParticipatingStudyIds(selectedTask);
			case 2: return sanitizeAndToString(selectedTask.getDescription());
			case 3: return sanitizeAndToString(selectedTask.getCreateTime());
			case 4: return sanitizeAndToString(selectedTask.getStartTime());
			case 5: return sanitizeAndToString(selectedTask.getEndTime());
			case 6: return sanitizeAndToString(selectedTask.getStatus());
			case 7: return selectedTask;
			default: throw new IllegalStateException("Can not fetch index for task-queue table: " + columnIndex);
		}
	}

	@Override
	public void taskStatusChanged(TaskQueueStatusChangedEvent evt) {

		final QueueState newStatus = evt.getTask().getStatus();
		if (newStatus.equals(QueueState.QUEUED)) {
			fireTableRowsInserted(evt.getQueueIndex(), evt.getQueueIndex());
		} else if (newStatus.equals(QueueState.REMOVED)) {
			fireTableRowsDeleted(evt.getQueueIndex(), evt.getQueueIndex());
		} else {
			fireTableRowsUpdated(evt.getQueueIndex(), evt.getQueueIndex());
		}
	}
}
