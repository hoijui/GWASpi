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

import java.util.Set;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.GWASpiExplorerNodes;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.loader.InMemorySamplesReceiver;
import org.gwaspi.netCDF.loader.LoadManager;
import org.gwaspi.netCDF.loader.LoadingNetCDFDataSetDestination;
import org.gwaspi.netCDF.loader.SampleInfoCollectorSwitch;
import org.gwaspi.netCDF.loader.ZipTwoWaySaverSamplesReceiver;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OP_QAMarkers;
import org.gwaspi.netCDF.operations.OP_QASamples;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.reports.OutputQAMarkers;
import org.gwaspi.reports.OutputQASamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_Loader_GWASifOK extends CommonRunnable {

	private final boolean dummySamples;
	private final boolean performGwas;
	private final GenotypesLoadDescription loadDescription;
	private final GWASinOneGOParams gwasParams;

	public Threaded_Loader_GWASifOK(
			GenotypesLoadDescription loadDescription,
			boolean dummySamples,
			boolean performGwas,
			GWASinOneGOParams gwasParams)
	{
		super(
				"Genotypes Loader & GWAS if OK",
				"Loading Genotypes & Performing GWAS",
				"Genotypes Loader & GWAS if OK: " + loadDescription.getFriendlyName(),
				"Loading Genotypes & Performing GWAS");

		this.loadDescription = loadDescription;
		this.dummySamples = dummySamples;
		this.performGwas = performGwas;
		this.gwasParams = gwasParams;
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_Loader_GWASifOK.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		LoadingNetCDFDataSetDestination samplesReceiver = new LoadingNetCDFDataSetDestination(loadDescription); // HACK FIXME
//		ZipTwoWaySaverSamplesReceiver samplesReceiver = new ZipTwoWaySaverSamplesReceiver(loadDescription); // HACK FIXME
//		InMemorySamplesReceiver samplesReceiver = new InMemorySamplesReceiver(); // HACK FIXME
		SampleInfoCollectorSwitch.collectSampleInfo(
				loadDescription.getStudyKey(),
				loadDescription.getFormat(),
				dummySamples,
				loadDescription.getSampleFilePath(),
				loadDescription.getGtDirPath(),
				loadDescription.getAnnotationFilePath(),
				samplesReceiver);
		Set<SampleInfo.Affection> affectionStates = SampleInfoCollectorSwitch.collectAffectionStates(samplesReceiver.getDataSet().getSampleInfos());

		//<editor-fold defaultstate="expanded" desc="LOAD PROCESS">
		MatrixKey matrixKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			LoadManager.dispatchLoadByFormat(
					loadDescription,
					samplesReceiver);
			matrixKey = samplesReceiver.getResultMatrixKey();
			samplesReceiver.done();
			MultiOperations.printCompleted("Loading Genotypes");
			GWASpiExplorerNodes.insertMatrixNode(matrixKey);
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="QA PROCESS">
		OperationKey samplesQAOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			samplesQAOpKey = new OperationKey(matrixKey, new OP_QASamples(matrixKey).processMatrix());
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(samplesQAOpKey);
			OutputQASamples.writeReportsForQASamplesData(samplesQAOpKey, true);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(samplesQAOpKey);
		}

		OperationKey markersQAOpKey = null;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			markersQAOpKey = new OperationKey(matrixKey, new OP_QAMarkers(matrixKey).processMatrix());
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(markersQAOpKey);
			OutputQAMarkers.writeReportsForQAMarkersData(markersQAOpKey);
			GWASpiExplorerNodes.insertReportsUnderOperationNode(markersQAOpKey);
			MultiOperations.printCompleted("Matrix Quality Control");
		}
		//</editor-fold>

		if (performGwas
				&& affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
				&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
		{
			Threaded_GWAS.checkRequired(gwasParams);

			//<editor-fold defaultstate="expanded" desc="PRE-GWAS PROCESS">
			// GENOTYPE FREQ.
			OperationKey censusOpKey = null;
			if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
				censusOpKey = OperationManager.censusCleanMatrixMarkers(
						matrixKey,
						samplesQAOpKey,
						markersQAOpKey,
						gwasParams.getDiscardMarkerMisRatVal(),
						gwasParams.isDiscardGTMismatches(),
						gwasParams.getDiscardSampleMisRatVal(),
						gwasParams.getDiscardSampleHetzyRatVal(),
						cNetCDF.Defaults.DEFAULT_AFFECTION);
				GWASpiExplorerNodes.insertOperationUnderMatrixNode(censusOpKey);
			}

			OperationKey hwOpKey = Threaded_GWAS.checkPerformHW(thisSwi, censusOpKey);
			//</editor-fold>

			Threaded_GWAS.performGWAS(gwasParams, matrixKey, thisSwi, markersQAOpKey, censusOpKey, hwOpKey);
		}
	}
}
