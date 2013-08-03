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

package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

/**
 * Is the receiving end of a whole data-set loading process.
 * This might store to ram, to an other format directly,
 * to this softwares internal format using NetCDF,
 * or ...?
 */
public interface DataSetDestination {

	void init() throws IOException;

	/**
	 * dummy means here, that these infos are used, if there are no real,
	 * full sample infos available for one, multiple
	 * or all samples in the data-set.
	 */
	void startLoadingDummySampleInfos() throws IOException;
	void finishedLoadingDummySampleInfos() throws IOException;

	void startLoadingSampleInfos(boolean storeOnlyKeys) throws IOException;
	void addSampleInfo(SampleInfo sampleInfo) throws IOException;
	void addSampleKey(SampleKey sampleKey) throws IOException;
	void finishedLoadingSampleInfos() throws IOException;

	void startLoadingMarkerMetadatas(boolean storeOnlyKeys) throws IOException;
	void addMarkerMetadata(MarkerMetadata markerMetadata) throws IOException;
	void addMarkerKey(MarkerKey markerKey) throws IOException;
	void finishedLoadingMarkerMetadatas() throws IOException;

	void startLoadingChromosomeMetadatas() throws IOException;
	void addChromosomeMetadata(ChromosomeKey chromosomeKey, ChromosomeInfo chromosomeInfo) throws IOException;
	void finishedLoadingChromosomeMetadatas() throws IOException;

	void startLoadingAlleles(boolean perSample) throws IOException;
	/**
	 * Adds all the GTs/SNPs for a single sample (one GT per marker).
	 * @param sampleIndex  index in relation to the samples infos
	 *   (as in, the index of a list created in the order they were added)
	 * @see #addSampleInfo
	 */
	void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException;
	/**
	 * Adds all the GTs/SNPs for a single marker (one GT per sample).
	 * @param markerIndex  index in relation to the markers meta-data
	 *   (as in, the index of a list created in the order they were added)
	 * @see #addMarkerMetadata
	 */
	void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException;
	void finishedLoadingAlleles() throws IOException;

	void done() throws IOException;
}
