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

package org.gwaspi.trastero;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteBinaryPlink {

	private static final Logger log = LoggerFactory.getLogger(WriteBinaryPlink.class);

	private static final byte[] allBytes = new byte[64];
	private static final byte[] gtBytes = new byte[1];
	private static final String AA = "AA";
	private static final String AT = "AT";
	private static final String CC = "CC";
	private static final String CG = "CG";

	public static void main(String arg[]) {

		File file = new File("/media/data/work/GWASpi/export/STUDY_1/test.bed"); // XXX system dependent path

		// Now write the data array to the file.
		try {
			// Create an output stream to the file.
			FileOutputStream file_output = new FileOutputStream(file);
			// Wrap the FileOutputStream with a DataOutputStream
			DataOutputStream data_out = new DataOutputStream(file_output);

			data_out.write(114);
			data_out.write(27);
			data_out.write(1);

			Map<String, Byte[]> sampleSetMap = new LinkedHashMap<String, Byte[]>();
			sampleSetMap.put("smpl01", new Byte[]{84, 84}); //65,84
			sampleSetMap.put("smpl02", new Byte[]{84, 84});
			sampleSetMap.put("smpl03", new Byte[]{84, 84});
			sampleSetMap.put("smpl04", new Byte[]{84, 84});

			StringBuilder tetraGTs = new StringBuilder("");
			for (Map.Entry<String, Byte[]> entry : sampleSetMap.entrySet()) {
				String key = entry.getKey();
				Byte[] value = entry.getValue();

				tetraGTs.insert(0, translateTo00011011(value, "A", "T"));
//				tetraGTs.append(translateTo00011011(value, "A", "T"));
			}
			tetraGTs.reverse();

			int number = Integer.parseInt(tetraGTs.toString(), 2);
			byte[] tetraGT = new byte[]{(byte) number};

			// arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
			System.arraycopy(tetraGT,
					0,
					allBytes,
					0,
					1);

			number = Integer.parseInt("11111111", 2);
			tetraGT = new byte[]{(byte) number};

			System.arraycopy(tetraGT,
					0,
					allBytes,
					1,
					1);

			data_out.write(allBytes, 0, allBytes.length);

			// Close file when finished with it..
			file_output.close();
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}

	protected static String translateTo00011011(Byte[] gt, String minorAllele, String majorAllele) {
		String result = "10"; // missing

		String allele1 = new String(new byte[]{gt[0]});
		String allele2 = new String(new byte[]{gt[1]});

		if (allele1.equals(minorAllele)) {
			if (allele2.equals(minorAllele)) {
				result = "00"; // Homozygous for minor allele
			} else {
				result = "01"; // Heterozygote
			}
		} else if (allele1.equals(majorAllele)) {
			if (allele2.equals(majorAllele)) {
				result = "11"; // Homozygous for major allele
			} else {
				result = "01"; // Heterozygote
			}
		}

		return result;
	}
}