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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.gwaspi.constants.cExport;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.GenotypesList;
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.progress.IntegerProgressHandler;
import org.gwaspi.progress.ProcessInfo;
import org.gwaspi.progress.ProcessStatus;
import org.gwaspi.progress.SubProcessInfo;
import org.gwaspi.progress.SuperProgressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports a Sample-Infos, Marker-Infos & Genotypes data-set to the Mach format.
 * NOTE This only works correctly if the markers in the source data-set to be exported
 *   are ordered by chromosome.
 */
public class MachFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(MachFormatter.class);
	private static final String SEP = cExport.separator_MACH;

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
				"export data per chromosome in the Mach format");

		final IntegerProgressHandler exportPS = new IntegerProgressHandler(exportPI, 0, dataSetSource.getNumChromosomes() - 1);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_EXPORT, exportPS, null);
		exportPS.setNewStatus(ProcessStatus.INITIALIZING);

		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		List<Iterator<byte[]>> samplesGenotypesIterators = new ArrayList<Iterator<byte[]>>(dataSetSource.getSamplesGenotypesSource().size());
		for (GenotypesList sampleGenotypesList : dataSetSource.getSamplesGenotypesSource()) {
			samplesGenotypesIterators.add(sampleGenotypesList.iterator());
		}

		// FIND START AND END MARKERS BY CHROMOSOME
		String tmpChr = "";
		int start = 0;
		int end = 0;
		String dataSetName = rdDataSetMetadata.getFriendlyName();
		List<String> chrMarkerRsIds = new LinkedList<String>();
		int chromosomeIndex = 0;
		exportPS.setNewStatus(ProcessStatus.RUNNING);
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
					exportChromosomeToMped(exportDir, dataSetName, dataSetSource, samplesGenotypesIterators, tmpChr, start, end - 1);
					exportChromosomeToDat(exportDir, dataSetName, dataSetSource, chrMarkerRsIds, tmpChr, start, end - 1);
					start = end;
					chrMarkerRsIds.clear();
					exportPS.setProgress(chromosomeIndex);
					chromosomeIndex++;
				}
				tmpChr = chr;
			}
			end++;
		}
		exportChromosomeToMped(exportDir, dataSetName, dataSetSource, samplesGenotypesIterators, tmpChr, start, end);
		exportChromosomeToDat(exportDir, dataSetName, dataSetSource, chrMarkerRsIds, tmpChr, start, end - 1);
		exportPS.setProgress(chromosomeIndex);
		chromosomeIndex++;
		exportPS.setNewStatus(ProcessStatus.COMPLEETED);

		return true;
	}

	private void exportChromosomeToMped(File exportDir, String dataSetName, DataSetSource dataSetSource, List<Iterator<byte[]>> samplesGenotypesIterators, String chr, int startPos, int endPos) throws IOException {

		BufferedWriter pedBW = null;
		try {
			FileWriter pedFW = new FileWriter(new File(exportDir.getPath(),
					dataSetName + "_chr" + chr + ".mped"));
			pedBW = new BufferedWriter(pedFW);

			// Iterate through all samples
			int sampleNb = 0;
			Iterator<Iterator<byte[]>> samplesGenotypesIt = samplesGenotypesIterators.iterator();
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

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
				pedBW.append(sampleInfo.getSampleId());
				pedBW.append(SEP);
				pedBW.append(fatherId);
				pedBW.append(SEP);
				pedBW.append(motherId);
				pedBW.append(SEP);
				pedBW.append(sexStr);

				// Iterate through current chrl markers
				int markerNb = 0;
				Iterator<byte[]> sampleGenotypesIt = samplesGenotypesIt.next();
				for (int ip = startPos; ip < endPos; ip++) {
					byte[] tempGT = sampleGenotypesIt.next();
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
