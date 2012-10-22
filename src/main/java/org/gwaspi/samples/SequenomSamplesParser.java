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

public class SequenomSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(SequenomSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {
		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File gtFileToImport = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(gtFileToImport);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String l;
		while (inputBufferReader.ready()) {
			l = inputBufferReader.readLine();
			if (!l.contains("SAMPLE_ID")) { //SKIP ALL HEADER LINES
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				if (!sampleInfoMap.containsKey(cVals[cImport.Annotation.Sequenom.sampleId])) {
					String[] infoVals = new String[]{"0",
						cVals[cImport.Annotation.Sequenom.sampleId],
						"0", "0", "0", "0", "0", "0", "0", "0"};
					sampleInfoMap.put(cVals[cImport.Annotation.Sequenom.sampleId], infoVals);
				}

				if (sampleInfoMap.size() % 100 == 0) {
					log.info("Parsed {} lines...", sampleInfoMap.size());
				}
			}

		}
		log.info("Parsed {} Samples in Sequenom file {}...",
				sampleInfoMap.size(), gtFileToImport);

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}
}
