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
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.AbstractAssociationTestsOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.genotypicassociationtest.AssociationTestOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;

public class AllelicAssociationTestOperation extends AbstractAssociationTestsOperation<AllelicAssociationTestsOperationDataSet> {

	public static final ProgressSource PLACEHOLDER_PS_TEST = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_TEST", null));

	private static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			Text.Operation.allelicAssocTest,
			Text.Operation.allelicAssocTest); // TODO We need a more elaborate description of this operation!

	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new AllelicAssociationTestsOperationFactory());
	}

	public AllelicAssociationTestOperation(final AssociationTestOperationParams params) {
		super(params);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return AllelicAssociationTestsOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
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
		final double chiSqr = Associations.calculateAssociationChiSquare(
				caseAA,
				caseAa,
				caseaa,
				ctrlAA,
				ctrlAa,
				ctrlaa,
				false);
		final double pVal = Pvalue.calculatePvalueFromChiSqr(chiSqr, 1);
		final double oddsRatio = Associations.calculateAllelicAssociationOR(
				caseAA,
				caseAa,
				caseaa,
				ctrlAA,
				ctrlAa,
				ctrlaa);

		dataSet.addEntry(new DefaultAllelicAssociationOperationEntry(
				markerKey,
				markerOrigIndex,
				chiSqr,
				pVal,
				oddsRatio));
	}
}
