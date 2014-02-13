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
import java.util.List;
import java.util.Set;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleInfoList;
import org.gwaspi.netCDF.loader.InMemorySamplesReceiver;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.reports.OutputTest;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_GWAS extends CommonRunnable {

	private final MatrixKey matrixKey;
	private final GWASinOneGOParams gwasParams;

	public Threaded_GWAS(
			MatrixKey matrixKey,
			GWASinOneGOParams gwasParams)
	{
		super("GWAS", "GWAS", "GWAS on Matrix ID: " + matrixKey.getMatrixId(), "GWAS");

		this.matrixKey = matrixKey;
		this.gwasParams = gwasParams;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GWAS.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey);
		OperationKey sampleQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.SAMPLE_QA);
		OperationKey markersQAOpKey = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		checkRequired(gwasParams);

		//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
		// GENOTYPE FREQ.
		OperationKey censusOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			final MarkerCensusOperationParams markerCensusOperationParams = gwasParams.getMarkerCensusOperationParams();

			markerCensusOperationParams.setSampleQAOpKey(sampleQAOpKey);

			final File phenotypeFile = markerCensusOperationParams.getPhenotypeFile();
//			if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) {
			if (phenotypeFile != null) {
				// BY EXTERNAL PHENOTYPE FILE
				// use Sample Info file affection state
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath());

				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					getLog().info("Updating Sample Info in DB");
					InMemorySamplesReceiver inMemorySamplesReceiver = new InMemorySamplesReceiver();
					SamplesParserManager.scanSampleInfo(
							matrixKey.getStudyKey(),
							ImportFormat.GWASpi,
							phenotypeFile.getPath(),
							inMemorySamplesReceiver);
					Collection<SampleInfo> sampleInfos = inMemorySamplesReceiver.getDataSet().getSampleInfos();
					SampleInfoList.insertSampleInfos(sampleInfos);

					String censusName = gwasParams.getFriendlyName() + " using " + phenotypeFile.getName();
					censusOpKey = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(
							matrixKey,
							sampleQAOpKey,
							markersQAOpKey,
							gwasParams.getDiscardMarkerMisRatVal(),
							gwasParams.isDiscardGTMismatches(),
							gwasParams.getDiscardSampleMisRatVal(),
							gwasParams.getDiscardSampleHetzyRatVal(),
							censusName,
							phenotypeFile);

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			} else { // BY DB AFFECTION
				// use Sample Info file affection state
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(matrixKey);
				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					String censusName = gwasParams.getFriendlyName() + " using " + cNetCDF.Defaults.DEFAULT_AFFECTION;
					censusOpKey = OperationManager.censusCleanMatrixMarkers(
							matrixKey,
							sampleQAOpKey,
							markersQAOpKey,
							gwasParams.getDiscardMarkerMisRatVal(),
							gwasParams.isDiscardGTMismatches(),
							gwasParams.getDiscardSampleMisRatVal(),
							gwasParams.getDiscardSampleHetzyRatVal(),
							censusName);

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			}

			if (censusOpKey != null) {
				censusOpKey = OperationKey.valueOf(OperationsList.getOperation(censusOpKey));
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(censusOpKey);
			}
		}

		OperationKey hwOpKey = checkPerformHW(thisSwi, censusOpKey);
		//</editor-fold>

		performGWAS(gwasParams, matrixKey, thisSwi, markersQAOpKey, censusOpKey, hwOpKey);
	}

	static OperationKey checkPerformHW(SwingWorkerItem thisSwi, OperationKey censusOpKey) throws Exception {

		// HW ON GENOTYPE FREQ.
		OperationKey hwOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& (censusOpKey != null))
		{
			hwOpKey = OperationManager.performHardyWeinberg(censusOpKey, cNetCDF.Defaults.DEFAULT_AFFECTION);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpKey, hwOpKey);
		}

		return hwOpKey;
	}

	static void checkRequired(GWASinOneGOParams gwasParams) {

		// CHECK IF GWAS IS REQUIRED AND IF AFFECTIONS IS AVAILABLE
		if (!gwasParams.isDiscardMarkerByMisRat()) {
			gwasParams.setDiscardMarkerMisRatVal(1);
		}
		if (!gwasParams.isDiscardMarkerByHetzyRat()) {
			gwasParams.setDiscardMarkerHetzyRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByMisRat()) {
			gwasParams.setDiscardSampleMisRatVal(1);
		}
		if (!gwasParams.isDiscardSampleByHetzyRat()) {
			gwasParams.setDiscardSampleHetzyRatVal(1);
		}
	}

	static void performGWAS(GWASinOneGOParams gwasParams, MatrixKey matrixKey, SwingWorkerItem thisSwi, OperationKey markersQAOpKey, OperationKey censusOpKey, OperationKey hwOpKey) throws Exception {
		// ASSOCIATION TEST (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if (gwasParams.isPerformAssociationTests()
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpKey != null
				&& hwOpKey != null)
		{
			final boolean allelic = gwasParams.isPerformAllelicTests();
			final OPType testType = allelic ? OPType.ALLELICTEST : OPType.GENOTYPICTEST;

			OperationMetadata markerQAMetadata = OperationsList.getOperation(markersQAOpKey);

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
			}

			OperationKey assocOpKey = OperationManager.performCleanTests(
					censusOpKey,
					hwOpKey,
					gwasParams.getDiscardMarkerHWTreshold(),
					testType);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpKey, assocOpKey);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (assocOpKey != null) {
				new OutputTest(assocOpKey, testType).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(assocOpKey);
			}
		}

		// TREND TESTS (needs newMatrixId, censusOpId, pickedMarkerSet, pickedSampleSet)
		if (gwasParams.isPerformTrendTests()
				&& thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& censusOpKey != null
				&& hwOpKey != null)
		{
			OperationMetadata markerQAMetadata = OperationsList.getOperation(markersQAOpKey);

			if (gwasParams.isDiscardMarkerHWCalc()) {
				gwasParams.setDiscardMarkerHWTreshold(0.05 / markerQAMetadata.getNumMarkers());
			}

			OperationKey trendOpKey = OperationManager.performCleanTests(
					censusOpKey,
					hwOpKey,
					gwasParams.getDiscardMarkerHWTreshold(),
					OPType.TRENDTEST);
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpKey, trendOpKey);

			// Make Reports (needs newMatrixId, QAopId, AssocOpId)
			if (trendOpKey != null) {
				new OutputTest(trendOpKey, OPType.TRENDTEST).writeReportsForTestData();
				GWASpiExplorerNodes.insertReportsUnderOperationNode(trendOpKey);
			}
		}
	}
}
