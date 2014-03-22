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
			final int caseAA,
			final int caseAa,
			final int caseaa,
			final int ctrlAA,
			final int ctrlAa,
			final int ctrlaa,
			final ChocranArmitageTrendTestModel model)
	{
		final double caseTot = caseAA + caseAa + caseaa;
		final double ctrlTot = ctrlAA + ctrlAa + ctrlaa;
		final double AATot = caseAA + ctrlAA;
		final double AaTot = caseAa + ctrlAa;
		final double aaTot = caseaa + ctrlaa;
		final double N = caseTot + ctrlTot;

		// initialzie the model weights
		final int[] weights = new int[3];
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

		// calculate trend-test
		final double trendTest
				= weights[0] * ((ctrlTot / N) * caseAA - (caseTot / N) * ctrlAA)
				+ weights[1] * ((ctrlTot / N) * caseAa - (caseTot / N) * ctrlAa)
				+ weights[2] * ((ctrlTot / N) * caseaa - (caseTot / N) * ctrlaa);


		// calculate variance
		final double trendTestVar
				= caseTot * ctrlTot * (
				(((Math.pow(weights[0], 2) * (AATot * (N - AATot)))
				+ (Math.pow(weights[1], 2) * (AaTot * (N - AaTot)))
				+ (Math.pow(weights[2], 2) * (aaTot * (N - aaTot))))
				- (2 * ((weights[0] * weights[1] * AATot * AaTot)
				+ (weights[1] * weights[2] * AaTot * aaTot))))
				/ Math.pow(N, 3));


		// calculate trend-test _X^2
		final double trendTestChiSqr = Math.pow(trendTest, 2) / trendTestVar;

		return trendTestChiSqr;
	}

	public static double calculateGenotypicAssociationChiSquare(
			final int obsCaseAA,
			final int obsCaseAa,
			final int obsCaseaa,
			final int caseTot,
			final int obsCtrlAA,
			final int obsCtrlAa,
			final int obsCtrlaa,
			final int ctrlTot)
	{
		final int[][] obsCntgTable = new int[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl
		final double[][] expCntgTable = new double[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl

		obsCntgTable[0][0] = obsCaseAA;
		obsCntgTable[1][0] = obsCaseAa;
		obsCntgTable[2][0] = obsCaseaa;

		obsCntgTable[0][1] = obsCtrlAA;
		obsCntgTable[1][1] = obsCtrlAa;
		obsCntgTable[2][1] = obsCtrlaa;

		final int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		final int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		final int obsAAColTot = obsCaseAA + obsCtrlAA;
		final int obsAaColTot = obsCaseAa + obsCtrlAa;
		final int obsaaColTot = obsCaseaa + obsCtrlaa;
		final int totGT = obsCaseRowTot + obsCtrlRowTot;

		final double chiSQ;
		if (totGT != 0) {
			expCntgTable[0][0] = (double) (obsCaseRowTot * obsAAColTot) / totGT;
			expCntgTable[1][0] = (double) (obsCaseRowTot * obsAaColTot) / totGT;
			expCntgTable[2][0] = (double) (obsCaseRowTot * obsaaColTot) / totGT;

			expCntgTable[0][1] = (double) (obsCtrlRowTot * obsAAColTot) / totGT;
			expCntgTable[1][1] = (double) (obsCtrlRowTot * obsAaColTot) / totGT;
			expCntgTable[2][1] = (double) (obsCtrlRowTot * obsaaColTot) / totGT;

			chiSQ = Chisquare.calculateGenotypicAssociationChiSquare(obsCntgTable, expCntgTable);
		} else {
			chiSQ = 0;
		}

		return chiSQ;
	}

	/**
	 * Calculate genotypic association odds ratios.
	 *             Genotypes
	 *             AA        Aa       aa
	 * Cases       caseAA    caseAa   caseaa
	 * Controls    ctrlAA    ctrlAa   ctrlaa
	 *
	 * There are three sets of odds.
	 * The odds of being a case, given the genotype AA are a/d,
	 * for Aa they are b/e, and for aa, they are c/f.
	 * These odds can be compared to one another.
	 * So the odds ratio comparing Aa to aa is,
	 * <code>OR10 = (caseAa/ctrlAa) / (caseaa/ctrlaa) = caseAa*ctrlaa/caseaa*ctrlAa</code>,
	 * and that comparing AA to aa is,
	 * <code>OR20 = (caseAA/ctrlAA) / (caseaa/ctrlaa) = caseAA*ctrlaa/caseaa*ctrlAA</code>.
	 *
	 * @param caseAA
	 * @param caseAa
	 * @param caseaa
	 * @param ctrlAA
	 * @param ctrlAa
	 * @param ctrlaa
	 * @return double[2] {OR10, OR20}
	 */
	public static double[] calculateGenotypicAssociationOR(
			final int caseAA,
			final int caseAa,
			final int caseaa,
			final int ctrlAA,
			final int ctrlAa,
			final int ctrlaa)
	{
		final double[] oddsRatio = new double[2]; // ORAAaa, ORAaaa

		oddsRatio[0] = (double) (caseAa * ctrlaa) / (caseaa * ctrlAa);  // ORAAaa
		oddsRatio[1] = (double) (caseAA * ctrlaa) / (caseaa * ctrlAA);  // ORAaaa

		return oddsRatio;
	}

	public static float calculateGenotypicFisherExactTest(
			final int sampleNb,
			final int obsCaseAA,
			final int obsCaseAa,
			final int obsCaseaa,
			final int caseTot,
			final int obsCtrlAA,
			final int obsCtrlAa,
			final int obsCtrlaa,
			final int ctrlTot)
	{
		// TODO implement Fisher's exact test
		final int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		final int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		final int obsAAColTot = obsCaseAA + obsCtrlAA;
		final int obsAaColTot = obsCaseAa + obsCtrlAa;
		final int obsaaColTot = obsCaseaa + obsCtrlaa;
		final int totGT = obsCaseRowTot + obsCtrlRowTot;

		final BigInteger rowBang = StatisticsUtils.factorial(BigInteger.valueOf(obsCaseRowTot)).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsCtrlRowTot)));
		final BigInteger colBang = StatisticsUtils.factorial(BigInteger.valueOf(obsAAColTot)).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsAaColTot))).multiply(StatisticsUtils.factorial(BigInteger.valueOf(obsaaColTot)));
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
			final int sampleNb,
			final int obsCaseAA,
			final int obsCaseAa,
			final int obsCaseaa,
			final int caseTot,
			final int obsCtrlAA,
			final int obsCtrlAa,
			final int obsCtrlaa,
			final int ctrlTot)
	{
		final int[][] obsCntgTable = new int[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl
		final double[][] expCntgTable = new double[3][2]; // 3 columns: AA Aa aa, 2 rows: case, ctrl

		obsCntgTable[0][0] = obsCaseAA;
		obsCntgTable[1][0] = obsCaseAa;
		obsCntgTable[2][0] = obsCaseaa;

		obsCntgTable[0][1] = obsCtrlAA;
		obsCntgTable[1][1] = obsCtrlAa;
		obsCntgTable[2][1] = obsCtrlaa;

		final int obsCaseRowTot = obsCaseAA + obsCaseAa + obsCaseaa;
		final int obsCtrlRowTot = obsCtrlAA + obsCtrlAa + obsCtrlaa;
		final int totGT = obsCaseRowTot + obsCtrlRowTot;

		final int obsAAColTot = obsCaseAA + obsCtrlAA;
		final int obsAaColTot = obsCaseAa + obsCtrlAa;
		final int obsaaColTot = obsCaseaa + obsCtrlaa;

		final double chiSQ;
		if (totGT != 0) {
			expCntgTable[0][0] = (double) (obsCaseRowTot * obsAAColTot) / totGT;
			expCntgTable[1][0] = (double) (obsCaseRowTot * obsAaColTot) / totGT;
			expCntgTable[2][0] = (double) (obsCaseRowTot * obsaaColTot) / totGT;

			expCntgTable[0][1] = (double) (obsCtrlRowTot * obsAAColTot) / totGT;
			expCntgTable[1][1] = (double) (obsCtrlRowTot * obsAaColTot) / totGT;
			expCntgTable[2][1] = (double) (obsCtrlRowTot * obsaaColTot) / totGT;

			chiSQ = Chisquare.calculateAllelicAssociationChiSquare(obsCntgTable, expCntgTable);
		} else {
			chiSQ = Double.NaN;
		}

		return chiSQ;
	}

	/**
	 * Calculates the allelic association adds ratio.
	 * 1) Check if any value is 0
	 * 	=> YES: OR = NaN
	 * 	=> NO
	 *                A       a
	 *     Cases      caseA   casea
	 *     Controls   ctrlA   ctrla
	 *
	 * 	  OR = caseA*ctrla/casea*ctrlA,
	 *
	 * 2) If OR is < 1
	 *  => OR = 1/OR
	 *
	 * @param caseAA
	 * @param caseAa
	 * @param caseaa
	 * @param ctrlAA
	 * @param ctrlAa
	 * @param ctrlaa
	 * @return
	 */
	public static double calculateAllelicAssociationOR(
			final int caseAA,
			final int caseAa,
			final int caseaa,
			final int ctrlAA,
			final int ctrlAa,
			final int ctrlaa)
	{
		final double oddsRatio;

		final double numerator = ((2 * caseaa) + caseAa) * ((2 * ctrlAA) + ctrlAa);
		final double denominator = ((2 * caseAA) + caseAa) * ((2 * ctrlaa) + ctrlAa);

		if (denominator != 0.0) {
			oddsRatio = numerator / denominator;
		} else {
			oddsRatio = Double.NaN;
		}

		return oddsRatio;
	}
	//</editor-fold>
}
