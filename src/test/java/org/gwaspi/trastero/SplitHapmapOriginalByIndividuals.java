package org.gwaspi.trastero;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SplitHapmapOriginalByIndividuals {

	protected static String hapmapBigFile = "/media/disk/Fernando/hapmap_orig/hapmapGenotypes_orden_OK_SORTED.txt";

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		processData();
	}

	public static void processData() throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		FileReader bigHapmapFileReader = new FileReader(new File(hapmapBigFile));
		BufferedReader bigHapmapBufferReader = new BufferedReader(bigHapmapFileReader);

		String currentSampleId = "";
		String l = null;
		boolean header = true;
		int hapmaprowcount = 0;
		int samplecount = 0;
		int genotypeCount = 0;
		while (header) { //ignoring top empty line
			l = bigHapmapBufferReader.readLine();
			header = false;
		}


		FileWriter fw = null;
		BufferedWriter bw = null;

		while ((l = bigHapmapBufferReader.readLine()) != null) {

			hapmaprowcount++;
			String[] cVals = null;
			cVals = l.split("[ \t,]");

			if (currentSampleId.equals(cVals[0])) { //same sample => engross current Sample's genotype file
				genotypeCount++;
				bw.append(l + "\n");
			} else { //encountered a new sampleId in bigHapmapFile

				samplecount++;
				if (!currentSampleId.isEmpty()) { //obviate first time round when starting application
					//Close previous Sample's files
					if (fw != null) {
						bw.flush();
						fw.flush();
					}
				}
				currentSampleId = cVals[0];
				fw = new FileWriter("/media/disk/Fernando/hapmap_orig/by_individuals/" + currentSampleId + ".txt");
				bw = new BufferedWriter(fw);

				System.out.println("SampleId: " + currentSampleId + " count=" + samplecount);
				//WRITE TO FILE
				bw.append(l + "\n");

			}
		}

		bw.close();
		fw.close();

	}
}
