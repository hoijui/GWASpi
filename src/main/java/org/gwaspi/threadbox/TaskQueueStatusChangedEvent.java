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

package org.gwaspi.threadbox;

import java.util.EventObject;
import org.gwaspi.progress.ProgressSource;

/**
 * Contains details about a task that is being registered or deleted.
 */
public class TaskQueueStatusChangedEvent extends EventObject {

	private final Task task;
	private final ProgressSource progressSource; // HACK we should not need this, as we could just use getSource.getProgressSource(), but these are currently not the same (HACKy!)

	/**
	 * @deprecated use other ctor only, and remove this one
	 */
	public TaskQueueStatusChangedEvent(final TaskQueue source, final Task task, final ProgressSource progressSource) {
		super(source);

		this.task = task;
		this.progressSource = progressSource;
	}

	public TaskQueueStatusChangedEvent(final TaskQueue source, final Task task) {
		this(source, task, task.getProgressSource());
	}

	@Override
	public TaskQueue getSource() {
		return (TaskQueue) super.getSource();
	}

	public Task getTask() {
		return task;
	}

	public ProgressSource getProgressSource() {
		return progressSource;
	}
}
