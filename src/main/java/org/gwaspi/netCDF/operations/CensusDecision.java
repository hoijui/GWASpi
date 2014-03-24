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


import org.gwaspi.model.SampleInfo.Sex;

public enum CensusDecision {

	CountAutosomally,
	CountMalesNonAutosomally,
	CountFemalesNonAutosomally;

	public static CensusDecision getDecisionByChrAndSex(final String chr, final Sex sex) {

		final CensusDecision decision;

		if (chr.equals("X") && (sex == Sex.MALE)) {
			// Do not count to census when Chromosome is X and Sex is male
			decision = CensusDecision.CountMalesNonAutosomally;
		} else if (chr.equals("Y") && (sex == Sex.FEMALE)) {
			// Do not count to census when Chromosome is Y and Sex is female
			decision = CensusDecision.CountFemalesNonAutosomally;
		} else {
			decision = CensusDecision.CountAutosomally;
		}

		return decision;
	}
}
