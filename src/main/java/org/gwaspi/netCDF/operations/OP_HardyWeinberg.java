package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class OP_HardyWeinberg {

	private final Logger log = LoggerFactory.getLogger(OP_HardyWeinberg.class);

	public OP_HardyWeinberg() {
	}

	public int processMatrix(Operation markerCensusOP, String censusName) throws IOException, InvalidRangeException {
		int resultOpId = Integer.MIN_VALUE;

		OperationMetadata rdOPMetadata = new OperationMetadata(markerCensusOP.getOperationId());
		NetcdfFile rdNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());

		OperationSet rdOperationSet = new OperationSet(rdOPMetadata.getStudyId(), markerCensusOP.getOperationId());
		Map<String, Object> rdMarkerSetMap = rdOperationSet.getOpSetMap();
		Map<String, Object> rdSampleSetMap = rdOperationSet.getImplicitSetMap();

		NetcdfFileWriteable wrNcFile = null;
		try {
			// CREATE netCDF-3 FILE

			OperationFactory wrOPHandler = new OperationFactory(rdOPMetadata.getStudyId(),
					"Hardy-Weinberg_" + censusName, // friendly name
					"Hardy-Weinberg test on Samples marked as controls (only females for the X chromosome)\nMarkers: " + rdMarkerSetMap.size() + "\nSamples: " + rdSampleSetMap.size(), //description
					rdMarkerSetMap.size(),
					rdSampleSetMap.size(),
					0,
					cNetCDF.Defaults.OPType.HARDY_WEINBERG.toString(),
					rdOPMetadata.getParentMatrixId(), // Parent matrixId
					markerCensusOP.getOperationId()); // Parent operationId
			wrNcFile = wrOPHandler.getNetCDFHandler();

			try {
				wrNcFile.create();
			} catch (IOException ex) {
				log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
			}

			//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
			// MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(rdMarkerSetMap, cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
			} catch (IOException ex) {
				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			// MARKERSET RSID
			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

			// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
			ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

			int[] sampleOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
			} catch (IOException ex) {
				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}
			log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GET CENSUS & PERFORM HW">
//			// PROCESS ALL SAMPLES
//			rdMarkerSetMap = rdOperationSet.fillMapWithDefaultValue(rdMarkerSetMap, 0); //PURGE
//			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
//			performHardyWeinberg(wrNcFile, rdMarkerSetMap, "ALL");
//
//			// PROCESS CASE SAMPLES
//			rdMarkerSetMap = rdOperationSet.fillMapWithDefaultValue(rdMarkerSetMap, 0); //PURGE
//			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
//			performHardyWeinberg(wrNcFile, rdMarkerSetMap, "CASE");

			// PROCESS CONTROL SAMPLES
			log.info(Text.All.processing);
			rdOperationSet.fillMapWithDefaultValue(rdMarkerSetMap, 0); //PURGE
			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
			performHardyWeinberg(wrNcFile, rdMarkerSetMap, "CTRL");

			// PROCESS ALTERNATE HW SAMPLES
			log.info(Text.All.processing);
			rdOperationSet.fillMapWithDefaultValue(rdMarkerSetMap, 0); //PURGE
			rdMarkerSetMap = rdOperationSet.fillOpSetMapWithVariable(rdNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSHW);
			performHardyWeinberg(wrNcFile, rdMarkerSetMap, "HW-ALT");
			//</editor-fold>

			resultOpId = wrOPHandler.getResultOPId();
			org.gwaspi.global.Utils.sysoutCompleted("Hardy-Weinberg Equilibrium Test");
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		} catch (IOException ex) {
			log.error(null, ex);
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
					wrNcFile.close();
				} catch (IOException ex) {
					log.error("Cannot close file", ex);
				}
			}
		}

		return resultOpId;
	}

	private void performHardyWeinberg(NetcdfFileWriteable wrNcFile, Map<String, Object> markersContingencyMap, String category) {
		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<String, Object> entry : markersContingencyMap.entrySet()) {
			// HARDY-WEINBERG
			int[] contingencyTable = (int[]) entry.getValue();
			int obsAA = contingencyTable[0];
			int obsAa = contingencyTable[1];
			int obsaa = contingencyTable[2];
			int sampleNb = obsAA + obsaa + obsAa;
			double obsHzy = (double) obsAa / sampleNb;

			double fA = org.gwaspi.statistics.Utils.calculatePunnettFrequency(obsAA, obsAa, sampleNb);
			double fa = org.gwaspi.statistics.Utils.calculatePunnettFrequency(obsaa, obsAa, sampleNb);

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
			entry.setValue(store); // Re-use AAMap to store P-value value

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers on category {} at {}",
						new Object[] {markerNb, category, org.gwaspi.global.Utils.getMediumDateTimeAsString()}); // FIXME log system already supplies time
			}
		}
		log.info("Processed {} markers on category: {} at {}",
				new Object[] {markerNb, category, org.gwaspi.global.Utils.getMediumDateTimeAsString()}); // FIXME log system already supplies time

		//<editor-fold defaultstate="collapsed" desc="HARDY-WEINBERG DATA WRITER">
//		// ALL SAMPLES
//		if(category.equals("ALL")){
//			Utils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALL);
//			int[] boxes = new int[]{1,2};
//			Utils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALL);
//		}
//
//		// CASE SAMPLES
//		if(category.equals("CASE")){
//			Utils.saveArrayDoubleItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CASE);
//			int[] boxes = new int[]{1,2};
//			Utils.saveArrayDoubleD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CASE);
//		}

		// CONTROL SAMPLES
		if (category.equals("CTRL")) {
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			int[] boxes = new int[]{1, 2};
			Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_CTRL);
		}

		// HW-ALT SAMPLES
		if (category.equals("HW-ALT")) {
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, markersContingencyMap, 0, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_ALT);
			int[] boxes = new int[]{1, 2};
			Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, markersContingencyMap, boxes, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWHETZY_ALT);
		}
		//</editor-fold>
	}
}
