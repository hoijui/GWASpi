package org.gwaspi.threadbox;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.netCDF.exporter.MatrixExporter_opt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class Threaded_ExportMatrix extends CommonRunnable {

	private boolean startWithGUI = org.gwaspi.gui.StartGWASpi.guiMode;
	private int matrixId;
	private ExportFormat format;
	private String phenotype;

	public Threaded_ExportMatrix(String threadName,
			String timeStamp,
			int matrixId,
			ExportFormat format,
			String phenotype)
	{
		super(threadName, timeStamp, "Exporting Matrix");

		this.matrixId = matrixId;
		this.format = format;
		this.phenotype = phenotype;

		startInternal(getTaskDescription());
	}

	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExportMatrix.class);
	}

	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (format != null) {
			switch (org.gwaspi.constants.cExport.ExportFormat.compareTo(format.toString())) {
				case PLINK:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					MatrixExporter_opt mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.PLINK.toString(), phenotype);
					break;
				case PLINK_Binary:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.PLINK_Binary.toString(), phenotype);
					break;
				case Eigensoft_Eigenstrat:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.Eigensoft_Eigenstrat.toString(), phenotype);
					break;
				case PLINK_Transposed:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.PLINK_Transposed.toString(), phenotype);
					break;
				case BEAGLE:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.BEAGLE.toString(), phenotype);
					break;
				case GWASpi:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.GWASpi.toString(), phenotype);
					break;
				case Spreadsheet:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.Spreadsheet.toString(), phenotype);
					break;
				case MACH:
					if (startWithGUI) {
						org.gwaspi.gui.ProcessTab.showTab();
					}
					mEx = new MatrixExporter_opt(matrixId);
					mEx.exportToFormat(org.gwaspi.constants.cExport.ExportFormat.MACH.toString(), phenotype);
					break;
			}
		}
	}
}
