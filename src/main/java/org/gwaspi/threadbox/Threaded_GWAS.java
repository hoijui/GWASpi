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

package org.gwaspi.threadbox;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.netCDF.loader.InMemorySamplesReceiver;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.operations.hardyweinberg.HardyWeinbergOperationParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_GWAS extends CommonRunnable {

	private final GWASinOneGOParams gwasParams;

	public Threaded_GWAS(GWASinOneGOParams gwasParams) {
		super("GWAS", "GWAS", "GWAS on: " + gwasParams.getMarkerCensusOperationParams().getParent().toString(), "GWAS");

		this.gwasParams = gwasParams;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GWAS.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		OperationKey censusOpKey = checkPerformMarkerCensus(getLog(), thisSwi, gwasParams);

		final OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(gwasParams.getMarkerCensusOperationParams().getParent(), OPType.MARKER_QA).get(0));
		OperationKey hwOpKey = checkPerformHW(thisSwi, censusOpKey, markersQAOpKey);

		performGWAS(gwasParams, thisSwi, censusOpKey, hwOpKey);
	}

	static OperationKey checkPerformMarkerCensus(Logger log, SwingWorkerItem thisSwi, GWASinOneGOParams gwasParams) throws Exception {

		final MarkerCensusOperationParams markerCensusOperationParams = gwasParams.getMarkerCensusOperationParams();

		final OperationKey sampleQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(markerCensusOperationParams.getParent(), OPType.SAMPLE_QA).get(0));
		final OperationKey markersQAOpKey = OperationKey.valueOf(OperationsList.getChildrenOperationsMetadata(markerCensusOperationParams.getParent(), OPType.MARKER_QA).get(0));

		markerCensusOperationParams.setSampleQAOpKey(sampleQAOpKey);
		markerCensusOperationParams.setMarkerQAOpKey(markersQAOpKey);

		//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
		// GENOTYPE FREQ. BY PHENOFILE OR DB AFFECTION
		OperationKey censusOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			final File phenotypeFile = markerCensusOperationParams.getPhenotypeFile();
			if (phenotypeFile != null) {
				// BY EXTERNAL PHENOTYPE FILE
				// use Sample Info file affection state
				Set<Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath());

				if (affectionStates.contains(Affection.UNAFFECTED)
						&& affectionStates.contains(Affection.AFFECTED))
				{
					log.info("Updating Sample Info in DB");
					InMemorySamplesReceiver inMemorySamplesReceiver = new InMemorySamplesReceiver();
					SamplesParserManager.scanSampleInfo(
							markerCensusOperationParams.getParent().getOrigin().getStudyKey(),
							ImportFormat.GWASpi,
							phenotypeFile.getPath(),
							inMemorySamplesReceiver);
					Collection<SampleInfo> sampleInfos = inMemorySamplesReceiver.getDataSet().getSampleInfos();
					SampleInfoList.insertSampleInfos(sampleInfos);

					String censusName = gwasParams.getFriendlyName() + " using " + phenotypeFile.getName();
					markerCensusOperationParams.setName(censusName);
				} else {
					log.warn(Text.Operation.warnAffectionMissing);
					return censusOpKey;
				}
			} else {
				// BY DB AFFECTION
				// use Sample Info from the DB to extract affection state
				Set<Affection> affectionStates = SamplesParserManager.getDBAffectionStates(markerCensusOperationParams.getParent());
				if (affectionStates.contains(Affection.UNAFFECTED)
						&& affectionStates.contains(Affection.AFFECTED))
				{
					String censusName = gwasParams.getFriendlyName() + " using " + cNetCDF.Defaults.DEFAULT_AFFECTION;
					markerCensusOperationParams.setName(censusName);
				} else {
					log.warn(Text.Operation.warnAffectionMissing);
					return censusOpKey;
				}
			}

			censusOpKey = OperationManager.censusCleanMatrixMarkers(markerCensusOperationParams);
		}

		return censusOpKey;
	}

	static OperationKey checkPerformHW(SwingWorkerItem thisSwi, OperationKey censusOpKey, final OperationKey markersQAOpKey) throws Exception {

		// HW ON GENOTYPE FREQ.
		OperationKey hwOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& (censusOpKey != null))
		{
			final HardyWeinbergOperationParams params = new HardyWeinbergOperationParams(censusOpKey, cNetCDF.Defaults.DEFAULT_AFFECTION, markersQAOpKey);
			hwOpKey = OperationManager.performHardyWeinberg(params);
		}

		return hwOpKey;
	}

	static void performGWAS(GWASinOneGOParams gwasParams, SwingWorkerItem thisSwi, OperationKey censusOpKey, OperationKey hwOpKey) throws Exception {

		final OperationKey markersQAOpKey = gwasParams.getMarkerCensusOperationParams().getMarkerQAOpKey();

		// ASSOCIATION TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if (gwasParams.isPerformAssociationTests()
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpKey != null
				&& hwOpKey != null)
		{
			final boolean allelic = gwasParams.isPerformAllelicTests();
			final OPType testType = allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST;

			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpKey);

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
			}

			OperationKey assocOpKey = OperationManager.performCleanTests(
					censusOpKey,
					hwOpKey,
					gwasParams.getDiscardMarkerHWTreshold(),
					testType);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (assocOpKey != null) {
				new OutputTest(assocOpKey, testType, markersQAOpKey).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpKey);
			}
		}

		// TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if (gwasParams.isPerformTrendTests()
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpKey != null
				&& hwOpKey != null)
		{
			OperationMetadata markerQAMetadata = OperationsList.getOperationMetadata(markersQAOpKey);

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
			}

			OperationKey trendOpKey = OperationManager.performCleanTests(
					censusOpKey,
					hwOpKey,
					gwasParams.getDiscardMarkerHWTreshold(),
					OPType.TRENDTEST);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (trendOpKey != null) {
				new OutputTest(trendOpKey, OPType.TRENDTEST, markersQAOpKey).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendOpKey);
			}
		}
	}
}
