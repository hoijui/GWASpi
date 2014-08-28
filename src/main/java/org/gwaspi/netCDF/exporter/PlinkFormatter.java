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
import org.gwaspi.model.MarkerMetadata;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(PlinkFormatter.class);

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
		String sep = cExport.separator_PLINK;
		String sepBig = cExport.separator_PLINK_big;

		//<editor-fold defaultstate="expanded" desc="PED FILE">
		BufferedWriter pedBW = null;
		try {
			FileWriter pedFW = new FileWriter(new File(exportDir.getPath(),
					rdMatrixMetadata.getFriendlyName() + ".ped"));
			pedBW = new BufferedWriter(pedFW);

			// Iterate through all samples
			int sampleNb = 0;
			Iterator<GenotypesList> samplesGenotypesIt = dataSetSource.getSamplesGenotypesSource().iterator();
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();

				// Family ID
				// Individual ID
				// Paternal ID
				// Maternal ID
				// Sex (1=male; 2=female; other=unknown)
				// Affection
				// Genotypes

				pedBW.append(familyId);
				pedBW.append(sepBig);

				pedBW.append(sampleInfo.getSampleId());
				pedBW.append(sep);
				pedBW.append(fatherId);
				pedBW.append(sep);
				pedBW.append(motherId);
				pedBW.append(sep);
				pedBW.append(sex);
				pedBW.append(sep);
				pedBW.append(affection);

				// Iterate through all markers
				for (byte[] tempGT : samplesGenotypesIt.next()) {
					pedBW.append(sepBig);
					pedBW.append((char) tempGT[0]);
					pedBW.append(sep);
					pedBW.append((char) tempGT[1]);
				}

				pedBW.append("\n");
				pedBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported to PED file: {}", sampleNb);
				}
			}
			log.info("Samples exported to PED file: {}", sampleNb);
		} finally {
			if (pedBW != null) {
				pedBW.close();
			}
		}
		//</editor-fold>

		//<editor-fold defaultstate="expanded" desc="MAP FILE">
		BufferedWriter mapBW = null;
		try {
			FileWriter mapFW = new FileWriter(new File(exportDir.getPath(),
					rdMatrixMetadata.getFriendlyName() + ".map"));
			mapBW = new BufferedWriter(mapFW);

			// MAP files
			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//     rs# or snp identifier
			//     Genetic distance (morgans)
			//     Base-pair position (bp units)

			for (MarkerMetadata curMarkerMetadata : dataSetSource.getMarkersMetadatasSource()) {
				mapBW.write(curMarkerMetadata.getChr());
				mapBW.write(sep);
				mapBW.write(curMarkerMetadata.getMarkerId());
				mapBW.write(sep);
				mapBW.write('0'); // DEFAULT GENETIC DISTANCE
				mapBW.write(sep);
				mapBW.write(String.valueOf(curMarkerMetadata.getPos()));
				mapBW.write('\n');
			}

			log.info("Markers exported to MAP file: {}",
					dataSetSource.getMarkersMetadatasSource().size());

			result = true;
		} finally {
			if (mapBW != null) {
				mapBW.close();
			}
		}
		//</editor-fold>

		return result;
	}
}
