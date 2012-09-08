package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.OP_HardyWeinberg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_OP_HardyWeinberg implements Runnable {

	private final static Logger log
			= LoggerFactory.getLogger(Threaded_OP_HardyWeinberg.class);

	private Thread runner;
	private int resultOpId;
	private static Operation censusOP;
	private static String hwName;

	public Threaded_OP_HardyWeinberg(String threadName,
			Operation _censusOP,
			String _hwName)
			throws InterruptedException
	{
		org.gwaspi.global.Config.initPreferences(false, null);
		censusOP = _censusOP;
		hwName = _hwName;

		runner = new Thread(this, threadName); // (1) Create a new thread.
		runner.start(); // (2) Start the thread.
		runner.join();
	}

	public void run() {
		try {
			resultOpId = OP_HardyWeinberg.processMatrix(censusOP,
					hwName);
		} catch (IOException ex) {
			log.error(null, ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
	}
}
