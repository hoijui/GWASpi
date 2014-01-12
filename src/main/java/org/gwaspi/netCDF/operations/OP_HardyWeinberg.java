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
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.OperationKey;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.hardyweinberg.DefaultHardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationDataSet;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationEntry.Category;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.markercensus.MarkerCensusOperationEntry;
import org.gwaspi.operations.markercensus.NetCdfMarkerCensusOperationDataSet;
import org.gwaspi.statistics.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_HardyWeinberg extends AbstractOperation<HardyWeinbergOperationDataSet> {

	private final Logger log = LoggerFactory.getLogger(OP_HardyWeinberg.class);

	private final OperationKey markerCensusOPKey;
	private final String hwName;

	public OP_HardyWeinberg(OperationKey markerCensusOPKey, String hwName) {
		super(markerCensusOPKey);

		this.markerCensusOPKey = markerCensusOPKey;
		this.hwName = hwName;
	}

	@Override
	public OPType getType() {
		return OPType.HARDY_WEINBERG;
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

//		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(markerCensusOPKey.getId());
//		NetcdfFile rdNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());

//		MarkerCensusOperationDataSet rdmarkerCensusOpDS
//				= new NetCdfMarkerCensusOperationDataSet(markerCensusOPKey);

		MarkerCensusOperationDataSet markerCensusOperationDataSet = new NetCdfMarkerCensusOperationDataSet(markerCensusOPKey);

		try {
			HardyWeinbergOperationDataSet dataSet = generateFreshOperationDataSet();
			((AbstractNetCdfOperationDataSet) dataSet).setReadOperationKey(markerCensusOPKey); // HACK
			dataSet.setNumMarkers(markerCensusOperationDataSet.getNumMarkers());
			dataSet.setNumChromosomes(markerCensusOperationDataSet.getNumChromosomes());
			dataSet.setNumSamples(markerCensusOperationDataSet.getNumSamples());

			dataSet.setHardyWeinbergName(hwName); // HACK
			dataSet.setMarkerCensusOperationKey(markerCensusOPKey); // HACK

//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllMarkersFromParent(true);
//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllSamplesFromParent(true);
//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllChromosomesFromParent(true);

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

//			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
//			// MARKERSET MARKERID
//			ArrayChar.D2 markersD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdMarkerSetMap.keySet(), cNetCDF.Strides.STRIDE_MARKER_NAME);
//			int[] markersOrig = new int[] {0, 0};
//			wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
//
//			// MARKERSET RSID
//			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
//			NetCdfUtils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSetMap.values(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);
//
//			// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
//			ArrayChar.D2 samplesD2 = NetCdfUtils.writeCollectionToD2ArrayChar(rdSampleSetMap.keySet(), cNetCDF.Strides.STRIDE_SAMPLE_NAME);
//			int[] sampleOrig = new int[] {0, 0};
//			wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
//			log.info("Done writing SampleSet to matrix");
//			//</editor-fold>


			//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM HW">
//			Map<MarkerKey, int[]> markersCensus;
			Collection<MarkerCensusOperationEntry> markersCensus = markerCensusOperationDataSet.getEntries();
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
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
			performHardyWeinberg(dataSet, markersCensus, Category.CONTROL);

			// PROCESS ALTERNATE HW SAMPLES
			log.info("Perform Hardy-Weinberg test (HW-ALT)");
//			rdOperationSet.fillOpSetMapWithDefaultValue(new int[0]); // PURGE
//			markersCensus = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
			performHardyWeinberg(dataSet, markersCensus, Category.ALTERNATE);
			//</editor-fold>

			dataSet.finnishWriting();
			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK
			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
//		} catch (InvalidRangeException ex) {
//			throw new IOException(ex);
		} finally {
//			if (rdNcFile != null) {
//				try {
//					rdNcFile.close();
//				} catch (IOException ex) {
//					log.warn("Cannot close file", ex);
//				}
//			}
		}

		return resultOpId;
	}

	private void performHardyWeinberg(HardyWeinbergOperationDataSet dataSet, Collection<MarkerCensusOperationEntry> markersContingencyMap, Category category) throws IOException {

		final String categoryName;
//		final String varPval;
//		final String varHetzy;
		switch(category) {
			default:
			case ALL:
			case CASE:
				throw new UnsupportedOperationException();
			case CONTROL:
				// CONTROL SAMPLES
				categoryName = "CTRL";
//				varPval = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL;
//				varHetzy = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL;
				break;
			case ALTERNATE:
				// HW-ALT SAMPLES
				categoryName = "HW-ALT";
//				varPval = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT;
//				varHetzy = cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT;
				break;
		}

		// Iterate through markerset
		int markerNb = 0;
		for (MarkerCensusOperationEntry entry : markersContingencyMap) {
			// HARDY-WEINBERG
			Census census = entry.getCensus().getCategoryCensus().get(category);
			int obsAA = census.getAA();
			int obsAa = census.getAa();
			int obsaa = census.getaa();
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

			HardyWeinbergOperationEntry hwEntry = new DefaultHardyWeinbergOperationEntry(entry.getKey(), entry.getIndex(), category, pvalue, obsHzy, expHzy);
			dataSet.addEntry(hwEntry);
//			Double[] store = new Double[3];
//			store[0] = pvalue;
//			store[1] = obsHzy;
//			store[2] = expHzy;
//			result.put(entry.getKey(), store); // store P-value

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers on category {}", markerNb, categoryName);
			}
		}
		log.info("Processed {} markers on category: {}", markerNb, categoryName);

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

//		NetCdfUtils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, result.values(), 0, varPval);
//		int[] boxes = new int[] {1, 2};
//		NetCdfUtils.saveDoubleMapD2ToWrMatrix(wrNcFile, result.values(), boxes, varHetzy);
	}
}
