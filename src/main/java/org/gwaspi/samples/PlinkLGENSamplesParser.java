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

public class PlinkLGENSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(PlinkLGENSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		while (inputBufferReader.ready()) {
			String l = inputBufferReader.readLine();
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
		}
		log.info("Parsed {} Samples in LGEN file {}...",
				sampleInfos.size(), sampleFile.getName());

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfos;
	}
}
