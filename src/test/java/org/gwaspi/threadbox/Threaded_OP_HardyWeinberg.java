package org.gwaspi.threadbox;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.*;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_OP_HardyWeinberg implements Runnable {

	private Thread runner;
	private int resultOpId;
	private static Operation censusOP;
	private static String hwName;

	public Threaded_OP_HardyWeinberg(String threadName,
			Operation _censusOP,
			String _hwName) throws InterruptedException {
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
			Logger.getLogger(Threaded_OP_HardyWeinberg.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvalidRangeException ex) {
			Logger.getLogger(Threaded_OP_HardyWeinberg.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
