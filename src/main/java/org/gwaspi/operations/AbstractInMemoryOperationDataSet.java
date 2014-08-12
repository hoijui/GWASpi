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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.datasource.filter.IndicesFilteredMarkersGenotypesSource;
import org.gwaspi.datasource.filter.IndicesFilteredMarkersMetadataSource;
import org.gwaspi.datasource.filter.IndicesFilteredSamplesGenotypesSource;
import org.gwaspi.datasource.filter.IndicesFilteredSamplesInfosSource;
import org.gwaspi.datasource.filter.InternalIndicesFilteredMarkersGenotypesSource;
import org.gwaspi.datasource.filter.InternalIndicesFilteredSamplesGenotypesSource;
import org.gwaspi.datasource.inmemory.AbstractInMemoryListSource;
import org.gwaspi.datasource.inmemory.InMemoryChromosomesInfosSource;
import org.gwaspi.datasource.inmemory.InMemoryChromosomesKeysSource;
import org.gwaspi.datasource.inmemory.InMemoryMarkersGenotypesSource;
import org.gwaspi.datasource.inmemory.InMemoryMarkersKeysSource;
import org.gwaspi.datasource.inmemory.InMemoryMarkersMetadataSource;
import org.gwaspi.datasource.inmemory.InMemorySamplesGenotypesSource;
import org.gwaspi.datasource.inmemory.InMemorySamplesInfosSource;
import org.gwaspi.datasource.inmemory.InMemorySamplesKeysSource;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;

