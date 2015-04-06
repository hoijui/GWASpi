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
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.combi.AllelicGenotypeEncoder;
import org.gwaspi.operations.combi.ByCombiWeightsFilterOperationParams;
import org.gwaspi.operations.combi.NominalGenotypeEncoder;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.combi.GenotypeEncoder;
import org.gwaspi.operations.combi.GenotypeEncodingParams;
import org.gwaspi.operations.combi.GenotypicGenotypeEncoder;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_Combi;

public class CombiTestScriptCommand extends AbstractScriptCommand {

	public static final Map<String, GenotypeEncoder> GENOTYPE_ENCODERS
			= new HashMap<String, GenotypeEncoder>();
	static {
		GENOTYPE_ENCODERS.put("allelic", AllelicGenotypeEncoder.SINGLETON);
		GENOTYPE_ENCODERS.put("genotypic", GenotypicGenotypeEncoder.SINGLETON);
		GENOTYPE_ENCODERS.put("nominal", NominalGenotypeEncoder.SINGLETON);
	}

	CombiTestScriptCommand() {
		super("combi_association");
	}

	@Override
	public void execute(final Map<String, String> args) throws ScriptExecutionException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=combi_association
		1.study-id=1 # or "1.study-name=studyX"
		2.matrix-id=8 # or "2.matrix-name=matrixX"; which matrix to operate on (read from).
		3.census-operation-id=10 # or "3.census-operation-name=censusOperationX"
		4.hw-operation-id=12 # or "4.hw-operation-name=hwOperationX"
		5.hw-threshold=0.005 # TODO
		6.genotype-encoding=genotypic # or "allelic", "nominal"
		7.markers-to-keep=10000 # how many markers to be left with, after the filtering with the Combi method.
		8.use-threshold-calibration=0 # whether to use resampling based threshold calibration. this feature takes a lot of computation time!
		9.phenotype-info-file=/tmp/myPhenotypeInfo.txt # [optional]
		10.result-operation-name=myCombiTextOperationX # [optional]
		[/script]
		*/
		//</editor-fold>

		try {
			// checking study
			StudyKey studyKey = fetchStudyKey(args);
			checkStudyForScript(studyKey);

			MatrixKey matrixKey = fetchMatrixKey(args, studyKey, "matrix-id", "matrix-name");

			OperationKey qaMarkersOperationKey = fetchOperationKey(args, matrixKey, "qa-markers-id", "qa-markers-name");

			final Boolean perChromosome = fetchBoolean(args, "per-chromosome", null);

			GenotypeEncoder genotypeEncoder = GENOTYPE_ENCODERS.get(args.get("genotype-encoding"));

			final Double genotypeEncodindP = fetchDouble(args, "genotype-encoding-p", null);
			final GenotypeEncodingParams genotypeEncodingParams;
			if (genotypeEncodindP == null) {
				genotypeEncodingParams = new GenotypeEncodingParams();
			} else {
				genotypeEncodingParams = new GenotypeEncodingParams(genotypeEncodindP);
			}

			final int weightsFilterWidth = fetchInteger(args, "weights-filter-width", null);

			final int markersToKeep = fetchInteger(args, "markers-to-keep", null);

			final Boolean useThresholdCalibration = fetchBoolean(args, "use-threshold-calibration", null);

			// This might return null, as it is optional,
			// which will lead to using the default name
			String resultOperationName = args.get("result-operation-name");

			final int totalMarkers = OperationsList.getOperationMetadata(qaMarkersOperationKey).getNumMarkers();

			CombiTestOperationParams paramsTest = new CombiTestOperationParams(
					qaMarkersOperationKey,
					genotypeEncoder,
					genotypeEncodingParams,
					useThresholdCalibration,
					perChromosome,
					resultOperationName);
			ByCombiWeightsFilterOperationParams paramsFilter = new ByCombiWeightsFilterOperationParams(
					totalMarkers,
					perChromosome,
					weightsFilterWidth,
					markersToKeep,
					"Filter on " + resultOperationName);

			// test block
			final CommonRunnable combiTask = new Threaded_Combi(paramsTest, paramsFilter);
			MultiOperations.queueTask(combiTask);
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
	}
}
