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
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SuperProgressSource;

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

	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(1);
		tmpSubProgressSourcesAndWeights.put(Formatter.PLACEHOLDER_PS_EXPORT, 1.0);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private static final Map<ExportFormat, Formatter> FORMATTERS;
	static {
		final Map<ExportFormat, Formatter> formatters
				= new EnumMap<ExportFormat, Formatter>(ExportFormat.class);
		formatters.put(ExportFormat.PLINK, new PlinkFormatter());
		formatters.put(ExportFormat.PLINK_Transposed, new PlinkTransposedFormatter());
		formatters.put(ExportFormat.PLINK_Binary, new PlinkBinaryFormatter());
		formatters.put(ExportFormat.Eigensoft_Eigenstrat, new PlinkBinaryFormatter(true));
		formatters.put(ExportFormat.BEAGLE, new BeagleFormatter());
		formatters.put(ExportFormat.GWASpi, new GWASpiFormatter());
		formatters.put(ExportFormat.Spreadsheet, new SpreadsheetFormatter());
		formatters.put(ExportFormat.MACH, new MachFormatter());
		FORMATTERS = Collections.unmodifiableMap(formatters);
	}

	private final MatrixExporterParams params;
	private final DataSetMetadata rdDataSetMetadata;
	private final DataSetSource rdDataSetSource;
	private final SuperProgressSource progressSource;

	public MatrixExporter(MatrixExporterParams params) throws IOException {

		this.params = params;
		final DataSetKey rdDataSetKey = params.getParent();
		rdDataSetMetadata = MatricesList.getDataSetMetadata(rdDataSetKey);

		rdDataSetSource = MatrixFactory.generateDataSetSource(rdDataSetKey);
		this.progressSource = new SuperProgressSource(PROCESS_INFO, subProgressSourcesAndWeights);
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
		return progressSource;
	}

	@Override
	public int processMatrix() throws IOException {

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);
		String exportPath = Study.constructExportsPath(rdDataSetMetadata.getStudyKey());
		String taskDesc = "exporting Data to \"" + exportPath + "\"";
		org.gwaspi.global.Utils.sysoutStart(taskDesc);

		org.gwaspi.global.Utils.createFolder(new File(exportPath));
		Formatter formatter = FORMATTERS.get(params.getExportFormat());

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		formatter.export(
				exportPath,
				rdDataSetMetadata,
				rdDataSetSource,
				progressSource,
				params.getPhenotype());

		org.gwaspi.global.Utils.sysoutCompleted(taskDesc);
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);

		return Integer.MIN_VALUE;
	}
}
