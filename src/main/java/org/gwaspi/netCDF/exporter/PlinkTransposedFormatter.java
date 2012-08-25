package org.gwaspi.netCDF.exporter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.gwaspi.model.OperationsList;
import org.gwaspi.netCDF.markers.MarkerSet_opt;
import org.gwaspi.netCDF.matrices.MatrixMetadata;
import org.gwaspi.reports.GatherQAMarkersData;
import org.gwaspi.samples.SampleSet;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class PlinkTransposedFormatter implements Formatter {

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
			//<editor-fold defaultstate="collapsed" desc="TPED FILE">
			FileWriter tpedFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tped");
			BufferedWriter tpedBW = new BufferedWriter(tpedFW);

			//TPED files:
			//chromosome (1-22, X(23), Y(24), XY(25), MT(26) or 0 if unplaced)
			//rs# or snp identifier
			//Genetic distance (morgans)
			//Base-pair position (bp units)
			//Genotypes

			//PURGE MARKERSET
			rdMarkerSet.fillInitLHMWithMyValue("");

			//MARKERSET CHROMOSOME
			rdMarkerSet.fillInitLHMWithVariable(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_CHR);

			//MARKERSET RSID
			rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_RSID, sep);

			//DEFAULT GENETIC DISTANCE = 0
			for (Iterator<String> it = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				String key = it.next();
				StringBuilder value = new StringBuilder(rdMarkerSet.getMarkerIdSetLHM().get(key).toString());
				value.append(sep);
				value.append("0");
				rdMarkerSet.getMarkerIdSetLHM().put(key, value);
			}

			//MARKERSET POSITION
			rdMarkerSet.appendVariableToMarkerSetLHMValue(org.gwaspi.constants.cNetCDF.Variables.VAR_MARKERS_POS, sep);

			//Iterate through markerset
			int markerNb = 0;
			for (Iterator it = rdMarkerSet.getMarkerIdSetLHM().keySet().iterator(); it.hasNext();) {
				StringBuilder line = new StringBuilder();
				Object markerId = it.next();
				Object pos = rdMarkerSet.getMarkerIdSetLHM().get(markerId);

				//Iterate through sampleset
				StringBuilder genotypes = new StringBuilder();
				rdSampleSetMap = rdSampleSet.readAllSamplesGTsFromCurrentMarkerToLHM(rdNcFile, rdSampleSetMap, markerNb);
				for (Iterator it2 = rdSampleSetMap.keySet().iterator(); it2.hasNext();) {
					Object sampleId = it2.next();
					byte[] tempGT = (byte[]) rdSampleSetMap.get(sampleId);
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[0]}));
					genotypes.append(sep);
					genotypes.append(new String(new byte[]{tempGT[1]}));
				}

				line.append(pos.toString());
				line.append(genotypes);

				tpedBW.append(line);
				tpedBW.append("\n");
				markerNb++;
			}

			System.out.println("Markers exported to tped:" + markerNb);

			tpedBW.close();
			tpedFW.close();

			//</editor-fold>

			//<editor-fold defaultstate="collapsed" desc="TFAM FILE">
			FileWriter tfamFW = new FileWriter(exportDir.getPath() + "/" + rdMatrixMetadata.getMatrixFriendlyName() + ".tfam");
			BufferedWriter tfamBW = new BufferedWriter(tfamFW);

			//Iterate through all samples
			int sampleNb = 0;
			for (Iterator<String> it = rdSampleSetMap.keySet().iterator(); it.hasNext();) {
				String sampleId = it.next();

				HashMap sampleInfo = Utils.getCurrentSampleFormattedInfo(sampleId, rdMatrixMetadata.getStudyId());

				String familyId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FAMILY_ID).toString();
				String fatherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_FATHER_ID).toString();
				String motherId = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_MOTHER_ID).toString();
				String sex = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_SEX).toString();
				String affection = sampleInfo.get(org.gwaspi.constants.cDBSamples.f_AFFECTION).toString();

				//TFAM files
				//Family ID
				//Individual ID
				//Paternal ID
				//Maternal ID
				//Sex (1=male; 2=female; other=unknown)
				//Affection

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


				tfamBW.append(line);
				tfamBW.append("\n");
				tfamBW.flush();

				sampleNb++;
				if (sampleNb % 100 == 0) {
					System.out.println("Samples exported:" + sampleNb);
				}

			}
			tfamBW.close();
			tfamFW.close();

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
