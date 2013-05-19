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
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class SpreadsheetFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(SpreadsheetFormatter.class);

	@Override
	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet rdMarkerSet,
			SampleSet rdSampleSet,
			Map<SampleKey, byte[]> rdSampleSetMap,
			String phenotype)
			throws IOException
	{

		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = cExport.separator_REPORTS;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		rdMarkerSet.initFullMarkerIdSetMap();

		try {
			//<editor-fold defaultstate="expanded" desc="SPREADSHEET FILE">
			FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".csv");
			BufferedWriter pedBW = new BufferedWriter(pedFW);

			// HEADER CONTAINING MARKER IDs
			StringBuilder line = new StringBuilder();
			for (MarkerKey key : rdMarkerSet.getMarkerKeys()) {
				line.append(sep);
				line.append(key.getMarkerId());
			}
			pedBW.append(line);
			pedBW.append("\n");
			pedBW.flush();

			// Iterate through all samples
			int sampleNb = 0;
			for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
				// Iterate through all markers
				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
				StringBuilder genotypes = new StringBuilder();
				for (byte[] tempGT : rdMarkerSet.getMarkerIdSetMapByteArray().values()) {
					genotypes.append(sep);
					genotypes.append(new String(tempGT, 0, 1));
					genotypes.append(new String(tempGT, 1, 1));
				}

				// Individual ID
				// Genotypes
				line = new StringBuilder();
				line.append(sampleKey.getSampleId());
				line.append(genotypes);

				pedBW.append(line);
				pedBW.append("\n");
				pedBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported to Fleur file: {}", sampleNb);
				}
			}
			log.info("Samples exported to Fleur file: {}", sampleNb);
			pedBW.close();
			pedFW.close();
			//</editor-fold>

			result = true;
		} catch (IOException ex) {
			log.error(null, ex);
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file: " + rdNcFile, ex);
				}
			}
		}

		return result;
	}
}
