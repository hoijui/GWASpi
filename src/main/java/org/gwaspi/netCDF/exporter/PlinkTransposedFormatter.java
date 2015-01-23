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
import org.gwaspi.constants.ExportConstants;
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

public class PlinkTransposedFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkTransposedFormatter.class);

	@Override
	public void export(
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
				"format and export in the PLink-Flat-Transposed format");
		final ProcessInfo exportSamplesPI = new SubProcessInfo(
				exportPI,
				"export samples",
				"format and export sample infos");
		final ProcessInfo exportMarkersPI = new SubProcessInfo(
				exportPI,
				"export markers",
				"format and export marker infos & genotypes");

		final SuperProgressSource exportPS = new SuperProgressSource(exportPI);
		superProgressSource.replaceSubProgressSource(PLACEHOLDER_PS_EXPORT, exportPS, null);
		exportPS.setNewStatus(ProcessStatus.INITIALIZING);

		final IntegerProgressHandler exportMarkersPS = new IntegerProgressHandler(exportMarkersPI, 0, dataSetSource.getNumMarkers() - 1);
		exportPS.addSubProgressSource(exportMarkersPS, 0.95);

		final IntegerProgressHandler exportSamplesPS = new IntegerProgressHandler(exportSamplesPI, 0, dataSetSource.getNumSamples() - 1);
		exportPS.addSubProgressSource(exportSamplesPS, 0.05);

		final File exportDir = Utils.checkDirPath(exportPath);

		String sep = ExportConstants.SEPARATOR_PLINK;

		//<editor-fold defaultstate="expanded" desc="TPED FILE">
		exportMarkersPS.setNewStatus(ProcessStatus.INITIALIZING);
		BufferedWriter tpedBW = null;
		try {
			FileWriter tpedFW = new FileWriter(new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".tped"));
			tpedBW = new BufferedWriter(tpedFW);
			// TPED files:
			// chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			// rs# or snp identifier
			// Genetic distance (morgans)
			// Base-pair position (bp units)
			// Genotypes

			Iterator<GenotypesList> markersGenotypesIt = dataSetSource.getMarkersGenotypesSource().iterator();
			int markerIndex = 0;
			exportMarkersPS.setNewStatus(ProcessStatus.RUNNING);
			for (MarkerMetadata curMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
				tpedBW.write(curMarkerMetadata.getChr());
				tpedBW.write(sep);
				tpedBW.write(curMarkerMetadata.getRsId()); // XXX Maybe has to be marker-id instead?
				tpedBW.write(sep);
				tpedBW.write('0'); // DEFAULT GENETIC DISTANCE
				tpedBW.write(sep);
				tpedBW.write(Integer.toString(curMarkerMetadata.getPos())); // NOTE This conversion is required, because Writer#write(int) actually writes a char, not the int value.

				// iterate through each samples genotype for the current marker
				for (byte[] tempGT : markersGenotypesIt.next()) {
					tpedBW.write(sep);
					tpedBW.write((char) tempGT[0]);
					tpedBW.write(sep);
					tpedBW.write((char) tempGT[1]);
				}

				tpedBW.write('\n');
				exportMarkersPS.setProgress(markerIndex);
				markerIndex++;
			}
			exportMarkersPS.setNewStatus(ProcessStatus.FINALIZING);

			log.info("Markers exported to tped: {}",
					dataSetSource.getMarkersGenotypesSource().size());
		} finally {
			if (tpedBW != null) {
				tpedBW.close();
			}
		}
		exportMarkersPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="TFAM FILE">
		exportSamplesPS.setNewStatus(ProcessStatus.INITIALIZING);
		BufferedWriter tfamBW = null;
		try {
			FileWriter tfamFW = new FileWriter(new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".tfam"));
			tfamBW = new BufferedWriter(tfamFW);

			// Iterate through all samples
			int sampleNb = 0;
			exportSamplesPS.setNewStatus(ProcessStatus.RUNNING);
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();

				// TFAM files
				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection

				tfamBW.append(familyId);
				tfamBW.append(sep);
				tfamBW.append(sampleInfo.getSampleId());
				tfamBW.append(sep);
				tfamBW.append(fatherId);
				tfamBW.append(sep);
				tfamBW.append(motherId);
				tfamBW.append(sep);
				tfamBW.append(sex);
				tfamBW.append(sep);
				tfamBW.append(affection);

				tfamBW.append("\n");
				tfamBW.flush();

				exportSamplesPS.setProgress(sampleNb);
				sampleNb++;
			}
		} finally {
			if (tfamBW != null) {
				tfamBW.close();
			}
		}
		exportSamplesPS.setNewStatus(ProcessStatus.COMPLEETED);
		//</editor-fold>
		exportPS.setNewStatus(ProcessStatus.COMPLEETED);
	}
}
