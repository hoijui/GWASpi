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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.threadbox.MultiOperations;

class GenotypeFrequencyHardyWeinbergScriptCommand extends AbstractScriptCommand {

	GenotypeFrequencyHardyWeinbergScriptCommand() {
		super("genotype_frequency_hardy_weinberg");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=genotype_frequency_hardy_weinberg
		1.study-id=1
		2.matrix-id=8
		3.gtfreq-name=alpha
		4.use-external-phenotype-file=true
		5.external-phenotype-file=/media/pheno_alpha
		6.discard-by-marker-missing-ratio=true
		7.discard-marker-missing-ratio-threshold=0.05
		8.discard-samples-by-missing-ratio=true
		9.discard-samples-missing-ratio-threshold=0.05
		[/script]
		*/
		//</editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		StudyKey studyKey = prepareStudy(args.get("study-id"), false);
		boolean studyExists = checkStudy(studyKey);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get("matrix-id")); // Parent Matrix Id
			MatrixKey matrixKey = new MatrixKey(studyKey, matrixId);
			String gtFrqName = args.get("gtfreq-name");
			boolean useExternalPhenoFile = Boolean.parseBoolean(args.get("use-external-phenotype-file"));
			File phenoFile = null;
			if (useExternalPhenoFile) {
				phenoFile = new File(args.get("external-phenotype-file"));
			}

			boolean markersMismatchDiscard = true;
//			boolean markersMismatchDiscard = Boolean.parseBoolean(args.get("discard-mismached-marker"));
//			boolean discardByMarkersMissingRatio = Boolean.parseBoolean(args.get("discard-by-marker-missing-ratio"));
			double markersMissingRatioThreshold = Double.parseDouble(args.get("discard-marker-missing-ratio-threshold"));
//			boolean discardBySamplesMissingRatio = Boolean.parseBoolean(args.get("discard-samples-by-missing-ratio"));
			double samplesMissingRatioThreshold = Double.parseDouble(args.get("discard-samples-missing-ratio-threshold"));
			double samplesHetzyRatioThreshold = Double.parseDouble(args.get("discard-samples-heterozygosity-ratio-threshold"));

			MarkerCensusOperationParams markerCensusOperationParams
					= new MarkerCensusOperationParams(
							new DataSetKey(matrixKey),
							gtFrqName,
							XXX,
							samplesMissingRatioThreshold,
							samplesHetzyRatioThreshold,
							XXX,
							markersMismatchDiscard,
							markersMissingRatioThreshold,
							phenoFile);

			gwasParams.setMarkerCensusOperationParams(markerCensusOperationParams);
			gwasParams.setFriendlyName(gtFrqName);
			gwasParams.setProceed(true);

			List<OPType> necessaryOPs = new ArrayList<OPType>();
			necessaryOPs.add(OPType.SAMPLE_QA);
			necessaryOPs.add(OPType.MARKER_QA);
			List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, matrixKey);

			// QA block
			if (gwasParams.isProceed() && missingOPs.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				MultiOperations.doMatrixQAs(matrixKey);
			}

			// GT freq. & HW block
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doGTFreqDoHW(
						matrixKey,
						phenoFile,
						gwasParams);
				return true;
			}
		}

		return false;
	}
}
