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

package org.gwaspi.cli;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.GTFreqAndHWCombinedOperation;

class GenotypeFrequencyHardyWeinbergScriptCommand extends AbstractScriptCommand {

	GenotypeFrequencyHardyWeinbergScriptCommand() {
		super("genotype_frequency_hardy_weinberg");
	}

	@Override
	public void execute(final Map<String, String> args) throws ScriptExecutionException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=genotype_frequency_hardy_weinberg
		1.study-id=1
		2.matrix-id=8
		3.gtfreq-name=alpha # [optional] [deprecated] use result-operation-name instead
		4.use-external-phenotype-file=true
		5.external-phenotype-file=/media/pheno_alpha
		6.discard-by-marker-missing-ratio=true
		7.discard-marker-missing-ratio-threshold=0.05
		8.discard-samples-by-missing-ratio=true
		9.discard-samples-missing-ratio-threshold=0.05
		10.result-gtfreq-operation-name=MyGtFreqOp # [optional]
		11.result-hw-operation-name=MyHWOp # [optional]
		[/script]
		*/
		//</editor-fold>

		try {
			GWASinOneGOParams gwasParams = new GWASinOneGOParams();

			// checking study
			final StudyKey studyKey = fetchStudyKey(args);
			checkStudyForScript(studyKey);

			final MatrixKey matrixKey = fetchMatrixKey(args, studyKey); // parent Matrix

			final String gtFrqNameDeprecated = args.get("gtfreq-name");
			String gtFrqName = args.get("result-gtfreq-operation-name");
			if (gtFrqName == null) {
				gtFrqName = gtFrqNameDeprecated;
			}
			String hwName = args.get("result-hw-operation-name");
			if ((hwName == null) && (gtFrqName != null)) {
				hwName = "H&W on " + gtFrqName;
			}

			boolean useExternalPhenoFile = Boolean.parseBoolean(args.get("use-external-phenotype-file"));
			File phenoFile = null;
			if (useExternalPhenoFile) {
				phenoFile = new File(args.get("external-phenotype-file"));
			}

			final boolean markersMismatchDiscard = fetchBoolean(args,
					"discard-mismached-marker",
					MarkerCensusOperationParams.DEFAULT_DISCARD_MISMATCHES);
//			final boolean discardByMarkersMissingRatio = fetchBoolean(args,
//					"discard-by-marker-missing-ratio",
//					MarkerCensusOperationParams.);
			final double markersMissingRatioThreshold = fetchDouble(args,
					"discard-marker-missing-ratio-threshold",
					MarkerCensusOperationParams.DEFAULT_MARKER_MISSING_RATIO);
//			final boolean discardBySamplesMissingRatio = fetchDouble(args,
//					"discard-samples-by-missing-ratio",
//					MarkerCensusOperationParams.);
			final double samplesMissingRatioThreshold = fetchDouble(args,
					"discard-samples-missing-ratio-threshold",
					MarkerCensusOperationParams.DEFAULT_SAMPLE_MISSING_RATIO);
			final double samplesHetzyRatioThreshold = fetchDouble(args,
					"discard-samples-heterozygosity-ratio-threshold",
					MarkerCensusOperationParams.DISABLE_SAMPLE_HETZY_RATIO);

			MarkerCensusOperationParams markerCensusOperationParams
					= new MarkerCensusOperationParams(
							new DataSetKey(matrixKey),
							gtFrqName,
							null, // qaSamplesOp
							samplesMissingRatioThreshold,
							samplesHetzyRatioThreshold,
							null, // qaMarkersOp
							markersMismatchDiscard,
							markersMissingRatioThreshold,
							phenoFile);

			gwasParams.setMarkerCensusOperationParams(markerCensusOperationParams);
			gwasParams.setHardyWeinbergOperationName(hwName);
			gwasParams.setProceed(true);

			TestScriptCommand.ensureMatrixQAs(matrixKey, gwasParams);

			// GT freq. & HW block
			if (gwasParams.isProceed()) {
				final CommonRunnable gtFreqHwTask = new GTFreqAndHWCombinedOperation(gwasParams);
				CommonRunnable.doRunNowInThread(gtFreqHwTask);
			} else {
				System.err.println("Not proceeding after trying to ensure QA operations");
			}
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
	}
}
