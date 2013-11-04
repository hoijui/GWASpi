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
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestsOperationDataSet;
import org.gwaspi.operations.allelicassociationtest.DefaultAllelicAssociationOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.DefaultGenotypicAssociationOperationEntry;
import org.gwaspi.operations.genotypicassociationtest.GenotypicAssociationTestsOperationDataSet;
import org.gwaspi.statistics.Associations;
import org.gwaspi.statistics.Pvalue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_AssociationTests extends AbstractTestMatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(OP_AssociationTests.class);

	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final boolean allelic;

	public OP_AssociationTests(
			MatrixKey rdMatrixKey,
			OperationMetadata markerCensusOP,
			OperationMetadata hwOP,
			double hwThreshold,
			boolean allelic)
	{
		super(
			rdMatrixKey,
			markerCensusOP,
			hwOP,
			hwThreshold,
			(allelic ? "Allelic" : "Genotypic") + " Association Test",
			allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST);

		this.allelic = allelic;
	}

	/**
	 * Performs the Allelic or Genotypic Association Tests.
	 * @param dataSet
	 * @param wrCaseMarkerIdSetMap
	 * @param wrCtrlMarkerSet
	 */
	@Override
	protected void performTest(OperationDataSet dataSet, Map<MarkerKey, int[]> wrCaseMarkerIdSetMap, Map<MarkerKey, int[]> wrCtrlMarkerSet) throws IOException {

		((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(wrCaseMarkerIdSetMap.size()); // HACK

		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<MarkerKey, int[]> entry : wrCaseMarkerIdSetMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();

			int[] caseCntgTable = entry.getValue();
			int[] ctrlCntgTable = wrCtrlMarkerSet.get(markerKey);

			// INIT VALUES
			int caseAA = caseCntgTable[0];
			int caseAa = caseCntgTable[1];
			int caseaa = caseCntgTable[2];
			int caseTot = caseAA + caseaa + caseAa;

			int ctrlAA = ctrlCntgTable[0];
			int ctrlAa = ctrlCntgTable[1];
			int ctrlaa = ctrlCntgTable[2];
			int ctrlTot = ctrlAA + ctrlaa + ctrlAa;

			if (allelic) {
				// allelic test
				int sampleNb = caseTot + ctrlTot;

				double allelicT = Associations.calculateAllelicAssociationChiSquare(
						sampleNb,
						caseAA,
						caseAa,
						caseaa,
						caseTot,
						ctrlAA,
						ctrlAa,
						ctrlaa,
						ctrlTot);
				double allelicPval = Pvalue.calculatePvalueFromChiSqr(allelicT, 1);

				double allelicOR = Associations.calculateAllelicAssociationOR(
						caseAA,
						caseAa,
						caseaa,
						ctrlAA,
						ctrlAa,
						ctrlaa);

				AllelicAssociationTestsOperationDataSet allelicAssociationDataSet = (AllelicAssociationTestsOperationDataSet) dataSet;
				allelicAssociationDataSet.addEntry(new DefaultAllelicAssociationOperationEntry(markerKey, allelicT, allelicPval, allelicOR));
			} else {
				// genotypic test
				double gntypT = Associations.calculateGenotypicAssociationChiSquare(
						caseAA,
						caseAa,
						caseaa,
						caseTot,
						ctrlAA,
						ctrlAa,
						ctrlaa,
						ctrlTot);
				double gntypPval = Pvalue.calculatePvalueFromChiSqr(gntypT, 2);
				double[] gntypOR = Associations.calculateGenotypicAssociationOR(
						caseAA,
						caseAa,
						caseaa,
						ctrlAA,
						ctrlAa,
						ctrlaa);

				GenotypicAssociationTestsOperationDataSet genotypicAssociationDataSet = (GenotypicAssociationTestsOperationDataSet) dataSet;
				genotypicAssociationDataSet.addEntry(new DefaultGenotypicAssociationOperationEntry(markerKey, gntypT, gntypPval, gntypOR[0], gntypOR[1]));
			}

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}
	}
}
