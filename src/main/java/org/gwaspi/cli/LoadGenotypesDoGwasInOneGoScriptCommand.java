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

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.operations.markercensus.MarkerCensusOperationParams;
import org.gwaspi.threadbox.MultiOperations;

class LoadGenotypesDoGwasInOneGoScriptCommand extends AbstractScriptCommand {

	LoadGenotypesDoGwasInOneGoScriptCommand() {
		super("load_genotypes_do_gwas_in_one_go");
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		// <editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/GWASpi/data/
		[script]
		0.command=load_genotypes_do_gwas_in_one_go
		1.study-id=1
		2.format=PLINK
		3.use-dummy-samples=true
		4.new-matrix-name=Matrix 43
		5.description=Load genotypes of batch 42, perform GWAS in one go.
		6.file1-path=/GWASpi/input/Plink/mi_input.map
		7.file2-path=/GWASpi/input/Plink/mi_input.ped
		8.sample-info-path=no info file
		9.discard-marker-by-missing-ratio=false
		10.discard-marker-missing-ratio-threshold=0
		11.calculate-discard-threshold-for-HW=false
		12.discard-marker-with-provided-threshold=true
		13.discard-marker-HW-treshold=0.0000005
		14.discard-samples-by-missing-ratio=false
		15.discard-samples-missing-ratio-threshold=0
		16.discard-samples-by-heterozygosity-ratio=false
		17.discard-samples-heterozygosity-ratio-threshold=0.5
		18.perform-Allelic-Tests=true
		19.perform-Genotypic-Tests=true
		20.perform-Trend-Tests=true
		[/script]
		*/
		// </editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		StudyKey studyKey = prepareStudy(args.get("study-id"), true);
		boolean studyExists = checkStudy(studyKey);

		if (studyExists) {
			ImportFormat format = ImportFormat.compareTo(args.get("format"));
			String newMatrixName = args.get("new-matrix-name");
			String description = args.get("description");

			boolean markersMismatchDiscard = true;
//			boolean markersMismatchDiscard = Boolean.parseBoolean(args.get("discard-mismached-marker"));
//			boolean discardByMarkersMissingRatio = Boolean.parseBoolean(args.get("discard-by-marker-missing-ratio"));
			double markersMissingRatioThreshold = Double.parseDouble(args.get("discard-marker-missing-ratio-threshold"));
//			boolean discardBySamplesMissingRatio = Boolean.parseBoolean(args.get("discard-samples-by-missing-ratio"));
			double samplesMissingRatioThreshold = Double.parseDouble(args.get("discard-samples-missing-ratio-threshold"));
			double samplesHetzyRatioThreshold = Double.parseDouble(args.get("discard-samples-heterozygosity-ratio-threshold"));

			MarkerCensusOperationParams markerCensusOperationParams
					= new MarkerCensusOperationParams(
							XXX,
							null,
							XXX,
							samplesMissingRatioThreshold,
							samplesHetzyRatioThreshold,
							XXX,
							markersMismatchDiscard,
							markersMissingRatioThreshold,
							null);

			gwasParams.setMarkerCensusOperationParams(markerCensusOperationParams);

			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get("calculate-discard-threshold-for-HW")));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get("discard-marker-with-provided-threshold")));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get("discard-marker-HW-treshold")));
			gwasParams.setPerformAllelicTests(Boolean.parseBoolean(args.get("perform-Allelic-Tests")));
			gwasParams.setPerformGenotypicTests(Boolean.parseBoolean(args.get("perform-Genotypic-Tests")));
			gwasParams.setPerformTrendTests(Boolean.parseBoolean(args.get("perform-Trend-Tests")));
			gwasParams.setFriendlyName(newMatrixName);
			gwasParams.setProceed(true);

			GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
					args.get("file1-path"), // File 1
					args.get("sample-info-path"), // Sample Info file
					args.get("file2-path"), // File 2
					studyKey, // StudyKey
					format, // Format
					newMatrixName, // New Matrix name
					description, // Description
					gwasParams.getChromosome(),
					gwasParams.getStrandType(),
					gwasParams.getGtCode() // Gt code (deprecated)
					);
			MultiOperations.loadMatrixDoGWASifOK(
					loadDescription, // Format
					Boolean.parseBoolean(args.get("use-dummy-samples")), // Dummy samples
					true, // Do GWAS
					gwasParams); // gwasParams (dummy)

			return true;
		}

		return false;
	}
}
