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
import java.util.LinkedList;
import java.util.List;
import org.gwaspi.constants.cExport;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(MachFormatter.class);
	private static final String SEP = cExport.separator_MACH;

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

		// FIND START AND END MARKERS BY CHROMOSOME
		String tmpChr = "";
		int start = 0;
		int end = 0;
		String dataSetName = rdMatrixMetadata.getMatrixFriendlyName();
		List<String> chrMarkerRsIds = new LinkedList<String>();
		for (MarkerMetadata value : dataSetSource.getMarkersMetadatasSource()) {
			String chr = value.getChr();

			// CHECK IF rsID is available
			String markerId = value.getRsId();
			if (markerId.isEmpty()) {
				markerId = value.getMarkerId();
			}
			chrMarkerRsIds.add(markerId);

			if (!chr.equals(tmpChr)) {
				if (start != end) {
					exportChromosomeToMped(exportDir, dataSetName, dataSetSource, tmpChr, start, end - 1);
					exportChromosomeToDat(exportDir, dataSetName, dataSetSource, chrMarkerRsIds, tmpChr, start, end - 1);
					start = end;
					chrMarkerRsIds.clear();
				}
				tmpChr = chr;
			}
			end++;
		}
		exportChromosomeToMped(exportDir, dataSetName, dataSetSource, tmpChr, start, end);
		exportChromosomeToDat(exportDir, dataSetName, dataSetSource, chrMarkerRsIds, tmpChr, start, end - 1);

		result = true;

		return result;
	}

	private void exportChromosomeToMped(File exportDir, String dataSetName, DataSetSource dataSetSource, String chr, int startPos, int endPos) throws IOException {

		BufferedWriter pedBW = null;
		try {
			FileWriter pedFW = new FileWriter(new File(exportDir.getPath(),
					dataSetName + "_chr" + chr + ".mped"));
			pedBW = new BufferedWriter(pedFW);

			// Iterate through all samples
			int sampleNb = 0;
			Iterator<GenotypesList> samplesGenotypesIt = dataSetSource.getSamplesGenotypesSource().iterator();
			for (SampleKey sampleKey : dataSetSource.getSamplesKeysSource()) {
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

				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Genotypes

				pedBW.append(familyId);
				pedBW.append(SEP);
				pedBW.append(sampleKey.getSampleId());
				pedBW.append(SEP);
				pedBW.append(fatherId);
				pedBW.append(SEP);
				pedBW.append(motherId);
				pedBW.append(SEP);
				pedBW.append(sexStr);

				// Iterate through current chrl markers
				XXX;
				rdMarkerSet.initMarkerIdSetMap(startPos, endPos);  XXX;
				rdMarkerSet.fillGTsForCurrentSampleIntoInitMap(sampleNb);
				int markerNb = 0;
				for (byte[] tempGT : samplesGenotypesIt.next()) {
					pedBW.append(SEP);
					pedBW.append((char) tempGT[0]);
					pedBW.append(SEP);
					pedBW.append((char) tempGT[1]);
					markerNb++;
				}

				pedBW.append("\n");
				pedBW.flush();

				sampleNb++;
			}
			log.info("Samples exported to chr{} MPED file: {}", chr, sampleNb);
		} finally {
			if (pedBW != null) {
				pedBW.close();
			}
		}
	}

	public void exportChromosomeToDat(File exportDir, String dataSetName, DataSetSource dataSetSource, List<String> chrMarkerRsIds, String chr, int startPos, int endPos) throws IOException {

		BufferedWriter datBW = null;
		try {
			FileWriter datFW = new FileWriter(new File(exportDir.getPath(),
					dataSetName + "_chr" + chr + ".dat"));
			datBW = new BufferedWriter(datFW);

			// DAT files
			//     "M" indicates a marker
			//     rs# or marker identifier

			// Iterate through current chr markers
			// INIT MARKERSET
			for (String rsId : chrMarkerRsIds) {
				datBW.append("M" + SEP);
				datBW.append(rsId);
				datBW.append("\n");
			}

			log.info("Markers exported to chr{} DAT file: {}", chr, (endPos + 1 - startPos));
		} finally {
			if (datBW != null) {
				datBW.close();
			}
		}
	}
}
