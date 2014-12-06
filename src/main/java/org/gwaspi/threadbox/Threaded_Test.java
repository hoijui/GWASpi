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
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.allelicassociationtest.AllelicAssociationTestOperation;
import org.gwaspi.operations.hardyweinberg.ByHardyWeinbergThresholdFilterOperation;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.NullProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.reports.TestOutputParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Test extends CommonRunnable {

	private static final ProcessInfo fullTestProcessInfo
			= new DefaultProcessInfo("Full Test",
					"Complete Test procedure and generation of the reports"); // TODO
	private static final ProgressSource PLACEHOLDER_PS_TEST_REPORTS = new NullProgressHandler(
			new SubProcessInfo(null, "PLACEHOLDER_PS_TEST_REPORTS", null));
	private static final Map<ProgressSource, Double> subProgressSourcesAndWeights;
	static {
		final LinkedHashMap<ProgressSource, Double> tmpSubProgressSourcesAndWeights
				= new LinkedHashMap<ProgressSource, Double>(3);
		tmpSubProgressSourcesAndWeights.put(ByHardyWeinbergThresholdFilterOperation.PLACEHOLDER_PS_HW_TF, 0.3);
		tmpSubProgressSourcesAndWeights.put(AllelicAssociationTestOperation.PLACEHOLDER_PS_TEST, 0.4);
		tmpSubProgressSourcesAndWeights.put(PLACEHOLDER_PS_TEST_REPORTS, 0.3);
		subProgressSourcesAndWeights = Collections.unmodifiableMap(tmpSubProgressSourcesAndWeights);
	}

	private final OperationKey censusOpKey;
	private final OperationKey hwOpKey;
	private final GWASinOneGOParams gwasParams;
	private final OPType testType;
	private final SuperProgressSource progressSource;
	private final TaskLockProperties taskLockProperties;

	public Threaded_Test(
			OperationKey censusOpKey,
			OperationKey hwOpKey,
			GWASinOneGOParams gwasParams,
			OPType testType)
	{
		super(
				OutputTest.createTestName(testType) + " Test",
				"on Matrix ID: " + censusOpKey.getParentMatrixKey().getMatrixId());

		this.testType = testType;
		this.censusOpKey = censusOpKey;
		this.hwOpKey = hwOpKey;
		this.gwasParams = gwasParams;
		this.progressSource = new SuperProgressSource(fullTestProcessInfo, subProgressSourcesAndWeights);

		final MatrixKey matrixKey = censusOpKey.getParentMatrixKey();
		this.taskLockProperties = new TaskLockProperties();
		this.taskLockProperties.getRequiredStudies().add(matrixKey.getStudyId());
		this.taskLockProperties.getRequiredMatrices().add(matrixKey.getMatrixId());
		this.taskLockProperties.getRequiredOperations().add(censusOpKey.getId());
		this.taskLockProperties.getRequiredOperations().add(hwOpKey.getId());
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
		return LoggerFactory.getLogger(Threaded_Test.class);
	}

	@Override
	protected void runInternal() throws IOException {

		progressSource.setNewStatus(ProcessStatus.INITIALIZING);

		final OperationMetadata operationMetadata = OperationsList.getOperationMetadata(censusOpKey);
		final List<OperationMetadata> operations = OperationsList.getOffspringOperationsMetadata(censusOpKey.getParentMatrixKey());
		final OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA, operationMetadata.getNumMarkers());

//		if (!gwasParams.isDiscardMarkerByMisRat()) {
//			gwasParams.setDiscardMarkerMisRatVal(1);
//		}
//		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
//			gwasParams.setDiscardMarkerHetzyRatVal(1);
//		}
//		if (!gwasParams.isDiscardSampleByMisRat()) {
//			gwasParams.setDiscardSampleMisRatVal(1);
//		}
//		if (!gwasParams.isDiscardSampleByHetzyRat()) {
//			gwasParams.setDiscardSampleHetzyRatVal(1);
//		}

		// TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpKey);

		if (gwasParams.isDiscardMarkerHWCalc()) {
			gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
		}

		// NOTE ABORTION_POINT We could be gracefully abort here

		progressSource.setNewStatus(ProcessStatus.RUNNING);
		OperationKey testOpKey = OperationManager.performCleanTests(
				censusOpKey,
				hwOpKey,
				gwasParams.getDiscardMarkerHWTreshold(),
				testType,
				progressSource);

		// Make Reports (needs newMatrixId, QAopId, AssocOpId)
		if (testOpKey != null) {
			final TestOutputParams testOutputParams = new TestOutputParams(testOpKey, testType, markersQAOpKey);
			final MatrixOperation reportsGenerationOperation = new OutputTest(testOutputParams);
			progressSource.replaceSubProgressSource(PLACEHOLDER_PS_TEST_REPORTS, reportsGenerationOperation.getProgressSource(), null);
//				OperationManager.performOperation(reportsGenerationOperation); // XXX We can not do that, because OutputTest does not support getParams() yet, so instead we do ...
			reportsGenerationOperation.processMatrix();
			progressSource.setNewStatus(ProcessStatus.FINALIZING);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(testOpKey);
		}
		progressSource.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
