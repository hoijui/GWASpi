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

package org.gwaspi.progress;

/**
 * Contains basic, immutable info about a process/task.
 */
public interface ProcessInfo {

	/**
	 * Returns a name for the process this source covers.
	 * @return a short, human oriented description of the process.
	 */
	String getShortName();

	/**
	 * Returns a more thorough description for the process.
	 * This will likely be used as a tool-tip.
	 * @return an extensive, human oriented description of the process.
	 */
	String getDescription();
}
