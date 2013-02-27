package org.gwaspi.threadbox;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.apache.felix.scr.annotations.Reference;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.gui.StartGWASpi;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MultiOperations {

	@Reference
	private SwingWorkerItemList swingWorkerItemList;
	@Reference
	private SwingDeleterItemList swingDeleterItemList;

	protected void bindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {
		this.swingWorkerItemList = swingWorkerItemList;
	}

	protected void unbindSwingWorkerItemList(SwingWorkerItemList swingWorkerItemList) {

		if (this.swingWorkerItemList == swingWorkerItemList) {
			this.swingWorkerItemList = null;
		}
	}

	protected void bindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {
		this.swingDeleterItemList = swingDeleterItemList;
	}

	protected void unbindSwingDeleterItemList(SwingDeleterItemList swingDeleterItemList) {

		if (this.swingDeleterItemList == swingDeleterItemList) {
			this.swingDeleterItemList = null;
		}
	}

	private MultiOperations() {
	}

	private void queueTask(CommonRunnable task, TaskLockProperties lockProperties) {

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				lockProperties.getStudyIds().toArray(new Integer[] {}),
				lockProperties.getMatricesIds().toArray(new Integer[] {}),
				lockProperties.getOperationsIds().toArray(new Integer[] {}));
		swingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	//<editor-fold defaultstate="collapsed" desc="LOADERS">
	/** SAMPLES QA */
	public void doMatrixQAs(final int studyId, final int matrixId) {

		CommonRunnable task = new Threaded_MatrixQA(matrixId);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS if requested and OK */
	public void loadMatrixDoGWASifOK(
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
		lockProperties.getStudyIds().add(loadDescription.getStudyId());

		queueTask(task, lockProperties);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ANALYSIS">
	/** LOAD & GWAS */
	public void doGWASwithAlterPhenotype(
			final int studyId,
			final int matrixId,
			final File phenofile,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_GWAS(
				matrixId,
				phenofile,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public void doHardyWeinberg(
			final int studyId,
			final int matrixId,
			final int censusOpId)
	{
		CommonRunnable task = new Threaded_HardyWeinberg(
				matrixId,
				censusOpId);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public void doGTFreqDoHW(
			final int studyId,
			final int matrixId,
			final File phenoFile,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_GTFreq_HW(
				matrixId,
				phenoFile,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public void doAllelicAssociationTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_AllelicAssociation(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);
		lockProperties.getOperationsIds().add(censusOPId);
		lockProperties.getOperationsIds().add(hwOPId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public void doGenotypicAssociationTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_GenotypicAssociation(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);
		lockProperties.getOperationsIds().add(censusOPId);
		lockProperties.getOperationsIds().add(hwOPId);

		queueTask(task, lockProperties);
	}

	/** LOAD & GWAS */
	public void doTrendTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		CommonRunnable task = new Threaded_TrendTest(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);
		lockProperties.getOperationsIds().add(censusOPId);
		lockProperties.getOperationsIds().add(hwOPId);

		queueTask(task, lockProperties);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="DATA MANAGEMENT">
	public void doExtractData(
			final int studyId,
			final int parentMatrixId,
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
		CommonRunnable task = new Threaded_ExtractMatrix(
				studyId,
				parentMatrixId,
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
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId);

		queueTask(task, lockProperties);
	}

	public void doTranslateAB12ToACGT(
			final int studyId,
			final int parentMatrixId,
			final GenotypeEncoding gtEncoding,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_TranslateMatrix(
				studyId,
				parentMatrixId,
				gtEncoding,
				newMatrixName,
				description);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId);

		queueTask(task, lockProperties);
	}

	public void doExportMatrix(
			final int studyId,
			final int matrixId,
			final ExportFormat format,
			final String phenotype)
	{
		CommonRunnable task = new Threaded_ExportMatrix(
				matrixId,
				format,
				phenotype);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(matrixId);

		queueTask(task, lockProperties);
	}

	public void doMergeMatrixAddMarkers(
			final int studyId,
			final int parentMatrixId1,
			final int parentMatrixId2,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_MergeMatricesAddMarkers(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId1);
		lockProperties.getMatricesIds().add(parentMatrixId2);

		queueTask(task, lockProperties);
	}

	public void doMergeMatrixAddSamples(
			final int studyId,
			final int parentMatrixId1,
			final int parentMatrixId2,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_MergeMatricesAddSamples(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId1);
		lockProperties.getMatricesIds().add(parentMatrixId2);

		queueTask(task, lockProperties);
	}

	public void doMergeMatrixAll(final int studyId,
			final int parentMatrixId1,
			final int parentMatrixId2,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_MergeMatricesAddAll(
				studyId,
				parentMatrixId1,
				parentMatrixId2,
				newMatrixName,
				description);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId1);
		lockProperties.getMatricesIds().add(parentMatrixId2);

		queueTask(task, lockProperties);
	}

	public void doStrandFlipMatrix(
			final int studyId,
			final int parentMatrixId,
			final String markerIdentifyer,
			final File markerFlipFile,
			final String newMatrixName,
			final String description)
	{
		CommonRunnable task = new Threaded_FlipStrandMatrix(
				studyId,
				parentMatrixId,
				newMatrixName,
				description,
				markerIdentifyer,
				markerFlipFile);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);
		lockProperties.getMatricesIds().add(parentMatrixId);

		queueTask(task, lockProperties);
	}

	public void updateSampleInfo(
			final int studyId,
			final File sampleInfoFile)
	{
		CommonRunnable task = new Threaded_UpdateSampleInfo(
				studyId,
				sampleInfoFile);

		TaskLockProperties lockProperties = new TaskLockProperties();
		lockProperties.getStudyIds().add(studyId);

		queueTask(task, lockProperties);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="DELETERS">
	private void queueDeleteTask(SwingDeleterItem sdi) {

		swingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public void deleteStudy(final int studyId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				SwingDeleterItem.DeleteTarget.STUDY,
				studyId,
				deleteReports);
		queueDeleteTask(sdi);
	}

	public void deleteMatrix(final int studyId, final int matrixId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				SwingDeleterItem.DeleteTarget.MATRIX,
				studyId,
				matrixId,
				deleteReports);
		queueDeleteTask(sdi);
	}

	public void deleteOperationsByOpId(final int studyId, final int matrixId, final int opId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(
				SwingDeleterItem.DeleteTarget.OPERATION_BY_OPID,
				studyId,
				matrixId,
				opId,
				deleteReports);
		queueDeleteTask(sdi);
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="HELPERS">
	public static void printFinished(String text) {
		org.gwaspi.global.Utils.sysoutFinish(text);
	}

	public static void printCompleted(String text) {
		org.gwaspi.global.Utils.sysoutCompleted(text);
	}

	public static void printError(String text) {
		org.gwaspi.global.Utils.sysoutError(text);
	}

	public void updateTree() throws IOException {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(false);
			GWASpiExplorerPanel.getSingleton().updateTreePanel(false);
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(true);
		}
	}

	public void updateTreeAndPanel() throws IOException {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(false);
			GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
			GWASpiExplorerPanel.getSingleton().getTree().setEnabled(true);
		}
	}

	private void refreshPanel() {
		if (StartGWASpi.guiMode) {
			GWASpiExplorerPanel.getSingleton().refreshContentPanel();
		}
	}

	public void updateProcessOverviewStartNext() throws IOException {
		swingWorkerItemList.startNext();
		if (StartGWASpi.guiMode) {
			ProcessTab.getSingleton().updateProcessOverview();
			ProcessTab.getSingleton().toggleBusyLogo();
		}
	}

	public void updateProcessOverviewDeleteNext() throws IOException {
		swingDeleterItemList.deleteAllListed();
		if (StartGWASpi.guiMode) {
			ProcessTab.getSingleton().updateProcessOverview();
			ProcessTab.getSingleton().toggleBusyLogo();
		}
	}
	//</editor-fold>
}
