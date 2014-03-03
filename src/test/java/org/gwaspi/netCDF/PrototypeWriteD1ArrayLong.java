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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class PrototypeWriteD1ArrayLong {

	private static final Logger log = LoggerFactory.getLogger(PrototypeWriteD1ArrayLong.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc"; // XXX system dependent path
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

		// add dimensions
		Dimension markersDim = ncfile.addDimension("markers", 100);

		List<Dimension> positionSpace = new ArrayList<Dimension>();
		positionSpace.add(markersDim);

		// define Variable
		ncfile.addVariable("marker_position_int", DataType.INT, positionSpace);

		// create the file
		try {
			ncfile.create();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		// FILL'ER UP!
//		List<Dimension> dims = ncfile.getDimensions();
//		Dimension samplesDim = dims.get(0);
//		Dimension markersDim = dims.get(1);
//		Dimension markerSpanDim = dims.get(2);

		ArrayInt intArray = new ArrayInt.D1(markersDim.getLength());
		int i;
		Index ima = intArray.getIndex();

		int method = 1;
		switch (method) {
			case 1:
				// METHOD 1: Feed the complete genotype in one go
				Random generator = new Random();
				for (i = 0; i < markersDim.getLength(); i++) {
					int rnd = generator.nextInt();
					intArray.setInt(ima.set(i), rnd);
//					log.info("SNP: {}", i);
				}
				break;
			case 3:
				// METHOD 3: One sample at a time -> feed in all snps
				break;
		}

		int[] offsetOrigin = new int[1]; // 0,0
		try {
			ncfile.write("marker_position_int", offsetOrigin, intArray);
			//ncfile.write("genotype", origin, A);
		} catch (IOException ex) {
			log.error("Failed writing file", ex);
		} catch (InvalidRangeException ex) {
			log.error(null, ex);
		}

		// close the file
		try {
			ncfile.close();
		} catch (IOException ex) {
			log.error("Failed closing file " + ncfile.getLocation(), ex);
		}
	}
}
