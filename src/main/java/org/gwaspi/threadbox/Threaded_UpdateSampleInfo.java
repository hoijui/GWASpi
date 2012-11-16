package org.gwaspi.threadbox;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.Study;
import org.gwaspi.model.StudyList;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_UpdateSampleInfo extends CommonRunnable {

	private File sampleInfoFile;
	private int poolId;

	public Threaded_UpdateSampleInfo(
			int poolId,
			File sampleInfoFile)
	{
		super(
				"Update Sample Info",
				"Sample Info Update",
				"Update Sample Info on Study ID: " + poolId,
				"Sample Info Update");

		this.poolId = poolId;
		this.sampleInfoFile = sampleInfoFile;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_UpdateSampleInfo.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		Collection<SampleInfo> sampleInfos = SamplesParserManager.scanGwaspiSampleInfo(sampleInfoFile.getPath());
		List<String> updatedSamples = SampleInfoList.insertSampleInfos(poolId, sampleInfos);

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

		Study study = StudyList.getStudy(poolId);

		StringBuilder oldDesc = new StringBuilder(study.getDescription());
		oldDesc.append("\n* Sample Info updated from: ");
		oldDesc.append(sampleInfoFile.getPath());
		oldDesc.append(" (");
		oldDesc.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
		oldDesc.append(") *");
		saveDescription(oldDesc.toString(), poolId);
	}

	private void saveDescription(String description, int studyId) {
		try {
			org.gwaspi.global.Utils.logBlockInStudyDesc(description, studyId);
			MatricesList.saveMatrixDescription(
					studyId,
					description);
		} catch (IOException ex) {
			getLog().error(null, ex);
		}
	}
}
