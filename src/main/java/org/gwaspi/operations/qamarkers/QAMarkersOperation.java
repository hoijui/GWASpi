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

package org.gwaspi.operations.qamarkers;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.NetCDFConstants.Defaults.AlleleByte;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.CensusDecision;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.AbstractOperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.ProgressHandler;
import org.gwaspi.progress.ProgressSource;

public class QAMarkersOperation extends AbstractOperationCreatingOperation<QAMarkersOperationDataSet, QAMarkersOperationParams> {

	private static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			"Markers Quality Assurance",
			""); // TODO

	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new QAMarkersOperationFactory());
	}

	public QAMarkersOperation(QAMarkersOperationParams params) {
		super(params);
	}

	@Override
	public OperationTypeInfo getTypeInfo() {
		return QAMarkersOperationFactory.OPERATION_TYPE_INFO;
	}

	@Override
	public ProgressSource getProgressSource() throws IOException {
		return getProgressHandler();
	}

	@Override
	public ProcessInfo getProcessInfo() {
		return PROCESS_INFO;
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

		final ProgressHandler progressHandler = getProgressHandler();
		progressHandler.setNewStatus(ProcessStatus.INITIALIZING);

		DataSetSource parentDataSetSource = getParentDataSetSource();

		QAMarkersOperationDataSet dataSet = generateFreshOperationDataSet();

		final int numSamples = parentDataSetSource.getNumSamples();
		dataSet.setNumMarkers(parentDataSetSource.getNumMarkers());
		dataSet.setNumChromosomes(parentDataSetSource.getNumChromosomes());
		dataSet.setNumSamples(numSamples);

		final List<Sex> sampleSexes = parentDataSetSource.getSamplesInfosSource().getSexes();

		// A is the major allele (the 'letter' (!= '0') which appears most often)
		// a is the minor allele (the 'letter' (!= '0') which appears less often)
		// There is always a major allele, but sometimes no minor allele

		final int[] alleleValueToOrdinalLookupTable = AlleleByte.createAlleleValueToOrdinalLookupTable();
		final float counter = 1.0f;

		RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics = new RawMarkerAlleleAndGTStatistics(alleleValueToOrdinalLookupTable);

		MarkersGenotypesSource markersGenotypesSource = parentDataSetSource.getMarkersGenotypesSource();
		Iterator<GenotypesList> markersGenotypesSourceIt = markersGenotypesSource.iterator();
		Iterator<String> markersChromosomesIt = parentDataSetSource.getMarkersMetadatasSource().getChromosomes().iterator();
		Map<Integer, MarkerKey> markersIndicesMap = parentDataSetSource.getMarkersKeysSource().getIndicesMap();
		int localMarkerIndex = 0;
		progressHandler.setNewStatus(ProcessStatus.RUNNING);
		for (Map.Entry<Integer, MarkerKey> markerOrigIndexKey : markersIndicesMap.entrySet()) {
			final int markerOrigIndex = markerOrigIndexKey.getKey();
			final MarkerKey markerKey = markerOrigIndexKey.getValue();
			final GenotypesList markerGenotypes = markersGenotypesSourceIt.next();
			final String chromosome = markersChromosomesIt.next();

			gatherRawMarkerAlleleAndGTStatistics(
					rawMarkerAlleleAndGTStatistics,
					chromosome,
					sampleSexes,
					counter,
					markerGenotypes);

			MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics
					= calculateMarkerAlleleAndGTStatistics(rawMarkerAlleleAndGTStatistics);
			extractCompactStatistics(rawMarkerAlleleAndGTStatistics, markerAlleleAndGTStatistics, numSamples);

			final double missingRatio = (double) rawMarkerAlleleAndGTStatistics.getMissingCount() / parentDataSetSource.getNumSamples();

			((AbstractOperationDataSet) dataSet).addEntry(new DefaultQAMarkersOperationEntry(
					markerKey,
					markerOrigIndex,
					markerAlleleAndGTStatistics.isMismatch(),
					markerAlleleAndGTStatistics.getMajorAllele(),
					markerAlleleAndGTStatistics.getMajorAlleleFreq(),
					markerAlleleAndGTStatistics.getMinorAllele(),
					1.0 - markerAlleleAndGTStatistics.getMajorAlleleFreq(),
//					markerAlleleAndGTStatistics.getNumAA(),
//					markerAlleleAndGTStatistics.getNumAa(),
//					markerAlleleAndGTStatistics.getNumaa(),
					rawMarkerAlleleAndGTStatistics.getMissingCount(),
					missingRatio,
					markerAlleleAndGTStatistics.getCompactAlleleStatistics(),
					markerAlleleAndGTStatistics.getCompactGenotypeStatistics()
			));
			progressHandler.setProgress(localMarkerIndex);
			localMarkerIndex++;
		}
		progressHandler.setNewStatus(ProcessStatus.FINALIZING);
		//</editor-fold>

		dataSet.finnishWriting();
		resultOpId = dataSet.getOperationKey().getId();

		org.gwaspi.global.Utils.sysoutCompleted("Marker QA");
		progressHandler.setNewStatus(ProcessStatus.COMPLEETED);

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
	 * @param chromosome
	 * @param sampleSexes
	 * @param counter
	 * @param markerGenotypes
	 * @throws IOException
	 */
	public static void gatherRawMarkerAlleleAndGTStatistics(
			final RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics,
			final String chromosome,
			final List<Sex> sampleSexes,
			final float counter,
			final GenotypesList markerGenotypes)
			throws IOException
	{
		rawMarkerAlleleAndGTStatistics.clear();

		final int[] alleleValueToOrdinalLookupTable = rawMarkerAlleleAndGTStatistics.getAlleleValueToOrdinalLookupTable();

		final float[] alleleOrdinalCounts = rawMarkerAlleleAndGTStatistics.getAlleleOrdinalCounts();
		final float[][] gtOrdinalCounts = rawMarkerAlleleAndGTStatistics.getGtOrdinalCounts();

		// number of observed GTs of type 00; depending on decision
		int missingCount = 0;

		// Count alleles and genotypes
		Iterator<byte[]> markerSamplesGenotypesIt = markerGenotypes.iterator();
		for (Sex sampleSex : sampleSexes) {
			byte[] markerSampleGenotype = markerSamplesGenotypesIt.next();

			//<editor-fold defaultstate="expanded" desc="SUMMING SAMPLESET GENOTYPES">
			final byte allele1 = markerSampleGenotype[0];
			final byte allele2 = markerSampleGenotype[1];
			final int allele1Ordinal = alleleValueToOrdinalLookupTable[allele1];
			final int allele2Ordinal = alleleValueToOrdinalLookupTable[allele2];

			alleleOrdinalCounts[allele1Ordinal] += counter;
			alleleOrdinalCounts[allele2Ordinal] += counter;
			if ((allele1 == AlleleByte._0_VALUE) && (allele2 == AlleleByte._0_VALUE)) {
				CensusDecision decision = CensusDecision.getDecisionByChrAndSex(chromosome, sampleSex);
				if (decision != CensusDecision.CountFemalesNonAutosomally) {
					missingCount++;
				}
			}

			gtOrdinalCounts[allele1Ordinal][allele2Ordinal] += counter;
			//</editor-fold>
		}
		rawMarkerAlleleAndGTStatistics.setMissingCount(missingCount);
	}

	public static void extractMajorAndMinorAllele(
			final Map<Byte, Float> alleleCounts,
			final MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics)
	{
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
		}
	}

	private static void extractContingency(
			final RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics,
			final MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics)
	{
		extractContingency(
				rawMarkerAlleleAndGTStatistics.getAlleleValueToOrdinalLookupTable(),
				rawMarkerAlleleAndGTStatistics.getGtOrdinalCounts(),
				markerAlleleAndGTStatistics);
	}

	public static void extractContingency(
			final int[] alleleValueToOrdinal,
			final float[][] knownGTsOrdinalTable,
			final MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics)
	{
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
	}

	public static void leaveNoSingleZeroAlleleBehind(
			final MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics)
	{
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

		// transcribe ordinal tables into value maps
		final Map<Byte, Float> alleleCounts = rawMarkerAlleleAndGTStatistics.extractAllelesCounts();
		alleleCounts.remove(AlleleByte._0_VALUE);

		// Check if there are mismatches in alleles
		if (alleleCounts.size() > 2) {
			markerAlleleAndGTStatistics.setMismatch();
//			throw new IOException("More then 2 known alleles ("
//					+ alleleCounts.size() + ")");
		} else {
			extractMajorAndMinorAllele(alleleCounts, markerAlleleAndGTStatistics);
			extractContingency(rawMarkerAlleleAndGTStatistics, markerAlleleAndGTStatistics);
			leaveNoSingleZeroAlleleBehind(markerAlleleAndGTStatistics);
		}

		return markerAlleleAndGTStatistics;
	}

	private static void extractCompactStatistics(
			final RawMarkerAlleleAndGTStatistics rawMarkerAlleleAndGTStatistics,
			final MarkerAlleleAndGTStatistics markerAlleleAndGTStatistics,
			final int numSamples)
	{
		final int[] alleleValueToOrdinalLookupTable = rawMarkerAlleleAndGTStatistics.getAlleleValueToOrdinalLookupTable();

		final int majorAlleleOrdinal = alleleValueToOrdinalLookupTable[markerAlleleAndGTStatistics.getMajorAllele()];
		final int minorAlleleOrdinal = alleleValueToOrdinalLookupTable[markerAlleleAndGTStatistics.getMinorAllele()];

		float[] alleleOrdinalCounts = rawMarkerAlleleAndGTStatistics.getAlleleOrdinalCounts();
		final int[] compactAlleleStatistics = new int[] {
			Math.round(alleleOrdinalCounts[majorAlleleOrdinal]),
			Math.round(alleleOrdinalCounts[minorAlleleOrdinal]),
			Math.round(alleleOrdinalCounts[AlleleByte._0_ORDINAL]),
			0 // will be set later
		};
		int remainingAlleleCount = numSamples * 2;
		for (int i = 0; i < compactAlleleStatistics.length - 1; i++) {
			remainingAlleleCount -= compactAlleleStatistics[i];
		}
		compactAlleleStatistics[compactAlleleStatistics.length - 1] = remainingAlleleCount;
		markerAlleleAndGTStatistics.setCompactAlleleStatistics(compactAlleleStatistics);

		float[][] genotypeOrdinalCounts = rawMarkerAlleleAndGTStatistics.getGtOrdinalCounts();
		final int[] compactGenotypeStatistics = new int[] {
			Math.round(genotypeOrdinalCounts[majorAlleleOrdinal][majorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[majorAlleleOrdinal][AlleleByte._0_ORDINAL]),
			Math.round(genotypeOrdinalCounts[AlleleByte._0_ORDINAL][majorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[majorAlleleOrdinal][minorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[minorAlleleOrdinal][majorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[minorAlleleOrdinal][minorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[minorAlleleOrdinal][AlleleByte._0_ORDINAL]),
			Math.round(genotypeOrdinalCounts[AlleleByte._0_ORDINAL][minorAlleleOrdinal]),
			Math.round(genotypeOrdinalCounts[AlleleByte._0_ORDINAL][AlleleByte._0_ORDINAL]),
			0, // will be set later
		};
		int remainingGenotypeCount = numSamples;
		for (int i = 0; i < compactGenotypeStatistics.length - 1; i++) {
			remainingGenotypeCount -= compactGenotypeStatistics[i];
		}
		compactGenotypeStatistics[compactGenotypeStatistics.length - 1] = remainingGenotypeCount;
		markerAlleleAndGTStatistics.setCompactGenotypeStatistics(compactGenotypeStatistics);
	}
}
