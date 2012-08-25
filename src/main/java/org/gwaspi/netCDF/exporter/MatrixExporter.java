package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cExport.ExportFormat;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.samples.SampleSet;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MatrixExporter {

	private int rdMatrixId = Integer.MIN_VALUE;
	private MatrixMetadata rdMatrixMetadata = null;
	private MarkerSet_opt rdMarkerSet = null;
	private SampleSet rdSampleSet = null;
	private Map<String, Object> rdSampleSetLHM = null;
	private Map<ExportFormat, Formatter> formatters;

	public MatrixExporter(int _rdMatrixId) throws IOException, InvalidRangeException {

		/////////// INIT EXTRACTOR OBJECTS //////////

		rdMatrixId = _rdMatrixId;
		rdMatrixMetadata = new MatrixMetadata(rdMatrixId);

		rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);

		rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdSampleSetLHM = rdSampleSet.getSampleIdSetLHM();

		formatters = new EnumMap<ExportFormat, Formatter>(ExportFormat.class);
		formatters.put(ExportFormat.PLINK, new PlinkFormatter());
		formatters.put(ExportFormat.PLINK_Transposed, new PlinkTransposedFormatter());
		formatters.put(ExportFormat.PLINK_Binary, new PlinkBinaryFormatter());
		formatters.put(ExportFormat.Eigensoft_Eigenstrat, new EigensoftFormatter());
		formatters.put(ExportFormat.BEAGLE, new BeagleFormatter());
		formatters.put(ExportFormat.GWASpi, new GWASpiFormatter());
		formatters.put(ExportFormat.Spreadsheet, new SpreadsheetFormatter());
		formatters.put(ExportFormat.MACH, new MachFormatter());
	}

	public boolean exportToFormat(String exportFormatStr, String phenotype) throws IOException {
		System.out.println(org.gwaspi.global.Text.All.processing);

		String exportPath = org.gwaspi.global.Config.getConfigValue("ExportDir", "");
		org.gwaspi.global.Utils.createFolder(exportPath, "STUDY_" + rdMatrixMetadata.getStudyId());
		exportPath = exportPath + "/STUDY_" + rdMatrixMetadata.getStudyId();
		ExportFormat exportFormat = ExportFormat.compareTo(exportFormatStr);
		Formatter formatter = formatters.get(exportFormat);

		if (exportFormat == ExportFormat.BEAGLE) {
			rdMarkerSet = new MarkerSet_opt(rdMatrixMetadata.getStudyId(), rdMatrixId);
		}
		boolean result = formatter.export(
				exportPath,
				rdMatrixMetadata,
				rdMarkerSet,
				rdSampleSet,
				rdSampleSetLHM,
				phenotype);

		if (rdMarkerSet.getMarkerIdSetLHM() != null) {
			rdMarkerSet.getMarkerIdSetLHM().clear();
		}

		org.gwaspi.global.Utils.sysoutCompleted("exporting Matrix" + exportPath);
		return result;
	}
}
