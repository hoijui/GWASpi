package org.gwaspi.samples;

import org.gwaspi.model.SampleInfo;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 * @deprecated
 */
public class DummySampleInfo {

	private DummySampleInfo() {
	}

	public static SampleInfo createDummySampleInfo() {

		SampleInfo dummySampleinfo = new SampleInfo();

		return dummySampleinfo;
	}
}
