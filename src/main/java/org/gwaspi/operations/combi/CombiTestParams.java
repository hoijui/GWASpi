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

import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;

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
	private final OperationKey hardyWeinbergOperationKey;
	private final double hardyWeinbergThreshold;
	private final GenotypeEncoder encoder;
//	private final File phenotypeInfo;
	private final String resultMatrixName;
	private final int markersToKeep;
	/**
	 * Whether to use resampling based threshold calibration.
	 * This feature takes a lot of computation time!
	 */
	private final boolean useThresholdCalibration;

	public CombiTestParams(
			MatrixKey matrixKey,
			OperationKey hardyWeinbergOperationKey,
			double hardyWeinbergThreshold,
			GenotypeEncoder encoder,
//			File phenotypeInfo,
			int markersToKeep,
			boolean useThresholdCalibration,
			String resultMatrixName)
	{
		this.matrixKey = matrixKey;
		this.hardyWeinbergOperationKey = hardyWeinbergOperationKey;
		this.hardyWeinbergThreshold = hardyWeinbergThreshold;
		this.encoder = encoder;
//		this.phenotypeInfo = phenotypeInfo;
		this.markersToKeep = markersToKeep;
		this.useThresholdCalibration = useThresholdCalibration;
		this.resultMatrixName = resultMatrixName;
	}

	public CombiTestParams(
			MatrixKey matrixKey,
			OperationKey hardyWeinbergOperationKey,
			double hardyWeinbergThreshold,
			GenotypeEncoder encoder,
			int markersToKeep,
			boolean useThresholdCalibration)
	{
		this(
				matrixKey,
				hardyWeinbergOperationKey,
				hardyWeinbergThreshold,
				encoder,
				markersToKeep,
				useThresholdCalibration,
				"Combi-Test for matrix " + matrixKey.toString()
				);
	}

	public CombiTestParams(
			MatrixKey matrixKey,
			OperationKey hardyWeinbergOperationKey)
	{
		this.matrixKey = matrixKey;
		this.hardyWeinbergOperationKey = hardyWeinbergOperationKey;
		this.hardyWeinbergThreshold = hardyWeinbergThreshold;
		this.encoder = encoder;
//		this.phenotypeInfo = phenotypeInfo;
		this.markersToKeep = markersToKeep;
		this.useThresholdCalibration = useThresholdCalibration;
		this.resultMatrixName = resultMatrixName;
	}

	public CombiTestParams(MatrixKey matrixKey) {
		this.matrixKey = matrixKey;
		this.hardyWeinbergOperationKey = hardyWeinbergOperationKey;
		this.hardyWeinbergThreshold = hardyWeinbergThreshold;
		this.encoder = encoder;
//		this.phenotypeInfo = phenotypeInfo;
		this.markersToKeep = markersToKeep;
		this.useThresholdCalibration = useThresholdCalibration;
		this.resultMatrixName = resultMatrixName;
	}

	public MatrixKey getMatrixKey() {
		return matrixKey;
	}

	public OperationKey getHardyWeinbergOperationKey() {
		return hardyWeinbergOperationKey;
	}

	public double getHardyWeinbergThreshold() {
		return hardyWeinbergThreshold;
	}

	public GenotypeEncoder getEncoder() {
		return encoder;
	}
//
//	public File getPhenotypeInfo() {
//		return phenotypeInfo;
//	}

	public int getMarkersToKeep() {
		return markersToKeep;
	}

	public boolean isUseThresholdCalibration() {
		return useThresholdCalibration;
	}

	public String getResultMatrixName() {
		return resultMatrixName;
	}
}
