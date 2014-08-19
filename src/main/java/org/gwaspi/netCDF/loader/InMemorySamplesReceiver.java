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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.datasource.inmemory.MatrixInMemoryDataSetSource;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;

/**
 * TODO
 */
public class InMemorySamplesReceiver extends AbstractDataSetDestination {

	public InMemorySamplesReceiver() {
	}

	@Override
	public DataSet getDataSet() {
		return super.getDataSet();
	}

//	@Override
//	public void startLoadingAlleles(boolean perSample) throws IOException {
//		super.startLoadingAlleles(perSample);
//
//		getDataSet().initAlleleStorage();
//	}
//
//	@Override
//	public void addSampleGTAlleles(int sampleIndex, Collection<byte[]> sampleAlleles) throws IOException {
//		getDataSet().setSampleAlleles(sampleIndex, sampleAlleles);
//	}
//
//	@Override
//	public void addMarkerGTAlleles(int markerIndex, Collection<byte[]> markerAlleles) throws IOException {
//		getDataSet().setMarkerAlleles(markerIndex, markerAlleles);
//	}

	private List<GenotypesList> markerGenotypes;
	private List<GenotypesList> sampleGenotypes;
	private GenotypesListFactory genotypesListFactory;
	private boolean loadAllelesPerSample;

	@Override
	public void startLoadingAlleles(boolean perSample) throws IOException {
		super.startLoadingAlleles(perSample);

		loadAllelesPerSample = perSample;

		if (loadAllelesPerSample) {
			sampleGenotypes = new ArrayList<GenotypesList>(getDataSet().getSampleInfos().size());
		} else {
			markerGenotypes = new ArrayList<GenotypesList>(getDataSet().getMarkerMetadatas().size());
		}
	}

	@Override
	public void addSampleGTAlleles(int sampleIndex, List<byte[]> sampleAlleles) throws IOException {
		super.addSampleGTAlleles(sampleIndex, sampleAlleles);

		sampleGenotypes.add(sampleIndex, genotypesListFactory.extract(sampleAlleles));
	}

	@Override
	public void addMarkerGTAlleles(int markerIndex, List<byte[]> markerAlleles) throws IOException {
		super.addMarkerGTAlleles(markerIndex, markerAlleles);

		markerGenotypes.add(markerIndex, genotypesListFactory.extract(markerAlleles));
	}

	@Override
	public void finishedLoadingAlleles() throws IOException {

		if (loadAllelesPerSample) {
//			markerGenotypes = ...; // TODO read from sampleGenotypes and store here
		} else {
//			sampleGenotypes = ...; // TODO read from markerGenotypes and store here
		}
	}

	@Override
	public void done() throws IOException {
		super.done();

		// HACK better change data storage collection types inside DataSet already?
		final List<SampleInfo> sampleInfos = new ArrayList<SampleInfo>(getDataSet().getSampleInfos());
		final List<SampleKey> sampleKeys = new ArrayList<SampleKey>(sampleInfos.size());
		for (SampleInfo sampleInfo : sampleInfos) {
			sampleKeys.add(SampleKey.valueOf(sampleInfo));
		}

		final Map<ChromosomeKey, ChromosomeInfo> chromosomeKeysAndInfos = getDataSet().getChromosomeInfos();
		final List<ChromosomeKey> chromosomeKeys = new ArrayList<ChromosomeKey>(chromosomeKeysAndInfos.keySet());
		final List<ChromosomeInfo> chromosomeInfos = new ArrayList<ChromosomeInfo>(chromosomeKeysAndInfos.values());

		final Map<MarkerKey, MarkerMetadata> markerKeysAndMetadatas = getDataSet().getMarkerMetadatas();
		final List<MarkerKey> markerKeys = new ArrayList<MarkerKey>(markerKeysAndMetadatas.keySet());
		final List<MarkerMetadata> markerMetadatas = new ArrayList<MarkerMetadata>(markerKeysAndMetadatas.values());

		final MatrixMetadata matrixMetadata = getDataSet().getMatrixMetadata();
		final MatrixKey matrixKey = MatrixKey.valueOf(matrixMetadata);

		// register/store in the in-memory storage
		new MatrixInMemoryDataSetSource(
				matrixKey,
				matrixMetadata,
				markerGenotypes,
				sampleGenotypes,
				chromosomeKeys,
				chromosomeInfos,
				markerKeys,
				markerMetadatas,
				sampleKeys,
				sampleInfos);
	}
}
