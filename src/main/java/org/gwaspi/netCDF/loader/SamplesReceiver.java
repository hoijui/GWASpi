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


import java.util.Collection;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;

/**
 * Is the receiving end of a whole data-set loading process.
 * This might store to ram, to an other format directly,
 * to this softwares internal format using NetCDF,
 * or ...?
 */
public interface SamplesReceiver {

	void init() throws Exception;

	/**
	 * dummy means here, that these infos are used, if there are no real,
	 * full sample infos available for one, multiple
	 * or all samples in the data-set.
	 */
	void startLoadingDummySampleInfos() throws Exception;
	void finishedLoadingDummySampleInfos() throws Exception;

	void startLoadingSampleInfos() throws Exception;
	void addSampleInfo(SampleInfo sampleInfo) throws Exception;
	void finishedLoadingSampleInfos() throws Exception;

	void startLoadingMarkerMetadatas() throws Exception;
	void addMarkerMetadata(MarkerMetadata markerMetadata) throws Exception;
	void finishedLoadingMarkerMetadatas() throws Exception;

	void startLoadingAlleles() throws Exception;
	void addSampleGTAlleles(Collection<byte[]> sampleAlleles) throws Exception;
	void finishedLoadingAlleles() throws Exception;

	void done() throws Exception;
}
