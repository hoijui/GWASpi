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

package org.gwaspi.operations.hardyweinberg;

import java.util.Comparator;
import org.gwaspi.global.Extractor;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.operations.OperationDataEntry;

public interface HardyWeinbergOperationEntry extends OperationDataEntry<MarkerKey> {

	public static enum Category {
		ALL,
		CASE,
		CONTROL,
		ALTERNATE;
	}

	static class PValueComparator implements Comparator<HardyWeinbergOperationEntry> {

		@Override
		public int compare(HardyWeinbergOperationEntry e1, HardyWeinbergOperationEntry e2) {
			return Double.compare(e1.getP(), e2.getP());
		}
	}
	public static final Comparator<HardyWeinbergOperationEntry> P_VALUE_COMPARATOR = new PValueComparator();

	public static final Extractor<HardyWeinbergOperationEntry, Category> CATEGORY_EXTRACTOR
			= new Extractor<HardyWeinbergOperationEntry, Category>() {

				@Override
				public Category extract(HardyWeinbergOperationEntry object) {
					return object.getCategory();
				}
			};

	public static final Extractor<HardyWeinbergOperationEntry, Double> P_VALUE_EXTRACTOR
			= new Extractor<HardyWeinbergOperationEntry, Double>() {

				@Override
				public Double extract(HardyWeinbergOperationEntry object) {
					return object.getP();
				}
			};

	public static final Extractor<HardyWeinbergOperationEntry, Double> HETZY_OBSERVED_EXTRACTOR
			= new Extractor<HardyWeinbergOperationEntry, Double>() {

				@Override
				public Double extract(HardyWeinbergOperationEntry object) {
					return object.getObsHzy();
				}
			};

	public static final Extractor<HardyWeinbergOperationEntry, Double> HETZY_EXPECTED_EXTRACTOR
			= new Extractor<HardyWeinbergOperationEntry, Double>() {

				@Override
				public Double extract(HardyWeinbergOperationEntry object) {
					return object.getExpHzy();
				}
			};

	/**
	 * @return what category this marker belongs to
	 */
	Category getCategory();

	/**
	 * @return P-Value
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT
	 * @see #getCategory()
	 */
	double getP();

	/**
	 * @return Observed Heterozygosity
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [0] Control
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [0] Alternate
	 * @see #getCategory()
	 */
	double getObsHzy();

	/**
	 * @return Hardy-Weinberg Expected Heterozygosity
	 *   NetCDF variables:
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL [1] Control
	 *   - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT  [1] Alternate
	 * @see #getCategory()
	 */
	double getExpHzy();
}
