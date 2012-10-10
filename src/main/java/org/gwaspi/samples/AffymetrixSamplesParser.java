package org.gwaspi.samples;

import org.gwaspi.constants.cImport;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AffymetrixSamplesParser implements SamplesParser {

	@Override
	public Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException {

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
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
			String[] infoVals = new String[]{"0", "0", "0", "0", "0", "0", "0", "0", "0", "0"};
			infoVals[cImport.Annotation.GWASpi.sampleId] = sampleId;
			resultMap.put(sampleId, infoVals);
		}

		return resultMap;
	}
}
