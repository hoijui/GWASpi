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
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class PlinkFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkFormatter.class);

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
		String sep = cExport.separator_PLINK;
		String sepBig = cExport.separator_PLINK_big;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		rdMarkerSet.initFullMarkerIdSetMap();

		try {
			//<editor-fold defaultstate="expanded" desc="PED FILE">
			FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".ped");
			BufferedWriter pedBW = new BufferedWriter(pedFW);

			// Iterate through all samples
			int sampleNb = 0;
			for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();

				// Iterate through all markers
				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
				StringBuilder genotypes = new StringBuilder();
				for (byte[] tempGT : rdMarkerSet.getMarkerIdSetMapByteArray().values()) {
					genotypes.append(sepBig);
					genotypes.append(new String(tempGT, 0, 1));
					genotypes.append(sep);
					genotypes.append(new String(tempGT, 1, 1));
				}

				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection
				// Genotypes

				StringBuilder line = new StringBuilder();
				line.append(familyId);
				line.append(sepBig);

				line.append(sampleKey.getSampleId());
				line.append(sep);
				line.append(fatherId);
				line.append(sep);
				line.append(motherId);
				line.append(sep);
				line.append(sex);
				line.append(sep);
				line.append(affection);

				line.append(genotypes);

				pedBW.append(line);
				pedBW.append("\n");
				pedBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported to PED file: {}", sampleNb);
				}
			}
			log.info("Samples exported to PED file: {}", sampleNb);
			pedBW.close();
			pedFW.close();
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="MAP FILE">
			FileWriter mapFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".map");
			BufferedWriter mapBW = new BufferedWriter(mapFW);

			// MAP files
			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//     rs# or snp identifier
			//     Genetic distance (morgans)
			//     Base-pair position (bp units)

			// PURGE MARKERSET
			rdMarkerSet.fillWith(new char[0]);

			// MARKERSET CHROMOSOME
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);

			// MARKERSET MARKER-ID and/or RSID
//			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_RSID, sep);
			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERSET, sep);

			// DEFAULT GENETIC DISTANCE = 0
			for (Map.Entry<?, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
				StringBuilder value = new StringBuilder(new String(entry.getValue()));
				value.append(sep);
				value.append("0");
				entry.setValue(value.toString().toCharArray());
			}

			// MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetMapValue(cNetCDF.Variables.VAR_MARKERS_POS, sep);

			// write the accumulated values to the .map file
			int markerNb = 0;
			for (char[] pos : rdMarkerSet.getMarkerIdSetMapCharArray().values()) {
				mapBW.append(new String(pos));
				mapBW.append("\n");
				markerNb++;
			}

			log.info("Markers exported to MAP file: {}", markerNb);

			mapBW.close();
			mapFW.close();
			//</editor-fold>

			result = true;
		} catch (Throwable ex) {
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
