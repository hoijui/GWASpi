package org.gwaspi.cli;

import java.io.IOException;
import java.util.List;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
class LoadGenotypesScriptCommand extends AbstractScriptCommand {

	LoadGenotypesScriptCommand() {
		super("load_genotypes");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get(1), true);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			ImportFormat format = ImportFormat.compareTo(args.get(2));
			String newMatrixName = args.get(4);
			String description = args.get(5);

			GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
					args.get(6), // File 1
					args.get(8), // Sample Info file
					args.get(7), // File 2
					studyId, // StudyId
					format, // Format
					newMatrixName, // New Matrix name
					description, // Description
					gwasParams.getChromosome(),
					gwasParams.getStrandType(),
					gwasParams.getGtCode() // Gt code (deprecated)
					);
			MultiOperations.loadMatrixDoGWASifOK(
					loadDescription, // Format
					Boolean.parseBoolean(args.get(3)), // Dummy samples
					false, // Do GWAS
					gwasParams); // gwasParams (dummy)

			return true;
		}

		return false;
	}
}
