package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
class ExportMatrixScriptCommand extends AbstractScriptCommand {

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

	ExportMatrixScriptCommand() {
		super("export_matrix");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		// checking study
		int studyId = prepareStudy(args.get(1), false);
		int matrixId = Integer.parseInt(args.get(2)); // Study Id
		boolean studyExists = checkStudy(studyId);

		String format = args.get(3);

		if (studyExists) {
			multiOperations.doExportMatrix(studyId, matrixId, cExport.ExportFormat.valueOf(format), cDBSamples.f_AFFECTION);
			return true;
		}

		return false;
	}
}
