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

package org.gwaspi.statistics;

import java.math.BigInteger;

public class Associations {

	private Associations() {
	}

	//<editor-fold defaultstate="expanded" desc="GENOTYPIC TESTS">
	public static enum ChocranArmitageTrendTestModel {
		DOMINANT,
		RECESSIVE,
		CODOMINANT;
	}

	public static double calculateChocranArmitageTrendTest(
			int caseAA,
			int caseAa,
			int caseaa,
			int ctrlAA,
			int ctrlAa,
			int ctrlaa,
			ChocranArmitageTrendTestModel model)
	{
		double caseTot = caseAA + caseAa + caseaa;
		double ctrlTot = ctrlAA + ctrlAa + ctrlaa;
		double AATot = caseAA + ctrlAA;
		double AaTot = caseAa + ctrlAa;
		double aaTot = caseaa + ctrlaa;
		double N = caseTot + ctrlTot;

		// INIT MODEL WEIGHTS
		int[] weights = new int[3];
		switch (model) {
			case DOMINANT:
				weights[0] = 1;
				weights[1] = 1;
				weights[2] = 0;
				break;
			case RECESSIVE:
				weights[0] = 0;
				weights[1] = 1;
				weights[2] = 1;
				break;
			case CODOMINANT:
				weights[0] = 0;
				weights[1] = 1;
				weights[2] = 2;
				break;
			default:
				throw new IllegalArgumentException("invalid Chocran-Armitage trend test model: " + model);
		}

		// CALCULATE TREND TEST
		double trendTest = weights[0] * ((ctrlTot / N) * caseAA
				- (caseTot / N) * ctrlAA)
				+ weights[1] * ((ctrlTot / N) * caseAa
				- (caseTot / N) * ctrlAa)
				+ weights[2] * ((ctrlTot / N) * caseaa
				- (caseTot / N) * ctrlaa);


		// CALCULATE VARIANCE
		double trendTestVar = caseTot
				* ctrlTot
				* ((((Math.pow(weights[0], 2) * (AATot * (N - AATot)))
				+ (Math.pow(weights[1], 2) * (AaTot * (N - AaTot)))
				+ (Math.pow(weights[2], 2) * (aaTot * (N - aaTot))))
				- (2 * ((weights[0] * weights[1] * AATot * AaTot)
				+ (weights[1] * weights[2] * AaTot * aaTot))))
				/ Math.pow(N, 3));


		// CALCULATE TREND TEST CHI-SQR
		double trendTestChiSqr = Math.pow(trendTest, 2) / trendTestVar;

		return trendTestChiSqr;
	}

