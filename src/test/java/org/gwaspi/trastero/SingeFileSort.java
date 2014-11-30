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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import org.gwaspi.constants.GlobalConstants;
import org.gwaspi.constants.ImportConstants.Separators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingeFileSort {

	private static final Logger log = LoggerFactory.getLogger(SingeFileSort.class);

	private SingeFileSort() {
	}

	public static String sortFile(String filePath, int compareIndex) throws IOException {
		File tempSorted;

		try {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			ArrayList<String[]> filerows = new ArrayList<String[]>();
			Random generator = new Random();
			int rnd = Math.abs(generator.nextInt());
			tempSorted = new File(org.gwaspi.global.Config.getConfigValue(
					GlobalConstants.SORT_SINGLE_DIR_CONFIG,
					GlobalConstants.USER_DIR_DEFAULT)
					+ "/" + rnd + ".csv");
			FileWriter fw = new FileWriter(tempSorted);
			BufferedWriter bw = new BufferedWriter(fw);
			String[] header;

			boolean someFileStillHasRows = false;

			// get each one past the header
			header = br.readLine().split(Separators.separators_CommaTab_rgxp);
			bw.write(flattenArray(header, ",") + "\n");

			// get the first row
			String line = br.readLine();
			if (line != null) {
				filerows.add(line.split(Separators.separators_CommaTab_rgxp));
				someFileStillHasRows = true;
			} else {
				filerows.add(null);
			}


			String[] row;
			int cnt = 0;
			while (someFileStillHasRows) {
				String min;
				int minIndex;

				row = filerows.get(0);
				if (row != null) {
					min = row[compareIndex];
					minIndex = 0;
				} else {
					min = null;
					minIndex = -1;
				}

				// check which one is min
				for (int i = 1; i < filerows.size(); i++) {
					row = filerows.get(i);
					if (min != null) {

						if (row != null && row[compareIndex].compareTo(min) <= 0) {
							minIndex = i;
							min = filerows.get(i)[compareIndex];
						}
					} else {
						if (row != null) {
							min = row[compareIndex];
							minIndex = i;
						}
					}
				}

				if (minIndex < 0) {
					someFileStillHasRows = false;
				} else {
					// write to the sorted file
					bw.append(flattenArray(filerows.get(minIndex), ",") + "\n");

					// get another row from the file that had the min
					line = br.readLine();
					if (line != null) {
						filerows.set(minIndex, line.split(Separators.separators_CommaTab_rgxp));
					} else {
						filerows.set(minIndex, null);
					}
				}
				// check if one still has rows
				for (final String[] filerow : filerows) {
					someFileStillHasRows = false;
					if (filerow != null) {
						if (minIndex < 0) {
							//log.trace("mindex < 0 and found row not null" + filerows.get(i).toString());
							System.exit(-1);
						}
						someFileStillHasRows = true;
						break;
					}
				}

				// check the actual files one more time
				if (!someFileStillHasRows) {

					// write the last one not covered above
					for (int i = 0; i < filerows.size(); i++) {
						if (filerows.get(i) == null) {
							line = br.readLine();
							if (line != null) {
								someFileStillHasRows = true;
								filerows.set(i, line.split(Separators.separators_CommaTab_rgxp));
							}
						}

					}
				}

			}

			// close all the files
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (Exception ex) {
			log.error(null, ex);
			System.exit(-1);
			return null;
		}

		return tempSorted.getPath();
	}

	private static String flattenArray(String[] a, String separator) {
		StringBuilder result = new StringBuilder(a[0]);
		for (int i = 1; i < Array.getLength(a); i++) {
			result.append(separator);
			result.append(a[i]);
		}
		return result.toString();
	}
}
