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

package org.gwaspi.operations.qamarkers;

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
import ucar.ma2.InvalidRangeException;

public class NetCdfQAMarkersOperationDataSet extends AbstractNetCdfOperationDataSet implements QAMarkersOperationDataSet {

	// - cNetCDF.Variables.VAR_OPSET: (String, key.getId()) marker keys
	// - cNetCDF.Variables.VAR_MARKERS_RSID: (String) marker RS-IDs
	// - cNetCDF.Variables.VAR_IMPLICITSET: (String, key.getSampleId() + " " + key.getFamilyId()) sample keys
	// - cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT: (double) missing ratio for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE: (int -> boolean (0->false->no_mismach, 1->true->there_is_mismatch)) mismatch state for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES: (byte) dictionary allele 1 for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ: (double) frequency of dictionary allele 1 in all the alleles for any given marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MINALLELES: (byte) dictionary allele 2 for each marker
	// - cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ: (double) frequency of dictionary allele 2 in all the alleles for any given marker
	// - cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL: ({int, int, int, int}) allele-AA, allele-Aa, allele-aa, missing-count for each marker

	public NetCdfQAMarkersOperationDataSet() {
		super(true);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

			String description = "Marker Quality Assurance on "
					+ rdMatrixMetadata.getMatrixFriendlyName()
					+ "\nMarkers: " + getNumMarkers()
					+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();
			return new OperationFactory(
					rdMatrixMetadata.getStudyKey(),
					"Marker QA", // friendly name
					description, // description
					getNumMarkers(),
					getNumSamples(),
					0,
					OPType.MARKER_QA,
					getReadMatrixKey(), // Parent matrixId
					-1); // Parent operationId
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

		ensureNcFile();
		//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, OrderedAlleles.TO_ALLELE_1, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_1_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
		NetCdfUtils.saveByteMapItemToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, OrderedAlleles.TO_ALLELE_2, cNetCDF.Strides.STRIDE_GT / 2);
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(getNetCdfWriteFile(), markerKnownAlleles, OrderedAlleles.TO_ALLELE_2_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);
	}

	@Override
	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {

		ensureNcFile();
		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
	}

	@Override
	public Collection<QAMarkersOperationEntry> getEntries() {
		XXX;
		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}
}
