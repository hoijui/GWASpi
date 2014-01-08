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
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.markers.NetCdfChromosomesInfosSource;
import org.gwaspi.netCDF.markers.NetCdfChromosomesKeysSource;
import org.gwaspi.netCDF.markers.NetCdfMarkersKeysSource;
import org.gwaspi.netCDF.markers.NetCdfMarkersMetadataSource;
import org.gwaspi.netCDF.markers.NetCdfSamplesInfosSource;
import org.gwaspi.netCDF.markers.NetCdfSamplesKeysSource;
import org.gwaspi.netCDF.operations.NetCdfUtils;
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

public abstract class AbstractNetCdfOperationDataSet<ET> extends AbstractOperationDataSet<ET> {

	private final Logger log = LoggerFactory.getLogger(AbstractNetCdfOperationDataSet.class);

	private Integer numMarkers;
	private Integer numSamples;
	private Integer numChromosomes;
	private Boolean useAllMarkersFromParent;
	private Boolean useAllSamplesFromParent;
	private Boolean useAllChromosomesFromParent;
	private NetcdfFile readNcFile;
	private NetcdfFileWriteable writeNcFile;

	public AbstractNetCdfOperationDataSet(
			boolean markersOperationSet,
			OperationKey operationKey,
			int entriesWriteBufferSize)
	{
		super(markersOperationSet, operationKey, entriesWriteBufferSize);

		this.numMarkers = null;
		this.numSamples = null;
		this.numChromosomes = null;
		this.useAllMarkersFromParent = null;
		this.useAllSamplesFromParent = null;
		this.useAllChromosomesFromParent = null;
		this.readNcFile = null;
		this.writeNcFile = null;
	}

	public AbstractNetCdfOperationDataSet(boolean markersOperationSet, OperationKey operationKey) {
		this(markersOperationSet, operationKey, 10);
	}

	public AbstractNetCdfOperationDataSet(boolean markersOperationSet) {
		this(markersOperationSet, null);
	}

	@Override
	public void setNumMarkers(int numMarkers) {
		this.numMarkers = numMarkers;
	}

	@Override
	protected int getNumMarkersRaw() throws IOException {

		if (numMarkers == null) {
			numMarkers = getNetCdfReadFile().findDimension(getIndexVar(true)).getLength();
		}

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
	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}

	@Override
	protected int getNumSamplesRaw() throws IOException {

		if (numSamples == null) {
			numSamples = getNetCdfReadFile().findDimension(getIndexVar(false)).getLength();
		}

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
	public void setNumChromosomes(int numChromosomes) {
		this.numChromosomes = numChromosomes;
	}

	@Override
	protected int getNumChromosomesRaw() throws IOException {

		if (numChromosomes == null) {
			numChromosomes = getNetCdfReadFile().findDimension(cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX).getLength();
		}

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

	protected NetcdfFileWriteable getNetCdfWriteFile() throws IOException {

		ensureWriteNcFile();

		return writeNcFile;
	}

	@Override
	public void finnishWriting() throws IOException {

		if (writeNcFile != null) {
			writeNcFile.close();
			writeNcFile = null;
		}
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
		final int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;
		final int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

		NetcdfFileWriteable ncFile = createNetCdfFile(operationMetadata);

		// global attributes
		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, operationMetadata.getStudyId());
		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, operationMetadata.getDescription());
		final boolean useAllParentMarkers = getUseAllMarkersFromParent();
		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_MARKERS, useAllParentMarkers ? 1 : 0);
		final boolean useAllParentChromosomes = getUseAllChromosomesFromParent();
		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_CHROMOSOMES, useAllParentChromosomes ? 1 : 0);
		final boolean useAllParentSamples = getUseAllSamplesFromParent();
		ncFile.addGlobalAttribute(cNetCDF.Attributes.GLOB_USE_ALL_SAMPLES, useAllParentSamples ? 1 : 0);
		// FIXME quite some more global attributes missing here (all of OperationMetadata)!

		// dimensions
		Dimension opSetDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_OPSET, operationMetadata.getOpSetSize());
		Dimension implicitSetDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, operationMetadata.getImplicitSetSize());

		final Dimension markersDim;
		final Dimension samplesDim;
		if (isMarkersOperationSet()) {
			markersDim = opSetDim;
			samplesDim = implicitSetDim;
		} else {
			markersDim = implicitSetDim;
			samplesDim = opSetDim;
		}
		Dimension chromosomesDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_CHRSET, operationMetadata.getNumChromosomes());
		Dimension markersNameDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);
		Dimension samplesNameDim = ncFile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
		Dimension dim8 = ncFile.addDimension(cNetCDF.Dimensions.DIM_8, 8);

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
			ncFile.addVariable(cNetCDF.Variables.VAR_OPSET_IDX, DataType.INT, opSpace);
			ncFile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, opNameSpace);
		}
		if (!implUseAll) {
			ncFile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET_IDX, DataType.INT, implSpace);
			ncFile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, implNameSpace);
		}
