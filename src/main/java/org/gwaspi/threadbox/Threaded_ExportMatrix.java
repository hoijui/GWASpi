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

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.netCDF.exporter.MatrixExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExportMatrix extends CommonRunnable {

	private boolean startWithGUI = org.gwaspi.gui.StartGWASpi.guiMode;
	private int matrixId;
	private ExportFormat format;
	private String phenotype;

	public Threaded_ExportMatrix(
			int matrixId,
			ExportFormat format,
			String phenotype)
	{
		super(
				"Export Matrix",
				"Exporting Matrix",
				"Export Matrix ID: " + matrixId,
				"Exporting Matrix");

		this.matrixId = matrixId;
		this.format = format;
		this.phenotype = phenotype;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExportMatrix.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (format != null) {
			if (startWithGUI) {
				ProcessTab.getSingleton().showTab();
			}
			MatrixExporter mEx = new MatrixExporter(matrixId);
			mEx.exportToFormat(format, phenotype);
		}
	}
}
