package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
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
public class OP_AllelicAssociationTests_opt implements MatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(OP_AllelicAssociationTests_opt.class);

	private int rdMatrixId;
	private Operation markerCensusOP;
	private Operation hwOP;
	private double hwThreshold;

	public OP_AllelicAssociationTests_opt(
			int rdMatrixId,
			Operation markerCensusOP,
			Operation hwOP,
			double hwThreshold)
	{
		this.rdMatrixId = rdMatrixId;
		this.markerCensusOP = markerCensusOP;
		this.hwOP = hwOP;
		this.hwThreshold = hwThreshold;
	}

	public int processMatrix() throws IOException, InvalidRangeException {
		int resultAssocId = Integer.MIN_VALUE;

		//<editor-fold defaultstate="collapsed" desc="EXCLUSION MARKERS FROM HW">
		Map<String, Object> excludeMarkerSetMap = new LinkedHashMap<String, Object>();
		int totalMarkerNb = 0;

		if (hwOP != null) {
			OperationMetadata hwMetadata = OperationsList.getOperationMetadata(hwOP.getId());
			NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
			OperationSet rdHWOperationSet = new OperationSet(hwMetadata.getStudyId(), hwMetadata.getOPId());
			Map<String, Object> rdHWMarkerSetMap = rdHWOperationSet.getOpSetMap();
			totalMarkerNb = rdHWMarkerSetMap.size();

			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
			rdHWMarkerSetMap = rdHWOperationSet.fillOpSetMapWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			for (Map.Entry<String, Object> entry : rdHWMarkerSetMap.entrySet()) {
				double value = (Double) entry.getValue();
				if (value < hwThreshold) {
					excludeMarkerSetMap.put(entry.getKey(), value);
				}
			}

			if (rdHWMarkerSetMap != null) {
				rdHWMarkerSetMap.clear();
			}
			rdHWNcFile.close();
		}
		//</editor-fold>

		if (excludeMarkerSetMap.size() < totalMarkerNb) { // CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
			OperationMetadata rdCensusOPMetadata = OperationsList.getOperationMetadata(markerCensusOP.getId());
			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

			OperationSet rdCaseMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			OperationSet rdCtrlMarkerSet = new OperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			Map<String, Object> rdSampleSetMap = rdCaseMarkerSet.getImplicitSetMap();
			Map<String, Object> rdCaseMarkerIdSetMap = rdCaseMarkerSet.getOpSetMap();
			Map<String, Object> rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.getOpSetMap();

			Map<String, Object> wrMarkerSetMap = new LinkedHashMap<String, Object>();
			for (String key : rdCtrlMarkerIdSetMap.keySet()) {
				if (!excludeMarkerSetMap.containsKey(key)) {
					wrMarkerSetMap.put(key, "");
				}
			}

			// GATHER INFO FROM ORIGINAL MATRIX
			MatrixMetadata parentMatrixMetadata = MatricesList.getMatrixMetadataById(markerCensusOP.getParentMatrixId());
			MarkerSet_opt rdMarkerSet = new MarkerSet_opt(parentMatrixMetadata.getStudyId(), markerCensusOP.getParentMatrixId());
			rdMarkerSet.initFullMarkerIdSetMap();

			// retrieve chromosome info
			rdMarkerSet.fillMarkerSetMapWithChrAndPos();
			MarkerSet_opt.replaceWithValuesFrom(wrMarkerSetMap, rdMarkerSet.getMarkerIdSetMap());
			Map<String, Object> rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetMap, 0, 1);

			NetcdfFileWriteable wrOPNcFile = null;
			try {
				// CREATE netCDF-3 FILE
				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
				OperationFactory wrOPHandler = new OperationFactory(rdCensusOPMetadata.getStudyId(),
						"Allelic Association Test", // friendly name
						"Allelic test on " + markerCensusOP.getFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + dfSci.format(hwThreshold), //description
						wrMarkerSetMap.size(),
						rdCensusOPMetadata.getImplicitSetSize(),
						rdChrInfoSetMap.size(),
						cNetCDF.Defaults.OPType.ALLELICTEST.toString(),
						rdCensusOPMetadata.getParentMatrixId(), // Parent matrixId
						markerCensusOP.getId()); // Parent operationId
				wrOPNcFile = wrOPHandler.getNetCDFHandler();

				try {
					wrOPNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrOPNcFile.getLocation(), ex);
				}
				//log.info("Done creating netCDF handle: {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

				//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
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
				rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
				for (Map.Entry<String, Object> entry : wrMarkerSetMap.entrySet()) {
					Object value = rdCaseMarkerIdSetMap.get(entry.getKey());
					entry.setValue(value);
				}
				Utils.saveCharMapValueToWrMatrix(wrOPNcFile, wrMarkerSetMap, cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

				// WRITE SAMPLESET TO MATRIX FROM SAMPLES ARRAYLIST
				ArrayChar.D2 samplesD2 = org.gwaspi.netCDF.operations.Utils.writeMapKeysToD2ArrayChar(rdSampleSetMap, cNetCDF.Strides.STRIDE_SAMPLE_NAME);

				int[] sampleOrig = new int[]{0, 0};
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
				int[] columns = new int[]{0, 1, 2, 3};
				org.gwaspi.netCDF.operations.Utils.saveIntMapD2ToWrMatrix(wrOPNcFile, rdChrInfoSetMap, columns, cNetCDF.Variables.VAR_CHR_INFO);
				//</editor-fold>

				//<editor-fold defaultstate="collapsed" desc="GET CENSUS & PERFORM ALLELICTEST TESTS">
				// CLEAN Maps FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
				Map<String, Object> wrCaseMarkerIdSetMap = new LinkedHashMap<String, Object>();
				rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				if (rdCaseMarkerIdSetMap != null) {
					for (Map.Entry<String, Object> entry : rdCaseMarkerIdSetMap.entrySet()) {
						String key = entry.getKey();

						if (!excludeMarkerSetMap.containsKey(key)) {
							wrCaseMarkerIdSetMap.put(key, entry.getValue());
						}
					}
					rdCaseMarkerIdSetMap.clear();
				}

				Map<String, Object> wrCtrlMarkerSet = new LinkedHashMap<String, Object>();
				rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				if (rdCtrlMarkerIdSetMap != null) {
					for (Map.Entry<String, Object> entry : rdCtrlMarkerIdSetMap.entrySet()) {
						String key = entry.getKey();

						if (!excludeMarkerSetMap.containsKey(key)) {
							wrCtrlMarkerSet.put(key, entry.getValue());
						}
					}
					rdCtrlMarkerIdSetMap.clear();
				}

				log.info(Text.All.processing);
				performAssociationTests(wrOPNcFile, wrCaseMarkerIdSetMap, wrCtrlMarkerSet);

				org.gwaspi.global.Utils.sysoutCompleted("Allelic Association Tests");
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
						log.warn("Cannot close file", ex);
					}
				}
			}
		} else { // NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.info(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultAssocId;
	}

	private void performAssociationTests(NetcdfFileWriteable wrNcFile, Map<String, Object> wrCaseMarkerIdSetMap, Map<String, Object> wrCtrlMarkerSet) {
		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<String, Object> entry : wrCaseMarkerIdSetMap.entrySet()) {
			String markerId = entry.getKey();

			int[] caseCntgTable = (int[]) entry.getValue();
			int[] ctrlCntgTable = (int[]) wrCtrlMarkerSet.get(markerId);

			// INIT VALUES
			int caseAA = caseCntgTable[0];
			int caseAa = caseCntgTable[1];
			int caseaa = caseCntgTable[2];
			int caseTot = caseAA + caseaa + caseAa;

			int ctrlAA = ctrlCntgTable[0];
			int ctrlAa = ctrlCntgTable[1];
			int ctrlaa = ctrlCntgTable[2];
			int ctrlTot = ctrlAA + ctrlaa + ctrlAa;

			int AAtot = caseAA + ctrlAA;
			int Aatot = caseAa + ctrlAa;
			int aatot = caseaa + ctrlaa;

			int sampleNb = caseTot + ctrlTot;

			double allelicT = org.gwaspi.statistics.Associations.calculateAllelicAssociationChiSquare(sampleNb,
					caseAA,
					caseAa,
					caseaa,
					caseTot,
					ctrlAA,
					ctrlAa,
					ctrlaa,
					ctrlTot);
			double allelicPval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(allelicT, 1);

			double allelicOR = org.gwaspi.statistics.Associations.calculateAllelicAssociationOR(caseAA,
					caseAa,
					caseaa,
					ctrlAA,
					ctrlAa,
					ctrlaa);

			Double[] store = new Double[3];
			store[0] = allelicT;
			store[1] = allelicPval;
			store[2] = allelicOR;
			wrCaseMarkerIdSetMap.put(markerId, store); // Re-use Map to store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}

		//<editor-fold defaultstate="collapsed" desc="ALLELICTEST DATA WRITER">
		int[] boxes = new int[]{0, 1, 2};
		Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, wrCaseMarkerIdSetMap, boxes, cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR);
		//</editor-fold>
	}
}
