package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.global.Config;
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

	private final Logger log
			= LoggerFactory.getLogger(Threaded_OP_HardyWeinberg.class);

	private Thread runner;
	private int resultOpId;
	private Operation censusOP;
	private String hwName;

	public Threaded_OP_HardyWeinberg(
			String threadName,
			Operation censusOP,
			String hwName)
			throws InterruptedException
	{
		Config.initPreferences(false, null);
		this.censusOP = censusOP;
		this.hwName = hwName;

		this.runner = new Thread(this, threadName); // (1) Create a new thread.
		this.runner.start(); // (2) Start the thread.
		this.runner.join();
	}

	public void run() {
		try {
			resultOpId = new OP_HardyWeinberg(
					censusOP,
					hwName).processMatrix();
		} catch (IOException ex) {
			log.error(null, ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
	}
}
