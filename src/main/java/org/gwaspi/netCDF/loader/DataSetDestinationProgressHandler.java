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

import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;

public class DataSetDestinationProgressHandler extends SuperProgressSource {

//	private DataSetDestination dataSetDestination;
	private final IntegerProgressHandler sampleInfosPH;
	private final IntegerProgressHandler markerInfosPH;
	private final IntegerProgressHandler chromosomeInfosPH;
	private final IntegerProgressHandler genotypesPH;

	public DataSetDestinationProgressHandler(ProcessInfo processInfo) {
//		super(processInfo, createSubProgressSourcesAndWeights(processInfo));
		super(processInfo);

		sampleInfosPH = new IntegerProgressHandler(
				new SubProcessInfo(processInfo, "SampleInfos", "Storing sample infos"), -1, -1);
		markerInfosPH = new IntegerProgressHandler(
				new SubProcessInfo(processInfo, "MarkerInfos", "Storing marker infos"), -1, -1);
		chromosomeInfosPH = new IntegerProgressHandler(
				new SubProcessInfo(processInfo, "ChromosomeInfos", "Storing chromosome infos"), -1, -1);
		genotypesPH = new IntegerProgressHandler(
				new SubProcessInfo(processInfo, "Genotypes", "Storing genotypes"), -1, -1);

		addSubProgressSource(sampleInfosPH, 0.002);
		addSubProgressSource(markerInfosPH, 0.017);
		addSubProgressSource(chromosomeInfosPH, 0.001);
		addSubProgressSource(genotypesPH, 0.98);
	}

//	private static Map<ProgressSource, Double> createSubProgressSourcesAndWeights(ProcessInfo superProcessInfo) {
//
//		Map<ProgressSource, Double> subProgressSourcesAndWeights
//				= new LinkedHashMap<ProgressSource, Double>(3);
//
//		subProgressSourcesAndWeights.put(new IntegerProgressHandler(
//				new SubProcessInfo(superProcessInfo, "SampleInfos", "Storing sample infos"),
//				-1, -1), 0.001);
//		subProgressSourcesAndWeights.put(new IntegerProgressHandler(
//				new SubProcessInfo(superProcessInfo, "MarkerInfos", "Storing marker infos"),
//				-1, -1), 0.009);
//		subProgressSourcesAndWeights.put(new IntegerProgressHandler(
//				new SubProcessInfo(superProcessInfo, "Genotypes",   "Storing genotypes"),
//				-1, -1), 0.99);
//
//		return subProgressSourcesAndWeights;
//	}

	public IntegerProgressHandler getSampleInfosProgressHandler() {
		return sampleInfosPH;
	}

	public IntegerProgressHandler getMarkerInfosProgressHandler() {
		return markerInfosPH;
	}

	public IntegerProgressHandler getChromosomeInfosProgressHandler() {
		return chromosomeInfosPH;
	}

	public IntegerProgressHandler getGenotypesProgressHandler() {
		return genotypesPH;
	}

//	public void setDataSetDestination(DataSetDestination dataSetDestination) {
//
//		this.dataSetDestination = dataSetDestination;
//		xxx;
//	}
}
