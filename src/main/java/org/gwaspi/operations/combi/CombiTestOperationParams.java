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

import java.io.IOException;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parameters for the {@link CombiTestMatrixOperation}.
 */
public class CombiTestOperationParams extends AbstractOperationParams {

	private static final Logger LOG = LoggerFactory.getLogger(CombiTestOperationParams.class);

//	/**
//	 * Which matrix to operate on (read from).
//	 */
//	private final MatrixKey matrixKey;
//	private final OperationKey censusOperationKey;
	private final OperationKey qaMarkersOperationKey;
//	private final OperationKey hardyWeinbergOperationKey;
//	private final double hardyWeinbergThreshold;
	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final GenotypeEncoder encoder;
//	private final File phenotypeInfo;
	/**
	 * The number of total markers in the matrix we operate on, unfiltered.
	 */
	private Integer totalMarkers;
	/**
	 * How many markers to be left with,
	 * after the filtering with the COMBI method.
	 */
	private final int markersToKeep;
	/**
	 * Whether to use resampling based threshold calibration.
	 * This feature takes a lot of computation time!
	 */
	private final boolean useThresholdCalibration;

	public CombiTestOperationParams(
//			MatrixKey matrixKey,
//			OperationKey censusOperationKey,
			OperationKey qaMarkersOperationKey,
//			OperationKey hardyWeinbergOperationKey,
//			Double hardyWeinbergThreshold,
			GenotypeEncoder encoder,
//			File phenotypeInfo,
			Integer markersToKeep,
			Boolean useThresholdCalibration,
			String name)
	{
		super(new DataSetKey(qaMarkersOperationKey), name);

//		this.matrixKey = matrixKey;
//		this.censusOperationKey = censusOperationKey;
		this.qaMarkersOperationKey = qaMarkersOperationKey;
//		this.hardyWeinbergOperationKey = hardyWeinbergOperationKey;
//		this.hardyWeinbergThreshold = (hardyWeinbergThreshold == null)
//				? getHardyWeinbergThresholdDefault()
//				: hardyWeinbergThreshold;
		this.encoder = (encoder == null)
				? getEncoderDefault()
				: encoder;
		this.markersToKeep = ((markersToKeep == null)
				|| (markersToKeep <= 0) || (markersToKeep >= getTotalMarkers()))
				? getMarkersToKeepDefault()
				: markersToKeep;
		this.useThresholdCalibration = (useThresholdCalibration == null)
				? isUseThresholdCalibrationDefault()
				: useThresholdCalibration;
//		this.phenotypeInfo = phenotypeInfo;
	}

//	public CombiTestParams(
//			MatrixKey matrixKey,
//			OperationKey hardyWeinbergOperationKey,
//			double hardyWeinbergThreshold,
//			GenotypeEncoder encoder,
//			int markersToKeep,
//			boolean useThresholdCalibration)
//	{
//		this(
//				matrixKey,
//				hardyWeinbergOperationKey,
//				hardyWeinbergThreshold,
//				encoder,
//				markersToKeep,
//				useThresholdCalibration,
//				null
//				);
//	}

	public CombiTestOperationParams(
//			MatrixKey matrixKey,
			OperationKey censusOperationKey/*,
			OperationKey hardyWeinbergOperationKey*/)
	{
		this(
//				matrixKey,
				censusOperationKey,
//				hardyWeinbergOperationKey,
//				null,
				null,
				null,
				null,
				null
				);
	}

	private static int fetchTotalMarkers(DataSetKey parentKey) {

		int total = -1;

		DataSetMetadata parentMetadata;
		try {
			parentMetadata = MatricesList.getDataSetMetadata(parentKey);
			if (parentMetadata != null) {
				total = parentMetadata.getNumMarkers();
			}
		} catch (IOException ex) {
			LOG.debug("Failed to fetch the total number of markers", ex);
		}

		return total;
	}

//	public MatrixKey getMatrixKey() {
//		return matrixKey;
//	}

//	public OperationKey getCensusOperationKey() {
//		return censusOperationKey;
//	}

	public OperationKey getQAMarkerOperationKey() {
		return qaMarkersOperationKey;
	}

//	public OperationKey getHardyWeinbergOperationKey() {
//		return hardyWeinbergOperationKey;
//	}
//
//	public double getHardyWeinbergThreshold() {
//		return hardyWeinbergThreshold;
//	}
//
//	public double getHardyWeinbergThresholdDefault() {
//		return 0.005; // XXX get from the HW oepration, or somewhere else where it is defined already!
//	}

	public GenotypeEncoder getEncoder() {
		return encoder;
	}

	public static GenotypeEncoder getEncoderDefault() {
		return GenotypicGenotypeEncoder.SINGLETON;
	}
//
//	public File getPhenotypeInfo() {
//		return phenotypeInfo;
//	}

	public int getMarkersToKeep() {
		return markersToKeep;
	}

	public int getTotalMarkers() {

		if (totalMarkers == null) {
			totalMarkers = fetchTotalMarkers(getParent());
		}

		return totalMarkers;
	}

	public int getMarkersToKeepDefault() {
		return (int) Math.ceil(getTotalMarkers() * 0.02);
	}

	public boolean isUseThresholdCalibration() {
		return useThresholdCalibration;
	}

	public boolean isUseThresholdCalibrationDefault() {
		return false;
	}

	@Override
	protected String getNameDefault() {
		return "Combi-Test for matrix " + getParent().getOrigin().toString(); // TODO use nicer matrix name!
	}
}
