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
import java.util.Iterator;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;

public class QASamplesOperation extends AbstractOperationCreatingOperation<QASamplesOperationDataSet, QASamplesOperationParams> {

	private static final ProcessInfo processInfo = new DefaultProcessInfo(
			"Samples Quality Assurance",
			""); // TODO

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Samples Quality Assurance",
					"Samples Quality Assurance", // TODO We need a more elaborate description of this operation!
					OPType.SAMPLE_QA,
					false,
					true);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new AbstractDefaultTypesOperationFactory(
				QASamplesOperation.class, OPERATION_TYPE_INFO) {
					@Override
					protected OperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
						return new NetCdfQASamplesOperationDataSet(parent.getOrigin(), parent, operationKey);
					}
				});
	}

	private ProgressHandler operationPH;

	public QASamplesOperation(QASamplesOperationParams params) {
		super(params);
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return processInfo;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {

		if (operationPH == null) {
			final DataSetSource parentDataSetSource = getParentDataSetSource();
			final int numSamples = parentDataSetSource.getNumSamples();

			operationPH =  new IntegerProgressHandler(
					processInfo,
					0, // start state, first sample
					numSamples - 1); // end state, last sample
		}

		return operationPH;
	}

	@Override
	public int processMatrix() throws IOException {

		int resultOpId;

		org.gwaspi.global.Utils.sysoutStart("Sample QA");
		operationPH.setNewStatus(ProcessStatus.INITIALIZING);

		DataSetSource dataSetSource = getParentDataSetSource();

		MatrixMetadata rdMatrixMetadata = getParentMatrixMetadata();

		QASamplesOperationDataSet dataSet = generateFreshOperationDataSet();
		dataSet.setNumMarkers(rdMatrixMetadata.getNumMarkers());
		dataSet.setNumChromosomes(rdMatrixMetadata.getNumChromosomes());
		dataSet.setNumSamples(rdMatrixMetadata.getNumSamples());

		SamplesGenotypesSource samplesGenotypes = dataSetSource.getSamplesGenotypesSource();
		MarkersMetadataSource markersInfSrc = dataSetSource.getMarkersMetadatasSource();

		// Iterate through samples
		Iterator<GenotypesList> samplesGenotypesIt = samplesGenotypes.iterator();
		operationPH.setNewStatus(ProcessStatus.RUNNING);
		int localSampleIndex = 0;
		for (Map.Entry<Integer, SampleKey> sampleKeyEntry : dataSetSource.getSamplesKeysSource().getIndicesMap().entrySet()) {
			final int sampleOrigIndex = sampleKeyEntry.getKey();
			final SampleKey sampleKey = sampleKeyEntry.getValue();

			int missingCount = 0;
			int heterozygCount = 0;

			// Iterate through markerset
			GenotypesList sampleGenotypes = samplesGenotypesIt.next();
			Iterator<MarkerMetadata> markersInfSrcIt = markersInfSrc.iterator();
			for (byte[] tempGT : sampleGenotypes) {
				if ((tempGT[0] == AlleleByte._0_VALUE) && (tempGT[1] == AlleleByte._0_VALUE)) {
					missingCount++;
				}

				// WE DON'T WANT NON AUTOSOMAL CHR FOR HETZY
				String currentChr = markersInfSrcIt.next().getChr();
				if (!currentChr.equals("X")
						&& !currentChr.equals("Y")
						&& !currentChr.equals("XY")
						&& !currentChr.equals("MT")
						&& (tempGT[0] != tempGT[1]))
				{
					heterozygCount++;
				}
			}

			final double missingRatio = (double) missingCount / dataSetSource.getNumMarkers();
			final double heterozygRatio = (double) heterozygCount / (dataSetSource.getNumMarkers() - missingCount);

			dataSet.addEntry(new DefaultQASamplesOperationEntry(
					sampleKey,
					sampleOrigIndex,
					missingRatio,
					missingCount,
					heterozygRatio
			));
			operationPH.setProgress(localSampleIndex);
			localSampleIndex++;
		}
		operationPH.setNewStatus(ProcessStatus.FINALIZING);

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Sample QA");
		operationPH.setNewStatus(ProcessStatus.COMPLEETED);

		return resultOpId;
	}
}
