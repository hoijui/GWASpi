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

package org.gwaspi.datasource.inmemory;

import java.io.IOException;
import java.util.List;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;

/**
 * TODO add description
 * TODO rename to MatrixNetCdfDataSetSource
 */
public class MatrixInMemoryDataSetSource implements DataSetSource {

	private final MatrixKey matrixKey;
	private final MatrixMetadata matrixMetadata;
	private final List<Integer> originalIndices;
	private final ChromosomesKeysSource chromosomesKeysSource;
	private final ChromosomesInfosSource chromosomesInfosSource;
	private final MarkersKeysSource markersKeysSource;
	private final MarkersMetadataSource markersMetadataSource;
	private final MarkersGenotypesSource markersGenotypesSource;
	private final SamplesKeysSource samplesKeysSource;
	private final SamplesInfosSource samplesInfosSource;
	private final SamplesGenotypesSource samplesGenotypesSource;

	public MatrixInMemoryDataSetSource(
			MatrixKey matrixKey,
			MatrixMetadata matrixMetadata,
			List<GenotypesList> markerGTs,
			List<GenotypesList> sampleGTs,
			List<ChromosomeKey> chromosomeKeys,
			List<ChromosomeInfo> chromosomeInfos,
			List<MarkerKey> markerKeys,
			List<MarkerMetadata> markerMetadatas,
			List<SampleKey> sampleKeys,
			List<SampleInfo> sampleInfos)
			throws IOException
	{
		this.matrixKey = matrixKey;
//		this.matrixMetadata = MatricesList.getMatrixMetadataById(matrixKey);
		this.matrixMetadata = matrixMetadata;
		this.originalIndices = null;

		this.chromosomesKeysSource = InMemoryChromosomesKeysSource.createForMatrix(matrixKey, chromosomeKeys);
		this.chromosomesInfosSource = InMemoryChromosomesInfosSource.createForMatrix(matrixKey, chromosomeInfos);
		this.markersKeysSource = InMemoryMarkersKeysSource.createForMatrix(matrixKey, markerKeys);
		this.markersMetadataSource = InMemoryMarkersMetadataSource.createForMatrix(this, matrixKey, markerMetadatas);
		this.markersGenotypesSource = InMemoryMarkersGenotypesSource.createForMatrix(matrixKey, markerGTs, originalIndices);
		this.samplesGenotypesSource = InMemorySamplesGenotypesSource.createForMatrix(matrixKey, sampleGTs, originalIndices);
		this.samplesInfosSource = InMemorySamplesInfosSource.createForMatrix(this, matrixKey, sampleInfos);
		this.samplesKeysSource = InMemorySamplesKeysSource.createForMatrix(matrixKey, matrixKey.getStudyKey(), sampleKeys);
	}

	/** Use this if the actual data is in memory already! */
	public MatrixInMemoryDataSetSource(MatrixKey matrixKey) throws IOException {
		this(matrixKey, MatricesList.getMatrixMetadataById(matrixKey), null, null, null, null, null, null, null, null);
	}

	public static void clearAllInMemoryStorage() {

		InMemorySamplesKeysSource.clearStorage();
		InMemorySamplesInfosSource.clearStorage();
		InMemorySamplesGenotypesSource.clearStorage();
		InMemoryMarkersMetadataSource.clearStorage();
		InMemoryMarkersKeysSource.clearStorage();
		InMemoryMarkersGenotypesSource.clearStorage();
		InMemoryChromosomesKeysSource.clearStorage();
		InMemoryChromosomesInfosSource.clearStorage();
	}

	@Override
	public DataSetSource getOriginDataSetSource() throws IOException {
		return this;
	}

	@Override
	public MatrixMetadata getMatrixMetadata() throws IOException {
		return matrixMetadata;
	}

	@Override
	public int getNumMarkers() throws IOException {
		return matrixMetadata.getNumMarkers();
	}

	@Override
	public int getNumChromosomes() throws IOException {
		return matrixMetadata.getNumChromosomes();
	}

	@Override
	public int getNumSamples() throws IOException {
		return matrixMetadata.getNumSamples();
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSource() throws IOException {
		return chromosomesKeysSource;
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {
		return chromosomesInfosSource;
	}

	@Override
	public MarkersKeysSource getMarkersKeysSource() throws IOException {
		return markersKeysSource;
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {
		return markersMetadataSource;
	}

	@Override
	public MarkersGenotypesSource getMarkersGenotypesSource() throws IOException {
		return markersGenotypesSource;
	}

	@Override
	public SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {
		return samplesGenotypesSource;
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {
		return samplesInfosSource;
	}

	@Override
	public SamplesKeysSource getSamplesKeysSource() throws IOException {
		return samplesKeysSource;
	}
}
