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
import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.threadbox.MultiOperations;

class ExportMatrixScriptCommand extends AbstractScriptCommand {

	ExportMatrixScriptCommand() {
		super("export_matrix");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		// checking study
		int studyId = prepareStudy(args.get("study-id"), false);
		int matrixId = Integer.parseInt(args.get("matrix-id"));
		boolean studyExists = checkStudy(studyId);

		String format = args.get("format");

		if (studyExists) {
			MultiOperations.doExportMatrix(
					studyId,
					matrixId,
					cExport.ExportFormat.valueOf(format),
					cDBSamples.f_AFFECTION);
			return true;
		}

		return false;
	}
}
