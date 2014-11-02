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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperation;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.HardyWeinbergOutputParams;
import org.gwaspi.reports.OutputHardyWeinberg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_HardyWeinberg extends CommonRunnable {

	private static final ProcessInfo fullHWProcessInfo
			= new DefaultProcessInfo("Full Hardy & Weinberg Test",
					"Complete Hardy & Weinberg Test and generation of reports"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_HW = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_HW", null));
	private static final ProgressSource PLACEHOLDER_PS_HW_REPORTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_HW_REPORTS", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(2);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_HW, 0.7);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_HW_REPORTS, 0.3);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final HardyWeinbergOperationParams params;
	/** The result, or <code>null</code> */
	private OperationKey hardyWeinbergOperationKey;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;

	public Threaded_HardyWeinberg(final HardyWeinbergOperationParams params) {
		super(
				"Hardy-Weinberg Test",
				"on " + params.getParent().toString());

		this.params = params;
		this.hardyWeinbergOperationKey = null;
		this.progressSource = new SuperProgressSource(fullHWProcessInfo, subProgressSourcesAndWeights);
		this.taskLockProperties = MultiOperations.createTaskLockProperties(params.getParent());
	}

	@Override
	public ProgressSource getProgressSource() {
		return progressSource;
	}

	@Override
	public TaskLockProperties getTaskLockProperties() {
		return taskLockProperties;
	}

	public static HardyWeinbergOperationParams createParams(OperationKey censusOpKey) throws IOException {

		if (censusOpKey != null) {
			OperationMetadata censusOpMetadata = OperationsList.getOperationMetadata(censusOpKey);
			final OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(censusOpMetadata.getParent(), OPType.MARKER_QA).get(0));
			return new HardyWeinbergOperationParams(censusOpKey, cNetCDF.Defaults.DEFAULT_AFFECTION, markersQAOpKey);
		}

		return null;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_HardyWeinberg.class);
	}

	public OperationKey getHardyWeinbergOperationKey() {
		return hardyWeinbergOperationKey;
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			progressSource.setNewStatus(ProcessStatus.INITIALIZING);
			final MatrixOperation operation = new HardyWeinbergOperation(params);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_HW, operation.getProgressSource(), null);
			progressSource.setNewStatus(ProcessStatus.RUNNING);
			hardyWeinbergOperationKey = OperationManager.performOperation(operation);

			final HardyWeinbergOutputParams hardyWeinbergOutputParams
					= new HardyWeinbergOutputParams(hardyWeinbergOperationKey, params.getMarkersQAOpKey());
			final OutputHardyWeinberg outputHardyWeinberg
					= new OutputHardyWeinberg(hardyWeinbergOutputParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_HW_REPORTS, outputHardyWeinberg.getProgressSource(), null);
			OperationManager.performOperation(outputHardyWeinberg);
			progressSource.setNewStatus(ProcessStatus.COMPLEETED);
		}
	}
}
