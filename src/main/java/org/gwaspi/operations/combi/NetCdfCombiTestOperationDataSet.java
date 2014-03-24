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

package org.gwaspi.operations.combi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.operations.NetCdfUtils;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfCombiTestOperationDataSet extends AbstractNetCdfOperationDataSet<CombiTestOperationEntry> implements CombiTestOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - VAR_OP_MARKERS_WEIGHT: SVN weight [Double]

	/**
	 * Combi test result weights (importance of the individual markers)
	 */
	private static final String VAR_OP_MARKERS_WEIGHT = "OP_markers_weight";

//	private String hardyWeinbergName;
//	private OperationKey markerCensusOperationKey;
	private ArrayDouble.D1 netCdfWeights;

	public NetCdfCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent, OperationKey operationKey) {
		super(true, origin, parent, operationKey);

//		this.hardyWeinbergName = null;
//		this.markerCensusOperationKey = null;
	}

	public NetCdfCombiTestOperationDataSet(MatrixKey origin, DataSetKey parent) {
		this(origin, parent, null);
	}

	@Override
	protected void supplementNetCdfHandler(
			NetcdfFileWriteable ncFile,
			OperationMetadata operationMetadata,
			List<Dimension> markersSpace,
			List<Dimension> chromosomesSpace,
			List<Dimension> samplesSpace)
			throws IOException
	{
		ncFile.addVariable(VAR_OP_MARKERS_WEIGHT, DataType.DOUBLE, markersSpace);
	}

	@Override
	protected OperationMetadata createOperationMetadata() throws IOException {

		DataSetKey parentDataSetKey = getParent();
		DataSetMetadata parentDataSetMetadata = MatricesList.getDataSetMetadata(parentDataSetKey);
		return new OperationMetadata(
				parentDataSetKey, // parent data set
				"COMBI_Test"/* + myFriendlyName*/, // friendly name
				"COMBI test on " + parentDataSetMetadata.getFriendlyName(), // description
				OPType.COMBI_ASSOC_TEST, // operationType
				getNumMarkers(),
				getNumSamples(),
				getNumChromosomes(),
				isMarkersOperationSet());
	}

//	@Override
//	public void setHardyWeinbergName(String hardyWeinbergName) {
//		this.hardyWeinbergName = hardyWeinbergName;
//	}

//	@Override
//	public void setMarkerCensusOperationKey(OperationKey markerCensusOperationKey) {
//		this.markerCensusOperationKey = markerCensusOperationKey;
//	}

	@Override
	public void setWeights(List<Double> weights) throws IOException {

		ArrayDouble.D1 netCdfWeightsLocal;
		int[] origin = new int[] {0};
		netCdfWeightsLocal = new ArrayDouble.D1(weights.size());

		int index = 0;
		for (Double weight : weights) {
			netCdfWeightsLocal.setDouble(netCdfWeightsLocal.getIndex().set(index), weight);
			index++;
		}
		try {
			getNetCdfWriteFile().write(VAR_OP_MARKERS_WEIGHT, origin, netCdfWeightsLocal);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<CombiTestOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfWeights == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfWeights = new ArrayDouble.D1(writeBuffer.size());
		} else if (writeBuffer.size() < netCdfWeights.getShape()[0]) {
			// we end up here at the end of the processing, if, for example,
			// we have a buffer size of 10, but only 7 items are left to be written
			List<Range> reducedRange1D = new ArrayList<Range>(1);
			reducedRange1D.add(new Range(writeBuffer.size()));
			try {
				netCdfWeights = (ArrayDouble.D1) netCdfWeights.sectionNoReduce(reducedRange1D);
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			}
		}
		int index = 0;
		for (CombiTestOperationEntry entry : writeBuffer) {
			netCdfWeights.setDouble(netCdfWeights.getIndex().set(index), entry.getWeight());
			index++;
		}
		try {
			getNetCdfWriteFile().write(VAR_OP_MARKERS_WEIGHT, origin, netCdfWeights);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public List<Double> getWeights() throws IOException {
		return getWeights(-1, -1);
	}

	@Override
	public List<Double> getWeights(int from, int to) throws IOException {

		List<Double> values = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), VAR_OP_MARKERS_WEIGHT, from, to, values, null);

		return values;
	}

	public List<CombiTestOperationEntry> getEntries(int from, int to) throws IOException {

		Map<Integer, MarkerKey> markersKeys = getMarkersKeysSource().getIndicesMap(from, to);
		List<Double> weights = getWeights(from, to);

		List<CombiTestOperationEntry> entries
				= new ArrayList<CombiTestOperationEntry>(markersKeys.size());
		Iterator<Double> weightsIt = weights.iterator();
		for (Map.Entry<Integer, MarkerKey> origIndicesAndKey : markersKeys.entrySet()) {
			entries.add(new DefaultCombiTestOperationEntry(
					origIndicesAndKey.getValue(),
					origIndicesAndKey.getKey(),
					weightsIt.next()));
		}

		return entries;
	}
}
