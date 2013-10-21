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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.OperationDataSet;
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

		// Iterate through markerset
		int markerNb = 0;
		Map<MarkerKey, Double[]> result = new LinkedHashMap<MarkerKey, Double[]>(wrCaseMarkerIdSetMap.size());
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

			Double[] store;
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

				store = new Double[3];
				store[0] = allelicT;
				store[1] = allelicPval;
				store[2] = allelicOR;
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

				store = new Double[4];
				store[0] = gntypT;
				store[1] = gntypPval;
				store[2] = gntypOR[0];
				store[3] = gntypOR[1];
			}
			result.put(markerKey, store); // store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}

		//<editor-fold defaultstate="expanded" desc="ALLELICTEST DATA WRITER">
		int[] boxes;
		String variableName;
		if (allelic) {
			boxes = new int[] {0, 1, 2};
			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR;
		} else {
			boxes = new int[] {0, 1, 2, 3};
			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
		}
		NetCdfUtils.saveDoubleMapD2ToWrMatrix(wrNcFile, result.values(), boxes, variableName);
		//</editor-fold>
	}
}
