package org.gwaspi.netCDF.loader;

import java.io.IOException;
import ucar.ma2.InvalidRangeException;

public interface GTFilesLoader {

	// PROCESS GENOTYPES
	int processData() throws IOException, InvalidRangeException, InterruptedException;
}
