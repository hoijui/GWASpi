package org.gwaspi.netCDF;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class PrototypeReadD4ArrayInt {

	private static final Logger log = LoggerFactory.getLogger(PrototypeReadD4ArrayInt.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc"; // XXX system dependent path
		NetcdfFile ncfile = null;

		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		for (int i = 0; i < 10; i++) {
			map.put(i, "00");
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
				StringWriter arrStr = new StringWriter();
				//Array gt = genotypes.read("0:0:1, 0:9:1, 0:1:1"); //sample 1, snp 0 - 10, alleles 0+1
				ArrayInt.D2 rdIntArray = (ArrayInt.D2) contingencies.read("0:" + (markersDim.getLength() - 1) + ":1, 0:" + (boxesDim.getLength() - 1) + ":1");
				NCdumpW.printArray(rdIntArray, varName, new PrintWriter(arrStr), null);
				log.info(arrStr.getBuffer().toString());

				ArrayInt wrIntArray = new ArrayInt(new int[]{1, boxesDim.getLength()});
				ArrayInt.D2.arraycopy(rdIntArray, 0, wrIntArray, 0, boxesDim.getLength());
				arrStr = new StringWriter();
				NCdumpW.printArray(wrIntArray, varName, new PrintWriter(arrStr), null);
				log.info(arrStr.getBuffer().toString());

				int[] test = (int[]) wrIntArray.copyTo1DJavaArray();

				//Map<Object, Object> filledMap = fillMap(map, gt, gtSpan);

				int stopme = 0;
			} catch (IOException ex) {
				log.error("Cannot read data", ex);
			} catch (InvalidRangeException ex) {
				log.error("Cannot read data", ex);
			}
		} catch (IOException ex) {
			log.error("Cannot open file", ex);
		} finally {
			if (null != ncfile) {
				try {
					ncfile.close();
				} catch (IOException ex) {
					log.warn("Cannot close file", ex);
				}
			}
		}
	}

	public static void fillMap(Map<Object, Object> map, Array inputArray, int gtSpan) {
		StringBuffer alleles = new StringBuffer("");
		int mapIndex = 0;
		int alleleCount = 0;
		for (int i = 0; i < inputArray.getSize(); i++) {
			if (alleleCount == gtSpan) {
				map.put(mapIndex, alleles);
				alleles = new StringBuffer("");
				alleleCount = 0;
				mapIndex++;
			}
			char c = inputArray.getChar(i);
			alleles.append(c);
			alleleCount++;
		}
		map.put(mapIndex, alleles);
	}
}
