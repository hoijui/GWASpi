package org.gwaspi.threadbox;

import java.io.IOException;
import org.gwaspi.global.Config;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.operations.OP_AllelicAssociationTests_opt;
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

	private final Logger log
			= LoggerFactory.getLogger(Threaded_OP_HardyWeinberg.class);

	private Thread runner;
	private int resultOpId;
	private int matrixId;
	private Operation censusOP;
	private Operation hwOP;
	private double hwThreshold;

	public Threaded_OP_AssociationTests(
			String threadName,
			int matrixId,
			Operation censusOP,
			Operation hwOP,
			double hwThreshold)
			throws InterruptedException
	{
		Config.initPreferences(false, null);
		this.matrixId = matrixId;
		this.censusOP = censusOP;
		this.hwOP = hwOP;
		this.hwThreshold = hwThreshold;

		this.runner = new Thread(this, threadName); // (1) Create a new thread.
		this.runner.start(); // (2) Start the thread.
		this.runner.join();
	}

	public void run() {
		try {
			resultOpId = new OP_AllelicAssociationTests_opt(
					matrixId,
					censusOP,
					hwOP,
					hwThreshold).processMatrix();
		} catch (IOException ex) {
			log.error(null, ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}
	}
}
