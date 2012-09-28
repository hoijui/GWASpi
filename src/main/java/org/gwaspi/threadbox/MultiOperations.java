package org.gwaspi.threadbox;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
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

	private static SwingWorkerItemList swingWorkerItemList = new SwingWorkerItemList();
	private static SwingDeleterItemList swingDeleterItemList = new SwingDeleterItemList();

	private MultiOperations() {
	}

	//<editor-fold defaultstate="collapsed" desc="LOADERS">
	public static void doMatrixQAs(final int studyId, final int matrixId) {

		// SAMPLES QA
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_MatrixQA sampleQA = new Threaded_MatrixQA("Matrix QA & Reports",
						timeStamp,
						matrixId);
				return sampleQA;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Matrix QA & Reports on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void loadMatrixDoGWASifOK(
			final GenotypesLoadDescription loadDescription,
			final boolean dummySamples,
			final int decision, // FIXME, use boolean instead!
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS if requested and OK
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();

		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_Loader_GWASifOK thread = new Threaded_Loader_GWASifOK(
						"Genotypes Loader & GWAS if OK",
						timeStamp,
						loadDescription,
						dummySamples,
						decision,
						gwasParams);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem(
				"Genotypes Loader & GWAS if OK: " + loadDescription.getFriendlyName(),
				worker,
				timeStamp,
				new Integer[]{loadDescription.getStudyId()});

		swingWorkerItemList.add(
				swi,
				new Integer[]{loadDescription.getStudyId()}, // Studies to be put on hold
				null, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="ANALYSIS">
	public static void doGWASwithAlterPhenotype(final int studyId,
			final int matrixId,
			final File phenofile,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GWAS thread = new Threaded_GWAS("GWAS",
						timeStamp,
						matrixId,
						phenofile,
						gwasParams);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("GWAS on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doHardyWeinberg(final int studyId,
			final int matrixId,
			final int censusOpId)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_HardyWeinberg thread = new Threaded_HardyWeinberg("Hardy-Weinberg",
						timeStamp,
						matrixId,
						censusOpId);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Hardy-Weinberg on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doGTFreqDoHW(final int studyId,
			final int matrixId,
			final File phenoFile,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GTFreq_HW thread = new Threaded_GTFreq_HW("GT Freq. & HW",
						timeStamp,
						matrixId,
						phenoFile,
						gwasParams);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Genotypes Freq. & HW on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doAllelicAssociationTest(final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_AllelicAssociation thread = new Threaded_AllelicAssociation("Allelic Association Test",
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem("Allelic Association Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				new Integer[]{censusOPId, hwOPId}); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doGenotypicAssociationTest(final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_GenotypicAssociation thread = new Threaded_GenotypicAssociation("Genotypic Association Test",
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem("Genotypic Association Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				new Integer[]{censusOPId, hwOPId}); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doTrendTest(final int studyId,
			final int matrixId,
			final int censusOPId,
			final int hwOPId,
			final GWASinOneGOParams gwasParams)
	{
		// LOAD & GWAS
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				Threaded_TrendTest thread = new Threaded_TrendTest("Cochran-Armitage Trend Test",
						timeStamp,
						matrixId,
						censusOPId,
						hwOPId,
						gwasParams);

				return thread;
			}
		};

//		List<Integer> holdOpIds = new ArrayList<Integer>();
//		holdOpIds.add(censusOPId);
//		holdOpIds.add(hwOPId);

		SwingWorkerItem swi = new SwingWorkerItem("Cochran-Armitage Trend Test on Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId},
				new Integer[]{censusOPId, hwOPId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				new Integer[]{censusOPId, hwOPId}); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed/expanded" desc="DATA MANAGEMENT">
	public static void doExtractData(final int studyId,
			final int parentMatrixId,
			final String newMatrixName,
			final String description,
			final cNetCDF.Defaults.SetMarkerPickCase markerPickCase,
			final cNetCDF.Defaults.SetSamplePickCase samplePickCase,
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

				Threaded_ExtractMatrix thread = new Threaded_ExtractMatrix("Data Extract",
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

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Data Extract: " + newMatrixName,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doTranslateAB12ToACGT(final int studyId,
			final int parentMatrixId,
			final cNetCDF.Defaults.GenotypeEncoding gtEncoding,
			final String newMatrixName,
			final String description)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_TranslateMatrix thread = new Threaded_TranslateMatrix("Translate Matrix",
						timeStamp,
						studyId,
						parentMatrixId,
						gtEncoding,
						newMatrixName,
						description);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Translate Matrix: " + newMatrixName,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doExportMatrix(final int studyId,
			final int matrixId,
			final ExportFormat format,
			final String phenotype)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_ExportMatrix thread = new Threaded_ExportMatrix("Export Matrix",
						timeStamp,
						matrixId,
						format,
						phenotype);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Export Matrix ID: " + matrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{matrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{matrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doMergeMatrixAddMarkers(final int studyId,
			final int parentMatrixId1,
			final int parentMatrixId2,
			final String newMatrixName,
			final String description)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_MergeMatricesAddMarkers thread = new Threaded_MergeMatricesAddMarkers("Merge Matrices",
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId1, parentMatrixId2}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doMergeMatrixAddSamples(final int studyId,
			final int parentMatrixId1,
			final int parentMatrixId2,
			final String newMatrixName,
			final String description)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_MergeMatricesAddSamples thread = new Threaded_MergeMatricesAddSamples("Merge Matrices",
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId1, parentMatrixId2}, // Matrices to be put on hold
				null); // Operations to be put on hold

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

				Threaded_MergeMatricesAddAll thread = new Threaded_MergeMatricesAddAll("Merge Matrices",
						timeStamp,
						studyId,
						parentMatrixId1,
						parentMatrixId2,
						newMatrixName,
						description);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Merge Matrices: " + newMatrixName,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId1, parentMatrixId2});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId1, parentMatrixId2}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void doStrandFlipMatrix(final int studyId,
			final int parentMatrixId,
			final String markerIdentifyer,
			final File markerFlipFile,
			final String newMatrixName,
			final String description)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_FlipStrandMatrix thread = new Threaded_FlipStrandMatrix("Flip Strand Matrix",
						timeStamp,
						studyId,
						parentMatrixId,
						newMatrixName,
						description,
						markerIdentifyer,
						markerFlipFile);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Flip Strand Matrix ID: " + parentMatrixId,
				worker,
				timeStamp,
				new Integer[]{studyId},
				new Integer[]{parentMatrixId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				new Integer[]{parentMatrixId}, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void updateSampleInfo(final int studyId,
			final File sampleInfoFile)
	{
		final String timeStamp = org.gwaspi.global.Utils.getTimeStamp();
		SwingWorker worker = new SwingWorker() {
			public Object construct() {

				Threaded_UpdateSampleInfo thread = new Threaded_UpdateSampleInfo("Update Sample Info",
						timeStamp,
						studyId,
						sampleInfoFile);

				return thread;
			}
		};

		SwingWorkerItem swi = new SwingWorkerItem("Update Sample Info on Study ID: " + studyId,
				worker,
				timeStamp,
				new Integer[]{studyId});
		swingWorkerItemList.add(swi,
				new Integer[]{studyId}, // Studies to be put on hold
				null, // Matrices to be put on hold
				null); // Operations to be put on hold

		ProcessTab.getSingleton().updateProcessOverview();
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="DELETERS">
	public static void deleteStudy(final int studyId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.STUDY,
				studyId,
				deleteReports);
		swingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void deleteMatrix(final int studyId, final int matrixId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.MATRIX,
				studyId,
				matrixId,
				deleteReports);
		swingDeleterItemList.add(sdi);

		ProcessTab.getSingleton().updateProcessOverview();
	}

	public static void deleteOperationsByOpId(final int studyId, final int matrixId, final int opId, final boolean deleteReports) {

		SwingDeleterItem sdi = new SwingDeleterItem(SwingDeleterItem.DeleteTarget.OPERATION_BY_OPID,
				studyId,
				matrixId,
				opId,
				deleteReports);
		swingDeleterItemList.add(sdi);

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
