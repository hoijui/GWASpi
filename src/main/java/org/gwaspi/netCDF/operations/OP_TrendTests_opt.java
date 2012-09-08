package org.gwaspi.netCDF.operations;

import org.gwaspi.constants.cNetCDF;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.Operation;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
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
public class OP_TrendTests_opt {

	private final static Logger log = LoggerFactory.getLogger(OP_TrendTests_opt.class);

	private OP_TrendTests_opt() {
	}

	public static int processMatrix(int _rdMatrixId,
			Operation markerCensusOP,
			Operation hwOP,
			double hwThreshold)
			throws IOException, InvalidRangeException
	{
		int resultAssocId = Integer.MIN_VALUE;

		//<editor-fold defaultstate="collapsed" desc="EXCLUSION MARKERS FROM HW">
		Map<String, Object> excludeMarkerSetLHM = new LinkedHashMap<String, Object>();
		int totalMarkerNb = 0;

		if (hwOP != null) {
			OperationMetadata hwMetadata = new OperationMetadata(hwOP.getOperationId());
			NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
			OperationSet rdHWOperationSet = new OperationSet(hwMetadata.getStudyId(), hwMetadata.getOPId());
			Map<String, Object> rdHWMarkerSetLHM = rdHWOperationSet.getOpSetLHM();
			totalMarkerNb = rdHWMarkerSetLHM.size();

			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
			rdHWMarkerSetLHM = rdHWOperationSet.fillOpSetLHMWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			for (Map.Entry<String, Object> entry : rdHWMarkerSetLHM.entrySet()) {
				double value = (Double) entry.getValue();
				if (value < hwThreshold) {
					excludeMarkerSetLHM.put(entry.getKey(), value);
				}
			}

			if (rdHWMarkerSetLHM != null) {
				rdHWMarkerSetLHM.clear();
			}
			rdHWNcFile.close();
		}
		//</editor-fold>

		if (excludeMarkerSetLHM.size() < totalMarkerNb) {  //CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
			OperationMetadata rdCensusOPMetadata = new OperationMetadata(markerCensusOP.getOperationId());
			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

			OperationSet rdCaseMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getOperationId());
			OperationSet rdCtrlMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getOperationId());
			Map<String, Object> rdSampleSetLHM = rdCaseMarkerSet.getImplicitSetLHM();
			Map<String, Object> rdCaseMarkerIdSetLHM = rdCaseMarkerSet.getOpSetLHM();
			Map<String, Object> rdCtrlMarkerIdSetLHM = rdCtrlMarkerSet.getOpSetLHM();

			Map<String, Object> wrMarkerSetLHM = new LinkedHashMap<String, Object>();
			for (String key : rdCtrlMarkerIdSetLHM.keySet()) {
				if (!excludeMarkerSetLHM.containsKey(key)) {
					wrMarkerSetLHM.put(key, "");
				}
			}

			// GATHER INFO FROM ORIGINAL MATRIX
			MatrixMetadata parentMatrixMetadata = new MatrixMetadata(markerCensusOP.getParentMatrixId());
			MarkerSet_opt rdMarkerSet = new MarkerSet_opt(parentMatrixMetadata.getStudyId(), markerCensusOP.getParentMatrixId());
			rdMarkerSet.initFullMarkerIdSetLHM();

