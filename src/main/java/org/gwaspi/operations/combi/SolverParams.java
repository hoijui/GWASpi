/*
 * Copyright (C) 2015 Universitat Pompeu Fabra
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

package org.gwaspi.operations.combi;

public class SolverParams {

	/** stopping criteria */
	private final double eps;
	/** TODO */
	private final double c;

	public SolverParams(
			final Double eps,
			final Double c)
	{
		this.eps = (eps == null) ? getEpsDefault() : eps;
		this.c = (c == null) ? getCDefault() : c;
	}

	public SolverParams() {

		this(
				null,
				null
				);
	}

	public double getEps() {
		return eps;
	}

	public static double getEpsDefault() {
		return 1E-7; // HACK 1E-3;
	}

	public double getC() {
		return c;
	}

	public static double getCDefault() {
		return 1.0; // HACK 1E-5;
	}
}
