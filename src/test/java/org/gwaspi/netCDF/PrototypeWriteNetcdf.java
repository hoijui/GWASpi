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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayChar;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class PrototypeWriteNetcdf {

	private static final Logger log = LoggerFactory.getLogger(PrototypeWriteNetcdf.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		NetcdfFileWriteable ncfile = org.gwaspi.netCDF.CreateNetcdf.setDimsAndAttributes(0,
				"INTERNAL",
				"test in PrototypeWriteNetcdf",
				"+/-",
				5,
				10);

		// create the file
		try {
			ncfile.create();
		} catch (IOException ex) {
			log.error("Failed creating file " + ncfile.getLocation(), ex);
		}

		// FILL'ER UP!
		List<Dimension> dims = ncfile.getDimensions();
		Dimension samplesDim = dims.get(0);
		Dimension markersDim = dims.get(1);
		Dimension markerSpanDim = dims.get(2);

		ArrayChar charArray = new ArrayChar.D3(samplesDim.getLength(), markersDim.getLength(), markerSpanDim.getLength());
		int i, j;
		Index ima = charArray.getIndex();

		int method = 1;
		switch (method) {
			case 1:
				// METHOD 1: Feed the complete genotype in one go
				for (i = 0; i < samplesDim.getLength(); i++) {
					for (j = 0; j < markersDim.getLength(); j++) {
						char c = (char) ((char) j + 65);
						String s = Character.toString(c) + Character.toString(c);
						charArray.setString(ima.set(i, j, 0), s);
						log.info("SNP: {}", i);
					}
				}
				break;
			case 2:
				// METHOD 2: One snp at a time -> feed in all samples
				for (i = 0; i < markersDim.getLength(); i++) {
					charArray.setString(ima.set(i, 0), "s" + i + "I0");
					log.info("SNP: {}", i);
				}
				break;
			case 3:
				// METHOD 3: One sample at a time -> feed in all snps
				break;
		}

		int[] offsetOrigin = new int[3]; // 0,0
		try {
			ncfile.write("genotypes", offsetOrigin, charArray);
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
