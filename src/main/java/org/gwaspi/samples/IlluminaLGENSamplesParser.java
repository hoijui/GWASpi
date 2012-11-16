package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IlluminaLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(IlluminaLGENSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);

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
				SampleInfo sampleInfo = new SampleInfo(
						cVals[cImport.Annotation.Plink_LGEN.lgen_sampleId],
						cVals[cImport.Annotation.Plink_LGEN.lgen_familyId],
						"0",
						"0",
						SampleInfo.Sex.UNKNOWN,
						SampleInfo.Affection.UNKNOWN,
						"0",
						"0",
						"0",
						0
						);

				sampleInfos.add(sampleInfo);

				if (sampleInfos.size() % 100 == 0) {
					log.info("Parsed {} Samples...", sampleInfos.size());
				}
			}
			log.info("Parsed {} Samples in LGEN file {}...",
					sampleInfos.size(), gtFilesToImport[i].getName());

			inputBufferReader.close();
			inputFileReader.close();
		}

		return sampleInfos;
	}
}
