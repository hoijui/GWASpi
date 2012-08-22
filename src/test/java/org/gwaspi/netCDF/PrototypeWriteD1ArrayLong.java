package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import ucar.ma2.*;
import ucar.nc2.*;

public class PrototypeWriteD1ArrayLong {

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc";
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

		// add dimensions
		Dimension markersDim = ncfile.addDimension("markers", 100);

		ArrayList positionSpace = new ArrayList();
		positionSpace.add(markersDim);

		// define Variable
		ncfile.addVariable("marker_position_int", DataType.INT, positionSpace);

		// create the file
		try {
			ncfile.create();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}

		////////////// FILL'ER UP! ////////////////
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
					int rnd = generator.nextInt();;
					intArray.setInt(ima.set(i), rnd);
					//System.out.println("SNP: "+i);

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
		} catch (IOException e) {
			System.err.println("ERROR writing file");
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		// close the file
		try {
			ncfile.close();
		} catch (IOException e) {
			System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
		}
	}
}
