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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.model.Census;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerAlleleAndGTStatistics;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.RawMarkerAlleleAndGTStatistics;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.operations.qamarkers.DefaultQAMarkersOperationEntry;
import org.gwaspi.operations.qamarkers.OrderedAlleles;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;

public class OP_QAMarkers extends AbstractOperation<QAMarkersOperationDataSet> {

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

		int resultOpId;

		DataSetSource parentDataSetSource = getParentDataSetSource();

		QAMarkersOperationDataSet dataSet = generateFreshOperationDataSet();

		dataSet.setNumMarkers(parentDataSetSource.getNumMarkers());
		dataSet.setNumChromosomes(parentDataSetSource.getNumChromosomes());
		dataSet.setNumSamples(parentDataSetSource.getNumSamples());

		final List<Sex> sampleSexes = parentDataSetSource.getSamplesInfosSource().getSexes();

		// A is the major allele (the 'letter' (!= '0') which appears most often)
		// a is the minor allele (the 'letter' (!= '0') which appears less often)
		// There is always a major allele, but sometimes no minor allele

		final int[] alleleValueToOrdinal = AlleleByte.createByteValueToOrdinalTable();
//		final int alleleByte0Ordinal = AlleleByte._0.ordinal();
		final float counter = 1.0f;
//		// counts which allele appears how many times per marker,
//		// whether in the father or in the mother position
//		final float[] knownAllelesOrdinalTable = new float[AlleleByte.values().length];
//		// counts which allele combinations (genotypes, father & mother allele)
//		// appears how many times per marker
//		final float[][] knownGTsOrdinalTable = new float[knownAllelesOrdinalTable.length][knownAllelesOrdinalTable.length];

		RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics = new RawMarkerAlleleAndGTStatistics(alleleValueToOrdinal);

