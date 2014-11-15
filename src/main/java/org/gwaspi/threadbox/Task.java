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

import java.util.Date;
import org.gwaspi.progress.ProgressSource;

/**
 * A task, visible to the user of the application as such.
 * It is usually long-running, forms some kind of a logically alone-standing unit
 * and might be scheduled as is in a multi-threaded system.
 * It could be a single operation, like a trend-test, or a combination of many operations
 * and report generations, like the COMBI analysis.
 */
public interface Task extends Runnable {

	QueueState getStatus();
	void setStatus(final QueueState status);

	Date getCreateTime();

	Date getStartTime();
	void setStartTime(final Date startTime);

	Date getEndTime();
	void setEndTime(final Date endTime);

	String getName();

	String getDescription();

	ProgressSource getProgressSource();

	TaskLockProperties getTaskLockProperties();
}
