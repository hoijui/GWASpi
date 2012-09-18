package org.gwaspi.netCDF;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NCdump;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class PrototypeReadNetcdf {

	private final static Logger log = LoggerFactory.getLogger(PrototypeReadNetcdf.class);

	public static void main(String[] arg) throws InvalidRangeException, IOException {

		String filename = "/media/data/work/moapi/genotypes/prototype.nc"; // XXX system dependent path
		NetcdfFile ncfile = null;

		int gtSpan = 2;
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		for (int i = 0; i < 10; i++) {
			map.put(i, "00");
		}

		try {
			ncfile = NetcdfFile.open(filename);

			String varName = "genotypes";
			Variable genotypes = ncfile.findVariable(varName);
			if (null == genotypes) {
				return;
			}
			try {
				//Array gt = genotypes.read("0:0:1, 0:9:1, 0:1:1"); // sample 1, snp 0 - 10, alleles 0+1
				ArrayChar.D3 gt = (ArrayChar.D3) genotypes.read("0:0:1, 0:9:1, 0:1:1");
				NCdump.printArray(gt, varName, System.out, null);

				Map<Object, Object> filledMap = fillLinkedHashMap(map, gt, gtSpan);

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
					log.error("Cannot close file", ex);
				}
			}
		}
	}

	private static Map<Object, Object> fillLinkedHashMap(Map<Object, Object> map, Array inputArray, int gtSpan) {
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
		return map;
	}
}
