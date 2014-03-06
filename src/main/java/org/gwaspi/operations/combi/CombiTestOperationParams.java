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

	/**
	 * This is our direct parent.
	 */
	private final OperationKey qaMarkersOperationKey;
	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final GenotypeEncoder encoder;
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
			OperationKey qaMarkersOperationKey,
			GenotypeEncoder encoder,
			Integer markersToKeep,
			Boolean useThresholdCalibration,
			String name)
	{
		super(new DataSetKey(qaMarkersOperationKey), name);

		this.qaMarkersOperationKey = qaMarkersOperationKey;
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
	}

	public CombiTestOperationParams(OperationKey qaMarkersOperationKey)
	{
		this(
				qaMarkersOperationKey,
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

	public OperationKey getQAMarkerOperationKey() {
		return qaMarkersOperationKey;
	}

	public GenotypeEncoder getEncoder() {
		return encoder;
	}

	public static GenotypeEncoder getEncoderDefault() {
		return GenotypicGenotypeEncoder.SINGLETON;
	}

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
