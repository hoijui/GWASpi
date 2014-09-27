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

package org.gwaspi.netCDF.exporter;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.Study;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.AbstractOperation;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;

public class MatrixExporter extends AbstractOperation<MatrixExporterParams> {

	public static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			Text.Trafo.exportMatrix,
			Text.Trafo.exportMatrix); // TODO add more detailed info

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Export data",
					"Export data to an external format",
					null);

	private final MatrixExporterParams params;
	private final DataSetMetadata rdDataSetMetadata;
	private final DataSetSource rdDataSetSource;
	private final Map<ExportFormat, Formatter> formatters;

	public MatrixExporter(MatrixExporterParams params) throws IOException {

		this.params = params;
		final DataSetKey rdDataSetKey = params.getParent();
		rdDataSetMetadata = MatricesList.getDataSetMetadata(rdDataSetKey);

		rdDataSetSource = MatrixFactory.generateDataSetSource(rdDataSetKey);

		formatters = new EnumMap<ExportFormat, Formatter>(ExportFormat.class);
		formatters.put(ExportFormat.PLINK, new PlinkFormatter());
		formatters.put(ExportFormat.PLINK_Transposed, new PlinkTransposedFormatter());
		formatters.put(ExportFormat.PLINK_Binary, new PlinkBinaryFormatter());
		formatters.put(ExportFormat.Eigensoft_Eigenstrat, new PlinkBinaryFormatter(true));
		formatters.put(ExportFormat.BEAGLE, new BeagleFormatter());
		formatters.put(ExportFormat.GWASpi, new GWASpiFormatter());
		formatters.put(ExportFormat.Spreadsheet, new SpreadsheetFormatter());
		formatters.put(ExportFormat.MACH, new MachFormatter());
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		return new NullProgressHandler(new DefaultProcessInfo("<TODO implement ME!>", null)); // FIXME actually implement a read progress handler/tracker for this class!
	}

	@Override
	public int processMatrix() throws IOException {

		String exportPath = Study.constructExportsPath(rdDataSetMetadata.getStudyKey());
		String taskDesc = "exporting Data to \"" + exportPath + "\"";
		org.gwaspi.global.Utils.sysoutStart(taskDesc);

		org.gwaspi.global.Utils.createFolder(new File(exportPath));
		Formatter formatter = formatters.get(params.getExportFormat());

		boolean result = formatter.export(
				exportPath,
				rdDataSetMetadata,
				rdDataSetSource,
				params.getPhenotype());
		if (!result) {
			throw new IOException("Failed to export, reason unknown. Maybe there is additional info in the log before this entry."); // XXX Bad way of ding it, use exceptions before already?
		}

		org.gwaspi.global.Utils.sysoutCompleted(taskDesc);

		return Integer.MIN_VALUE;
	}
}
