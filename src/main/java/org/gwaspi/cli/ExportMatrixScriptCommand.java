package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.threadbox.MultiOperations;

class ExportMatrixScriptCommand extends AbstractScriptCommand {

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
			MultiOperations.doExportMatrix(studyId, matrixId, cExport.ExportFormat.valueOf(format), cDBSamples.f_AFFECTION);
			return true;
		}

		return false;
	}
}
