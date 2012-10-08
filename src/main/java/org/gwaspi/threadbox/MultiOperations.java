package org.gwaspi.threadbox;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.SetMarkerPickCase;
import org.gwaspi.constants.cNetCDF.Defaults.SetSamplePickCase;
import org.gwaspi.gui.GWASpiExplorerPanel;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.gui.StartGWASpi;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MultiOperations {

	private MultiOperations() {
	}

	//<editor-fold defaultstate="collapsed" desc="LOADERS">
	public static void doMatrixQAs(final int studyId, final int matrixId) {

		// SAMPLES QA
		CommonRunnable task = new Threaded_MatrixQA(matrixId);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void loadMatrixDoGWASifOK(
			final GenotypesLoadDescription loadDescription,
			final boolean dummySamples,
			final boolean performGwas,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS if requested and OK
		CommonRunnable task = new Threaded_Loader_GWASifOK(
				loadDescription,
				dummySamples,
				performGwas,
				gwasParams);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{loadDescription.getStudyId()});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ANALYSIS">
	public static void doGWASwithAlterPhenotype(
			final int studyId,
			final int matrixId,
			final File phenofile,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_GWAS(
				matrixId,
				phenofile,
				gwasParams);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doHardyWeinberg(
			final int studyId,
			final int matrixId,
			final int censusOpId)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_HardyWeinberg(
				matrixId,
				censusOpId);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doGTFreqDoHW(
			final int studyId,
			final int matrixId,
			final File phenoFile,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_GTFreq_HW(
				matrixId,
				phenoFile,
				gwasParams);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doAllelicAssociationTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_AllelicAssociation(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doGenotypicAssociationTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_GenotypicAssociation(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doTrendTest(
			final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		CommonRunnable task = new Threaded_TrendTest(
				matrixId,
				censusOPId,
				hwOPId,
				gwasParams);

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="DATA MANAGEMENT">
	public static void doExtractData(
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doTranslateAB12ToACGT(
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doExportMatrix(
			final int studyId,
			final int matrixId,
			final ExportFormat format,
			final String phenotype)
	{
		CommonRunnable task = new Threaded_ExportMatrix(
				matrixId,
				format,
				phenotype);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doMergeMatrixAddMarkers(
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doMergeMatrixAddSamples(
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doMergeMatrixAll(final int studyId,
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doStrandFlipMatrix(
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

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void updateSampleInfo(
			final int studyId,
			final File sampleInfoFile)
	{
		CommonRunnable task = new Threaded_UpdateSampleInfo(
				studyId,
				sampleInfoFile);

		SwingWorkerItem swi = new SwingWorkerItem(
				task,
				new Integer[]{studyId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="DELETERS">
	public static void deleteStudy(final int studyId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.STUDY,
				studyId,
				deleteReports);
		SwingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void deleteMatrix(final int studyId, final int matrixId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.MATRIX,
				studyId,
				matrixId,
				deleteReports);
		SwingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void deleteOperationsByOpId(final int studyId, final int matrixId, final int opId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.OPERATION_BY_OPID,
				studyId,
				matrixId,
				opId,
				deleteReports);
		SwingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
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
		if (StartGWASpi.guiMode) {
			ProcessTab.getSingleton().updateProcessOverview();
			ProcessTab.getSingleton().toggleBusyLogo();
		}
	}

	public static void updateProcessOverviewDeleteNext() throws IOException {
		SwingDeleterItemList.deleteAllListed();
		if (StartGWASpi.guiMode) {
			ProcessTab.getSingleton().updateProcessOverview();
			ProcessTab.getSingleton().toggleBusyLogo();
		}
	}
	//</editor-fold>
}