public abstract class AbstractInMemoryOperationDataSet<ET extends OperationDataEntry>
		extends AbstractOperationDataSet<ET>
{
	private Integer numMarkers;
	private Integer numSamples;
	private Integer numChromosomes;
	private Boolean useAllMarkersFromParent;
	private Boolean useAllSamplesFromParent;
	private Boolean useAllChromosomesFromParent;
//	private Map<Integer, SampleKey> matrixIndexSampleKeys;
//	private Map<Integer, MarkerKey> matrixIndexMarkerKeys;
//	private Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys;
	private MarkersKeysSource markersKeysSource;
	private MarkersMetadataSource markersInfosSource;
	private SamplesKeysSource samplesKeysSource;
	private SamplesInfosSource samplesInfosSource;
	private ChromosomesKeysSource chromosomesKeysSource;
	private ChromosomesInfosSource chromosomesInfosSource;
	private final List<ET> elements;

	public AbstractInMemoryOperationDataSet(
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey)
	{
		super(origin, parent, operationKey, 0 /* entriesWriteBufferSize, unused */);

		this.numMarkers = null;
		this.numSamples = null;
		this.numChromosomes = null;
		this.useAllMarkersFromParent = null;
		this.useAllSamplesFromParent = null;
		this.useAllChromosomesFromParent = null;
		this.markersKeysSource = null;
		this.markersInfosSource = null;
		this.samplesKeysSource = null;
		this.samplesInfosSource = null;
		this.chromosomesKeysSource = null;
		this.chromosomesInfosSource = null;
		this.elements = new ArrayList<ET>();
	}

	public AbstractInMemoryOperationDataSet(
			MatrixKey origin,
			DataSetKey parent)
	{
		this(origin, parent, null);
	}

	@Override
	protected int getNumMarkersRaw() throws IOException {
		return numMarkers;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the markers from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of markers
	 *   for this operation.
	 */
	@Override
	protected void setUseAllMarkersFromParent(boolean useAll) {
		this.useAllMarkersFromParent = useAll;
	}

	@Override
	protected boolean getUseAllMarkersFromParent() throws IOException {

		if (useAllMarkersFromParent == null) {
			throw new IllegalStateException("This value has not yet been set");
		}

		return useAllMarkersFromParent;
	}

	@Override
	protected int getNumSamplesRaw() throws IOException {
		return numSamples;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the samples from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of samples
	 *   for this operation.
	 */
	@Override
	protected void setUseAllSamplesFromParent(boolean useAll) {
		this.useAllSamplesFromParent = useAll;
	}

	@Override
	protected boolean getUseAllSamplesFromParent() throws IOException {

		if (useAllSamplesFromParent == null) {
			throw new IllegalStateException("This value has not yet been set");
		}

		return useAllSamplesFromParent;
	}

	@Override
	protected int getNumChromosomesRaw() throws IOException {
		return numChromosomes;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the chromosomes from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of chromosomes
	 *   for this operation.
	 */
	@Override
	protected void setUseAllChromosomesFromParent(boolean useAll) {
		this.useAllChromosomesFromParent = useAll;
	}

	@Override
	protected boolean getUseAllChromosomesFromParent() throws IOException {

		if (useAllChromosomesFromParent == null) {
			throw new IllegalStateException("This value has not yet been set");
		}

		return useAllChromosomesFromParent;
	}

	@Override
	public void setSamples(List<Integer> originalIndices, List<SampleKey> keys) throws IOException {

		if (!getUseAllSamplesFromParent()) {
			this.samplesKeysSource = InMemorySamplesKeysSource.createForOperation(getOrigin(), getOrigin().getStudyKey(), keys, originalIndices);
		}
	}

	@Override
	public void setMarkers(List<Integer> originalIndices, List<MarkerKey> keys) throws IOException {

		if (!getUseAllMarkersFromParent()) {
			this.markersKeysSource = InMemoryMarkersKeysSource.createForOperation(getOrigin(), keys, originalIndices);
		}
	}

	@Override
	public void setChromosomes(List<Integer> originalIndices, List<ChromosomeKey> keys) throws IOException {

		if (!getUseAllChromosomesFromParent()) {
			this.chromosomesKeysSource = InMemoryChromosomesKeysSource.createForOperation(getOrigin(), keys, originalIndices);
		}
	}

	@Override
	public void addEntry(ET entry) throws IOException {
		elements.add(entry);
	}

	@Override
	public List<ET> getEntries(int from, int to) throws IOException {
		return Collections.unmodifiableList(elements);
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<ET> writeBuffer)
			throws IOException
	{
	}

	@Override
	public SamplesKeysSource getSamplesKeysSourceRaw() throws IOException {

		if (useAllSamplesFromParent) {
			return getParentDataSetSource().getSamplesKeysSource();
		} else if (samplesKeysSource == null) {
			List<SampleKey> keys = AbstractInMemoryListSource.extractProperty(
					(List<OperationDataEntry<SampleKey>>) getEntries(),
					new OperationDataEntry.KeyExtractor<SampleKey>());
			List<Integer> originalIndices = AbstractInMemoryListSource.extractProperty(
					getEntries(),
					OperationDataEntry.TO_INDEX);
			samplesKeysSource = InMemorySamplesKeysSource.createForOperation(getOrigin(), getOrigin().getStudyKey(),
					keys, originalIndices);
		}

		return samplesKeysSource;
	}

	@Override
	protected SamplesInfosSource getSamplesInfosSourceRaw() throws IOException {

		if (useAllSamplesFromParent) {
			return getParentDataSetSource().getSamplesInfosSource();
		} else {
			return new IndicesFilteredSamplesInfosSource(
					this,
					InMemorySamplesInfosSource.createForMatrix(this, getOrigin(), null),
					getSamplesKeysSource().getIndices());
		}
	}

	@Override
	protected SamplesGenotypesSource getSamplesGenotypesSourceRaw() throws IOException {

		if (useAllSamplesFromParent && useAllMarkersFromParent) {
			return getParentDataSetSource().getSamplesGenotypesSource();
		} else {
			return new IndicesFilteredSamplesGenotypesSource(
					new InternalIndicesFilteredSamplesGenotypesSource(
							InMemorySamplesGenotypesSource.createForMatrix(getOrigin(), null, null),
							getMarkersKeysSource().getIndices()),
					getSamplesKeysSource().getIndices());
		}
	}

	@Override
	protected MarkersKeysSource getMarkersKeysSourceRaw() throws IOException {

		if (useAllMarkersFromParent) {
			return getParentDataSetSource().getMarkersKeysSource();
		} else if (markersKeysSource == null) {
			List<MarkerKey> keys = AbstractInMemoryListSource.extractProperty(
					(List<OperationDataEntry<MarkerKey>>) getEntries(),
					new OperationDataEntry.KeyExtractor<MarkerKey>());
			List<Integer> originalIndices = AbstractInMemoryListSource.extractProperty(
					getEntries(),
					OperationDataEntry.TO_INDEX);
			markersKeysSource = InMemoryMarkersKeysSource.createForOperation(getOrigin(),
					keys, originalIndices);
		}

		return markersKeysSource;
	}

	@Override
	protected MarkersMetadataSource getMarkersMetadatasSourceRaw() throws IOException {

		if (useAllMarkersFromParent) {
			return getParentDataSetSource().getMarkersMetadatasSource();
		} else {
			return new IndicesFilteredMarkersMetadataSource(
					this,
					InMemoryMarkersMetadataSource.createForMatrix(this, getOrigin(), null),
					getMarkersKeysSource().getIndices());
		}
	}

	@Override
	protected MarkersGenotypesSource getMarkersGenotypesSourceRaw() throws IOException {

		if (useAllSamplesFromParent && useAllMarkersFromParent) {
			return getParentDataSetSource().getMarkersGenotypesSource();
		} else {
			return new IndicesFilteredMarkersGenotypesSource(
					new InternalIndicesFilteredMarkersGenotypesSource(
							InMemoryMarkersGenotypesSource.createForMatrix(getOrigin(), null, null),
							getSamplesKeysSource().getIndices()),
					getMarkersKeysSource().getIndices());
		}
	}

	@Override
	protected ChromosomesKeysSource getChromosomesKeysSourceRaw() throws IOException {

		if (useAllChromosomesFromParent) {
			return getParentDataSetSource().getChromosomesKeysSource();
		} else if (chromosomesKeysSource == null) {
			Map<Integer, ChromosomeKey> aggregateChromosomeKeys
					= ChromosomeUtils.aggregateChromosomeIndicesAndKeys(
							getParentDataSetSource().getChromosomesKeysSource().getIndicesMap(),
							ChromosomeUtils.aggregateChromosomeKeys(getMarkersMetadatasSource().getChromosomes()));
			chromosomesKeysSource = InMemoryChromosomesKeysSource.createForOperation(
					getOrigin(),
					new ArrayList<ChromosomeKey>(aggregateChromosomeKeys.values()),
					new ArrayList<Integer>(aggregateChromosomeKeys.keySet()));
		}

		return chromosomesKeysSource;
	}

	@Override
	protected ChromosomesInfosSource getChromosomesInfosSourceRaw() throws IOException {

		if (useAllChromosomesFromParent) {
			return getParentDataSetSource().getChromosomesInfosSource();
		} else if (chromosomesInfosSource == null) {
			final List<Integer> originalIndices = getChromosomesKeysSource().getIndices();

			final List<ChromosomeInfo> values = new ArrayList<ChromosomeInfo>(originalIndices.size());
			if (!originalIndices.isEmpty()) {
				// filter the info entries by the filtered keys indices
				final ChromosomesInfosSource originChromosomesInfosSource = getOriginDataSetSource().getChromosomesInfosSource();
				final Iterator<Integer> filteredOriginalIndicesIt = originalIndices.iterator();
				Integer currentOrigIndex = filteredOriginalIndicesIt.next();
				int index = 0;
				for (ChromosomeInfo chromosomeInfo : originChromosomesInfosSource) {
					if (currentOrigIndex == index) {
						values.add(chromosomeInfo);
						if (filteredOriginalIndicesIt.hasNext()) {
							currentOrigIndex = filteredOriginalIndicesIt.next();
						} else {
							break;
						}
					}
					index++;
				}
			}

			chromosomesInfosSource = InMemoryChromosomesInfosSource.createForOperation(getOrigin(),
					values, originalIndices);
		}

		return chromosomesInfosSource;
	}
}
