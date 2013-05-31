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
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

class AssociationScriptCommand extends AbstractScriptCommand {

	private final boolean allelic;

	AssociationScriptCommand(boolean allelic) {
		super((allelic ? "allelic" : "genotypic") + "_association");

		this.allelic = allelic;
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=allelic_association # or "genotypic_association"
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
		int studyId = prepareStudy(args.get("study-id"), false);
		boolean studyExists = checkStudy(studyId);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get("matrix-id")); // Parent Matrix Id
			int gtFreqId = Integer.parseInt(args.get("gtfreq-id")); // Parent GtFreq operation Id
			int hwId = Integer.parseInt(args.get("hw-id")); // Parent Hardy-Weinberg operation Id

			gwasParams.setPerformAllelicTests(allelic);
			gwasParams.setPerformGenotypicTests(!allelic);
			gwasParams.setPerformTrendTests(false);

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get("calculate-discard-threshold-for-HW")));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get("discard-marker-with-provided-HW-threshold")));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get("discard-marker-HW-treshold")));
			gwasParams.setProceed(true);

			List<OPType> necessaryOPs = new ArrayList<OPType>();
			necessaryOPs.add(OPType.SAMPLE_QA);
			necessaryOPs.add(OPType.MARKER_QA);
			List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(necessaryOPs, matrixId);

			// QA block
			if (gwasParams.isProceed() && missingOPs.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				MultiOperations.doMatrixQAs(studyId, matrixId);
			}

			// test block
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				MultiOperations.doAssociationTest(
						studyId,
						matrixId,
						gtFreqId,
						hwId,
						gwasParams,
						allelic);
				return true;
			}
		}

		return false;
	}
}
