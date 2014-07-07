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

package org.gwaspi.cli;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.threadbox.MultiOperations;

class ExportMatrixScriptCommand extends AbstractScriptCommand {

	ExportMatrixScriptCommand() {
		super("export_matrix");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=export_matrix
		1.study-id=1
		2.matrix-id=8
		3.format=MACH
		[/script]
		*/
		//</editor-fold>

		// checking study
		StudyKey studyKey = prepareStudy(args.get("study-id"), false);
		int matrixId = Integer.parseInt(args.get("matrix-id"));
		MatrixKey matrixKey = new MatrixKey(studyKey, matrixId);
		boolean studyExists = checkStudy(studyKey);

		String formatStr = args.get("format");
		ExportFormat format = ExportFormat.valueOf(formatStr);

		if (studyExists) {
			final MatrixExporterParams matrixExporterParams = new MatrixExporterParams(
					matrixKey, format, cDBSamples.f_AFFECTION);
			MultiOperations.doExportMatrix(matrixExporterParams);
			return true;
		}

		return false;
	}
}
