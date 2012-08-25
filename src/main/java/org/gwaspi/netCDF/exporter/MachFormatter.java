package org.gwaspi.netCDF.exporter;

import org.gwaspi.constants.cNetCDF;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class MachFormatter implements Formatter {

	private static final String SEP = org.gwaspi.constants.cExport.separator_MACH;

	public boolean export(
			String exportPath,
			MatrixMetadata rdMatrixMetadata,
			MarkerSet_opt rdMarkerSet,
			SampleSet rdSampleSet,
			Map<String, Object> rdSampleSetMap,
			String phenotype)
			throws IOException
	{
		File exportDir = new File(exportPath);
		if (!exportDir.exists() || !exportDir.isDirectory()) {
			return false;
		}

		boolean result = false;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());

		try {
			rdMarkerSet.initFullMarkerIdSetLHM();

			//FIND START AND END MARKERS BY CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
			LinkedHashMap chrMarkerSetLHM = new LinkedHashMap();
			chrMarkerSetLHM.putAll(rdMarkerSet.getMarkerIdSetLHM());
			String tmpChr = "";
			int start = 0;
			int end = 0;
			for (Iterator it = chrMarkerSetLHM.keySet().iterator(); it.hasNext();) {
				Object chrId = it.next();
				String chr = chrMarkerSetLHM.get(chrId).toString();
				if (!chr.equals(tmpChr)) {
					if (start != end) {
						exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end - 1);
						exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);
						start = end;
					}
					tmpChr = chr;
				}
				end++;
			}
			exportChromosomeToMped(exportDir, rdMatrixMetadata, rdMarkerSet, rdSampleSetMap, tmpChr, start, end);
			exportChromosomeToDat(exportDir, rdMatrixMetadata, rdMarkerSet, tmpChr, start, end - 1);


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

	public static void exportChromosomeToMped(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet_opt rdMarkerSet, Map<String, Object> rdSampleSetLHM, String chr, int startPos, int endPos) throws IOException {

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


			//Iterate through current chrl markers
			rdMarkerSet.initMarkerIdSetLHM(startPos, endPos);
			rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
			StringBuilder genotypes = new StringBuilder();
			int markerNb = 0;
			for (Iterator it2 = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it2.hasNext();) {
				Object markerId = it2.next();
				byte[] tempGT = (byte[]) rdMarkerSet.getMarkerIdSetLHM().get(markerId);
				genotypes.append(SEP);
				genotypes.append(new String(new byte[]{tempGT[0]}));
				genotypes.append(SEP);
				genotypes.append(new String(new byte[]{tempGT[1]}));
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
			line.append(SEP);
			line.append(sampleId);
			line.append(SEP);
			line.append(fatherId);
			line.append(SEP);
			line.append(motherId);
			line.append(SEP);
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

	public void exportChromosomeToDat(File exportDir, MatrixMetadata rdMatrixMetadata, MarkerSet_opt rdMarkerSet, String chr, int startPos, int endPos) throws IOException {

		FileWriter datFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + "_chr" + chr + ".dat");
		BufferedWriter datBW = new BufferedWriter(datFW);

		//DAT files
		//     "M" indicates a marker
		//     rs# or marker identifier

		//MARKERSET RSID
		rdMarkerSet.fillInitLHMWithVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID);

		//Iterate through current chr markers
		//INIT MARKERSET
		rdMarkerSet.initMarkerIdSetLHM(startPos, endPos);
		int markerNb = 0;
		for (Iterator it = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
			Object key = it.next();

			//CHECK IF rsID available
			String value = rdMarkerSet.getMarkerIdSetLHM().get(key).toString();
			String markerId = key.toString();
			if (!value.isEmpty()) {
				markerId = value;
			}

			datBW.append("M" + SEP);
			datBW.append(markerId);
			datBW.append("\n");

			markerNb++;
		}

		System.out.println("Markers exported to chr" + chr + " DAT file: " + (endPos + 1 - startPos));

		datBW.close();
		datFW.close();
	}
}
