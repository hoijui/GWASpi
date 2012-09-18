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

public class PlinkStandardSamplesParser implements SamplesParser {

	private final static Logger log
			= LoggerFactory.getLogger(PlinkStandardSamplesParser.class);

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> sampleInfoMap = new LinkedHashMap<String, Object>();
		FileReader inputFileReader;
		BufferedReader inputBufferReader;

		File sampleFile = new File(sampleInfoPath);
		inputFileReader = new FileReader(sampleFile);
		inputBufferReader = new BufferedReader(inputFileReader);

		char[] chunker = new char[300];
		inputBufferReader.read(chunker, 0, 300);
		if (String.valueOf(chunker).contains("\n")) { // SHORT PED FILE
			inputBufferReader.close();
			inputFileReader.close();
			inputFileReader = new FileReader(sampleFile);
			inputBufferReader = new BufferedReader(inputFileReader);

			int count = 0;
			while (inputBufferReader.ready()) {
				String l = inputBufferReader.readLine();
				String[] cVals = new String[10];
				if (chunker.length > 0) {
					cVals = l.split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sex = (cVals[4].equals("-9")) ? "0" : cVals[4];
					String affection = (cVals[5].equals("-9")) ? "0" : cVals[5];
					cVals[4] = sex;
					cVals[5] = affection;
					cVals[6] = "0"; //
					cVals[7] = "0";
					cVals[8] = "0";
					cVals[9] = "0"; // AGE
				}

				sampleInfoMap.put(cVals[cImport.Annotation.GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		} else { // LONG PED FILE
			// This has sucked out 1 week of my life and caused many grey hairs!
			int count = 0;
			while (inputBufferReader.ready()) {
				if (count != 0) {
					chunker = new char[300];
					inputBufferReader.read(chunker, 0, 300); // Read a sizable but conrolled chunk of data into memory
				}

				String[] cVals = new String[10];
				if (chunker.length > 0) {
					cVals = String.valueOf(chunker).split(cImport.Separators.separators_CommaSpaceTab_rgxp, 10);
					String sex = (cVals[4].equals("-9")) ? "0" : cVals[4];
					String affection = (cVals[5].equals("-9")) ? "0" : cVals[5];
					cVals[4] = sex;
					cVals[5] = affection;
					cVals[6] = "0"; //
					cVals[7] = "0";
					cVals[8] = "0";
					cVals[9] = "0"; // AGE
				}
				inputBufferReader.readLine(); // Read rest of line and discard it...

				sampleInfoMap.put(cVals[cImport.Annotation.GWASpi.sampleId], cVals);

				count++;
				if (count % 100 == 0) {
					log.info("Parsed {} Samples for info...", count);
				}
			}
		}

		inputBufferReader.close();
		inputFileReader.close();

		return sampleInfoMap;
	}
}
