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
import org.gwaspi.constants.ImportConstants.ImportFormat;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_Loader_GWASifOK;

class LoadGenotypesScriptCommand extends AbstractScriptCommand {

	LoadGenotypesScriptCommand() {
		super("load_genotypes");
	}

	@Override
	public void execute(final Map<String, String> args) throws ScriptExecutionException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=load_genotypes
		1.study-id=1
		2.format=PLINK
		3.use-dummy-samples=true
		4.new-matrix-name=Matrix 42
		5.description=Load genotypes of batch 42
		6.file1-path=/GWASpi/input/Plink/mi_input.map
		7.file2-path=/GWASpi/input/Plink/mi_input.ped
		8.sample-info-path=no info file
		[/script]
		*/
		//</editor-fold>

		try {
			GWASinOneGOParams gwasParams = new GWASinOneGOParams();

			// checking study
			StudyKey studyKey = prepareStudy(args.get("study-id"), true);
			checkStudyForScript(studyKey);

			ImportFormat format = ImportFormat.compareTo(args.get("format"));
			String newMatrixName = args.get("new-matrix-name");
			String description = args.get("description");

			GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
					args.get("file1-path"), // File 1
					args.get("sample-info-path"), // Sample Info file
					args.get("file2-path"), // File 2
					studyKey, // StudyKey
					format, // Format
					newMatrixName, // New Matrix name
					description, // Description
					gwasParams.getChromosome(),
					gwasParams.getStrandType(),
					gwasParams.getGtCode() // Gt code (deprecated)
					);
			final CommonRunnable loadGwasTask = new Threaded_Loader_GWASifOK(
					loadDescription, // Format
					Boolean.parseBoolean(args.get("use-dummy-samples")), // Dummy samples
					false, // Do GWAS
					gwasParams); // gwasParams (dummy)
			MultiOperations.queueTask(loadGwasTask);
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
	}
}
