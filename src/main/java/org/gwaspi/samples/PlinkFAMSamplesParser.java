package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlinkFAMSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkFAMSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = new String[10];
			String[] sampleInfoVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

			String sex = (sampleInfoVals[4].equals("-9")) ? "0" : sampleInfoVals[4];
			String affection = (sampleInfoVals[5].equals("-9")) ? "0" : sampleInfoVals[5];

			cVals[0] = sampleInfoVals[0]; //
			cVals[1] = sampleInfoVals[1]; //
			cVals[2] = sampleInfoVals[2]; //
			cVals[3] = sampleInfoVals[3]; //
			cVals[4] = sex; //
			cVals[5] = affection; //
			cVals[6] = "0"; //
			cVals[7] = "0";
			cVals[8] = "0";
			cVals[9] = "0"; // AGE

			sampleInfoMap.put(cVals[cImport.Annotation.GWASpi.sampleId], cVals);

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
