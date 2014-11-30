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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.NetCDFConstants;
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
import org.gwaspi.model.OperationMetadata;
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
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCdfOperationDataSet<ET extends OperationDataEntry>
		extends AbstractOperationDataSet<ET>
{
	private final Logger log = LoggerFactory.getLogger(AbstractNetCdfOperationDataSet.class);

//	private Integer numMarkers;
//	private Integer numSamples;
//	private Integer numChromosomes;
	private Boolean useAllMarkersFromParent;
	private Boolean useAllSamplesFromParent;
	private Boolean useAllChromosomesFromParent;
	private NetcdfFile originReadNcFile;
	private NetcdfFile readNcFile;
	private NetcdfFileWriteable writeNcFile;

	public AbstractNetCdfOperationDataSet(
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey,
			int entriesWriteBufferSize)
	{
		super(origin, parent, operationKey, entriesWriteBufferSize);

//		this.numMarkers = null;
//		this.numSamples = null;
//		this.numChromosomes = null;
		this.useAllMarkersFromParent = null;
		this.useAllSamplesFromParent = null;
		this.useAllChromosomesFromParent = null;
		this.originReadNcFile = null;
		this.readNcFile = null;
		this.writeNcFile = null;
	}

	public AbstractNetCdfOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey)
	{
		this(origin, parent, operationKey, getDefaultEntriesWriteBufferSize(markersOperationSet));
	}

	public AbstractNetCdfOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent)
	{
		this(markersOperationSet, origin, parent, null);
	}

	@Override
	protected int getNumMarkersRaw() throws IOException {
//		return getNetCdfReadFile().findVariable(getIndexVar(true)).getShape(0);
		return getNetCdfReadFile().findDimension(getDimensionName(true)).getLength();
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
			useAllMarkersFromParent = (getNetCdfReadFile().findGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_MARKERS).getNumericValue().intValue() != 0);
		}

		return useAllMarkersFromParent;
	}

	@Override
	protected int getNumSamplesRaw() throws IOException {
//		return getNetCdfReadFile().findVariable(getIndexVar(false)).getShape(0);
		return getNetCdfReadFile().findDimension(getDimensionName(false)).getLength();
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
			useAllSamplesFromParent = (getNetCdfReadFile().findGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_SAMPLES).getNumericValue().intValue() != 0);
		}

		return useAllSamplesFromParent;
	}

	@Override
	protected int getNumChromosomesRaw() throws IOException {
//		return getNetCdfReadFile().findVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX).getShape(0);
		return getNetCdfReadFile().findDimension(NetCDFConstants.Dimensions.DIM_CHRSET).getLength();
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
			useAllChromosomesFromParent = (getNetCdfReadFile().findGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_CHROMOSOMES).getNumericValue().intValue() != 0);
		}

		return useAllChromosomesFromParent;
	}

	protected NetcdfFileWriteable getNetCdfWriteFile() throws IOException {

		ensureWriteNcFile();

		return writeNcFile;
	}

	@Override
	public void finnishWriting() throws IOException {
		super.finnishWriting();

		if (writeNcFile == null) {
			ensureWriteNcFile();
		}

		writeNcFile.close();
		writeNcFile = null;
	}

	protected NetcdfFile getOriginNetCdfReadFile() throws IOException {

		if (originReadNcFile == null) {
			MatrixMetadata originMatrixMetadata = MatricesList.getMatrixMetadataById(getOrigin());

			File readFile = MatrixMetadata.generatePathToNetCdfFile(originMatrixMetadata);
			originReadNcFile = NetcdfFile.open(readFile.getAbsolutePath());
		}

		return originReadNcFile;
	}

	protected NetcdfFile getNetCdfReadFile() throws IOException {

		ensureReadNcFile();

		return readNcFile;
	}

	private NetcdfFileWriteable createNetCdfFile(
			OperationMetadata operationMetadata)
			throws IOException
	{
		File writeFile = OperationMetadata.generatePathToNetCdfFile(operationMetadata);
		File parentFolder = writeFile.getParentFile();
		if (!parentFolder.exists()) {
			org.gwaspi.global.Utils.createFolder(parentFolder);
		}
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		return ncfile;
	}

	protected abstract void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException;

	private NetcdfFileWriteable generateNetCdfHandler(
			OperationMetadata operationMetadata)
			throws IOException
	{
		final int markerStride = NetCDFConstants.Strides.STRIDE_MARKER_NAME;
		final int sampleStride = NetCDFConstants.Strides.STRIDE_SAMPLE_NAME;

		NetcdfFileWriteable ncFile = createNetCdfFile(operationMetadata);

		// global attributes
		ncFile.addGlobalAttribute(NetCDFConstants.Attributes.GLOB_STUDY, operationMetadata.getStudyId());
		ncFile.addGlobalAttribute(NetCDFConstants.Attributes.GLOB_DESCRIPTION, operationMetadata.getDescription());
		final boolean useAllParentMarkers = getUseAllMarkersFromParent();
		ncFile.addGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_MARKERS, useAllParentMarkers ? 1 : 0);
		final boolean useAllParentChromosomes = getUseAllChromosomesFromParent();
		ncFile.addGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_CHROMOSOMES, useAllParentChromosomes ? 1 : 0);
		final boolean useAllParentSamples = getUseAllSamplesFromParent();
		ncFile.addGlobalAttribute(NetCDFConstants.Attributes.GLOB_USE_ALL_SAMPLES, useAllParentSamples ? 1 : 0);
		// FIXME quite some more global attributes missing here (all of OperationMetadata)!

		// dimensions
		Dimension opSetDim = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_OPSET, operationMetadata.getOpSetSize());
		Dimension implicitSetDim = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_IMPLICITSET, operationMetadata.getImplicitSetSize());

		final Dimension markersDim;
		final Dimension samplesDim;
		if (isMarkersOperationSet()) {
			markersDim = opSetDim;
			samplesDim = implicitSetDim;
		} else {
			markersDim = implicitSetDim;
			samplesDim = opSetDim;
		}
		Dimension chromosomesDim = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_CHRSET, operationMetadata.getNumChromosomes());
		Dimension markersNameDim = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension samplesNameDim = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
		Dimension dim8 = ncFile.addDimension(NetCDFConstants.Dimensions.DIM_8, 8);

		// MARKER SPACES
		List<Dimension> markersSpace = new ArrayList<Dimension>(1);
		markersSpace.add(markersDim);

		List<Dimension> markersNameSpace = new ArrayList<Dimension>(2);
		markersNameSpace.add(markersDim);
		markersNameSpace.add(markersNameDim);

		// CHROMOSOME SPACES
		List<Dimension> chromosomesSpace = new ArrayList<Dimension>(1);
		chromosomesSpace.add(chromosomesDim);

		List<Dimension> chromosomesNameSpace = new ArrayList<Dimension>(2);
		chromosomesNameSpace.add(chromosomesDim);
		chromosomesNameSpace.add(dim8);

		// SAMPLE SPACES
		List<Dimension> samplesSpace = new ArrayList<Dimension>(1);
		samplesSpace.add(samplesDim);

		List<Dimension> samplesNameSpace = new ArrayList<Dimension>(2);
		samplesNameSpace.add(samplesDim);
		samplesNameSpace.add(samplesNameDim);

		// Define Variables
		final boolean opUseAll;
		final List<Dimension> opSpace;
		final List<Dimension> opNameSpace;
		final boolean implUseAll;
		final List<Dimension> implSpace;
		final List<Dimension> implNameSpace;
		if (isMarkersOperationSet()) {
			opUseAll = useAllParentMarkers;
			opSpace = markersSpace;
			opNameSpace = markersNameSpace;
			implUseAll = useAllParentSamples;
			implSpace = samplesSpace;
			implNameSpace = samplesNameSpace;
		} else {
			opUseAll = useAllParentSamples;
			opSpace = samplesSpace;
			opNameSpace = samplesNameSpace;
			implUseAll = useAllParentMarkers;
			implSpace = markersSpace;
			implNameSpace = markersNameSpace;
		}
		if (!opUseAll) {
			ncFile.addVariable(NetCDFConstants.Variables.VAR_OPSET_IDX, DataType.INT, opSpace);
			ncFile.addVariable(NetCDFConstants.Variables.VAR_OPSET, DataType.CHAR, opNameSpace);
		}
		if (!implUseAll) {
			ncFile.addVariable(NetCDFConstants.Variables.VAR_IMPLICITSET_IDX, DataType.INT, implSpace);
			ncFile.addVariable(NetCDFConstants.Variables.VAR_IMPLICITSET, DataType.CHAR, implNameSpace);
		}
