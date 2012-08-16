/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.model;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Study {

	private int studyId = Integer.MIN_VALUE;    //id INTEGER generated by default as identity
	private String studyName = "";              //name VARCHAR(64)
	private StringBuffer studyDescription = new StringBuffer();       //study_description LONG VARCHAR
	private String studyType = "";              //study_type VARCHAR(255)
	private String validity = "";               //validity SMALLINT
	private Date creationDate;                  //creation_date TIMESTAMP (2009-05-13 17:22:10.984)
	private StringBuffer studyLog = new StringBuffer();
	private ArrayList studyMatrices = new ArrayList();

	//This will init the Study object requested from the DB
	public Study(int _studyId) throws IOException {
		List<Map<String, Object>> rs = getStudy(_studyId);

		//PREVENT PHANTOM-DB READS EXCEPTIONS
		if (!rs.isEmpty() && rs.get(0).size() == org.gwaspi.constants.cDBGWASpi.T_CREATE_STUDIES.length) {
			studyId = Integer.parseInt(rs.get(0).get("id").toString());
			studyName = rs.get(0).get("name").toString();
			studyDescription.append(rs.get(0).get("study_description").toString());
			studyType = rs.get(0).get("study_type").toString();
			validity = rs.get(0).get("validity").toString();
			creationDate = org.gwaspi.global.Utils.stringToDate(rs.get(0).get("creation_date").toString(), "yyyy-MM-dd hh:mm:ss.SSS");

			studyMatrices = getStudyMatricesId(_studyId);
		}
	}

	public int getStudyId() {
		return studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public String getStudyType() {
		return studyType;
	}

	public String getValidity() {
		return validity;
	}

	public StringBuffer getStudyDescription() {
		return studyDescription;
	}

	public static List<Map<String, Object>> getStudy(int studyId) throws IOException {
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_APP + "." + org.gwaspi.constants.cDBGWASpi.T_STUDIES + " WHERE id=" + studyId + "  WITH RR");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rs;
	}

	public static ArrayList getStudyMatricesId(int studyId) throws IOException {
		ArrayList studyMatricesList = new ArrayList();
		List<Map<String, Object>> rs = null;
		String dbName = org.gwaspi.constants.cDBGWASpi.DB_DATACENTER;
		DbManager studyDbManager = ServiceLocator.getDbManager(dbName);
		try {
			rs = studyDbManager.executeSelectStatement("SELECT * FROM " + org.gwaspi.constants.cDBGWASpi.SCH_MATRICES + "." + org.gwaspi.constants.cDBMatrix.T_MATRICES + " WHERE " + org.gwaspi.constants.cDBMatrix.f_STUDYID + "=" + studyId + "  WITH RR");
		} catch (Exception e) {
			e.printStackTrace();
		}

		int rowcount = rs.size();
		if (rowcount > 0) {
			for (int i = rowcount - 1; i >= 0; i--) // loop through rows of result set
			{
				int currentMatrixId = (Integer) rs.get(i).get(org.gwaspi.constants.cDBMatrix.f_ID);
				studyMatricesList.add(currentMatrixId);
			}
		}

		return studyMatricesList;
	}

	public StringBuffer getStudyLog() throws IOException {
		String history_path = org.gwaspi.global.Config.getConfigValue("LogDir", "") + "/STUDY_" + this.studyId + ".log";
		File input = new File(history_path);
		studyLog = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(input)));
		return studyLog;
	}
}
