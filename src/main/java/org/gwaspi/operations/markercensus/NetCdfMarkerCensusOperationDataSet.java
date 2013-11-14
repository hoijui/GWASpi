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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.netCDF.operations.NetCdfUtils;
import org.gwaspi.netCDF.operations.OperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

public class NetCdfMarkerCensusOperationDataSet extends AbstractNetCdfOperationDataSet<MarkerCensusOperationEntry> implements MarkerCensusOperationDataSet {

	// - Variables.VAR_OPSET: [Collection<MarkerKey>]
	// - Variables.VAR_MARKERS_RSID: [Collection<String>]
	// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
	// - Variables.VAR_ALLELES: known alleles [Collection<char[]>]
	// - Census.VAR_OP_MARKERS_CENSUSALL: marker census - all [Collection<Census.all<== int[]>>]
	// - Census.VAR_OP_MARKERS_CENSUSCASE: marker census - case [Collection<Census.case>]
	// - Census.VAR_OP_MARKERS_CENSUSCTRL: marker census - control [Collection<Census.control>]
	// - Census.VAR_OP_MARKERS_CENSUSHW: marker census - alternate hardy-weinberg [Collection<Census.altHW>]

	private String censusName;
	private File phenoFile;
	private double sampleMissingRatio;
	private double sampleHetzygRatio;
	private double markerMissingRatio;
	private boolean discardMismatches;

	public NetCdfMarkerCensusOperationDataSet(OperationKey operationKey) {
		super(true, operationKey);
	}

	public NetCdfMarkerCensusOperationDataSet() {
		this(null);
	}

	public void setPhenoFile(File phenoFile) {
		this.phenoFile = phenoFile;
	}

	public void setCensusName(String censusName) {
		this.censusName = censusName;
	}

	public void setSampleMissingRatio(double sampleMissingRatio) {
		this.sampleMissingRatio = sampleMissingRatio;
	}

	public void setSampleHetzygRatio(double sampleHetzygRatio) {
		this.sampleHetzygRatio = sampleHetzygRatio;
	}

	public void setMarkerMissingRatio(double markerMissingRatio) {
		this.markerMissingRatio = markerMissingRatio;
	}

	public void setDiscardMismatches(boolean discardMismatches) {
		this.discardMismatches = discardMismatches;
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
					getReadMatrixKey(), // Parent matrixId
					-1); // Parent operationId
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void setMarkerCensusAll(Collection<Census> markerCensusAll) throws IOException {
		NetCdfUtils.saveIntMapD2ToWrMatrix(getNetCdfWriteFile(), markerCensusAll, Census.EXTRACTOR_ALL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
	}

	@Override
	public Collection<MarkerCensusOperationEntry> getEntries(int from, int to) {

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

	private ArrayByte.D2 netCdfKnownAlleles;
	private ArrayInt.D2 netCdfCensusAlls;
	private ArrayInt.D2 netCdfCensusesRest;

	@Override
	protected void writeEntries(int alreadyWritten, Queue<MarkerCensusOperationEntry> writeBuffer) throws IOException {

		int[] origin = new int[] {alreadyWritten, 0};
		if (netCdfCensusAlls == null) {
			// only create once, and reuse later on
			// NOTE This might be bad for multi-threading in a later stage
			netCdfKnownAlleles = new ArrayByte.D2(writeBuffer.size(), cNetCDF.Strides.STRIDE_GT);
			netCdfCensusAlls = new ArrayInt.D2(writeBuffer.size(), 4);
			netCdfCensusesRest = new ArrayInt.D2(writeBuffer.size(), 3);
		}
		int index = 0;
		try {
			for (MarkerCensusOperationEntry entry : writeBuffer) {
				Index indexObj;

				byte[] knownAlleles = entry.getKnownAlleles();
				indexObj = netCdfKnownAlleles.getIndex().set(index);
				netCdfKnownAlleles.setInt(indexObj.set(index, 0), knownAlleles[0]);
				netCdfKnownAlleles.setInt(indexObj.set(index, 1), knownAlleles[1]);
				getNetCdfWriteFile().write(cNetCDF.Variables.VAR_ALLELES, origin, netCdfKnownAlleles);


				Census censusAll = entry.getCensus().getCategoryCensus().get(Category.ALL);
				indexObj = netCdfCensusAlls.getIndex().set(index);
				netCdfCensusAlls.setInt(indexObj.set(index, 0), censusAll.getAA());
				netCdfCensusAlls.setInt(indexObj.set(index, 1), censusAll.getAa());
				netCdfCensusAlls.setInt(indexObj.set(index, 2), censusAll.getaa());
				netCdfCensusAlls.setInt(indexObj.set(index, 3), censusAll.getMissingCount());
				getNetCdfWriteFile().write(cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL, origin, netCdfCensusAlls);

				Map<Category, String> categoryNetCdfVarName = new EnumMap<Category, String>(Category.class);
				categoryNetCdfVarName.put(Category.CASE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				categoryNetCdfVarName.put(Category.CONTROL, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				categoryNetCdfVarName.put(Category.ALTERNATE, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
				for (Map.Entry<Category, String> censusEntry : categoryNetCdfVarName.entrySet()) {
					Census census = entry.getCensus().getCategoryCensus().get(censusEntry.getKey());
					indexObj = netCdfCensusesRest.getIndex().set(index);
					netCdfCensusesRest.setInt(indexObj.set(index, 0), census.getAA());
					netCdfCensusesRest.setInt(indexObj.set(index, 1), census.getAa());
					netCdfCensusesRest.setInt(indexObj.set(index, 2), census.getaa());
					getNetCdfWriteFile().write(censusEntry.getValue(), origin, netCdfCensusesRest);
				}
				index++;
			}
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		}


		throw new UnsupportedOperationException("Not supported yet.");
	}
}
