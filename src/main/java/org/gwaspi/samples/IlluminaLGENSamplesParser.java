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

public class IlluminaLGENSamplesParser implements SamplesParser {

	private final static Logger log
			= LoggerFactory.getLogger(IlluminaLGENSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath, false);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			FileReader inputFileReader = new FileReader(gtFilesToImport[i]);
			BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

			boolean gotHeader = false;
			while (!gotHeader && inputBufferReader.ready()) {
				String header = inputBufferReader.readLine();
				if (header.startsWith("[Data]")) {
					/*header = */inputBufferReader.readLine(); // Get next line which is real header
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

				if (sampleInfoMap.size() % 100 == 0) {
					log.info("Parsed {} Samples...", sampleInfoMap.size());
				}
			}
			log.info("Parsed {} Samples in LGEN file {}...",
					sampleInfoMap.size(), gtFilesToImport[i].getName());

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfoMap;
	}
}
