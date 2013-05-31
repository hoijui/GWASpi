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
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.MultiOperations;

class LoadGenotypesScriptCommand extends AbstractScriptCommand {

	LoadGenotypesScriptCommand() {
		super("load_genotypes");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get("study-id"), true);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			ImportFormat format = ImportFormat.compareTo(args.get("format"));
			String newMatrixName = args.get("new-matrix-name");
			String description = args.get("description");

			GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
					args.get("file1-path"), // File 1
					args.get("sample-info-path"), // Sample Info file
					args.get("file2-path"), // File 2
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
					Boolean.parseBoolean(args.get("use-dummy-samples")), // Dummy samples
					false, // Do GWAS
					gwasParams); // gwasParams (dummy)

			return true;
		}

		return false;
	}
}
