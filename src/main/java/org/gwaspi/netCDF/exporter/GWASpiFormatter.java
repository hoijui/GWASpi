package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cExport;
import org.gwaspi.gui.utils.Dialogs;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.samples.SampleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiFormatter implements Formatter {

	private final Logger log = LoggerFactory.getLogger(GWASpiFormatter.class);

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
		String sep = cExport.separator_SAMPLE_INFO;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		try {

			//<editor-fold defaultstate="expanded" desc="SAMPLE INFO FILE">
			FileWriter sampleInfoFW = new FileWriter(exportDir.getPath() + "/SampleInfo_" + rdMatrixMetadata.getMatrixFriendlyName() + ".txt");
			BufferedWriter sampleInfoBW = new BufferedWriter(sampleInfoFW);

			sampleInfoBW.append("FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tCategory\tDesease\tPopulation\tAge");
			sampleInfoBW.append("\n");

			//Iterate through all samples
			int sampleNb = 0;
			for (SampleKey sampleKey : rdSampleSetMap.keySet()) {
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

				SampleInfo sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleKey, rdMatrixMetadata.getStudyId());

				String familyId = sampleInfo.getFamilyId();
				String fatherId = sampleInfo.getFatherId();
				String motherId = sampleInfo.getMotherId();
				String sex = sampleInfo.getSexStr();
				String affection = sampleInfo.getAffectionStr();
				String category = sampleInfo.getCategory();
				String desease = sampleInfo.getDisease();
				String population = sampleInfo.getPopulation();
				int age = sampleInfo.getAge();

				StringBuilder line = new StringBuilder();
				line.append(familyId);
				line.append(sep);
				line.append(sampleKey.getSampleId());
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

				sampleNb++;
				if (sampleNb % 100 == 0) {
					log.info("Samples exported: {}", sampleNb);
				}
			}
			sampleInfoBW.close();
			sampleInfoFW.close();
			//</editor-fold>

			//<editor-fold defaultstate="expanded" desc="GWASpi netCDF MATRIX">
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
