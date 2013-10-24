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
import java.util.Collection;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import ucar.ma2.InvalidRangeException;

public class NetCdfHardyWeinbergOperationDataSet extends AbstractNetCdfOperationDataSet<HardyWeinbergOperationEntry> implements HardyWeinbergOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternative P-Value [Double[1]]
	// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternative Obs Hetzy & Exp Hetzy [Double[2]]

	private String hardyWeinbergName = null;
	private OperationKey markerCensusOperationKey = null;

	public NetCdfHardyWeinbergOperationDataSet() {
		super(true);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			return new OperationFactory(
					markerCensusOperationKey.getParentMatrixKey().getStudyKey(),
					"Hardy-Weinberg_" + hardyWeinbergName, // friendly name
					"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)"
						+ "\nMarkers: " + getNumMarkers() + ""
						+ "\nSamples: " + getNumSamples(), // description
					getNumMarkers(),
					getNumSamples(),
					0,
					OPType.HARDY_WEINBERG,
					markerCensusOperationKey.getParentMatrixKey(), // Parent matrixId
					markerCensusOperationKey.getId()); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void setHardyWeinbergName(String hardyWeinbergName) {
		this.hardyWeinbergName = hardyWeinbergName;
	}

	@Override
	public void setMarkerCensusOperationKey(OperationKey markerCensusOperationKey) {
		this.markerCensusOperationKey = markerCensusOperationKey;
	}

	@Override
	public void addEntry(HardyWeinbergOperationEntry entry) throws IOException {

		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, result.values(), 0, varPval);
		int[] boxes = new int[] {1, 2};
		NetCdfUtils.saveDoubleMapD2ToWrMatrix(wrNcFile, result.values(), boxes, varHetzy);
		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}

	@Override
	public Collection<HardyWeinbergOperationEntry> getEntries(int from, int to) throws IOException {
		XXX;
		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}
}
