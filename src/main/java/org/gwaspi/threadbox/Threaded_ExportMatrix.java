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
import org.gwaspi.netCDF.exporter.MatrixExporter;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressHandlerForwarder;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExportMatrix extends CommonRunnable {

	private final MatrixExporterParams params;
	private final ProgressHandlerForwarder progressForwarder;
	private final TaskLockProperties taskLockProperties;

	public Threaded_ExportMatrix(final MatrixExporterParams params) {
		super("Export Data", "from " + params.getParent().toString());

		this.params = params;
		this.progressForwarder = new ProgressHandlerForwarder(MatrixExporter.PROCESS_INFO);
		this.taskLockProperties = MultiOperations.createTaskLockProperties(params.getParent());
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExportMatrix.class);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressForwarder;
	}

	@Override
	protected ProgressHandler getProgressHandler() {
		return progressForwarder;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return taskLockProperties;
	}

	@Override
	protected void runInternal() throws IOException {

		final MatrixExporter mEx = new MatrixExporter(params);
		progressForwarder.setInnerProgressHandler((ProgressHandler) mEx.getProgressSource()); // HACK
//		OperationManager.performOperation(mEx); // XXX We can not do that, because MatrixExporter does not support getParams() yet, so instead we do ...
		mEx.processMatrix();
	}
}
