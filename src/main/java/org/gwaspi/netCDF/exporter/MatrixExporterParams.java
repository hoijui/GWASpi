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
package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.ExportConstants.ExportFormat;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.operations.AbstractOperationParams;

/**
 * Parameters for the {@link MatrixExporter}.
 */
public class MatrixExporterParams extends AbstractOperationParams {

	private final ExportFormat exportFormat;
	private final String phenotype;

	public MatrixExporterParams(
			final DataSetKey readDataSetKey,
			final ExportFormat exportFormat,
			final String phenotype)
	{
		super(null, readDataSetKey, null);

		this.exportFormat = exportFormat;
		this.phenotype = phenotype;
	}

	public ExportFormat getExportFormat() {
		return exportFormat;
	}

	public String getPhenotype() {
		return phenotype;
	}

	@Override
	protected String getNameDefault() {
		return "Data-Export for matrix " + getParent().getOrigin().toString(); // TODO use nicer matrix name!
	}
}
