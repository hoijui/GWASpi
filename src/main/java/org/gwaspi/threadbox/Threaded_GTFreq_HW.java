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
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.samples.SamplesParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_GTFreq_HW extends CommonRunnable {

	private final MatrixKey matrixKey;
	private final File phenotypeFile;
	private final GWASinOneGOParams gwasParams;

	public Threaded_GTFreq_HW(
			MatrixKey matrixKey,
			File phenotypeFile,
			GWASinOneGOParams gwasParams)
	{
		super(
				"GT Freq. & HW",
				"Genotype Frequency count & Hardy-Weinberg test",
				"Genotypes Freq. & HW on Matrix ID: " + matrixKey.getMatrixId(),
				"Genotype Frequency count & Hardy-Weinberg test");

		this.matrixKey = matrixKey;
		this.phenotypeFile = phenotypeFile;
		this.gwasParams = gwasParams;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_GTFreq_HW.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		List<OperationMetadata> operations = OperationsList.getOperationsList(matrixKey.getMatrixId());
		int sampleQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.SAMPLE_QA);
		int markersQAOpId = OperationsList.getIdOfLastOperationTypeOccurance(operations, OPType.MARKER_QA);

		//<editor-fold defaultstate="expanded" desc="GT FREQ. & HW PROCESS">
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

		// GT FREQ. BY PHENOFILE OR DB AFFECTION
		int censusOpId = Integer.MIN_VALUE;
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)) {
			if (phenotypeFile != null && phenotypeFile.exists() && phenotypeFile.isFile()) { //BY EXTERNAL PHENOTYPE FILE

				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.scanSampleInfoAffectionStates(phenotypeFile.getPath()); //use Sample Info file affection state

				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					getLog().info("Updating Sample Info in DB");
					Collection<SampleInfo> sampleInfos = SamplesParserManager.scanSampleInfo(
							matrixKey.getStudyKey(),
							ImportFormat.GWASpi,
							phenotypeFile.getPath());
					SampleInfoList.insertSampleInfos(sampleInfos);

					censusOpId = OperationManager.censusCleanMatrixMarkersByPhenotypeFile(
							matrixKey.getMatrixId(),
							sampleQAOpId,
							markersQAOpId,
							gwasParams.getDiscardMarkerMisRatVal(),
							gwasParams.isDiscardGTMismatches(),
							gwasParams.getDiscardSampleMisRatVal(),
							gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(phenotypeFile.getName()).toString(),
							phenotypeFile);

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
					//MultiOperations.updateTree();
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			} else { // BY DB AFFECTION
				Set<SampleInfo.Affection> affectionStates = SamplesParserManager.getDBAffectionStates(matrixKey.getMatrixId()); // use Sample Info file affection state
				if (affectionStates.contains(SampleInfo.Affection.UNAFFECTED)
						&& affectionStates.contains(SampleInfo.Affection.AFFECTED))
				{
					censusOpId = OperationManager.censusCleanMatrixMarkers(
							matrixKey.getMatrixId(),
							sampleQAOpId,
							markersQAOpId,
							gwasParams.getDiscardMarkerMisRatVal(),
							gwasParams.isDiscardGTMismatches(),
							gwasParams.getDiscardSampleMisRatVal(),
							gwasParams.getDiscardSampleHetzyRatVal(),
							new StringBuilder().append(gwasParams.getFriendlyName()).append(" using ").append(cNetCDF.Defaults.DEFAULT_AFFECTION).toString());

					org.gwaspi.global.Utils.sysoutCompleted("Genotype Frequency Count");
					//MultiOperations.updateTree();
				} else {
					getLog().warn(Text.Operation.warnAffectionMissing);
				}
			}
			OperationKey censusOpKey = OperationKey.valueOf(OperationsList.getById(censusOpId));
			GWASpiExplorerNodes.insertOperationUnderMatrixNode(censusOpKey);
		}

		// HW ON GENOTYPE FREQ.
		if (thisSwi.getQueueState().equals(QueueState.PROCESSING)
				&& (censusOpId != Integer.MIN_VALUE))
		{
			int hwOpId = OperationManager.performHardyWeinberg(censusOpId, cNetCDF.Defaults.DEFAULT_AFFECTION);
			OperationKey hwOpKey = OperationKey.valueOf(OperationsList.getById(hwOpId));
			GWASpiExplorerNodes.insertSubOperationUnderOperationNode(censusOpId, hwOpKey);
		}
		//</editor-fold>
	}
}