			// retrieve chromosome info
			rdMarkerSet.fillMarkerSetLHMWithChrAndPos();
			wrMarkerSetLHM = MarkerSet_opt.replaceWithValuesFrom(wrMarkerSetLHM, rdMarkerSet.getMarkerIdSetLHM());
			Map<String, Object> rdChrInfoSetLHM = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetLHM, 0, 1);

			NetcdfFileWriteable wrOPNcFile = null;
			try {
				// CREATE netCDF-3 FILE

				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
				OperationFactory wrOPHandler = new OperationFactory(rdCensusOPMetadata.getStudyId(),
						"Cochran-Armitage Trend Test", // friendly name
						"Cochran-Armitage Trend Test on " + markerCensusOP.getOperationFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + dfSci.format(hwThreshold), //description
						wrMarkerSetLHM.size(),
						rdCensusOPMetadata.getImplicitSetSize(),
						rdChrInfoSetLHM.size(),
						cNetCDF.Defaults.OPType.TRENDTEST.toString(),
						rdCensusOPMetadata.getParentMatrixId(), // Parent matrixId
						markerCensusOP.getOperationId());       // Parent operationId
				wrOPNcFile = wrOPHandler.getNetCDFHandler();

				try {
					wrOPNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrOPNcFile.getLocation(), ex);
				}
				//log.info("Done creating netCDF handle: {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
				// MARKERSET MARKERID
				ArrayChar.D2 markersD2 = Utils.writeLHMKeysToD2ArrayChar(wrMarkerSetLHM, cNetCDF.Strides.STRIDE_MARKER_NAME);
				int[] markersOrig = new int[]{0, 0};
				try {
					wrOPNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrOPNcFile.getLocation(), ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}

				// MARKERSET RSID
				rdCaseMarkerIdSetLHM = rdCaseMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
				for (Map.Entry<String, Object> entry : wrMarkerSetLHM.entrySet()) {
					Object value = rdCaseMarkerIdSetLHM.get(entry.getKey());
					entry.setValue(value);
				}
				Utils.saveCharLHMValueToWrMatrix(wrOPNcFile, wrMarkerSetLHM, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
				ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeLHMKeysToD2ArrayChar(rdSampleSetLHM, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
				try {
					wrOPNcFile.write(cNetCDF.Variables.VAR_IMPLICITSET, sampleOrig, samplesD2);
				} catch (IOException ex) {
					log.error("Failed creating file: " + wrOPNcFile.getLocation(), ex);
				} catch (InvalidRangeException ex) {
					log.error(null, ex);
				}
				log.info("Done writing SampleSet to matrix at {}", org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time

				// WRITE CHROMOSOME INFO
				// Set of chromosomes found in matrix along with number of markersinfo
				org.gwaspi.netCDF.operations.Utils.saveCharLHMKeyToWrMatrix(wrOPNcFile, rdChrInfoSetLHM, cNetCDF.Variables.VAR_CHR_IN_MATRIX, 8);
				// Number of marker per chromosome & max pos for each chromosome
				int[] columns = new int[]{0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntLHMD2ToWrMatrix(wrOPNcFile, rdChrInfoSetLHM, columns, cNetCDF.Variables.VAR_CHR_INFO);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GET CENSUS & PERFORM TREND TESTS">
				// CLEAN LHMs FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
				Map<String, Object> wrCaseMarkerIdSetLHM = new LinkedHashMap<String, Object>();
				rdCaseMarkerIdSetLHM = rdCaseMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				if (rdCaseMarkerIdSetLHM != null) {
					for (Map.Entry<String, Object> entry : rdCaseMarkerIdSetLHM.entrySet()) {
						String key = entry.getKey();

						if (!excludeMarkerSetLHM.containsKey(key)) {
							wrCaseMarkerIdSetLHM.put(key, entry.getValue());
						}
					}
					rdCaseMarkerIdSetLHM.clear();
				}

				Map<String, Object> wrCtrlMarkerSet = new LinkedHashMap<String, Object>();
				rdCtrlMarkerIdSetLHM = rdCtrlMarkerSet.fillOpSetLHMWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				if (rdCtrlMarkerIdSetLHM != null) {
					for (Map.Entry<String, Object> entry : rdCtrlMarkerIdSetLHM.entrySet()) {
						String key = entry.getKey();

						if (!excludeMarkerSetLHM.containsKey(key)) {
							wrCtrlMarkerSet.put(key, entry.getValue());
						}
					}
					rdCtrlMarkerIdSetLHM.clear();
				}

				log.info(org.gwaspi.global.Text.All.processing);
				performTrendTest(wrOPNcFile, wrCaseMarkerIdSetLHM, wrCtrlMarkerSet);

				org.gwaspi.global.Utils.sysoutCompleted("Cochran-Armitage Trend Test");
				//</editor-fold>

				resultAssocId = wrOPHandler.getResultOPId();
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			} catch (IOException ex) {
				log.error(null, ex);
			} finally {
				if (null != rdOPNcFile) {
					try {
						rdOPNcFile.close();
						wrOPNcFile.close();
					} catch (IOException ex) {
						log.error("Cannot close file", ex);
					}
				}
			}
		} else { // NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.info(org.gwaspi.global.Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultAssocId;
	}

	protected static void performTrendTest(NetcdfFileWriteable wrNcFile, Map<String, Object> wrCaseMarkerIdSetLHM, Map<String, Object> wrCtrlMarkerSet) {
		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<String, Object> entry : wrCaseMarkerIdSetLHM.entrySet()) {
			String markerId = entry.getKey();

			int[] caseCntgTable = (int[]) entry.getValue();
			int[] ctrlCntgTable = (int[]) wrCtrlMarkerSet.get(markerId);

			// INIT VALUES

			int caseAA = caseCntgTable[0];
			int caseAa = caseCntgTable[1];
			int caseaa = caseCntgTable[2];

			int ctrlAA = ctrlCntgTable[0];
			int ctrlAa = ctrlCntgTable[1];
			int ctrlaa = ctrlCntgTable[2];

			// COCHRAN ARMITAGE TREND TEST
			double armitageT = org.gwaspi.statistics.Associations.calculateChocranArmitageTrendTest(caseAA, caseAa, caseaa, ctrlAA, ctrlAa, ctrlaa, 2); //Model 2, codominant
			double armitagePval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(armitageT, 1);  //1 Degree of freedom

			Double[] store = new Double[7];
			store[0] = armitageT;
			store[1] = armitagePval;
			entry.setValue(store); // Re-use LHM to store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers at {}", markerNb, org.gwaspi.global.Utils.getMediumDateTimeAsString()); // FIXME log system already supplies time
			}
		}

		//<editor-fold defaultstate="collapsed" desc="TREND-TEST DATA WRITER">
		int[] boxes = new int[]{0, 1};
		Utils.saveDoubleLHMD2ToWrMatrix(wrNcFile, wrCaseMarkerIdSetLHM, boxes, cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP);
		//</editor-fold>
	}
}
