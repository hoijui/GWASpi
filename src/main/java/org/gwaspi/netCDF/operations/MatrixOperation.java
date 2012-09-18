package org.gwaspi.netCDF.operations;

import java.io.IOException;
import ucar.ma2.InvalidRangeException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public interface MatrixOperation {

	public int processMatrix() throws IOException, InvalidRangeException;
}
