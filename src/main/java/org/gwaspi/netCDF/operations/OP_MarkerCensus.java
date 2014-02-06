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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.AlleleByte;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.datasource.filter.SampleIndicesFilterDataSetSource;
import org.gwaspi.global.Text;
import org.gwaspi.model.Census;
import org.gwaspi.model.CensusFull;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;
import org.gwaspi.model.SampleKey;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.AbstractNetCdfOperationDataSet;
import org.gwaspi.operations.markercensus.DefaultMarkerCensusOperationEntry;
import org.gwaspi.operations.markercensus.MarkerCensusOperationDataSet;
import org.gwaspi.operations.qamarkers.QAMarkersOperationDataSet;
import org.gwaspi.operations.qasamples.QASamplesOperationDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OP_MarkerCensus extends AbstractOperation<MarkerCensusOperationDataSet> {

	private final Logger log = LoggerFactory.getLogger(OP_MarkerCensus.class);

	private final String censusName;
	private final OperationKey sampleQAOPKey;
	private final double sampleMissingRatio;
	private final double sampleHetzygRatio;
	private final OperationKey markerQAOPKey;
	private final boolean discardMismatches;
	private final double markerMissingRatio;
	private final File phenoFile;

	public OP_MarkerCensus(
			MatrixKey parent,
			String censusName,
			OperationKey sampleQAOPKey,
			double sampleMissingRatio,
			double sampleHetzygRatio,
			OperationKey markerQAOPKey,
			boolean discardMismatches,
			double markerMissingRatio,
			File phenoFile)
	{
		super(parent);

		this.censusName = censusName;
		this.sampleQAOPKey = sampleQAOPKey;
		this.sampleMissingRatio = sampleMissingRatio;
		this.sampleHetzygRatio = sampleHetzygRatio;
		this.markerQAOPKey = markerQAOPKey;
		this.discardMismatches = discardMismatches;
		this.markerMissingRatio = markerMissingRatio;
		this.phenoFile = phenoFile;
	}

	@Override
	public OPType getType() {
		return OPType.MARKER_CENSUS_BY_AFFECTION;
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

		if (dataRemaining) {
			// THERE IS DATA LEFT TO PROCESS AFTER PICKING

			//<editor-fold defaultstate="expanded" desc="PURGE Maps">
			DataSetSource dataSetSource = getParentDataSetSource();

			Map<Integer, SampleKey> wrSampleKeys = new LinkedHashMap<Integer, SampleKey>();
			for (Map.Entry<Integer, SampleKey> origIndexKey : dataSetSource.getSamplesKeysSource().getIndicesMap().entrySet()) {
				if (!excludeSamplesOrigIndexAndKey.containsKey(origIndexKey.getKey())) {
					wrSampleKeys.put(origIndexKey.getKey(), origIndexKey.getValue());
				}
			}
			List<Integer> wrSampleOrigIndices = new ArrayList<Integer>(wrSampleKeys.keySet());
			//</editor-fold>

			MarkerCensusOperationDataSet dataSet = generateFreshOperationDataSet();

			dataSet.setNumMarkers(dataSetSource.getNumMarkers());
			dataSet.setNumChromosomes(dataSetSource.getNumChromosomes());
			dataSet.setNumSamples(wrSampleKeys.size());

			dataSet.setCensusName(censusName); // HACK
			dataSet.setPhenoFile(phenoFile); // HACK
			dataSet.setSampleMissingRatio(sampleMissingRatio);// HACK
			dataSet.setSampleHetzygRatio(sampleHetzygRatio); // HACK
			dataSet.setMarkerMissingRatio(markerMissingRatio); // HACK
			dataSet.setDiscardMismatches(discardMismatches); // HACK

			dataSet.setSamples(wrSampleKeys);

			//<editor-fold defaultstate="expanded" desc="PROCESSOR">
			List<Sex> samplesSex = new ArrayList<Sex>(wrSampleKeys.size());
			List<Affection> samplesAffection = new ArrayList<Affection>(wrSampleKeys.size());
			fetchSampleInfo(getParentMatrixKey().getStudyKey(), dataSetSource, wrSampleOrigIndices, samplesSex, samplesAffection);

			log.info("Start Census testing markers");

			Iterator<GenotypesList> markersGTsIt = dataSetSource.getMarkersGenotypesSource().iterator();
			Iterator<String> markerChromosomesIt = dataSetSource.getMarkersMetadatasSource().getChromosomes().iterator();
			for (final Map.Entry<Integer, MarkerKey> markerEntry : dataSetSource.getMarkersKeysSource().getIndicesMap().entrySet()) {
				final int markerOrigIndex = markerEntry.getKey();
				final MarkerKey markerKey = markerEntry.getValue();
				final String markerChr = markerChromosomesIt.next();

				// We use float instead of int here,
				// even though it is a counter,
				// because under certain circumstances,
				// we want to count some things only half (+ 0.5).
				Map<Byte, Float> knownAlleles = new LinkedHashMap<Byte, Float>();
				Map<Integer, Float> allSamplesGTsTable = new LinkedHashMap<Integer, Float>();
				Map<Integer, Float> caseSamplesGTsTable = new LinkedHashMap<Integer, Float>();
				Map<Integer, Float> ctrlSamplesGTsTable = new LinkedHashMap<Integer, Float>();
				Map<Integer, Float> hwSamplesGTsTable = new LinkedHashMap<Integer, Float>();
				Map<String, Integer> allSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Map<String, Integer> caseSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Map<String, Integer> ctrlSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Map<String, Integer> hwSamplesContingencyTable = new LinkedHashMap<String, Integer>();
				Integer missingCount = 0;

				// Get a sample-set full of GTs
				Iterator<byte[]> markerGTsIt = markersGTsIt.next().iterator();
				Iterator<Affection> samplesAffectionIt = samplesAffection.iterator();
				for (Sex sex : samplesSex) {
					Affection affection = samplesAffectionIt.next();

					final float counter;
					//<editor-fold defaultstate="expanded" desc="THE DECIDER">
					final CensusDecision decision = CensusDecision.getDecisionByChrAndSex(markerChr, sex);

//						if (decision == CensusDecision.CountMalesNonAutosomally) {
//							counter = 0.5f;
//						} else {
						counter = 1.0f;
//						}
					//</editor-fold>

					// SUMMING SAMPLESET GENOTYPES
					byte[] tempGT = markerGTsIt.next();
					missingCount = summingSampleSetGenotypes(
							tempGT,
							decision,
							knownAlleles,
							allSamplesGTsTable,
							affection,
							caseSamplesGTsTable,
							ctrlSamplesGTsTable,
							hwSamplesGTsTable,
							counter,
							missingCount);
				}

				byte[] alleles;
				CensusFull censusFull;
				// AFFECTION ALLELE CENSUS + MISMATCH STATE + MISSINGNESS
				if (knownAlleles.size() <= 2) {
					// Check if there are mismatches in alleles

					List<Integer> AAnumVals = new ArrayList<Integer>();
					List<Integer> AanumVals = new ArrayList<Integer>();
					List<Integer> aanumVals = new ArrayList<Integer>();

					knowYourAlleles(
							knownAlleles,
							AAnumVals,
							AanumVals,
							aanumVals);

					contingencyCensusSamples(
							allSamplesContingencyTable,
							allSamplesGTsTable,
							AAnumVals,
							AanumVals,
							aanumVals);

					contingencyCensusSamples(
							caseSamplesContingencyTable,
							caseSamplesGTsTable,
							AAnumVals,
							AanumVals,
							aanumVals);

					contingencyCensusSamples(
							ctrlSamplesContingencyTable,
							ctrlSamplesGTsTable,
							AAnumVals,
							AanumVals,
							aanumVals);

					contingencyCensusSamples(
							hwSamplesContingencyTable,
							hwSamplesGTsTable,
							AAnumVals,
							AanumVals,
							aanumVals);

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
					int obsHwAA = 0;
					int obsHwAa = 0;
					int obsHwaa = 0;
					if (allSamplesContingencyTable.containsKey("AA")) {
						obsAllAA = allSamplesContingencyTable.get("AA");
					}
					if (allSamplesContingencyTable.containsKey("Aa")) {
						obsAllAa = allSamplesContingencyTable.get("Aa");
					}
					if (allSamplesContingencyTable.containsKey("aa")) {
						obsAllaa = allSamplesContingencyTable.get("aa");
					}
					if (caseSamplesContingencyTable.containsKey("AA")) {
						obsCaseAA = caseSamplesContingencyTable.get("AA");
					}
					if (caseSamplesContingencyTable.containsKey("Aa")) {
						obsCaseAa = caseSamplesContingencyTable.get("Aa");
					}
					if (caseSamplesContingencyTable.containsKey("aa")) {
						obsCaseaa = caseSamplesContingencyTable.get("aa");
					}
					if (ctrlSamplesContingencyTable.containsKey("AA")) {
						obsCntrlAA = ctrlSamplesContingencyTable.get("AA");
					}
					if (ctrlSamplesContingencyTable.containsKey("Aa")) {
						obsCntrlAa = ctrlSamplesContingencyTable.get("Aa");
					}
					if (ctrlSamplesContingencyTable.containsKey("aa")) {
						obsCntrlaa = ctrlSamplesContingencyTable.get("aa");
					}
					if (hwSamplesContingencyTable.containsKey("AA")) {
						obsHwAA = hwSamplesContingencyTable.get("AA");
					}
					if (hwSamplesContingencyTable.containsKey("Aa")) {
						obsHwAa = hwSamplesContingencyTable.get("Aa");
					}
					if (hwSamplesContingencyTable.containsKey("aa")) {
						obsHwaa = hwSamplesContingencyTable.get("aa");
					}

					censusFull = new CensusFull(
							new Census(obsAllAA, obsAllAa, obsAllaa, missingCount), // all
							new Census(obsCaseAA, obsCaseAa, obsCaseaa, -1), // case
							new Census(obsCntrlAA, obsCntrlAa, obsCntrlaa, -1), // control
							new Census(obsHwAA, obsHwAa, obsHwaa, -1) // alternate HW samples
							);

					alleles = cNetCDF.Defaults.DEFAULT_GT;
					Iterator<Byte> knit = knownAlleles.keySet().iterator();
					if (knownAlleles.size() == 2) {
						Byte allele1 = knit.next();
						Byte allele2 = knit.next();
						alleles = new byte[] {allele1, allele2};
					}
					if (knownAlleles.size() == 1) {
						Byte allele1 = knit.next();
						alleles = new byte[] {allele1, allele1};
					}
				} else {
					// MISMATCHES FOUND
					censusFull = new CensusFull();
					alleles = "00".getBytes();
				}

				dataSet.addEntry(new DefaultMarkerCensusOperationEntry(
						markerKey, markerOrigIndex, alleles, censusFull));
			}
			//</editor-fold>

			dataSet.finnishWriting();
			resultOpId = ((AbstractNetCdfOperationDataSet) dataSet).getOperationKey().getId(); // HACK

			org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
		} else {
			// NO DATA LEFT AFTER THRESHOLD FILTER PICKING
			log.warn(Text.Operation.warnNoDataLeftAfterPicking);
		}

		return resultOpId;
	}

	private static void knowYourAlleles(
			Map<Byte, Float> knownAlleles,
			List<Integer> AAnumVals,
			List<Integer> AanumVals,
			List<Integer> aanumVals)
	{
		Iterator<Byte> itKnAll = knownAlleles.keySet().iterator();
		if (knownAlleles.size() == 1) {
			// Homozygote (AA or aa)
			byte key = itKnAll.next();
			int intAllele1 = (int) key;
			AAnumVals.add(intAllele1); // Single A
			AAnumVals.add(intAllele1 * 2); // Double AA
		}
		if (knownAlleles.size() == 2) {
			// Heterezygote (AA, Aa or aa)
			byte key = itKnAll.next();
			int countA = Math.round(knownAlleles.get(key));
			int intAllele1 = (int) key;
			key = itKnAll.next();
			int countB = Math.round(knownAlleles.get(key));
			int intAllele2 = (int) key;

			// Finding out what allele is major and minor
			if (countA >= countB) {
				AAnumVals.add(intAllele1);
				AAnumVals.add(intAllele1 * 2);

				aanumVals.add(intAllele2);
				aanumVals.add(intAllele2 * 2);

				AanumVals.add(intAllele1 + intAllele2);
			} else {
				AAnumVals.add(intAllele2);
				AAnumVals.add(intAllele2 * 2);

				aanumVals.add(intAllele1);
				aanumVals.add(intAllele1 * 2);

				AanumVals.add(intAllele1 + intAllele2);
			}
		}
	}

	private static void contingencyCensusSamples(
			Map<String, Integer> hwSamplesContingencyTable,
			Map<Integer, Float> hwSamplesGTsTable,
			List<Integer> AAnumVals,
			List<Integer> AanumVals,
			List<Integer> aanumVals)
	{
		for (Map.Entry<Integer, Float> samplesEntry : hwSamplesGTsTable.entrySet()) {
			Integer key = samplesEntry.getKey();
			Integer value = Math.round(samplesEntry.getValue());

			if (AAnumVals.contains(key)) {
				// compare to all possible character values of AA
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("AA")) {
					tempCount = hwSamplesContingencyTable.get("AA");
				}
				hwSamplesContingencyTable.put("AA", tempCount + value);
			}
			if (AanumVals.contains(key)) {
				// compare to all possible character values of Aa
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("Aa")) {
					tempCount = hwSamplesContingencyTable.get("Aa");
				}
				hwSamplesContingencyTable.put("Aa", tempCount + value);
			}
			if (aanumVals.contains(key)) {
				// compare to all possible character values of aa
				// HW CENSUS
				int tempCount = 0;
				if (hwSamplesContingencyTable.containsKey("aa")) {
					tempCount = hwSamplesContingencyTable.get("aa");
				}
				hwSamplesContingencyTable.put("aa", tempCount + value);
			}
		}
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
		QAMarkersOperationDataSet qaMarkersOperationDataSet = (QAMarkersOperationDataSet) OperationFactory.generateOperationDataSet(markerQAOPKey);
		QASamplesOperationDataSet qaSamplesOperationDataSet = (QASamplesOperationDataSet) OperationFactory.generateOperationDataSet(sampleQAOPKey);

		final int excludedMarkerNb;
		if (excludeMarkersOrigIndexAndKey != null) {
			// EXCLUDE MARKER BY MISMATCH STATE
			if (discardMismatches) {
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
				if (missingRatio > markerMissingRatio) {
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
					if (missingRatio > sampleMissingRatio) {
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
					if (hetzyRatio > sampleHetzygRatio) {
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

	static int summingSampleSetGenotypes(
			byte[] tempGT,
			CensusDecision decision,
			Map<Byte, Float> knownAlleles,
			Map<Integer, Float> allSamplesGTsTable,
			Affection affection,
			Map<Integer, Float> caseSamplesGTsTable,
			Map<Integer, Float> ctrlSamplesGTsTable,
			Map<Integer, Float> hwSamplesGTsTable,
			float counter,
			int missingCount)
	{
		int newMissingCount = missingCount;

		final byte allele1 = tempGT[0];
		final byte allele2 = tempGT[1];

		// Gather alleles different from 0 into a list of known alleles
		// and count the number of appearences
		// XXX This following stuff could be made faster by using an array
		if (allele1 != AlleleByte._0_VALUE) {
			float tempCount = 0;
			if (knownAlleles.containsKey(allele1)) {
				tempCount = knownAlleles.get(allele1);
			}
			knownAlleles.put(allele1, tempCount + counter);
		}
		if (allele2 != AlleleByte._0_VALUE) {
			float tempCount = 0;
			if (knownAlleles.containsKey(allele2)) {
				tempCount = knownAlleles.get(allele2);
			}
			knownAlleles.put(allele2, tempCount + counter);
		}
		if ((allele1 == AlleleByte._0_VALUE) && (allele2 == AlleleByte._0_VALUE)) {
			newMissingCount++;
		}

		final int intAllele1 = allele1;
		final int intAllele2 = allele2;
		Integer intAlleleSum = intAllele1 + intAllele2; // 2 alleles per GT

		// CASE/CONTROL CENSUS
		float tempCount = 0;
		if (allSamplesGTsTable.containsKey(intAlleleSum)) {
			tempCount = allSamplesGTsTable.get(intAlleleSum);
		}
		allSamplesGTsTable.put(intAlleleSum, tempCount + counter);

		if (affection == Affection.AFFECTED) {
			tempCount = 0;
			if (caseSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = caseSamplesGTsTable.get(intAlleleSum);
			}
			caseSamplesGTsTable.put(intAlleleSum, tempCount + counter);
		} else if (affection == Affection.UNAFFECTED) {
			tempCount = 0;
			if (ctrlSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = ctrlSamplesGTsTable.get(intAlleleSum);
			}
			ctrlSamplesGTsTable.put(intAlleleSum, tempCount + counter);

			// HARDY WEINBERG COUNTER
			if (hwSamplesGTsTable.containsKey(intAlleleSum)) {
				tempCount = hwSamplesGTsTable.get(intAlleleSum);
			}
			if (decision == CensusDecision.CountMalesNonAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount);
			}
			if (decision == CensusDecision.CountFemalesNonAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount);
			}
			if (decision == CensusDecision.CountAutosomally) {
				hwSamplesGTsTable.put(intAlleleSum, tempCount + counter);
			}
		}

		return newMissingCount;
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

	private void fetchSampleInfo(
			StudyKey studyKey,
			DataSetSource dataSetSource,
			List<Integer> toKeepSampleOrigIndices,
			List<Sex> samplesSex,
			List<Affection> samplesAffection)
			throws IOException
	{
		if (phenoFile == null) {
			SamplesInfosSource samplesInfosSource = dataSetSource.getSamplesInfosSource();
			Iterator<Integer> allSampleOrigIndicesIt = dataSetSource.getSamplesKeysSource().getIndices().iterator();
			Iterator<Sex> allSexesIt = samplesInfosSource.getSexes().iterator();
			Iterator<Affection> allAffectionsIt = samplesInfosSource.getAffections().iterator();
			for (Integer toKeepSampleOrigIndex : toKeepSampleOrigIndices) {
				Integer curOrigIndex = allSampleOrigIndicesIt.next();
				Sex curSex = allSexesIt.next();
				Affection curAffection = allAffectionsIt.next();
				if (curOrigIndex == toKeepSampleOrigIndex) {
					samplesSex.add(curSex);
					samplesAffection.add(curAffection);
				}
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
