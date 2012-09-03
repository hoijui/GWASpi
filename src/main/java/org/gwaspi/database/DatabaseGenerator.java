package org.gwaspi.database;

import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class DatabaseGenerator {

	private static final Logger log = LoggerFactory.getLogger(DatabaseGenerator.class);

	private DatabaseGenerator() {
	}

	//private static DbManager db;
	public static String initDataCenter() throws IOException {
		String allResults = "";
		DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
		db.createSchema(org.gwaspi.constants.cDBGWASpi.SCH_MARKERS);
		db.createSchema(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES);
		//db.createSchema(org.gwaspi.database.cDBMoapi.SCH_MATRICES);

		// MOAPI GENERIC TABLES
		allResults += createStatusTypes(db);

		allResults += org.gwaspi.samples.SampleManager.createSamplesInfoTable(db);

		allResults += org.gwaspi.netCDF.matrices.MatrixManager.createMatricesTable(db);

		allResults += org.gwaspi.netCDF.operations.OperationManager.createOperationsMetadataTable(db);

		allResults += org.gwaspi.reports.ReportManager.createReportsMetadataTable(db);

		// STUDY_0 SPECIFIC DATA
		Object[] testStudy = new Object[]{"Study 1",
			"",
			"",
			"0"};
		allResults += StudyGenerator.createStudyManagementTable(db, testStudy);

		return allResults;
	}

	public static String createStatusTypes(DbManager db) {
		boolean result = false;
		try {
			// CREATE STATUS_TYPES table in APP SCHEMA and fill with init data
			db.createTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STATUS_TYPES,
					org.gwaspi.constants.cDBGWASpi.T_CREATE_STATUS_TYPES);

			db.executeStatement(org.gwaspi.constants.cDBGWASpi.IE_STATUS_TYPES_INIT);
		} catch (Exception ex) {
			log.error("Error creating management database", ex);
		}

		return (result) ? "1" : "0";
	}
}
