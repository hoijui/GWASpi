package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_OP_AssociationTests implements Runnable {

	private final static Logger log
			= LoggerFactory.getLogger(Threaded_OP_HardyWeinberg.class);

	private Thread runner;
	private int resultOpId;
	private static int matrixId;
	private static Operation censusOP;
	private static Operation hwOP;
	private static double hwThreshold;

	public Threaded_OP_AssociationTests(String threadName,
			int _matrixId,
			Operation _censusOP,
			Operation _hwOP,
			double _hwThreshold)
			throws InterruptedException
	{
		org.gwaspi.global.Config.initPreferences(false, null);
		matrixId = _matrixId;
		censusOP = _censusOP;
		hwOP = _hwOP;
		hwThreshold = _hwThreshold;

		runner = new Thread(this, threadName); // (1) Create a new thread.
		runner.start(); // (2) Start the thread.
		runner.join();
	}

	public void run() {
		try {
			resultOpId = org.gwaspi.netCDF.operations.OP_AllelicAssociationTests_opt.processMatrix(matrixId,
					censusOP,
					hwOP,
					hwThreshold);
		} catch (IOException ex) {
			log.error(null, ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
	}
}
