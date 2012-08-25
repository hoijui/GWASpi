package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.Iterator;
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
public class IterateThroughMarkerLHM {

	private Map<String, Object> basesLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> rdSampleSetLHM = new LinkedHashMap<String, Object>();
	private Map<String, Object> wrSampleSetLHM = new LinkedHashMap<String, Object>();
	private SampleSet rdSampleSet = null;

	public IterateThroughMarkerLHM() throws IOException {
		// Iterate through pmAllelesAndStrandsLHM, use marker item position to read correct GTs from all Samples into rdMarkerIdSetLHM.
		int markerNb = 0;
		NetcdfFile rdNcFile = NetcdfFile.open("pathToMatrix");
		for (Iterator<String> it = basesLHM.keySet().iterator(); it.hasNext();) {
			String markerId = it.next();
			String bases = basesLHM.get(markerId).toString();

			// Get alleles from read matrix
			rdSampleSetLHM = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetLHM, markerNb);

			markerNb++;
		}
	}
}
