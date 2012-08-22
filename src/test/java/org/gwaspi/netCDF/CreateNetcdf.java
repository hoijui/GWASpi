package org.gwaspi.netCDF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import ucar.ma2.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
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
			int markerSetSize) throws InvalidRangeException, IOException {

		///////////// CREATE netCDF-3 FILE ////////////
		String genotypesFolder = "/media/data/work/GWASpi/genotypes";
		File pathToStudy = new File(genotypesFolder + "/STUDY_1");
		int gtSpan = org.gwaspi.constants.cNetCDF.Strides.STRIDE_GT;
		int markerSpan = org.gwaspi.constants.cNetCDF.Strides.STRIDE_MARKER_NAME;
		int sampleSpan = org.gwaspi.constants.cNetCDF.Strides.STRIDE_SAMPLE_NAME;

		String matrixName = "prototype";
		String writeFileName = pathToStudy + "/" + matrixName + ".nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(writeFileName, false);

		// add dimensions
		Dimension samplesDim = ncfile.addDimension("samples", sampleSetSize);
		Dimension markersDim = ncfile.addDimension("markers", markerSetSize);
		Dimension gtSpanDim = ncfile.addDimension("span", gtSpan);
		ArrayList dims = new ArrayList();
		dims.add(samplesDim);
		dims.add(markersDim);
		dims.add(gtSpanDim);

		ArrayList markerGenotypeDims = new ArrayList();
		markerGenotypeDims.add(markersDim);
		markerGenotypeDims.add(markerSpan);

		ArrayList markerPositionDim = new ArrayList();
		markerPositionDim.add(markersDim);

		ArrayList markerPropertyDim32 = new ArrayList();
		markerPropertyDim32.add(markersDim);
		markerPropertyDim32.add(32);

		ArrayList markerPropertyDim16 = new ArrayList();
		markerPropertyDim16.add(markersDim);
		markerPropertyDim16.add(16);

		ArrayList markerPropertyDim8 = new ArrayList();
		markerPropertyDim8.add(markersDim);
		markerPropertyDim8.add(8);

		ArrayList markerPropertyDim2 = new ArrayList();
		markerPropertyDim2.add(markersDim);
		markerPropertyDim2.add(2);

		ArrayList markerPropertyDim1 = new ArrayList();
		markerPropertyDim1.add(markersDim);
		markerPropertyDim1.add(1);

		ArrayList sampleSetDims = new ArrayList();
		sampleSetDims.add(samplesDim);
		sampleSetDims.add(sampleSpan);

		// Define Marker Variables
		ncfile.addVariable("markerset", DataType.CHAR, markerGenotypeDims);
		ncfile.addVariableAttribute("markerset", org.gwaspi.constants.cNetCDF.Attributes.LENGTH, markerSetSize);

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
		ncfile.addVariableAttribute("sampleset", org.gwaspi.constants.cNetCDF.Attributes.LENGTH, sampleSetSize);

		// Define Genotype Variables
		ncfile.addVariable("genotypes", DataType.CHAR, dims);
		ncfile.addVariableAttribute("genotypes", org.gwaspi.constants.cNetCDF.Attributes.GLOB_STRAND, "+/-");

		// add global attributes
		ncfile.addGlobalAttribute(org.gwaspi.constants.cNetCDF.Attributes.GLOB_STUDY, studyId);
		ncfile.addGlobalAttribute(org.gwaspi.constants.cNetCDF.Attributes.GLOB_TECHNOLOGY, "INTERNAL");
		ncfile.addGlobalAttribute(org.gwaspi.constants.cNetCDF.Attributes.GLOB_DESCRIPTION, "Matrix created by MOAPI through addition of 2 matrices");

		return ncfile;

	}
}
