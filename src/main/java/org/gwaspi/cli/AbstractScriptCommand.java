package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.gwaspi.global.Text;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyList;

/**
 * Parses, prepares and executes one command read from a script file.
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
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
