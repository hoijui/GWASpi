package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cDBSamples;
import org.gwaspi.constants.cExport;
import org.gwaspi.gui.utils.Dialogs;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(GWASpiFormatter.class);

	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet_opt rdMarkerSet,
			SampleSet rdSampleSet,
			Map<String, Object> rdSampleSetMap,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = cExport.separator_SAMPLE_INFO;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		try {

			//<editor-fold defaultstate="collapsed" desc="SAMPLE INFO FILE">
			FileWriter sampleInfoFW = new FileWriter(exportDir.getPath() + "/SampleInfo_" + rdMatrixMetadata.getMatrixFriendlyName() + ".txt");
			BufferedWriter sampleInfoBW = new BufferedWriter(sampleInfoFW);

			sampleInfoBW.append("FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tCategory\tDesease\tPopulation\tAge");
			sampleInfoBW.append("\n");

			//Iterate through all samples
			int sampleNb = 0;
			for (String sampleId : rdSampleSetMap.keySet()) {
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

				Map<String, Object> sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				if (!sampleInfo.isEmpty()) {
					String familyId = sampleInfo.get(cDBSamples.f_FAMILY_ID).toString();
					String fatherId = sampleInfo.get(cDBSamples.f_FATHER_ID).toString();
					String motherId = sampleInfo.get(cDBSamples.f_MOTHER_ID).toString();
					String sex = sampleInfo.get(cDBSamples.f_SEX).toString();
					String affection = sampleInfo.get(cDBSamples.f_AFFECTION).toString();
					String category = sampleInfo.get(cDBSamples.f_CATEGORY).toString();
					String desease = sampleInfo.get(cDBSamples.f_DISEASE).toString();
					String population = sampleInfo.get(cDBSamples.f_POPULATION).toString();
					String age = sampleInfo.get(cDBSamples.f_AGE).toString();

					StringBuilder line = new StringBuilder();
					line.append(familyId);
					line.append(sep);
					line.append(sampleId);
					line.append(sep);
					line.append(fatherId);
					line.append(sep);
					line.append(motherId);
					line.append(sep);
					line.append(sex);
					line.append(sep);
					line.append(affection);
					line.append(sep);
					line.append(category);
					line.append(sep);
					line.append(desease);
					line.append(sep);
					line.append(population);
					line.append(sep);
					line.append(age);

					sampleInfoBW.append(line);
					sampleInfoBW.append("\n");
					sampleInfoBW.flush();
				}

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported: {}", sampleNb);
				}
			}
			sampleInfoBW.close();
			sampleInfoFW.close();
			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="GWASpi netCDF MATRIX">
			try {
				File origFile = new File(rdMatrixMetadata.getPathToMatrix());
				File newFile = new File(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".nc");
				if (origFile.exists()) {
					org.gwaspi.global.Utils.copyFile(origFile, newFile);
				}
			} catch (Exception ex) {
				Dialogs.showWarningDialogue("A table saving error has occurred");
				log.error("A table saving error has occurred", ex);
			}
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
