package org.gwaspi.netCDF.operations;

import java.io.IOException;
import org.gwaspi.model.SampleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class SampleOperationSet extends AbstractOperationSet<SampleKey> {

	private static final Logger log = LoggerFactory.getLogger(SampleOperationSet.class);

	public SampleOperationSet(int studyId, int opId) throws IOException {
		super(studyId, opId, SampleKey.KEY_FACTORY);
	}
}
