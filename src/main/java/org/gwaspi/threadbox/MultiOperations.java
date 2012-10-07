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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_MatrixQA thread = new Threaded_MatrixQA(
						timeStamp,
						matrixId);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Matrix QA & Reports on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();

		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_Loader_GWASifOK thread = new Threaded_Loader_GWASifOK(
						timeStamp,
						loadDescription,
						dummySamples,
						performGwas,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Genotypes Loader & GWAS if OK: " + loadDescription.getFriendlyName(),
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GWAS thread = new Threaded_GWAS(
						timeStamp,
						matrixId,
						phenofile,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"GWAS on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_HardyWeinberg thread = new Threaded_HardyWeinberg(
						timeStamp,
						matrixId,
						censusOpId);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Hardy-Weinberg on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GTFreq_HW thread = new Threaded_GTFreq_HW(
						timeStamp,
						matrixId,
						phenoFile,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Genotypes Freq. & HW on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_AllelicAssociation thread = new Threaded_AllelicAssociation(
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				"Allelic Association Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GenotypicAssociation thread = new Threaded_GenotypicAssociation(
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				"Genotypic Association Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_TrendTest thread = new Threaded_TrendTest(
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);
				thread.startThreaded();

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem(
				"Cochran-Armitage Trend Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_ExtractMatrix thread = new Threaded_ExtractMatrix(
						timeStamp,
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
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Data Extract: " + newMatrixName,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_TranslateMatrix thread = new Threaded_TranslateMatrix(
						timeStamp,
						studyId,
						parentMatrixId,
						gtEncoding,
						newMatrixName,
						description);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Translate Matrix: " + newMatrixName,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_ExportMatrix thread = new Threaded_ExportMatrix(
						timeStamp,
						matrixId,
						format,
						phenotype);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Export Matrix ID: " + matrixId,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_MergeMatricesAddMarkers thread = new Threaded_MergeMatricesAddMarkers(
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_MergeMatricesAddSamples thread = new Threaded_MergeMatricesAddSamples(
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_MergeMatricesAddAll thread = new Threaded_MergeMatricesAddAll(
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
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
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_FlipStrandMatrix thread = new Threaded_FlipStrandMatrix(
						timeStamp,
						studyId,
						parentMatrixId,
						newMatrixName,
						description,
						markerIdentifyer,
						markerFlipFile);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Flip Strand Matrix ID: " + parentMatrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		SwingWorkerItemList.add(swi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void updateSampleInfo(
			final int studyId,
			final File sampleInfoFile)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_UpdateSampleInfo thread = new Threaded_UpdateSampleInfo(
						timeStamp,
						studyId,
						sampleInfoFile);
				thread.startThreaded();

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Update Sample Info on Study ID: " + studyId,
				worker,
				timeStamp,
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

	public static void refreshPanel() {
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
