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
import org.gwaspi.global.Text;
import org.gwaspi.model.Study;
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
	 * parses the study ID, and eventually creates a new study, if requested.
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