		// Iterate through markerset, take it marker by marker
		Iterator<GenotypesList> markersGenotypesSourceIt = parentDataSetSource.getMarkersGenotypesSource().iterator();
		for (Map.Entry<Integer, MarkerKey> markerOrigIndexKey : parentDataSetSource.getMarkersKeysSource().getIndicesMap().entrySet()) {
			final int markerOrigIndex = markerOrigIndexKey.getKey();
			final MarkerKey markerKey = markerOrigIndexKey.getValue();
			GenotypesList markerGenotypes = markersGenotypesSourceIt.next();

			rawMarkerAlleleAndGTStatistics.clear();
			gatherRawMarkerAlleleAndGTStatistics(
					rawMarkerAlleleAndGTStatistics,
					sampleSexes,
					alleleValueToOrdinal,
					counter,
					markerGenotypes);

			MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics
					= calculateMarkerAlleleAndGTStatistics(rawMarkerAlleleAndGTStatistics);
//
//			Arrays.fill(knownAllelesOrdinalTable, 0.0f);
//			for (float[] knownGTsTableRow : knownGTsOrdinalTable) {
//				Arrays.fill(knownGTsTableRow, 0.0f);
//			}
//			int missingCount = 0; // number of observed GTs of type: 00
//
//			// Get a sampleset-full of GTs
//			Iterator<byte[]> markerSamplesGenotypesIt = markerGenotypes.iterator();
//			for (Sex sampleSex : sampleSexes) {
//				byte[] markerSampleGenotype = markerSamplesGenotypesIt.next();
//
//				//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
//				final byte allele1 = markerSampleGenotype[0];
//				final byte allele2 = markerSampleGenotype[1];
//				final int allele1Ordinal = alleleValueToOrdinal[allele1];
//				final int allele2Ordinal = alleleValueToOrdinal[allele2];
//
//				knownAllelesOrdinalTable[allele1Ordinal] += counter;
//				knownAllelesOrdinalTable[allele2Ordinal] += counter;
//				if ((allele1 == AlleleByte._0_VALUE) && (allele2 == AlleleByte._0_VALUE)) {
//					CensusDecision decision = CensusDecision.getDecisionByChrAndSex(new String(markerSampleGenotype), sampleSex);
//					if (decision != CensusDecision.CountFemalesNonAutosomally) {
//						missingCount++;
//					}
//				}
//
//				knownGTsOrdinalTable[allele1Ordinal][allele2Ordinal] += counter;
//				//</editor-fold>
//			}
//
//			// transcribe ordinal tables into value maps
//			final Map<Byte, Float> knownAllelesOrdinalMap = new LinkedHashMap<Byte, Float>(3);
//			for (int ao = 0; ao < knownAllelesOrdinalTable.length; ao++) {
//				if (knownAllelesOrdinalTable[ao] != 0.0f) {
//					knownAllelesOrdinalMap.put(AlleleByte.values()[ao].getValue(), knownAllelesOrdinalTable[ao]);
//				}
//			}
//			knownAllelesOrdinalMap.remove(AlleleByte._0_VALUE);
//
//			final Map<Short, Float> knownGTsMap = new LinkedHashMap<Short, Float>(3);
//			for (int a1o = 0; a1o < knownGTsOrdinalTable.length; a1o++) {
//				for (int a2o = 0; a2o < knownGTsOrdinalTable.length; a2o++) {
//					if (knownGTsOrdinalTable[a1o][a2o] != 0.0f) {
//						knownGTsMap.put((short) (a1o + a2o), knownGTsOrdinalTable[a1o][a2o]);
//					}
//				}
//			}
//
//			// ALLELE CENSUS + MISMATCH STATE + MISSINGNESS
//			final Boolean mismatches;
//			final Census census;
//			final Double missingRatio;
//			final OrderedAlleles knownAlleles;
//
//			OrderedAlleles orderedAlleles = null;
//			if (knownAllelesOrdinalMap.size() <= 2) { // Check if there are mismatches in alleles
//				//<editor-fold defaultstate="expanded" desc="KNOW YOUR ALLELES">
//				Iterator<Byte> itKnAll = knownAllelesOrdinalMap.keySet().iterator();
//				if (knownAllelesOrdinalMap.isEmpty()) {
//					// Completely missing (00)
//					orderedAlleles = new OrderedAlleles();
//				} else if (knownAllelesOrdinalMap.size() == 1) {
//					// Homozygote (AA or aa)
//					final byte majorAllele = itKnAll.next();
//					final byte minorAllele = AlleleByte._0_VALUE;
//
//					orderedAlleles = new OrderedAlleles(
//							majorAllele,
//							1.0,
//							minorAllele
//							);
//				} else if (knownAllelesOrdinalMap.size() == 2) {
//					// Heterezygote (contains mix of AA, Aa/aA or aa)
//					final byte byteAllele1 = itKnAll.next();
//					final int countAllele1 = Math.round(knownAllelesOrdinalMap.get(byteAllele1));
//					final byte byteAllele2 = itKnAll.next();
//					final int countAllele2 = Math.round(knownAllelesOrdinalMap.get(byteAllele2));
//					final int totAlleles = countAllele1 + countAllele2;
//
//					// Finding out what allele is major and minor
//					if (countAllele1 >= countAllele2) {
//						orderedAlleles = new OrderedAlleles(
//								byteAllele1,
//								(double) countAllele1 / totAlleles,
//								byteAllele2
//								);
//					} else {
//						orderedAlleles = new OrderedAlleles(
//								byteAllele2,
//								(double) countAllele2 / totAlleles,
//								byteAllele1
//								);
//					}
//				} else {
//					throw new IOException("More then 2 known alleles ("
//							+ knownAllelesOrdinalMap.size() + ")");
//				}
//				//</editor-fold>
//
//				//<editor-fold defaultstate="expanded" desc="CONTINGENCY ALL SAMPLES">
//				int obsAA = 0; // number of observed GTs of type: AA, A0, 0A
//				int obsAa = 0; // number of observed GTs of type: Aa, aA
//				int obsaa = 0; // number of observed GTs of type: aa, a0, 0a
//				if (orderedAlleles.getMajorAllele() != AlleleByte._0_VALUE) {
//					final int majorAlleleOrdinal = alleleValueToOrdinal[orderedAlleles.getMajorAllele()];
//					obsAA
//							= Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][majorAlleleOrdinal])  // #AA
//							+ Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][alleleByte0Ordinal])  // #A0
//							+ Math.round(knownGTsOrdinalTable[alleleByte0Ordinal][majorAlleleOrdinal]); // #0A
//					if (orderedAlleles.getMinorAllele() != AlleleByte._0_VALUE) {
//						final int minorAlleleOrdinal = alleleValueToOrdinal[orderedAlleles.getMinorAllele()];
//						obsAa
//								= Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][minorAlleleOrdinal])  // #Aa
//								+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][majorAlleleOrdinal]); // #aA
//						obsaa
//								= Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][minorAlleleOrdinal])  // #aa
//								+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][alleleByte0Ordinal])  // #a0
//								+ Math.round(knownGTsOrdinalTable[alleleByte0Ordinal][minorAlleleOrdinal]); // #0a
//					}
//				}
//				//</editor-fold>
//
//				// CENSUS
//				census = new Census(
//						obsAA,
//						obsAa,
//						obsaa,
//						missingCount);
//
//				mismatches = cNetCDF.Defaults.MISMATCH_NO;
//
//				// NOTE This was checking for <code>== null</code>
//				//   (which was never the case)
//				//   instead of <code>== '0'</code> before.
//				//   Therefore, some '0' were left in the end
//				//   (when there was only one known allele).
//				if (orderedAlleles.getMajorAllele() == AlleleByte._0_VALUE
//						&& orderedAlleles.getMinorAllele() != AlleleByte._0_VALUE)
//				{
//					orderedAlleles.setMajorAllele(orderedAlleles.getMinorAllele());
//				} else if (orderedAlleles.getMinorAllele() == AlleleByte._0_VALUE
//						&& orderedAlleles.getMajorAllele() != AlleleByte._0_VALUE)
//				{
//					orderedAlleles.setMinorAllele(orderedAlleles.getMajorAllele());
//				}
//
//				knownAlleles = orderedAlleles;
//			} else {
//				census = new Census();
//				mismatches = cNetCDF.Defaults.MISMATCH_YES;
//
//				orderedAlleles = new OrderedAlleles();
//				knownAlleles = orderedAlleles;
//			}
//

			final double missingRatio = (double) rawMarkerAlleleAndGTStatistics.getMissingCount() / parentDataSetSource.getNumSamples();

			((AbstractOperationDataSet) dataSet).addEntry(new DefaultQAMarkersOperationEntry(
					markerKey,
					markerOrigIndex,
					missingRatio,
					markerAlleleAndGTStatistics.isMismatch(),
					markerAlleleAndGTStatistics.getMajorAllele(),
					markerAlleleAndGTStatistics.getMajorAlleleFreq(),
					markerAlleleAndGTStatistics.getMinorAllele(),
					1.0 - markerAlleleAndGTStatistics.getMajorAlleleFreq(),
					markerAlleleAndGTStatistics.getNumAA(),
					markerAlleleAndGTStatistics.getNumAa(),
					markerAlleleAndGTStatistics.getNumaa(),
					rawMarkerAlleleAndGTStatistics.getMissingCount()
			));
		}
		//</editor-fold>

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Marker QA");

