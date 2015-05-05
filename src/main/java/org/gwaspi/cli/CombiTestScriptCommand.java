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
import org.gwaspi.operations.combi.SolverLibrary;
import org.gwaspi.operations.combi.SolverParams;
import org.gwaspi.threadbox.CommonRunnable;
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
		3.qa-markers-id=3 # or "3.qa-markers-name=qaMarkersOperationX"
		4.per-chromosome=true
		5.genotype-encoding=genotypic # or "allelic", "nominal"
		6.genotype-encoding-p=6 # [optional] for example 1, 2 or 6
		7.markers-to-keep=10000 # how many markers to be left with, after the filtering with the Combi method.
		8.use-threshold-calibration=0 # whether to use resampling based threshold calibration. this feature takes a lot of computation time!
		9.phenotype-info-file=/tmp/myPhenotypeInfo.txt # [optional]
		10.result-operation-name=myCombiTestOperationX # [optional]
		11.result-filter-operation-name=myCombiTestFilterOperationX # [optional]
		[/script]
		*/
		//</editor-fold>

		try {
			// checking study
			StudyKey studyKey = fetchStudyKey(args);
			checkStudyForScript(studyKey);

			final MatrixKey matrixKey = fetchMatrixKey(args, studyKey); // parent Matrix

			final OperationKey qaMarkersOperationKey = fetchOperationKey(args, matrixKey, "qa-markers");
			final int totalMarkers
					= OperationsList.getOperationMetadata(qaMarkersOperationKey).getNumMarkers();

			final Boolean perChromosome = fetchBoolean(args, "per-chromosome", null);

			String genotypeEncoderStr = args.get("genotype-encoding");
			if (genotypeEncoderStr != null) {
				genotypeEncoderStr = genotypeEncoderStr.toLowerCase();
			}
			final GenotypeEncoder genotypeEncoder = GENOTYPE_ENCODERS.get(genotypeEncoderStr);
			if ((genotypeEncoderStr != null) && (genotypeEncoder == null)) {
				throw new ScriptExecutionException(
						"Unsupported genotype encoding specified in script: \""
								+ genotypeEncoderStr + "\"");
			}

			final Double genotypeEncodindP = fetchDouble(args, "genotype-encoding-p", null);
			final GenotypeEncodingParams genotypeEncodingParams;
			if (genotypeEncodindP == null) {
				genotypeEncodingParams = new GenotypeEncodingParams();
			} else {
				genotypeEncodingParams = new GenotypeEncodingParams(genotypeEncodindP);
			}

			final String svmLibraryStr = args.get("svm-library");
			final SolverLibrary svmLibrary = (svmLibraryStr == null) ? null
					: SolverLibrary.valueOf(svmLibraryStr);
			final Double svmEps = fetchDouble(args, "svm-eps", null);
			final Double svmC = fetchDouble(args, "svm-C", null);

			final Integer weightsFilterWidth = fetchInteger(args, "weights-filter-width", null);

			Integer markersToKeep = fetchInteger(args, "markers-to-keep", null);
			final Double markersToKeepFraction = fetchDouble(args, "markers-to-keep-fraction", null);

			final Boolean useThresholdCalibration = fetchBoolean(args, "use-threshold-calibration", null);

			// These might return null, as it is optional,
			// which will lead to using the default name
			String resultOperationName = args.get("result-operation-name");
			String resultFilterOperationName = args.get("result-filter-operation-name");

			CombiTestOperationParams paramsTest = new CombiTestOperationParams(
					qaMarkersOperationKey,
					genotypeEncoder,
					genotypeEncodingParams,
					useThresholdCalibration,
					perChromosome,
					svmLibrary,
					new SolverParams(svmEps, svmC),
					resultOperationName);
			if (resultFilterOperationName == null) {
				resultFilterOperationName = "Filter on " + paramsTest.getName();
			}
			ByCombiWeightsFilterOperationParams paramsFilter = new ByCombiWeightsFilterOperationParams(
					totalMarkers,
					perChromosome,
					weightsFilterWidth,
					markersToKeep,
					markersToKeepFraction,
					resultFilterOperationName);

			// test block
			final CommonRunnable combiTask = new Threaded_Combi(paramsTest, paramsFilter);
			CommonRunnable.doRunNowInThread(combiTask);
		} catch (final IOException ex) {
			throw new ScriptExecutionException(ex);
		}
	}
}
