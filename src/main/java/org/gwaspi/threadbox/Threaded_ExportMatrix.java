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

import org.gwaspi.netCDF.exporter.MatrixExporter;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.progress.ProgressForwarder;
import org.gwaspi.progress.ProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExportMatrix extends CommonRunnable {

	private final MatrixExporterParams params;
	private final ProgressForwarder progressForwarder;

	public Threaded_ExportMatrix(final MatrixExporterParams params) {
		super(
				"Export Matrix",
				"Exporting Matrix",
				"Export Matrix ID: " + params.getParent().toString(),
				"Exporting Matrix");

		this.params = params;
		this.progressForwarder = new ProgressForwarder(MatrixExporter.PROCESS_INFO);
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
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		MatrixExporter mEx = new MatrixExporter(params);
		progressForwarder.setInnerProgressSource(mEx.getProgressSource());
		mEx.processMatrix();
	}
}
