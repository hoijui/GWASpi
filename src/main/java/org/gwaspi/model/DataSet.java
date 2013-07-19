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

package org.gwaspi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Holds a whole (raw) dataset in memory.
 */
public class DataSet {

	private final Collection<SampleInfo> sampleInfos;
	private final Map<MarkerKey, MarkerMetadata> markerMetadatas;
	private final List<Collection<byte[]>> samplesAlleles;

	public DataSet() {

		// NOTE We use LinkedHashSet to:
		//   - preserve insertion order of the first insertion;
		//     for example: first dummies get inserted, as compleete set,
		//     in the correct order,
		//     then eventually real/extended info is inserted,
		//     possibly in a chaotic order (Linked)
		//   - not have duplicate entries (Set)
		this.sampleInfos = new LinkedHashSet<SampleInfo>();
		// NOTE We use LinkedHashMap to preserve insertion order!
		this.markerMetadatas = new LinkedHashMap<MarkerKey, MarkerMetadata>();
		this.samplesAlleles = new ArrayList<Collection<byte[]>>();
	}

	/**
	 * @return all sample infos of the data-set, in a well specified order.
	 */
	public Collection<SampleInfo> getSampleInfos() {
		return sampleInfos;
	}

	/**
	 * @return all markers meta-data of the data-set, in a well specified order.
	 */
	public Map<MarkerKey, MarkerMetadata> getMarkerMetadatas() {
		return markerMetadatas;
	}

//	/**
//	 * @return all alleles/genotype pairs of the specified sample.
//	 * @see #getSampleInfos()
//	 */
//	public Collection<byte[]> getSampleAlleles(int sampleIndex) {
//		return samplesAlleles.get(sampleIndex);
//	}
	/**
	 * @return all alleles/genotype pairs of the all samples.
	 */
	public Collection<Collection<byte[]>> getSamplesAlleles() {
		return samplesAlleles;
	}
}
