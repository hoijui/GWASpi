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
import java.util.Set;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.exporter.MatrixExporterParams;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.MatrixOperation;
import org.gwaspi.operations.dataextractor.MatrixDataExtractorParams;
import org.gwaspi.operations.genotypesflipper.MatrixGenotypesFlipperParams;
import org.gwaspi.operations.genotypestranslator.MatrixGenotypesTranslatorParams;
import org.gwaspi.operations.merge.MergeMatrixOperationParams;

public class MultiOperations {

	private MultiOperations() {
	}

	public static void queueTask(CommonRunnable task) {

		SwingWorkerItem swi = new SwingWorkerItem(task);
		SwingWorkerItemList.add(swi);
	}

	public static void addDataSet(final TaskLockProperties lockProperties, final DataSetKey dataSet) {

		lockProperties.getStudyIds().add(dataSet.getOrigin().getStudyId());
		lockProperties.getMatricesIds().add(dataSet.getOrigin().getMatrixId());

		if (dataSet.isOperation()) {
			lockProperties.getOperationsIds().add(dataSet.getOperationParent().getId());
		}
	}

	public static TaskLockProperties createTaskLockProperties(
			final DataSetKey parent,
			final Set<MatrixKey> participatingMatrices)
	{
		final TaskLockProperties lockProperties = new TaskLockProperties();

//		if (params.getMatrixKey().getStudyKey().isSpecifiedByName()) {
//			throw new IllegalStateException(); // FIXME need to fetch the study-id
//		}
//		if (params.getMatrixKey().isSpecifiedByName()) {
//			throw new IllegalStateException(); // FIXME need to fetch the matrix-id
//		}

		for (MatrixKey participatingMatrix : participatingMatrices) {
			lockProperties.getStudyIds().add(participatingMatrix.getStudyId());
			lockProperties.getMatricesIds().add(participatingMatrix.getMatrixId());
		}

		if (parent.isOperation()) {
			lockProperties.getOperationsIds().add(parent.getOperationParent().getId());
		}

		return lockProperties;
	}

	public static TaskLockProperties createTaskLockProperties(final DataSetKey parent) {
		return createTaskLockProperties(parent, Collections.singleton(parent.getOrigin()));
	}

	//<editor-fold defaultstate="expanded" desc="ANALYSIS">
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

		queueTask(task);
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

	public static void doExtractData(final MatrixDataExtractorParams params) {

		CommonRunnable task = new Threaded_ExtractMatrix(params);

		queueTask(task);
	}

	public static void doTranslateAB12ToACGT(final MatrixGenotypesTranslatorParams params) {

		CommonRunnable task = new Threaded_TranslateMatrix(params);

		queueTask(task);
	}

	public static void doExportMatrix(final MatrixExporterParams matrixExporterParams) {

		CommonRunnable task = new Threaded_ExportMatrix(matrixExporterParams);

		queueTask(task);
	}

	public static void doMergeMatrix(final MergeMatrixOperationParams params) {

		CommonRunnable task = new Threaded_MergeMatrices(params);

		queueTask(task);
	}

	public static void doStrandFlipMatrix(MatrixGenotypesFlipperParams params) {

		CommonRunnable task = new Threaded_FlipStrandMatrix(params);

		queueTask(task);
	}

	public static void updateSampleInfo(final int studyId, final File sampleInfoFile) {

		CommonRunnable task = new Threaded_UpdateSampleInfo(new StudyKey(studyId), sampleInfoFile);

		queueTask(task);
	}
	//</editor-fold>

	//<editor-fold defaultstate="expanded" desc="DELETERS">
	private static void queueDeleteTask(SwingDeleterItem sdi) {

		SwingDeleterItemList.add(sdi);
	}

	public static void deleteStudy(final StudyKey studyKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(studyKey, deleteReports);
		queueDeleteTask(sdi);
	}

	public static void deleteMatrix(final MatrixKey matrixKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(matrixKey, deleteReports);
		queueDeleteTask(sdi);
	}

	public static void deleteOperation(final OperationKey operationKey, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(operationKey, deleteReports);
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
