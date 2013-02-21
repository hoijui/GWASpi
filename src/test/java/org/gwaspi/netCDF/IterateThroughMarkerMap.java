package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.MarkerKey;
import org.gwaspi.model.SampleKey;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class IterateThroughMarkerMap {

	private Map<MarkerKey, Object> basesMap = new LinkedHashMap<MarkerKey, Object>();
	private Map<SampleKey, Object> rdSampleSetMap = new LinkedHashMap<SampleKey, Object>();
	private Map<SampleKey, Object> wrSampleSetMap = new LinkedHashMap<SampleKey, Object>();
	private SampleSet rdSampleSet = null;

	public IterateThroughMarkerMap() throws IOException {
		// Iterate through pmAllelesAndStrandsMap, use marker item position to read correct GTs from all Samples into rdMarkerIdSetMap.
		int markerNb = 0;
		NetcdfFile rdNcFile = NetcdfFile.open("pathToMatrix");
		for (Map.Entry<MarkerKey, Object> entry : basesMap.entrySet()) {
			MarkerKey markerKey = entry.getKey();
			String bases = entry.getValue().toString();

			// Get alleles from read matrix
			rdSampleSetMap = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToMap(rdNcFile, rdSampleSetMap, markerNb);

			markerNb++;
		}
	}
}
