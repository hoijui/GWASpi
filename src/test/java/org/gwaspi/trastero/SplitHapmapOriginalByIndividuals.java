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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplitHapmapOriginalByIndividuals {

	private static final Logger log = LoggerFactory.getLogger(SplitHapmapOriginalByIndividuals.class);
	private static final String hapmapBigFile = "/media/disk/Fernando/hapmap_orig/hapmapGenotypes_orden_OK_SORTED.txt"; // HACK system dependent path

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		processData();
	}

	public static void processData() throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		FileReader bigHapmapFileReader = new FileReader(new File(hapmapBigFile));
		BufferedReader bigHapmapBufferReader = new BufferedReader(bigHapmapFileReader);

		String currentSampleId = "";
		boolean header = true;
		int hapmapRowCount = 0;
		int samplecount = 0;
		int genotypeCount = 0;
		while (header) { // ignoring top empty line
			bigHapmapBufferReader.readLine();
			header = false;
		}

		FileWriter fw = null;
		BufferedWriter bw = null;

		String l = bigHapmapBufferReader.readLine();
		while (l != null) {
			hapmapRowCount++;
			String[] cVals = l.split("[ \t,]");

			if (currentSampleId.equals(cVals[0])) { // same sample => engross current Sample's genotype file
				genotypeCount++;
				bw.append(l).append('\n');
			} else { // encountered a new sampleId in bigHapmapFile

				samplecount++;
				if (!currentSampleId.isEmpty()) { // obviate first time round when starting application
					// Close previous Sample's files
					if (fw != null) {
						bw.flush();
						fw.flush();
					}
				}
				currentSampleId = cVals[0];
				fw = new FileWriter("/media/disk/Fernando/hapmap_orig/by_individuals/" + currentSampleId + ".txt"); // XXX system dependent path
				bw = new BufferedWriter(fw);

				log.info("SampleId: {} count={}", currentSampleId, samplecount);
				// WRITE TO FILE
				bw.append(l).append('\n');
			}

			l = bigHapmapBufferReader.readLine();
		}

		bw.close();
		fw.close();
	}
}
