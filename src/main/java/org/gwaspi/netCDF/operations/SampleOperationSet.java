package org.gwaspi.netCDF.operations;

import java.io.IOException;
import org.gwaspi.model.SampleKey;

public class SampleOperationSet<V> extends AbstractOperationSet<SampleKey, V> {

	public SampleOperationSet(int studyId, int opId) throws IOException {
		super(studyId, opId, SampleKey.KEY_FACTORY);
	}
}
