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
import org.gwaspi.constants.DBSamplesConstants;
import org.gwaspi.constants.ExportConstants.ExportFormat;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.ExportCombinedOperation;

class ExportMatrixScriptCommand extends AbstractScriptCommand {

	ExportMatrixScriptCommand() {
		super("export_matrix");
	}

	@Override
	public void execute(final Map<String, String> args) throws ScriptExecutionException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=export_matrix
		1.study-id=1
		2.matrix-id=8
		3.operation-id=65 # [optional]
		4.format=MACH
		[/script]
		*/
		//</editor-fold>

		try {
			// checking study
			final StudyKey studyKey = fetchStudyKey(args);
			final MatrixKey matrixKey = fetchMatrixKey(args, studyKey);
			final Integer operationId = fetchInteger(args, "operation-id", null);
			final DataSetKey dataSetKey;
			if (operationId == null) {
				// we will export the matrix
				dataSetKey = new DataSetKey(matrixKey);
			} else {
				// we will export the operation
				dataSetKey = new DataSetKey(new OperationKey(matrixKey, operationId));
			}
			checkStudyForScript(studyKey);

			String formatStr = args.get("format");
			ExportFormat format = ExportFormat.valueOf(formatStr);

			final MatrixExporterParams matrixExporterParams = new MatrixExporterParams(
					dataSetKey, format, DBSamplesConstants.F_AFFECTION);
			final CommonRunnable exportTask = new ExportCombinedOperation(matrixExporterParams);
			CommonRunnable.doRunNowInThread(exportTask);
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
	}
}
