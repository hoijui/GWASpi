package org.gwaspi.netCDF.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.samples.SampleSet;
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
public class OP_QAMarkers_opt implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_QAMarkers_opt.class);

	private int rdMatrixId;

	public OP_QAMarkers_opt(int rdMatrixId) {
		this.rdMatrixId = rdMatrixId;
	}

	public int processMatrix() throws IOException, InvalidRangeException {
		int resultOpId = Integer.MIN_VALUE;

		Map<String, Object> wrMarkerSetMismatchStateMap = new LinkedHashMap();
		Map<String, Object> wrMarkerSetCensusMap = new LinkedHashMap();
		Map<String, Object> wrMarkerSetMissingRatioMap = new LinkedHashMap();
		Map<String, Object> wrMarkerSetKnownAllelesMap = new LinkedHashMap();

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixId);

		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		MarkerSet_opt rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdMarkerSet.initFullMarkerIdSetMap();
		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; //This to test heap usage of copying locally the Map from markerset

		SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		Map<String, Object> rdSampleSetMap = rdSampleSet.getSampleIdSetMap();

		NetcdfFileWriteable wrNcFile = null;
		try {
			// CREATE netCDF-3 FILE
			String description = "Marker Quality Assurance on "
					+ rdMatrixMetadata.getMatrixFriendlyName()
					+ "\nMarkers: " + rdMarkerSet.getMarkerIdSetMap().size()
					+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();
			OperationFactory wrOPHandler = new OperationFactory(rdMatrixMetadata.getStudyId(),
					"Marker QA", // friendly name
					description, // description
					rdMarkerSet.getMarkerIdSetMap().size(),
					rdSampleSetMap.size(),
					0,
					cNetCDF.Defaults.OPType.MARKER_QA.toString(),
					rdMatrixMetadata.getMatrixId(), // Parent matrixId
					-1); // Parent operationId

			wrNcFile = wrOPHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException ex) {
				log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
			}
			//log.trace("Done creating netCDF handle: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());


			//<editor-fold defaultstate="collapsed" desc="METADATA WRITER">
			// MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeMapKeysToD2ArrayChar(rdMarkerSet.getMarkerIdSetMap(), cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[]{0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
			} catch (IOException ex) {
				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			// MARKERSET RSID
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMap(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

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
			log.info("Done writing SampleSet to matrix");
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="PROCESSOR">
			// INIT MARKER AND SAMPLE INFO
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

			List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyId());
			Map<String, Object> samplesInfoMap = new LinkedHashMap();
			for (SampleInfo sampleInfo : sampleInfos) {
				String tempSampleId = sampleInfo.getSampleId();
				if (rdSampleSetMap.containsKey(tempSampleId)) {
					String sex = sampleInfo.getSexStr();
					samplesInfoMap.put(tempSampleId, sex);
				}
			}
			sampleInfos.clear();

			// Iterate through markerset, take it marker by marker
			int markerNb = 0;
			for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetMap().entrySet()) {
				String markerId = entry.getKey();

				Map<Object, Object> knownAlleles = new LinkedHashMap();
				Map<Object, Object> allSamplesGTsTable = new LinkedHashMap();
				Map<Object, Object> allSamplesContingencyTable = new LinkedHashMap();
				Integer missingCount = 0;

				// Get a sampleset-full of GTs
				rdSampleSetMap = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);
				for (Map.Entry<String, Object> sampleEntry : rdSampleSetMap.entrySet()) {
					String sampleId = sampleEntry.getKey();

					//<editor-fold defaultstate="expanded" desc="THE DECIDER">
					CensusDecision decision = CensusDecision.getDecisionByChrAndSex(sampleEntry.getValue().toString(), samplesInfoMap.get(sampleId).toString());
					//</editor-fold>

					//<editor-fold defaultstate="collapsed" desc="SUMMING SAMPLESET GENOTYPES">
					float counter = 1;
					byte[] tempGT = (byte[]) sampleEntry.getValue();
					// Gather alleles different from 0 into a list of known alleles and count the number of appearences
					// 48 is byte for 0
					// 65 is byte for A
					// 67 is byte for C
					// 71 is byte for G
					// 84 is byte for T

					if (tempGT[0] != AlleleBytes._0) {
						float tempCount = 0;
						if (knownAlleles.containsKey(tempGT[0])) {
							tempCount = (Float) knownAlleles.get(tempGT[0]);
						}
						knownAlleles.put(tempGT[0], tempCount + counter);
					}
					if (tempGT[1] != AlleleBytes._0) { //48 is byte for 0
						float tempCount = 0;
						if (knownAlleles.containsKey(tempGT[1])) {
							tempCount = (Float) knownAlleles.get(tempGT[1]);
						}
						knownAlleles.put(tempGT[1], tempCount + counter);
					}
					if (tempGT[0] == AlleleBytes._0 && tempGT[1] == AlleleBytes._0) {
						if (decision != CensusDecision.CountFemalesNonAutosomally) {
							missingCount++;
						}
					}


					Integer intAlleleSum = tempGT[0] + tempGT[1]; //2 alleles per GT

					float tempCount = 0;
					if (allSamplesGTsTable.containsKey(intAlleleSum)) {
						tempCount = (Float) allSamplesGTsTable.get(intAlleleSum);
					}
					allSamplesGTsTable.put(intAlleleSum, tempCount + counter);
					//</editor-fold>
				}

				// ALLELE CENSUS + MISMATCH STATE + MISSINGNESS

				Object[] orderedAlleles = new Object[4];
				if (knownAlleles.size() <= 2) { // Check if there are mismatches in alleles

					//<editor-fold defaultstate="collapsed" desc="KNOW YOUR ALLELES">
					List<Integer> intAA = new ArrayList<Integer>();
					List<Integer> intAa = new ArrayList<Integer>();
					List<Integer> intaa = new ArrayList<Integer>();

					Iterator<Object> itKnAll = knownAlleles.keySet().iterator();
					if (knownAlleles.isEmpty()) {
						// Completely missing (00)
						orderedAlleles[0] = '0';
						orderedAlleles[1] = 0d;
						orderedAlleles[2] = '0';
						orderedAlleles[3] = 0d;
					}
					if (knownAlleles.size() == 1) {
						// Homozygote (AA or aa)
						byte byteAllele1 = (Byte) itKnAll.next();
						byte byteAllele2 = '0';
						int intAllele1 = byteAllele1;
						intAA.add(intAllele1);
						intAA.add(intAllele1 * 2);

						orderedAlleles[0] = new String(new byte[]{byteAllele1});
						orderedAlleles[1] = 1d;
						orderedAlleles[2] = new String(new byte[]{byteAllele2});
						orderedAlleles[3] = 0d;
					}
					if (knownAlleles.size() == 2) {
						// Heterezygote (contains mix of AA, Aa or aa)
						byte byteAllele1 = (Byte) itKnAll.next();
						int countAllele1 = Math.round((Float) knownAlleles.get(byteAllele1));
						int intAllele1 = byteAllele1;
						byte byteAllele2 = (Byte) itKnAll.next();
						int countAllele2 = Math.round((Float) knownAlleles.get(byteAllele2));
						int intAllele2 = byteAllele2;
						int totAlleles = countAllele1 + countAllele2;

						if (countAllele1 >= countAllele2) {
							// Finding out what allele is major and minor
							intAA.add(intAllele1);
							intAA.add(intAllele1 * 2);

							intaa.add(intAllele2);
							intaa.add(intAllele2 * 2);

							intAa.add(intAllele1 + intAllele2);

							orderedAlleles[0] = new String(new byte[]{byteAllele1});
							orderedAlleles[1] = (double) countAllele1 / totAlleles;
							orderedAlleles[2] = new String(new byte[]{byteAllele2});
							orderedAlleles[3] = (double) countAllele2 / totAlleles;
						} else {
							intAA.add(intAllele2);
							intAA.add(intAllele2 * 2);

							intaa.add(intAllele1);
							intaa.add(intAllele1 * 2);

							intAa.add(intAllele1 + intAllele2);

							orderedAlleles[0] = new String(new byte[]{byteAllele2});
							orderedAlleles[1] = (double) countAllele2 / totAlleles;
							orderedAlleles[2] = new String(new byte[]{byteAllele1});
							orderedAlleles[3] = (double) countAllele1 / totAlleles;
						}
					}
					//</editor-fold>

					//<editor-fold defaultstate="collapsed" desc="CONTINGENCY ALL SAMPLES">
					for (Map.Entry<Object, Object> sampleEntry : allSamplesGTsTable.entrySet()) {
						Object key = sampleEntry.getKey();
						Integer value = Math.round((Float) sampleEntry.getValue());

						if (intAA.contains(value)) {
							// compare to all possible character values of AA
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("AA")) {
								tempCount = (Integer) allSamplesContingencyTable.get("AA");
							}
							allSamplesContingencyTable.put("AA", tempCount + value);
						}
						if (intAa.contains(value)) {
							// compare to all possible character values of Aa
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("Aa")) {
								tempCount = (Integer) allSamplesContingencyTable.get("Aa");
							}
							allSamplesContingencyTable.put("Aa", tempCount + value);
						}
						if (intaa.contains(value)) {
							// compare to all possible character values of aa
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("aa")) {
								tempCount = (Integer) allSamplesContingencyTable.get("aa");
							}
							allSamplesContingencyTable.put("aa", tempCount + value);
						}
					}
					//</editor-fold>

					// CENSUS
					int obsAllAA = 0;
					int obsAllAa = 0;
					int obsAllaa = 0;
					int obsCaseAA = 0;
					int obsCaseAa = 0;
					int obsCaseaa = 0;
					int obsCntrlAA = 0;
					int obsCntrlAa = 0;
					int obsCntrlaa = 0;
					if (allSamplesContingencyTable.containsKey("AA")) {
						obsAllAA = (Integer) allSamplesContingencyTable.get("AA");
					}
					if (allSamplesContingencyTable.containsKey("Aa")) {
						obsAllAa = (Integer) allSamplesContingencyTable.get("Aa");
					}
					if (allSamplesContingencyTable.containsKey("aa")) {
						obsAllaa = (Integer) allSamplesContingencyTable.get("aa");
					}

					int[] census = new int[4];

					census[0] = obsAllAA; // all
					census[1] = obsAllAa; // all
					census[2] = obsAllaa; // all
					census[3] = missingCount; // all

					wrMarkerSetCensusMap.put(markerId, census);
					wrMarkerSetMismatchStateMap.put(markerId, cNetCDF.Defaults.DEFAULT_MISMATCH_NO);

					if (orderedAlleles[0] == null && orderedAlleles[2] != null) {
						orderedAlleles[0] = orderedAlleles[2];
					}
					if (orderedAlleles[2] == null && orderedAlleles[0] != null) {
						orderedAlleles[2] = orderedAlleles[0];
					}
					if (orderedAlleles[0] == null && orderedAlleles[2] == null) {
						orderedAlleles[0] = '0';
						orderedAlleles[2] = '0';
					}
					wrMarkerSetKnownAllelesMap.put(markerId, orderedAlleles);
				} else {
					int[] census = new int[4];
					wrMarkerSetCensusMap.put(markerId, census);
					wrMarkerSetMismatchStateMap.put(markerId, cNetCDF.Defaults.DEFAULT_MISMATCH_YES);

					orderedAlleles[0] = '0';
					orderedAlleles[1] = 0d;
					orderedAlleles[2] = '0';
					orderedAlleles[3] = 0d;
					wrMarkerSetKnownAllelesMap.put(markerId, orderedAlleles);
				}

				double missingRatio = (double) missingCount / rdSampleSet.getSampleSetSize();
				wrMarkerSetMissingRatioMap.put(markerId, missingRatio);

				markerNb++;
				if (markerNb == 1) {
					log.info(Text.All.processing);
				} else if (markerNb % 100000 == 0) {
					log.info("Processed markers: {}", markerNb);
				}
			}
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="QA DATA WRITER">
			// MISSING RATIO
			Utils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrMarkerSetMissingRatioMap, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

			// MISMATCH STATE
			Utils.saveIntMapD1ToWrMatrix(wrNcFile, wrMarkerSetMismatchStateMap, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

			// KNOWN ALLELES
			//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
			Utils.saveCharMapItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, 0, cNetCDF.Strides.STRIDE_GT / 2);
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, 1, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
			Utils.saveCharMapItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, 2, cNetCDF.Strides.STRIDE_GT / 2);
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, 3, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

			// ALL CENSUS
			int[] columns = new int[]{0, 1, 2, 3};
			Utils.saveIntMapD2ToWrMatrix(wrNcFile, wrMarkerSetCensusMap, columns, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
			//</editor-fold>

			resultOpId = wrOPHandler.getResultOPId();
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
					wrNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}

			org.gwaspi.global.Utils.sysoutCompleted("Marker QA");
		}

		return resultOpId;
	}
}
