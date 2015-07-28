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
import org.gwaspi.dao.MatrixService;
import org.gwaspi.dao.OperationService;
import org.gwaspi.dao.StudyService;
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

	private static MatrixService getMatrixService() {
		return MatricesList.getMatrixService();
	}

	private static OperationService getOperationService() {
		return OperationsList.getOperationService();
	}

	private static StudyService getStudyService() {
		return StudyList.getStudyService();
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
	 * @param allowNew if true, then we create a new study
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
			} catch (final NumberFormatException ex) {
				throw new IOException(
						"The Study-Id has to be an integer value and the Id of an existing Study, "
						+ "\"" + idValue + "\" is not so!");
			}
			return studyKey;
		} else { // -> (nameValue != null)
			final StudyKey studyByName = getStudyService().getStudyByName(nameValue);
			if (studyByName != null) {
				return studyByName;
			} else {
				// a study by the given name does not yet exist
				if (allowNew) {
					StudyKey studyKey = getStudyService().insertStudy(new Study(
							nameValue,
							"Study created by command-line interface"));
					GWASpiExplorerNodes.insertStudyNode(studyKey);
					return studyKey;
				} else {
					throw new UnsupportedOperationException("Study with name \"" + nameValue
							+ "\" does not exist, and conditions to create a new one are not met (allowNew: "
							+ Boolean.toString(allowNew) + ").");
				}
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
			final List<MatrixKey> matrixKeysByName = getMatrixService().getMatrixKeysByName(studyKey, nameValue);
			if (matrixKeysByName.isEmpty()) {
				throw new IllegalStateException(
						"No matrix with name \"" + nameValue + "\" found in the study "
								+ studyKey.toString());
			} else if (matrixKeysByName.size() == 1) {
				return matrixKeysByName.get(0);
			} else {
				throw new IllegalStateException(
						"The name of a matrix within a study is supposed to be unique, "
								+ "but there are multiple matrices with the name \""
								+ nameValue + "\" in the study " + studyKey.toString());
			}
		}
	}

	protected static MatrixKey fetchMatrixKey(
			final Map<String, String> script,
			final StudyKey studyKey,
			final String keyPrefix)
			throws IOException
	{
		return fetchMatrixKey(script, studyKey, keyPrefix + "-id", keyPrefix + "-name");
	}

	protected static MatrixKey fetchMatrixKey(
			final Map<String, String> script,
			final StudyKey studyKey)
			throws IOException
	{
		return fetchMatrixKey(script, studyKey, "matrix");
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
			final List<OperationKey> operationKeysByName
					= getOperationService().getOperationKeysByName(parentMatrixKey.getStudyKey(), nameValue);
			if (operationKeysByName.isEmpty()) {
				throw new IllegalStateException(
						"No operation with name \"" + nameValue + "\" found in the study "
								+ parentMatrixKey.getStudyKey().toString());
			} else if (operationKeysByName.size() == 1) {
				return operationKeysByName.get(0);
			} else {
				throw new IllegalStateException(
						"The name of an operation within a study is supposed to be unique, "
								+ "but there are multiple matrices with the name \""
								+ nameValue + "\" in the study "
								+ parentMatrixKey.getStudyKey().toString());
			}
		}
	}

	protected static OperationKey fetchOperationKey(
			final Map<String, String> script,
			final MatrixKey parentMatrixKey,
			final String keyPrefix)
			throws IOException
	{
		return fetchOperationKey(script, parentMatrixKey, keyPrefix + "-id", keyPrefix + "-name");
	}

	protected static OperationKey fetchOperationKey(
			final Map<String, String> script,
			final MatrixKey parentMatrixKey)
			throws IOException
	{
		return fetchOperationKey(script, parentMatrixKey, "operation");
	}

	private static boolean checkStudy(StudyKey studyKey) throws IOException {

		boolean studyExists;

		Study study = getStudyService().getStudy(studyKey);
		studyExists = (study != null);

		if (!studyExists) {
			List<Study> studyList = getStudyService().getStudiesInfos();
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

	protected static void checkMatrixForScript(final MatrixKey matrixKey) throws ScriptExecutionException {

		if (matrixKey == null) {
			throw new ScriptExecutionException(new IllegalArgumentException(
					"Invalid matrix: \"" + matrixKey + "\""));
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
