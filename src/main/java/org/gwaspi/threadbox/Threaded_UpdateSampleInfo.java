package org.gwaspi.threadbox;

import org.gwaspi.database.DbManager;
import org.gwaspi.global.ServiceLocator;
import org.gwaspi.global.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.samples.SamplesParser;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_UpdateSampleInfo implements Runnable {

	private Thread runner;
	private String timeStamp = "";
	private File sampleInfoFile;
	private static int poolId;

	public Threaded_UpdateSampleInfo(String threadName,
			String _timeStamp,
			int _poolId,
			File _sampleInfoFile) {
		try {
			timeStamp = _timeStamp;
			org.gwaspi.global.Utils.sysoutStart("Sample Info Update");
			org.gwaspi.global.Config.initPreferences(false, null);
			poolId = _poolId;
			sampleInfoFile = _sampleInfoFile;
			runner = new Thread(this, threadName); // (1) Create a new thread.
			runner.start(); // (2) Start the thread.
			runner.join();
		} catch (InterruptedException ex) {
			//Logger.getLogger(Threaded_UpdateSampleInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		SwingWorkerItem thisSwi = SwingWorkerItemList.getSwingWorkerItemByTimeStamp(timeStamp);

		try {
			LinkedHashMap sampleInfoLHM = SamplesParser.scanGwaspiSampleInfo(sampleInfoFile.getPath());
			ArrayList updatedSamplesAL = org.gwaspi.samples.InsertSampleInfo.processData(poolId, sampleInfoLHM);

			/////DO NOT! Write new reports of SAMPLE QA
//            OperationsList opList = new OperationsList(matrix.getMatrixId());
//            ArrayList<Operation> opAL = opList.operationsListAL;
//            int qaOpId = Integer.MIN_VALUE;
//            for (int i = 0; i < opAL.size(); i++) {
//                if (opAL.get(i).getOperationType().equals(org.gwaspi.constants.cNetCDF.Defaults.OPType.SAMPLE_QA.toString())) {
//                    qaOpId = opAL.get(i).getOperationId();
//                }
//            }
//            if (qaOpId != Integer.MIN_VALUE) {
//                org.gwaspi.reports.OutputQASamples.writeReportsForQASamplesData(qaOpId, false);
//            }

			org.gwaspi.model.Study study = new org.gwaspi.model.Study(poolId);

			StringBuilder oldDesc = new StringBuilder(study.getStudyDescription());
			oldDesc.append("\n* Sample Info updated from: ");
			oldDesc.append(sampleInfoFile.getPath());
			oldDesc.append(" (");
			oldDesc.append(org.gwaspi.global.Utils.getShortDateTimeAsString());
			oldDesc.append(") *");
			saveDescription(oldDesc.toString(), poolId);

			org.gwaspi.global.Utils.sysoutFinish("Sample Info Update");
			MultiOperations.swingWorkerItemList.flagCurrentItemDone(timeStamp);
			//MultiOperations.updateTree();
			MultiOperations.updateProcessOverviewStartNext();

		} catch (OutOfMemoryError e) {
			System.out.println(Text.App.outOfMemoryError);
		} catch (Exception ex) {
			Logger.getLogger(Threaded_UpdateSampleInfo.class.getName()).log(Level.SEVERE, null, ex);
			MultiOperations.printError("Matrix Quality Control");
			try {
				MultiOperations.swingWorkerItemList.flagCurrentItemError(timeStamp);
				MultiOperations.updateTree();
				MultiOperations.updateProcessOverviewStartNext();
			} catch (Exception ex1) {
			}
		}
	}

	private void saveDescription(String description, int studyId) {
		try {
			org.gwaspi.global.Utils.logBlockInStudyDesc(description, studyId);

			DbManager db = ServiceLocator.getDbManager(org.gwaspi.constants.cDBGWASpi.DB_DATACENTER);
			db.updateTable(org.gwaspi.constants.cDBGWASpi.SCH_APP,
					org.gwaspi.constants.cDBGWASpi.T_STUDIES,
					new String[]{constants.cDBGWASpi.f_STUDY_DESCRIPTION},
					new Object[]{description},
					new String[]{constants.cDBGWASpi.f_ID},
					new Object[]{studyId});

		} catch (IOException ex) {
			Logger.getLogger(Threaded_UpdateSampleInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
