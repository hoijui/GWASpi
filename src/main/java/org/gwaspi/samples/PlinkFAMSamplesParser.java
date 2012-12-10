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

public class PlinkFAMSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkFAMSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
		FileReader inputFileReader = new FileReader(new File(sampleInfoPath));
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		int count = 0;
		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
			String[] cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp);

			String sexStr = cVals[cImport.Annotation.Plink_Binary.ped_sex];
			sexStr = sexStr.equals("-9") ? "0" : sexStr;
			String affectionStr = cVals[cImport.Annotation.Plink_Binary.ped_affection];
			affectionStr = affectionStr.equals("-9") ? "0" : affectionStr;
			SampleInfo.Sex sex = SampleInfo.Sex.parse(sexStr);
			SampleInfo.Affection affection = SampleInfo.Affection.parse(affectionStr);
			SampleInfo sampleInfo = new SampleInfo(
					cVals[cImport.Annotation.Plink_Binary.ped_sampleId],
					cVals[cImport.Annotation.Plink_Binary.ped_familyId],
					cVals[cImport.Annotation.Plink_Binary.ped_fatherId],
					cVals[cImport.Annotation.Plink_Binary.ped_motherId],
					sex,
					affection,
					"0",
					"0",
					"0",
					0
					);

			sampleInfos.add(sampleInfo);

			count++;
			if (count % 100 == 0) {
				log.info("Parsed {} Samples for info...", count);
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfos;
	}
}
