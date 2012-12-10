package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;

public class BeagleSamplesParser implements SamplesParser {

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		FileReader inputFileReader;
		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		BufferedReader inputBufferReader = new BufferedReader(inputFileReader);

		String sampleIdHeader = "";
		String affectionHeader = "";
		boolean gotAffection = false;
		while (!gotAffection) {
			String l = inputBufferReader.readLine();
			if (l == null) {
				break;
			}
			if (l.startsWith("I")) {
				sampleIdHeader = l;
			}
			if (l.startsWith("A")) {
				affectionHeader = l;
				gotAffection = true;
			}
		}

		String[] sampleIds = sampleIdHeader.split(cImport.Separators.separators_SpaceTab_rgxp);
		String[] beagleAffections = affectionHeader.split(cImport.Separators.separators_SpaceTab_rgxp);

		for (int i = 2; i < beagleAffections.length; i++) {
			SampleInfo sampleInfo = new SampleInfo(
					sampleIds[i],
					"0",
					"0",
					"0",
					SampleInfo.Sex.UNKNOWN,
					SampleInfo.Affection.parse(beagleAffections[i]),
					"0",
					"0",
					"0",
					0
					);
			sampleInfos.add(sampleInfo);
		}

		inputFileReader.close();

		return sampleInfos;
	}
}
