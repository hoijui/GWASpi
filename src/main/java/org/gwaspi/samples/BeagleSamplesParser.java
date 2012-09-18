package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeagleSamplesParser implements SamplesParser {

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = "";
		String affectionHeader = "";
		boolean gotAffection = false;
		while (!gotAffection) {
			String l = inputBufferReader.readLine();
			if (l == null) {
				break;
			}
			if (l.startsWith("I")) {
				sampleIdHeader = l;
			}
			if (l.startsWith("A")) {
				affectionHeader = l;
				gotAffection = true;
			}
		}

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		String[] beagleAffections = affectionHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleAffections.length; i++) {
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[cImport.Annotation.GWASpi.sampleId] = sampleIds[i];
			infoVals[cImport.Annotation.GWASpi.affection] = beagleAffections[i];
			sampleInfoMap.put(sampleIds[i], infoVals);
		}

		inputFileReader.close();

		return sampleInfoMap;
	}
}
