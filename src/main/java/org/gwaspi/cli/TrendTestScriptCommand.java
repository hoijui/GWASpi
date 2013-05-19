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
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

class TrendTestScriptCommand extends AbstractScriptCommand {

	TrendTestScriptCommand() {
		super("trend_test");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=trend_test
		1.study-id=1
		2.matrix-id=8
		3.gtfreq-id=46
		4.hw-id=47
		5.calculate-discard-threshold-for-HW=false
		6.discard-marker-with-provided-HW-threshold=true
		7.discard-marker-HW-treshold=0.0000005
		[/script]
		*/
		//</editor-fold>

		GWASinOneGOParams gwasParams = new GWASinOneGOParams();

		// checking study
		int studyId = prepareStudy(args.get(1), false);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get(2)); // Parent Matrix Id
			int gtFreqId = Integer.parseInt(args.get(3)); // Parent GtFreq Id
			int hwId = Integer.parseInt(args.get(4)); // Parent GtFreq Id

			gwasParams.setPerformAllelicTests(false);
			gwasParams.setPerformGenotypicTests(false);
			gwasParams.setPerformTrendTests(true);

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get(5)));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get(6)));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get(7)));
			gwasParams.setProceed(true);

			List<OPType> necessaryOPs = new ArrayList<OPType>();
			necessaryOPs.add(OPType.SAMPLE_QA);
			necessaryOPs.add(OPType.MARKER_QA);
			List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, matrixId);

			// QA BLOCK
			if (gwasParams.isProceed() && missingOPs.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				MultiOperations.doMatrixQAs(studyId, matrixId);
			}

			// TRend TEST BLOCK
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doTrendTest(studyId,
						matrixId,
						gtFreqId,
						hwId,
						gwasParams);
				return true;
			}
		}

		return false;
	}
}
