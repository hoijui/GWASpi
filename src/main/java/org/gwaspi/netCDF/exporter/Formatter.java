package org.gwaspi.netCDF.exporter;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.SampleKey;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.samples.SampleSet;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
interface Formatter {

	boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet_opt rdMarkerSet,
			SampleSet rdSampleSet,
			Map<SampleKey, Object> rdSampleSetMap,
			String phenotype)
			throws IOException;
}
