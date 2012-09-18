package org.gwaspi.samples;

import java.io.IOException;
import java.util.Map;

public interface SamplesParser {

	Map<String, Object> scanSampleInfo(String sampleInfoPath) throws IOException;
}
