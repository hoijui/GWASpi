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

package org.gwaspi.database;

public class Utils {

	private Utils() {
	}

	/**
	 * @deprecated unused
	 */
	public static String arrayToString(String[] a, String separator) {
		StringBuilder result = new StringBuilder();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	public static String generateMatrixNetCDFNameByDate() {
		String matrixName = "GT_";
		matrixName += org.gwaspi.global.Utils.getShortDateTimeForFileName();
		matrixName = matrixName.replace(":", "");
		matrixName = matrixName.replace(" ", "");
		matrixName = matrixName.replace("/", "");
//		matrixName = matrixName.replaceAll("[a-zA-Z]", "");

//		matrixName = matrixName.substring(0, matrixName.length() - 3); // Remove "CET" from name
		return matrixName;
	}
}
