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
import java.util.HashMap;
import java.util.Map;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.combi.AllelicGenotypeEncoder;
import org.gwaspi.operations.combi.NominalGenotypeEncoder;
import org.gwaspi.operations.combi.CombiTestOperationParams;
import org.gwaspi.operations.combi.GenotypeEncoder;
import org.gwaspi.operations.combi.GenotypicGenotypeEncoder;
import org.gwaspi.threadbox.MultiOperations;

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
	public boolean execute(Map<String, String> args) throws IOException {

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

//		GWASinOneGOParams gwasParams = new GWASinOneGOParams();
		// checking study
		StudyKey studyKey = fetchStudyKey(args, "study-id", "study-name", false);
		boolean studyExists = checkStudy(studyKey);

		if (studyExists) {
			MatrixKey matrixKey = fetchMatrixKey(args, studyKey, "matrix-id", "matrix-name");

//			OperationKey censusOperationKey = fetchOperationKey(args, matrixKey, "census-operation-id", "census-operation-name");
			OperationKey censusOperationKey = fetchOperationKey(args, matrixKey, "gtfreq-id", "gtfreq-name");

			OperationKey hwOperationKey = fetchOperationKey(args, matrixKey, "hw-id", "hw-name");
			double hwThreshold = Double.parseDouble(args.get("hw-threshold"));

			GenotypeEncoder genotypeEncoder = GENOTYPE_ENCODERS.get(args.get("genotype-encoding"));

			int markersToKeep = Integer.parseInt(args.get("markers-to-keep"));

			boolean useThresholdCalibration = (Integer.parseInt(args.get("use-threshold-calibration")) != 0);

			String phenotypeInfoStr = args.get("phenotype-info-file");
			File phenotypeInfo = (phenotypeInfoStr == null) ? null : new File(phenotypeInfoStr);

			// This might return null, as it is optional,
			// which will lead to using the default name
			String resultOperationName = args.get("result-operation-name");

			CombiTestOperationParams params = new CombiTestOperationParams(
//					matrixKey,
					censusOperationKey,
//					hwOperationKey,
//					hwThreshold,
					genotypeEncoder,
					markersToKeep,
					useThresholdCalibration,
//					phenotypeInfo,
					resultOperationName);

			// test block
//			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doCombiTest(params);
				return true;
//			}
//		} else {
//			throw new RuntimeException("Study does not exist: " + studyKey.toString());
		}

		return false;
	}
}
