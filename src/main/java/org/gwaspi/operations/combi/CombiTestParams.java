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
package org.gwaspi.operations.combi;

import java.io.File;
import org.gwaspi.model.MatrixKey;

/**
 * Parameters for the {@link CombiTestMatrixOperation}.
 */
public class CombiTestParams {

//	/**
//	 * Which study we operate in (read from and write to).
//	 */
//	private final StudyKey studyKey;
	/**
	 * Which matrix to operate on (read from).
	 */
	private final MatrixKey matrixKey;

	private final GenotypeEncoder encoder;
	private final File phenotypeInfo;
	private final String resultMatrixName;

	public CombiTestParams(
			MatrixKey matrixKey,
			GenotypeEncoder encoder,
			File phenotypeInfo,
			String resultMatrixName)
	{
		this.matrixKey = matrixKey;
		this.encoder = encoder;
		this.phenotypeInfo = phenotypeInfo;
		this.resultMatrixName = resultMatrixName;
	}

	public MatrixKey getMatrixKey() {
		return matrixKey;
	}

	public GenotypeEncoder getEncoder() {
		return encoder;
	}

	public File getPhenotypeInfo() {
		return phenotypeInfo;
	}

	public String getResultMatrixName() {
		return resultMatrixName;
	}
}
