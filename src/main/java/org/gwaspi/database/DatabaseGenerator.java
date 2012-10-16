package org.gwaspi.database;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.ReportsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SampleManager;

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

	public static String initDataCenter() throws IOException {
		String allResults = "";
		DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
		db.createSchema(cDBGWASpi.SCH_MARKERS);
		db.createSchema(cDBGWASpi.SCH_SAMPLES);
		//db.createSchema(org.gwaspi.database.cDBMoapi.SCH_MATRICES);

		// MOAPI GENERIC TABLES
		allResults += createStatusTypes(db);

		allResults += SampleManager.createSamplesInfoTable(db);

		allResults += MatricesList.createMatricesTable(db);

		allResults += OperationsList.createOperationsMetadataTable(db);

		allResults += ReportsList.createReportsMetadataTable(db);

		// STUDY_0 SPECIFIC DATA
		Object[] testStudy = new Object[]{
			"Study 1",
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
			db.createTable(cDBGWASpi.SCH_APP,
					cDBGWASpi.T_STATUS_TYPES,
					cDBGWASpi.T_CREATE_STATUS_TYPES);

			db.executeStatement(cDBGWASpi.IE_STATUS_TYPES_INIT);
			result = true;
		} catch (Exception ex) {
			log.error("Error creating management database", ex);
		}

		return (result ? "1" : "0");
	}
}
