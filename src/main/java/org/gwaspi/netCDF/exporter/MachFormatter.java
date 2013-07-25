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
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

public class MachFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(MachFormatter.class);
	private static final String SEP = cExport.separator_MACH;

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
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		try {
			rdMarkerSet.initFullMarkerIdSetMap();

			// FIND START AND END MARKERS BY CHROMOSOME
			rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			Map<MarkerKey, char[]> chrMarkerSetMap = new LinkedHashMap<MarkerKey, char[]>();
			chrMarkerSetMap.putAll(rdMarkerSet.getMarkerIdSetMapCharArray());
			String tmpChr = "";
			int start = 0;
			int end = 0;
			for (char[] value : chrMarkerSetMap.values()) {
				String chr = new String(value);
				if (!chr.equals(tmpChr)) {
					if (start != end) {
						exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end - 1);
						exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);
						start = end;
					}
					tmpChr = chr;
				}
				end++;
			}
			exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end);
			exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);

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

	private void exportChromosomeToMped(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet rdMarkerSet, Map<SampleKey, ?> rdSampleSetMap, String chr, int startPos, int endPos) throws IOException {

		FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".mped");
		BufferedWriter pedBW = new BufferedWriter(pedFW);

		// Iterate through all samples
		int sampleNb = 0;
		for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
			SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey);
			String sexStr = "0";
			String familyId = sampleInfo.getFamilyId();
			String fatherId = sampleInfo.getFatherId();
			String motherId = sampleInfo.getMotherId();
			SampleInfo.Sex sex = sampleInfo.getSex();
			if (sex == SampleInfo.Sex.MALE) {
				sexStr = "M";
			} else if (sex == SampleInfo.Sex.FEMALE) {
				sexStr = "F";
			}

			// Iterate through current chrl markers
			rdMarkerSet.initMarkerIdSetMap(startPos, endPos);
			rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
			StringBuilder genotypes = new StringBuilder();
			int markerNb = 0;
			for (byte[] tempGT : rdMarkerSet.getMarkerIdSetMapByteArray().values()) {
				genotypes.append(SEP);
				genotypes.append(new String(tempGT, 0, 1));
				genotypes.append(SEP);
				genotypes.append(new String(tempGT, 1, 1));
				markerNb++;
			}

			// Family ID
			// Individual ID
			// Paternal ID
			// Maternal ID
			// Sex (1=male; 2=female; other=unknown)
			// Genotypes

			StringBuilder line = new StringBuilder();
			line.append(familyId);
			line.append(SEP);
			line.append(sampleKey.getSampleId());
			line.append(SEP);
			line.append(fatherId);
			line.append(SEP);
			line.append(motherId);
			line.append(SEP);
			line.append(sexStr);
			line.append(genotypes);

			pedBW.append(line);
			pedBW.append("\n");
			pedBW.flush();

			sampleNb++;
		}
		log.info("Samples exported to chr{} MPED file: {}", chr, sampleNb);
		pedBW.close();
		pedFW.close();
	}

	public void exportChromosomeToDat(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet rdMarkerSet, String chr, int startPos, int endPos) throws IOException {

		FileWriter datFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".dat");
		BufferedWriter datBW = new BufferedWriter(datFW);

		// DAT files
		//     "M" indicates a marker
		//     rs# or marker identifier

		// MARKERSET RSID
		rdMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_RSID);

		// Iterate through current chr markers
		// INIT MARKERSET
		rdMarkerSet.initMarkerIdSetMap(startPos, endPos);
		int markerNb = 0;
		for (Map.Entry<MarkerKey, char[]> entry : rdMarkerSet.getMarkerIdSetMapCharArray().entrySet()) {
			// CHECK IF rsID is available
			String markerId = entry.getKey().getMarkerId();
			String value = new String(entry.getValue());
			if (!value.isEmpty()) {
				markerId = value;
			}

			datBW.append("M" + SEP);
			datBW.append(markerId);
			datBW.append("\n");

			markerNb++;
		}

		log.info("Markers exported to chr{} DAT file: {}", chr, (endPos + 1 - startPos));

		datBW.close();
		datFW.close();
	}
}
