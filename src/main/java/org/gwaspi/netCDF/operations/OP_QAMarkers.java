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
import org.gwaspi.model.Census;
import org.gwaspi.model.ChromosomeInfo;
import org.gwaspi.model.ChromosomeKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.qamarkers.NetCdfQAMarkersOperationDataSet;
import org.gwaspi.operations.qamarkers.OrderedAlleles;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_QAMarkers implements MatrixOperation {

	private final Logger log = LoggerFactory.getLogger(OP_QAMarkers.class);

	private MatrixKey rdMatrixKey;

	public OP_QAMarkers(MatrixKey rdMatrixKey) {
		this.rdMatrixKey = rdMatrixKey;
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

		Map<MarkerKey, Boolean> wrMarkerMismatches = new LinkedHashMap<MarkerKey, Boolean>();
		Map<MarkerKey, Census> wrMarkerSetCensusMap = new LinkedHashMap<MarkerKey, Census>();
		Map<MarkerKey, Double> wrMarkerSetMissingRatioMap = new LinkedHashMap<MarkerKey, Double>();
		Map<MarkerKey, OrderedAlleles> wrMarkerSetKnownAllelesMap = new LinkedHashMap<MarkerKey, OrderedAlleles>();

		MatrixMetadata rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixKey);

		MarkerSet rdMarkerSet = new MarkerSet(rdMatrixKey);
		rdMarkerSet.initFullMarkerIdSetMap();
		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; // This to test heap usage of copying locally the Map from markerset

		SampleSet rdSampleSet = new SampleSet(rdMatrixKey);
		DataSetSource rdDataSetSource = MatrixFactory.generateMatrixDataSetSource(rdMatrixKey);
		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		try {
			QAMarkersOperationDataSet dataSet = new NetCdfQAMarkersOperationDataSet(); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setReadMatrixKey(rdMatrixKey); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(rdMarkerSet.getMarkerKeys().size()); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumSamples(rdSampleSetMap.size()); // HACK

//			dataSet.setMarkers(rdMarkerSet.getMarkerKeys());
//			dataSet.setSamples(rdSampleSetMap.keySet());
//			Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = rdMarkerSet.getChrInfoSetMap();
//			dataSet.setChromosomes(chromosomeInfo.keySet(), chromosomeInfo.values());
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllSamplesFromParent(true);
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllMarkersFromParent(true);
			((AbstractNetCdfOperationDataSet) dataSet).setUseAllChromosomesFromParent(true);

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
			Iterator<GenotypesList> markersGenotypesSourceIt = rdDataSetSource.getMarkersGenotypesSource().iterator();
			for (MarkerKey markerKey : rdMarkerSet.getMarkerKeys()) {
				GenotypesList markerGenotypes = markersGenotypesSourceIt.next();
				Map<Byte, Float> knownAlleles = new LinkedHashMap<Byte, Float>();
				Map<Short, Float> allSamplesGTsTable = new LinkedHashMap<Short, Float>();
				Map<String, Integer> allSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Integer missingCount = 0;

				// Get a sampleset-full of GTs
				byte[] tempGT = new byte[2];
				Iterator<byte[]> markerGenotypesIt = markerGenotypes.iterator();
				for (SampleKey sampleKey : rdDataSetSource.getSamplesKeysSource()) {
					byte[] genotype = markerGenotypesIt.next();

					//<editor-fold defaultstate="expanded" desc="THE DECIDER">
					CensusDecision decision = CensusDecision.getDecisionByChrAndSex(new String(genotype), samplesInfoMap.get(sampleKey));
					//</editor-fold>

					//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
					float counter = 1;
//					byte[] tempGT = genotype;
//					byte[] tempGT = genotype.clone();
					tempGT[0] = genotype[0];
					tempGT[1] = genotype[1];
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
								byteAllele1,
								1.0,
								byteAllele2
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
									byteAllele1,
									(double) countAllele1 / totAlleles,
									byteAllele2
									);
						} else {
							intAA.add(intAllele2);
							intAA.add(intAllele2 * 2);

							intaa.add(intAllele1);
							intaa.add(intAllele1 * 2);

							intAa.add(intAllele1 + intAllele2);

							orderedAlleles = new OrderedAlleles(
									byteAllele2,
									(double) countAllele2 / totAlleles,
									byteAllele1
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

					Census census = new Census(
							obsAllAA, // all
							obsAllAa, // all
							obsAllaa, // all
							missingCount); // all

					wrMarkerSetCensusMap.put(markerKey, census);
					wrMarkerMismatches.put(markerKey, cNetCDF.Defaults.MISMATCH_NO);

					// NOTE This was checking for <code>== null</code>
					//   (which was never the case)
					//   instead of <code>== '0'</code> before.
					//   Therefore, some '0' were left in the end
					//   (when there was only one known allele).
					if (orderedAlleles.getAllele1() == (byte) '0'
							&& orderedAlleles.getAllele2() != (byte) '0')
					{
						orderedAlleles.setAllele1(orderedAlleles.getAllele2());
					} else if (orderedAlleles.getAllele2() == (byte) '0'
							&& orderedAlleles.getAllele1() != (byte) '0')
					{
						orderedAlleles.setAllele2(orderedAlleles.getAllele1());
					}

					wrMarkerSetKnownAllelesMap.put(markerKey, orderedAlleles);
				} else {
					wrMarkerSetCensusMap.put(markerKey, new Census());
					wrMarkerMismatches.put(markerKey, cNetCDF.Defaults.MISMATCH_YES);

					orderedAlleles = new OrderedAlleles();
					wrMarkerSetKnownAllelesMap.put(markerKey, orderedAlleles);
				}

				double missingRatio = (double) missingCount / rdSampleSet.getSampleSetSize();
				wrMarkerSetMissingRatioMap.put(markerKey, missingRatio);

				markerNb++;
				if ((markerNb == 1) || (markerNb % 10000 == 0)) {
					log.info("Processed markers: {} / {}", markerNb, rdMarkerSet.size());
				}
			}
			//</editor-fold>

			dataSet.setMarkerMissingRatios(wrMarkerSetMissingRatioMap.values());
			dataSet.setMarkerMismatchStates(wrMarkerMismatches.values());
			dataSet.setMarkerKnownAlleles(wrMarkerSetKnownAllelesMap.values());
			dataSet.setMarkerCensusAll(wrMarkerSetCensusMap.values());

			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK
		} finally {
			org.gwaspi.global.Utils.sysoutCompleted("Marker QA");
		}

		return resultOpId;
	}
}
