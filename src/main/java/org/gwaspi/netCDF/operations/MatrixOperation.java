package org.gwaspi.netCDF.operations;

import java.io.IOException;
import ucar.ma2.InvalidRangeException;

public interface MatrixOperation {

	int processMatrix() throws IOException, InvalidRangeException;
}
