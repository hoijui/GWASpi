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
import org.gwaspi.constants.cExport;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.DataSetMetadata;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GWASpiFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(GWASpiFormatter.class);

	@Override
	public boolean export(
			String exportPath,
			DataSetMetadata rdDataSetMetadata,
			DataSetSource dataSetSource,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = cExport.separator_SAMPLE_INFO;
		BufferedWriter sampleInfoBW = null;
		try {
			//<editor-fold defaultstate="expanded" desc="SAMPLE INFO FILE">
			FileWriter sampleInfoFW = new FileWriter(new File(exportDir.getPath(),
					"SampleInfo_" + rdDataSetMetadata.getFriendlyName() + ".txt"));
			sampleInfoBW = new BufferedWriter(sampleInfoFW);

			sampleInfoBW.append("FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tCategory\tDesease\tPopulation\tAge");
			sampleInfoBW.append("\n");

			//Iterate through all samples
			int sampleNb = 0;
			for (SampleInfo sampleInfo : dataSetSource.getSamplesInfosSource()) {
//				FamilyID
//				SampleID
//				FatherID
//				MotherID
//				Sex
//				Affection
//				Category
//				Desease
//				Population
//				Age

				sampleInfo = org.gwaspi.netCDF.exporter.Utils.formatSampleInfo(sampleInfo);

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();
				String category = sampleInfo.getCategory();
				String desease = sampleInfo.getDisease();
				String population = sampleInfo.getPopulation();
				int age = sampleInfo.getAge();

				sampleInfoBW.write(familyId);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(sampleInfo.getSampleId());
				sampleInfoBW.write(sep);
				sampleInfoBW.write(fatherId);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(motherId);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(sex);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(affection);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(category);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(desease);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(population);
				sampleInfoBW.write(sep);
				sampleInfoBW.write(age);
				sampleInfoBW.write('\n');
				sampleInfoBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported: {}", sampleNb);
				}
			}
			//</editor-fold>
		} finally {
			if (sampleInfoBW != null) {
				sampleInfoBW.close();
			}
		}

		//<editor-fold defaultstate="expanded" desc="GWASpi netCDF MATRIX">
		try {
			File origFile = MatrixMetadata.generatePathToNetCdfFileGeneric(rdDataSetMetadata);
			File newFile = new File(exportDir.getPath(),
					rdDataSetMetadata.getFriendlyName() + ".nc");
			if (origFile.exists()) {
				org.gwaspi.global.Utils.copyFile(origFile, newFile);
			}

			result = true;
		} catch (Exception ex) {
			Dialogs.showWarningDialogue("A table saving error has occurred");
			log.error("A table saving error has occurred", ex);
		}
		//</editor-fold>

		return result;
	}
}
