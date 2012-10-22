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

public class MultipleIlluminaLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(MultipleIlluminaLGENSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		File[] lgenFilesToScan = org.gwaspi.global.Utils.listFiles(sampleInfoPath);

		for (File currentLGENFile : lgenFilesToScan) {
			FileReader inputFileReader = new FileReader(currentLGENFile);
			BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

			boolean gotHeader = false;
			while (!gotHeader) {
				String header = inputBufferReader.readLine();
				if (header == null) {
					break;
				}
				if (header.startsWith("[Data]")) {
					/*header = */inputBufferReader.readLine(); // get the next line, which is the real header
					gotHeader = true;
				}
			}

			String l;
			while (inputBufferReader.ready()) {
				l = inputBufferReader.readLine();
				String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);
				String[] infoVals = new String[]{cVals[cImport.Annotation.Plink_LGEN.lgen_familyId],
					cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId],
					"0", "0", "0", "0", "0", "0", "0", "0"};

				sampleInfoMap.put(cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId], infoVals);
			}
			log.info("Parsed {} Samples in LGEN file {}...",
					sampleInfoMap.size(), currentLGENFile.getName());

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoMap;
	}
}
