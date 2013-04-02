package org.gwaspi.gui;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cImport;
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
public class TestLoadAndExportScripts extends AbstractTestScripts {

	private static final Logger log = LoggerFactory.getLogger(TestLoadAndExportScripts.class);

	private static void testLoadPlinkBinary(Setup setup, String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK_Binary.name() + "." + name;

		String bedFileName = name + ".bed";
		String bimFileName = name + ".bim";
		String famFileName = name + ".fam";

		log.info("Load from PLINK Binary ({}, {}, {}) ...", bedFileName, famFileName, bimFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_loadPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/binary/";
		URL plinkBinaryBed = TestLoadAndExportScripts.class.getResource(formatBasePath + bedFileName);
		URL plinkBinaryBim = TestLoadAndExportScripts.class.getResource(formatBasePath + bimFileName);
		URL plinkBinaryFam = TestLoadAndExportScripts.class.getResource(formatBasePath + famFileName);
		URL plinkLoadScript = TestLoadAndExportScripts.class.getResource(resBasePath + scriptFileName);

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
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_1\\}", bedFile.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_2\\}", bimFile.getAbsolutePath());
		substitutions.put("\\$\\{SAMPLE_INFO_FILE\\}", famFile.getAbsolutePath());
		substitutions.put("\\$\\{MATRIX_NAME\\}", matrixName);
		substitutions.put("\\$\\{FORMAT\\}", cImport.ImportFormat.PLINK_Binary.name());
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_loadPlinkFlat_" + bedFileName + "_" + famFileName + "_" + bimFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		setup.addLoadedFileName(matrixName);

		log.info("Load from PLINK Binary DONE.");
	}

	static void testLoadPlinkFlat(Setup setup, String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK.name() + "." + name;

		String mapFileName = name + ".map";
		String pedFileName = name + ".ped";

		log.info("Load from PLINK Flat ({}, {}) ...", mapFileName, pedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_loadPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/flat/";
		URL plinkFlatMap = TestLoadAndExportScripts.class.getResource(formatBasePath + mapFileName);
		URL plinkFlatPed = TestLoadAndExportScripts.class.getResource(formatBasePath + pedFileName);
		URL plinkLoadScript = TestLoadAndExportScripts.class.getResource(resBasePath + scriptFileName);

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
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_1\\}", mapFile.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_2\\}", pedFile.getAbsolutePath());
		substitutions.put("\\$\\{SAMPLE_INFO_FILE\\}", "no info file");
		substitutions.put("\\$\\{MATRIX_NAME\\}", matrixName);
		substitutions.put("\\$\\{FORMAT\\}", cImport.ImportFormat.PLINK.name());
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_loadPlinkFlat_" + mapFileName + "_" + pedFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		setup.addLoadedFileName(matrixName);

		log.info("Load from PLINK Flat DONE.");
	}

	private static void testExportPlinkFlat(Setup setup, String name) throws Exception {

		String matrixName = cImport.ImportFormat.PLINK.name() + "." + name;

		if (!setup.getMatrixIds().containsKey(matrixName)) {
			testLoadPlinkFlat(setup, name);
		}
		int matrixId = setup.getMatrixIds().get(matrixName);

		String compareMapFileName = name + ".map";
		String comparePedFileName = name + ".ped";

		log.info("Export into PLINK Flat ({}, {}) ...", compareMapFileName, comparePedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_exportPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "plink/flat/";
		URL plinkFlatMap = TestLoadAndExportScripts.class.getResource(formatBasePath + compareMapFileName);
		URL plinkFlatPed = TestLoadAndExportScripts.class.getResource(formatBasePath + comparePedFileName);
		URL plinkLoadScript = TestLoadAndExportScripts.class.getResource(resBasePath + scriptFileName);

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
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
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

	private void testLoadHGDP1(Setup setup, String name) throws Exception {

		String matrixName = cImport.ImportFormat.HGDP1.name() + "." + name;

		String markersFileName = name + ".markers.txt";
		String samplesFileName = name + ".samples.txt";

		log.info("Load from HGDP1 ({}, {}) ...", markersFileName, samplesFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_loadPlink.txt";

		// original resource files used during the test run
		String formatBasePath = resBasePath + "hgdp1/";
		URL plinkFlatMap = TestLoadAndExportScripts.class.getResource(formatBasePath + markersFileName);
		URL plinkFlatPed = TestLoadAndExportScripts.class.getResource(formatBasePath + samplesFileName);
		URL plinkLoadScript = TestLoadAndExportScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File markersFile = new File(setup.getScriptsDir(), markersFileName);
		File samplesFile = new File(setup.getScriptsDir(), samplesFileName);
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		copyFile(plinkFlatMap, markersFile, substitutions);
		copyFile(plinkFlatPed, samplesFile, substitutions);
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_1\\}", markersFile.getAbsolutePath());
		substitutions.put("\\$\\{IN_FILE_2\\}", samplesFile.getAbsolutePath());
		substitutions.put("\\$\\{MATRIX_NAME\\}", matrixName);
		substitutions.put("\\$\\{FORMAT\\}", cImport.ImportFormat.HGDP1.name());
		substitutions.put("\\$\\{SAMPLE_INFO_FILE\\}", "no info file");
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_loadPlinkFlat_" + markersFileName + "_" + samplesFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		setup.addLoadedFileName(matrixName);

		log.info("Load from HGDP1 DONE.");
	}

	@Test
	public void testExportPlinkFlatGwaspi() throws Exception {

		testExportPlinkFlat(getSetup(), "minimalGwaspi");
	}

	@Test
	public void testExportPlinkFlatPlink() throws Exception {

		testExportPlinkFlat(getSetup(), "minimalPlink");
	}

	/**
	 * Tests loading of the (minimal) Plink (Flat format) samples
	 * from the GWASpi home-page.
	 */
	@Test
	public void testLoadPlinkFlatMinimalGwaspi() throws Exception {

		testLoadPlinkFlat(getSetup(), "minimalGwaspi");
	}

	/**
	 * Tests loading of the (minimal) Plink (Flat format) samples
	 * from the Plink home-page.
	 */
	@Test
	public void testLoadPlinkFlatMinimalPlink() throws Exception {

		testLoadPlinkFlat(getSetup(), "minimalPlink");
	}

	/**
	 * Tests loading of the (minimal) Plink samples
	 * from the Plink home-page, converted from Flat to Binary format.
	 */
	@org.junit.Ignore
	@Test
	public void testLoadPlinkBinaryMinimalPlink() throws Exception {

		testLoadPlinkBinary(getSetup(), "minimalPlink");
	}

	/**
	 * Tests loading of the (minimal) HGDP1 samples
	 * from the GWASpi home-page.
	 */
	@org.junit.Ignore
	@Test
	public void testLoadHGDP1MinimalGwaspi() throws Exception {

		testLoadHGDP1(getSetup(), "minimalGwaspi");
	}
}
