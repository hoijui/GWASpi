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
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpreadsheetFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(SpreadsheetFormatter.class);

	@Override
	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			DataSetSource dataSetSource,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = cExport.separator_REPORTS;

		//<editor-fold defaultstate="expanded" desc="SPREADSHEET FILE">
		BufferedWriter pedBW = null;
		try {
			FileWriter pedFW = new FileWriter(new File(exportDir.getPath(),
					rdMatrixMetadata.getFriendlyName() + ".csv"));
			pedBW = new BufferedWriter(pedFW);

			// HEADER CONTAINING MARKER IDs
			for (MarkerKey key : dataSetSource.getMarkersKeysSource()) {
				pedBW.append(sep);
				pedBW.append(key.getMarkerId());
			}
			pedBW.append("\n");
			pedBW.flush();

			// Iterate through all samples
			int sampleNb = 0;
			Iterator<GenotypesList> samplesGenotypesIt = dataSetSource.getSamplesGenotypesSource().iterator();
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

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported to Fleur file: {}", sampleNb);
				}
			}
			log.info("Samples exported to Fleur file: {}", sampleNb);

			result = true;
		} finally {
			if (pedBW != null) {
				pedBW.close();
			}
		}
		//</editor-fold>

		return result;
	}
}
