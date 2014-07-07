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

package org.gwaspi.operations.markercensus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.gwaspi.constants.cImport.Annotation.GWASpi;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.datasource.filter.SampleIndicesFilterDataSetSource;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.CensusFull;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.AbstractDefaultTypesOperationFactory;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.AbstractOperationCreatingOperation;
import org.gwaspi.operations.CensusDecision;
import org.gwaspi.operations.DefaultOperationTypeInfo;
import org.gwaspi.operations.OperationDataSet;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.OperationTypeInfo;
import org.gwaspi.operations.qamarkers.MarkerAlleleAndGTStatistics;
import org.gwaspi.operations.qamarkers.QAMarkersOperation;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.gwaspi.progress.DefaultProcessInfo;
import org.gwaspi.progress.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkerCensusOperation extends AbstractOperationCreatingOperation<MarkerCensusOperationDataSet, MarkerCensusOperationParams> {

	private final Logger log = LoggerFactory.getLogger(MarkerCensusOperation.class);

	public static final ProcessInfo PROCESS_INFO = new DefaultProcessInfo(
			"Marker Census",
			"Marker Census (== Genotypes frequencies)"); // TODO We need a more elaborate description of this operation!); // TODO We need a more elaborate description of this operation!

	private static final OperationTypeInfo OPERATION_TYPE_INFO
			= new DefaultOperationTypeInfo(
					false,
					"Marker Census",
					"Marker Census (== Genotypes frequencies)", // TODO We need a more elaborate description of this operation!
					OPType.MARKER_CENSUS_BY_AFFECTION,
					true,
					false);
	public static void register() {
		// NOTE When converting to OSGi, this would be done in bundle init,
		//   or by annotations.
		OperationManager.registerOperationFactory(new AbstractDefaultTypesOperationFactory(
				MarkerCensusOperation.class, OPERATION_TYPE_INFO) {
					@Override
					protected OperationDataSet generateReadOperationDataSetNetCdf(OperationKey operationKey, DataSetKey parent, Map<String, Object> properties) throws IOException {
						return new NetCdfMarkerCensusOperationDataSet(parent.getOrigin(), parent, operationKey);
					}
				});
	}

	public MarkerCensusOperation(final MarkerCensusOperationParams params) {
		super(params);
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

		int resultOpId = Integer.MIN_VALUE;

		Map<Integer, SampleKey> excludeSamplesOrigIndexAndKey = new LinkedHashMap<Integer, SampleKey>();
		boolean dataRemaining = pickingMarkersAndSamplesFromQA(
				excludeSamplesOrigIndexAndKey,
				null,
				null,
				null);

		if (!dataRemaining) {
			// NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
			return resultOpId;
		}

		DataSetSource dataSetSource = getParentDataSetSource();

		//<editor-fold defaultstate="expanded" desc="PURGE Maps">
		Map<Integer, SampleKey> wrSampleKeys = new LinkedHashMap<Integer, SampleKey>();
		for (Map.Entry<Integer, SampleKey> origIndexKey : dataSetSource.getSamplesKeysSource().getIndicesMap().entrySet()) {
			if (!excludeSamplesOrigIndexAndKey.containsKey(origIndexKey.getKey())) {
				wrSampleKeys.put(origIndexKey.getKey(), origIndexKey.getValue());
			}
		}
		List<Integer> wrSampleOrigIndices = new ArrayList<Integer>(wrSampleKeys.keySet());
		//</editor-fold>

		MarkerCensusOperationDataSet dataSet = generateFreshOperationDataSet();

		final int numMySamples = wrSampleKeys.size();
//		final int numParentSamples = dataSet.getParentDataSetSource().getNumSamples();

		dataSet.setNumMarkers(dataSetSource.getNumMarkers());
		dataSet.setNumChromosomes(dataSetSource.getNumChromosomes());
		dataSet.setNumSamples(numMySamples);

		dataSet.setParams(getParams());

		dataSet.setSamples(wrSampleKeys);

		//<editor-fold defaultstate="expanded" desc="PROCESSOR">
		final List<Sex> samplesSex = new ArrayList<Sex>(numMySamples);
		final List<Affection> samplesAffection = new ArrayList<Affection>(numMySamples);
		fetchSampleInfo(getParentMatrixKey().getStudyKey(), dataSetSource, wrSampleOrigIndices, getParams().getPhenotypeFile(), samplesSex, samplesAffection);

		log.info("Start Census testing markers");

		final int[] alleleValueToOrdinalLookupTable = AlleleByte.createAlleleValueToOrdinalLookupTable();

		RawMarkerCensusStatistics rawMarkerCensusStatistics = new RawMarkerCensusStatistics(alleleValueToOrdinalLookupTable);

		Iterator<GenotypesList> markersGTsIt = dataSetSource.getMarkersGenotypesSource().iterator();
		Iterator<String> markerChromosomesIt = dataSetSource.getMarkersMetadatasSource().getChromosomes().iterator();
		for (final Map.Entry<Integer, MarkerKey> markerEntry : dataSetSource.getMarkersKeysSource().getIndicesMap().entrySet()) {
			final int markerOrigIndex = markerEntry.getKey();
			final MarkerKey markerKey = markerEntry.getValue();
			final String markerChr = markerChromosomesIt.next();
			final GenotypesList markerGTs = markersGTsIt.next();

			// This is the very expensive task, as we have to read all GTs
			gatherRawMarkerAlleleAndGTStatistics(rawMarkerCensusStatistics, markerChr, samplesSex, samplesAffection, markerGTs);

			MarkerAlleleAndGTStatistics allSamplesStatistics = new MarkerAlleleAndGTStatistics();

			// transcribe ordinal tables into value maps
			final Map<Byte, Float> alleleCounts = rawMarkerCensusStatistics.extractAllelesCounts();
			alleleCounts.remove(AlleleByte._0_VALUE);

			// Check if there are mismatches in alleles
			final byte[] majorAndMinorAlleles;
			final CensusFull censusFull;
			if (alleleCounts.size() > 2) {
				allSamplesStatistics.setMismatch();

				censusFull = new CensusFull();

//				throw new IOException("More then 2 known alleles ("
//						+ alleleCounts.size() + ")");
			} else {
				QAMarkersOperation.extractMajorAndMinorAllele(alleleCounts, allSamplesStatistics);

				// We clone here, as we want to use the same major
				// and minor alleles already extracted.
				MarkerAlleleAndGTStatistics caseSamplesStatistics = allSamplesStatistics.clone();
				MarkerAlleleAndGTStatistics ctrlSamplesStatistics = allSamplesStatistics.clone();
				MarkerAlleleAndGTStatistics hwSamplesStatistics = allSamplesStatistics.clone();

				// all samples
				QAMarkersOperation.extractContingency(
						alleleValueToOrdinalLookupTable,
						rawMarkerCensusStatistics.getGtOrdinalCounts(),
						allSamplesStatistics);

				// case/affected samples
				QAMarkersOperation.extractContingency(
						alleleValueToOrdinalLookupTable,
						rawMarkerCensusStatistics.getCaseGtOrdinalCounts(),
						caseSamplesStatistics);

				// control/unaffected samples
				QAMarkersOperation.extractContingency(
						alleleValueToOrdinalLookupTable,
						rawMarkerCensusStatistics.getControlGtOrdinalCounts(),
						ctrlSamplesStatistics);

				// hardy&weinberg relevant samples
				QAMarkersOperation.extractContingency(
						alleleValueToOrdinalLookupTable,
						rawMarkerCensusStatistics.getHardyWeinbergGtOrdinalCounts(),
						hwSamplesStatistics);

				QAMarkersOperation.leaveNoSingleZeroAlleleBehind(allSamplesStatistics);

				censusFull = new CensusFull(
						new Census(
								allSamplesStatistics.getNumAA(),
								allSamplesStatistics.getNumAa(),
								allSamplesStatistics.getNumaa(),
								rawMarkerCensusStatistics.getMissingCount()),
						new Census(
								caseSamplesStatistics.getNumAA(),
								caseSamplesStatistics.getNumAa(),
								caseSamplesStatistics.getNumaa(),
								-1),
						new Census(
								ctrlSamplesStatistics.getNumAA(),
								ctrlSamplesStatistics.getNumAa(),
								ctrlSamplesStatistics.getNumaa(),
								-1),
						new Census(
								hwSamplesStatistics.getNumAA(),
								hwSamplesStatistics.getNumAa(),
								hwSamplesStatistics.getNumaa(),
								-1)
						);
			}

			majorAndMinorAlleles = new byte[] {
				allSamplesStatistics.getMajorAllele(),
				allSamplesStatistics.getMinorAllele()};

			dataSet.addEntry(new DefaultMarkerCensusOperationEntry(
					markerKey,
					markerOrigIndex,
					majorAndMinorAlleles,
					censusFull));
		}
		//</editor-fold>

		dataSet.finnishWriting();
		resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

		org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");

		return resultOpId;
	}

	/**
	 * PICKING CLEAN MARKERS AND SAMPLES FROM QA.
	 * @return true if there is data left after picking, false otherwise
	 */
	private boolean pickingMarkersAndSamplesFromQA(
			Map<Integer, SampleKey> excludeSamplesOrigIndexAndKey,
			Map<Integer, Double> excludeSampleValue,
			Map<Integer, MarkerKey> excludeMarkersOrigIndexAndKey,
			Map<Integer, Object> excludeMarkersValue)
			throws IOException
	{
		QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationManager.generateOperationDataSet(getParams().getMarkerQAOpKey());
		QASamplesOperationDataSet qaSamplesOperationDataSet = (QASamplesOperationDataSet) OperationManager.generateOperationDataSet(getParams().getSampleQAOpKey());

		final int excludedMarkerNb;
		if (excludeMarkersOrigIndexAndKey != null) {
			// EXCLUDE MARKER BY MISMATCH STATE
			if (getParams().isDiscardMismatches()) {
				Iterator<Boolean> mismatchStatesIt = qaMarkersOperationDataSet.getMismatchStates().iterator();
				for (Map.Entry<Integer, MarkerKey> qaMarkerOrigIndexKey : qaMarkersOperationDataSet.getMarkersKeysSource().getIndicesMap().entrySet()) {
					MarkerKey key = qaMarkerOrigIndexKey.getValue();
					Boolean mismatchState = mismatchStatesIt.next();
					Integer origIndex = qaMarkerOrigIndexKey.getKey();
					if (mismatchState) {
						excludeMarkersOrigIndexAndKey.put(origIndex, key);
						if (excludeMarkersValue != null) {
							excludeMarkersValue.put(origIndex, mismatchState);
						}
					}
				}
			}

			// EXCLUDE MARKER BY MISSING RATIO
			Iterator<Double> missingRatioIt = qaMarkersOperationDataSet.getMissingRatio().iterator();
			for (Map.Entry<Integer, MarkerKey> qaMarkerOrigIndexKey : qaMarkersOperationDataSet.getMarkersKeysSource().getIndicesMap().entrySet()) {
				MarkerKey key = qaMarkerOrigIndexKey.getValue();
				Double missingRatio = missingRatioIt.next();
				Integer origIndex = qaMarkerOrigIndexKey.getKey();
				if (missingRatio > getParams().getMarkerMissingRatio()) {
					excludeMarkersOrigIndexAndKey.put(origIndex, key);
					if (excludeMarkersValue != null) {
						excludeMarkersValue.put(origIndex, missingRatio);
					}
				}
			}
			excludedMarkerNb = excludeMarkersOrigIndexAndKey.size();
		} else {
			excludedMarkerNb = 0;
		}

		final int excludedSampleNb;
		if (excludeSamplesOrigIndexAndKey != null) {
			// EXCLUDE SAMPLE BY MISSING RATIO
			Iterator<Double> missingRatioIt = qaSamplesOperationDataSet.getMissingRatios(-1, -1).iterator();
			if (missingRatioIt != null) {
				for (Map.Entry<Integer, SampleKey> qaSampleOrigIndexKey : qaSamplesOperationDataSet.getSamplesKeysSource().getIndicesMap().entrySet()) {
					SampleKey key = qaSampleOrigIndexKey.getValue();
					Double missingRatio = missingRatioIt.next();
					Integer origIndex = qaSampleOrigIndexKey.getKey();
					if (missingRatio > getParams().getSampleMissingRatio()) {
						excludeSamplesOrigIndexAndKey.put(origIndex, key);
						if (excludeSampleValue != null) {
							excludeSampleValue.put(origIndex, missingRatio);
						}
					}
				}
			}

			// EXCLUDE SAMPLE BY HETEROZYGOSITY RATIO
			Iterator<Double> hetzyRatioIt = qaSamplesOperationDataSet.getHetzyRatios(-1, -1).iterator();
			if (hetzyRatioIt != null) {
				for (Map.Entry<Integer, SampleKey> qaSampleOrigIndexKey : qaSamplesOperationDataSet.getSamplesKeysSource().getIndicesMap().entrySet()) {
					SampleKey key = qaSampleOrigIndexKey.getValue();
					Double hetzyRatio = hetzyRatioIt.next();
					Integer origIndex = qaSampleOrigIndexKey.getKey();
					if (hetzyRatio > getParams().getSampleHetzygRatio()) {
						excludeSamplesOrigIndexAndKey.put(origIndex, key);
						if (excludeSampleValue != null) {
							excludeSampleValue.put(origIndex, hetzyRatio);
						}
					}
				}
			}
			excludedSampleNb = excludeSamplesOrigIndexAndKey.size();
		} else {
			excludedSampleNb = 0;
		}

		final int totalSampleNb = qaSamplesOperationDataSet.getNumSamples();
		final int totalMarkerNb = qaMarkersOperationDataSet.getNumMarkers();

		return ((excludedSampleNb < totalSampleNb)
				&& (excludedMarkerNb < totalMarkerNb));
	}

	/**
	 * This gathers all possible raw statistics about the alleles and genotypes
	 * of a single marker.
	 * With allele statistics, we throw away the info of whether an allele
	 * is from the father or the mother.
	 * With genotype statistics, we look at the two allele as an ordered pair
	 * (fatherAllele_motherAllele).
	 *
	 * @param rawMarkerCensusStatistics
	 * @param chromosome
	 * @param samplesSexes
	 * @param samplesAffections
	 * @param markerGenotypes
	 * @throws IOException
	 */
	public static void gatherRawMarkerAlleleAndGTStatistics(
			final RawMarkerCensusStatistics rawMarkerCensusStatistics,
			final String chromosome,
			final List<Sex> samplesSexes,
			final List<Affection> samplesAffections,
			final GenotypesList markerGenotypes)
			throws IOException
	{
		rawMarkerCensusStatistics.clear();

		final int[] alleleValueToOrdinalLookupTable = rawMarkerCensusStatistics.getAlleleValueToOrdinalLookupTable();

		final float[] alleleOrdinalCounts = rawMarkerCensusStatistics.getAlleleOrdinalCounts();
//		final float[][] gtOrdinalCounts = rawMarkerCensusStatistics.getGtOrdinalCounts();
		final float[][] allSamplesGtOrdinalCounts = rawMarkerCensusStatistics.getGtOrdinalCounts();
		final float[][] caseSamplesGtOrdinalCounts = rawMarkerCensusStatistics.getCaseGtOrdinalCounts();
		final float[][] ctrlSamplesGtOrdinalCounts = rawMarkerCensusStatistics.getControlGtOrdinalCounts();
		final float[][] hwSamplesGtOrdinalCounts = rawMarkerCensusStatistics.getHardyWeinbergGtOrdinalCounts();

		Iterator<byte[]> markerGenotypesIt = markerGenotypes.iterator();
		Iterator<Affection> samplesAffectionIt = samplesAffections.iterator();
		for (Sex sex : samplesSexes) {
			Affection affection = samplesAffectionIt.next();
			byte[] genotype = markerGenotypesIt.next();

			final float counter;
			//<editor-fold defaultstate="expanded" desc="THE DECIDER">
			final CensusDecision decision = CensusDecision.getDecisionByChrAndSex(chromosome, sex);

//			if (decision == CensusDecision.CountMalesNonAutosomally) {
//				counter = 0.5f;
//			} else {
				counter = 1.0f;
//			}
			//</editor-fold>

			final int allele1Ordinal = alleleValueToOrdinalLookupTable[genotype[0]];
			final int allele2Ordinal = alleleValueToOrdinalLookupTable[genotype[1]];

			alleleOrdinalCounts[allele1Ordinal] += counter;
			alleleOrdinalCounts[allele2Ordinal] += counter;

			allSamplesGtOrdinalCounts[allele1Ordinal][allele2Ordinal] += counter;

			if (affection == Affection.AFFECTED) {
				caseSamplesGtOrdinalCounts[allele1Ordinal][allele2Ordinal] += counter;
			} else if (affection == Affection.UNAFFECTED) {
				ctrlSamplesGtOrdinalCounts[allele1Ordinal][allele2Ordinal] += counter;

				// HARDY WEINBERG COUNTER
				if (decision == CensusDecision.CountAutosomally) {
					hwSamplesGtOrdinalCounts[allele1Ordinal][allele2Ordinal] += counter;
				}
			}
		}
		final int missingCount = Math.round(allSamplesGtOrdinalCounts[AlleleByte._0_ORDINAL][AlleleByte._0_ORDINAL]);
		rawMarkerCensusStatistics.setMissingCount(missingCount);
	}

	private static Map<SampleKey, SampleInfo> readSampleInfosFromPhenoFile(StudyKey studyKey, File phenoFile) throws IOException {

		Map<SampleKey, SampleInfo> samplesInfos = new LinkedHashMap<SampleKey, SampleInfo>();

		FileReader phenotypeFR = new FileReader(phenoFile); // Pheno file has SampleInfo format!
		BufferedReader phenotypeBR = new BufferedReader(phenotypeFR);

		/*String header = */phenotypeBR.readLine(); // ignore header block
		String l;
		while ((l = phenotypeBR.readLine()) != null) {
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			SampleInfo info = new SampleInfo(
					studyKey,
					cVals[GWASpi.sampleId],
					cVals[GWASpi.familyId],
					Sex.parse(cVals[GWASpi.sex]),
					Affection.parse(cVals[GWASpi.affection]));
			samplesInfos.put(info.getKey(), info);
		}
		phenotypeBR.close();

		return samplesInfos;
	}

	private static void fetchSampleInfo(
			final StudyKey studyKey,
			final DataSetSource dataSetSource,
			final List<Integer> toKeepSampleOrigIndices,
			final File phenoFile,
			List<Sex> samplesSex,
			List<Affection> samplesAffection)
			throws IOException
	{
		if (phenoFile == null) {
			SamplesInfosSource samplesInfosSource = dataSetSource.getSamplesInfosSource();
			Iterator<Integer> allSampleOrigIndicesIt = dataSetSource.getSamplesKeysSource().getIndices().iterator();
			Iterator<Sex> allSexesIt = samplesInfosSource.getSexes().iterator();
			Iterator<Affection> allAffectionsIt = samplesInfosSource.getAffections().iterator();

			Integer curOrigIndex = allSampleOrigIndicesIt.next();
			Sex curSex = allSexesIt.next();
			Affection curAffection = allAffectionsIt.next();
			toKeep: for (Integer toKeepSampleOrigIndex : toKeepSampleOrigIndices) {
				while (!curOrigIndex.equals(toKeepSampleOrigIndex)) {
					if (!allSampleOrigIndicesIt.hasNext()) {
						break toKeep;
					}
					curOrigIndex = allSampleOrigIndicesIt.next();
					curSex = allSexesIt.next();
					curAffection = allAffectionsIt.next();
				}
				samplesSex.add(curSex);
				samplesAffection.add(curAffection);
			}
		} else {
			DataSetSource sampleIndicesFilteredData = new SampleIndicesFilterDataSetSource(dataSetSource.getOriginDataSetSource(), toKeepSampleOrigIndices);
			SamplesInfosSource filteredStorageSamplesInfosSource = sampleIndicesFilteredData.getSamplesInfosSource();
			Map<SampleKey, SampleInfo> phenoFileSamplesInfos = readSampleInfosFromPhenoFile(studyKey, phenoFile);
			for (SampleInfo storageSampleInfo : filteredStorageSamplesInfosSource) {
				final SampleKey sampleKey = SampleKey.valueOf(storageSampleInfo);
				SampleInfo sampleInfoToUse = phenoFileSamplesInfos.get(sampleKey);
				if (sampleInfoToUse == null) {
					// this samples info is not in the pheno file,
					// thus we use the info from our storage
					sampleInfoToUse = storageSampleInfo;
				}
				samplesSex.add(sampleInfoToUse.getSex());
				samplesAffection.add(sampleInfoToUse.getAffection());
			}
		}
	}
}
