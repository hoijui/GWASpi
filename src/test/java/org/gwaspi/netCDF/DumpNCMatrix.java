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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.constants.cNetCDF;
import org.gwaspi.operations.NetCdfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class DumpNCMatrix {

	private static final Logger log = LoggerFactory.getLogger(DumpNCMatrix.class);

	public static void main(String[] args) throws Exception {
		printSystemOutMatrix("/media/data/work/GWASpi/genotypes/STUDY_8/GT_51310170721CEST.nc"); // XXX system dependent path
	}

	public static void printSystemOutMatrix(String matrixPath) throws IOException, InvalidRangeException {
		NetcdfFile ncfile = NetcdfFile.open(matrixPath);
		Map<String, char[]> markerIdSetMap = new LinkedHashMap<String, char[]>();
		Map<String, Object> sampleIdSetMap = new LinkedHashMap<String, Object>();

		FileWriter dumpFW = new FileWriter("/media/data/work/GWASpi/export/NCDump.txt"); // XXX system dependent path
		BufferedWriter dumpBW = new BufferedWriter(dumpFW);

		// GET MARKERSET
		Variable var = ncfile.findVariable(cNetCDF.Variables.VAR_MARKERSET);
		DataType dataType = var.getDataType();
		int[] varShape = var.getShape();

		try {
			int markerSetSize = varShape[0];

			if (dataType == DataType.CHAR) {
				ArrayChar.D2 markerSetAC = (ArrayChar.D2) var.read("(0:" + (markerSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");
				markerIdSetMap = NetCdfUtils.writeD2ArrayCharToMapKeys(markerSetAC, null);
			}
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		// GET SAMPLESET
		var = ncfile.findVariable(cNetCDF.Variables.VAR_SAMPLE_KEY);
		varShape = var.getShape();
		Dimension markerSetDim = ncfile.findDimension(cNetCDF.Dimensions.DIM_SAMPLESET);

		try {
			int sampleSetSize = markerSetDim.getLength();
			ArrayChar.D2 sampleSetAC = (ArrayChar.D2) var.read("(0:" + (sampleSetSize - 1) + ":1, 0:" + (varShape[1] - 1) + ":1)");

			sampleIdSetMap = NetCdfUtils.writeD2ArrayCharToMapKeys(sampleSetAC, null);
		} catch (IOException ex) {
			log.error("Cannot read data", ex);
		} catch (InvalidRangeException ex) {
			log.error("Cannot read data", ex);
		}

		// BUILD MARKERSET HEADER
		StringBuilder headerSB = new StringBuilder();
		for (String markerId : markerIdSetMap.keySet()) {
			headerSB.append("\t").append(markerId.toString());
		}
		dumpBW.append(headerSB.toString());
		dumpBW.append("\n");

		// ITERATE READ OF MARKERSETMap BY SAMPLE
		int sampleNb = 0;
		for (String sampleId : sampleIdSetMap.keySet()) {
			StringBuilder sampleLineSB = new StringBuilder(sampleId.toString());

			Variable genotypes = ncfile.findVariable(cNetCDF.Variables.VAR_GENOTYPES);

			try {
				varShape = genotypes.getShape();

				ArrayChar.D3 gt_ACD3 = (ArrayChar.D3) genotypes.read("(" + sampleNb + ":" + sampleNb + ":1, 0:" + (varShape[1] - 1) + ":1, 0:" + (varShape[2] - 1) + ":1)");
				ArrayChar.D2 gt_ACD2 = (ArrayChar.D2) gt_ACD3.reduce();

				NetCdfUtils.writeD2ArrayCharToMapValues(gt_ACD2, markerIdSetMap);

				for (Object alleles : markerIdSetMap.values()) {
					sampleLineSB.append("\t").append(alleles);
				}
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}

			dumpBW.append(sampleLineSB.toString());
			dumpBW.append("\n");
			sampleNb++;
		}

		dumpBW.close();
		dumpFW.close();
	}
}
