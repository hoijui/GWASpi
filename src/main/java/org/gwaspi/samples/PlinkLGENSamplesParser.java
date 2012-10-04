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

public class PlinkLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkLGENSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
			String[] infoVals = new String[]{cVals[cImport.Annotation.Plink_LGEN.lgen_familyId],
				cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId],
				"0", "0", "0", "0", "0", "0", "0", "0"};

			sampleInfoMap.put(cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId], infoVals);
		}
		log.info("Parsed {} Samples in LGEN file {}...",
				sampleInfoMap.size(), sampleFile.getName());

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}
}
