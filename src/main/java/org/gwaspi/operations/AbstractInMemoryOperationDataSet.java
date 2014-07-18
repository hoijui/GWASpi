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
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.datasource.filter.IndicesFilteredMarkersGenotypesSource;
import org.gwaspi.datasource.filter.IndicesFilteredMarkersMetadataSource;
import org.gwaspi.datasource.filter.IndicesFilteredSamplesGenotypesSource;
import org.gwaspi.datasource.filter.IndicesFilteredSamplesInfosSource;
import org.gwaspi.datasource.filter.InternalIndicesFilteredMarkersGenotypesSource;
import org.gwaspi.datasource.filter.InternalIndicesFilteredSamplesGenotypesSource;
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
import org.gwaspi.datasource.netcdf.NetCdfChromosomesInfosSource;
import org.gwaspi.datasource.netcdf.NetCdfChromosomesKeysSource;
import org.gwaspi.datasource.netcdf.NetCdfMarkersGenotypesSource;
import org.gwaspi.datasource.netcdf.NetCdfMarkersKeysSource;
import org.gwaspi.datasource.netcdf.NetCdfMarkersMetadataSource;
import org.gwaspi.datasource.netcdf.NetCdfSamplesGenotypesSource;
import org.gwaspi.datasource.netcdf.NetCdfSamplesInfosSource;
import org.gwaspi.datasource.netcdf.NetCdfSamplesKeysSource;

public abstract class AbstractInMemoryOperationDataSet<ET> extends AbstractOperationDataSet<ET> {

	private Integer numMarkers;
	private Integer numSamples;
	private Integer numChromosomes;
	private Boolean useAllMarkersFromParent;
	private Boolean useAllSamplesFromParent;
	private Boolean useAllChromosomesFromParent;
	private Map<Integer, SampleKey> matrixIndexSampleKeys;
	private Map<Integer, MarkerKey> matrixIndexMarkerKeys;
	private Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys;

	public AbstractInMemoryOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey,
			int entriesWriteBufferSize)
	{
		super(markersOperationSet, origin, parent, operationKey, entriesWriteBufferSize);

		this.numMarkers = null;
		this.numSamples = null;
		this.numChromosomes = null;
		this.useAllMarkersFromParent = null;
		this.useAllSamplesFromParent = null;
		this.useAllChromosomesFromParent = null;
		this.matrixIndexSampleKeys = null;
		this.matrixIndexMarkerKeys = null;
		this.matrixIndexChromosomeKeys = null;
	}

	public AbstractInMemoryOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey)
	{
		this(markersOperationSet, origin, parent, operationKey, getDefaultEntriesWriteBufferSize(markersOperationSet));
	}

	public AbstractInMemoryOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent)
	{
		this(markersOperationSet, origin, parent, null);
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
			useAllMarkersFromParent = (getNetCdfReadFile().findGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_MARKERS).getNumericValue().intValue() != 0);
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
			useAllSamplesFromParent = (getNetCdfReadFile().findGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_SAMPLES).getNumericValue().intValue() != 0);
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
			useAllChromosomesFromParent = (getNetCdfReadFile().findGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_CHROMOSOMES).getNumericValue().intValue() != 0);
		}

		return useAllChromosomesFromParent;
	}

	@Override
	public void setSamples(Map<Integer, SampleKey> matrixIndexSampleKeys) throws IOException {

		if (!getUseAllSamplesFromParent()) {
			this.matrixIndexSampleKeys = matrixIndexSampleKeys;
		}
	}

	@Override
	public void setMarkers(Map<Integer, MarkerKey> matrixIndexMarkerKeys) throws IOException {

		if (!getUseAllMarkersFromParent()) {
			this.matrixIndexMarkerKeys = matrixIndexMarkerKeys;
		}
	}

	@Override
	public void setChromosomes(Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys/*, Collection<ChromosomeInfo> chromosomeInfos*/) throws IOException {

		if (!getUseAllChromosomesFromParent()) {
			this.matrixIndexChromosomeKeys = matrixIndexChromosomeKeys;
		}
	}

	@Override
	public SamplesKeysSource getSamplesKeysSourceRaw() throws IOException {
		return InMemorySamplesKeysSource.createForOperation(getOrigin(), getOrigin().getStudyKey(), getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	protected SamplesInfosSource getSamplesInfosSourceRaw() throws IOException {
		return new IndicesFilteredSamplesInfosSource(
				this,
				NetCdfSamplesInfosSource.createForMatrix(this, getOrigin().getStudyKey(), getOriginNetCdfReadFile()),
				getSamplesKeysSource().getIndices());
	}

	@Override
	protected SamplesGenotypesSource getSamplesGenotypesSourceRaw() throws IOException {
		return new IndicesFilteredSamplesGenotypesSource(
				new InternalIndicesFilteredSamplesGenotypesSource(
						NetCdfSamplesGenotypesSource.createForMatrix(getOrigin(), getOriginNetCdfReadFile()),
						getMarkersKeysSource().getIndices()),
				getSamplesKeysSource().getIndices());
	}

	@Override
	protected MarkersKeysSource getMarkersKeysSourceRaw() throws IOException {
		return NetCdfMarkersKeysSource.createForOperation(getOrigin(), getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	protected MarkersMetadataSource getMarkersMetadatasSourceRaw() throws IOException {
		return new IndicesFilteredMarkersMetadataSource(
				this,
				NetCdfMarkersMetadataSource.createForMatrix(this, getOriginNetCdfReadFile()),
				getMarkersKeysSource().getIndices());
	}

	@Override
	protected MarkersGenotypesSource getMarkersGenotypesSourceRaw() throws IOException {
		return new IndicesFilteredMarkersGenotypesSource(
				new InternalIndicesFilteredMarkersGenotypesSource(
						NetCdfMarkersGenotypesSource.createForMatrix(getOrigin(), getOriginNetCdfReadFile()),
						getSamplesKeysSource().getIndices()),
				getMarkersKeysSource().getIndices());
	}

	@Override
	protected ChromosomesKeysSource getChromosomesKeysSourceRaw() throws IOException {
		return NetCdfChromosomesKeysSource.createForOperation(getOrigin(), getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	protected ChromosomesInfosSource getChromosomesInfosSourceRaw() throws IOException {
		return NetCdfChromosomesInfosSource.createForOperation(getOrigin(), getOriginNetCdfReadFile(), getChromosomesKeysSource().getIndices());
	}
}
