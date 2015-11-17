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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Chisquare {

	private Chisquare() {
	}

	public static double calculateHWChiSquare(int obsAA, double expAA,
			int obsAa, double expAa,
			int obsaa, double expaa) {

		double chiSQ = (Math.pow(obsAA - expAA, 2) / (expAA))
				+ (Math.pow(obsaa - expaa, 2) / (expaa))
				+ (Math.pow(obsAa - expAa, 2) / (expAa));
		return chiSQ;
	}

	static double calculateGenotypicAssociationChiSquare(int[][] obsCntgTable, double[][] expCntgTable) {
		//3 columns: AA Aa aa, 2 rows: case, ctrl
		double chiSQ = (Math.pow(obsCntgTable[0][0] - expCntgTable[0][0], 2) / (expCntgTable[0][0]))
				+ (Math.pow(obsCntgTable[1][0] - expCntgTable[1][0], 2) / (expCntgTable[1][0]))
				+ (Math.pow(obsCntgTable[2][0] - expCntgTable[2][0], 2) / (expCntgTable[2][0]))
				+ (Math.pow(obsCntgTable[0][1] - expCntgTable[0][1], 2) / (expCntgTable[0][1]))
				+ (Math.pow(obsCntgTable[1][1] - expCntgTable[1][1], 2) / (expCntgTable[1][1]))
				+ (Math.pow(obsCntgTable[2][1] - expCntgTable[2][1], 2) / (expCntgTable[2][1]));
		return chiSQ;
	}

	static double calculateAllelicAssociationChiSquare(int[][] obsCntgTable, double[][] expCntgTable) {
		//3 columns: AA Aa aa, 2 rows: case, ctrl
		double obsCaseA = 2 * obsCntgTable[0][0] + obsCntgTable[1][0];
		double obsCasea = 2 * obsCntgTable[2][0] + obsCntgTable[1][0];

		double expCaseA = 2 * expCntgTable[0][0] + expCntgTable[1][0];
		double expCasea = 2 * expCntgTable[2][0] + expCntgTable[1][0];

		double obsCtrlA = 2 * obsCntgTable[0][1] + obsCntgTable[1][1];
		double obsCtrla = 2 * obsCntgTable[2][1] + obsCntgTable[1][1];

		double expCtrlA = 2 * expCntgTable[0][1] + expCntgTable[1][1];
		double expCtrla = 2 * expCntgTable[2][1] + expCntgTable[1][1];

		double chiSQ = (Math.pow(obsCaseA - expCaseA, 2) / expCaseA)
				+ (Math.pow(obsCasea - expCasea, 2) / expCasea)
				+ (Math.pow(obsCtrlA - expCtrlA, 2) / expCtrlA)
				+ (Math.pow(obsCtrla - expCtrla, 2) / expCtrla);
		return chiSQ;
	}

	public static List<Double> getChiSquareDistributionDf1(int size, float C) {
		List<Double> chiQuareDist = new ArrayList<Double>();
		Random generator = new Random();
		for (int i = 0; i < size; i++) {
			double gaussRnd = (generator.nextGaussian()) * C;
			double chiSqrRnd = Math.pow(gaussRnd, 2);
			chiQuareDist.add(chiSqrRnd);
		}

		return chiQuareDist;
	}

	public static List<Double> getChiSquareDistributionDf2(int size, float C) {
		List<Double> chiQuareDist = new ArrayList<Double>();
		Random generator = new Random();
		for (int i = 0; i < size; i++) {
			double gaussRnd1 = (generator.nextGaussian()) * C;
			double gaussRnd2 = (generator.nextGaussian()) * C;
			double chiSqrRnd = Math.pow(gaussRnd1, 2) + Math.pow(gaussRnd2, 2);
			chiQuareDist.add(chiSqrRnd);
		}

		return chiQuareDist;
	}
}
