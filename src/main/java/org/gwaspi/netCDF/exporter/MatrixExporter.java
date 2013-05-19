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

package org.gwaspi.netCDF.exporter;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.gwaspi.constants.cExport.ExportFormat;
import org.gwaspi.global.Config;
import org.gwaspi.global.Text;
import org.gwaspi.model.MatricesList;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;

public class MatrixExporter {

	private final Logger log = LoggerFactory.getLogger(MachFormatter.class);

	private int rdMatrixId = Integer.MIN_VALUE;
	private MatrixMetadata rdMatrixMetadata = null;
	private MarkerSet rdMarkerSet = null;
	private SampleSet rdSampleSet = null;
	private Map<SampleKey, byte[]> rdSampleSetMap = null;
	private Map<ExportFormat, Formatter> formatters;

	public MatrixExporter(int _rdMatrixId) throws IOException, InvalidRangeException {

		// INIT EXTRACTOR OBJECTS

		rdMatrixId = _rdMatrixId;
		rdMatrixMetadata = MatricesList.getMatrixMetadataById(rdMatrixId);

		rdMarkerSet = new MarkerSet(rdMatrixMetadata.getStudyId(), rdMatrixId);

		rdSampleSet = new SampleSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		rdSampleSetMap = rdSampleSet.getSampleIdSetMapByteArray();

		formatters = new EnumMap<ExportFormat, Formatter>(ExportFormat.class);
		formatters.put(ExportFormat.PLINK, new PlinkFormatter());
		formatters.put(ExportFormat.PLINK_Transposed, new PlinkTransposedFormatter());
		formatters.put(ExportFormat.PLINK_Binary, new PlinkBinaryFormatter());
		formatters.put(ExportFormat.Eigensoft_Eigenstrat, new PlinkBinaryFormatter(true));
		formatters.put(ExportFormat.BEAGLE, new BeagleFormatter());
		formatters.put(ExportFormat.GWASpi, new GWASpiFormatter());
		formatters.put(ExportFormat.Spreadsheet, new SpreadsheetFormatter());
		formatters.put(ExportFormat.MACH, new MachFormatter());
	}

	public boolean exportToFormat(ExportFormat exportFormat, String phenotype) throws IOException {
		log.info(Text.All.processing);

		String exportPath = Config.getConfigValue(Config.PROPERTY_EXPORT_DIR, "");
		exportPath = exportPath + "/STUDY_" + rdMatrixMetadata.getStudyId();
		org.gwaspi.global.Utils.createFolder(new File(exportPath));
		Formatter formatter = formatters.get(exportFormat);

		if (exportFormat == ExportFormat.BEAGLE) {
			rdMarkerSet = new MarkerSet(rdMatrixMetadata.getStudyId(), rdMatrixId);
		}
		boolean result = formatter.export(
				exportPath,
				rdMatrixMetadata,
				rdMarkerSet,
				rdSampleSet,
				rdSampleSetMap,
				phenotype);

		if (rdMarkerSet.getMarkerIdSetMapCharArray() != null) {
			rdMarkerSet.getMarkerIdSetMapCharArray().clear();
		}

		org.gwaspi.global.Utils.sysoutCompleted("exporting Matrix to \"" + exportPath + "\"");
		return result;
	}
}
