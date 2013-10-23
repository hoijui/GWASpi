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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.netCDF.operations.SampleOperationSet;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

public class NetCdfQASamplesOperationDataSet extends AbstractNetCdfOperationDataSet<QASamplesOperationEntry> implements QASamplesOperationDataSet {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getId()) marker keys
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT: (double) missing ratio for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT: (int) missing count for each sample
	// - cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT: (double) heterozygosity ratio for each sample

	private final Logger log = LoggerFactory.getLogger(NetCdfQASamplesOperationDataSet.class);

	public NetCdfQASamplesOperationDataSet() {
		super(false);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

			// CREATE netCDF-3 FILE
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
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void setSampleMissingRatios(Collection<Double> sampleMissingRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), sampleMissingRatios, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
	}

	@Override
	public void setSampleMissingCount(Collection<Integer> sampleMissingCount) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), sampleMissingCount, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);
	}

	@Override
	public void setSampleHetzyRatios(Collection<Double> sampleHetzyRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), sampleHetzyRatios, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
	}

	public Collection<Double> getSampleMissingRatios(int from, int to) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(getResultOperationKey());
		SampleOperationSet rdInfoSampleSet = new SampleOperationSet(getResultOperationKey(), from, to);
		Map<SampleKey, Double> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();
		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
		ncFile.close();

		return rdMatrixSampleSetMap.values();
	}

	public Collection<Integer> getSampleMissingCount(int from, int to) throws IOException {

//		OperationMetadata rdOPMetadata = OperationsList.getOperation(getResultOperationKey());
		SampleOperationSet rdInfoSampleSet = new SampleOperationSet(getResultOperationKey(), from, to);
		Map<SampleKey, Integer> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();
//		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		NetcdfFile ncFile = getNetCdfWriteFile();
		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGCOUNT);
		ncFile.close();

		return rdMatrixSampleSetMap.values();
	}

	public Collection<Double> getSampleHetzyRatios(int from, int to) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(getResultOperationKey());
		SampleOperationSet rdInfoSampleSet = new SampleOperationSet(getResultOperationKey(), from, to);
		Map<SampleKey, Double> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();
		NetcdfFile ncFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
		rdMatrixSampleSetMap = rdInfoSampleSet.fillOpSetMapWithVariable(ncFile, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
		ncFile.close();

		return rdMatrixSampleSetMap.values();
	}

	@Override
	public Collection<QASamplesOperationEntry> getEntries(int from, int to) throws IOException {

		OperationMetadata rdOPMetadata = OperationsList.getOperation(getResultOperationKey());
		SampleOperationSet rdInfoSampleSet = new SampleOperationSet(getResultOperationKey(), from, to);
		Map<SampleKey, ?> rdMatrixSampleSetMap = rdInfoSampleSet.getOpSetMap();

		Collection<Double> sampleMissingRatios = getSampleMissingRatios(from, to);
		Collection<Integer> sampleMissingCount = getSampleMissingCount(from, to);
		Collection<Double> sampleHetzyRatios = getSampleHetzyRatios(from, to);

		Collection<QASamplesOperationEntry> entries
				= new ArrayList<QASamplesOperationEntry>(sampleMissingRatios.size());
		Iterator<Double> sampleMissingRatioIt = sampleMissingRatios.iterator();
		Iterator<Integer> sampleMissingCountIt = sampleMissingCount.iterator();
		Iterator<Double> sampleHetzyRatiosIt = sampleHetzyRatios.iterator();
		for (SampleKey sampleKey : rdMatrixSampleSetMap.keySet()) {
			entries.add(new DefaultQASamplesOperationEntry(sampleKey, sampleMissingRatioIt.next(), sampleMissingCountIt.next(), sampleHetzyRatiosIt.next()));
		}

		return entries;
	}
}
