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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestsOperationDataSet;
import org.gwaspi.operations.allelicassociationtest.DefaultAllelicAssociationOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.DefaultGenotypicAssociationOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestsOperationDataSet;
import org.gwaspi.operations.trendtest.CommonTestOperationDataSet;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;

public class OP_AssociationTests extends AbstractTestMatrixOperation<CommonTestOperationDataSet> {

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final OPType testType;

	public OP_AssociationTests(
			OperationKey markerCensusOPKey,
			OperationKey hwOPKey,
			double hwThreshold,
			OPType testType)
	{
		super(
			markerCensusOPKey,
			hwOPKey,
			hwThreshold,
			OutputTest.createTestName(testType) + " Test");

		this.testType = testType;
	}

	@Override
	public OPType getType() {
		return testType;
	}

	private boolean isAllelic() {
		return (testType == OPType.ALLELICTEST);
	}

	/**
	 * Performs the Allelic or Genotypic Association Tests.
	 * @param dataSet
	 * @param markerOrigIndicesKeys
	 * @param caseMarkersCensus
	 * @param ctrlMarkersCensus
	 * @throws IOException
	 */
	@Override
	protected void performTest(
			OperationDataSet dataSet,
			Map<Integer, MarkerKey> markerOrigIndicesKeys,
			List<Census> caseMarkersCensus,
			List<Census> ctrlMarkersCensus)
			throws IOException
	{
		Iterator<Census> caseMarkerCensusIt = caseMarkersCensus.iterator();
		Iterator<Census> ctrlMarkersCensusIt = ctrlMarkersCensus.iterator();
		for (Map.Entry<Integer, MarkerKey> caseMarkerOrigIndexKey : markerOrigIndicesKeys.entrySet()) {
			final Integer origIndex = caseMarkerOrigIndexKey.getKey();
			final MarkerKey markerKey = caseMarkerOrigIndexKey.getValue();
			final Census caseCensus = caseMarkerCensusIt.next();
			final Census ctrlCensus = ctrlMarkersCensusIt.next();

			// INIT VALUES
			final int caseAA = caseCensus.getAA();
			final int caseAa = caseCensus.getAa();
			final int caseaa = caseCensus.getaa();
			final int caseTot = caseAA + caseaa + caseAa;

			final int ctrlAA = ctrlCensus.getAA();
			final int ctrlAa = ctrlCensus.getAa();
			final int ctrlaa = ctrlCensus.getaa();
			final int ctrlTot = ctrlAA + ctrlaa + ctrlAa;

			if (isAllelic()) {
				// allelic test
				final int sampleNb = caseTot + ctrlTot;

				final double allelicT = Associations.calculateAllelicAssociationChiSquare(
						sampleNb,
						caseAA,
						caseAa,
						caseaa,
						caseTot,
						ctrlAA,
						ctrlAa,
						ctrlaa,
						ctrlTot);
				final double allelicPval = Pvalue.calculatePvalueFromChiSqr(allelicT, 1);

				final double allelicOR = Associations.calculateAllelicAssociationOR(
						caseAA,
						caseAa,
						caseaa,
						ctrlAA,
						ctrlAa,
						ctrlaa);

				AllelicAssociationTestsOperationDataSet allelicAssociationDataSet = (AllelicAssociationTestsOperationDataSet) dataSet;
				allelicAssociationDataSet.addEntry(new DefaultAllelicAssociationOperationEntry(
						markerKey,
						origIndex,
						allelicT,
						allelicPval,
						allelicOR));
			} else {
				// genotypic test
				final double gntypT = Associations.calculateGenotypicAssociationChiSquare(
						caseAA,
						caseAa,
						caseaa,
						caseTot,
						ctrlAA,
						ctrlAa,
						ctrlaa,
						ctrlTot);
				final double gntypPval = Pvalue.calculatePvalueFromChiSqr(gntypT, 2);
				final double[] gntypOR = Associations.calculateGenotypicAssociationOR(
						caseAA,
						caseAa,
						caseaa,
						ctrlAA,
						ctrlAa,
						ctrlaa);

				GenotypicAssociationTestsOperationDataSet genotypicAssociationDataSet = (GenotypicAssociationTestsOperationDataSet) dataSet;
				genotypicAssociationDataSet.addEntry(new DefaultGenotypicAssociationOperationEntry(
						markerKey,
						origIndex,
						gntypT,
						gntypPval,
						gntypOR[0],
						gntypOR[1]));
			}
		}
	}
}
