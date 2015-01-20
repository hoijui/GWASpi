/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.reports;

import org.gwaspi.global.Extractor;
import org.gwaspi.gui.reports.Report_Analysis;

public abstract class AbstractReportLineParser implements Extractor<String[], Object[]> {

	private final boolean exactValues;

	protected AbstractReportLineParser(final boolean exactValues) {

		this.exactValues = exactValues;
	}

	private static Double tryToRoundNicely(final Double exactValue) {

		Double roundedValue;
		try {
			roundedValue = Double.parseDouble(Report_Analysis.FORMAT_ROUND.format(exactValue));
		} catch (final NumberFormatException ex) {
			roundedValue = exactValue;
		}

		return roundedValue;
	}

	protected Double maybeTryToRoundNicely(final Double exactValue) {

		Double resultValue;
		if (exactValues) {
			resultValue = exactValue;
		} else {
			resultValue = tryToRoundNicely(exactValue);
		}

		return resultValue;
	}

	protected static Double tryToParseDouble(final String parsedValue) {
		return (parsedValue != null) ? Double.parseDouble(parsedValue) : Double.NaN;
	}
}
