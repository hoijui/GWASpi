package org.gwaspi.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.global.Text;
import org.gwaspi.netCDF.operations.GWASinOneGOParams;
import org.gwaspi.netCDF.operations.OperationManager;
import org.gwaspi.threadbox.MultiOperations;

/**
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
@Component
class AllelicAssociationScriptCommand extends AbstractScriptCommand {

	@Reference
	private MultiOperations multiOperations;

	protected void bindMultiOperations(MultiOperations multiOperations) {
		this.multiOperations = multiOperations;
	}

	protected void unbindMultiOperations(MultiOperations multiOperations) {

		if (this.multiOperations == multiOperations) {
			this.multiOperations = null;
		}
	}

	AllelicAssociationScriptCommand() {
		super("allelic_association");
	}

	@Override
	public boolean execute(List<String> args) throws IOException {

		//<editor-fold defaultstate="collapsed" desc="SCRIPT EXAMPLE">
		/*
		#This is a demo file
		#Usage: java -Xms1500m -Xmx2500m -jar GWASpi.jar script scriptFile [log org.gwaspi.cli.log]
		data-dir=/media/data/GWASpi
		[script]
		0.command=allelic_association
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

			gwasParams.setPerformAllelicTests(true);
			gwasParams.setPerformGenotypicTests(false);
			gwasParams.setPerformTrendTests(false);

			gwasParams.setDiscardGTMismatches(true);
			gwasParams.setDiscardMarkerHWCalc(Boolean.parseBoolean(args.get(5)));
			gwasParams.setDiscardMarkerHWFree(Boolean.parseBoolean(args.get(6)));
			gwasParams.setDiscardMarkerHWTreshold(Double.parseDouble(args.get(7)));
			gwasParams.setProceed(true);

			List<String> necessaryOPsAL = new ArrayList<String>();
			necessaryOPsAL.add(cNetCDF.Defaults.OPType.SAMPLE_QA.toString());
			necessaryOPsAL.add(cNetCDF.Defaults.OPType.MARKER_QA.toString());
			List<String> missingOPsAL = OperationManager.checkForNecessaryOperations(necessaryOPsAL, matrixId);

			// QA BLOCK
			if (gwasParams.isProceed() && missingOPsAL.size() > 0) {
				gwasParams.setProceed(false);
				System.out.println(Text.Operation.warnQABeforeAnything + "\n" + Text.Operation.willPerformOperation);
				multiOperations.doMatrixQAs(studyId, matrixId);
			}

			// ALLELIC ALLELICTEST BLOCK
			if (gwasParams.isProceed()) {
				System.out.println(Text.All.processing);
				multiOperations.doAllelicAssociationTest(studyId,
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
