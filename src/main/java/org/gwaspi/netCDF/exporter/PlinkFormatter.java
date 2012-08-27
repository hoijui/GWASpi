package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
public class PlinkFormatter implements Formatter {

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
		String sep = org.gwaspi.constants.cExport.separator_PLINK;
		NetcdfFile rdNcFile = NetcdfFile.open(rdMatrixMetadata.getPathToMatrix());
		rdMarkerSet.initFullMarkerIdSetLHM();

		try {

			//<editor-fold defaultstate="collapsed" desc="PED FILE">
			FileWriter pedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".ped");
			BufferedWriter pedBW = new BufferedWriter(pedFW);

			//Iterate through all samples
			int sampleNb = 0;
			for (String sampleId : rdSampleSetMap.keySet()) {
				HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
				String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
				String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
				String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
				String affection = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();


				// Iterate through all markers
				rdMarkerSet.fillGTsForCurrentSampleIntoInitLHM(sampleNb);
				StringBuilder genotypes = new StringBuilder();
				for (Object value : rdMarkerSet.getMarkerIdSetLHM().values()) {
					byte[] tempGT = (byte[]) value;
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[0]}));
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[1]}));
				}

				//Family ID
				//Individual ID
				//Paternal ID
				//Maternal ID
				//Sex (1=male; 2=female; other=unknown)
				//Affection
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
				line.append(sep);
				line.append(affection);
				line.append(genotypes);

				pedBW.append(line);
				pedBW.append("\n");
				pedBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					System.out.println("Samples exported to PED file:" + sampleNb);
				}

			}
			System.out.println("Samples exported to PED file:" + sampleNb);
			pedBW.close();
			pedFW.close();

			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="MAP FILE">
			FileWriter mapFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".map");
			BufferedWriter mapBW = new BufferedWriter(mapFW);

			//MAP files
			//     chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//     rs# or snp identifier
			//     Genetic distance (morgans)
			//     Base-pair position (bp units)

			//PURGE MARKERSET
			rdMarkerSet.fillInitLHMWithMyValue("");

			//MARKERSET CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR);

			//MARKERSET RSID
			rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);

			//DEFAULT GENETIC DISTANCE = 0
			for (Map.Entry<String, Object> entry : rdMarkerSet.getMarkerIdSetLHM().entrySet()) {
				StringBuilder value = new StringBuilder(entry.getValue().toString());
				value.append(sep);
				value.append("0");
				entry.setValue(value); // FIXME use toString()?
			}

			//MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS, sep);
			int markerNb = 0;
			for (Object pos : rdMarkerSet.getMarkerIdSetLHM().values()) {
				mapBW.append(pos.toString());
				mapBW.append("\n");
				markerNb++;
			}

			System.out.println("Markers exported to MAP file:" + markerNb);

			mapBW.close();
			mapFW.close();

			//</editor-fold>

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
}
