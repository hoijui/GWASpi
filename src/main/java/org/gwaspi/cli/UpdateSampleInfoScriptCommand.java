package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
class UpdateSampleInfoScriptCommand extends AbstractScriptCommand {

	@Reference
	private MultiOperations multiOperations;

	protected void bindMultiOperations(MultiOperations multiOperations) {
		this.multiOperations = multiOperations;
	}

	protected void unbindMultiOperations(MultiOperations multiOperations) {

		if (this.multiOperations == multiOperations) {
			this.multiOperations = null;
		}
	}

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
			multiOperations.updateSampleInfo(studyId,
					sampleInfoFile);
			return true;
		}

		return false;
	}
}
