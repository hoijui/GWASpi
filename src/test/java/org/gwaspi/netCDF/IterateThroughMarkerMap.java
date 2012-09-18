package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class IterateThroughMarkerMap {

	private Map<String, Object> basesMap = new LinkedHashMap<String, Object>();
	private Map<String, Object> rdSampleSetMap = new LinkedHashMap<String, Object>();
	private Map<String, Object> wrSampleSetMap = new LinkedHashMap<String, Object>();
	private SampleSet rdSampleSet = null;

	public IterateThroughMarkerMap() throws IOException {
		// Iterate through pmAllelesAndStrandsMap, use marker item position to read correct GTs from all Samples into rdMarkerIdSetMap.
		int markerNb = 0;
		NetcdfFile rdNcFile = NetcdfFile.open("pathToMatrix");
		for (Map.Entry<String, Object> entry : basesMap.entrySet()) {
			String markerId = entry.getKey();
			String bases = entry.getValue().toString();

			// Get alleles from read matrix
			rdSampleSetMap = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);

			markerNb++;
		}
	}
}
