/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.netCDF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
import ucar.ma2.*;
import ucar.nc2.*;

public class CreateNetcdf {

	private CreateNetcdf() {
	}

	public static void main() throws InvalidRangeException, IOException {
		setDimsAndAttributes(0,
				"INTERNAL",
				"testing netCDF reading and writing behaviour",
				"+",
				10,
				100);
	}

	public static NetcdfFileWriteable setDimsAndAttributes(Integer studyId,
			String technology,
			String description,
			String strand,
			int sampleSetSize,
			int markerSetSize)
			throws InvalidRangeException, IOException
	{
		// CREATE netCDF-3 FILE
		String genotypesFolder = "/media/data/work/GWASpi/genotypes";
		File pathToStudy = new File(genotypesFolder + "/STUDY_1");
		int gtSpan = cNetCDF.Strides.STRIDE_GT;
		int markerSpan = cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleSpan = cNetCDF.Strides.STRIDE_SAMPLE_NAME;

		String matrixName = "prototype";
		String writeFileName = pathToStudy + "/" + matrixName + ".nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

		// add dimensions
		Dimension samplesDim = ncfile.addDimension("samples", sampleSetSize);
		Dimension markersDim = ncfile.addDimension("markers", markerSetSize);
		Dimension gtSpanDim = ncfile.addDimension("span", gtSpan);
		Dimension markersSpanDim = ncfile.addDimension("markerSpan", markerSpan);
		ArrayList<Dimension> dims = new ArrayList<Dimension>();
		dims.add(samplesDim);
		dims.add(markersDim);
		dims.add(gtSpanDim);

		List<Dimension> markerGenotypeDims = new ArrayList<Dimension>();
		markerGenotypeDims.add(markersDim);
		markerGenotypeDims.add(markersSpanDim);

		List<Dimension> markerPositionDim = new ArrayList<Dimension>();
		markerPositionDim.add(markersDim);

		List<Dimension> markerPropertyDim32 = new ArrayList<Dimension>();
		markerPropertyDim32.add(markersDim);
		markerPropertyDim32.add(ncfile.addDimension("dim32", 32));

		List<Dimension> markerPropertyDim16 = new ArrayList<Dimension>();
		markerPropertyDim16.add(markersDim);
		markerPropertyDim16.add(ncfile.addDimension("dim16", 16));

		List<Dimension> markerPropertyDim8 = new ArrayList<Dimension>();
		markerPropertyDim8.add(markersDim);
		markerPropertyDim8.add(ncfile.addDimension("dim8", 8));

		List<Dimension> markerPropertyDim2 = new ArrayList<Dimension>();
		markerPropertyDim2.add(markersDim);
		markerPropertyDim2.add(ncfile.addDimension("dim2", 2));

		List<Dimension> markerPropertyDim1 = new ArrayList<Dimension>();
		markerPropertyDim1.add(markersDim);
		markerPropertyDim1.add(ncfile.addDimension("dim1", 1));

		List<Dimension> sampleSetDims = new ArrayList<Dimension>();
		sampleSetDims.add(samplesDim);
		sampleSetDims.add(markersSpanDim);

		// Define Marker Variables
		ncfile.addVariable("markerset", DataType.CHAR, markerGenotypeDims);
		ncfile.addVariableAttribute("markerset", cNetCDF.Attributes.LENGTH, markerSetSize);

		ncfile.addVariable("marker_chromosome", DataType.CHAR, markerPropertyDim8);
		ncfile.addVariable("marker_position", DataType.CHAR, markerPropertyDim32);
		ncfile.addVariable("marker_position_int", DataType.INT, markerPositionDim);
		ncfile.addVariable("marker_strand", DataType.CHAR, markerPropertyDim8);

		ncfile.addVariable("marker_property_1", DataType.CHAR, markerPropertyDim1);
		ncfile.addVariable("marker_property_2", DataType.CHAR, markerPropertyDim2);
		ncfile.addVariable("marker_property_8", DataType.CHAR, markerPropertyDim8);
		ncfile.addVariable("marker_property_16", DataType.CHAR, markerPropertyDim16);
		ncfile.addVariable("marker_property_32", DataType.CHAR, markerPropertyDim32);

		// Define Sample Variables
		ncfile.addVariable("sampleset", DataType.CHAR, sampleSetDims);
		ncfile.addVariableAttribute("sampleset", cNetCDF.Attributes.LENGTH, sampleSetSize);

		// Define Genotype Variables
		ncfile.addVariable("genotypes", DataType.CHAR, dims);
		ncfile.addVariableAttribute("genotypes", cNetCDF.Attributes.GLOB_STRAND, StrandType.PLSMIN.toString());

		// add global attributes
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_STUDY, studyId);
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_TECHNOLOGY, "INTERNAL");
		ncfile.addGlobalAttribute(cNetCDF.Attributes.GLOB_DESCRIPTION, "Matrix created by MOAPI through addition of 2 matrices");

		return ncfile;

	}
}
