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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.loader.DataSetDestination;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.dataextractor.MatrixDataExtractor;
import org.gwaspi.operations.dataextractor.MatrixDataExtractorMetadataFactory;
import org.gwaspi.operations.dataextractor.MatrixDataExtractorParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Merge Threaded_ExtractMatrix, Threaded_FlipStrandMatrix and Threaded_TranslateMatrix, as they are all the same (with minor differences).
 */
public class Threaded_ExtractMatrix extends CommonRunnable {

	private static final ProcessInfo fullExtractMatrixInfo
			= new DefaultProcessInfo("Full Data Extraction",
					"Data Extraction and evaluation of the results (QA)"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_MATRIX_EXTRACTION = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_MATRIX_EXTRACTION", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MATRIX_EXTRACTION, 0.4);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.6);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final MatrixDataExtractorParams params;
	private final SuperProgressSource progressSource;

	public Threaded_ExtractMatrix(MatrixDataExtractorParams params) {
		super("Extract Data", "from " + params.getMatrixFriendlyName());

		this.params = params;
		this.progressSource = new SuperProgressSource(fullExtractMatrixInfo, subProgressSourcesAndWeights);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExtractMatrix.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			progressSource.setNewStatus(ProcessStatus.INITIALIZING);
			final DataSetDestination dataSetDestination
					= MatrixFactory.generateMatrixDataSetDestination(params, MatrixDataExtractorMetadataFactory.SINGLETON);
			MatrixDataExtractor matrixOperation = new MatrixDataExtractor(params, dataSetDestination);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MATRIX_EXTRACTION, matrixOperation.getProgressSource(), null);

			progressSource.setNewStatus(ProcessStatus.RUNNING);
//			OperationManager.performOperation(matrixOperation); // XXX We can not do that, because MatrixDataExtractor does not support getParams() yet, so instead we do ...
			matrixOperation.processMatrix();
			final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

			Threaded_MatrixQA.matrixCompleeted(thisSwi, resultMatrixKey, progressSource);
			progressSource.setNewStatus(ProcessStatus.COMPLEETED);
		}
	}
}
