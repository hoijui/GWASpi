package org.gwaspi.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.constants.cImport;
import org.gwaspi.model.SampleInfo;
import org.gwaspi.netCDF.loader.LoadGTFromHapmapFiles;

public class HapmapSamplesParser implements SamplesParser {

	/**
	 * NOTE No affection state available
	 */
	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();
		FileReader fr = null;
		BufferedReader inputAnnotationBr = null;
		File hapmapGTFile = new File(sampleInfoPath);
		if (hapmapGTFile.isDirectory()) {
			File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);
			for (int i = 0; i < gtFilesToImport.length; i++) {
				fr = new FileReader(gtFilesToImport[i]);
				inputAnnotationBr = new BufferedReader(fr);

				String header = inputAnnotationBr.readLine();

				String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
				for (int j = LoadGTFromHapmapFiles.Standard.sampleId; j < hapmapVals.length; j++) {
					SampleInfo sampleInfo = new SampleInfo(hapmapVals[j]);
					sampleInfos.add(sampleInfo);
				}
			}
		} else {
			fr = new FileReader(sampleInfoPath);
			inputAnnotationBr = new BufferedReader(fr);

			String header = inputAnnotationBr.readLine();

			String[] hapmapVals = header.split(cImport.Separators.separators_SpaceTab_rgxp);
			for (int i = LoadGTFromHapmapFiles.Standard.sampleId; i < hapmapVals.length; i++) {
				SampleInfo sampleInfo = new SampleInfo(hapmapVals[i]);
				sampleInfos.add(sampleInfo);
			}
		}

		inputAnnotationBr.close();
		fr.close();

		return sampleInfos;
	}
}
