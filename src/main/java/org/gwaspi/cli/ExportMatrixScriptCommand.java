package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class ExportMatrixScriptCommand extends AbstractScriptCommand {

	ExportMatrixScriptCommand() {
		super("export_matrix");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		// checking study
		int studyId = Integer.parseInt(args.get(1)); // Study Id
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
