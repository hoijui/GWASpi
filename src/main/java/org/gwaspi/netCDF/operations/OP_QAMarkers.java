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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleBytes;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.global.TypeConverter;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;

public class OP_QAMarkers implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_QAMarkers.class);

	private int rdMatrixId;

	private static final class OrderedAlleles {

		public static final TypeConverter<OrderedAlleles, String> TO_ALLELE_1
				= new TypeConverter<OrderedAlleles, String>()
		{
			@Override
			public String convert(OrderedAlleles from) {
				return String.valueOf(from.getAllele1());
			}
		};

		public static final TypeConverter<OrderedAlleles, Double> TO_ALLELE_1_FREQ
				= new TypeConverter<OrderedAlleles, Double>()
		{
			@Override
			public Double convert(OrderedAlleles from) {
				return from.getAllele1Freq();
			}
		};

		public static final TypeConverter<OrderedAlleles, String> TO_ALLELE_2
				= new TypeConverter<OrderedAlleles, String>()
		{
			@Override
			public String convert(OrderedAlleles from) {
				return String.valueOf(from.getAllele2());
			}
		};

		public static final TypeConverter<OrderedAlleles, Double> TO_ALLELE_2_FREQ
				= new TypeConverter<OrderedAlleles, Double>()
		{
			@Override
			public Double convert(OrderedAlleles from) {
				return from.getAllele2Freq();
			}
		};

		private char allele1;
		private final double allele1Freq;
		private char allele2;

		OrderedAlleles(
				char allele1,
				double allele1Freq,
				char allele2
				)
		{
			this.allele1 = allele1;
			this.allele1Freq = allele1Freq;
			this.allele2 = allele2;
		}

		OrderedAlleles() {
			this('0', 0.0, '0');
		}

		public char getAllele1() {
			return allele1;
		}

		public void setAllele1(char allele1) {
			this.allele1 = allele1;
		}

		public double getAllele1Freq() {
			return allele1Freq;
		}

		public char getAllele2() {
			return allele2;
		}

		public void setAllele2(char allele2) {
			this.allele2 = allele2;
		}

		public double getAllele2Freq() {
			return 1.0 - allele1Freq;
		}
	}

	public OP_QAMarkers(int rdMatrixId) {
		this.rdMatrixId = rdMatrixId;
	}

	public int processMatrix() throws IOException, InvalidRangeException {
		int resultOpId = Integer.MIN_VALUE;

		Map<MarkerKey, Integer> wrMarkerSetMismatchStateMap = new LinkedHashMap<MarkerKey, Integer>();
		Map<MarkerKey, int[]> wrMarkerSetCensusMap = new LinkedHashMap<MarkerKey, int[]>();
		Map<MarkerKey, Double> wrMarkerSetMissingRatioMap = new LinkedHashMap<MarkerKey, Double>();
		Map<MarkerKey, OrderedAlleles> wrMarkerSetKnownAllelesMap = new LinkedHashMap<MarkerKey, OrderedAlleles>();

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixId);

		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		MarkerSet rdMarkerSet = new MarkerSet(rdMatrixMetadata.getStudyKey(), rdMatrixId);
		rdMarkerSet.initFullMarkerIdSetMap();
		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; // This to test heap usage of copying locally the Map from markerset

		SampleSet rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyKey(), rdMatrixId);
		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		NetcdfFileWriteable wrNcFile = null;
		try {
			// CREATE netCDF-3 FILE
			String description = "Marker Quality Assurance on "
					+ rdMatrixMetadata.getMatrixFriendlyName()
					+ "\nMarkers: " + rdMarkerSet.getMarkerKeys().size()
					+ "\nStarted at: " + org.gwaspi.global.Utils.getShortDateTimeAsString();
			OperationFactory wrOPHandler = new OperationFactory(
					rdMatrixMetadata.getStudyKey(),
					"Marker QA", // friendly name
					description, // description
					rdMarkerSet.getMarkerKeys().size(),
					rdSampleSetMap.size(),
					0,
					OPType.MARKER_QA,
					rdMatrixMetadata.getMatrixId(), // Parent matrixId
					-1); // Parent operationId

			wrNcFile = wrOPHandler.getNetCDFHandler();
			try {
				wrNcFile.create();
			} catch (IOException ex) {
				log.error("Failed creating file: " + wrNcFile.getLocation(), ex);
			}
			//log.trace("Done creating netCDF handle: " + org.gwaspi.global.Utils.getMediumDateTimeAsString());

			//<editor-fold defaultstate="expanded" desc="METADATA WRITER">
			// MARKERSET MARKERID
			ArrayChar.D2 markersD2 = Utils.writeCollectionToD2ArrayChar(rdMarkerSet.getMarkerKeys(), cNetCDF.Strides.STRIDE_MARKER_NAME);
			int[] markersOrig = new int[] {0, 0};
			try {
				wrNcFile.write(cNetCDF.Variables.VAR_OPSET, markersOrig, markersD2);
			} catch (IOException ex) {
				log.error("Failed writing file: " + wrNcFile.getLocation(), ex);
			} catch (InvalidRangeException ex) {
				log.error(null, ex);
			}

			// MARKERSET RSID
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);
			Utils.saveCharMapValueToWrMatrix(wrNcFile, rdMarkerSet.getMarkerIdSetMapCharArray(), cNetCDF.Variables.VAR_MARKERS_RSID, cNetCDF.Strides.STRIDE_MARKER_NAME);

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

			//<editor-fold defaultstate="expanded" desc="PROCESSOR">
			// INIT MARKER AND SAMPLE INFO
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

			List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyKey());
			Map<SampleKey, Sex> samplesInfoMap = new LinkedHashMap<SampleKey, Sex>();
			for (SampleInfo sampleInfo : sampleInfos) {
				SampleKey tempSampleId = sampleInfo.getKey();
				if (rdSampleSetMap.containsKey(tempSampleId)) {
					Sex sex = sampleInfo.getSex();
					samplesInfoMap.put(tempSampleId, sex);
				}
			}
			sampleInfos.clear();

			// Iterate through markerset, take it marker by marker
			int markerNb = 0;
			for (MarkerKey markerKey : rdMarkerSet.getMarkerKeys()) {
				Map<Byte, Float> knownAlleles = new LinkedHashMap<Byte, Float>();
				Map<Short, Float> allSamplesGTsTable = new LinkedHashMap<Short, Float>();
				Map<String, Integer> allSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Integer missingCount = 0;

				// Get a sampleset-full of GTs
				rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);
				for (Map.Entry<SampleKey, byte[]> sampleEntry : rdSampleSetMap.entrySet()) {
					SampleKey sampleKey = sampleEntry.getKey();

					//<editor-fold defaultstate="expanded" desc="THE DECIDER">
					CensusDecision decision = CensusDecision.getDecisionByChrAndSex(new String(sampleEntry.getValue()), samplesInfoMap.get(sampleKey));
					//</editor-fold>

					//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
					float counter = 1;
					byte[] tempGT = sampleEntry.getValue();
					// Gather alleles different from 0 into a list of known alleles and count the number of appearences
					// 48 is byte for 0
					// 65 is byte for A
					// 67 is byte for C
					// 71 is byte for G
					// 84 is byte for T

					if (tempGT[0] != AlleleBytes._0) {
						float tempCount = 0;
						if (knownAlleles.containsKey(tempGT[0])) {
							tempCount = knownAlleles.get(tempGT[0]);
						}
						knownAlleles.put(tempGT[0], tempCount + counter);
					}
					if (tempGT[1] != AlleleBytes._0) {
						float tempCount = 0;
						if (knownAlleles.containsKey(tempGT[1])) {
							tempCount = knownAlleles.get(tempGT[1]);
						}
						knownAlleles.put(tempGT[1], tempCount + counter);
					}
					if ((tempGT[0] == AlleleBytes._0)
							&& (tempGT[1] == AlleleBytes._0)
							&& (decision != CensusDecision.CountFemalesNonAutosomally))
					{
						missingCount++;
					}

					Short intAlleleSum = (short)((short)tempGT[0] + (short)tempGT[1]); // 2 alleles per GT

					float tempCount = 0;
					if (allSamplesGTsTable.containsKey(intAlleleSum)) {
						tempCount = allSamplesGTsTable.get(intAlleleSum);
					}
					allSamplesGTsTable.put(intAlleleSum, tempCount + counter);
					//</editor-fold>
				}

				// ALLELE CENSUS + MISMATCH STATE + MISSINGNESS

				OrderedAlleles orderedAlleles = null;
				if (knownAlleles.size() <= 2) { // Check if there are mismatches in alleles

					//<editor-fold defaultstate="expanded" desc="KNOW YOUR ALLELES">
					List<Integer> intAA = new ArrayList<Integer>();
					List<Integer> intAa = new ArrayList<Integer>();
					List<Integer> intaa = new ArrayList<Integer>();

					Iterator<Byte> itKnAll = knownAlleles.keySet().iterator();
					if (knownAlleles.isEmpty()) {
						// Completely missing (00)
						orderedAlleles = new OrderedAlleles();
					} else if (knownAlleles.size() == 1) {
						// Homozygote (AA or aa)
						final byte byteAllele1 = itKnAll.next();
						final byte byteAllele2 = '0';
						final int intAllele1 = byteAllele1;
						intAA.add(intAllele1);
						intAA.add(intAllele1 * 2);

						orderedAlleles = new OrderedAlleles(
								new String(new byte[] {byteAllele1}).charAt(0),
								1.0,
								new String(new byte[] {byteAllele2}).charAt(0)
								);
					} else if (knownAlleles.size() == 2) {
						// Heterezygote (contains mix of AA, Aa or aa)
						final byte byteAllele1 = itKnAll.next();
						final int countAllele1 = Math.round(knownAlleles.get(byteAllele1));
						final int intAllele1 = byteAllele1;
						final byte byteAllele2 = itKnAll.next();
						final int countAllele2 = Math.round(knownAlleles.get(byteAllele2));
						final int intAllele2 = byteAllele2;
						final int totAlleles = countAllele1 + countAllele2;

						if (countAllele1 >= countAllele2) {
							// Finding out what allele is major and minor
							intAA.add(intAllele1);
							intAA.add(intAllele1 * 2);

							intaa.add(intAllele2);
							intaa.add(intAllele2 * 2);

							intAa.add(intAllele1 + intAllele2);

							orderedAlleles = new OrderedAlleles(
									new String(new byte[] {byteAllele1}).charAt(0),
									(double) countAllele1 / totAlleles,
									new String(new byte[] {byteAllele2}).charAt(0)
									);
						} else {
							intAA.add(intAllele2);
							intAA.add(intAllele2 * 2);

							intaa.add(intAllele1);
							intaa.add(intAllele1 * 2);

							intAa.add(intAllele1 + intAllele2);

							orderedAlleles = new OrderedAlleles(
									new String(new byte[] {byteAllele2}).charAt(0),
									(double) countAllele2 / totAlleles,
									new String(new byte[] {byteAllele1}).charAt(0)
									);
						}
					} else {
						throw new IOException("More then 2 known alleles ("
								+ knownAlleles.size() + ")");
					}
					//</editor-fold>

					//<editor-fold defaultstate="expanded" desc="CONTINGENCY ALL SAMPLES">
					for (Map.Entry<Short, Float> sampleEntry : allSamplesGTsTable.entrySet()) {
						Integer value = Math.round(sampleEntry.getValue());

						if (intAA.contains(value)) {
							// compare to all possible character values of AA
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("AA")) {
								tempCount = allSamplesContingencyTable.get("AA");
							}
							allSamplesContingencyTable.put("AA", tempCount + value);
						}
						if (intAa.contains(value)) {
							// compare to all possible character values of Aa
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("Aa")) {
								tempCount = allSamplesContingencyTable.get("Aa");
							}
							allSamplesContingencyTable.put("Aa", tempCount + value);
						}
						if (intaa.contains(value)) {
							// compare to all possible character values of aa
							// ALL CENSUS
							int tempCount = 0;
							if (allSamplesContingencyTable.containsKey("aa")) {
								tempCount = allSamplesContingencyTable.get("aa");
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
						obsAllAA = allSamplesContingencyTable.get("AA");
					}
					if (allSamplesContingencyTable.containsKey("Aa")) {
						obsAllAa = allSamplesContingencyTable.get("Aa");
					}
					if (allSamplesContingencyTable.containsKey("aa")) {
						obsAllaa = allSamplesContingencyTable.get("aa");
					}

					int[] census = new int[4];

					census[0] = obsAllAA; // all
					census[1] = obsAllAa; // all
					census[2] = obsAllaa; // all
					census[3] = missingCount; // all

					wrMarkerSetCensusMap.put(markerKey, census);
					wrMarkerSetMismatchStateMap.put(markerKey, cNetCDF.Defaults.DEFAULT_MISMATCH_NO);

					// NOTE This was checking for <code>== null</code>
					//   (which was never the case)
					//   instead of <code>== '0'</code> before.
					//   Therefore, some '0' were left in the end
					//   (when there was only one known allele).
					if (orderedAlleles.getAllele1() == '0'
							&& orderedAlleles.getAllele2() != '0')
					{
						orderedAlleles.setAllele1(orderedAlleles.getAllele2());
					} else if (orderedAlleles.getAllele2() == '0'
							&& orderedAlleles.getAllele1() != '0')
					{
						orderedAlleles.setAllele2(orderedAlleles.getAllele1());
					}

					wrMarkerSetKnownAllelesMap.put(markerKey, orderedAlleles);
				} else {
					int[] census = new int[4];
					wrMarkerSetCensusMap.put(markerKey, census);
					wrMarkerSetMismatchStateMap.put(markerKey, cNetCDF.Defaults.DEFAULT_MISMATCH_YES);

					orderedAlleles = new OrderedAlleles();
					wrMarkerSetKnownAllelesMap.put(markerKey, orderedAlleles);
				}

				double missingRatio = (double) missingCount / rdSampleSet.getSampleSetSize();
				wrMarkerSetMissingRatioMap.put(markerKey, missingRatio);

				markerNb++;
				if (markerNb == 1) {
					log.info(Text.All.processing);
				} else if (markerNb % 100000 == 0) {
					log.info("Processed markers: {}", markerNb);
				}
			}
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="QA DATA WRITER">
			// MISSING RATIO
			Utils.saveDoubleMapD1ToWrMatrix(wrNcFile, wrMarkerSetMissingRatioMap, cNetCDF.Census.VAR_OP_MARKERS_MISSINGRAT);

			// MISMATCH STATE
			Utils.saveIntMapD1ToWrMatrix(wrNcFile, wrMarkerSetMismatchStateMap, cNetCDF.Census.VAR_OP_MARKERS_MISMATCHSTATE);

			// KNOWN ALLELES
			//Utils.saveCharMapValueToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_KNOWNALLELES, cNetCDF.Strides.STRIDE_GT);
			Utils.saveCharMapItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELES, OrderedAlleles.TO_ALLELE_1, cNetCDF.Strides.STRIDE_GT / 2);
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, OrderedAlleles.TO_ALLELE_1_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MAJALLELEFRQ);
			Utils.saveCharMapItemToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, cNetCDF.Census.VAR_OP_MARKERS_MINALLELES, OrderedAlleles.TO_ALLELE_2, cNetCDF.Strides.STRIDE_GT / 2);
			Utils.saveDoubleMapItemD1ToWrMatrix(wrNcFile, wrMarkerSetKnownAllelesMap, OrderedAlleles.TO_ALLELE_2_FREQ, cNetCDF.Census.VAR_OP_MARKERS_MINALLELEFRQ);

			// ALL CENSUS
			int[] columns = new int[] {0, 1, 2, 3};
			Utils.saveIntMapD2ToWrMatrix(wrNcFile, wrMarkerSetCensusMap, columns, cNetCDF.Census.VAR_OP_MARKERS_CENSUSALL);
			//</editor-fold>

			resultOpId = wrOPHandler.getResultOPId();
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file " + rdNcFile, ex);
				}
			}
			if (null != wrNcFile) {
				try {
					wrNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file " + wrNcFile, ex);
				}
			}

			org.gwaspi.global.Utils.sysoutCompleted("Marker QA");
		}

		return resultOpId;
	}
}
