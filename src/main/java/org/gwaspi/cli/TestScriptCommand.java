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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.gwaspi.constants.cNetCDF.Defaults.OPType;
import org.gwaspi.global.Text;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.StudyKey;
import org.gwaspi.operations.GWASinOneGOParams;
import org.gwaspi.operations.OperationManager;
import org.gwaspi.threadbox.CommonRunnable;
import org.gwaspi.threadbox.MultiOperations;
import org.gwaspi.threadbox.Threaded_MatrixQA;
import org.gwaspi.threadbox.Threaded_Test;

public class TestScriptCommand extends AbstractScriptCommand {

	private static final List<OPType> QA_OPERATION_TYPES;
	static {
		List<OPType> qaOpTypes = new ArrayList<OPType>();
		qaOpTypes.add(OPType.SAMPLE_QA);
		qaOpTypes.add(OPType.MARKER_QA);
		QA_OPERATION_TYPES = Collections.unmodifiableList(qaOpTypes);
	}

	private final OPType testType;

	TestScriptCommand(OPType testType) {
		super(createCommandName(testType));

		this.testType = testType;
	}

	private static String createCommandName(OPType testType) {

		final String commandName;
		switch (testType) {
			case ALLELICTEST:
				commandName = "allelic_association";
				break;
			case GENOTYPICTEST:
				commandName = "genotypic_association";
				break;
			case COMBI_ASSOC_TEST:
				commandName = "combi_association";
				break;
			case TRENDTEST:
				commandName = "trend_test";
				break;
			default:
				throw new IllegalArgumentException("Not a supported test type: " + testType.toString());
		}

		return commandName;
	}

	@Override
	public boolean execute(Map<String, String> args) throws IOException {

		//<editor-fold defaultstate="expanded" desc="SCRIPT EXAMPLE">
		/*
		# This is a demo file
		# Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=allelic_association # or "genotypic_association" or "trend_test"
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
		StudyKey studyKey = prepareStudy(args.get("study-id"), false);
		boolean studyExists = checkStudy(studyKey);

		if (studyExists) {
			int matrixId = Integer.parseInt(args.get("matrix-id")); // Parent Matrix Id
			MatrixKey matrixKey = new MatrixKey(studyKey, matrixId);
			int gtFreqId = Integer.parseInt(args.get("gtfreq-id")); // Parent GtFreq operation Id
			OperationKey gtFreqKey = new OperationKey(matrixKey, gtFreqId);
			int hwId = Integer.parseInt(args.get("hw-id")); // Parent Hardy-Weinberg operation Id
			OperationKey hwKey = new OperationKey(matrixKey, hwId);

			gwasParams.setPerformAllelicTests(testType == OPType.ALLELICTEST);
			gwasParams.setPerformGenotypicTests(testType == OPType.GENOTYPICTEST);
			gwasParams.setPerformTrendTests(testType == OPType.TRENDTEST);

			gwasParams.getMarkerCensusOperationParams().setDiscardMismatches(true);
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get("calculate-discard-threshold-for-HW")));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get("discard-marker-with-provided-HW-threshold")));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get("discard-marker-HW-treshold")));
			gwasParams.setProceed(true);

			ensureMatrixQAs(matrixKey, gwasParams);

			// test block
			if (gwasParams.isProceed()) {
				final CommonRunnable testTask = new Threaded_Test(
						gtFreqKey,
						hwKey,
						gwasParams,
						testType);
				MultiOperations.queueTask(testTask);
				return true;
			}
		}

		return false;
	}

	public static void ensureQAOperations(final DataSetKey dataSetKey, final GWASinOneGOParams gwasParams) {

		final List<OPType> missingOPs = OperationManager.checkForNecessaryOperations(QA_OPERATION_TYPES, dataSetKey, true);

		if (gwasParams.isProceed() && missingOPs.size() > 0) {
			gwasParams.setProceed(false);
			System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
			final CommonRunnable task = new Threaded_MatrixQA(dataSetKey);
			MultiOperations.queueTask(task);
		}
	}

	public static void ensureMatrixQAs(final MatrixKey matrixKey, final GWASinOneGOParams gwasParams) {
		ensureQAOperations(new DataSetKey(matrixKey), gwasParams);
	}
}
