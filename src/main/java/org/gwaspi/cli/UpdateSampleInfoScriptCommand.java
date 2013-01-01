package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class UpdateSampleInfoScriptCommand extends AbstractScriptCommand {

	UpdateSampleInfoScriptCommand() {
		super("update_sample_info");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		// checking study
		int studyId = Integer.MIN_VALUE;
		try {
			studyId = Integer.parseInt(args.get(1)); // Study Id
		} catch (Exception ex) {
			if (args.get(1).contains("New Study")) {
				studyId = addStudy(args.get(1).substring(10), "Study created by command-line interface");
			}
		}
		boolean studyExists = checkStudy(studyId);

		File sampleInfoFile = new File(args.get(2));
		if (sampleInfoFile != null && sampleInfoFile.exists()) {
			MultiOperations.updateSampleInfo(studyId,
					sampleInfoFile);
			return true;
		}

		return false;
	}
}
