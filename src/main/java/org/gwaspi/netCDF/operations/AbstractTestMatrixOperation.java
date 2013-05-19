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
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public abstract class AbstractTestMatrixOperation implements MatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(AbstractTestMatrixOperation.class);

	private int rdMatrixId;
	private Operation markerCensusOP;
	private Operation hwOP;
	private double hwThreshold;
	private String testName;
	private OPType testType;

	public AbstractTestMatrixOperation(
			int rdMatrixId,
			Operation markerCensusOP,
			Operation hwOP,
			double hwThreshold,
			String testName,
			OPType testType)
	{
		this.rdMatrixId = rdMatrixId;
		this.markerCensusOP = markerCensusOP;
		this.hwOP = hwOP;
		this.hwThreshold = hwThreshold;
		this.testName = testName;
		this.testType = testType;
	}

	@Override
	public int processMatrix() throws IOException, InvalidRangeException {
		int resultAssocId = Integer.MIN_VALUE;

		Collection<MarkerKey> toBeExcluded = new HashSet<MarkerKey>();
		boolean dataLeft = excludeMarkersByHW(hwOP, hwThreshold, toBeExcluded);

		if (dataLeft) { // CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
			OperationMetadata rdCensusOPMetadata = OperationsList.getOperationMetadata(markerCensusOP.getId());
			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

			MarkerOperationSet rdCaseMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			MarkerOperationSet rdCtrlMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			Map<SampleKey, ?> rdSampleSetMap = rdCaseMarkerSet.getImplicitSetMap();
			rdCaseMarkerSet.getOpSetMap(); // without this, we get an NPE later on
			Map<MarkerKey, char[]> rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.getOpSetMap();

			Map<MarkerKey, MarkerMetadata> wrMarkerSetMap = new LinkedHashMap<MarkerKey, MarkerMetadata>();
			for (MarkerKey key : rdCtrlMarkerIdSetMap.keySet()) {
				if (!toBeExcluded.contains(key)) {
					wrMarkerSetMap.put(key, null);
				}
			}

			// GATHER INFO FROM ORIGINAL MATRIX
			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(markerCensusOP.getParentMatrixId());
			MarkerSet rdMarkerSet = new MarkerSet(parentMatrixMetadata.getStudyId(), markerCensusOP.getParentMatrixId());
			rdMarkerSet.initFullMarkerIdSetMap();

			// retrieve chromosome info
			rdMarkerSet.fillMarkerSetMapWithChrAndPos();
			MarkerSet.replaceWithValuesFrom(wrMarkerSetMap, rdMarkerSet.getMarkerMetadata());
			Map<MarkerKey, int[]> rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetMap, 0, 1);

			NetcdfFileWriteable wrOPNcFile = null;
			try {
				// CREATE netCDF-3 FILE
				OperationFactory wrOPHandler = new OperationFactory(
						rdCensusOPMetadata.getStudyId(),
						testName, // friendly name
						testName + " on " + markerCensusOP.getFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + Report_Analysis.FORMAT_SCIENTIFIC.format(hwThreshold), // description
						wrMarkerSetMap.size(),
						rdCensusOPMetadata.getImplicitSetSize(),
						rdChrInfoSetMap.size(),
						testType,
						rdCensusOPMetadata.getParentMatrixId(), // Parent matrixId
						markerCensusOP.getId()); // Parent operationId
				wrOPNcFile = wrOPHandler.getNetCDFHandler();

				try {
					wrOPNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrOPNcFile.getLocation(), ex);
				}

				//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(wrMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				try {
					wrOPNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error("Failed writing file", ex);
				}

				// MARKERSET RSID
				Map<MarkerKey, char[]> rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
				Map<MarkerKey, char[]> sortedCaseMarkerIds = org.gwaspi.global.Utils.createOrderedMap(wrMarkerSetMap, rdCaseMarkerIdSetMap);
				Utils.saveCharMapValueToWrMatrix(wrOPNcFile, sortedCaseMarkerIds, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
				ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[] {0, 0};
				try {
					wrOPNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed writing file", ex);
				} catch (InvalidRangeException ex) {
					log.error("Failed writing file", ex);
				}
				log.info("Done writing SampleSet to matrix");

				// WRITE CHROMOSOME INFO
				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharMapKeyToWrMatrix(wrOPNcFile, rdChrInfoSetMap, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[] {0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrOPNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);
				//</editor-fold>

				//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM TESTS">
				// CLEAN Maps FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
				Map<MarkerKey, int[]> rdMarkerCensusCases = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				Map<MarkerKey, int[]> wrCaseMarkerIdSetMap = filter(rdMarkerCensusCases, toBeExcluded);

				Map<MarkerKey, int[]> rdMarkerCensusCtrls = rdCtrlMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				Map<MarkerKey, int[]> wrCtrlMarkerSet = filter(rdMarkerCensusCtrls, toBeExcluded);

				log.info(Text.All.processing);
				performTest(wrOPNcFile, wrCaseMarkerIdSetMap, wrCtrlMarkerSet);

				org.gwaspi.global.Utils.sysoutCompleted(testName);
				//</editor-fold>

				resultAssocId = wrOPHandler.getResultOPId();
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			} catch (IOException ex) {
				log.error(null, ex);
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

	static boolean excludeMarkersByHW(Operation hwOP, double hwThreshold, Collection<MarkerKey> excludeMarkerSetMap) throws IOException {

		excludeMarkerSetMap.clear();
		int totalMarkerNb = 0;

		if (hwOP != null) {
			OperationMetadata hwMetadata = OperationsList.getOperationMetadata(hwOP.getId());
			NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
			MarkerOperationSet rdHWOperationSet = new MarkerOperationSet(hwMetadata.getStudyId(), hwMetadata.getOPId());
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
	protected abstract void performTest(NetcdfFileWriteable wrNcFile, Map<MarkerKey, int[]> wrCaseMarkerIdSetMap, Map<MarkerKey, int[]> wrCtrlMarkerSet);
}
