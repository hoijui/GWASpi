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

package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.gui.reports.Report_Analysis;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.ChromosomeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractTestMatrixOperation implements MatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(AbstractTestMatrixOperation.class);

	private final MatrixKey rdMatrixKey;
	private final OperationMetadata markerCensusOP;
	private final OperationMetadata hwOP;
	private final double hwThreshold;
	private final String testName;
	private final OPType testType;

	public AbstractTestMatrixOperation(
			MatrixKey rdMatrixKey,
			OperationMetadata markerCensusOP,
			OperationMetadata hwOP,
			double hwThreshold,
			String testName,
			OPType testType)
	{
		this.rdMatrixKey = rdMatrixKey;
		this.markerCensusOP = markerCensusOP;
		this.hwOP = hwOP;
		this.hwThreshold = hwThreshold;
		this.testName = testName;
		this.testType = testType;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getProblemDescription() {
		return null;
	}

	@Override
	public int processMatrix() throws IOException {
		int resultAssocId = Integer.MIN_VALUE;

		Collection<MarkerKey> toBeExcluded = new HashSet<MarkerKey>();
		boolean dataLeft = excludeMarkersByHW(hwOP, hwThreshold, toBeExcluded);

		if (dataLeft) { // CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
			OperationKey markerCensusOPKey = OperationKey.valueOf(markerCensusOP);
			OperationMetadata rdCensusOPMetadata = OperationsList.getOperation(markerCensusOPKey);
			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

			MarkerOperationSet rdCaseMarkerSet = new MarkerOperationSet(markerCensusOPKey);
			MarkerOperationSet rdCtrlMarkerSet = new MarkerOperationSet(markerCensusOPKey);
			Map<SampleKey, ?> rdSampleSetMap = rdCaseMarkerSet.getImplicitSetMap();
			rdCaseMarkerSet.getOpSetMap(); // without this, we get an NPE later on
			Map<MarkerKey, char[]> rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.getOpSetMap();

			Map<MarkerKey, MarkerMetadata> wrMarkerMetadata = new LinkedHashMap<MarkerKey, MarkerMetadata>();
			for (MarkerKey key : rdCtrlMarkerIdSetMap.keySet()) {
				if (!toBeExcluded.contains(key)) {
					wrMarkerMetadata.put(key, null);
				}
			}

			// GATHER INFO FROM ORIGINAL MATRIX
			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(markerCensusOP.getParentMatrixKey());
			MarkerSet rdMarkerSet = new MarkerSet(MatrixKey.valueOf(parentMatrixMetadata));
			rdMarkerSet.initFullMarkerIdSetMap();

			// retrieve chromosome info
			rdMarkerSet.fillMarkerSetMapWithChrAndPos();
			MarkerSet.replaceWithValuesFrom(wrMarkerMetadata, rdMarkerSet.getMarkerMetadata());
			Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = ChromosomeUtils.aggregateChromosomeInfo(wrMarkerMetadata, 0, 1);

			NetcdfFileWriteable wrOPNcFile = null;
			try {
				// CREATE netCDF-3 FILE
				OperationFactory wrOPHandler = new OperationFactory(
						rdCensusOPMetadata.getStudyKey(),
						testName, // friendly name
						testName + " on " + markerCensusOP.getFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + Report_Analysis.FORMAT_SCIENTIFIC.format(hwThreshold), // description
						wrMarkerMetadata.size(),
						rdCensusOPMetadata.getImplicitSetSize(),
						chromosomeInfo.size(),
						testType,
						rdCensusOPMetadata.getParentMatrixKey(), // Parent matrixId
						markerCensusOP.getId()); // Parent operationId

				// what will be written to the operation NetCDF file (wrOPNcFile):
				// - Variables.VAR_OPSET: wrMarkerMetadata.keySet() [Collection<MarkerKey>]
				// - Variables.VAR_MARKERS_RSID: markers RS ID from the rd marker census opertion, sorted by wrMarkerMetadata.keySet() [Collection<String>]
				// - Variables.VAR_IMPLICITSET: "implicit set", rdSampleSetMap.keySet(), original sample keys [Collection<SampleKey>]
				// - Variables.VAR_CHR_IN_MATRIX: chromosomeInfo.keySet() [Collection<ChromosomeKey>]
				// - Variables.VAR_CHR_INFO: chromosomeInfo.values() [Collection<ChromosomeInfo>]
				// switch (test-type) {
				//   case "allelic association test": Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR: {T, P-Value, OR} [Double[3]]
				//   case "genotypic association test": Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR: {T, P-Value, OR-1, OR-2} [Double[4]]
				//   case "trend test": Association.VAR_OP_MARKERS_ASTrendTestTP: {T, P-Value} [Double[2]]
				// }

				wrOPNcFile = wrOPHandler.getNetCDFHandler();
				wrOPNcFile.create();
				log.trace("Done creating netCDF handle: " + wrOPNcFile.toString());

				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(wrMarkerMetadata.keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				wrOPNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);

				// MARKERSET RSID
				Map<MarkerKey, char[]> rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
				Map<MarkerKey, char[]> sortedCaseMarkerIds = org.gwaspi.global.Utils.createOrderedMap(wrMarkerMetadata.keySet(), rdCaseMarkerIdSetMap);
				NetCdfUtils.saveCharMapValueToWrMatrix(wrOPNcFile, sortedCaseMarkerIds.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// WRITE SAMPLESET TO MATRIX FROM SAMPLES
				ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
				int[] sampleOrig = new int[] {0, 0};
				wrOPNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
				log.info("Done writing SampleSet to matrix");

				// WRITE CHROMOSOME INFO
				// Set of chromosomes found in matrix along with number of markersinfo
				NetCdfUtils.saveObjectsToStringToMatrix(wrOPNcFile, chromosomeInfo.keySet(), cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[] {0, 1, 2, 3};
				NetCdfUtils.saveChromosomeInfosD2ToWrMatrix(wrOPNcFile, chromosomeInfo.values(), columns, cNetCDF.Variables.VAR_CHR_INFO);
				//</editor-fold>

				//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM TESTS">
				// CLEAN Maps FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
				Map<MarkerKey, int[]> rdMarkerCensusCases = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				Map<MarkerKey, int[]> wrCaseMarkerIdSetMap = filter(rdMarkerCensusCases, toBeExcluded);

				Map<MarkerKey, int[]> rdMarkerCensusCtrls = rdCtrlMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				Map<MarkerKey, int[]> wrCtrlMarkerSet = filter(rdMarkerCensusCtrls, toBeExcluded);

				org.gwaspi.global.Utils.sysoutStart(testName);
				performTest(wrOPNcFile, wrCaseMarkerIdSetMap, wrCtrlMarkerSet);
				org.gwaspi.global.Utils.sysoutCompleted(testName);
				//</editor-fold>

				resultAssocId = wrOPHandler.getResultOPId();
			} catch (InvalidRangeException ex) {
				throw new IOException(ex);
			} finally {
				try {
					if (rdOPNcFile != null) {
						rdOPNcFile.close();
					}
					if (wrOPNcFile != null) {
						wrOPNcFile.close();
					}
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}
		} else { // NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultAssocId;
	}

	private static <K, V> Map<K, V> filter(Map<K, V> toBeFiltered, Collection<K> toBeExcluded) {

		Map<K, V> filtered = new LinkedHashMap<K, V>();
		if (toBeFiltered != null) {
			for (Map.Entry<K, V> entry : toBeFiltered.entrySet()) {
				K key = entry.getKey();

				if (!toBeExcluded.contains(key)) {
					filtered.put(key, entry.getValue());
				}
			}
		}

		return filtered;
	}

	static boolean excludeMarkersByHW(OperationMetadata hwOP, double hwThreshold, Collection<MarkerKey> excludeMarkerSetMap) throws IOException {

		excludeMarkerSetMap.clear();
		int totalMarkerNb = 0;

		if (hwOP != null) {
			NetcdfFile rdHWNcFile = NetcdfFile.open(hwOP.getPathToMatrix());
			MarkerOperationSet rdHWOperationSet = new MarkerOperationSet(OperationKey.valueOf(hwOP));
			Map<MarkerKey, Double> rdHWMarkerSetMap = rdHWOperationSet.getOpSetMap();
			totalMarkerNb = rdHWMarkerSetMap.size();

			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
			rdHWMarkerSetMap = rdHWOperationSet.fillOpSetMapWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			totalMarkerNb = rdHWMarkerSetMap.size();
			for (Map.Entry<MarkerKey, Double> entry : rdHWMarkerSetMap.entrySet()) {
				double value = entry.getValue();
				if (value < hwThreshold) {
					excludeMarkerSetMap.add(entry.getKey());
				}
			}
			rdHWNcFile.close();
		}

		return (excludeMarkerSetMap.size() < totalMarkerNb);
	}

	/**
	 * Performs actual Test.
	 * @param wrNcFile
	 * @param wrCaseMarkerIdSetMap
	 * @param wrCtrlMarkerSet
	 */
	protected abstract void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, int[]> wrCaseMarkerIdSetMap, Map<MarkerKey, int[]> wrCtrlMarkerSet) throws IOException;
}
