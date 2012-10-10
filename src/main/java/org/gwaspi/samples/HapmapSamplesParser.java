package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.netCDF.loader.LoadGTFromHapmapFiles;

public class HapmapSamplesParser implements SamplesParser {

	/**
	 * NOTE No affection state available
	 */
	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader fr = null;
		BufferedReader inputAnnotationBr = null;
		File hapmapGTFile = new File(sampleInfoPath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				fr = new FileReader(gtFilesToImport[i]);
				inputAnnotationBr = new BufferedReader(fr);

				String header = inputAnnotationBr.readLine();

				String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < hapmapVals.length; j++) {
					String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
					infoVals[cImport.Annotation.GWASpi.sampleId] = hapmapVals[j];
					sampleInfoMap.put(hapmapVals[j], infoVals);
				}
			}
		} else {
			fr = new FileReader(sampleInfoPath);
			inputAnnotationBr = new BufferedReader(fr);

			String header = inputAnnotationBr.readLine();

			String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
			for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
				String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
				infoVals[cImport.Annotation.GWASpi.sampleId] = hapmapVals[i];
				sampleInfoMap.put(hapmapVals[i], infoVals);
			}
		}

		inputAnnotationBr.close();
		fr.close();

		return sampleInfoMap;
	}
}