		return resultOpId;
	}

	/**
	 * This gathers all possible raw statistics about the alleles and genotypes
	 * of a single marker.
	 * With allele statistics, we throw away the info of whether an allele
	 * is from the father or the mother.
	 * With genotype statistics, we look at the two allele as an ordered pair
	 * (fatherAllele_motherAllele).
	 *
	 * @param rawMarkerAlleleAndGTStatistics
	 * @param sampleSexes
	 * @param alleleValueToOrdinal
	 * @param counter
	 * @param markerGenotypes
	 * @throws IOException
	 */
	public static void gatherRawMarkerAlleleAndGTStatistics(
			final RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics,
			final List<Sex> sampleSexes,
			final int[] alleleValueToOrdinal,
			final float counter,
			final GenotypesList markerGenotypes)
			throws IOException
	{
		final float[] knownAllelesOrdinalTable = rawMarkerAlleleAndGTStatistics.getAlleleOrdinalCounts();
		final float[][] knownGTsOrdinalTable = rawMarkerAlleleAndGTStatistics.getGtOrdinalCounts();

		// number of observed GTs of type 00; depending on decision
		int missingCount = 0;

		// Count alleles and genotypes
		Iterator<byte[]> markerSamplesGenotypesIt = markerGenotypes.iterator();
		for (Sex sampleSex : sampleSexes) {
			byte[] markerSampleGenotype = markerSamplesGenotypesIt.next();

			//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
			final byte allele1 = markerSampleGenotype[0];
			final byte allele2 = markerSampleGenotype[1];
			final int allele1Ordinal = alleleValueToOrdinal[allele1];
			final int allele2Ordinal = alleleValueToOrdinal[allele2];

			knownAllelesOrdinalTable[allele1Ordinal] += counter;
			knownAllelesOrdinalTable[allele2Ordinal] += counter;
			if ((allele1 == AlleleByte._0_VALUE) && (allele2 == AlleleByte._0_VALUE)) {
				CensusDecision decision = CensusDecision.getDecisionByChrAndSex(new String(markerSampleGenotype), sampleSex);
				if (decision != CensusDecision.CountFemalesNonAutosomally) {
					missingCount++;
				}
			}

			knownGTsOrdinalTable[allele1Ordinal][allele2Ordinal] += counter;
			//</editor-fold>
		}
		rawMarkerAlleleAndGTStatistics.setMissingCount(missingCount);
	}

	/**
	 * This calculates secondary statistics about the alleles and genotypes
	 * of a single marker.
	 * The values we gather here are all derived from the raw statistics.
	 *
	 * @param rawMarkerAlleleAndGTStatistics
	 * @return
	 * @throws IOException
	 */
	public static MarkerAlleleAndGTStatistics calculateMarkerAlleleAndGTStatistics(
			final RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics)
			throws IOException
	{
		MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics = new MarkerAlleleAndGTStatistics();

		final int[] alleleValueToOrdinal = rawMarkerAlleleAndGTStatistics.getAlleleValueToOrdinalLookupTable();
		final float[] knownAllelesOrdinalTable = rawMarkerAlleleAndGTStatistics.getAlleleOrdinalCounts();
		final float[][] knownGTsOrdinalTable = rawMarkerAlleleAndGTStatistics.getGtOrdinalCounts();

		// transcribe ordinal tables into value maps
		final Map<Byte, Float> alleleCounts = rawMarkerAlleleAndGTStatistics.extractAllelesCounts();
		alleleCounts.remove(AlleleByte._0_VALUE);

//		// ALLELE CENSUS + MISMATCH STATE + MISSINGNESS
//		final Boolean mismatches;
//		final Census census;
//		final Double missingRatio;
//		final OrderedAlleles knownAlleles;
//
//		OrderedAlleles orderedAlleles = null;
		// Check if there are mismatches in alleles
		if (alleleCounts.size() > 2) {
//			markerAlleleAndGTStatistics.setMismatches(cNetCDF.Defaults.MISMATCH_YES);
			markerAlleleAndGTStatistics.setMismatch();

//			markerAlleleAndGTStatistics.setMajorAllele(AlleleByte._0_VALUE);
//			markerAlleleAndGTStatistics.setMinorAllele(AlleleByte._0_VALUE);
//
//			census = new Census();
//			orderedAlleles = new OrderedAlleles();
//			knownAlleles = orderedAlleles;
		} else {
//			markerAlleleAndGTStatistics.setMismatches(cNetCDF.Defaults.MISMATCH_NO);

			//<editor-fold defaultstate="expanded" desc="KNOW YOUR ALLELES">
			Iterator<Byte> itKnAll = alleleCounts.keySet().iterator();
			if (alleleCounts.isEmpty()) {
				// Completely missing (00)
//				orderedAlleles = new OrderedAlleles();
				markerAlleleAndGTStatistics.setMajorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMinorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMajorAlleleFreq(0.0); // NOTE Maybe 1.0 would be better?
			} else if (alleleCounts.size() == 1) {
				// Homozygote (AA or aa)
				final byte majorAllele = itKnAll.next();
//				final byte minorAllele = AlleleByte._0_VALUE;
				markerAlleleAndGTStatistics.setMajorAllele(majorAllele);
				markerAlleleAndGTStatistics.setMinorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMajorAlleleFreq(1.0);

//				orderedAlleles = new OrderedAlleles(
//						majorAllele,
//						1.0,
//						minorAllele
//						);
			} else if (alleleCounts.size() == 2) {
				// Heterezygote (contains mix of AA, Aa/aA or aa)
				final byte byteAllele1 = itKnAll.next();
				final int countAllele1 = Math.round(alleleCounts.get(byteAllele1));
				final byte byteAllele2 = itKnAll.next();
				final int countAllele2 = Math.round(alleleCounts.get(byteAllele2));
				final int totAlleles = countAllele1 + countAllele2;

				// Finding out what allele is major and minor
				if (countAllele1 >= countAllele2) {
//					orderedAlleles = new OrderedAlleles(
//							byteAllele1,
//							(double) countAllele1 / totAlleles,
//							byteAllele2
//							);
					markerAlleleAndGTStatistics.setMajorAllele(byteAllele1);
					markerAlleleAndGTStatistics.setMinorAllele(byteAllele2);
					markerAlleleAndGTStatistics.setMajorAlleleFreq((double) countAllele1 / totAlleles);
				} else {
//					orderedAlleles = new OrderedAlleles(
//							byteAllele2,
//							(double) countAllele2 / totAlleles,
//							byteAllele1
//							);
					markerAlleleAndGTStatistics.setMajorAllele(byteAllele2);
					markerAlleleAndGTStatistics.setMinorAllele(byteAllele1);
					markerAlleleAndGTStatistics.setMajorAlleleFreq((double) countAllele2 / totAlleles);
				}
			} else {
				throw new IOException("More then 2 known alleles ("
						+ alleleCounts.size() + ")");
			}
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="CONTINGENCY ALL SAMPLES">
//			int obsAA = 0; // number of observed GTs of type: AA, A0, 0A
//			int obsAa = 0; // number of observed GTs of type: Aa, aA
//			int obsaa = 0; // number of observed GTs of type: aa, a0, 0a
			if (markerAlleleAndGTStatistics.getMajorAllele() != AlleleByte._0_VALUE) {
				final int majorAlleleOrdinal = alleleValueToOrdinal[markerAlleleAndGTStatistics.getMajorAllele()];
				markerAlleleAndGTStatistics.setNumAA(
						Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][majorAlleleOrdinal])  // #AA
						+ Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #A0
						+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][majorAlleleOrdinal])); // #0A
				if (markerAlleleAndGTStatistics.getMinorAllele() != AlleleByte._0_VALUE) {
					final int minorAlleleOrdinal = alleleValueToOrdinal[markerAlleleAndGTStatistics.getMinorAllele()];
					markerAlleleAndGTStatistics.setNumAa(
							Math.round(knownGTsOrdinalTable[majorAlleleOrdinal][minorAlleleOrdinal])  // #Aa
							+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][majorAlleleOrdinal])); // #aA
					markerAlleleAndGTStatistics.setNumaa(
							Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][minorAlleleOrdinal])  // #aa
							+ Math.round(knownGTsOrdinalTable[minorAlleleOrdinal][AlleleByte._0_ORDINAL])  // #a0
							+ Math.round(knownGTsOrdinalTable[AlleleByte._0_ORDINAL][minorAlleleOrdinal])); // #0a
				} else {
					markerAlleleAndGTStatistics.setNumAa(0);
					markerAlleleAndGTStatistics.setNumaa(0);
				}
			} else {
				markerAlleleAndGTStatistics.setNumAA(0);
			}
			//</editor-fold>

			// CENSUS
//			census = new Census(
//					obsAA,
//					obsAa,
//					obsaa,
//					missingCount);

			// NOTE This was checking for <code>== null</code>
			//   (which was never the case)
			//   instead of <code>== '0'</code> before.
			//   Therefore, some '0' were left in the end
			//   (when there was only one known allele).
			if (markerAlleleAndGTStatistics.getMajorAllele() == AlleleByte._0_VALUE
					&& markerAlleleAndGTStatistics.getMinorAllele() != AlleleByte._0_VALUE)
			{
				markerAlleleAndGTStatistics.setMajorAllele(markerAlleleAndGTStatistics.getMinorAllele());
			} else if (markerAlleleAndGTStatistics.getMinorAllele() == AlleleByte._0_VALUE
					&& markerAlleleAndGTStatistics.getMajorAllele() != AlleleByte._0_VALUE)
			{
				markerAlleleAndGTStatistics.setMinorAllele(markerAlleleAndGTStatistics.getMajorAllele());
			}

//			knownAlleles = orderedAlleles;
		}

		return markerAlleleAndGTStatistics;
	}
}
