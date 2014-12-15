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
import java.util.Map;
import org.gwaspi.model.StudyKey;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_UpdateSampleInfo;

class UpdateSampleInfoScriptCommand extends AbstractScriptCommand {

	private static final String PARAM_SAMPLE_INFO_FILE = "sample-info-file";

	UpdateSampleInfoScriptCommand() {
		super("update_sample_info");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script <scriptFile>
		data-dir=/media/data/GWASpi
		[script]
		0.command=update_sample_info
		1.study-id=1
		2.sample-info-file=/tmp/mySampleInfo.txt
		[/script]
		*/
		//</editor-fold>

		// checking study
		final StudyKey studyKey = prepareStudy(args.get("study-id"), true);
		final boolean studyExists = checkStudy(studyKey);
		if (!studyExists) {
			return false;
		}

		final String sampleInfoFilePath = fetchRequired(args, PARAM_SAMPLE_INFO_FILE);
		final File sampleInfoFile = new File(sampleInfoFilePath);
		if (!sampleInfoFile.exists()) {
			System.err.println("Sample-info file does not exist: \"" + sampleInfoFile.getAbsolutePath() + "\"");
			return false;
		}

		final CommonRunnable updateSampleInfoTask = new Threaded_UpdateSampleInfo(studyKey, sampleInfoFile);
		MultiOperations.queueTask(updateSampleInfoTask);
		return true;
	}
}
