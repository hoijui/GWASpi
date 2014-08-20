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

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.dataextractor.MatrixDataExtractor;
import org.gwaspi.operations.dataextractor.MatrixDataExtractorNetCDFDataSetDestination;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private final DataSetSource dataSetSource;
	private final String newMatrixName;
	private final String description;
	private final SetMarkerPickCase markerPickCase;
	private final SetSamplePickCase samplePickCase;
	private final String markerPickVar;
	private final String samplePickVar;
	private final Set<Object> markerCriteria;
	private final Set<Object> sampleCriteria;
	private final File markerCriteriaFile;
	private final File sampleCriteriaFile;
	private final SuperProgressSource progressSource;

	public Threaded_ExtractMatrix(
			DataSetSource dataSetSource,
			String newMatrixName,
			String description,
			SetMarkerPickCase markerPickCase,
			SetSamplePickCase samplePickCase,
			String markerPickVar,
			String samplePickVar,
			Set<Object> markerCriteria,
			Set<Object> sampleCriteria,
			File markerCriteriaFile,
			File sampleCriteriaFile)
	{
		super(
				"Data Extract",
				"Extracting Data",
				"Data Extract: " + newMatrixName,
				"Extracting");

		this.dataSetSource = dataSetSource;
		this.newMatrixName = newMatrixName;
		this.description = description;
		this.markerPickCase = markerPickCase;
		this.samplePickCase = samplePickCase;
		this.markerPickVar = markerPickVar;
		this.samplePickVar = samplePickVar;
		this.markerCriteria = markerCriteria;
		this.sampleCriteria = sampleCriteria;
		this.markerCriteriaFile = markerCriteriaFile;
		this.sampleCriteriaFile = sampleCriteriaFile;
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
			MatrixDataExtractorNetCDFDataSetDestination dataSetDestination
					= new MatrixDataExtractorNetCDFDataSetDestination(
					dataSetSource,
					description,
					newMatrixName,
					markerCriteriaFile,
					sampleCriteriaFile,
					markerPickCase,
					markerPickVar,
					samplePickCase,
					samplePickVar);
			MatrixDataExtractor matrixOperation = new MatrixDataExtractor(
					dataSetSource,
					dataSetDestination,
					markerPickCase,
					samplePickCase,
					markerPickVar,
					samplePickVar,
					markerCriteria,
					sampleCriteria,
					Integer.MIN_VALUE, // Filter pos, not used now
					markerCriteriaFile,
					sampleCriteriaFile);
			dataSetDestination.setMatrixDataExtractor(matrixOperation); // HACK!

			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MATRIX_EXTRACTION, matrixOperation.getProgressSource(), null);
			matrixOperation.processMatrix();
			final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

			Threaded_MatrixQA.matrixCompleeted(thisSwi, resultMatrixKey, progressSource);
		}
	}
}
