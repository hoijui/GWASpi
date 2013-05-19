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
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class PrototypeWriteD4ArrayInt {

	private static final Logger log = LoggerFactory.getLogger(PrototypeWriteD4ArrayInt.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc"; // XXX system dependent path
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

		// add dimensions
		Dimension markersDim = ncfile.addDimension("markers", 100);
		Dimension boxesDim = ncfile.addDimension("boxes", 4); // 0=>AA, 1=>Aa, 2=>aa, 3=>00
		ArrayList punettSpace = new ArrayList();
		punettSpace.add(markersDim);
		punettSpace.add(boxesDim);

		// define Variable
		ncfile.addVariable("contingencies", DataType.INT, punettSpace);

		// create the file
		try {
			ncfile.create();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		// FILL'ER UP!
		ArrayInt intArray = new ArrayInt.D2(markersDim.getLength(), boxesDim.getLength());
		int i, j;
		Index ima = intArray.getIndex();

		int method = 1;
		switch (method) {
			case 1:
				// METHOD 1: Feed the complete genotype in one go
				Random generator = new Random();
				for (i = 0; i < markersDim.getLength(); i++) {
					for (j = 0; j < boxesDim.getLength(); j++) {
						int rnd = generator.nextInt(50 * (j + 1));
						intArray.setInt(ima.set(i, j), rnd);
//						log.info("SNP: {}", i);
					}
				}
				break;
			case 3:
				// METHOD 3: One sample at a time -> feed in all snps
				break;
		}

		int[] offsetOrigin = new int[2]; // 0,0
		try {
			ncfile.write("contingencies", offsetOrigin, intArray);
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
