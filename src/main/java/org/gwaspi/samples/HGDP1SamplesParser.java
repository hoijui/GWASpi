package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HGDP1SamplesParser implements SamplesParser {

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = inputBufferReader.readLine();

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		for (int i = 1; i < sampleIds.length; i++) {
			String sampleId = sampleIds[i];
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[cImport.Annotation.GWASpi.sampleId] = sampleId;
			sampleInfoMap.put(sampleId, infoVals);
		}

		inputFileReader.close();

		return sampleInfoMap;
	}
}
