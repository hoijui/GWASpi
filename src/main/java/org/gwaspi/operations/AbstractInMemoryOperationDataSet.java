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

//	private NetcdfFileWriteable generateNetCdfHandler(
//			OperationMetadata operationMetadata)
//			throws IOException
//	{
//		final int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
//		final int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
//
//		NetcdfFileWriteable ncFile = createNetCdfFile(operationMetadata);
//
//		// global attributes
//		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, operationMetadata.getStudyId());
//		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, operationMetadata.getDescription());
//		final boolean useAllParentMarkers = getUseAllMarkersFromParent();
//		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_MARKERS, useAllParentMarkers ? 1 : 0);
//		final boolean useAllParentChromosomes = getUseAllChromosomesFromParent();
//		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_CHROMOSOMES, useAllParentChromosomes ? 1 : 0);
//		final boolean useAllParentSamples = getUseAllSamplesFromParent();
//		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_SAMPLES, useAllParentSamples ? 1 : 0);
//		// FIXME quite some more global attributes missing here (all of OperationMetadata)!
//
//		// dimensions
//		Dimension opSetDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_OPSET, operationMetadata.getOpSetSize());
//		Dimension implicitSetDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, operationMetadata.getImplicitSetSize());
//
//		final Dimension markersDim;
//		final Dimension samplesDim;
//		if (isMarkersOperationSet()) {
//			markersDim = opSetDim;
//			samplesDim = implicitSetDim;
//		} else {
//			markersDim = implicitSetDim;
//			samplesDim = opSetDim;
//		}
//		Dimension chromosomesDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, operationMetadata.getNumChromosomes());
//		Dimension markersNameDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
//		Dimension samplesNameDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
//		Dimension dim8 = ncFile.addDimension(cNetCDF.Dimensions.DIM_8, 8);
//
//		// MARKER SPACES
//		List<Dimension> markersSpace = new ArrayList<Dimension>(1);
//		markersSpace.add(markersDim);
//
//		List<Dimension> markersNameSpace = new ArrayList<Dimension>(2);
//		markersNameSpace.add(markersDim);
//		markersNameSpace.add(markersNameDim);
//
//		// CHROMOSOME SPACES
//		List<Dimension> chromosomesSpace = new ArrayList<Dimension>(1);
//		chromosomesSpace.add(chromosomesDim);
//
//		List<Dimension> chromosomesNameSpace = new ArrayList<Dimension>(2);
//		chromosomesNameSpace.add(chromosomesDim);
//		chromosomesNameSpace.add(dim8);
//
//		// SAMPLE SPACES
//		List<Dimension> samplesSpace = new ArrayList<Dimension>(1);
//		samplesSpace.add(samplesDim);
//
//		List<Dimension> samplesNameSpace = new ArrayList<Dimension>(2);
//		samplesNameSpace.add(samplesDim);
//		samplesNameSpace.add(samplesNameDim);
//
//		// Define Variables
//		final boolean opUseAll;
//		final List<Dimension> opSpace;
//		final List<Dimension> opNameSpace;
//		final boolean implUseAll;
//		final List<Dimension> implSpace;
//		final List<Dimension> implNameSpace;
//		if (isMarkersOperationSet()) {
//			opUseAll = useAllParentMarkers;
//			opSpace = markersSpace;
//			opNameSpace = markersNameSpace;
//			implUseAll = useAllParentSamples;
//			implSpace = samplesSpace;
//			implNameSpace = samplesNameSpace;
//		} else {
//			opUseAll = useAllParentSamples;
//			opSpace = samplesSpace;
//			opNameSpace = samplesNameSpace;
//			implUseAll = useAllParentMarkers;
//			implSpace = markersSpace;
//			implNameSpace = markersNameSpace;
//		}
//		if (!opUseAll) {
//			ncFile.addVariable(cNetCDF.Variables.VAR_OPSET_IDX, DataType.INT, opSpace);
//			ncFile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, opNameSpace);
//		}
//		if (!implUseAll) {
//			ncFile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET_IDX, DataType.INT, implSpace);
//			ncFile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, implNameSpace);
//		}
////		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace); // always get these from the parent matrix
//		if (!useAllParentChromosomes) {
//			ncFile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX, DataType.INT, chromosomesSpace);
//			ncFile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chromosomesNameSpace);
//		}
//
//		supplementNetCdfHandler(ncFile, operationMetadata, markersSpace, chromosomesSpace, samplesSpace);
//
////		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, operationMetadata.getOpSetSize()); // NOTE not required, as it can be read from th edimensions directly, which is also more reliable
//
//		return ncFile;
//	}

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
		return NetCdfSamplesKeysSource.createForOperation(getOrigin(), getOrigin().getStudyKey(), getNetCdfReadFile(), isMarkersOperationSet());
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
