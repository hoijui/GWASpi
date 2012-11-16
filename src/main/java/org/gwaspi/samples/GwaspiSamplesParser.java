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

public class GwaspiSamplesParser implements SamplesParser {

	private static final Logger log
			= LoggerFactory.getLogger(GwaspiSamplesParser.class);

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
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
				sampleInfos.add(new SampleInfo(
						Integer.MIN_VALUE,
						cVals[cImport.Annotation.GWASpi.sampleId],
						cVals[cImport.Annotation.GWASpi.familyId],
						cVals[cImport.Annotation.GWASpi.fatherId],
						cVals[cImport.Annotation.GWASpi.motherId],
						SampleInfo.Sex.values()[Integer.parseInt(cVals[cImport.Annotation.GWASpi.sex])],
						SampleInfo.Affection.values()[Integer.parseInt(cVals[cImport.Annotation.GWASpi.affection])],
						cVals[cImport.Annotation.GWASpi.category],
						cVals[cImport.Annotation.GWASpi.disease],
						cVals[cImport.Annotation.GWASpi.population],
						Integer.parseInt(cVals[cImport.Annotation.GWASpi.age]),
						"",
						"",
						Integer.MIN_VALUE,
						Integer.MIN_VALUE
						));
			}

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
