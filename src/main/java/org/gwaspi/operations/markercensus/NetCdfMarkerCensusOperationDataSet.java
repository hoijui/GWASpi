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

package org.gwaspi.operations.markercensus;

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

public class NetCdfMarkerCensusOperationDataSet extends AbstractNetCdfOperationDataSet<MarkersCensusOperationEntry> implements MarkerCensusOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<char[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

	private final Logger log = LoggerFactory.getLogger(NetCdfMarkerCensusOperationDataSet.class);

	public NetCdfMarkerCensusOperationDataSet() {
		super(true);
	}

	@Override
	protected OperationFactory createOperationFactory() throws IOException {

		try {
			MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(getReadMatrixKey());

			OPType opType = OPType.MARKER_CENSUS_BY_AFFECTION;

			String description = "Genotype frequency count -" + censusName + "- on " + rdMatrixMetadata.getMatrixFriendlyName();
			if (phenoFile != null) {
				description += "\nCase/Control status read from file: " + phenoFile.getPath();
				opType = OPType.MARKER_CENSUS_BY_PHENOTYPE;
			}
			return new OperationFactory(
					rdMatrixMetadata.getStudyKey(),
					"Genotypes freq. - " + censusName, // friendly name
					description
						+ "\nSample missing ratio threshold: " + sampleMissingRatio
						+ "\nSample heterozygosity ratio threshold: " + sampleHetzygRatio
						+ "\nMarker missing ratio threshold: " + markerMissingRatio
						+ "\nDiscard mismatching Markers: " + discardMismatches
						+ "\nMarkers: " + getNumMarkers()
						+ "\nSamples: " + getNumSamples(), // description
					getNumMarkers(),
					getNumSamples(),
					0,
					opType,
					rdMatrixKey, // Parent matrixId
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
	public Collection<MarkersCensusOperationEntry> getEntries(int from, int to) {


		// PROCESS CONTROL SAMPLES
		log.info("Perform Hardy-Weinberg test (Control)");
		rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
		markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
		performHardyWeinberg(dataSet, markersCensus, true);

		// PROCESS ALTERNATE HW SAMPLES
		log.info("Perform Hardy-Weinberg test (HW-ALT)");
		rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
		markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
		performHardyWeinberg(dataSet, markersCensus, false);
		//</editor-fold>

		throw new UnsupportedOperationException("Not supported yet."); // TODO
	}
}
