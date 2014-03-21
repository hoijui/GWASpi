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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.NetCdfAllelicAssociationTestsOperationDataSet;
import org.gwaspi.operations.combi.NetCdfCombiTestOperationDataSet;
import org.gwaspi.operations.filter.NetCdfSimpleOperationDataSet;
import org.gwaspi.operations.genotypicassociationtest.NetCdfGenotypicAssociationTestsOperationDataSet;
import org.gwaspi.operations.hardyweinberg.NetCdfHardyWeinbergOperationDataSet;
import org.gwaspi.operations.markercensus.NetCdfMarkerCensusOperationDataSet;
import org.gwaspi.operations.qamarkers.NetCdfQAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.NetCdfQASamplesOperationDataSet;
import org.gwaspi.operations.trendtest.NetCdfTrendTestOperationDataSet;

public class OperationFactory {

	private static final Map<Class<? extends MatrixOperation>, OperationTypeInfo> operationTypeInfos
			= new HashMap<Class<? extends MatrixOperation>, OperationTypeInfo>();

	private OperationFactory() {}

	public static void registerOperationTypeInfo(final Class<? extends MatrixOperation> type, final OperationTypeInfo info) {
		operationTypeInfos.put(type, info);
	}

	public static OperationTypeInfo getOperationTypeInfo(final Class<? extends MatrixOperation> type) {
		return operationTypeInfos.get(type);
	}

	/**
	 * Creates a new OperationDataSet for the specified type.
	 * @param operationType
	 * @param origin
	 * @param parent
	 * @return
	 * @throws IOException
	 */
	public static OperationDataSet generateOperationDataSet(OPType operationType, MatrixKey origin, DataSetKey parent) throws IOException {
		return generateOperationDataSet(operationType, null, origin, parent);
	}

	public static OperationDataSet generateOperationDataSet(OperationKey operationKey) throws IOException {

		OperationMetadata operationMetadata = OperationsList.getOperationMetadata(operationKey);
		OPType operationType = operationMetadata.getOperationType();

		return generateOperationDataSet(operationType, operationKey, operationKey.getParentMatrixKey(), operationMetadata.getParent());
	}

	private static OperationDataSet generateOperationDataSet(OPType operationType, OperationKey operationKey, MatrixKey origin, DataSetKey parent) throws IOException {

		AbstractOperationDataSet operationDataSet;

		boolean useNetCdf = true;
		if (useNetCdf) {
			switch (operationType) {
				case SAMPLE_QA:
					operationDataSet = new NetCdfQASamplesOperationDataSet(origin, parent, operationKey);
					break;
				case MARKER_QA:
					operationDataSet = new NetCdfQAMarkersOperationDataSet(origin, parent, operationKey);
					break;
				case MARKER_CENSUS_BY_AFFECTION:
				case MARKER_CENSUS_BY_PHENOTYPE:
					operationDataSet = new NetCdfMarkerCensusOperationDataSet(origin, parent, operationKey);
					break;
				case HARDY_WEINBERG:
					operationDataSet = new NetCdfHardyWeinbergOperationDataSet(origin, parent, operationKey);
					break;
				case ALLELICTEST:
					operationDataSet = new NetCdfAllelicAssociationTestsOperationDataSet(origin, parent, operationKey);
					break;
				case GENOTYPICTEST:
					operationDataSet = new NetCdfGenotypicAssociationTestsOperationDataSet(origin, parent, operationKey);
					break;
				case COMBI_ASSOC_TEST:
					operationDataSet = new NetCdfCombiTestOperationDataSet(origin, parent, operationKey);
					break;
				case TRENDTEST:
					operationDataSet = new NetCdfTrendTestOperationDataSet(origin, parent, operationKey);
					break;
				case FILTER_BY_HW_THREASHOLD:
				case FILTER_BY_VALID_AFFECTION:
				case FILTER_BY_WEIGHTS:
					operationDataSet = new NetCdfSimpleOperationDataSet(origin, parent, operationKey);
					break;
				case SAMPLE_HTZYPLOT:
				case MANHATTANPLOT:
				case QQPLOT:
				default:
					throw new IllegalArgumentException("This operation type is invalid, or has no data-attached");
			}
		} else {
			throw new UnsupportedOperationException("Not yet implemented!");
		}

		return operationDataSet;
	}
}
