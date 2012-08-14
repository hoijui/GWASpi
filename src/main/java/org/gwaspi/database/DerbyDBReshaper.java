/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.database;

import org.gwaspi.global.ServiceLocator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author u56124
 */
public class DerbyDBReshaper {
    
    public static void alterTableUpdates() throws URISyntaxException, ParseException, IOException{

        DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);

        URL localVersionPath = DerbyDBReshaper.class.getClass().getResource(org.gwaspi.constants.cGlobal.LOCAL_VERSION_XML);
        Document localDom = org.gwaspi.global.XMLParser.parseXmlFile(localVersionPath.toURI().toString());

        if(localDom!=null){ //Found local version info
            //Retrieve data from XML files
            ArrayList<Element> localElements = org.gwaspi.global.XMLParser.parseDocument(localDom, "GWASpi");
            Date localUpdateDate = org.gwaspi.global.XMLParser.getDateValue(localElements.get(0), "Date");
            String localVersionNumber = org.gwaspi.global.XMLParser.getTextValue(localElements.get(0), "Number");     
            //MAKE VERSION CHECKS

            //<editor-fold defaultstate="collapsed" desc="IF < 2.0.1">

            if (localVersionNumber.compareTo("2.0.1") < 0) { //Remote version is still compatible with local version
                System.out.println("Updating Sample ID field size");
                StringBuilder alterSQLprefix = new StringBuilder("ALTER TABLE ");
                alterSQLprefix.append(org.gwaspi.constants.cDBGWASpi.SCH_SAMPLES);
                alterSQLprefix.append(".");
                alterSQLprefix.append(org.gwaspi.constants.cDBSamples.T_SAMPLES_INFO);

                StringBuilder alterSQLsuffix = new StringBuilder(" ALTER ");
                alterSQLsuffix.append(org.gwaspi.constants.cDBSamples.f_SAMPLE_ID).append(" SET DATA TYPE VARCHAR(64)");
                try {
                    db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
                } catch (Exception e) {
                    System.out.print(e);
                    e.printStackTrace();
                }

                alterSQLsuffix = new StringBuilder(" ALTER ");
                alterSQLsuffix.append(org.gwaspi.constants.cDBSamples.f_FATHER_ID).append(" SET DATA TYPE VARCHAR(64)");
                try {
                    db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
                } catch (Exception e) {
                    System.out.print(e);
                    e.printStackTrace();
                }

                alterSQLsuffix = new StringBuilder(" ALTER ");
                alterSQLsuffix.append(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).append(" SET DATA TYPE VARCHAR(64)");
                try {
                    db.executeStatement(alterSQLprefix.toString() + alterSQLsuffix.toString());
                } catch (Exception e) {
                    System.out.print(e);
                    e.printStackTrace();
                }

            }
            //</editor-fold>

        }

        db = null;

    }
}
