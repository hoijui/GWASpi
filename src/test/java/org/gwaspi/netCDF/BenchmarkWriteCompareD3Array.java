package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

public class BenchmarkWriteCompareD3Array {

	private static final Logger log = LoggerFactory.getLogger(BenchmarkWriteCompareD3Array.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		int method = 2; // 1=int, 2=byte, 3=char
		int markerNb = 100000;
		String filename = "/media/data/work/GWASpi/genotypes/method" + method + "mk" + markerNb + ".nc"; // XXX system dependent path
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);

		// add dimensions
		//Dimension samplesDim = ncfile.addDimension("samples", 0, true, true, false); //UNLIMITED
		Dimension samplesDim = ncfile.addDimension("samples", 100);
		Dimension markersDim = ncfile.addDimension("markers", markerNb);
		Dimension allelesDim = ncfile.addDimension("alleles", 2);
		ArrayList gtSpace = new ArrayList();
		gtSpace.add(samplesDim);
		gtSpace.add(markersDim);
		gtSpace.add(allelesDim);

		// FILL'ER UP!
		int i, j, k;

		int[] offsetOrigin = new int[3]; // 0, 0, 0
		long timeAverage = 0;
		switch (method) {
			case 1: {
				Random generator = new Random();

				// Define Variable
				ncfile.addVariable("genotypes", DataType.INT, gtSpace);
				// Create the file
				try {
					ncfile.create();
				} catch (IOException ex) {
					log.error("Failed creating file " + ncfile.getLocation(), ex);
				}

				// Filler'up
				ArrayInt intArray = new ArrayInt.D3(samplesDim.getLength(), markersDim.getLength(), allelesDim.getLength());
				Index ima = intArray.getIndex();
				for (k = 0; k < 100; k++) { //Samples
					for (i = 0; i < markersDim.getLength(); i++) { //Markers
						for (j = 0; j < allelesDim.getLength(); j++) {  //Alleles
							int rnd = generator.nextInt(50 * (j + 1));
							intArray.setInt(ima.set(0, i, j), rnd);
						}
					}
					Date start = new Date();
					// Write
					try {
						//offsetOrigin[0] = k;
						ncfile.write("genotypes", offsetOrigin, intArray);
						//ncfile.write("genotype", origin, A);
					} catch (IOException ex) {
						log.error("Failed writing file", ex);
					} catch (InvalidRangeException ex) {
						log.error(null, ex);
					}
					Date end = new Date();
					long tmpTime = end.getTime() - start.getTime();
					timeAverage = ((timeAverage * k) + tmpTime) / (k + 1);
					if (k % 10 == 0) {
						log.info("Processing {}", k);
					}
				}
				log.info("Time average with int: {}", timeAverage);
				break;
			}
			case 2: {
				// Define Variable
				ncfile.addVariable("genotypes", DataType.BYTE, gtSpace);
				// Create the file
				try {
					ncfile.create();
				} catch (IOException ex) {
					log.error("Failed writing file " + ncfile.getLocation(), ex);
				}

				// Filler'up
				ArrayByte byteArray = new ArrayByte.D3(samplesDim.getLength(), markersDim.getLength(), allelesDim.getLength());
				Index ima = byteArray.getIndex();
				for (k = 0; k < 100; k++) { //Samples
					for (i = 0; i < markersDim.getLength(); i++) { //Markers
						for (j = 0; j < allelesDim.getLength(); j++) {  //Alleles
							String allele = "A";
							byteArray.setByte(ima.set(0, i, j), (byte) (allele.charAt(0)));
						}
					}
					// Write
					Date start = new Date();
					try {
						//offsetOrigin[0] = k;
						ncfile.write("genotypes", offsetOrigin, byteArray);
						//ncfile.write("genotype", origin, A);
					} catch (IOException ex) {
						log.error("Failed writing file", ex);
					} catch (InvalidRangeException ex) {
						log.error(null, ex);
					}
					Date end = new Date();
					long tmpTime = end.getTime() - start.getTime();
					timeAverage = ((timeAverage * k) + tmpTime) / (k + 1);
					if (k % 10 == 0) {
						log.info("Processing {}", k);
					}
				}
				log.info("Time average with int: {}", timeAverage);
				break;
			}
			case 3: {
				// Define Variable
				ncfile.addVariable("genotypes", DataType.CHAR, gtSpace);
				// Create the file
				try {
					ncfile.create();
				} catch (IOException ex) {
					log.error("Failed writing file " + ncfile.getLocation(), ex);
				}

				// Filler'up
				ArrayChar charArray = new ArrayChar.D3(samplesDim.getLength(), markersDim.getLength(), allelesDim.getLength());
				Index ima = charArray.getIndex();
				for (k = 0; k < 100; k++) { //Samples
					for (i = 0; i < markersDim.getLength(); i++) { //Markers
						charArray.setString(ima.set(0, i, 0), "AA");
					}
					// Write
					Date start = new Date();
					//offsetOrigin[0] = k;
					try {
						ncfile.write("genotypes", offsetOrigin, charArray);
						//ncfile.write("genotype", origin, A);
					} catch (IOException ex) {
						log.error("Failed writing file", ex);
					} catch (InvalidRangeException ex) {
						log.error(null, ex);
					}
					Date end = new Date();
					long tmpTime = end.getTime() - start.getTime();
					timeAverage = ((timeAverage * k) + tmpTime) / (k + 1);
					if (k % 10 == 0) {
						log.info("Processing {}", k);
					}
				}
				log.info("Time average with int: {}", timeAverage);
				break;
			}
		}

		// close the file
		try {
			ncfile.close();
		} catch (IOException ex) {
			log.error("Failed writing file " + ncfile.getLocation(), ex);
		}
	}
}
