package org.gwaspi.samples;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.gwaspi.model.SampleInfo;

public class AffymetrixSamplesParser implements SamplesParser {

	@Override
	public Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException {

		Collection<SampleInfo> sampleInfos = new LinkedList<SampleInfo>();

		File[] gtFilesToImport = org.gwaspi.global.Utils.listFiles(sampleInfoPath);

		for (int i = 0; i < gtFilesToImport.length; i++) {
			String l = gtFilesToImport[i].getName();
			String sampleId;
			int end = l.lastIndexOf(".birdseed-v2");
			if (end != -1) {
				sampleId = l.substring(0, end);
			} else {
				sampleId = l.substring(0, l.lastIndexOf('.'));
			}
			SampleInfo sampleInfo = new SampleInfo(sampleId);
			sampleInfos.add(sampleInfo);
		}

		return sampleInfos;
	}
}
