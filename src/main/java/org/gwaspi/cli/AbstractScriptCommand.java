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

package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;

/**
 * Parses, prepares and executes one command read from a script file.
 */
abstract class AbstractScriptCommand implements ScriptCommand {

	private final String commandName;

	AbstractScriptCommand(String commandName) {
		this.commandName = commandName;
	}

	@Override
	public String getCommandName() {
		return commandName;
	}

	/**
	 * Fetches a study-key from the properties.
	 * @param script properties specified in the script
	 * @param idKey property-key of the id of the study
	 * @param nameKey property-key of the name of the study
	 * @param allowNew if true, then we create a new study,
	 *   if the found value <code>contains("New Study")</code>
	 * @return the parsed study-key
	 */
	protected static StudyKey fetchStudyKey(Map<String, String> script, String idKey, String nameKey, boolean allowNew) throws IOException {

		String idValue = script.get(idKey);
		String nameValue = script.get(nameKey);

		if ((idValue == null) && (nameValue == null)) {
			throw new RuntimeException("Neither nameKey (\"" + nameKey
					+ "\") nor id (\"" + idKey + "\") of the study are specified");
		} else if ((idValue != null) && (nameValue != null)) {
			throw new RuntimeException("You may only specify either name (\"" + nameKey
					+ "\") or id (\"" + idKey + "\") of the study, not both");
		} else if (idValue != null) {
			int studyId;
			try {
				studyId = Integer.parseInt(idValue);
			} catch (NumberFormatException ex) {
				if (allowNew && idValue.contains("New Study")) {
					studyId = StudyList.insertNewStudy(
							idValue/*.substring(10)*/,
							"Study created by command-line interface");
				} else {
					throw new IOException("The Study-id must be an integer value of an existing Study, \"" + idValue + "\" is not so!");
				}
			}
			return new StudyKey(studyId);
		} else {
			if (allowNew && nameValue.contains("New Study")) {
				int studyId = StudyList.insertNewStudy(
						idValue/*.substring(10)*/,
						"Study created by command-line interface");
				return new StudyKey(studyId);
			} else {
				return new StudyKey(nameValue);
			}
		}
	}

	protected static MatrixKey fetchMatrixKey(Map<String, String> script, int studyId, String idKey, String nameKey) throws IOException {

		String idValue = script.get(idKey);
		String nameValue = script.get(nameKey);

		if ((idValue == null) && (nameValue == null)) {
			throw new RuntimeException("Neither nameKey (\"" + nameKey
					+ "\") nor id (\"" + idKey + "\") of the matrix are specified");
		} else if ((idValue != null) && (nameValue != null)) {
			throw new RuntimeException("You may only specify either name (\"" + nameKey
					+ "\") or id (\"" + idKey + "\") of the matrix, not both");
		} else if (idValue != null) {
			int matrixId = Integer.parseInt(idValue);
			return new MatrixKey(studyId, matrixId);
		} else {
			MatrixMetadata matrixMetadata = MatricesList.getMatrixMetadataByNetCDFname(nameValue);
			return new MatrixKey(studyId, matrixMetadata.getMatrixId());
		}
	}

	/**
	 * Parses the study ID, and eventually creates a new study, if requested.
	 * @param studyIdStr the study ID parameter as given on the command-line
	 * @param allowNew if true, then we create a new study,
	 *   if <code>studyIdStr.contains("New Study")</code>
	 * @return the study ID or Integer.MIN_VALUE, in case of a problem.
	 */
	protected static int prepareStudy(String studyIdStr, boolean allowNew) throws IOException {

		int studyId = Integer.MIN_VALUE;

		try {
			studyId = Integer.parseInt(studyIdStr); // Study Id
		} catch (Exception ex) {
			if (allowNew) {
				if (studyIdStr.contains("New Study")) {
					studyId = StudyList.insertNewStudy(
							studyIdStr/*.substring(10)*/,
							"Study created by command-line interface");
				}
			} else {
				System.out.println("The Study-id must be an integer value of an existing Study, \""+studyIdStr+"\" is not so!");
			}
		}

		return studyId;
	}

	protected static boolean checkStudy(int studyId) throws IOException {

		boolean studyExists;

		Study study = StudyList.getStudy(studyId);
		studyExists = (study != null);

		if (!studyExists) {
			List<Study> studyList = StudyList.getStudyList();
			System.out.println("\n" + Text.Cli.studyNotExist);
			System.out.println(Text.Cli.availableStudies);
			for (Study studyX : studyList) {
				System.out.println("Study ID: " + studyX.getId());
				System.out.println("Name: " + studyX.getName());
				System.out.println("Description: " + studyX.getDescription());
				System.out.println("\n");
			}
		}

		return studyExists;
	}
}
