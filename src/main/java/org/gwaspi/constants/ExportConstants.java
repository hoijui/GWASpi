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

package org.gwaspi.constants;

public class ExportConstants {

	public static final String SEPARATOR_PLINK = " ";
	public static final String SEPARATOR_PLINK_big = "\t";
	public static final String SEPARATOR_BEAGLE = " ";
	public static final String SEPARATOR_MACH = " ";
	public static final String SEPARATOR_REPORTS = "\t";
	public static final String SEPARATOR_SAMPLE_INFO = "\t";

	public static enum ExportFormat {

		BEAGLE,
		Eigensoft_Eigenstrat,
		GWASpi,
		MACH,
		PLINK,
		PLINK_Binary,
		PLINK_Transposed,
		Spreadsheet;

		public static ExportFormat compareTo(String str) {
			try {
				return valueOf(str);
			} catch (Exception ex) {
				return PLINK;
			}
		}
	}

	private ExportConstants() {
	}
}
