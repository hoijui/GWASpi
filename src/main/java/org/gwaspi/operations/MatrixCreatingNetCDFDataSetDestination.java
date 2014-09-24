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

package org.gwaspi.operations;

import java.io.IOException;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.loader.AbstractNetCDFDataSetDestination;

public class MatrixCreatingNetCDFDataSetDestination<PT extends MatrixCreatingOperationParams>
		extends AbstractNetCDFDataSetDestination
{
	private final PT params;
	private final MatrixMetadataFactory<DataSet, PT> metadataFactory;

	public MatrixCreatingNetCDFDataSetDestination(
			PT params,
			MatrixMetadataFactory<DataSet, PT> metadataFactory)
	{
		this.params = params;
		this.metadataFactory = metadataFactory;
	}

	@Override
	protected MatrixMetadata createMatrixMetadata() throws IOException {
		return metadataFactory.generateMetadata(this.getDataSet(), params);
	}

	@Override
	protected String getStrandFlag() {
		return metadataFactory.getStrandFlag();
	}

	@Override
	protected cNetCDF.Defaults.GenotypeEncoding getGuessedGTCode() {
		return metadataFactory.getGuessedGTCode(params);
	}

}
