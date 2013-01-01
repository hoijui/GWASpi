package org.gwaspi.cli;

import java.io.IOException;
import org.gwaspi.global.Text;
import org.gwaspi.gui.StartGWASpi;
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

	protected static boolean checkStudy(int studyId) throws IOException {
		boolean studyExists = false;
		Object[][] studyTable = StudyList.getStudyTable();
		for (int i = 0; i < studyTable.length; i++) {
			if ((Integer) studyTable[i][0] == studyId) {
				studyExists = true;
			}
		}

		if (!studyExists) {
			System.out.println("\n" + Text.Cli.studyNotExist);
			System.out.println(Text.Cli.availableStudies);
			for (int i = 0; i < studyTable.length; i++) {
				System.out.println("Study ID: " + studyTable[i][0]);
				System.out.println("Name: " + studyTable[i][1]);
				System.out.println("Description: " + studyTable[i][2]);
				System.out.println("\n");
			}

			StartGWASpi.exit();
		}

		return studyExists;
	}

	protected static int addStudy(String newStudyName, String description) throws IOException {

		int newStudyId;

		StudyList.insertNewStudy(newStudyName, description);

		Object[][] studyTable = StudyList.getStudyTable();

		newStudyId = (Integer) studyTable[studyTable.length - 1][0];

		return newStudyId;
	}
}
