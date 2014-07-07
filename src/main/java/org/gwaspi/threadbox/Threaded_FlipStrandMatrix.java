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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.operations.MatrixGenotypesFlipper;
import org.gwaspi.operations.MatrixGenotypesFlipperNetCDFDataSetDestination;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_FlipStrandMatrix extends CommonRunnable {

	private static final ProcessInfo fullFlipStrandMatrixInfo
			= new DefaultProcessInfo("Full Flip Strand",
					"Flip Strand and QA"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_MATRIX_STRAND_FLIP = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_MATRIX_STRAND_FLIP", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_MATRIX_STRAND_FLIP, 0.4);
		tmpSubProgressSourcesAndWeights.put(Threaded_MatrixQA.PLACEHOLDER_PS_QA, 0.6);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final DataSetSource parentDataSetSource;
	private final String newMatrixName;
	private final String description;
	private final File markerFlipFile;
	private final SuperProgressSource progressSource;

	public Threaded_FlipStrandMatrix(
			DataSetSource parentDataSetSource,
			String newMatrixName,
			String description,
			File markerFlipFile)
			throws IOException
	{
		super(
				"Flip Strand Matrix",
				"Flipping Genotypes",
				"Flip Strand Matrix ID: " + parentDataSetSource.getMatrixMetadata().getKey().getMatrixId(),
				"Extracting");


		this.parentDataSetSource = parentDataSetSource;
		this.newMatrixName = newMatrixName;
		this.description = description;
		this.markerFlipFile = markerFlipFile;
		this.progressSource = new SuperProgressSource(fullFlipStrandMatrixInfo, subProgressSourcesAndWeights);
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_FlipStrandMatrix.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			MatrixGenotypesFlipperNetCDFDataSetDestination dataSetDestination
					= new MatrixGenotypesFlipperNetCDFDataSetDestination(
					parentDataSetSource,
					newMatrixName,
					description,
					markerFlipFile);
			MatrixGenotypesFlipper matrixOperation = new MatrixGenotypesFlipper(
					parentDataSetSource,
					dataSetDestination,
					markerFlipFile);

			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_MATRIX_STRAND_FLIP, matrixOperation.getProgressSource(), null);
			matrixOperation.processMatrix();
			final MatrixKey resultMatrixKey = dataSetDestination.getResultMatrixKey();

			Threaded_MatrixQA.matrixCompleeted(thisSwi, resultMatrixKey, progressSource);
		}
	}
}
