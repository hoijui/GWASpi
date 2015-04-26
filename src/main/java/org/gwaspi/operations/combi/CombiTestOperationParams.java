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
import org.gwaspi.constants.NetCDFConstants.Defaults.OPType;
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
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final GenotypeEncoder encoder;
	private final GenotypeEncodingParams encodingParams;
	/**
	 * The number of total markers in the matrix we operate on, unfiltered.
	 */
	private Integer totalMarkers;
	/**
	 * Whether to use resampling based threshold calibration.
	 * This feature takes a lot of computation time!
	 */
	private final boolean useThresholdCalibration;
	/**
	 * Whether to use run the SVM once per chromosome, or rather over the whole genome at once.
	 */
	private final Boolean perChromosome;

	public CombiTestOperationParams(
			OperationKey qaMarkersOperationKey,
			GenotypeEncoder encoder,
			final GenotypeEncodingParams encodingParams,
			Boolean useThresholdCalibration,
			final Boolean perChromosome,
			String name)
	{
		super(OPType.COMBI_ASSOC_TEST, new DataSetKey(qaMarkersOperationKey), name);

		this.encoder = (encoder == null)
				? getEncoderDefault()
				: encoder;
		this.encodingParams = (encodingParams == null)
				? getEncodingParamsDefault()
				: encodingParams;
		this.useThresholdCalibration = (useThresholdCalibration == null)
				? isUseThresholdCalibrationDefault()
				: useThresholdCalibration;
		this.perChromosome = (perChromosome == null)
				? isPerChromosomeDefault()
				: perChromosome;
	}

	public CombiTestOperationParams(OperationKey qaMarkersOperationKey) {

		this(
				qaMarkersOperationKey,
				null,
				null,
				null,
				null,
				null
				);
	}

	static int fetchTotalMarkers(DataSetKey parentKey) {

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

	/** @deprecated */
	public OperationKey getQAMarkerOperationKey() {
		return getParent().getOperationParent();
	}

	public GenotypeEncoder getEncoder() {
		return encoder;
	}

	public static GenotypeEncoder getEncoderDefault() {
		return GenotypicGenotypeEncoder.SINGLETON;
	}

	public GenotypeEncodingParams getEncodingParams() {
		return encodingParams;
	}

	public static GenotypeEncodingParams getEncodingParamsDefault() {
		return new GenotypeEncodingParams();
	}

	public int getTotalMarkers() {

		if (totalMarkers == null) {
			totalMarkers = fetchTotalMarkers(getParent());
		}

		return totalMarkers;
	}

	public boolean isUseThresholdCalibration() {
		return useThresholdCalibration;
	}

	public static boolean isUseThresholdCalibrationDefault() {
		return false;
	}

	public boolean isPerChromosome() {
		return perChromosome;
	}

	public static boolean isPerChromosomeDefault() {
		return true;
	}

	@Override
	protected String getNameDefault() {
		return "Combi-Test for matrix " + getParent().getOrigin().toString(); // TODO use nicer matrix name!
	}
}
