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
import org.gwaspi.model.Census;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.operations.qamarkers.DefaultQAMarkersOperationEntry;
import org.gwaspi.operations.qamarkers.OrderedAlleles;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_QAMarkers extends AbstractOperation<QAMarkersOperationDataSet> {

	private final Logger log = LoggerFactory.getLogger(OP_QAMarkers.class);

	public OP_QAMarkers(MatrixKey parent) {
		super(parent);
	}

	public OP_QAMarkers(OperationKey parent) {
		super(parent);
	}

	@Override
	public OPType getType() {
		return OPType.MARKER_QA;
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

		MatrixMetadata rdMatrixMetadata = getParentMatrixMetadata();

//		MarkerSet rdMarkerSet = new MarkerSet(rdMatrixKey);
//		rdMarkerSet.initFullMarkerIdSetMap();
		//Map<String, Object> rdMarkerSetMap = rdMarkerSet.markerIdSetMap; // This to test heap usage of copying locally the Map from markerset

//		SampleSet rdSampleSet = new SampleSet(rdMatrixKey);
		DataSetSource rdDataSetSource = getParentDataSetSource();
//		Map<SampleKey, byte[]> rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		try {
			QAMarkersOperationDataSet dataSet = generateFreshOperationDataSet();
			((AbstractNetCdfOperationDataSet) dataSet).setNumMarkers(rdDataSetSource.getNumMarkers()); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumChromosomes(rdDataSetSource.getNumChromosomes()); // HACK
			((AbstractNetCdfOperationDataSet) dataSet).setNumSamples(rdDataSetSource.getNumSamples()); // HACK

//			dataSet.setMarkers(rdMarkerSet.getMarkerKeys());
//			dataSet.setSamples(rdSampleSetMap.keySet());
//			Map<ChromosomeKey, ChromosomeInfo> chromosomeInfo = rdMarkerSet.getChrInfoSetMap();
//			dataSet.setChromosomes(chromosomeInfo.keySet(), chromosomeInfo.values());

//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllSamplesFromParent(true);
//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllMarkersFromParent(true);
//			((AbstractNetCdfOperationDataSet) dataSet).setUseAllChromosomesFromParent(true);

			//<editor-fold defaultstate="expanded" desc="PROCESSOR">
			// INIT MARKER AND SAMPLE INFO
//			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

//			List<SampleInfo> sampleInfos = SampleInfoList.getAllSampleInfoFromDBByPoolID(rdMatrixMetadata.getStudyKey());
//			Map<SampleKey, Sex> samplesInfoMap = new LinkedHashMap<SampleKey, Sex>();
//			SamplesKeysSource rdSampleKeys = rdDataSetSource.getSamplesKeysSource();
//			for (SampleInfo sampleInfo : sampleInfos) {
//				SampleKey tempSampleId = sampleInfo.getKey();
//				if (rdSampleKeys.contains(tempSampleId)) {
//					Sex sex = sampleInfo.getSex();
//					samplesInfoMap.put(tempSampleId, sex);
//				}
//			}
//			sampleInfos.clear();
//			List<Sex> sampleSexes = samplesInfoMap.values();
			List<Sex> sampleSexes = rdDataSetSource.getSamplesInfosSource().getSexes();

			// Iterate through markerset, take it marker by marker
			Iterator<GenotypesList> markersGenotypesSourceIt = rdDataSetSource.getMarkersGenotypesSource().iterator();
			for (Map.Entry<Integer, MarkerKey> markerOrigIndexKey : rdDataSetSource.getMarkersKeysSource().getIndicesMap().entrySet()) {
				final int markerOrigIndex = markerOrigIndexKey.getKey();
				final MarkerKey markerKey = markerOrigIndexKey.getValue();
				GenotypesList markerGenotypes = markersGenotypesSourceIt.next();
				Map<Byte, Float> tmpKnownAlleles = new LinkedHashMap<Byte, Float>();
				Map<Short, Float> allSamplesGTsTable = new LinkedHashMap<Short, Float>();
				Map<String, Integer> allSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Integer missingCount = 0;

				// Get a sampleset-full of GTs
				byte[] tempGT = new byte[2];
				Iterator<byte[]> markerSamplesGenotypesIt = markerGenotypes.iterator();
				for (Sex sampleSex : sampleSexes) {
					byte[] markerSampleGenotype = markerSamplesGenotypesIt.next();

					//<editor-fold defaultstate="expanded" desc="THE DECIDER">
					CensusDecision decision = CensusDecision.getDecisionByChrAndSex(new String(markerSampleGenotype), sampleSex);
					//</editor-fold>

					//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
					float counter = 1;
//					byte[] tempGT = genotype;
//					byte[] tempGT = genotype.clone();
					tempGT[0] = markerSampleGenotype[0];
					tempGT[1] = markerSampleGenotype[1];
					// Gather alleles different from 0 into a list of known alleles and count the number of appearences
					// 48 is byte for 0
					// 65 is byte for A
					// 67 is byte for C
					// 71 is byte for G
					// 84 is byte for T

					if (tempGT[0] != AlleleBytes._0) {
						float tempCount = 0;
						if (tmpKnownAlleles.containsKey(tempGT[0])) {
							tempCount = tmpKnownAlleles.get(tempGT[0]);
						}
						tmpKnownAlleles.put(tempGT[0], tempCount + counter);
					}
					if (tempGT[1] != AlleleBytes._0) {
						float tempCount = 0;
						if (tmpKnownAlleles.containsKey(tempGT[1])) {
							tempCount = tmpKnownAlleles.get(tempGT[1]);
						}
						tmpKnownAlleles.put(tempGT[1], tempCount + counter);
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
				final Boolean mismatches;
				final Census census;
				final Double missingRatio;
				final OrderedAlleles knownAlleles;

				OrderedAlleles orderedAlleles = null;
				if (tmpKnownAlleles.size() <= 2) { // Check if there are mismatches in alleles
					//<editor-fold defaultstate="expanded" desc="KNOW YOUR ALLELES">
					List<Integer> intAA = new ArrayList<Integer>();
					List<Integer> intAa = new ArrayList<Integer>();
					List<Integer> intaa = new ArrayList<Integer>();

					Iterator<Byte> itKnAll = tmpKnownAlleles.keySet().iterator();
					if (tmpKnownAlleles.isEmpty()) {
						// Completely missing (00)
						orderedAlleles = new OrderedAlleles();
					} else if (tmpKnownAlleles.size() == 1) {
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
					} else if (tmpKnownAlleles.size() == 2) {
						// Heterezygote (contains mix of AA, Aa or aa)
						final byte byteAllele1 = itKnAll.next();
						final int countAllele1 = Math.round(tmpKnownAlleles.get(byteAllele1));
						final int intAllele1 = byteAllele1;
						final byte byteAllele2 = itKnAll.next();
						final int countAllele2 = Math.round(tmpKnownAlleles.get(byteAllele2));
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
								+ tmpKnownAlleles.size() + ")");
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

					census = new Census(
							obsAllAA, // all
							obsAllAa, // all
							obsAllaa, // all
							missingCount); // all

					mismatches = cNetCDF.Defaults.MISMATCH_NO;

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

					knownAlleles = orderedAlleles;
				} else {
					census = new Census();
					mismatches = cNetCDF.Defaults.MISMATCH_YES;

					orderedAlleles = new OrderedAlleles();
					knownAlleles = orderedAlleles;
				}

				missingRatio = (double) missingCount / rdDataSetSource.getNumSamples();

				((AbstractOperationDataSet) dataSet).addEntry(new DefaultQAMarkersOperationEntry(
						markerKey,
						markerOrigIndex,
						missingRatio,
						mismatches,
						knownAlleles.getAllele1(),
						knownAlleles.getAllele1Freq(),
						knownAlleles.getAllele2(),
						knownAlleles.getAllele2Freq(),
						census.getAA(),
						census.getAa(),
						census.getaa(),
						census.getMissingCount()
				));
			}
			//</editor-fold>

			dataSet.finnishWriting();
			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK
		} finally {
			org.gwaspi.global.Utils.sysoutCompleted("Marker QA");
		}

		return resultOpId;
	}
}
