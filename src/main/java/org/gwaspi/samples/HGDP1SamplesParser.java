package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;

public class HGDP1SamplesParser implements SamplesParser {

	/**
	 * NOTE No affection state available
	 */
	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
		File sampleFile = new File(sampleInfoPath);
		FileReader inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = inputBufferReader.readLine();

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		for (int i = 1; i < sampleIds.length; i++) {
			String sampleId = sampleIds[i];
			sampleInfos.add(new SampleInfo(
					Integer.MIN_VALUE,
					sampleId,
					"0",
					"0",
					"0",
					SampleInfo.Sex.UNKNOWN,
					SampleInfo.Affection.UNKNOWN,
					"0",
					"0",
					"0",
					0,
					"",
					"",
					Integer.MIN_VALUE,
					Integer.MIN_VALUE
					));
		}

		inputFileReader.close();

		return sampleInfos;
	}
}
