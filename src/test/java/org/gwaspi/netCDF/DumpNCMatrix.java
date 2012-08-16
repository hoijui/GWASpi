/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.netCDF;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class DumpNCMatrix {

	public static final void main(String[] args) throws Exception {
		printSystemOutMatrix("/media/data/work/GWASpi/genotypes/STUDY_8/GT_51310170721CEST.nc");
	}

	public static void printSystemOutMatrix(String matrixPath) throws IOException, InvalidRangeException {
		NetcdfFile ncfile = NetcdfFile.open(matrixPath);
		LinkedHashMap markerIdSetLHM = new LinkedHashMap();
		LinkedHashMap sampleIdSetLHM = new LinkedHashMap();

		FileWriter dumpFW = new FileWriter("/media/data/work/GWASpi/export/NCDump.txt");
		BufferedWriter dumpBW = new BufferedWriter(dumpFW);

		//GET MARKERSET
		Variable var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERSET);
		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();

		try {
			int markerSetSize = varShape[0];

			if (dataType == DataType.CHAR) {
				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
				markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(markerSetAC);
			}

		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
		}


		//GET SAMPLESET
		var = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_SAMPLESET);
		varShape = var.getShape();
		Dimension markerSetDim = ncfile.findDimension(org.gwaspi.constants.cNetCDF.Dimensions.DIM_SAMPLESET);

		try {
			int sampleSetSize = markerSetDim.getLength();
			ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");

			sampleIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMKeys(sampleSetAC);

		} catch (IOException ioe) {
			System.out.println("Cannot read data: " + ioe);
		} catch (InvalidRangeException e) {
			System.out.println("Cannot read data: " + e);
		}



		//BUILD MARKERSET HEADER
		StringBuilder headerSB = new StringBuilder();
		for (Iterator it = markerIdSetLHM.keySet().iterator(); it.hasNext();) {
			Object markerId = it.next();
			headerSB.append("\t" + markerId.toString());
		}
		dumpBW.append(headerSB.toString());
		dumpBW.append("\n");

		//ITERATE READ OF MARKERSETLHM BY SAMPLE
		int sampleNb = 0;
		for (Iterator it = sampleIdSetLHM.keySet().iterator(); it.hasNext();) {
			Object sampleId = it.next();
			StringBuilder sampleLineSB = new StringBuilder(sampleId.toString());

			Variable genotypes = ncfile.findVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_GENOTYPES);

			try {
				varShape = genotypes.getShape();

				ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read("(" + sampleNb + ":" + sampleNb + ":1, 0:" + (varShape[1] - 1) + ":1, 0:" + (varShape[2] - 1) + ":1)");
				ArrayChar.D2 gt_ACD2 = (ArrayChar.D2) gt_ACD3.reduce();

				markerIdSetLHM = org.gwaspi.netCDF.operations.Utils.writeD2ArrayCharToLHMValues(gt_ACD2, markerIdSetLHM);

				for (Iterator it2 = markerIdSetLHM.keySet().iterator(); it2.hasNext();) {
					Object markerId = it2.next();
					Object alleles = markerIdSetLHM.get(markerId);
					sampleLineSB.append("\t" + alleles);
				}

			} catch (IOException ioe) {
				System.out.println("Cannot read data: " + ioe);
			} catch (InvalidRangeException e) {
				System.out.println("Cannot read data: " + e);
			}

			dumpBW.append(sampleLineSB.toString());
			dumpBW.append("\n");
			sampleNb++;

		}

		dumpBW.close();
		dumpFW.close();

	}
}
