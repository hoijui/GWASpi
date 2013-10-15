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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.NetCdfHardyWeinbergOperationDataSet;
import org.gwaspi.statistics.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import ucar.ma2.ArrayChar;
//import ucar.ma2.InvalidRangeException;
//import ucar.nc2.NetcdfFile;
//import ucar.nc2.NetcdfFileWriteable;

public class OP_HardyWeinberg implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_HardyWeinberg.class);

	private final OperationKey markerCensusOPKey;
	private final String censusName;

	public OP_HardyWeinberg(OperationKey markerCensusOPKey, String censusName) {

		this.markerCensusOPKey = markerCensusOPKey;
		this.censusName = censusName;
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
		int resultOpId = Integer.MIN_VALUE;

		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markerCensusOPKey.getId());
		NetcdfFile rdNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());

		MarkerOperationSet rdOperationSet = new MarkerOperationSet(markerCensusOPKey);
		Map<MarkerKey, char[]> rdMarkerSetMap = rdOperationSet.getOpSetMap();
		Map<SampleKey, ?> rdSampleSetMap = rdOperationSet.getImplicitSetMap();

		NetcdfFileWriteable wrNcFile = null;
		try {
			HardyWeinbergOperationDataSet dataSet = new NetCdfHardyWeinbergOperationDataSet(); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setReadMatrixKey(rdMatrixKey); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(rdMarkerSetMap.size()); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumSamples(rdSampleSetMap.size()); // HACK

			dataSet.setMarkers(rdMarkerSet.getMarkerKeys());
			dataSet.setSamples(rdSampleSetMap.keySet());
			dataSet.setChromosomes(rdMarkerSet.getChrInfoSetMap().keySet());

//
//			OperationFactory wrOPHandler = new OperationFactory(
//					markerCensusOPKey.getParentMatrixKey().getStudyKey(),
//					"Hardy-Weinberg_" + censusName, // friendly name
//					"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)\nMarkers: " + rdMarkerSetMap.size() + "\nSamples: " + rdSampleSetMap.size(), //description
//					rdMarkerSetMap.size(),
//					rdSampleSetMap.size(),
//					0,
//					OPType.HARDY_WEINBERG,
//					markerCensusOPKey.getParentMatrixKey(), // Parent matrixId
//					markerCensusOPKey.getId()); // Parent operationId

			// what will be written to the operation NetCDF file (wrNcFile):
			// - Variables.VAR_OPSET: [Collection<MarkerKey>]
			// - Variables.VAR_MARKERS_RSID: [Collection<String>]
			// - Variables.VAR_IMPLICITSET: [Collection<SampleKey>]
			// - HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL: Control P-Value [Double[1]]
			// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL: Control Obs Hetzy & Exp Hetzy [Double[2]]
			// - HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT: Hardy-Weinberg Alternative P-Value [Double[1]]
			// - HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT: Hardy-Weinberg Alternative Obs Hetzy & Exp Hetzy [Double[2]]

			wrNcFile = wrOPHandler.getNetCDFHandler();
			wrNcFile.create();
			log.trace("Done creating netCDF handle: " + wrNcFile.toString());

			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
			// MARKERSET MARKERID
			ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSetMap.keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);

			// MARKERSET RSID
			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
			NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSetMap.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
			ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
			int[] sampleOrig = new int[] {0, 0};
			wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
			log.info("Done writing SampleSet to matrix");
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM HW">
			Map<MarkerKey, int[]> markersCensus;
//			// PROCESS ALL SAMPLES
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
//			performHardyWeinberg(wrNcFile, markersCensus, "ALL");
//
//			// PROCESS CASE SAMPLES
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
//			performHardyWeinberg(wrNcFile, markersCensus, "CASE");

			// PROCESS CONTROL SAMPLES
			log.info("Perform Hardy-Weinberg test (Control)");
			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
			performHardyWeinberg(wrNcFile, markersCensus, "CTRL");

			// PROCESS ALTERNATE HW SAMPLES
			log.info("Perform Hardy-Weinberg test (HW-ALT)");
			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
			performHardyWeinberg(wrNcFile, markersCensus, "HW-ALT");
			//</editor-fold>

			resultOpId = wrOPHandler.getResultOPId();
			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
		} catch (InvalidRangeException ex) {
			throw new IOException(ex);
		} finally {
			if (rdNcFile != null) {
				try {
					rdNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}
			if (wrNcFile != null) {
				try {
					wrNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}
		}

		return resultOpId;
	}

	private void performHardyWeinberg(NetcdfFileWriteable wrNcFile, Map<MarkerKey, int[]> markersContingencyMap, String category) throws IOException {
		// Iterate through markerset
		int markerNb = 0;
		Map<MarkerKey, Double[]> result = new LinkedHashMap<MarkerKey, Double[]>(markersContingencyMap.size());
		for (Map.Entry<MarkerKey, int[]> entry : markersContingencyMap.entrySet()) {
			// HARDY-WEINBERG
			int[] contingencyTable = entry.getValue();
			int obsAA = contingencyTable[0];
			int obsAa = contingencyTable[1];
			int obsaa = contingencyTable[2];
			int sampleNb = obsAA + obsaa + obsAa;
			double obsHzy = (double) obsAa / sampleNb;

			double fA = StatisticsUtils.calculatePunnettFrequency(obsAA, obsAa, sampleNb);
			double fa = StatisticsUtils.calculatePunnettFrequency(obsaa, obsAa, sampleNb);

			double pAA = fA * fA;
			double pAa = 2 * fA * fa;
			double paa = fa * fa;

			double expAA = pAA * sampleNb;
			double expAa = pAa * sampleNb;
			double expaa = paa * sampleNb;
			double expHzy = pAa;

			double chiSQ = org.gwaspi.statistics.Chisquare.calculateHWChiSquare(obsAA, expAA, obsAa, expAa, obsaa, expaa);
			double pvalue = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(chiSQ, 1);

			Double[] store = new Double[3];
			store[0] = pvalue;
			store[1] = obsHzy;
			store[2] = expHzy;
			result.put(entry.getKey(), store); // store P-value

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers on category {}", markerNb, category);
			}
		}
		log.info("Processed {} markers on category: {}", markerNb, category);

		//<editor-fold defaultstate="expanded" desc="HARDY-WEINBERG DATA WRITER">
//		// ALL SAMPLES
//		if(category.equals("ALL")){
//			NetCdfUtils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);
//			int[] boxes = new int[]{1,2};
//			NetCdfUtils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);
//		}
//
//		// CASE SAMPLES
//		if(category.equals("CASE")){
//			NetCdfUtils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);
//			int[] boxes = new int[]{1,2};
//			NetCdfUtils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);
//		}


		String varPval;
		String varHetzy;
		if (category.equals("CTRL")) {
			// CONTROL SAMPLES
			varPval = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL;
			varHetzy = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL;
		} else if (category.equals("HW-ALT")) {
			// HW-ALT SAMPLES
			varPval = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT;
			varHetzy = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT;
		} else {
			throw new IllegalArgumentException("unknown cathegory: " + category);
		}
		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, result.values(), 0, varPval);
		int[] boxes = new int[] {1, 2};
		NetCdfUtils.saveDoubleMapD2ToWrMatrix(wrNcFile, result.values(), boxes, varHetzy);
	}
}
