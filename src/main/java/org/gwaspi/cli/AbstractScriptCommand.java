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
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
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
			StudyKey studyKey;
			try {
				studyKey = new StudyKey(Integer.parseInt(idValue));
			} catch (NumberFormatException ex) {
				if (allowNew && idValue.contains("New Study")) {
					studyKey = StudyList.insertNewStudy(new Study(
							idValue/*.substring(10)*/,
							"Study created by command-line interface"));
					GWASpiExplorerNodes.insertStudyNode(studyKey);
				} else {
					throw new IOException("The Study-id must be an integer value of an existing Study, \"" + idValue + "\" is not so!");
				}
			}
			return studyKey;
		} else { // -> (nameValue != null)
			if (allowNew && nameValue.contains("New Study")) {
				StudyKey studyKey = StudyList.insertNewStudy(new Study(
						idValue/*.substring(10)*/,
						"Study created by command-line interface"));
				GWASpiExplorerNodes.insertStudyNode(studyKey);
				return studyKey;
			} else {
				throw new UnsupportedOperationException("Not yet implemented");
//				return StudyList.getStudyKey(nameValue);
			}
		}
	}

	protected static StudyKey fetchStudyKey(
			final Map<String, String> script,
			final String keyPrefix,
			final boolean allowNew)
			throws IOException
	{
		return fetchStudyKey(script, keyPrefix + "-id", keyPrefix + "-name", allowNew);
	}

	protected static StudyKey fetchStudyKey(
			final Map<String, String> script,
			final boolean allowNew)
			throws IOException
	{
		return fetchStudyKey(script, "study", allowNew);
	}

	protected static StudyKey fetchStudyKey(
			final Map<String, String> script)
			throws IOException
	{
		return fetchStudyKey(script, false);
	}

	protected static MatrixKey fetchMatrixKey(Map<String, String> script, StudyKey studyKey, String idKey, String nameKey) throws IOException {

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
			return new MatrixKey(studyKey, matrixId);
		} else {
			List<MatrixKey> matrixKeysByName = MatricesList.getMatrixKeysByName(studyKey, nameValue);
			return matrixKeysByName.isEmpty() ? null : matrixKeysByName.get(0);
		}
	}

	protected static OperationKey fetchOperationKey(Map<String, String> script, MatrixKey parentMatrixKey, String idKey, String nameKey) throws IOException {

		String idValue = script.get(idKey);
		String nameValue = script.get(nameKey);

		if ((idValue == null) && (nameValue == null)) {
			throw new RuntimeException("Neither nameKey (\"" + nameKey
					+ "\") nor id (\"" + idKey + "\") of the operation are specified");
		} else if ((idValue != null) && (nameValue != null)) {
			throw new RuntimeException("You may only specify either name (\"" + nameKey
					+ "\") or id (\"" + idKey + "\") of the operation, not both");
		} else if (idValue != null) {
			int operationId = Integer.parseInt(idValue);
			return new OperationKey(parentMatrixKey, operationId);
		} else {
			List<OperationKey> operationKeysByName = OperationsList.getOperationKeysByName(parentMatrixKey.getStudyKey(), nameValue);
			return operationKeysByName.isEmpty() ? null : operationKeysByName.get(0);
		}
	}

	private static boolean checkStudy(StudyKey studyKey) throws IOException {

		boolean studyExists;

		Study study = StudyList.getStudy(studyKey);
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

	protected static void checkStudyForScript(StudyKey studyKey) throws ScriptExecutionException {

		final boolean studyExists;
		try {
			studyExists = checkStudy(studyKey);
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
		if (!studyExists) {
			throw new ScriptExecutionException(new IllegalArgumentException(
					"Study does not exist: " + studyKey.toRawIdString()));
		}
	}

	protected static String fetchRequired(final Map<String, String> args, final String argName) throws IOException {

		final String value = args.get(argName);
		if (value == null) {
			throw new IOException("Script is missing required parameter \"" + argName + "\"");
		}
		return value;
	}

	protected static Double fetchDouble(Map<String, String> args, String argName, Double defaultValue) throws IOException {

		Double value;

		try {
			value = Double.parseDouble(args.get(argName));
		} catch (NullPointerException ex) {
			value = defaultValue;
		} catch (NumberFormatException ex) {
			value = defaultValue;
		}

		return value;
	}

	protected static Integer fetchInteger(Map<String, String> args, String argName, Integer defaultValue) throws IOException {

		Integer value;

		try {
			value = Integer.parseInt(args.get(argName));
		} catch (NullPointerException ex) {
			value = defaultValue;
		} catch (NumberFormatException ex) {
			value = defaultValue;
		}

		return value;
	}

	protected static Boolean fetchBoolean(Map<String, String> args, String argName, Boolean defaultValue) throws IOException {

		Boolean value;

		try {
			value = (Integer.parseInt(args.get(argName)) != 0);
		} catch (NullPointerException ex) {
			value = defaultValue;
		} catch (NumberFormatException ex) {
			value = defaultValue;
		}

		return value;
	}
}
