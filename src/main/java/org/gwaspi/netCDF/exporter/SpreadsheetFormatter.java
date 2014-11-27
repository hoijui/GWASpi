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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.gwaspi.constants.cExport;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import static org.gwaspi.netCDF.exporter.Formatter.PLACEHOLDER_PS_EXPORT;
import org.gwaspi.progress.IndeterminateProgressHandler;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpreadsheetFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(SpreadsheetFormatter.class);

	@Override
	public boolean export(
			String exportPath,
			DataSetMetadata rdDataSetMetadata,
			DataSetSource dataSetSource,
			final SuperProgressSource superProgressSource,
			String phenotype)
			throws IOException
	{
		final ProcessInfo exportPI = new SubProcessInfo(
				superProgressSource.getInfo(),
				"export",
				"format and export in a simple spreadsheet (CSV) format");
		final ProcessInfo exportSamplesPI = new SubProcessInfo(
				exportPI,
				"export samples",
				"format and export sample infos & genotypes");
		final ProcessInfo exportMarkersPI = new SubProcessInfo(
				exportPI,
				"export markers",
				"format and export marker infos");

		final SuperProgressSource exportPS = new SuperProgressSource(exportPI);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_EXPORT, exportPS, null);
		exportPS.setNewStatus(ProcessStatus.INITIALIZING);

		final double headerTimeFraction = 1.0 / (dataSetSource.getNumSamples() + 1);
		final IndeterminateProgressHandler exportMarkersPS = new IndeterminateProgressHandler(exportMarkersPI);
		exportPS.addSubProgressSource(exportMarkersPS, headerTimeFraction);

		final IntegerProgressHandler exportSamplesPS = new IntegerProgressHandler(exportSamplesPI, 0, dataSetSource.getNumSamples() - 1);
		exportPS.addSubProgressSource(exportSamplesPS, 1.0 - headerTimeFraction);

		final File exportDir = Utils.checkDirPath(exportPath);

		boolean result = false;
		String sep = cExport.separator_REPORTS;

		//<editor-fold defaultstate="expanded" desc="SPREADSHEET FILE">
		exportMarkersPS.setNewStatus(ProcessStatus.INITIALIZING);
		BufferedWriter pedBW = null;
		try {
			FileWriter pedFW = new FileWriter(new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".csv"));
			pedBW = new BufferedWriter(pedFW);

			// HEADER CONTAINING MARKER IDs
			exportMarkersPS.setNewStatus(ProcessStatus.RUNNING);
			for (MarkerKey key : dataSetSource.getMarkersKeysSource()) {
				pedBW.append(sep);
				pedBW.append(key.getMarkerId());
			}
			exportMarkersPS.setNewStatus(ProcessStatus.FINALIZING);
			pedBW.append("\n");
			pedBW.flush();
			exportMarkersPS.setNewStatus(ProcessStatus.COMPLEETED);

			// Iterate through all samples
			exportSamplesPS.setNewStatus(ProcessStatus.INITIALIZING);
			int sampleNb = 0;
			Iterator<GenotypesList> samplesGenotypesIt = dataSetSource.getSamplesGenotypesSource().iterator();
			exportSamplesPS.setNewStatus(ProcessStatus.RUNNING);
			for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
				// Individual ID
				pedBW.append(sampleKey.getSampleId());

				// Genotypes
				// Iterate through all markers
				for (byte[] tempGT : samplesGenotypesIt.next()) {
					pedBW.append(sep);
					pedBW.append((char) tempGT[0]);
					pedBW.append((char) tempGT[1]);
				}

				pedBW.append("\n");
				pedBW.flush();

				exportSamplesPS.setProgress(sampleNb);
				sampleNb++;
			}
			exportSamplesPS.setNewStatus(ProcessStatus.FINALIZING);
			log.info("Samples exported to Fleur file: {}", sampleNb);

			result = true;
		} finally {
			if (pedBW != null) {
				pedBW.close();
			}
		}
		exportMarkersPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>

		return result;
	}
}
