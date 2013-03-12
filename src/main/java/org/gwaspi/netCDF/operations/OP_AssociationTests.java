package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.Operation;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleKey;
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
public class OP_AssociationTests implements MatrixOperation {

	private final Logger log
			= LoggerFactory.getLogger(OP_AssociationTests.class);

	private int rdMatrixId;
	private Operation markerCensusOP;
	private Operation hwOP;
	private double hwThreshold;
	/**
	 * Whether we are to perform allelic or genotypic association tests.
	 */
	private final boolean allelic;

	public OP_AssociationTests(
			int rdMatrixId,
			Operation markerCensusOP,
			Operation hwOP,
			double hwThreshold,
			boolean allelic)
	{
		this.rdMatrixId = rdMatrixId;
		this.markerCensusOP = markerCensusOP;
		this.hwOP = hwOP;
		this.hwThreshold = hwThreshold;
		this.allelic = allelic;
	}

	@Override
	public int processMatrix() throws IOException, InvalidRangeException {
		int resultAssocId = Integer.MIN_VALUE;

		String testName = allelic ? "Allelic" : "Genotypic";

		//<editor-fold defaultstate="expanded" desc="EXCLUSION MARKERS FROM HW">
		Map<MarkerKey, Object> excludeMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
		int totalMarkerNb = 0;

		if (hwOP != null) {
			OperationMetadata hwMetadata = OperationsList.getOperationMetadata(hwOP.getId());
			NetcdfFile rdHWNcFile = NetcdfFile.open(hwMetadata.getPathToMatrix());
			MarkerOperationSet rdHWOperationSet = new MarkerOperationSet(hwMetadata.getStudyId(), hwMetadata.getOPId());
			Map<MarkerKey, Object> rdHWMarkerSetMap = rdHWOperationSet.getOpSetMap();
			totalMarkerNb = rdHWMarkerSetMap.size();

			// EXCLUDE MARKER BY HARDY WEINBERG THRESHOLD
			rdHWMarkerSetMap = rdHWOperationSet.fillOpSetMapWithVariable(rdHWNcFile, cNetCDF.HardyWeinberg.VAR_OP_MARKERS_HWPval_CTRL);
			for (Map.Entry<MarkerKey, Object> entry : rdHWMarkerSetMap.entrySet()) {
				double value = (Double) entry.getValue();
				if (value < hwThreshold) {
					excludeMarkerSetMap.put(entry.getKey(), value);
				}
			}

			if (rdHWMarkerSetMap != null) { // FIXME this check does not make sense here
				rdHWMarkerSetMap.clear();
			}
			rdHWNcFile.close();
		}
		//</editor-fold>

		if (excludeMarkerSetMap.size() < totalMarkerNb) { // CHECK IF THERE IS ANY DATA LEFT TO PROCESS AFTER PICKING
			OperationMetadata rdCensusOPMetadata = OperationsList.getOperationMetadata(markerCensusOP.getId());
			NetcdfFile rdOPNcFile = NetcdfFile.open(rdCensusOPMetadata.getPathToMatrix());

			MarkerOperationSet rdCaseMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			MarkerOperationSet rdCtrlMarkerSet = new MarkerOperationSet(rdCensusOPMetadata.getStudyId(), markerCensusOP.getId());
			Map<SampleKey, Object> rdSampleSetMap = rdCaseMarkerSet.getImplicitSetMap();
			Map<MarkerKey, Object> rdCaseMarkerIdSetMap = rdCaseMarkerSet.getOpSetMap();
			Map<MarkerKey, Object> rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.getOpSetMap();

			Map<MarkerKey, Object> wrMarkerSetMap = new LinkedHashMap<MarkerKey, Object>();
			for (MarkerKey key : rdCtrlMarkerIdSetMap.keySet()) {
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
			Map<MarkerKey, Object> rdChrInfoSetMap = org.gwaspi.netCDF.matrices.Utils.aggregateChromosomeInfo(wrMarkerSetMap, 0, 1);

			NetcdfFileWriteable wrOPNcFile = null;
			try {
				// CREATE netCDF-3 FILE
				DecimalFormat dfSci = new DecimalFormat("0.##E0#");
				OPType testType = allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST;
				OperationFactory wrOPHandler = new OperationFactory(rdCensusOPMetadata.getStudyId(),
						testName + " Association Test", // friendly name
						testName + " test on " + markerCensusOP.getFriendlyName() + "\n" + rdCensusOPMetadata.getDescription() + "\nHardy-Weinberg threshold: " + dfSci.format(hwThreshold), // description
						wrMarkerSetMap.size(),
						rdCensusOPMetadata.getImplicitSetSize(),
						rdChrInfoSetMap.size(),
						testType.toString(),
						rdCensusOPMetadata.getParentMatrixId(), // Parent matrixId
						markerCensusOP.getId()); // Parent operationId
				wrOPNcFile = wrOPHandler.getNetCDFHandler();

				try {
					wrOPNcFile.create();
				} catch (IOException ex) {
					log.error("Failed creating file " + wrOPNcFile.getLocation(), ex);
				}
				//log.info("Done creating netCDF handle: {}", org.gwaspi.global.Utils.getMediumDateTimeAsString());

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
				rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Variables.VAR_MARKERS_RSID);
				for (Map.Entry<MarkerKey, Object> entry : wrMarkerSetMap.entrySet()) {
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

				//<editor-fold defaultstate="expanded" desc="GET CENSUS & PERFORM TESTS">
				// CLEAN Maps FROM MARKERS THAT FAILED THE HARDY WEINBERG THRESHOLD
				Map<MarkerKey, Object> wrCaseMarkerIdSetMap = new LinkedHashMap<MarkerKey, Object>();
				rdCaseMarkerIdSetMap = rdCaseMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCASE);
				if (rdCaseMarkerIdSetMap != null) {
					for (Map.Entry<MarkerKey, Object> entry : rdCaseMarkerIdSetMap.entrySet()) {
						MarkerKey key = entry.getKey();

						if (!excludeMarkerSetMap.containsKey(key)) {
							wrCaseMarkerIdSetMap.put(key, entry.getValue());
						}
					}
					rdCaseMarkerIdSetMap.clear();
				}

				Map<MarkerKey, Object> wrCtrlMarkerSet = new LinkedHashMap<MarkerKey, Object>();
				rdCtrlMarkerIdSetMap = rdCtrlMarkerSet.fillOpSetMapWithVariable(rdOPNcFile, cNetCDF.Census.VAR_OP_MARKERS_CENSUSCTRL);
				if (rdCtrlMarkerIdSetMap != null) {
					for (Map.Entry<MarkerKey, Object> entry : rdCtrlMarkerIdSetMap.entrySet()) {
						MarkerKey key = entry.getKey();

						if (!excludeMarkerSetMap.containsKey(key)) {
							wrCtrlMarkerSet.put(key, entry.getValue());
						}
					}
					rdCtrlMarkerIdSetMap.clear();
				}

				log.info(Text.All.processing);
				performAssociationTests(wrOPNcFile, wrCaseMarkerIdSetMap, wrCtrlMarkerSet);

				org.gwaspi.global.Utils.sysoutCompleted(testName + " Association Tests");
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
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultAssocId;
	}

	private void performAssociationTests(NetcdfFileWriteable wrNcFile, Map<MarkerKey, Object> wrCaseMarkerIdSetMap, Map<MarkerKey, Object> wrCtrlMarkerSet) {
		// Iterate through markerset
		int markerNb = 0;
		for (Map.Entry<MarkerKey, Object> entry : wrCaseMarkerIdSetMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();

			int[] caseCntgTable = (int[]) entry.getValue();
			int[] ctrlCntgTable = (int[]) wrCtrlMarkerSet.get(markerKey);

			// INIT VALUES
			int caseAA = caseCntgTable[0];
			int caseAa = caseCntgTable[1];
			int caseaa = caseCntgTable[2];
			int caseTot = caseAA + caseaa + caseAa;

			int ctrlAA = ctrlCntgTable[0];
			int ctrlAa = ctrlCntgTable[1];
			int ctrlaa = ctrlCntgTable[2];
			int ctrlTot = ctrlAA + ctrlaa + ctrlAa;

			Double[] store;
			if (allelic) {
				// allelic test
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

				store = new Double[3];
				store[0] = allelicT;
				store[1] = allelicPval;
				store[2] = allelicOR;
			} else {
				// genotypic test
				double gntypT = org.gwaspi.statistics.Associations.calculateGenotypicAssociationChiSquare(caseAA,
						caseAa,
						caseaa,
						caseTot,
						ctrlAA,
						ctrlAa,
						ctrlaa,
						ctrlTot);
				double gntypPval = org.gwaspi.statistics.Pvalue.calculatePvalueFromChiSqr(gntypT, 2);
				double[] gntypOR = org.gwaspi.statistics.Associations.calculateGenotypicAssociationOR(caseAA,
						caseAa,
						caseaa,
						ctrlAA,
						ctrlAa,
						ctrlaa);

				store = new Double[4];
				store[0] = gntypT;
				store[1] = gntypPval;
				store[2] = gntypOR[0];
				store[3] = gntypOR[1];
			}
			wrCaseMarkerIdSetMap.put(markerKey, store); // Re-use Map to store P-value and stuff

			markerNb++;
			if (markerNb % 100000 == 0) {
				log.info("Processed {} markers", markerNb);
			}
		}

		//<editor-fold defaultstate="expanded" desc="ALLELICTEST DATA WRITER">
		int[] boxes;
		String variableName;
		if (allelic) {
			boxes = new int[] {0, 1, 2};
			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR;
		} else {
			boxes = new int[] {0, 1, 2, 3};
			variableName = cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR;
		}
		Utils.saveDoubleMapD2ToWrMatrix(wrNcFile, wrCaseMarkerIdSetMap, boxes, variableName);
		//</editor-fold>
	}
}
