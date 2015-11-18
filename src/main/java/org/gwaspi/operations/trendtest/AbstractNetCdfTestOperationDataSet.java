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

package org.gwaspi.operations.trendtest;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.NetCDFConstants;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractAssociationTestsOperation;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationDataEntry;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestOperation;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCdfTestOperationDataSet<E extends OperationDataEntry>
		extends AbstractNetCdfOperationDataSet<E>
{
	// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
	// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
	// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
	// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
	// switch (test-type) {
	//   case "allelic association test": Association.VAR_OP_MARKERS_AS_ALLELIC_ASSOCIATION_TPOR: {T, P-Value, OR} [Double[3]]
	//   case "genotypic association test": Association.VAR_OP_MARKERS_AS_GENOTYPIC_ASSOCIATION_TP2OR: {T, P-Value, OR-1, OR-2} [Double[4]]
	//   case "trend test": Association.VAR_OP_MARKERS_AS_TREND_TEST_TP: {T, P-Value} [Double[2]]
	// }

	public AbstractNetCdfTestOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);
	}

	public AbstractNetCdfTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	protected void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException
	{
		final Class<? extends MatrixOperation> type = operationMetadata.getType();

		// Define Variables
		ncFile.addVariable(NetCDFConstants.Association.VAR_OP_MARKERS_T, DataType.DOUBLE, markersSpace);
		ncFile.addVariable(NetCDFConstants.Association.VAR_OP_MARKERS_P, DataType.DOUBLE, markersSpace);
		if (AbstractAssociationTestsOperation.class.isAssignableFrom(type)) { // allelic or genotypic
			ncFile.addVariable(NetCDFConstants.Association.VAR_OP_MARKERS_OR, DataType.DOUBLE, markersSpace);
			if (type.equals(GenotypicAssociationTestOperation.class)) {
				ncFile.addVariable(NetCDFConstants.Association.VAR_OP_MARKERS_OR2, DataType.DOUBLE, markersSpace);
			}
		}
		// NOTE this method is overriden for the COMBI test
	}
}
