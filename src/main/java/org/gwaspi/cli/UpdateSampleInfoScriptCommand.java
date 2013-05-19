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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.gwaspi.threadbox.MultiOperations;

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
