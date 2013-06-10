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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.operations.combi.AllelicGenotypeEncoder;
import org.gwaspi.operations.combi.NominalGenotypeEncoder;
import org.gwaspi.operations.combi.CombiTestParams;
import org.gwaspi.operations.combi.GenotypeEncoder;
import org.gwaspi.operations.combi.GenotypicGenotypeEncoder;
import org.gwaspi.threadbox.MultiOperations;

class CombiTestScriptCommand extends AbstractScriptCommand {

	private static final Map<String, GenotypeEncoder> GENOTYPE_ENCODERS
			= new HashMap<String, GenotypeEncoder>();
	static {
		GENOTYPE_ENCODERS.put("allelic", new AllelicGenotypeEncoder());
		GENOTYPE_ENCODERS.put("genotypic", new GenotypicGenotypeEncoder());
		GENOTYPE_ENCODERS.put("nominal", new NominalGenotypeEncoder());
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
		2.matrix-id=8 # or "2.matrix-name=matrixX"
		3.genotype-encoding=allelic # ..., "genotypic" or "XXX"
		4.phenotype-info-file=/tmp/myPhenotypeInfo.txt # [optional]
		[/script]
		*/
		//</editor-fold>

//		GWASinOneGOParams gwasParams = new GWASinOneGOParams();
		// checking study
		StudyKey studyKey = fetchStudyKey(args, "study-id", "study-name", false);
		boolean studyExists = checkStudy(studyKey);

		if (studyExists) {
			MatrixKey matrixKey = fetchMatrixKey(args, studyKey, "matrix-id", "matrix-name");

			GenotypeEncoder genotypeEncoder = GENOTYPE_ENCODERS.get(args.get("genotype-encoding"));

			String phenotypeInfoStr = args.get("phenotype-info-file");
			File phenotypeInfo = (phenotypeInfoStr == null) ? null : new File(phenotypeInfoStr);

			String resultMatrixName = "Combi-Test for matrix " + matrixKey.toString();

			CombiTestParams params = new CombiTestParams(
					matrixKey,
					genotypeEncoder,
					phenotypeInfo,
					resultMatrixName);

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