	public static double calculateGenotypicAssociationChiSquare(
			int obsCaseAA,
			int obsCaseAa,
			int obsCaseaa,
			int caseTot,
			int obsCtrlAA,
			int obsCtrlAa,
			int obsCtrlaa,
			int ctrlTot)
	{
		int[][] obsCntgTable = new int[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl
		double[][] expCntgTable = new double[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl

		obsCntgTable[0][0] = obsCaseAA;
		obsCntgTable[1][0] = obsCaseAa;
		obsCntgTable[2][0] = obsCaseaa;

		obsCntgTable[0][1] = obsCtrlAA;
		obsCntgTable[1][1] = obsCtrlAa;
		obsCntgTable[2][1] = obsCtrlaa;

		int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		int obsAAColTot = obsCaseAA + obsCtrlAA;
		int obsAaColTot = obsCaseAa + obsCtrlAa;
		int obsaaColTot = obsCaseaa + obsCtrlaa;
		int totGT = obsCaseRowTot + obsCtrlRowTot;

		double chiSQ = 0;
		if (totGT != 0) {
			expCntgTable[0][0] = (double) (obsCaseRowTot * obsAAColTot) / totGT;
			expCntgTable[1][0] = (double) (obsCaseRowTot * obsAaColTot) / totGT;
			expCntgTable[2][0] = (double) (obsCaseRowTot * obsaaColTot) / totGT;

			expCntgTable[0][1] = (double) (obsCtrlRowTot * obsAAColTot) / totGT;
			expCntgTable[1][1] = (double) (obsCtrlRowTot * obsAaColTot) / totGT;
			expCntgTable[2][1] = (double) (obsCtrlRowTot * obsaaColTot) / totGT;

			chiSQ = Chisquare.calculateGenotypicAssociationChiSquare(obsCntgTable, expCntgTable);
		}
		return chiSQ;
	}

	public static double[] calculateGenotypicAssociationOR(
			int caseAA,
			int caseAa,
			int caseaa,
			int ctrlAA,
			int ctrlAa,
			int ctrlaa)
	{
//					Genotypes
//					AA        Aa       aa
//		Cases       caseAA    caseAa   caseaa
//		Controls    ctrlAA    ctrlAa   ctrlaa
//
//		There are three sets of odds.
//		The odds of being a case, given the genotype AA are a/d, for Aa they are b/e, and for aa, they are c/f.
//		These odds can be compared to one another. So the odds ratio comparing Aa to aa is,
//
//		OR10 = (caseAa/ctrlAa) / (caseaa/ctrlaa) = caseAa*ctrlaa/caseaa*ctrlAa,
//
//		and that comparing AA to aa is,
//
//		OR20 = (caseAA/ctrlAA) / (caseaa/ctrlaa) = caseAA*ctrlaa/caseaa*ctrlAA.

		double[] oddsRatio = new double[2]; // ORAAaa, ORAaaa
		oddsRatio[0] = (double) (caseAa * ctrlaa) / (caseaa * ctrlAa);  // ORAAaa
		oddsRatio[1] = (double) (caseAA * ctrlaa) / (caseaa * ctrlAA);  // ORAaaa
		return oddsRatio;
	}

	public static float calculateGenotypicFisherExactTest(
			int sampleNb,
			int obsCaseAA,
			int obsCaseAa,
			int obsCaseaa,
			int caseTot,
			int obsCtrlAA,
			int obsCtrlAa,
			int obsCtrlaa,
			int ctrlTot)
	{
		// TODO implement Fisher's exact test
		int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		int obsAAColTot = obsCaseAA + obsCtrlAA;
		int obsAaColTot = obsCaseAa + obsCtrlAa;
		int obsaaColTot = obsCaseaa + obsCtrlaa;
		int totGT = obsCaseRowTot + obsCtrlRowTot;

		BigInteger rowBang = StatisticsUtils.factorial(BigInteger.valueOf(obsCaseRowTot)).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCtrlRowTot)));
		BigInteger colBang = StatisticsUtils.factorial(BigInteger.valueOf(obsAAColTot)).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsAaColTot))).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsaaColTot)));
		BigInteger denomBang = StatisticsUtils.factorial(BigInteger.valueOf(totGT)).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCaseAA)));
		denomBang = denomBang.multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCaseAa)));
		denomBang = denomBang.multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCaseaa)));
		denomBang = denomBang.multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCtrlAA)));
		denomBang = denomBang.multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCtrlAa)));
		denomBang = denomBang.multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCtrlaa)));

		BigInteger result = rowBang.multiply(colBang);
		result = result.divide(denomBang);

		return result.floatValue();
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="ALLELIC TESTS">
	public static double calculateAllelicAssociationChiSquare(
			int sampleNb,
			int obsCaseAA,
			int obsCaseAa,
			int obsCaseaa,
			int caseTot,
			int obsCtrlAA,
			int obsCtrlAa,
			int obsCtrlaa,
			int ctrlTot)
	{
		int[][] obsCntgTable = new int[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl
		double[][] expCntgTable = new double[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl

		obsCntgTable[0][0] = obsCaseAA;
		obsCntgTable[1][0] = obsCaseAa;
		obsCntgTable[2][0] = obsCaseaa;

		obsCntgTable[0][1] = obsCtrlAA;
		obsCntgTable[1][1] = obsCtrlAa;
		obsCntgTable[2][1] = obsCtrlaa;

		int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		int totGT = obsCaseRowTot + obsCtrlRowTot;

		int obsAAColTot = obsCaseAA + obsCtrlAA;
		int obsAaColTot = obsCaseAa + obsCtrlAa;
		int obsaaColTot = obsCaseaa + obsCtrlaa;

		double chiSQ = Double.NaN;
		if (totGT != 0) {
			expCntgTable[0][0] = (double) (obsCaseRowTot * obsAAColTot) / totGT;
			expCntgTable[1][0] = (double) (obsCaseRowTot * obsAaColTot) / totGT;
			expCntgTable[2][0] = (double) (obsCaseRowTot * obsaaColTot) / totGT;

			expCntgTable[0][1] = (double) (obsCtrlRowTot * obsAAColTot) / totGT;
			expCntgTable[1][1] = (double) (obsCtrlRowTot * obsAaColTot) / totGT;
			expCntgTable[2][1] = (double) (obsCtrlRowTot * obsaaColTot) / totGT;

			chiSQ = Chisquare.calculateAllelicAssociationChiSquare(obsCntgTable, expCntgTable);
		}
		return chiSQ;
	}

	public static double calculateAllelicAssociationOR(
			int caseAA,
			int caseAa,
			int caseaa,
			int ctrlAA,
			int ctrlAa,
			int ctrlaa)
	{
//		1) Check if any value is 0
//			=> YES: OR = NaN
//			=> NO
//						  A           a
//			  Cases       caseA	casea
//			  Controls    ctrlA	ctrla
//
//			  OR=caseA*ctrla/casea*ctrlA,
//
//		2) If OR is < 1
//			=> OR = 1/OR

		double oddsRatio = Double.NaN;

		double numerator = ((2 * caseaa) + caseAa) * ((2 * ctrlAA) + ctrlAa);
		double denominator = ((2 * caseAA) + caseAa) * ((2 * ctrlaa) + ctrlAa);

		if (denominator != 0.0) {
			oddsRatio = numerator / denominator;
		}

		return oddsRatio;
	}
	//</editor-fold>
}
