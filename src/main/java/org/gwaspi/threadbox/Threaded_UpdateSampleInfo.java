package org.gwaspi.threadbox;

import org.gwaspi.constants.cDBGWASpi;
import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SamplesParser;

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
			String threadName,
			String timeStamp,
			int poolId,
			File sampleInfoFile)
	{
		super(threadName, timeStamp, "Sample Info Update");

		this.poolId = poolId;
		this.sampleInfoFile = sampleInfoFile;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_UpdateSampleInfo.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		Map<String, Object> sampleInfoMap = SamplesParser.scanGwaspiSampleInfo(sampleInfoFile.getPath());
		List<String> updatedSamplesAL = org.gwaspi.samples.InsertSampleInfo.processData(poolId, sampleInfoMap);

		// DO NOT! Write new reports of SAMPLE QA
//		OperationsList opList = new OperationsList(matrix.getMatrixId());
//		List<Operation> opAL = opList.operationsListAL;
//		int qaOpId = Integer.MIN_VALUE;
//		for (int i = 0; i < opAL.size(); i++) {
//			if (opAL.get(i).getOperationType().equals(cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
//				qaOpId = opAL.get(i).getOperationId();
//			}
//		}
//		if (qaOpId != Integer.MIN_VALUE) {
//			org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(qaOpId, false);
//		}

		org.gwaspi.model.Study study = new org.gwaspi.model.Study(poolId);

		StringBuilder oldDesc = new StringBuilder(study.getStudyDescription());
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

			DbManager db = ServiceLocator.getDbManager(cDBGWASpi.DB_DATACENTER);
			db.updateTable(cDBGWASpi.SCH_APP,
					cDBGWASpi.T_STUDIES,
					new String[]{cDBGWASpi.f_STUDY_DESCRIPTION},
					new Object[]{description},
					new String[]{cDBGWASpi.f_ID},
					new Object[]{studyId});
		} catch (IOException ex) {
			getLog().error(null, ex);
		}
	}
}
