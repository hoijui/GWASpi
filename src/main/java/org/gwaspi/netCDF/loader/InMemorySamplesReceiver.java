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
import org.gwaspi.dao.MatrixService;
import org.gwaspi.datasource.inmemory.MatrixInMemoryDataSetSource;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSet;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.GenotypesListFactory;
import org.gwaspi.model.GenotypesListManager;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.TransposedGenotypesListList;
import org.gwaspi.operations.MatrixCreatingOperationParams;
import org.gwaspi.operations.MatrixMetadataFactory;

/**
 * TODO
 */
public class InMemorySamplesReceiver<P extends MatrixCreatingOperationParams>
		extends AbstractDataSetDestination
{
	private final P params;
	private final MatrixMetadataFactory<DataSet, P> metadataFactory;
	private MatrixKey resultMatrixKey;
	private List<GenotypesList> markerGenotypes;
	private List<GenotypesList> sampleGenotypes;
	private final GenotypesListFactory genotypesListFactory;
	private Boolean loadAllelesPerSample;

	public InMemorySamplesReceiver(
			P params,
			MatrixMetadataFactory<DataSet, P> metadataFactory)
	{
		this.params = params;
		this.metadataFactory = metadataFactory;
		this.resultMatrixKey = null;
		this.markerGenotypes = null;
		this.sampleGenotypes = null;
		this.genotypesListFactory = GenotypesListManager.getCommon();
		this.loadAllelesPerSample = null;
	}

	private MatrixService getMatrixService() {
		return MatricesList.getMatrixService();
	}

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
		super.finishedLoadingAlleles();

		// XXX Maybe instead of using the transposed view directly, use it only as a source, and rather create an actual copy of the list. this woudl speed thigns up, but use mor memory. alternatively, keep always the same view (markers!?) as the actual list one (fast), and always the other (samples) as the view (slower) one
		if (loadAllelesPerSample) {
			markerGenotypes = new TransposedGenotypesListList(sampleGenotypes);
		} else {
			sampleGenotypes = new TransposedGenotypesListList(markerGenotypes);
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

//		final MatrixMetadata matrixMetadata = getDataSet().getMatrixMetadata();
		final MatrixMetadata matrixMetadata = metadataFactory.generateMetadata(this.getDataSet(), params);
		resultMatrixKey = getMatrixService().insertMatrix(matrixMetadata);
		getDataSet().setMatrixMetadata(matrixMetadata);

		// register/store in the in-memory storage
		// NOTE ... that happens inside this ctor
		new MatrixInMemoryDataSetSource(
				resultMatrixKey,
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

	@Override
	public MatrixKey getResultMatrixKey() {
		return resultMatrixKey;
	}
}
