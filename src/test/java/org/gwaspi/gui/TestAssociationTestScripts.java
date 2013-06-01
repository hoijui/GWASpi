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

package org.gwaspi.gui;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAssociationTestScripts extends AbstractTestScripts {

	private static final Logger log = LoggerFactory.getLogger(TestAssociationTestScripts.class);

	private static void testHardyWeinbergTest(Setup setup, String name) throws Exception {

		String matrixName = TestLoadAndExportScripts.testLoadPlinkFlat(setup, name);
		int matrixId = setup.getMatrixIds().get(matrixName);
		MatrixKey matrixKey = new MatrixKey(new StudyKey(setup.getStudyId()), matrixId);

		String mapFileName = name + ".map";
		String pedFileName = name + ".ped";

		log.info("Run Hardy-Weinberg Test ({}, {}) ...", mapFileName, pedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_hardyWeinberg.txt";

		// original resource files used during the test run
		URL plinkLoadScript = TestAssociationTestScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
		substitutions.put("\\$\\{STUDY_ID\\}", String.valueOf(setup.getStudyId()));
		substitutions.put("\\$\\{MATRIX_ID\\}", String.valueOf(matrixId));
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_hardyWeinberg_" + mapFileName + "_" + pedFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		log.info("Run Hardy-Weinberg Test ({}, {}) DONE.", mapFileName, pedFileName);

		List<OperationMetadata> operationsTable = OperationsList.getOperationsTable(matrixKey);
		log.info("available opeartions:");
		for (OperationMetadata operationMetadata : operationsTable) {
			log.info("\toperation id: {}, name: \"{}\"",
					operationMetadata.getId(),
					operationMetadata.getOPName());
		}
	}

	private static void testCombiAssociationTest(Setup setup, String name) throws Exception {

		String matrixName = TestLoadAndExportScripts.testLoadPlinkFlat(setup, name);
		int matrixId = setup.getMatrixIds().get(matrixName);
//		int gtFreqOpId = 3; // FIXME should not be hardcoded
//		int hwOpId = 4; // FIXME should not be hardcoded

		String mapFileName = name + ".map";
		String pedFileName = name + ".ped";

		log.info("Run Combi Association Test ({}, {}) ...", mapFileName, pedFileName);

		String resBasePath = "/samples/";
		String scriptFileName = "gwaspiScript_combiAssociation.txt";

		// original resource files used during the test run
		URL plinkLoadScript = TestAssociationTestScripts.class.getResource(resBasePath + scriptFileName);

		// paths of the temporary file copies
		File scriptFile = new File(setup.getScriptsDir(), scriptFileName);

		// copy all the files used during the run to a temp dir
		// so we are independent of the storage type of these files
		// for example, in case they are packaged in a jar, originally
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("\\$\\{DATA_DIR\\}", setup.getDbDataDir().getAbsolutePath());
		substitutions.put("\\$\\{STUDY_ID\\}", String.valueOf(setup.getStudyId()));
		substitutions.put("\\$\\{MATRIX_ID\\}", String.valueOf(matrixId));
//		substitutions.put("\\$\\{GENOTYPE_FREQUENCY_OPERATION_ID\\}", String.valueOf(gtFreqOpId));
//		substitutions.put("\\$\\{HARDY_WEINBERG_OPERATION_ID\\}", String.valueOf(hwOpId));
		copyFile(plinkLoadScript, scriptFile, substitutions);

		File logFile = new File(setup.getTmpDir(), "log_test_combiAssociation_" + mapFileName + "_" + pedFileName + ".txt");

		startGWASpi(createArgs(scriptFile.getAbsolutePath(), logFile.getAbsolutePath()));

		log.info("Run Combi Association Test ({}, {}) DONE.", mapFileName, pedFileName);
	}

	/**
	 * Runs the Combi association Test on the "extra" dataset.
	 */
	@Test
	public void testCombiAssociationTest() throws Exception {

//		testHardyWeinbergTest(getSetup(), "extra");
		testCombiAssociationTest(getSetup(), "extra");
	}

	@org.junit.Ignore
	@Test
	public void testHardyWeinbergTest() throws Exception {

		testHardyWeinbergTest(getSetup(), "extra");
	}
}
