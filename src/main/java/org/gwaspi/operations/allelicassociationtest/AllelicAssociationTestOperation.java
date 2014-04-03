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

package org.gwaspi.operations.allelicassociationtest;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.AbstractAssociationTestsOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;

public class AllelicAssociationTestOperation extends AbstractAssociationTestsOperation<AllelicAssociationTestsOperationDataSet> {

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					Text.Operation.allelicAssocTest,
					Text.Operation.allelicAssocTest, // TODO We need a more elaborate description of this operation!
					OPType.ALLELICTEST);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new AbstractDefaultTypesOperationFactory(
				AllelicAssociationTestOperation.class, OPERATION_TYPE_INFO) {
					@Override
					protected OperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
						return new NetCdfAllelicAssociationTestsOperationDataSet(parent.getOrigin(), parent, operationKey);
					}
				});
	}

	public AllelicAssociationTestOperation(final AssociationTestOperationParams params) {
		super(params);
	}

	@Override
	protected void associationTest(
			final AllelicAssociationTestsOperationDataSet dataSet,
			final Integer markerOrigIndex,
			final MarkerKey markerKey,
			final int caseAA,
			final int caseAa,
			final int caseaa,
			final int ctrlAA,
			final int ctrlAa,
			final int ctrlaa)
			throws IOException
	{
		final double allelicT = Associations.calculateAllelicAssociationChiSquare(
				caseAA,
				caseAa,
				caseaa,
				ctrlAA,
				ctrlAa,
				ctrlaa);
		final double allelicPval = Pvalue.calculatePvalueFromChiSqr(allelicT, 1);

		final double allelicOR = Associations.calculateAllelicAssociationOR(
				caseAA,
				caseAa,
				caseaa,
				ctrlAA,
				ctrlAa,
				ctrlaa);

		dataSet.addEntry(new DefaultAllelicAssociationOperationEntry(
				markerKey,
				markerOrigIndex,
				allelicT,
				allelicPval,
				allelicOR));
	}
}
