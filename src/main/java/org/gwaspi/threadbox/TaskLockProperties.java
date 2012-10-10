package org.gwaspi.threadbox;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains all the info that defines what things might be locked by a task.
 * Meaning, that while the task is running, it might alter these things,
 * and thus, other parts of the code, other threads, are not allowed
 * to change these things.
 */
public class TaskLockProperties {

	private Collection<Integer> studyIds;
	private Collection<Integer> matricesIds;
	private Collection<Integer> operationsIds;

	public TaskLockProperties() {

		this.studyIds = new ArrayList<Integer>();
		this.matricesIds = new ArrayList<Integer>();
		this.operationsIds = new ArrayList<Integer>();
	}

	public Collection<Integer> getMatricesIds() {
		return matricesIds;
	}

	public Collection<Integer> getOperationsIds() {
		return operationsIds;
	}

	public Collection<Integer> getStudyIds() {
		return studyIds;
	}
}
