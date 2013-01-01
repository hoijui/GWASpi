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
		int studyId = prepareStudy(args.get(1), true);
		boolean studyExists = checkStudy(studyId);

		File sampleInfoFile = new File(args.get(2));
		if (studyExists && (sampleInfoFile != null) && sampleInfoFile.exists()) {
			MultiOperations.updateSampleInfo(studyId,
					sampleInfoFile);
			return true;
		}

		return false;
	}
}
