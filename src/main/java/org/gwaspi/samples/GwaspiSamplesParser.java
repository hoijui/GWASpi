package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GwaspiSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(GwaspiSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
		BufferedReader inputBufferReader;
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String[] cVals = new String[10];
			if (count == 0) {
				inputBufferReader.readLine(); // Skip header
			} else {
				int i = 0;
				for (String field : inputBufferReader.readLine().split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10)) {
					cVals[i] = field;
					i++;
				}
				sampleInfoMap.put(cVals[cImport.Annotation.GWASpi.sampleId], cVals);
			}

			count++;
			if (count % 100 == 0) {
				log.info("Parsed {} Samples for info...", count);
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}
}
