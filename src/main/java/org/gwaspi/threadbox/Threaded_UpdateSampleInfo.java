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

package org.gwaspi.threadbox;

import java.io.File;
import java.util.Collection;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyKey;
import org.gwaspi.model.StudyList;
import org.gwaspi.netCDF.loader.InMemorySamplesReceiver;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_UpdateSampleInfo extends CommonRunnable {

	private final StudyKey studyKey;
	private final File sampleInfoFile;

	public Threaded_UpdateSampleInfo(
			StudyKey studyKey,
			File sampleInfoFile)
	{
		super(
				"Update Sample Info",
				"Sample Info Update",
				"Update Sample Info on Study ID: " + studyKey,
				"Sample Info Update");

		this.studyKey = studyKey;
		this.sampleInfoFile = sampleInfoFile;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_UpdateSampleInfo.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		InMemorySamplesReceiver inMemorySamplesReceiver = new InMemorySamplesReceiver();
		SamplesParserManager.scanSampleInfo(
				studyKey,
				ImportFormat.GWASpi,
				sampleInfoFile.getPath(),
				inMemorySamplesReceiver);
		Collection<SampleInfo> sampleInfos = inMemorySamplesReceiver.getDataSet().getSampleInfos();
		SampleInfoList.insertSampleInfos(sampleInfos);

		// DO NOT! Write new reports of SAMPLE QA
//		OperationsList opList = new OperationsList(matrix.getMatrixId());
//		List<Operation> opAL = opList.operationsListAL;
//		int qaOpId = Integer.MIN_VALUE;
//		for (int i = 0; i < opAL.size(); i++) {
//			if (opAL.get(i).getOperationType().equals(OPType.SAMPLE_QA)) {
//				qaOpId = opAL.get(i).getOperationId();
//			}
//		}
//		if (qaOpId != Integer.MIN_VALUE) {
//			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(qaOpId, false);
//		}

		Study study = StudyList.getStudy(studyKey);

		StringBuilder oldDesc = new StringBuilder(study.getDescription());
		oldDesc.append("\n* Sample Info updated from: ");
		oldDesc.append(sampleInfoFile.getPath());
		oldDesc.append(" (");
		oldDesc.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		oldDesc.append(") *");
		study.setDescription(oldDesc.toString());
		StudyList.updateStudy(study);
	}
}