//		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace); // always get these from the parent matrix
		if (!useAllParentChromosomes) {
			ncFile.addVariable(NetCDFConstants.Variables.VAR_CHR_IN_MATRIX_IDX, DataType.INT, chromosomesSpace);
			ncFile.addVariable(NetCDFConstants.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chromosomesNameSpace);
		}

		supplementNetCdfHandler(ncFile, operationMetadata, markersSpace, chromosomesSpace, samplesSpace);

//		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, operationMetadata.getOpSetSize()); // NOTE not required, as it can be read from th edimensions directly, which is also more reliable

		return ncFile;
	}

	private NetcdfFileWriteable ensureWriteNcFile() throws IOException {

		if (writeNcFile == null) {
			OperationMetadata operationMetadata = getOperationMetadata();

//			resultOperationKey = OperationsList.insertOPMetadata(new OperationMetadata(
//					rdMatrixKey,
//					rdOperationKey,
//					friendlyName,
//					description,
//					opType
//					));
//
//			opMetaData = OperationsList.getOperation(resultOperationKey);
//
//			OperationManager = createOperationManager();
//			wrNcFile = OperationManager.getNetCDFHandler();
			writeNcFile = generateNetCdfHandler(operationMetadata);
			writeNcFile.create();
			log.trace("Done creating netCDF handle: " + writeNcFile.toString());
		}

//		File writeFile = OperationMetadata.generatePathToNetCdfFile(opMetaData);
//		File containingFolder = writeFile.getParentFile();
//		if (!containingFolder.exists()) {
//			org.gwaspi.global.Utils.createFolder(containingFolder);
//		}
//
//		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		return writeNcFile;
	}

	private NetcdfFile ensureReadNcFile() throws IOException {

		if (readNcFile == null) {
			OperationMetadata operationMetadata = getOperationMetadata();

			File readFile = OperationMetadata.generatePathToNetCdfFile(operationMetadata);
			readNcFile = NetcdfFile.open(readFile.getAbsolutePath());
		}

//		File writeFile = OperationMetadata.generatePathToNetCdfFile(opMetaData);
//		File containingFolder = writeFile.getParentFile();
//		if (!containingFolder.exists()) {
//			org.gwaspi.global.Utils.createFolder(containingFolder);
//		}
//
//		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		return readNcFile;
	}

	protected void write(NetcdfFileWriteable ncFile, String varName, int[] origin, Array values) throws IOException {

		try {
//			ensureNcFile();
			ncFile.write(varName, origin, values);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	private String getIndexVar(boolean markers) {

		final String varName;
		if (isMarkersOperationSet() == markers) {
			varName = NetCDFConstants.Variables.VAR_OPSET_IDX;
		} else {
			varName = NetCDFConstants.Variables.VAR_IMPLICITSET_IDX;
		}

		return varName;
	}

	private String getNameVar(boolean markers) {

		final String varName;
		if (isMarkersOperationSet() == markers) {
			varName = NetCDFConstants.Variables.VAR_OPSET;
		} else {
			varName = NetCDFConstants.Variables.VAR_IMPLICITSET;
		}

		return varName;
	}

	private String getDimensionName(boolean markers) {

		final String dimensionName;
		if (isMarkersOperationSet() == markers) {
			dimensionName = NetCDFConstants.Dimensions.DIM_OPSET;
		} else {
			dimensionName = NetCDFConstants.Dimensions.DIM_IMPLICITSET;
		}

		return dimensionName;
	}

	@Override
	public void setSamples(List<Integer> originalIndices, List<SampleKey> keys) throws IOException {

		if (!getUseAllSamplesFromParent()) {
			ArrayInt.D1 sampleIdxsD1 = NetCdfUtils.writeValuesToD1ArrayInt(originalIndices);
			final int[] sampleIdxOrig = new int[] {0};
			final String varIdx = getIndexVar(false);
			write(getNetCdfWriteFile(), varIdx, sampleIdxOrig, sampleIdxsD1);

			ArrayChar.D2 sampleKeysD2 = NetCdfUtils.writeCollectionToD2ArrayChar(keys, NetCDFConstants.Strides.STRIDE_SAMPLE_NAME);
			final int[] sampleOrig = new int[] {0, 0};
			final String varName = getNameVar(false);
			write(getNetCdfWriteFile(), varName, sampleOrig, sampleKeysD2);
			log.info("Done writing SampleSet to matrix");
		}
	}

	@Override
	public void setMarkers(List<Integer> originalIndices, List<MarkerKey> keys) throws IOException {

		if (!getUseAllMarkersFromParent()) {
			ArrayInt.D1 markerIdxsD1 = NetCdfUtils.writeValuesToD1ArrayInt(originalIndices);
			final int[] markerIdxOrig = new int[] {0};
			final String varIdx = getIndexVar(true);
			write(getNetCdfWriteFile(), varIdx, markerIdxOrig, markerIdxsD1);

			ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(keys, NetCDFConstants.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[] {0, 0};
			final String varName = getNameVar(true);
			write(getNetCdfWriteFile(), varName, markersOrig, markersD2);
			log.info("Done writing MarkerSet to matrix");
		}
	}

	@Override
	public void setChromosomes(List<Integer> originalIndices, List<ChromosomeKey> keys) throws IOException {

		if (!getUseAllChromosomesFromParent()) {
			// Set of chromosomes found in matrix - index in the original set of chromosomes
			NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), originalIndices, NetCDFConstants.Variables.VAR_CHR_IN_MATRIX_IDX);
			// Set of chromosomes found in matrix - key of the chromosome
			NetCdfUtils.saveObjectsToStringToMatrix(getNetCdfWriteFile(), keys, NetCDFConstants.Variables.VAR_CHR_IN_MATRIX, NetCDFConstants.Strides.STRIDE_CHR);
//			// Number of marker per chromosome & max pos for each chromosome
//			int[] columns = new int[] {0, 1, 2, 3};
//			NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(getNetCdfWriteFile(), chromosomeInfos, columns, cNetCDF.Variables.VAR_CHR_INFO);
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
