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

package org.gwaspi.operations;

import java.io.IOException;
import org.gwaspi.constants.NetCDFConstants.Defaults.GenotypeEncoding;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.MatrixMetadata;

public interface MatrixMetadataFactory<DST extends DataSet, PT extends MatrixCreatingOperationParams> {

//	OperationTypeInfo getTypeInfo();

	MatrixMetadata generateMetadata(DST dataSet, PT params) throws IOException;

	String getStrandFlag();

	GenotypeEncoding getGuessedGTCode(PT params);
}
