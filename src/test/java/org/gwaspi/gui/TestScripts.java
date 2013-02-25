package org.gwaspi.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cImport;
import org.gwaspi.threadbox.SwingDeleterItemList;
import org.gwaspi.threadbox.SwingWorkerItemList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hoijui
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestScripts {

	private static final Logger log = LoggerFactory.getLogger(TestScripts.class);

	private static Setup setup = null;

	private static int lastLoadedMatrixId = -1;
	private static Map<String, Integer> fileNameLoadedMatrixId = new HashMap<String, Integer>();


	private static void copyFile(URL srcFile, File dstFile, Map<String, String> substitutions) throws IOException {

		BufferedReader src = new BufferedReader(new InputStreamReader(srcFile.openStream()));

		OutputStreamWriter dst = new OutputStreamWriter(new FileOutputStream(dstFile));

		String line = src.readLine();
		while (line != null) {
			for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
				line = line.replaceAll(substitution.getKey(), substitution.getValue());
			}
			dst.write(line, 0, line.length());
			dst.write('\n');

			line = src.readLine();
		}

		src.close();
		dst.close();
	}

	private static void compareFiles(File origFile, File compareFile) throws IOException {

		BufferedReader orig = new BufferedReader(new FileReader(origFile));
		BufferedReader compare = new BufferedReader(new FileReader(compareFile));

		String origLine = orig.readLine();
		String compareLine = compare.readLine();
		while (origLine != null) {
			if (compareLine == null) {
				throw new IOException("the file to compare with is shorter then the original file");
			}
			if (!origLine.equals(compareLine)) {
				throw new IOException("the files are not equal");
			}

			origLine = orig.readLine();
			compareLine = compare.readLine();
		}
		if (compareLine != null) {
			throw new IOException("the file to compare with is longer then the original file");
		}

		orig.close();
		compare.close();
	}

	private static class Setup {

		/**
		 * Temporary data-dir, only valid for a single unit-test-suite run.
		 */
		private File dbDataDir = null;
		/**
		 * Where the application exports data to.
		 */
		private File exportDir = null;
		/**
		 * Where we store temporary files to.
		 */
		private File tmpDir = null;
		/**
		 * Where we store temporary script files to.
		 */
		private File scriptsDir = null;

		Setup(File dbDataDir, File exportDir, File tmpDir, File scriptsDir) {

			this.dbDataDir = dbDataDir;
			this.exportDir = exportDir;
			this.tmpDir = tmpDir;
			this.scriptsDir = scriptsDir;
		}

		public static Setup createTemp() throws IOException {

			File dbDataDir = File.createTempFile("gwaspi_dbData_dir_", null);
			dbDataDir.delete();
			File exportDir = new File(dbDataDir, "export");

			File tmpDataDir = File.createTempFile("gwaspi_tmpData_dir_", null);
			tmpDataDir.delete();
			tmpDataDir.mkdir();
			File scriptsDir = new File(tmpDataDir, "scripts");
			scriptsDir.mkdir();

			return new Setup(dbDataDir, exportDir, tmpDataDir, scriptsDir);
		}

		public static void deleteDirRecursively(File toBeDeleted) throws IOException {

			if (toBeDeleted.isFile()) {
				toBeDeleted.delete();
			} else if (toBeDeleted.isDirectory()) {
				for (File containedFile : toBeDeleted.listFiles()) {
					deleteDirRecursively(containedFile);
				}
				toBeDeleted.delete();
			}
		}

		public void cleanupTemp() throws IOException {

			deleteDirRecursively(getDbDataDir());
			deleteDirRecursively(getTmpDir());
		}

		public File getDbDataDir() {
			return dbDataDir;
		}

		public File getExportDir() {
			return exportDir;
		}

		public File getTmpDir() {
			return tmpDir;
		}

		public File getScriptsDir() {
			return scriptsDir;
		}
	}


	@BeforeClass
	public static void createTempDataDirs() throws IOException {

		setup = Setup.createTemp();
	}

	@AfterClass
	public static void cleanupTempDataDirs() throws IOException {

		setup.cleanupTemp();
		setup = null;
	}

	private void startGWASpi(String[] args) throws Exception {

//		StartGWASpi.main(args); // NOTE overrides all args!!

// FIXME BAD THREADDING!!! fix it first, and then all will resolve into wohlgefallen!
		StartGWASpi startGWASpi = new StartGWASpi();
		startGWASpi.start(Arrays.asList(args));

		int sum = 999;
		do {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			sum = SwingWorkerItemList.sizePending() + SwingDeleterItemList.size();
		} while (sum > 0);
	}

	private String[] createArgs(String scriptPath, String logPath) {

		String[] args = ("script " + scriptPath + " log " + logPath).split(" ");

		return args;
	}

	private void testLoadPlinkBinary(String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK_Binary.name() + "." + name;

		String bedFileName = name + ".bed";
		String bimFileName = name + ".bim";
		String famFileName = name + ".fam";

		log.info("Load from PLINK Binary ({}, {}, {}) ...", bedFileName, famFileName, bimFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_loadPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/binary/";
		URL plinkBinaryBed = TestScripts.class.getResource(formatBasePath + bedFileName);
		URL plinkBinaryBim = TestScripts.class.getResource(formatBasePath + bimFileName);
		URL plinkBinaryFam = TestScripts.class.getResource(formatBasePath + famFileName);
		URL plinkLoadScript = TestScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File bedFile = new File(setup.getScriptsDir(), bedFileName);
		File bimFile = new File(setup.getScriptsDir(), bimFileName);
		File famFile = new File(setup.getScriptsDir(), famFileName);
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		copyFile(plinkBinaryBed, bedFile, substitutions);
		copyFile(plinkBinaryBim, bimFile, substitutions);
		copyFile(plinkBinaryFam, famFile, substitutions);
		substitutions.put("\\$\\{DATA_DIR\\}", setup.dbDataDir.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_1\\}", bedFile.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_2\\}", bimFile.getAbsolutePath());
		substitutions.put("\\$\\{SAMPLE_INFO_FILE\\}", famFile.getAbsolutePath());
		substitutions.put("\\$\\{MATRIX_NAME\\}", matrixName);
		substitutions.put("\\$\\{FORMAT\\}", cImport.ImportFormat.PLINK_Binary.name());
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_loadPlinkFlat_" + bedFileName + "_" + famFileName + "_" + bimFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		if (fileNameLoadedMatrixId.get(matrixName) == null) {
			fileNameLoadedMatrixId.put(matrixName, ++lastLoadedMatrixId + 1);
		}

		log.info("Load from PLINK Binary DONE.");
	}

	private void testLoadPlinkFlat(String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK.name() + "." + name;

		String mapFileName = name + ".map";
		String pedFileName = name + ".ped";

		log.info("Load from PLINK Flat ({}, {}) ...", mapFileName, pedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_loadPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/flat/";
		URL plinkFlatMap = TestScripts.class.getResource(formatBasePath + mapFileName);
		URL plinkFlatPed = TestScripts.class.getResource(formatBasePath + pedFileName);
		URL plinkLoadScript = TestScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File mapFile = new File(setup.getScriptsDir(), mapFileName);
		File pedFile = new File(setup.getScriptsDir(), pedFileName);
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		copyFile(plinkFlatMap, mapFile, substitutions);
		copyFile(plinkFlatPed, pedFile, substitutions);
		substitutions.put("\\$\\{DATA_DIR\\}", setup.dbDataDir.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_1\\}", mapFile.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_2\\}", pedFile.getAbsolutePath());
		substitutions.put("\\$\\{SAMPLE_INFO_FILE\\}", "no info file");
		substitutions.put("\\$\\{MATRIX_NAME\\}", matrixName);
		substitutions.put("\\$\\{FORMAT\\}", cImport.ImportFormat.PLINK.name());
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_loadPlinkFlat_" + mapFileName + "_" + pedFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		if (fileNameLoadedMatrixId.get(matrixName) == null) {
			fileNameLoadedMatrixId.put(matrixName, ++lastLoadedMatrixId + 1);
		}

		log.info("Load from PLINK Flat DONE.");
	}

	private void testExportPlinkFlat(String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK.name() + "." + name;

		if (fileNameLoadedMatrixId.get(matrixName) == null) {
			testLoadPlinkFlat(name);
		}
		int matrixId = fileNameLoadedMatrixId.get(matrixName);

		String compareMapFileName = name + ".map";
		String comparePedFileName = name + ".ped";

		log.info("Export into PLINK Flat ({}, {}) ...", compareMapFileName, comparePedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_exportPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/flat/";
		URL plinkFlatMap = TestScripts.class.getResource(formatBasePath + compareMapFileName);
		URL plinkFlatPed = TestScripts.class.getResource(formatBasePath + comparePedFileName);
		URL plinkLoadScript = TestScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File mapFile = new File(setup.getScriptsDir(), compareMapFileName);
		File pedFile = new File(setup.getScriptsDir(), comparePedFileName);
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		copyFile(plinkFlatMap, mapFile, substitutions);
		copyFile(plinkFlatPed, pedFile, substitutions);
		substitutions.put("\\$\\{DATA_DIR\\}", setup.dbDataDir.getAbsolutePath());
		substitutions.put("\\$\\{MATRIX_ID\\}", String.valueOf(matrixId));
		substitutions.put("\\$\\{FORMAT\\}", cExport.ExportFormat.PLINK.name());
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_exportPlinkFlat_" + compareMapFileName + "_" + comparePedFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		File outputMapFileName = new File(setup.getExportDir(), "STUDY_1/" + matrixName + ".map");
		File outputPedFileName = new File(setup.getExportDir(), "STUDY_1/" + matrixName + ".ped");

		// compare the export results with the references
		compareFiles(mapFile, outputMapFileName);
		compareFiles(pedFile, outputPedFileName);

		log.info("Export into PLINK Flat DONE.");
	}

	@Test
	public void testExportPlinkFlatGwaspi() throws Exception {

		testExportPlinkFlat("minimalGwaspi");
	}

	@Test
	public void testExportPlinkFlatPlink() throws Exception {

		testExportPlinkFlat("minimalPlink");
	}

	/**
	 * Tests loading of the (minimal) Plink (Flat format) samples
	 * from the GWASpi home-page.
	 */
	@Test
	public void testLoadPlinkFlatMinimalGwaspi() throws Exception {

		testLoadPlinkFlat("minimalGwaspi");
	}

	/**
	 * Tests loading of the (minimal) Plink (Flat format) samples
	 * from the Plink home-page.
	 */
	@Test
	public void testLoadPlinkFlatMinimalPlink() throws Exception {

		testLoadPlinkFlat("minimalPlink");
	}

	/**
	 * Tests loading of the (minimal) Plink samples
	 * from the Plink home-page, converted from Flat to Binary format.
	 */
	@org.junit.Ignore
	@Test
	public void testLoadPlinkBinaryMinimalPlink() throws Exception {

		testLoadPlinkBinary("minimalPlink");
	}
}
