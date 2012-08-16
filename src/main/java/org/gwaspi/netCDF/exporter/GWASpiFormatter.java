/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.*;
import org.gwaspi.samples.SampleManager;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class GWASpiFormatter {

	public static boolean exportGWASpiFiles(String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet rdMarkerSet,
			SampleSet rdSampleSet,
			LinkedHashMap rdMarkerIdSetLHM,
			LinkedHashMap rdSampleSetLHM) throws IOException {
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		String sep = org.gwaspi.constants.cExport.separator_SAMPLE_INFO;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		try {

			//<editor-fold defaultstate="collapsed" desc="SAMPLE INFO FILE">
			FileWriter sampleInfoFW = new FileWriter(exportDir.getPath() + "/SampleInfo_" + rdMatrixMetadata.getMatrixFriendlyName() + ".txt");
			BufferedWriter sampleInfoBW = new BufferedWriter(sampleInfoFW);

			sampleInfoBW.append("FamilyID\tSampleID\tFatherID\tMotherID\tSex\tAffection\tCategory\tDesease\tPopulation\tAge");
			sampleInfoBW.append("\n");

			//Iterate through all samples
			int sampleNb = 0;
			for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
				StringBuilder line = new StringBuilder();
				String sampleId = it.next().toString();

//                FamilyID
//                SampleID
//                FatherID
//                MotherID
//                Sex
//                Affection
//                Category
//                Desease
//                Population
//                Age

				HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				if (!sampleInfo.isEmpty()) {
					String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
					String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
					String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
					String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
					String affection = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();
					String category = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_CATEGORY).toString();
					String desease = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_DISEASE).toString();
					String population = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_POPULATION).toString();
					String age = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AGE).toString();

					line = new StringBuilder();
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
					System.out.println("Samples exported:" + sampleNb);
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
			} catch (IOException ex) {
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
				Logger.getLogger(GWASpiFormatter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception ex) {
				org.gwaspi.gui.utils.Dialogs.showWarningDialogue("A table saving error has occurred");
				Logger.getLogger(GWASpiFormatter.class.getName()).log(Level.SEVERE, null, ex);
			}

			//</editor-fold>

			result = true;
		} catch (IOException iOException) {
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}

		return result;
	}
}
