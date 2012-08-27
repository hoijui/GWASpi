package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ucar.ma2.*;
import ucar.nc2.*;

public class PrototypeReadD4ArrayInt {

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc";
		NetcdfFile ncfile = null;

		Map<Object, Object> lhm = new LinkedHashMap<Object, Object>();
		for (int i = 0; i < 10; i++) {
			lhm.put(i, "00");
		}

		try {
			ncfile = NetcdfFile.open(filename);
			List<Dimension> dims = ncfile.getDimensions();
			Dimension markersDim = dims.get(0);
			Dimension boxesDim = dims.get(1);

			String varName = "contingencies";
			Variable contingencies = ncfile.findVariable(varName);
			if (null == contingencies) {
				return;
			}
			try {
				//Array gt = genotypes.read("0:0:1, 0:9:1, 0:1:1"); //sample 1, snp 0 - 10, alleles 0+1
				ArrayInt.D2 rdIntArray = (ArrayInt.D2) contingencies.read("0:" + (markersDim.getLength() - 1) + ":1, 0:" + (boxesDim.getLength() - 1) + ":1");
				NCdump.printArray(rdIntArray, varName, System.out, null);

				ArrayInt wrIntArray = new ArrayInt(new int[]{1, boxesDim.getLength()});
				ArrayInt.D2.arraycopy(rdIntArray, 0, wrIntArray, 0, boxesDim.getLength());
				NCdump.printArray(wrIntArray, varName, System.out, null);

				int[] test = (int[]) wrIntArray.copyTo1DJavaArray();

				//Map<Object, Object> filledLhm = fillLinkedHashMap(lhm, gt, gtSpan);

				int stopme = 0;
			} catch (IOException ioe) {
				System.out.println("Cannot read data: " + ioe);
			} catch (InvalidRangeException e) {
				System.out.println("Cannot read data: " + e);
			}

		} catch (IOException ioe) {
			System.out.println("Cannot open file: " + ioe);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}
	}

	public static Map<Object, Object> fillLinkedHashMap(Map<Object, Object> lhm, Array inputArray, int gtSpan) {
		StringBuffer alleles = new StringBuffer("");
		int lhmIndex = 0;
		int alleleCount = 0;
		for (int i = 0; i < inputArray.getSize(); i++) {
			if (alleleCount == gtSpan) {
				lhm.put(lhmIndex, alleles);
				alleles = new StringBuffer("");
				alleleCount = 0;
				lhmIndex++;
			}
			char c = inputArray.getChar(i);
			alleles.append(c);
			alleleCount++;
		}
		lhm.put(lhmIndex, alleles);
		return lhm;
	}
}