//		ncfile.addVariable(cNetCDF.Variables.VAR_MARKERS_RSID, DataType.CHAR, markerNameSpace); // always get these from the parent matrix
		if (!useAllParentChromosomes) {
			ncFile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX, DataType.INT, chromosomesSpace);
			ncFile.addVariable(cNetCDF.Variables.VAR_CHR_IN_MATRIX, DataType.CHAR, chromosomesNameSpace);
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
//			operationFactory = createOperationFactory();
//			wrNcFile = operationFactory.getNetCDFHandler();
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
		} finally {
			if (null != ncFile) {
				try {
					ncFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file " + ncFile, ex);
				}
			}
		}
	}

	private String getIndexVar(boolean markers) {

		final String varName;
		if (isMarkersOperationSet() == markers) {
			varName = cNetCDF.Variables.VAR_OPSET_IDX;
		} else {
			varName = cNetCDF.Variables.VAR_IMPLICITSET_IDX;
		}

		return varName;
	}

	private String getNameVar(boolean markers) {

		final String varName;
		if (isMarkersOperationSet() == markers) {
			varName = cNetCDF.Variables.VAR_OPSET;
		} else {
			varName = cNetCDF.Variables.VAR_IMPLICITSET;
		}

		return varName;
	}

	@Override
	public void setSamples(Map<Integer, SampleKey> matrixIndexSampleKeys) throws IOException {

		ArrayInt.D1 sampleIdxsD1 = NetCdfUtils.writeValuesToD1ArrayInt(matrixIndexSampleKeys.keySet());
		final int[] sampleIdxOrig = new int[] {0};
		final String varIdx = getIndexVar(false);
		write(getNetCdfWriteFile(), varIdx, sampleIdxOrig, sampleIdxsD1);

		ArrayChar.D2 sampleKeysD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexSampleKeys.values(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
		final int[] sampleOrig = new int[] {0, 0};
		final String varName = getNameVar(false);
		write(getNetCdfWriteFile(), varName, sampleOrig, sampleKeysD2);
		log.info("Done writing SampleSet to matrix");
	}

	@Override
	public void setMarkers(Map<Integer, MarkerKey> matrixIndexMarkerKeys) throws IOException {

		ArrayInt.D1 markerIdxsD1 = NetCdfUtils.writeValuesToD1ArrayInt(matrixIndexMarkerKeys.keySet());
		final int[] markerIdxOrig = new int[] {0};
		final String varIdx = getIndexVar(true);
		write(getNetCdfWriteFile(), varIdx, markerIdxOrig, markerIdxsD1);

		ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexMarkerKeys.values(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		int[] markersOrig = new int[] {0, 0};
		final String varName = getNameVar(true);
		write(getNetCdfWriteFile(), varName, markersOrig, markersD2);
		log.info("Done writing MarkerSet to matrix");
	}

	@Override
	public void setChromosomes(Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys/*, Collection<ChromosomeInfo> chromosomeInfos*/) throws IOException {

		// Set of chromosomes found in matrix - index in the original set of chromosomes
		NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), matrixIndexChromosomeKeys.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX);
		// Set of chromosomes found in matrix - key of the chromosome
		NetCdfUtils.saveObjectsToStringToMatrix(getNetCdfWriteFile(), matrixIndexChromosomeKeys.values(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);
//		// Number of marker per chromosome & max pos for each chromosome
//		int[] columns = new int[] {0, 1, 2, 3};
//		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(getNetCdfWriteFile(), chromosomeInfos, columns, cNetCDF.Variables.VAR_CHR_INFO);
	}

	@Override
	public SamplesKeysSource getSamplesKeysSourceRaw() throws IOException {
		return NetCdfSamplesKeysSource.createForOperation(getReadMatrixKey().getStudyKey(), getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	public SamplesInfosSource getSamplesInfosSource() throws IOException {
		return NetCdfSamplesInfosSource.createForOperation(getReadMatrixKey().getStudyKey(), getNetCdfReadFile(), getSamplesKeysSourceRaw().getIndices());
	}

	@Override
	public MarkersKeysSource getMarkersKeysSourceRaw() throws IOException {
		return NetCdfMarkersKeysSource.createForOperation(getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	public MarkersMetadataSource getMarkersMetadatasSource() throws IOException {
		return NetCdfMarkersMetadataSource.createForOperation(getNetCdfReadFile(), getMarkersKeysSourceRaw().getIndices());
	}

	@Override
	public ChromosomesKeysSource getChromosomesKeysSourceRaw() throws IOException {
		return NetCdfChromosomesKeysSource.createForOperation(getNetCdfReadFile(), isMarkersOperationSet());
	}

	@Override
	public ChromosomesInfosSource getChromosomesInfosSource() throws IOException {
		return NetCdfChromosomesInfosSource.createForOperation(getNetCdfReadFile(), getChromosomesKeysSourceRaw().getIndices());
	}
}
