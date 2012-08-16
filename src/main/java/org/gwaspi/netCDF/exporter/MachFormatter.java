package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cNetCDF;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.gwaspi.netCDF.markers.MarkerSet;
import org.gwaspi.netCDF.matrices.*;
import ucar.nc2.*;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MachFormatter {

	protected static String sep = org.gwaspi.constants.cExport.separator_MACH;
	protected static MatrixMetadata rdMatrixMetadata;
	protected static NetcdfFile rdNcFile;
	protected static MarkerSet rdMarkerSet;
	protected static LinkedHashMap rdMarkerIdSetLHM;
	protected static LinkedHashMap rdSampleSetLHM;
	protected static File exportDir;

	public static boolean exportToMach(String exportPath,
			MatrixMetadata _rdMatrixMetadata,
			MarkerSet _rdMarkerSet,
			LinkedHashMap _rdMarkerIdSetLHM,
			LinkedHashMap _rdSampleSetLHM) throws IOException {

		exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		rdMatrixMetadata = _rdMatrixMetadata;
		rdMarkerSet = _rdMarkerSet;

		rdMarkerIdSetLHM = _rdMarkerIdSetLHM;
		rdSampleSetLHM = _rdSampleSetLHM;

		boolean result = false;
		rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		try {

			//FIND START AND END MARKERS BY CHROMOSOME
			rdMarkerIdSetLHM = rdMarkerSet.fillMarkerSetLHMWithVariable(rdNcFile, cNetCDF.Variables.VAR_MARKERS_CHR);
			LinkedHashMap chrMarkerSetLHM = new LinkedHashMap();
			chrMarkerSetLHM.putAll(rdMarkerIdSetLHM);
			String tmpChr = "";
			int start = 0;
			int end = 0;
			for (Iterator it = chrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object markerId = it.next();
				String value = chrMarkerSetLHM.get(markerId).toString();
				if (!value.equals(tmpChr)) {
					if (start != end) {
						exportChromosomeToMped(tmpChr, start, end - 1);
						exportChromosomeToDat(tmpChr, start, end - 1);
						start = end;
					}
					tmpChr = value;
				}
				end++;
			}
			exportChromosomeToMped(tmpChr, start, end);
			exportChromosomeToDat(tmpChr, start, end - 1);


			result = true;
		} catch (IOException iOException) {
		} finally {
			if (null != rdNcFile) {
				try {
					rdNcFile.close();
				} catch (IOException ioe) {
					System.out.println("Cannot close file: " + ioe);
				}
			}
		}


		return result;
	}

	public static void exportChromosomeToMped(String chr, int startPos, int endPos) throws IOException {

		FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".mped");
		BufferedWriter pedBW = new BufferedWriter(pedFW);

		//Iterate through all samples
		int sampleNb = 0;
		for (Iterator it = rdSampleSetLHM.keySet().iterator(); it.hasNext();) {
			String sampleId = it.next().toString();

			HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());
			String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
			String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
			String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
			String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
			if (sex.equals("1")) {
				sex = "M";
			}
			if (sex.equals("2")) {
				sex = "F";
			}

			//Iterate through all markers
			rdMarkerIdSetLHM = rdMarkerSet.readAllMarkersGTsForCurrentSampleToLHM(rdNcFile, rdMarkerIdSetLHM, sampleNb);
			StringBuilder genotypes = new StringBuilder();
			int markerNb = 0;
			for (Iterator it2 = rdMarkerIdSetLHM.keySet().iterator(); it2.hasNext();) {
				Object markerId = it2.next();
				if (markerNb >= startPos) {
					if (markerNb <= endPos) {
						byte[] tempGT = (byte[]) rdMarkerIdSetLHM.get(markerId);
						genotypes.append(sep);
						genotypes.append(new String(new byte[]{tempGT[0]}));
						genotypes.append(sep);
						genotypes.append(new String(new byte[]{tempGT[1]}));
					}
				}
				markerNb++;
			}


			//Family ID
			//Individual ID
			//Paternal ID
			//Maternal ID
			//Sex (1=male; 2=female; other=unknown)
			//Genotypes

			StringBuilder line = new StringBuilder();
			line.append(familyId);
			line.append(sep);
			line.append(sampleId);
			line.append(sep);
			line.append(fatherId);
			line.append(sep);
			line.append(motherId);
			line.append(sep);
			line.append(sex);
			line.append(genotypes);

			pedBW.append(line);
			pedBW.append("\n");
			pedBW.flush();

			sampleNb++;
		}
		System.out.println("Samples exported to chr" + chr + " MPED file: " + sampleNb);
		pedBW.close();
		pedFW.close();


	}

	public static void exportChromosomeToDat(String chr, int startPos, int endPos) throws IOException {

		FileWriter datFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".dat");
		BufferedWriter datBW = new BufferedWriter(datFW);

		//DAT files
		//     "M" indicates a marker
		//     rs# or marker identifier

		//PURGE MARKERSET
		rdMarkerIdSetLHM = rdMarkerSet.fillLHMWithDefaultValue(rdMarkerIdSetLHM, "");


		//MARKERSET RSID
		rdMarkerIdSetLHM = rdMarkerSet.appendVariableToMarkerSetLHMValue(rdNcFile, org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);


		int markerNb = 0;
		for (Iterator it = rdMarkerIdSetLHM.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			if (markerNb >= startPos) {
				if (markerNb <= endPos) {
					//CHECK IF rsID available
					String value = rdMarkerIdSetLHM.get(key).toString();
					String markerId = key.toString();
					if (!value.isEmpty()) {
						markerId = value;
					}

					datBW.append("M" + sep);
					datBW.append(markerId);
					datBW.append("\n");
				}
			}
			markerNb++;
		}

		System.out.println("Markers exported to chr" + chr + " DAT file: " + (endPos + 1 - startPos));

		datBW.close();
		datFW.close();


	}
}
