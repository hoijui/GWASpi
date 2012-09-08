package org.gwaspi.threadbox;

import java.io.IOException;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class UsePool2 {

	public static void main(String args[]) throws InterruptedException, IOException, InvalidRangeException {
//		Random random = new Random();
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		// Sum up wait times to know when to shutdown
//		int waitTime = 500;
//		Executor_OP_QASamples qa = new Executor_OP_QASamples(145);
//
//		for (int i = 0; i < 3; i++) {
//			String name = "Executor_OP_QASamples " + i;
//			int time = random.nextInt(1000);
//			waitTime += time;
//
//			Runnable runner = qa;
//
//			log.info("Adding: {} / {}", name, time);
//			executor.execute(runner);
//		}
//		try {
//			Thread.sleep(waitTime);
//			executor.shutdown();
//			executor.awaitTermination(waitTime, TimeUnit.MILLISECONDS);
//			log.info("Executed OperationId: {}", qa.getResult());
//		} catch (InterruptedException ignored) {
//		}
//		System.exit(0);
	}
}

//public class UsePool2 {
//
//	public static void main(String args[]) throws InterruptedException, IOException, InvalidRangeException, ExecutionException {
//		Random random = new Random();
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		// Sum up wait times to know when to shutdown
//		int waitTime = 500;
//		for (int i = 0; i < 3; i++) {
//			String name = "Executor_OP_QASamples " + i;
//			int time = random.nextInt(1000);
//			waitTime += time;
//
//			Executor_OP_QASamples qa = new Executor_OP_QASamples(145);
//			FutureTask task = new FutureTask(qa);
//
//			log.info("Adding: {} / {}", name, time);
//			executor.execute(task);
//
//			log.info("Executed OperationId: {}", task.get());
//		}
//		System.exit(0);
//	}
//}
