package org.gwaspi.threadbox;

import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.gui.ProcessTab;
import org.gwaspi.netCDF.exporter.MatrixExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Threaded_ExportMatrix extends CommonRunnable {

	private boolean startWithGUI = org.gwaspi.gui.StartGWASpi.guiMode;
	private int matrixId;
	private ExportFormat format;
	private String phenotype;

	public Threaded_ExportMatrix(
			int matrixId,
			ExportFormat format,
			String phenotype)
	{
		super(
				"Export Matrix",
				"Exporting Matrix",
				"Export Matrix ID: " + matrixId,
				"Exporting Matrix");

		this.matrixId = matrixId;
		this.format = format;
		this.phenotype = phenotype;
	}

	@Override
	protected Logger createLog() {
		return LoggerFactory.getLogger(Threaded_ExportMatrix.class);
	}

	@Override
	protected void runInternal(SwingWorkerItem thisSwi) throws Exception {

		if (format != null) {
			if (startWithGUI) {
				ProcessTab.getSingleton().showTab();
			}
			MatrixExporter mEx = new MatrixExporter(matrixId);
			mEx.exportToFormat(format, phenotype);
		}
	}
}
