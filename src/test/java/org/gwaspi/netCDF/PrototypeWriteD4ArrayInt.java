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

	private final static Logger log = LoggerFactory.getLogger(PrototypeWriteD4ArrayInt.class);

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
