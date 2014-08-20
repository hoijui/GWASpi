/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

package org.gwaspi.operations.genotypesflipper;

import java.io.File;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.operations.AbstractMatrixCreatingOperationParams;

public class MatrixGenotypesFlipperParams extends AbstractMatrixCreatingOperationParams {

	private final File flipperFile;

	public MatrixGenotypesFlipperParams(
			DataSetKey parent,
			String matrixDescription,
			String matrixFriendlyName,
			File flipperFile)
	{
		super(parent, matrixDescription, matrixFriendlyName);

		this.flipperFile = flipperFile;
	}

	public File getFlipperFile() {
		return flipperFile;
	}
}
