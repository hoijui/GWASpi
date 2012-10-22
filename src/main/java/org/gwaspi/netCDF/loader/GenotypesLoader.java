package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Map;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import ucar.ma2.InvalidRangeException;

public interface GenotypesLoader {

	/**
	 * Process Genotypes
	 */
	int processData(GenotypesLoadDescription loadDescription, Map<String, Object> sampleInfo) throws IOException, InvalidRangeException, InterruptedException;

	ImportFormat getFormat();

	/**
	 * @return if <code>null</code>, then we will use
	 *   <code>GenotypesLoadDescription#getStrand()</code> instead
	 */
	StrandType getMatrixStrand();

	boolean isHasDictionary();

	int getMarkersD2ItemNb();

	String getMarkersD2Variables();
}
