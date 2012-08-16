/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.threadbox;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

class ThreadPoolExecutor implements ExecutorService {

	int MAX_THREADPOOL_SIZE = 1;
	public ExecutorService executor;

	public ThreadPoolExecutor(int maxPoolSize) {
		MAX_THREADPOOL_SIZE = maxPoolSize;
		executor = Executors.newFixedThreadPool(MAX_THREADPOOL_SIZE);
	}

	public void execute(Runnable command) {
		new Thread(command).start();
	}

	public void shutdown() {
		this.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return this.shutdownNow();
	}

	public boolean isShutdown() {
		return this.isShutdown();
	}

	public boolean isTerminated() {
		return this.isTerminated();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.awaitTermination(timeout, unit);
	}

	public <T> Future<T> submit(Callable<T> task) {
		return this.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return this.submit(task, result);
	}

	public Future<?> submit(Runnable task) {
		return this.submit(task);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.invokeAll(tasks);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return this.invokeAll(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.invokeAny(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.invokeAny(tasks, timeout, unit);
	}
}
