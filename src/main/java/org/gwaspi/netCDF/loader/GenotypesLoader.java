package org.gwaspi.netCDF.loader;

import java.io.IOException;
import java.util.Collection;
import org.gwaspi.constants.cImport.ImportFormat;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import org.gwaspi.model.SampleInfo;
import ucar.ma2.InvalidRangeException;

public interface GenotypesLoader {

	/**
	 * Process Genotypes
	 */
	int processData(GenotypesLoadDescription loadDescription, Collection<SampleInfo> sampleInfo) throws IOException, InvalidRangeException, InterruptedException;

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
