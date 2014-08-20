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
import java.util.Set;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.merge.MergeMatrixOperationParams;

public class MultiOperations {

	private MultiOperations() {
	}

	private static void queueTask(CommonRunnable task, TaskLockProperties lockProperties) {

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				lockProperties.getStudyIds().toArray(new Integer[] {}),
				lockProperties.getMatricesIds().toArray(new Integer[] {}),
				lockProperties.getOperationsIds().toArray(new Integer[] {}));
		SwingWorkerItemList.add(swi);
	}

	public static void doMatrixQAs(final DataSetKey parentKey) {

		CommonRunnable task = new Threaded_MatrixQA(parentKey);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(parentKey.getOrigin().getStudyKey().getId());
		lockProperties.getMatricesIds().add(parentKey.getOrigin().getMatrixId());
		if (parentKey.isOperation()) {
			lockProperties.getOperationsIds().add(parentKey.getOperationParent().getId());
		}

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS if requested and OK */
	public static void loadMatrixDoGWASifOK(
			final GenotypesLoadDescription loadDescription,
			final boolean dummySamples,
			final boolean performGwas,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_Loader_GWASifOK(
				loadDescription,
				dummySamples,
				performGwas,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(loadDescription.getStudyKey().getId());

		queueTask(task, lockProperties);
	}

	private static TaskLockProperties createTaskLockProperties(
			final DataSetKey parent)
	{
		final MatrixKey matrixKey = parent.getOrigin();

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(matrixKey.getStudyId());
		lockProperties.getMatricesIds().add(matrixKey.getMatrixId());
		if (parent.isOperation()) {
			lockProperties.getOperationsIds().add(parent.getOperationParent().getId());
		}

		return lockProperties;
	}

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
	/** LOAD & GWAS */
	public static void doGWASwithAlterPhenotype(
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_GWAS(gwasParams);

		final DataSetKey parent = gwasParams.getMarkerCensusOperationParams().getParent();
		TaskLockProperties lockProperties = createTaskLockProperties(parent);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public static void doGTFreqDoHW(final GWASinOneGOParams gwasParams) {

		CommonRunnable task = new Threaded_GTFreq_HW(gwasParams);

		final DataSetKey parent = gwasParams.getMarkerCensusOperationParams().getParent();
		TaskLockProperties lockProperties = createTaskLockProperties(parent);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public static void doTest(
			final OperationKey censusOPKey,
			final OperationKey hwOPKey,
			final GWASinOneGOParams gwasParams,
			final OPType testType)
	{
		CommonRunnable task = new Threaded_Test(
				censusOPKey,
				hwOPKey,
				gwasParams,
				testType);

		final MatrixKey matrixKey = censusOPKey.getParentMatrixKey();

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(matrixKey.getStudyId());
		lockProperties.getMatricesIds().add(matrixKey.getMatrixId());
		lockProperties.getOperationsIds().add(censusOPKey.getId());
		lockProperties.getOperationsIds().add(hwOPKey.getId());

		queueTask(task, lockProperties);
	}

	public static void doCombiTest(
			final CombiTestOperationParams paramsTest,
			final ByCombiWeightsFilterOperationParams paramsFilter)
	{
		CommonRunnable task = new Threaded_Combi(paramsTest, paramsFilter);

		TaskLockProperties lockProperties = new TaskLockProperties();
//		if (params.getMatrixKey().getStudyKey().isSpecifiedByName()) {
//			throw new IllegalStateException(); // FIXME need to fetch the study-id
//		}
//		if (params.getMatrixKey().isSpecifiedByName()) {
//			throw new IllegalStateException(); // FIXME need to fetch the matrix-id
//		}
		Set<MatrixKey> participatingMatrices = paramsTest.getParticipatingMatrices();
//		participatingMatrices.addAll(paramsFilter.getParticipatingMatrices());
		for (MatrixKey participatingMatrix : participatingMatrices) {
			lockProperties.getStudyIds().add(participatingMatrix.getStudyId());
			lockProperties.getMatricesIds().add(participatingMatrix.getMatrixId());
		}

		queueTask(task, lockProperties);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="DATA MANAGEMENT">
	public static void doMatrixOperation(final MatrixOperation matrixOperation) {
		throw new UnsupportedOperationException("implement (and use) me!"); // TODO
//		CommonRunnable task = new Threaded_MatrixOperation(matrixOperation);
//
//		TaskLockProperties lockProperties = new TaskLockProperties();
//		lockProperties.getStudyIds().add(parentMatrixKey.getStudyId());
//		lockProperties.getMatricesIds().add(parentMatrixKey.getMatrixId());
//
//		queueTask(task, lockProperties);
	}

	public static void doExtractData(
			final MatrixKey parentMatrixKey,
			final String newMatrixName,
			final String description,
			final SetMarkerPickCase markerPickCase,
			final SetSamplePickCase samplePickCase,
			final String markerPickVar,
			final String samplePickVar,
			final Set<Object> markerCriteria,
			final Set<Object> sampleCriteria,
			final File markerCriteriaFile,
			final File sampleCriteriaFile)
	{
//		try {
			DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey);
			CommonRunnable task = new Threaded_ExtractMatrix(
					dataSetSource,
					newMatrixName,
					description,
					markerPickCase,
					samplePickCase,
					markerPickVar,
					samplePickVar,
					markerCriteria,
					sampleCriteria,
					markerCriteriaFile,
					sampleCriteriaFile);

			TaskLockProperties lockProperties = new TaskLockProperties();
			lockProperties.getStudyIds().add(parentMatrixKey.getStudyId());
			lockProperties.getMatricesIds().add(parentMatrixKey.getMatrixId());

			queueTask(task, lockProperties);
//		} catch (IOException ex) {
//			throw new RuntimeException(ex);
//		}
	}

	public static void doTranslateAB12ToACGT(
			final MatrixKey parentMatrixKey,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_TranslateMatrix(
				parentMatrixKey,
				newMatrixName,
				description);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(parentMatrixKey.getStudyId());
		lockProperties.getMatricesIds().add(parentMatrixKey.getMatrixId());

		queueTask(task, lockProperties);
	}

	public static void doExportMatrix(final MatrixExporterParams matrixExporterParams)
	{
		CommonRunnable task = new Threaded_ExportMatrix(matrixExporterParams);

		final MatrixKey matrixKey = matrixExporterParams.getParent().getOrigin();
		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(matrixKey.getStudyId());
		lockProperties.getMatricesIds().add(matrixKey.getMatrixId());

		queueTask(task, lockProperties);
	}

	public static void doMergeMatrix(final MergeMatrixOperationParams params) {

		CommonRunnable task = new Threaded_MergeMatrices(params);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(params.getParent().getMatrixParent().getStudyKey().getId());
		lockProperties.getMatricesIds().add(params.getParent().getMatrixParent().getMatrixId());
		lockProperties.getMatricesIds().add(params.getSource2().getMatrixParent().getMatrixId());

		queueTask(task, lockProperties);
	}

	public static void doStrandFlipMatrix(
			final MatrixKey parentMatrixKey,
			final String markerIdentifyer,
			final File markerFlipFile,
			final String newMatrixName,
			final String description)
	{
		try {
			DataSetSource dataSetSource = MatrixFactory.generateMatrixDataSetSource(parentMatrixKey);
			CommonRunnable task = new Threaded_FlipStrandMatrix(
					dataSetSource,
					newMatrixName,
					description,
					markerFlipFile);

			TaskLockProperties lockProperties = new TaskLockProperties();
			lockProperties.getStudyIds().add(parentMatrixKey.getStudyKey().getId());
			lockProperties.getMatricesIds().add(parentMatrixKey.getMatrixId());

			queueTask(task, lockProperties);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void updateSampleInfo(
			final int studyId,
			final File sampleInfoFile)
	{
		CommonRunnable task = new Threaded_UpdateSampleInfo(
				new StudyKey(studyId),
				sampleInfoFile);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);

		queueTask(task, lockProperties);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="DELETERS">
	private static void queueDeleteTask(SwingDeleterItem sdi) {

		SwingDeleterItemList.add(sdi);
	}

	public static void deleteStudy(final StudyKey studyKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				studyKey,
				deleteReports);
		queueDeleteTask(sdi);
	}

	public static void deleteMatrix(final MatrixKey matrixKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				matrixKey,
				deleteReports);
		queueDeleteTask(sdi);
	}

	public static void deleteOperation(final OperationKey operationKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				operationKey,
				deleteReports);
		queueDeleteTask(sdi);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="HELPERS">
	public static void printFinished(String text) {
		org.gwaspi.global.Utils.sysoutFinish(text);
	}

	public static void printCompleted(String text) {
		org.gwaspi.global.Utils.sysoutCompleted(text);
	}

	public static void printError(String text) {
		org.gwaspi.global.Utils.sysoutError(text);
	}

	public static void updateTree() throws IOException {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(false);
			GWASpiExplorerPanel.getSingleton().updateTreePanel(false);
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(true);
		}
	}

	public static void updateTreeAndPanel() throws IOException {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(false);
			GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(true);
		}
	}

	private static void refreshPanel() {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().refreshContentPanel();
		}
	}

	public static void updateProcessOverviewStartNext() throws IOException {
		SwingWorkerItemList.startNext();
	}

	public static void updateProcessOverviewDeleteNext() throws IOException {
		SwingDeleterItemList.deleteAllListed();
	}
	//</editor-fold>
}
