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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCdfOperationDataSet<ET> implements OperationDataSet<ET> {

	private final Logger log = LoggerFactory.getLogger(AbstractNetCdfOperationDataSet.class);

	private final boolean markersOperationSet;
	private MatrixKey rdMatrixKey;
	private int numMarkers;
	private int numSamples;
	private int numChromosomes;
	private NetcdfFileWriteable wrNcFile;
	private OperationFactory operationFactory;
	private final Queue<ET> writeBuffer;
	private int alreadyWritten;
	private int entriesWriteBufferSize;

	public AbstractNetCdfOperationDataSet(boolean markersOperationSet, int entriesWriteBufferSize) {

		this.markersOperationSet = markersOperationSet;
		this.rdMatrixKey = null;
		this.numMarkers = -1;
		this.numSamples = -1;
		this.numChromosomes = -1;
		this.wrNcFile = null;
		this.operationFactory = null;
		this.writeBuffer = new LinkedList<ET>();
		this.alreadyWritten = 0;
		this.entriesWriteBufferSize = entriesWriteBufferSize;
	}

	public AbstractNetCdfOperationDataSet(boolean markersOperationSet) {
		this(markersOperationSet, 10);
	}

	protected int getEntriesWriteBufferSize() {
		return entriesWriteBufferSize;
	}

	public void setReadMatrixKey(MatrixKey rdMatrixKey) {
		this.rdMatrixKey = rdMatrixKey;
	}

	protected MatrixKey getReadMatrixKey() {
		return rdMatrixKey;
	}

	public void setNumMarkers(int numMarkers) {
		this.numMarkers = numMarkers;
	}

	protected int getNumMarkers() {
		return numMarkers;
	}

	public void setUseAllMarkers(MatrixKey matrixKey) {
		xxx; // TODO make a map with
	}

	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}

	protected int getNumSamples() {
		return numSamples;
	}

	public void setUseAllSamples(MatrixKey matrixKey) {
		xxx;
	}

	public void setNumChromosomes(int numChromosomes) {
		this.numChromosomes = numChromosomes;
	}

	protected int getNumChromosomes() {
		return numChromosomes;
	}

	public void setUseAllChromosomes(MatrixKey matrixKey) {
		xxx;
	}

	protected NetcdfFileWriteable getNetCdfWriteFile() {
		return wrNcFile;
	}

	protected NetcdfFileWriteable getNetCdfReadFile() {
		return wrNcFile;
	}

	protected OperationFactory getOperationFactory() {
		return operationFactory;
	}

	protected abstract OperationFactory createOperationFactory() throws IOException;

	protected void ensureNcFile() throws IOException {

		if (wrNcFile == null) {
			operationFactory = createOperationFactory();

			wrNcFile = operationFactory.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());
		}
	}

	public OperationKey getResultOperationKey() {
		return operationFactory.getResultOperationKey();
	}

	protected void write(NetcdfFileWriteable ncFile, String varName, int[] origin, Array values) throws IOException {

		try {
			ensureNcFile();
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
		if (markersOperationSet == markers) {
			varName = cNetCDF.Variables.VAR_OPSET_IDX;
		} else {
			varName = cNetCDF.Variables.VAR_IMPLICITSET_IDX;
		}

		return varName;
	}

	private String getNameVar(boolean markers) {

		final String varName;
		if (markersOperationSet == markers) {
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
		write(wrNcFile, varIdx, sampleIdxOrig, sampleIdxsD1);

		ArrayChar.D2 sampleKeysD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexSampleKeys.values(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
		final int[] sampleOrig = new int[] {0, 0};
		final String varName = getNameVar(false);
		write(wrNcFile, varName, sampleOrig, sampleKeysD2);
		log.info("Done writing SampleSet to matrix");
	}

	@Override
	public void setMarkers(Map<Integer, MarkerKey> matrixIndexMarkerKeys) throws IOException {

		ArrayInt.D1 markerIdxsD1 = NetCdfUtils.writeValuesToD1ArrayInt(matrixIndexMarkerKeys.keySet());
		final int[] markerIdxOrig = new int[] {0};
		final String varIdx = getIndexVar(true);
		write(wrNcFile, varIdx, markerIdxOrig, markerIdxsD1);

		ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexMarkerKeys.values(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		int[] markersOrig = new int[] {0, 0};
		final String varName = getNameVar(true);
		write(wrNcFile, varName, markersOrig, markersD2);
		log.info("Done writing MarkerSet to matrix");
	}

	@Override
	public void setChromosomes(Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys, Collection<ChromosomeInfo> chromosomeInfos) throws IOException {

		// Set of chromosomes found in matrix - index in the original set of chromosomes
		NetCdfUtils.saveIntMapD1ToWrMatrix(wrNcFile, matrixIndexChromosomeKeys.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX_IDX);
		// Set of chromosomes found in matrix - key of the chromosome
		NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, matrixIndexChromosomeKeys.values(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, cNetCDF.Strides.STRIDE_CHR);
		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, chromosomeInfos, columns, cNetCDF.Variables.VAR_CHR_INFO);
	}

	@Override
	public Map<Integer, SampleKey> getSamples() throws IOException {

		final String varIdx = getIndexVar(false);
		NetCdfUtils.readVariable(wrNcFile, varIdx, -1, -1, null, null);

		final String varName = getNameVar(false);
		NetCdfUtils.readVariable(wrNcFile, varName, -1, -1, null, null);

		return XXX;
	}

	@Override
	public Map<Integer, MarkerKey> getMarkers() throws IOException {

		final String varName = getNameVar(true);
		return XXX;
	}

	@Override
	public Map<Integer, ChromosomeKey> getChromosomes() throws IOException {
		return XXX;
	}

	public void addEntry(ET entry) throws IOException {

		writeBuffer.add(entry);

		if (writeBuffer.size() >= entriesWriteBufferSize) {
			writeEntries(alreadyWritten, writeBuffer);
			alreadyWritten += writeBuffer.size();
			writeBuffer.clear();
		}
	}

	protected abstract void writeEntries(int alreadyWritten, Queue<ET> writeBuffer) throws IOException;
}
