/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.operations;

import java.io.IOException;
import org.gwaspi.progress.ProgressSource;

public interface MatrixOperation<PT extends OperationParams> {

//	/**
//	 * Every operation is in one of these states at any time.
//	 * {@link #addOperationListener(OperationListener)}
//	 * {@link #removeOperationListener(OperationListener)}
//	 *
//	 * NOTE
//	 * Why "Status" instead of "State"?
//	 * See the explanation given here:
//	 * {@see http://forum.wordreference.com/showthread.php?t=287984&s=0d5454f63863bcb10cb775d94faef585&p=3423244#post3423244}
//	 * ... while it is a different thing if we move away from IT:
//	 * {@see https://answers.yahoo.com/question/index?qid=20070427185233AAcmFY5}
//	 */
//	public static enum Status {
//		INITIALIZING,
//		INITIALIZED,
//		STARTED,
//		PAUSED,
//		RESUMED,
//		ABORTED,
//		FAILED,
//		COMPLEETED; // XXX or FINNISHED?
//	}

	OperationTypeInfo getTypeInfo();

	ProgressSource getProgressSource() throws IOException;

	void addOperationListener(OperationListener listener);

	void removeOperationListener(OperationListener listener);

	/**
	 * Whether the operation is valid, given its parameters.
	 * @return true, if the operation seems to be valid,
	 *   and may be able to execute properly; false otherwise
	 * @throws IOException
	 */
	boolean isValid() throws IOException;

	/**
	 * Returns true, if the operation looks to be valid,
	 * and may be able to execute properly.
	 * @return a human readable description of the problem
	 *   with the given parameters if the operation is invalid;
	 *   <code>null</code> otherwise
	 * @see #isValid()
	 */
	String getProblemDescription();

	/**
	 * @return this operations parameters; may be <code>null</code>
	 */
	PT getParams();

	/**
	 * Execute this operation.
	 * @return the resulting matrixes ID if the operation succeeded,
	 *   with the given parameters if the operation is invalid;
	 *   <code>Integer#MIN_VALUE</code> otherwise
	 * @throws IOException
	 */
	int processMatrix() throws IOException;
}
