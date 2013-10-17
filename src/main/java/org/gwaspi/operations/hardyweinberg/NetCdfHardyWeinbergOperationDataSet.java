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

package org.gwaspi.operations.hardyweinberg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class NetCdfHardyWeinbergOperationDataSet extends AbstractNetCdfOperationDataSet implements HardyWeinbergOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternative P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternative Obs Hetzy & Exp Hetzy [Double[2]]

	private final Logger log = LoggerFactory.getLogger(NetCdfHardyWeinbergOperationDataSet.class);

	public NetCdfHardyWeinbergOperationDataSet() {
		super(true);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			return new OperationFactory(
					markerCensusOPKey.getParentMatrixKey().getStudyKey(),
					"Hardy-Weinberg_" + censusName, // friendly name
					"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)"
						+ "\nMarkers: " + getNumMarkers() + ""
						+ "\nSamples: " + getNumSamples(), // description
					getNumMarkers(),
					getNumSamples(),
					0,
					OPType.HARDY_WEINBERG,
					markerCensusOPKey.getParentMatrixKey(), // Parent matrixId
					markerCensusOPKey.getId()); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void setMarkerMissingRatios(Collection<Double> markerMissingRatios) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveDoubleMapD1ToWrMatrix(getNetCdfWriteFile(), markerMissingRatios, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);
	}

	@Override
	public void setMarkerMismatchStates(Collection<Boolean> markerMismatchStates) throws IOException {

		Collection<Integer> markerMismatchIntegerStates
				= new ArrayList<Integer>(markerMismatchStates.size()); // XXX not sooooo nice! maybe use a converter while writing (saves memory)
		for (boolean mismatch : markerMismatchStates) {
			markerMismatchIntegerStates.add(mismatch
					? cNetCDF.Defaults.DEFAULT_MISMATCH_YES
					: cNetCDF.Defaults.DEFAULT_MISMATCH_NO);
		}
		ensureNcFile();
		NetCdfUtils.saveIntMapD1ToWrMatrix(getNetCdfWriteFile(), markerMismatchIntegerStates, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);
	}

	@Override
	public void setMarkerKnownAlleles(Collection<OrderedAlleles> markerKnownAlleles) throws IOException {

		//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, OrderedAlleles.TO_ALLELE_1, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_1_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, OrderedAlleles.TO_ALLELE_2, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_2_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);
	}

	@Override
	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {
		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
	}

	@Override
	public Collection<QAMarkersOperationEntry> getEntries() {
		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}
}
