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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;

/**
 * Holds a whole (raw) dataset in memory.
 */
public class DataSet {

	private MatrixMetadata matrixMetadata;
	private final Collection<SampleInfo> sampleInfos;
	private final Map<MarkerKey, MarkerMetadata> markerMetadatas;
//	private List<List<byte[]>> samplesGTs;
	private List<List<byte[]>> markersGTs;
	private Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo;

	public DataSet() {

		this.matrixMetadata = null;
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
//		this.samplesGTs = null;
		this.markersGTs = null;
		this.chromosomeInfo = null;
	}

	public void setMatrixMetadata(MatrixMetadata matrixMetadata) {
		this.matrixMetadata = matrixMetadata;
	}

	public MatrixMetadata getMatrixMetadata() {
		return matrixMetadata;
	}

	/**
	 * Allocates memory to store all alleles.
	 * NOTE: This will use a lot of RAM!
	 *   Approximately 2 * (#samples + 4) * (#markers) Bytes!
	 */
	public void initAlleleStorage() {

//		samplesGTs = new ArrayList<List<byte[]>>(sampleInfos.size());
//		for (int si = 0; si < sampleInfos.size(); si++) {
//			samplesGTs.add(new ArrayList<byte[]>(Collections.nCopies(markerMetadatas.size(), (byte[]) null)));
//		}

		markersGTs = new ArrayList<List<byte[]>>(markerMetadatas.size());
		for (int mi = 0; mi < markerMetadatas.size(); mi++) {
			markersGTs.add(new ArrayList<byte[]>(Collections.nCopies(sampleInfos.size(), (byte[]) null)));
		}
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

	/**
	 * Returns all GTs/Alleles of a single sample.
	 * The returned collection has size() == #markers.
	 * We use both a parameter list and a return list for performance reasons.
	 * This way, we can either fill the supplied one,
	 * eliminating the cost for creating a new one,
	 * or return the internally stored one, in case it exists.
	 * @return all alleles/genotype pairs of the specified sample.
	 * @see #getSampleInfos()
	 */
	public List<byte[]> getSampleAlleles(int sampleIndex, List<byte[]> toFill) {

		int markerIndex = 0;
		for (List<byte[]> markerGTs : markersGTs) {
			toFill.set(markerIndex++, markerGTs.get(sampleIndex));
		}
		return toFill;

//		return samplesAlleles.get(sampleIndex);
	}
//	/**
//	 * @return all alleles/genotype pairs of all the samples.
//	 */
//	public Collection<Collection<byte[]>> getSamplesAlleles() {
//		return samplesAlleles;
//	}

	public void setSampleAlleles(int sampleIndex, Collection<byte[]> newSampleGTs) {

		Iterator<byte[]> newSampleGTsIt = newSampleGTs.iterator();
		for (List<byte[]> markerGTs : markersGTs) {
			markerGTs.set(sampleIndex, newSampleGTsIt.next());
		}
	}

	/**
	 * Returns all GTs/Alleles of a single marker.
	 * The returned collection has size() == #samples.
	 * We use both a parameter list and a return list for performance reasons.
	 * This way, we can either fill the supplied one,
	 * eliminating the cost for creating a new one,
	 * or return the internally stored one, in case it exists.
	 * @return all allele/genotype pairs of the specified marker.
	 * @see #getMarkerMetadatas()
	 */
	public List<byte[]> getMarkerAlleles(int markerIndex, List<byte[]> toFill) {

//		int sampleIndex = 0;
//		for (List<byte[]> sampleGts : samplesGts) {
//			toFill.set(sampleIndex++, sampleGts.get(markerIndex));
//		}
//		return toFill;

		return markersGTs.get(markerIndex);
	}

	public void setMarkerAlleles(int markerIndex, Collection<byte[]> newMarkersGTs) {

		List<byte[]> markerGTs = markersGTs.get(markerIndex);
		int sampleIndex = 0;
		for (byte[] newMarkerGTs : newMarkersGTs) {
			markerGTs.set(sampleIndex++, newMarkerGTs);
		}
	}

	public void extractChromosomeInfos() throws IOException {
		chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(getMarkerMetadatas(), 2, 3);
	}

	public Map<ChromosomeKey, ChromosomeInfo> getChromosomeInfos() {
		return chromosomeInfo;
	}
}
