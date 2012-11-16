package org.gwaspi.samples;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.model.SampleInfo;

public interface SamplesParser {

	Collection<SampleInfo> scanSampleInfo(String sampleInfoPath) throws IOException;
}
