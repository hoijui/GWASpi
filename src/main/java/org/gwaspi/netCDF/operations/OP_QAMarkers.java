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
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
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
		final float counter = 1.0f;

		RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics = new RawMarkerAlleleAndGTStatistics(alleleValueToOrdinal);

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

		// Check if there are mismatches in alleles
		if (alleleCounts.size() > 2) {
			markerAlleleAndGTStatistics.setMismatch();
		} else {
			//<editor-fold defaultstate="expanded" desc="KNOW YOUR ALLELES">
			Iterator<Byte> itKnAll = alleleCounts.keySet().iterator();
			if (alleleCounts.isEmpty()) {
				// Completely missing (00)
				markerAlleleAndGTStatistics.setMajorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMinorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMajorAlleleFreq(0.0); // NOTE Maybe 1.0 would be better?
			} else if (alleleCounts.size() == 1) {
				// Homozygote (AA or aa)
				final byte majorAllele = itKnAll.next();
				markerAlleleAndGTStatistics.setMajorAllele(majorAllele);
				markerAlleleAndGTStatistics.setMinorAllele(AlleleByte._0_VALUE);
				markerAlleleAndGTStatistics.setMajorAlleleFreq(1.0);
			} else if (alleleCounts.size() == 2) {
				// Heterezygote (contains mix of AA, Aa/aA or aa)
				final byte allele1 = itKnAll.next();
				final int allele1Count = Math.round(alleleCounts.get(allele1));
				final byte allele2 = itKnAll.next();
				final int allele2Count = Math.round(alleleCounts.get(allele2));
				final int totAlleles = allele1Count + allele2Count;

				// Finding out what allele is major and minor
				if (allele1Count >= allele2Count) {
					markerAlleleAndGTStatistics.setMajorAllele(allele1);
					markerAlleleAndGTStatistics.setMinorAllele(allele2);
					markerAlleleAndGTStatistics.setMajorAlleleFreq((double) allele1Count / totAlleles);
				} else {
					markerAlleleAndGTStatistics.setMajorAllele(allele2);
					markerAlleleAndGTStatistics.setMinorAllele(allele1);
					markerAlleleAndGTStatistics.setMajorAlleleFreq((double) allele2Count / totAlleles);
				}
			} else {
				throw new IOException("More then 2 known alleles ("
						+ alleleCounts.size() + ")");
			}
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="CONTINGENCY ALL SAMPLES">
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
		}

		return markerAlleleAndGTStatistics;
	}
}
