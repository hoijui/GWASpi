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

package org.gwaspi.threadbox;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.merge.MatrixMergeSamples;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.merge.MergeAllMatrixOperation;
import org.gwaspi.operations.merge.MergeMarkersMatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.merge.MergeMatrixMetadataFactory;
import org.gwaspi.operations.merge.MergeMatrixOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_MergeMatrices extends CommonRunnable {

	private static final ProcessInfo fullMergeMatricesProcessInfo
			= new DefaultProcessInfo("Merge Matrices & QA",
					"Merge Matrices & QA"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_MERGE = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_MERGE", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MERGE, 0.6);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.4);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final MergeMatrixOperationParams params;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;

	public Threaded_MergeMatrices(final MergeMatrixOperationParams params) {
		super(
				"Merge Data",
				"on " + params.getMatrixFriendlyName() + " and " + params.getSource2().toString());

		this.params = params;
		this.progressSource = new SuperProgressSource(fullMergeMatricesProcessInfo, subProgressSourcesAndWeights);
		this.taskLockProperties = MultiOperations.createTaskLockProperties(params.getParent());
		MultiOperations.addDataSet(taskLockProperties, params.getSource2());
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected ProgressHandler getProgressHandler() {
		return progressSource;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return taskLockProperties;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_MergeMatrices.class);
	}

	private MatrixOperation createMatrixOperation(
			MergeMatrixOperationParams params,
			DataSetDestination dataSetDestination)
			throws IOException
	{
		final MatrixOperation joinMatrices;
		if (params.isMergeSamples()) {
			joinMatrices = new MatrixMergeSamples(
					params,
					dataSetDestination);
		} else if (params.isMergeMarkers()) {
			joinMatrices = new MergeMarkersMatrixOperation(
					params,
					dataSetDestination);
		} else {
			joinMatrices = new MergeAllMatrixOperation(
					params,
					dataSetDestination);
		}

		return joinMatrices;
	}

	@Override
	protected void runInternal() throws IOException {

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);
		final DataSetDestination dataSetDestination
				= MatrixFactory.generateMatrixDataSetDestination(params, MergeMatrixMetadataFactory.SINGLETON);
		final MatrixOperation matrixOperation = createMatrixOperation(params, dataSetDestination);

		progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MERGE, matrixOperation.getProgressSource(), null);
		progressSource.setNewStatus(ProcessStatus.RUNNING);
		OperationManager.performOperation(matrixOperation);
		final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

		Threaded_MatrixQA.matrixCompleeted(resultMatrixKey, progressSource);
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
