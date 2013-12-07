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

package org.gwaspi.operations.qasamples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.netCDF.operations.SampleOperationSet;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class NetCdfQASamplesOperationDataSet extends AbstractNetCdfOperationDataSet<QASamplesOperationEntry> implements QASamplesOperationDataSet {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getId()) marker keys
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT: (double) missing ratio for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT: (int) missing count for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT: (double) heterozygosity ratio for each sample

	private ArrayDouble.D1 netCdfMissingRatios;
	private ArrayInt.D1 netCdfMissingCounts;
	private ArrayDouble.D1 netCdfHetzyRatios;

	public NetCdfQASamplesOperationDataSet(OperationKey operationKey) {
		super(false, operationKey);
	}

	public NetCdfQASamplesOperationDataSet() {
		this(null);
	}

	@Override
	public NetcdfFileWriteable generateNetCdfHandler(
			StudyKey studyKey,
			String resultOPName,
			String description,
			OPType opType,
			int markerSetSize,
			int sampleSetSize,
			int chrSetSize)
			throws IOException
	{
		File pathToStudy = new File(Study.constructGTPath(studyKey));
		if (!pathToStudy.exists()) {
			org.gwaspi.global.Utils.createFolder(pathToStudy);
		}

		File writeFile = new File(pathToStudy, resultOPName + ".nc");
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFile.getAbsolutePath(), false);

		// global attributes
		int sampleStride = cNetCDF.Strides.STRIDE_SAMPLE_NAME;
		int markerStride = cNetCDF.Strides.STRIDE_MARKER_NAME;

		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyKey.getId());
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, description);

		// dimensions
		Dimension sampleSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_OPSET, sampleSetSize);
		Dimension implicitSetDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_IMPLICITSET, markerSetSize);
		Dimension sampleStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_SAMPLESTRIDE, sampleStride);
		Dimension markerStrideDim = ncfile.addDimension(cNetCDF.Dimensions.DIM_MARKERSTRIDE, markerStride);

		// OP SPACES
		List<Dimension> OP1Space = new ArrayList<Dimension>();
		OP1Space.add(sampleSetDim);

		// SAMPLE SPACES
		List<Dimension> sampleSetSpace = new ArrayList<Dimension>();
		sampleSetSpace.add(sampleSetDim);
		sampleSetSpace.add(sampleStrideDim);

		// MARKER SPACES
		List<Dimension> markerSetSpace = new ArrayList<Dimension>();
		markerSetSpace.add(implicitSetDim);
		markerSetSpace.add(markerStrideDim);

		// Define OP Variables
		ncfile.addVariable(cNetCDF.Variables.VAR_OPSET, DataType.CHAR, sampleSetSpace);
		ncfile.addVariable(cNetCDF.Variables.VAR_IMPLICITSET, DataType.CHAR, markerSetSpace);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT, DataType.DOUBLE, OP1Space);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT, DataType.INT, OP1Space);
		ncfile.addVariable(cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT, DataType.DOUBLE, OP1Space);
		ncfile.addVariableAttribute(cNetCDF.Variables.VAR_OPSET, cNetCDF.Attributes.LENGTH, sampleSetSize);

		return ncfile;
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

		return new OperationFactory(
				rdMatrixMetadata.getStudyKey(),
				"Sample QA", // friendly name
				"Sample census on " + rdMatrixMetadata.getMatrixFriendlyName()
						+ "\nSamples: " + getNumSamples(), // description
				getNumSamples(),
				getNumMarkers(),
				0,
				OPType.SAMPLE_QA,
				getReadMatrixKey(), // Parent matrixId
				-1); // Parent operationId
	}

	@Override
	public void setMissingRatios(Collection<Double> sampleMissingRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), sampleMissingRatios, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
	}

	@Override
	public void setMissingCounts(Collection<Integer> sampleMissingCount) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), sampleMissingCount, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);
	}

	@Override
	public void setHetzyRatios(Collection<Double> sampleHetzyRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), sampleHetzyRatios, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
	}

	@Override
	public Collection<Double> getMissingRatios(int from, int to) throws IOException {

		Collection<Double> missingRatios = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT, from, to, missingRatios, null);
//		// the old way:
//		OperationMetadata rdOPMetadata = OperationsList.getOperation(getResultOperationKey());
//		SampleOperationSet rdInfoSampleSet = new SampleOperationSet(getResultOperationKey(), from, to);
//		Map<SampleKey, Double> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();
//		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
//		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
//		ncFile.close();

		return missingRatios;
	}

	@Override
	public Collection<Integer> getMissingCounts(int from, int to) throws IOException {

		Collection<Integer> missingCount = new ArrayList<Integer>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT, from, to, missingCount, null);

		return missingCount;
	}

	@Override
	public Collection<Double> getHetzyRatios(int from, int to) throws IOException {

		Collection<Double> hetzyRatios = new ArrayList<Double>(0);
		NetCdfUtils.readVariable(getNetCdfReadFile(), cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT, from, to, hetzyRatios, null);

		return hetzyRatios;
	}

	@Override
	public Collection<QASamplesOperationEntry> getEntries(int from, int to) throws IOException {

		SampleOperationSet rdSampleSet = new SampleOperationSet(getOperationKey(), from, to);
		Map<SampleKey, Integer> rdSamples = rdSampleSet.getOpSetMap();

		Collection<Double> missingRatios = getMissingRatios(from, to);
		Collection<Integer> missingCount = getMissingCounts(from, to);
		Collection<Double> hetzyRatios = getHetzyRatios(from, to);

		Collection<QASamplesOperationEntry> entries
				= new ArrayList<QASamplesOperationEntry>(missingRatios.size());
		Iterator<Double> missingRatioIt = missingRatios.iterator();
		Iterator<Integer> missingCountIt = missingCount.iterator();
		Iterator<Double> hetzyRatiosIt = hetzyRatios.iterator();
		for (Map.Entry<SampleKey, Integer> sampleKeyIndex : rdSamples.entrySet()) {
			entries.add(new DefaultQASamplesOperationEntry(
					sampleKeyIndex.getKey(),
					sampleKeyIndex.getValue(),
					missingRatioIt.next(),
					missingCountIt.next(),
					hetzyRatiosIt.next()));
		}

		return entries;
	}

	@Override
	protected void writeEntries(int alreadyWritten, Queue<QASamplesOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten};
		if (netCdfMissingRatios == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfMissingRatios = new ArrayDouble.D1(writeBuffer.size());
			netCdfMissingCounts = new ArrayInt.D1(writeBuffer.size());
			netCdfHetzyRatios = new ArrayDouble.D1(writeBuffer.size());
		}
		int index = 0;
		for (QASamplesOperationEntry entry : writeBuffer) {
			netCdfMissingRatios.setDouble(netCdfMissingRatios.getIndex().set(index), entry.getMissingRatio());
			netCdfMissingCounts.setInt(netCdfMissingCounts.getIndex().set(index), entry.getMissingCount());
			netCdfHetzyRatios.setDouble(netCdfHetzyRatios.getIndex().set(index), entry.getHetzyRatio());
			index++;
		}
		try {
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT, origin, netCdfMissingRatios);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT, origin, netCdfMissingCounts);
			getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT, origin, netCdfHetzyRatios);
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}
}
