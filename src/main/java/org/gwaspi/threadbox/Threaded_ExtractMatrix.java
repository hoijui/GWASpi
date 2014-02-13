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
import java.util.Set;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.netCDF.operations.MatrixDataExtractor;
import org.gwaspi.netCDF.operations.MatrixDataExtractorNetCDFDataSetDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExtractMatrix extends CommonRunnable {

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
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExtractMatrix.class);
	}

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

			matrixOperation.processMatrix();
			final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

			Threaded_TranslateMatrix.matrixCompleeted(thisSwi, resultMatrixKey);
		}
	}
}
