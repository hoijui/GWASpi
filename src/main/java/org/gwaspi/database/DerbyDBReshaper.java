package org.gwaspi.database;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cGlobal;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.XMLParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author u56124
 */
public class DerbyDBReshaper {

	private static final Logger log = LoggerFactory.getLogger(DerbyDBReshaper.class);

	private DerbyDBReshaper() {
	}

	public static void alterTableUpdates() throws URISyntaxException, ParseException, IOException {

		DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);

		URL localVersionPath = DerbyDBReshaper.class.getClass().getResource(cGlobal.LOCAL_VERSION_XML);
		Document localDom = XMLParser.parseXmlFile(localVersionPath.toURI().toString());

		if (localDom != null) { // Found local version info
			// Retrieve data from XML files
			List<Element> localElements = XMLParser.parseDocument(localDom, "GWASpi");
			Date localUpdateDate = XMLParser.getDateValue(localElements.get(0), "Date");
			String localVersionNumber = XMLParser.getTextValue(localElements.get(0), "Number");
			// MAKE VERSION CHECKS

			//<editor-fold defaultstate="collapsed" desc="IF < 2.0.1">
			if (localVersionNumber.compareTo("2.0.1") < 0) { // Remote version is still compatible with local version
				log.info("Updating Sample ID field size");
				StringBuilder alterSQLprefix = new StringBuilder("ALTER TABLE ");
				alterSQLprefix.append(cDBGWASpi.SCH_SAMPLES);
				alterSQLprefix.append(".");
				alterSQLprefix.append(cDBSamples.T_SAMPLES_INFO);

				StringBuilder alterSQLsuffix = new StringBuilder(" ALTER ");
				alterSQLsuffix.append(cDBSamples.f_SAMPLE_ID).append(" SET DATA TYPE VARCHAR(64)");
				try {
					db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
				} catch (Exception ex) {
					log.error("Failed to update Sample ID field size", ex);
				}

				alterSQLsuffix = new StringBuilder(" ALTER ");
				alterSQLsuffix.append(cDBSamples.f_FATHER_ID).append(" SET DATA TYPE VARCHAR(64)");
				try {
					db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
				} catch (Exception ex) {
					log.error("Failed to update Sample ID field size", ex);
				}

				alterSQLsuffix = new StringBuilder(" ALTER ");
				alterSQLsuffix.append(cDBSamples.f_MOTHER_ID).append(" SET DATA TYPE VARCHAR(64)");
				try {
					db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
				} catch (Exception ex) {
					log.error("Failed to update Sample ID field size", ex);
				}
			}
			//</editor-fold>
		}
	}
}
