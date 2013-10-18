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
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractNetCdfOperationDataSet implements OperationDataSet {

	private final Logger log = LoggerFactory.getLogger(AbstractNetCdfOperationDataSet.class);

	private final boolean markersOperationSet;
	private MatrixKey rdMatrixKey = null;
	private int numMarkers = -1;
	private int numSamples = -1;
	private int numChromosomes = -1;
	private NetcdfFileWriteable wrNcFile = null;
	private OperationFactory operationFactory = null;

	public AbstractNetCdfOperationDataSet(boolean markersOperationSet) {
		this.markersOperationSet = markersOperationSet;
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

	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}

	protected int getNumSamples() {
		return numSamples;
	}

	public void setNumChromosomes(int numChromosomes) {
		this.numChromosomes = numChromosomes;
	}

	protected int getNumChromosomes() {
		return numChromosomes;
	}

	protected NetcdfFileWriteable getNetCdfWriteFile() {
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

	public int getResultOperationId() {
		return operationFactory.getResultOPId();
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

	@Override
	public void setSamples(Map<Integer, SampleKey> matrixIndexSampleKeys) throws IOException {

		ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexSampleKeys.values(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
		int[] sampleOrig = new int[] {0, 0};
		String varName;
		if (markersOperationSet) {
			varName = cNetCDF.Variables.VAR_IMPLICITSET;
		} else {
			varName = cNetCDF.Variables.VAR_OPSET;
		}
		write(wrNcFile, varName, sampleOrig, samplesD2);
		log.info("Done writing SampleSet to matrix");
	}

	@Override
	public void setMarkers(Map<Integer, MarkerKey> matrixIndexMarkerKeys) throws IOException {

		ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(matrixIndexMarkerKeys.values(), cNetCDF.Strides.STRIDE_MARKER_NAME);
		int[] markersOrig = new int[] {0, 0};
		String varName;
		if (markersOperationSet) {
			varName = cNetCDF.Variables.VAR_OPSET;
		} else {
			varName = cNetCDF.Variables.VAR_IMPLICITSET;
		}
		write(wrNcFile, varName, markersOrig, markersD2);
		log.info("Done writing MarkerSet to matrix");
	}

	@Override
	public void setChromosomes(Map<Integer, ChromosomeKey> matrixIndexChromosomeKeys, Collection<ChromosomeInfo> chromosomeInfos) throws IOException {

		// Set of chromosomes found in matrix along with number of markersinfo
		NetCdfUtils.saveObjectsToStringToMatrix(wrNcFile, matrixIndexChromosomeKeys.values(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
		// Number of marker per chromosome & max pos for each chromosome
		int[] columns = new int[] {0, 1, 2, 3};
		NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrNcFile, chromosomeInfos, columns, cNetCDF.Variables.VAR_CHR_INFO);
	}

	@Override
	public Map<Integer, SampleKey> getSamples() throws IOException {

	}

	@Override
	public Map<Integer, MarkerKey> getMarkers() throws IOException {

	}

	@Override
	public Map<Integer, ChromosomeKey> getChromosomes() throws IOException {

	}
}
