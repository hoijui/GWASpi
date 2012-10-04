package org.gwaspi.formats;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class ReadBinaryPlink {

	private static final Logger log
			= LoggerFactory.getLogger(ReadBinaryPlink.class);

	private static final String inputBed = "/media/data/work/GWASpi/input/Plink/mi_input.bed"; // XXX system dependent path
	private static final String inputBim = "/media/data/work/GWASpi/input/Plink/mi_input.bim"; // XXX system dependent path
	private static final String inputFam = "/media/data/work/GWASpi/input/Plink/mi_input.fam"; // XXX system dependent path

	public static void main(String[] args) throws IOException {
		// Wrap the FileInputStream with a DataInputStream
		FileInputStream file_input = new FileInputStream(new File(inputBed));
		DataInputStream data_in = new DataInputStream(file_input);

		// SKIP HEADER
		data_in.readByte();
		data_in.readByte();
		data_in.readByte();

		int sampleNb = 15;
		int byteToMunch;
		if (sampleNb % 8 == 0) {
			byteToMunch = (sampleNb / 8) * 2;
		} else {
			byteToMunch = (Math.round(sampleNb / 8) + 1) * 2;
		}
		while (true) {
			try {
				log.info("New SNP");
				int sampleCount = 1;
				for (int j = 0; j < byteToMunch; j++) {
					byte b_data = data_in.readByte();

					for (int i = 0; i < 8; i++) {
						if (sampleCount <= (sampleNb * 2)) {
//							log.trace("{}", isBitSet(b_data, i).toString());
							log.info("{}", translateBitSet(b_data, i));
							sampleCount++;
						}
					}

				}
			} catch (EOFException ex) {
				log.error(null, ex);
				break;
			}
		}
		data_in.close();
	}

	private static Boolean isBitSet(byte b, int bit) {
		return (b & (1 << bit)) != 0;
	}

	private static int translateBitSet(byte b, int bit) {
		int result;
		boolean by = (b & (1 << bit)) != 0;
		if (by) {
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}
}
